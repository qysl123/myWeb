/*
 * E社区
 * Copyright (c) 2014 成都翼承科技 All Rights Reserved.
 */
package com.yc.eps.payment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yc.commons.AmountUtil;
import com.yc.commons.TimeUtil;
import com.yc.commons.Tools;
import com.yc.commons.Utility;
import com.yc.commons.mybatis.pagination.utils.RowBoundsUtil;
import com.yc.commons.mybatis.pagination.vo.Pagination;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.community.CommunityPO;
import com.yc.edsi.community.ICommunityService;
import com.yc.edsi.community.IPropertyService;
import com.yc.edsi.community.PropertyPO;
import com.yc.edsi.order.IOrderService;
import com.yc.edsi.payment.AdvertIncomePO;
import com.yc.edsi.payment.ISettleService;
import com.yc.edsi.payment.PlantformExportBean;
import com.yc.edsi.payment.PlantformSettleAdvertPO;
import com.yc.edsi.payment.PlantformSettleInfo;
import com.yc.edsi.payment.PlantformSettlePO;
import com.yc.edsi.payment.PlantformUnApply;
import com.yc.edsi.payment.SellerPaymentPO;
import com.yc.edsi.payment.SellerSettleDetailPO;
import com.yc.edsi.payment.SellerSettlePO;
import com.yc.edsi.seller.ISellerService;
import com.yc.edsi.seller.SellerPO;
import com.yc.edsi.seller.SellerProperty;
import com.yc.edsi.system.ISystemService;

/**
 * @author <a href="mailto:zhouc@yichenghome.com">Zhou Chao</a>
 * @version 2.0
 * @since 2015年1月29日
 */
@Service
public class SettleService implements ISettleService {
    @Resource
    private ISettleDao settleDao;

    @Resource
    private ICommunityService edsCommunityService;

    @Resource
    private ISystemService edsSystemService;

    @Resource
    private IPaymentDao paymentDao;

    @Resource
    private ISellerService edsSellerService;

    @Resource
    private IOrderService edsOrderService;

    @Resource
    private IPropertyService edsPropertyService;

    @Override
    public List<SellerSettlePO> getSellerWaitApplySettleList(Long sellerId) throws EdsiException {
        // 1.0 获取未结算的(按照月物业分组)
        List<SellerSettlePO> waitList = settleDao.getSellerWaitApplySettleList(sellerId);
        setCommunityInfo(waitList);
        return waitList;
    }

    private void setCommunityInfo(List<SellerSettlePO> ssList) throws EdsiException {
        if (Utility.isNotEmpty(ssList)) {
            Map<Long, CommunityPO> communityMap = new HashMap<Long, CommunityPO>();
            CommunityPO community = null;
            for (SellerSettlePO ss : ssList) {
                Long communityId = ss.getCommunityId();
                if (communityId == -1) {
                    ss.setCommunityName("翼社区平台");
                    continue;
                }
                if (communityMap.containsKey(communityId)) {
                    community = communityMap.get(communityId);
                } else {
                    community = edsCommunityService.getById(communityId);
                    communityMap.put(communityId, community);
                }
                ss.setCommunityName(community.getCommunityName());
            }
        }
    }

    @Override
    @Transactional
    public void approveSettle(Long sellerId, SellerSettlePO settle) throws EdsiException {
        Long communityId = settle.getCommunityId();
        String settleMonth = settle.getSettleMonth();
        // 1.0 获取结算申请信息
        SellerSettlePO approvSettle = getSellerWaitApplySettle(sellerId, communityId, settleMonth);
        // 2.0 获取商家交易结算信息
        List<SellerPaymentPO> sellerPayList = paymentDao.getWaitApplySettleDetail(sellerId, communityId,
                settleMonth);
        // 3.0 修改商家交易信息结算状态
        paymentDao.batchUpdateApplyStatus(sellerId, communityId, settleMonth);
        settleDao.batchUpdateApplyStatus(sellerId, communityId, settleMonth);// 修改商家交易基础统计信息结算状态
        // 4.0 插入结算申请信息
        paymentDao.insertSellerSettle(approvSettle);
        // 5.0 插入结算申请明细
        List<SellerSettleDetailPO> ssDetailList = new ArrayList<SellerSettleDetailPO>();
        if (Utility.isNotEmpty(sellerPayList)) {
            SellerSettleDetailPO ssd = null;
            for (SellerPaymentPO sp : sellerPayList) {
                ssd = new SellerSettleDetailPO(edsSystemService.getId(), approvSettle.getSellerSettleId(),
                        sp.getsPaymentId());
                ssDetailList.add(ssd);
            }
            paymentDao.batchInsertSettleDetail(ssDetailList);
        }
    }

    /**
     * 根据结算商家Id、物业Id、结算月份获取商家待结算信息
     * 
     * @param sellerId
     * @param communityId
     * @param settleMonth
     * @return
     * @throws EdsiException
     */
    private SellerSettlePO getSellerWaitApplySettle(Long sellerId, Long communityId, String settleMonth) throws EdsiException {
        SellerSettlePO sellerSettle = settleDao.getSellerWaitApplySettle(sellerId, communityId, settleMonth);
        // 设置结算状态
        sellerSettle.setSettleStatus(SellerSettlePO.SETTLE_STATUS_0);
        sellerSettle.setSellerSubmitTime(new Date());
        sellerSettle.setCreateTime(new Date());
        sellerSettle.setSellerSettleId(edsSystemService.getId());
        return sellerSettle;
    }

    @Override
    public Pagination getPlantformWaitApplySettle(String subcomId, Pagination pagination, PlantformUnApply unApply) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<PlantformUnApply> puaList = settleDao.getPlantformWaitApplySettle(subcomId, pagination, rb);
        fillCommunityInfo(puaList, PlantformSettlePO.PF_TYPE_EBP);
        pagination.setList(puaList);
        return pagination;
    }

    @Override
    public Pagination getEEPWaitApplySettle(Long communityId, Pagination pagination, PlantformUnApply unApply) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<PlantformUnApply> puaList = settleDao.getEEPWaitApplySettle(communityId, pagination, rb);
        fillCommunityInfo(puaList, PlantformSettlePO.PF_TYPE_EEP);
        pagination.setList(puaList);
        return pagination;
    }

    private void fillCommunityInfo(List<PlantformUnApply> puaList, String pfType) throws EdsiException {
        if (Utility.isNotEmpty(puaList)) {
            Map<Long, CommunityPO> communityMap = new HashMap<Long, CommunityPO>();
            Long communityId = null;
            CommunityPO community = null;
            for (PlantformUnApply pua : puaList) {
                // 1.0 设置物业信息
                communityId = pua.getCommunityId();
                if (communityMap.containsKey(communityId)) {
                    community = communityMap.get(communityId);
                } else {
                    community = edsCommunityService.getById(communityId);
                    communityMap.put(communityId, community);
                }
                pua.setCommunityName(community.getCommunityName());
                pua.setCommunityNo(community.getCommunityNo());
                pua.setCommunityPercent(community.getCommissionPercent() == null ? 0.00 : community
                        .getCommissionPercent());
                // 2.0 计算相应的收益
                pua.setAdvertProfit(pua.getAdvertProfit());
                pua.setCommissionProfit(AmountUtil.format(pua.getTcAmount(), 2));
                // 广告收益+佣金收益
                String totalProfit = Tools.add(pua.getAdvertProfit(), pua.getCommissionProfit());
                pua.setTotalProfit(totalProfit);
                Double plantformPercent = (1d - pua.getCommunityPercent());
                pua.setPlantformProfit(Tools.multiply(totalProfit, plantformPercent + "") + "");
                pua.setCommunityProfit(Tools.subtract(totalProfit, pua.getPlantformProfit()) + "");
                if (PlantformSettlePO.PF_TYPE_EBP.equals(pfType) && "2016-02".compareTo(pua.getSettleMonth()) < 0) {
                    pua.setPlantformProfit(Tools.add(pua.getPlantformProfit(), AmountUtil.format(pua.getCsAmount(), 2)));
                } else {
                    pua.setCsAmount(BigDecimal.ZERO);
                }
            }
        }
    }

    @Override
    public Pagination getPlatformSettleDetail(Long communityId, String settleMonth, Pagination pagination,
            SellerSettlePO sellerSettlePO) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<SellerSettlePO> ssList = settleDao.getPlatformSettleDetail(communityId, settleMonth, pagination, rb);
        setSellerInfo(ssList);
        setCommunityInfo(ssList);
        pagination.setList(ssList);
        return pagination;
    }

    private void setSellerInfo(List<SellerSettlePO> ssList) throws EdsiException {
        if (Utility.isNotEmpty(ssList)) {
            Map<Long, SellerPO> sellerMap = new HashMap<Long, SellerPO>();
            SellerPO seller = null;
            for (SellerSettlePO ss : ssList) {
                Long sellerId = ss.getSellerId();
                if (sellerMap.containsKey(sellerId)) {
                    seller = sellerMap.get(sellerId);
                } else {
                    seller = edsSellerService.getBaseSellerPO(sellerId);
                    sellerMap.put(sellerId, seller);
                }
                ss.setSellerName(seller.getSellerName());
                ss.setSellerNo(seller.getSellerNo());
            }
        }
    }

    @Override
    public SellerSettlePO getSellerSettle(Long sellerId, Long communityId, String settleMonth) throws EdsiException {
        SellerSettlePO settle = settleDao.getSellerSettle(sellerId, communityId, settleMonth);
        if (settle != null) {
            setSellerName(settle);
        }
        return settle;
    }

    private void setSellerName(SellerSettlePO ss) throws EdsiException {
        SellerPO sellerInfo = edsSellerService.getBaseSellerPO(ss.getSellerId());
        ss.setSellerName(sellerInfo.getSellerName());
        ss.setSellerNo(sellerInfo.getSellerNo());
    }

    @Override
    public Pagination getSellerSettleDetail(Long sellerId, Long communityId, String settleMonth, Pagination pagination,
            SellerSettleDetailPO ssd) throws EdsiException {
        List<SellerSettleDetailPO> ssdList = new ArrayList<SellerSettleDetailPO>();
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<SellerPaymentPO> spList = settleDao.getSellerSettleDetail(sellerId, communityId, settleMonth,
                pagination, rb);
        if (Utility.isNotEmpty(spList)) {
            Map<Long, PropertyPO> propMap = new HashMap<Long, PropertyPO>();
            for (SellerPaymentPO sp : spList) {
                ssdList.add(toSellerSettleDetail(sp, propMap));
            }
            pagination.setList(ssdList);
        }
        return pagination;
    }

    private SellerSettleDetailPO toSellerSettleDetail(SellerPaymentPO sp, Map<Long, PropertyPO> propMap) throws EdsiException {
        SellerSettleDetailPO ssp = new SellerSettleDetailPO();
        ssp = new SellerSettleDetailPO();
        ssp.setFinishTime(TimeUtil.parse(sp.getCreateTime(), TimeUtil.YYYY_MM_DD_HH_MM_SS));
        ssp.setOrderId(sp.getOrderId());
        ssp.setOwnerId(sp.getOwnerId());
        ssp.setReceivableAmount(new BigDecimal(sp.getAmount()));
        ssp.setSpaymentId(sp.getsPaymentId());
        ssp.setSellerSettleId(sp.getSellerSettleId());
        ssp.setPropertyId(sp.getPropertyId());
        setPropertyInfo(ssp, propMap);
        return ssp;
    }

    private void setPropertyInfo(SellerSettleDetailPO ssp, Map<Long, PropertyPO> propMap) throws EdsiException {
        Long propertyId = ssp.getPropertyId();
        PropertyPO pro = null;
        if (propMap.containsKey(propertyId)) {
            pro = propMap.get(propertyId);
        } else {// 查询
            pro = edsPropertyService.getProperty(propertyId);
            propMap.put(propertyId, pro);
        }
        if (pro != null) {
            ssp.setPropertyId(pro.getPropertyId());
            ssp.setPropertyName(pro.getPropertyName());
        }
    }

    @Override
    public List<SellerSettleDetailPO> getExportSettleDetail(Long sellerId, Long communityId, String settleMonth)
            throws EdsiException {
        List<SellerSettleDetailPO> ssdList = new ArrayList<SellerSettleDetailPO>();
        List<SellerPaymentPO> spList = settleDao.getSellerSettleDetail(sellerId, communityId, settleMonth);
        if (Utility.isNotEmpty(spList)) {
            Map<Long, PropertyPO> propMap = new HashMap<Long, PropertyPO>();
            for (SellerPaymentPO sp : spList) {
                ssdList.add(toSellerSettleDetail(sp, propMap));
            }
        }
        return ssdList;
    }

    @Override
    public List<PlantformSettlePO> getPfExportSettleData(String exportCode, String subcomId, Long communityId,
            PlantformSettlePO ps) throws EdsiException {
        return getPfExportSettle(exportCode, subcomId, communityId);
    }

    private List<PlantformSettlePO> getPfExportSettle(String exportCode, String subcomId, Long communityId)
            throws EdsiException {
        List<PlantformSettlePO> pfList = new ArrayList<PlantformSettlePO>();
        if ("0".equals(exportCode)) {
            pfList = getUnSubmitSettleList(subcomId);
        } else if ("1".equals(exportCode)) {
            pfList = settleDao.getPfHandleSettleApply(subcomId, communityId);
        } else if ("2".equals(exportCode)) {
            pfList = settleDao.getPfFinishSettleApply(subcomId, communityId);
        }
        setExportInfo(pfList);// 设置导出相关信息
        return pfList;
    }

    @Override
    public List<PlantformSettlePO> getEEPExportSettleData(String exportCode, String subcomId, Long communityId, PlantformSettlePO ps)
            throws EdsiException {
        return getEEPExportSettle(exportCode, subcomId, communityId);
    }

    private List<PlantformSettlePO> getEEPExportSettle(String exportCode, String subcomId, Long communityId)
            throws EdsiException {
        List<PlantformSettlePO> pfList = new ArrayList<PlantformSettlePO>();
        if ("0".equals(exportCode)) {
            pfList = getEEPUnSubmitSettleList(communityId);
        } else if ("1".equals(exportCode)) {
            pfList = paymentDao.getEEPHandleSettleApply(subcomId, communityId);
        } else if ("2".equals(exportCode)) {
            pfList = paymentDao.getEEPFinishSettleApply(subcomId, communityId);
        }
        setExportInfo(pfList);// 设置导出相关信息
        return pfList;
    }

    private void setExportInfo(List<PlantformSettlePO> pfList) {
        if (Utility.isNotEmpty(pfList)) {
            for (PlantformSettlePO pf : pfList) {
                pf.setCommissionProfitString(AmountUtil.format(pf.getCommissionProfit(), 2));
                pf.setAdvertProfitString(AmountUtil.format(pf.getAdvertProfit(), 2));
                pf.setTotalProfitString(AmountUtil.format(pf.getTotalProfit(), 2));
                pf.setPlantformProfitString(AmountUtil.format(pf.getPlantformProfit(), 2));
                pf.setCommunityProfitString(AmountUtil.format(pf.getCommunityProfit(), 2));
            }
        }
    }

    private List<PlantformSettlePO> getUnSubmitSettleList(String subcomId) throws EdsiException {
        List<PlantformSettlePO> psList = new ArrayList<PlantformSettlePO>();
        List<PlantformUnApply> puaList = settleDao.getPlantformWaitApplySettle(subcomId);
        if (Utility.isNotEmpty(puaList)) {
            fillCommunityInfo(puaList, PlantformSettlePO.PF_TYPE_EBP);
            convert2PlantformSettle(psList, puaList);
        }
        return psList;
    }

    private List<PlantformSettlePO> getEEPUnSubmitSettleList(Long communityId) throws EdsiException {
        List<PlantformSettlePO> psList = new ArrayList<PlantformSettlePO>();
        List<PlantformUnApply> puaList = settleDao.getEEPWaitApplySettle(communityId);
        if (Utility.isNotEmpty(puaList)) {
            fillCommunityInfo(puaList, PlantformSettlePO.PF_TYPE_EEP);
            convert2PlantformSettle(psList, puaList);
        }
        return psList;
    }

    private void convert2PlantformSettle(List<PlantformSettlePO> psList, List<PlantformUnApply> puaList) {
        PlantformSettlePO ps = null;
        for (PlantformUnApply pfua : puaList) {
            ps = new PlantformSettlePO();
            ps.setSettleMonth(pfua.getSettleMonth());
            ps.setAdvertProfit(new BigDecimal(pfua.getAdvertProfit()));
            ps.setCanSettleAmount(pfua.getCsAmount());
            ps.setCanSettleNum(pfua.getCsNum());
            ps.setCommissionProfit(new BigDecimal(pfua.getCommissionProfit()));
            ps.setCommunityId(pfua.getCommunityId());
            ps.setCommunityName(pfua.getCommunityName());
            ps.setCommunityPercent(pfua.getCommunityPercent());
            ps.setCommunityProfit(new BigDecimal(pfua.getCommunityProfit()));
            ps.setNeedSettleAmount(pfua.getCsAmount());
            ps.setPlantformProfit(new BigDecimal(pfua.getPlantformProfit()));
            ps.setSettleStatus("-1");
            ps.setSettleStatusString("平台未申请");
            ps.setTotalProfit(new BigDecimal(pfua.getTotalProfit()));
            ps.setTotalTradeNum(pfua.getTtNum());
            psList.add(ps);
        }
    }

    @Override
    public PlantformExportBean getPfExportSettleDetailData(String exportCode, String subcomId, Long communityId) throws EdsiException {
        List<PlantformSettlePO> pfList = getPfExportSettle(exportCode, subcomId, communityId);
        return getPlatformExportBean(pfList, communityId, exportCode);
    }

    @Override
    public PlantformExportBean getEEPExportSettleDetailData(String exportCode, String subcomId, Long communityId) throws EdsiException {
        List<PlantformSettlePO> pfList = getEEPExportSettle(exportCode, subcomId, communityId);
        return getPlatformExportBean(pfList, communityId, exportCode);
    }

    private PlantformExportBean getPlatformExportBean(List<PlantformSettlePO> pfList, Long communityId,
            String exportCode) throws EdsiException {
        PlantformExportBean pfeb = new PlantformExportBean();
        if (Utility.isNotEmpty(pfList)) {
            // sheetName设置
            List<String> sheetNameList = new ArrayList<String>();
            for (PlantformSettlePO pf : pfList) {
                if (communityId != null) {// 物业本人
                    sheetNameList.add(pf.getSettleMonth());
                } else {// 平台导出
                    sheetNameList.add(pf.getCommunityName() + "-" + pf.getSettleMonth());
                }
                pfeb.setSheetNameList(sheetNameList);
            }
            // 设置内容部分
            for (PlantformSettlePO pf : pfList) {
                PlantformSettleInfo psi = new PlantformSettleInfo();
                // 设置基本信息
                psi.setPfSettle(pf);
                // 设置详细信息
                if ("0".equals(exportCode)) {// 未提交申请的
                    List<SellerSettlePO> ssList = settleDao.getPlatformSettleDetail(pf.getCommunityId(),pf.getSettleMonth());
                    List<AdvertIncomePO> aiList = paymentDao.getSettleAdvertIncomeList(pf.getCommunityId(), pf.getSettleMonth());
                    if(Utility.isNotEmpty(aiList)){
                        //获取物业相关小区
                        List<SellerProperty> spList = edsPropertyService.getCommunityPropertys(pf.getCommunityId());
                        setRegions(aiList,spList);
                    }
                    AdvertIncomePO  totalAdvert = paymentDao.getTotalAdvertByComMonth(pf.getCommunityId(), pf.getSettleMonth());
                    setSellerInfo(ssList);
                    setCommunityInfo(ssList);
                    psi.getPfDetailList().addAll(ssList);
                    psi.getPdAdvertList().addAll(aiList);
                    psi.setTotalAdvert(totalAdvert);
                } else {// 结算中的、结算完成的
                    List<SellerSettlePO> ssList = settleDao.getPlatformSettleDetail(pf.getCommunityId(),pf.getSettleMonth());
                    List<AdvertIncomePO> aiList = paymentDao.getPfSettleAdvertList(pf.getPlantformSettleId());
                    if(Utility.isNotEmpty(aiList)){
                        //获取物业相关小区
                        List<SellerProperty> spList = edsPropertyService.getCommunityPropertys(pf.getCommunityId());
                        setRegions(aiList,spList);
                    }
                    AdvertIncomePO  totalAdvert = paymentDao.getTotalAdvertByPfSettle(pf.getPlantformSettleId());
                    setSellerInfo(ssList);
                    setCommunityInfo(ssList);
                    psi.getPfDetailList().addAll(ssList);
                    psi.getPdAdvertList().addAll(aiList);
                    psi.setTotalAdvert(totalAdvert);
                }
                pfeb.getData().add(psi);
            }
        }
        return pfeb;
    }

    private void setRegions(List<AdvertIncomePO> aiList, List<SellerProperty> spList) {
        //propertyId,propertyName
        Map<Long,String>   propertyMap = getConvertMap(spList);
        if(Utility.isNotEmpty(aiList)){
            for (AdvertIncomePO ai : aiList) {
               if(Utility.isNotBlank(ai.getAdvertRegion())){
                   List<Long> propertyIdList = Utility.getSplitList(ai.getAdvertRegion(),",");
                   StringBuilder sb = new StringBuilder();
                   for (Long propertyId : propertyIdList) {
                       if(propertyMap.containsKey(propertyId)){
                         sb.append(",").append(propertyMap.get(propertyId));
                       }
                   }
                   ai.setAdvertRegionNames(sb.toString().substring(1));
               }
            }
        }
    }

    private Map<Long, String> getConvertMap(List<SellerProperty> spList) {
        Map<Long, String> propertyMap = new HashMap<Long, String>();
        if(Utility.isNotEmpty(spList)){
            for (SellerProperty sp : spList) {
                propertyMap.put(sp.getPropertyId(),sp.getPropertyName());
            }
        }
        return propertyMap;
    }

    @Override
    @Transactional
    public void approvePfSettle(Long communityId, String settleMonth) throws EdsiException {
        //判断是否已经申请结算
        PlantformUnApply pfu = settleDao.getPfApplySettle(communityId, settleMonth);
        if(pfu == null){
            throw new EdsiException("未找到需要申请的结算记录");
        }
        // 1.0 生成结算信息
        PlantformSettlePO pfSettle = getPfUnSubmitSettle(communityId, settleMonth);
        paymentDao.insertPlantformSettle(pfSettle);
        // 3.0 生成广告结算明细
        List<PlantformSettleAdvertPO> psaList = getPlantformSettleAdvertList(pfSettle);
        if(Utility.isNotEmpty(psaList)) {
          paymentDao.batchInsertPFAdvertSettle(psaList);
        }
        // 4.0 批量修改商家交易信息平台结算状态
        settleDao.batchUpdatePfApplyStatus(communityId, settleMonth);
        // 5.0 批量修改广告收入信息结算状态
        paymentDao.batchUpdateAdvertIncomeStatus(communityId, settleMonth,AdvertIncomePO.SETTLE_APPLY_DOING);
    }

    @Override
    @Transactional
    public void approveEEPSettle(Long communityId, String settleMonth) throws EdsiException {
        //判断是否已经申请结算
        PlantformUnApply pfu = settleDao.getEEPApplySettle(communityId, settleMonth);
        if(pfu == null){
            throw new EdsiException("未找到需要申请的结算记录");
        }
        // 1.0 生成结算信息
        PlantformSettlePO pfSettle = getEEPUnSubmitSettle(communityId, settleMonth);
        paymentDao.insertPlantformSettle(pfSettle);
        // 3.0 生成广告结算明细
        List<PlantformSettleAdvertPO> psaList = getPlantformSettleAdvertList(pfSettle);
        if(Utility.isNotEmpty(psaList)) {
          paymentDao.batchInsertPFAdvertSettle(psaList);
        }
        // 4.0 批量修改商家交易信息平台结算状态
        settleDao.batchUpdateEEPApplyStatus(communityId, settleMonth);
        // 5.0 批量修改广告收入信息结算状态
        paymentDao.batchUpdateAdvertIncomeStatus(communityId, settleMonth,AdvertIncomePO.SETTLE_APPLY_DOING);
    }

    private PlantformSettlePO getPfUnSubmitSettle(Long communityId, String settleMonth) throws EdsiException {
        PlantformUnApply pfu = settleDao.getPfApplySettle(communityId, settleMonth);
        CommunityPO community = edsCommunityService.getById(communityId);
        PlantformSettlePO pfs = toPlatformSettlePO(pfu, community, PlantformSettlePO.PF_TYPE_EBP);
        return pfs;
    }

    private PlantformSettlePO getEEPUnSubmitSettle(Long communityId, String settleMonth) throws EdsiException {
        PlantformUnApply pfu = settleDao.getEEPApplySettle(communityId, settleMonth);
        CommunityPO community = edsCommunityService.getById(communityId);
        PlantformSettlePO pfs = toPlatformSettlePO(pfu, community, PlantformSettlePO.PF_TYPE_EEP);
        return pfs;
    }

    private PlantformSettlePO toPlatformSettlePO(PlantformUnApply pfu, CommunityPO community, String pfType) throws EdsiException {
        PlantformSettlePO pfs = new PlantformSettlePO();
        // 设置结算基本信息
        String totalProfit = Tools.add(pfu.getAdvertProfit(), pfu.getTcAmount().doubleValue() + "");
        pfs.setAdvertProfit(new BigDecimal(pfu.getAdvertProfit()));
        pfs.setCanSettleAmount(pfu.getCstAmount());
        pfs.setCanSettleNum(pfu.getCsNum());
        pfs.setCommissionProfit(pfu.getTcAmount());
        pfs.setCommunityId(community.getCommunityId());
        pfs.setCommunityName(community.getCommunityName());
        pfs.setCommunityPercent(community.getCommissionPercent());
        pfs.setCommunityProfit(Tools.multiplyDecimal(totalProfit, community.getCommissionPercent() + ""));
        pfs.setCreateTime(new Date());
        pfs.setNeedSettleAmount(pfu.getCsAmount());
        pfs.setPlantformProfit(Tools.subtractDecimal(totalProfit, pfs.getCommunityProfit().doubleValue() + ""));
        if (PlantformSettlePO.PF_TYPE_EBP.equals(pfType) && "2016-02".compareTo(pfu.getSettleMonth()) < 0) {
            pfs.setPlantformProfit(Tools.addDecimal(AmountUtil.format(pfs.getPlantformProfit(), 2),
                    AmountUtil.format(pfu.getCsAmount(), 2)));
        }
        pfs.setPlantformSettleId(edsSystemService.getId());
        pfs.setPlantformSubmitTime(new Date());
        pfs.setSettleStatus(PlantformSettlePO.SETTLE_STATUS_0);
        pfs.setTotalProfit(new BigDecimal(totalProfit));
        pfs.setTotalTradeNum(pfu.getTtNum());
        pfs.setSettleMonth(pfu.getSettleMonth());
        // 设置所属分公司ID
        pfs.setSubcomId(community.getSubcomId());
        pfs.setIsCommPayee(pfu.getIsCommPayee());
        return pfs;
    }

    private List<PlantformSettleAdvertPO> getPlantformSettleAdvertList(PlantformSettlePO pfSettle) 
            throws EdsiException {
        List<PlantformSettleAdvertPO> psaList = new ArrayList<PlantformSettleAdvertPO>();
        List<AdvertIncomePO> aiList = paymentDao.getSettleAdvertIncomeList(pfSettle.getCommunityId(),pfSettle.getSettleMonth());
        if(Utility.isNotEmpty(aiList)){
            for (AdvertIncomePO ai : aiList) {
                PlantformSettleAdvertPO psa = new PlantformSettleAdvertPO();
                psa.setPlantformAdvertSettleId(edsSystemService.getId());
                psa.setPlantformSettleId(pfSettle.getPlantformSettleId());
                psa.setAdvertIncomeId(ai.getId());
                psaList.add(psa);
            }
        }        
        return psaList;
    }

    @Override
    public String batchCheckPfApproveSettle(String pfCommunityIdMonths) throws EdsiException {
        if (Utility.isNotBlank(pfCommunityIdMonths)) {
            String[] communityIdMonths = pfCommunityIdMonths.split(",");
            for (String communityIdMonth : communityIdMonths) {
                String[] items = communityIdMonth.split(":");
                PlantformUnApply pua = settleDao.getPfApplySettle(Long.valueOf(items[0]), items[1]);
                if (pua == null || pua.getTtNum() <= 0) {// 没有需要申请的结算信息
                    return "1";
                }
            }
            return "0";
        }
        return "-1";
    }

    @Override
    public String batchCheckEEPApproveSettle(String pfCommunityIdMonths) throws EdsiException {
        if (Utility.isNotBlank(pfCommunityIdMonths)) {
            String[] communityIdMonths = pfCommunityIdMonths.split(",");
            for (String communityIdMonth : communityIdMonths) {
                String[] items = communityIdMonth.split(":");
                PlantformUnApply pua = settleDao.getEEPApplySettle(Long.valueOf(items[0]), items[1]);
                if (pua == null || pua.getTtNum() <= 0) {// 没有需要申请的结算信息
                    return "1";
                }
            }
            return "0";
        }
        return "-1";
    }

    @Override
    @Transactional
    public void batchApprovePfSettle(String pfCommunityIdMonths) throws EdsiException {
        if (Utility.isNotBlank(pfCommunityIdMonths)) {
            String[] communityIdMonths = pfCommunityIdMonths.split(",");
            for (String communityIdMonth : communityIdMonths) {
                String[] items = communityIdMonth.split(":");
                approvePfSettle(Long.valueOf(items[0]), items[1]);
            }
        }
    }

    @Override
    @Transactional
    public void batchApproveEEPSettle(String pfCommunityIdMonths) throws EdsiException {
        if (Utility.isNotBlank(pfCommunityIdMonths)) {
            String[] communityIdMonths = pfCommunityIdMonths.split(",");
            for (String communityIdMonth : communityIdMonths) {
                String[] items = communityIdMonth.split(":");
                approveEEPSettle(Long.valueOf(items[0]), items[1]);
            }
        }
    }

    @Override
    public Pagination getPfHandleSettleApply(String subcomId, Long communityId, Pagination pagination,
            PlantformSettlePO plantformSettlePO) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<PlantformSettlePO> ssList = settleDao.getPfHandleSettleApply(subcomId, communityId, pagination, rb);
        pagination.setList(ssList);
        return pagination;
    }

    @Override
    public Pagination getPfFinishSettleApply(String subcomId, Long communityId, Pagination pagination,
            PlantformSettlePO plantformSettlePO) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<PlantformSettlePO> ssList = settleDao.getPfFinishSettleApply(subcomId, communityId, pagination, rb);
        pagination.setList(ssList);
        return pagination;
    }

    @Override
    public PlantformUnApply getEEPWaitApplySettleSum(Long communityId) throws EdsiException {
        List<PlantformUnApply> puaList = settleDao.getEEPWaitApplySettle(communityId);
        fillCommunityInfo(puaList, PlantformSettlePO.PF_TYPE_EEP);
        return sum(puaList);
    }

    private PlantformUnApply sum(List<PlantformUnApply> puaList) {
        PlantformUnApply pfu = new PlantformUnApply();
        pfu.setCommunityName("合计");
        pfu.setCommissionProfit("0.00");
        pfu.setAdvertProfit("0.00");
        pfu.setTotalProfit("0.00");
        pfu.setPlantformProfit("0.00");
        pfu.setCommunityProfit("0.00");
        if (CollectionUtils.isNotEmpty(puaList)) {
            for(PlantformUnApply pfua : puaList) {
                pfu.setCommissionProfit(Tools.add(pfu.getCommissionProfit(), pfua.getCommissionProfit()));
                pfu.setAdvertProfit(Tools.add(pfu.getAdvertProfit(), pfua.getAdvertProfit()));
                pfu.setTotalProfit(Tools.add(pfu.getTotalProfit(), pfua.getTotalProfit()));
                pfu.setPlantformProfit(Tools.add(pfu.getPlantformProfit(), pfua.getPlantformProfit()));
                pfu.setCommunityProfit(Tools.add(pfu.getCommunityProfit(), pfua.getCommunityProfit()));
            }
        }
        return pfu;
    }

}
