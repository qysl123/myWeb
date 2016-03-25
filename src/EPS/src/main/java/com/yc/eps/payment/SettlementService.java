/*
 * E社区
 * Copyright (c) 2014 YT All Rights Reserved.
 */
package com.yc.eps.payment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
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
import com.yc.edsi.count.CountPO;
import com.yc.edsi.count.StatQueryCond;
import com.yc.edsi.order.IOrderService;
import com.yc.edsi.payment.AdvertIncomePO;
import com.yc.edsi.payment.AdvertOut;
import com.yc.edsi.payment.ISettlementService;
import com.yc.edsi.payment.PlantformExportBean;
import com.yc.edsi.payment.PlantformSettleAdvertPO;
import com.yc.edsi.payment.PlantformSettleDetailPO;
import com.yc.edsi.payment.PlantformSettleInfo;
import com.yc.edsi.payment.PlantformSettlePO;
import com.yc.edsi.payment.PlantformUnApply;
import com.yc.edsi.payment.SellerPaymentPO;
import com.yc.edsi.payment.SellerSettleDetailPO;
import com.yc.edsi.payment.SellerSettleInfo;
import com.yc.edsi.payment.SellerSettlePO;
import com.yc.edsi.payment.SettleExportBean;
import com.yc.edsi.seller.ISellerService;
import com.yc.edsi.seller.SellerPO;
import com.yc.edsi.seller.SellerProperty;
import com.yc.edsi.system.ISystemService;

/**
 * @author  <a href="mailto:xuyy@yichenghome.com">Xu Yuanyuan</a>
 * @version 1.0
 * @date    2014年10月13日 上午11:35:03
 * @desc
 */
@Service
public class SettlementService implements ISettlementService {

    @Resource
    private IPaymentDao paymentDao;

    @Resource
    private ISellerService edsSellerService;

    @Resource
    private IOrderService edsOrderService;

    @Resource
    private ISystemService edsSystemService;

    @Resource
    private ICommunityService edsCommunityService;

    @Resource
    private IPropertyService edsPropertyService;

    @Override
    public Pagination getWaitApplySettleDetail(Long sellerId, Long communityId, String settleMonth,
            Pagination pagination, SellerSettleDetailPO ssd) throws EdsiException {
        List<SellerSettleDetailPO> ssdList = new ArrayList<SellerSettleDetailPO>();
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<SellerPaymentPO> spList = paymentDao.getWaitApplySettleDetail(sellerId, communityId, settleMonth,
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
    public Pagination getSettleDetail(Long sellerSettleId, Pagination page, SellerSettleDetailPO ssd)
            throws EdsiException {
        List<SellerSettleDetailPO> ssdList = new ArrayList<SellerSettleDetailPO>();
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(page);
        List<SellerPaymentPO> spList = paymentDao.getSellerSettleDetail(sellerSettleId, page, rb);
        if (Utility.isNotEmpty(spList)) {
            Map<Long, PropertyPO> propMap = new HashMap<Long, PropertyPO>();
            for (SellerPaymentPO sp : spList) {
                ssdList.add(toSellerSettleDetail(sp, propMap));
            }
            page.setList(ssdList);
        }
        return page;
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
        // 3.0 修改商家交易结算信息状态
        paymentDao.batchUpdateApplyStatus(sellerId, communityId, settleMonth);
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
        SellerSettlePO sellerSettle = paymentDao.getSellerWaitApplySettle(sellerId, communityId, settleMonth);
        // 设置佣金等
        setCommission(sellerSettle, sellerId);
        // 设置结算状态
        sellerSettle.setSettleStatus(SellerSettlePO.SETTLE_STATUS_0);
        sellerSettle.setSellerSubmitTime(new Date());
        sellerSettle.setCreateTime(new Date());
        sellerSettle.setSellerSettleId(edsSystemService.getId());
        return sellerSettle;
    }

    @Override
    public List<SellerSettleDetailPO> getExportSettleMonthSettleDetail(Long sellerId, Long communityId,
            String settleMonth, SellerSettleDetailPO sellerSettleDetailPO) throws EdsiException {
        List<SellerSettleDetailPO> ssdList = new ArrayList<SellerSettleDetailPO>();
        List<SellerPaymentPO> spList = paymentDao.getWaitApplySettleDetail(sellerId, communityId, settleMonth);
        if (Utility.isNotEmpty(spList)) {
            Map<Long, PropertyPO> propMap = new HashMap<Long, PropertyPO>();
            for (SellerPaymentPO sp : spList) {
                ssdList.add(toSellerSettleDetail(sp, propMap));
            }
        }
        return ssdList;
    }

    @Override
    public List<SellerSettleDetailPO> getExportSettleDetail(Long sellerSettleId,
            SellerSettleDetailPO sellerSettleDetailPO) throws EdsiException {
        List<SellerSettleDetailPO> ssdList = new ArrayList<SellerSettleDetailPO>();
        List<SellerPaymentPO> spList = paymentDao.getSellerSettleDetail(sellerSettleId);
        if (Utility.isNotEmpty(spList)) {
            Map<Long, PropertyPO> propMap = new HashMap<Long, PropertyPO>();
            for (SellerPaymentPO sp : spList) {
                ssdList.add(toSellerSettleDetail(sp, propMap));
            }
        }
        return ssdList;
    }

    @Override
    public void sureReceived(Long sellerId, Long sellerSettleId) throws EdsiException {
        SellerSettlePO settle = paymentDao.getSellerSettle(sellerSettleId);
        if (settle == null || !settle.getSellerId().equals(sellerId)
                || !SellerSettlePO.SETTLE_STATUS_3.equals(settle.getSettleStatus())) {
            throw new EdsiException("没有找到需要确认收款的结算信息");
        } else {
            // 确认收款
            // 1.0 修改结算状态为确认打款
            settle.setSettleStatus(SellerSettlePO.SETTLE_STATUS_4);
            settle.setSellerSureTime(new Date());
            paymentDao.updateSettle(settle);
            // 2.0 修改商家交易信息结算完成状态为已结算
            paymentDao.batchUpdateFinishStatus(sellerSettleId);
        }
    }

    @Override
    public Pagination getSellerSettleApply(String pfType, Pagination pagination, SellerSettlePO sellerSettlePO)
            throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<SellerSettlePO> ssList = paymentDao.getSellerSettleApply(pfType, sellerSettlePO, pagination, rb);
        setSellerInfo(ssList);
        setCommunityInfo(ssList);
        pagination.setList(ssList);
        return pagination;
    }

    @Override
    public SellerSettlePO getSellerSettleSum(String pfType, SellerSettlePO sellerSettlePO) throws EdsiException {
        return paymentDao.getSellerSettleSum(pfType, sellerSettlePO);
    }

    private void setSellerName(SellerSettlePO ss) throws EdsiException {
        SellerPO sellerInfo = edsSellerService.getBaseSellerPO(ss.getSellerId());
        ss.setSellerName(sellerInfo.getSellerName());
        ss.setSellerNo(sellerInfo.getSellerNo());
    }

    @Override
    @Transactional
    public void changeStatus(Long sellerSettleId, String settleStatus, String remark) throws EdsiException {
        SellerSettlePO settle = paymentDao.getSellerSettle(sellerSettleId);
        settle.setSettleStatus(settleStatus);
        if (SellerSettlePO.SETTLE_STATUS_1.equals(settleStatus)) {// 处理物业同意结算事宜
            settle.setFinanceAgreeTime(new Date());
            paymentDao.updateSettle(settle);
        } else if (SellerSettlePO.SETTLE_STATUS_2.equals(settleStatus)) {// 处理物业财务不同意结算
            settle.setFinanceRefuseTime(new Date());
            settle.setFinanceRefuseReason(remark);
            paymentDao.updateSettle(settle);
        } else if (SellerSettlePO.SETTLE_STATUS_3.equals(settleStatus)) {// 处理物业财务确认打款
            settle.setFinanceSureTime(new Date());
            paymentDao.updateSettle(settle);
        } else if (SellerSettlePO.SETTLE_STATUS_4.equals(settleStatus)) {// 处理商家确认收款
            // 1.0 修改状态
            settle.setSellerSureTime(new Date());
            paymentDao.updateSettle(settle);
            // 2.0 修改商家交易信息状态为已结算
            paymentDao.batchUpdateFinishStatus(sellerSettleId);
        } else if (SellerSettlePO.SETTLE_STATUS_5.equals(settleStatus)) {// 处理商家收款有异议
            // 待处理
            settle.setSellerDissentReason(remark);
            paymentDao.updateSettle(settle);
        }
    }

    @Override
    public boolean checkSettleStatus(Long sellerSettleId, String oldStatus, String newStatus) throws EdsiException {
        SellerSettlePO settle = paymentDao.getSellerSettle(sellerSettleId);
        if (settle != null) {
            String settleStatus = settle.getSettleStatus();
            if (settleStatus.equals(oldStatus)) {
                if (SellerSettlePO.SETTLE_STATUS_0.equals(oldStatus)
                        && SellerSettlePO.SETTLE_STATUS_1.equals(newStatus)) {// 商家申请-物业同意
                    return true;
                } else if (SellerSettlePO.SETTLE_STATUS_0.equals(oldStatus)
                        && SellerSettlePO.SETTLE_STATUS_2.equals(newStatus)) {// 商家申请-物业不同意
                    return true;
                } else if (SellerSettlePO.SETTLE_STATUS_1.equals(oldStatus)
                        && SellerSettlePO.SETTLE_STATUS_3.equals(newStatus)) {// 物业同意-物业确认打款
                    return true;
                } else if (SellerSettlePO.SETTLE_STATUS_3.equals(oldStatus)
                        && SellerSettlePO.SETTLE_STATUS_4.equals(newStatus)) {// 物业确认打款-商家确认收款
                    return true;
                } else if (SellerSettlePO.SETTLE_STATUS_3.equals(oldStatus)
                        && SellerSettlePO.SETTLE_STATUS_5.equals(newStatus)) {// 物业确认打款-商家收款有异议
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @Transactional
    public void batchAgreeSettle(String sellerSettleIds, String settleStatus) throws EdsiException {
        if (Utility.isNotEmpty(sellerSettleIds)) {
            String[] sellerSettleIdAry = sellerSettleIds.split(",");
            for (String ssId : sellerSettleIdAry) {
                Long sellerSettleId = Long.valueOf(ssId);
                changeStatus(sellerSettleId, settleStatus, "");
            }
        }
    }

    @Override
    public boolean batchCheckSettleStatus(String sellerSettleIds, String oldStatus, String newStatus)
            throws EdsiException {
        if (Utility.isNotEmpty(sellerSettleIds)) {
            String[] sellerSettleIdAry = sellerSettleIds.split(",");
            for (String ssId : sellerSettleIdAry) {
                Long sellerSettleId = Long.valueOf(ssId);
                if (!checkSettleStatus(sellerSettleId, oldStatus, newStatus)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<SellerSettlePO> getSettlementApply(Long sellerId) throws EdsiException {
        // 1.0 获取未结算的(按照月物业分组)
        List<SellerSettlePO> waitList = paymentDao.getSellerWaitApplySettleList(sellerId);
        if (waitList == null) {
            waitList = new ArrayList<SellerSettlePO>();
        }
        // 2.0 补充相关信息
        setCommission(waitList, sellerId);
        setCommunityInfo(waitList);
        return waitList;
    }

    @Override
    public List<SellerSettlePO> getSettlementHandle(Long sellerId) throws EdsiException {
        // 1.0 获取未处理完毕的结算信息
        List<SellerSettlePO> submitList = paymentDao.getSubmitSettlementApply(sellerId);
        if (submitList == null) {
            submitList = new ArrayList<SellerSettlePO>();
        }
        // 2.0 补充相关信息
        setCommunityInfo(submitList);
        return submitList;
    }

    /**
     * 设置佣金等
     * 
     * @param sellerSettleList
     * @return
     * @throws EdsiException
     */
    private void setCommission(List<SellerSettlePO> sellerSettleList, Long sellerId)
            throws EdsiException {
        SellerPO seller = edsSellerService.getBaseSellerPO(sellerId);
        if (Utility.isNotEmpty(sellerSettleList)) {
            Double percent = Utility.isNotBlank(seller.getCommissionPercent()) ? Double.valueOf(seller
                    .getCommissionPercent()) : 0.00;
            for (SellerSettlePO ss : sellerSettleList) {
                // 计算佣金、可结算佣金
                BigDecimal tradeCommiAmount = new BigDecimal(Tools.multiply(ss.getCanSettleTotalAmount().doubleValue(),
                        percent));
                ss.setTradeCommiAmount(tradeCommiAmount);
                ss.setCanSettleAmount(new BigDecimal(Tools.subtract(ss.getCanSettleTotalAmount().doubleValue(), ss
                        .getTradeCommiAmount().doubleValue())));
                ss.setCommissionPercent(percent);
            }
        }
    }

    private void setCommission(SellerSettlePO settleInfo, Long sellerId) throws EdsiException {
        SellerPO seller = edsSellerService.getBaseSellerPO(sellerId);
        Double percent = Utility.isNotBlank(seller.getCommissionPercent()) ? Double.valueOf(seller
                .getCommissionPercent()) : 0.00;
        // 构建返回信息
        BigDecimal tradeCommiAmount = new BigDecimal(Tools.multiply(settleInfo.getCanSettleTotalAmount().doubleValue(),
                percent));
        settleInfo.setTradeCommiAmount(tradeCommiAmount);
        settleInfo.setCanSettleAmount(new BigDecimal(Tools.subtract(settleInfo.getCanSettleTotalAmount().doubleValue(),
                settleInfo.getTradeCommiAmount().doubleValue())));
        settleInfo.setCommissionPercent(percent);
    }

    @Override
    public SellerSettlePO getSellerSettleInfo(Long sellerSettleId) throws EdsiException {
        SellerSettlePO settle = paymentDao.getSellerSettle(sellerSettleId);
        if (settle != null) {
            setSellerName(settle);
        }
        return settle;
    }

    @Override
    public List<SellerSettlePO> getSellerSettleApplyList(String pfType, SellerSettlePO sellerSettle)
            throws EdsiException {
        List<SellerSettlePO> ssList = paymentDao.getSellerSettleApply(pfType, sellerSettle);
        setSellerInfo(ssList);
        setCommunityInfo(ssList);
        return ssList;
    }

    @Override
    public SettleExportBean getSettleExportBean(String pfType, SellerSettlePO sellerSettle)
            throws EdsiException {
        SettleExportBean seb = new SettleExportBean();
        // 1.0 获取结算基本信息
        List<SellerSettlePO> ssList = getSellerSettleApplyList(pfType, sellerSettle);
        // 2.0 设置结算基本信息
        setSettleInfo(seb, ssList);
        // 3.0 获取所有物业需要处理的结算明细
        List<SellerSettleDetailPO> ssdList = getSellerSettleApplyDetail(pfType, sellerSettle);
        // 4.0 根据对应的结算Id添加到不同的对象同去
        setSettleDetail(seb, ssdList);
        return seb;
    }

    /**
     * 把结算详细信息设置进去
     * 
     * @param seb
     * @param ssdList
     */
    private void setSettleDetail(SettleExportBean seb, List<SellerSettleDetailPO> ssdList) {
        if (Utility.isNotEmpty(ssdList) && Utility.isNotEmpty(seb.getData())) {
            List<SellerSettleInfo> ssiList = seb.getData();
            for (SellerSettleInfo ssi : ssiList) {
                for (SellerSettleDetailPO ssd : ssdList) {
                    if (ssi.getSellerSettle().getSellerSettleId().equals(ssd.getSellerSettleId())) {
                        ssi.getSsdList().add(ssd);
                    }
                }
            }
        }
    }

    private List<SellerSettleDetailPO> getSellerSettleApplyDetail(String pfType, SellerSettlePO sellerSettle) throws EdsiException {
        List<SellerSettleDetailPO> ssdList = new ArrayList<SellerSettleDetailPO>();
        List<SellerPaymentPO> spList = paymentDao.getSellerSettleApplyDetail(pfType, sellerSettle);
        if (Utility.isNotEmpty(spList)) {
            Map<Long, PropertyPO> propMap = new HashMap<Long, PropertyPO>();
            for (SellerPaymentPO sp : spList) {
                ssdList.add(toSellerSettleDetail(sp, propMap));
            }
        }
        return ssdList;
    }

    /**
     * 设置结算基本信息
     * 
     * @param seb
     * @param ssList
     */
    private void setSettleInfo(SettleExportBean seb, List<SellerSettlePO> ssList) {
        if (Utility.isNotEmpty(ssList)) {
            SellerSettleInfo ssi = null;
            for (SellerSettlePO ss : ssList) {
                seb.getSheetNameList().add(ss.getSellerName() + "-" + ss.getCommunityName() + "-" + ss.getSettleMonth());
                ssi = new SellerSettleInfo();
                ssi.setSellerSettle(ss);
                seb.getData().add(ssi);
            }
        }
    }

    @Override
    public Pagination getFinishSettleApply(String pfType, Pagination pagination, SellerSettlePO sellerSettle)
            throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<SellerSettlePO> ssList = paymentDao.getFinishSettleApply(pfType, sellerSettle, pagination, rb);
        setSellerInfo(ssList);
        setCommunityInfo(ssList);
        pagination.setList(ssList);
        return pagination;
    }

    @Override
    public Pagination getSettlementApplyHistory(Long sellerId, Pagination pagination, SellerSettlePO sellerSettle)
            throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<SellerSettlePO> ssList = paymentDao.getSettlementApplyHistory(sellerId, pagination, rb);
        setCommunityInfo(ssList);
        pagination.setList(ssList);
        return pagination;
    }

    @Override
    public Pagination getPlantformWaitApplySettle(String subcomId, Pagination pagination, PlantformUnApply unApply) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<PlantformUnApply> puaList = paymentDao.getPlantformWaitApplySettle(subcomId, pagination, rb);
        fillCommunityInfo(puaList);
        pagination.setList(puaList);
        return pagination;
    }

    @Override
    public Pagination getEEPWaitApplySettle(Long communityId, Pagination pagination, PlantformUnApply unApply) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<PlantformUnApply> puaList = paymentDao.getEEPWaitApplySettle(communityId, pagination, rb);
        fillCommunityInfo(puaList);
        pagination.setList(puaList);
        return pagination;
    }

    private void fillCommunityInfo(List<PlantformUnApply> puaList) throws EdsiException {
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
                // TODO 暂时没有广告收益
                pua.setAdvertProfit(pua.getAdvertProfit());
                pua.setCommissionProfit(AmountUtil.format(pua.getTcAmount(), 2));
                // 广告收益+佣金收益
                String totalProfit = Tools.add(pua.getAdvertProfit(), pua.getCommissionProfit());
                pua.setTotalProfit(totalProfit);
                Double plantformPercent = (1d - pua.getCommunityPercent());
                pua.setPlantformProfit(Tools.multiply(totalProfit, plantformPercent + "") + "");
                pua.setCommunityProfit(Tools.subtract(totalProfit, pua.getPlantformProfit()) + "");
            }
        }
    }

    @Override
    public Pagination getPfUnSettleDetail(Long communityId, String settleMonth, Pagination pagination,
            SellerSettlePO sellerSettle) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<SellerSettlePO> ssList = paymentDao.getPfUnSettleDetail(communityId, settleMonth, pagination, rb);
        setSellerInfo(ssList);
        setCommunityInfo(ssList);
        pagination.setList(ssList);
        return pagination;
    }

    @Override
    public Pagination getEEPUnSettleDetail(Long communityId, String settleMonth, Pagination pagination,
            SellerSettlePO sellerSettle) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<SellerSettlePO> ssList = paymentDao.getEEPUnSettleDetail(communityId, settleMonth, pagination, rb);
        setSellerInfo(ssList);
        setCommunityInfo(ssList);
        pagination.setList(ssList);
        return pagination;
    }

    private List<SellerSettlePO> getPfUnSettleDetail(Long communityId, String settleMonth) throws EdsiException {
        return paymentDao.getPfUnSettleDetail(communityId, settleMonth);
    }

    private List<SellerSettlePO> getEEPUnSettleDetail(Long communityId, String settleMonth) throws EdsiException {
        return paymentDao.getEEPUnSettleDetail(communityId, settleMonth);
    }

    @Override
    public Pagination getPfSettleDetail(Long plantformSettleId, Pagination pagination, SellerSettlePO sellerSettlePO)
            throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<SellerSettlePO> ssList = paymentDao.getPfSettleDetail(plantformSettleId, pagination, rb);
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
    public void approvePfSettle(Long communityId, String settleMonth) throws EdsiException {
        //判断是否已经申请结算
        PlantformUnApply pfu = paymentDao.getPfApplySettle(communityId, settleMonth);
        if(pfu == null){
            throw new EdsiException("未找到需要申请的结算记录");
        }
        // 1.0 生成结算信息
        PlantformSettlePO pfSettle = getPfUnSubmitSettle(communityId, settleMonth);
        paymentDao.insertPlantformSettle(pfSettle);
        // 2.0 生成结算明细
        List<PlantformSettleDetailPO> psdList = getPfSettleDetailList(pfSettle);
        if(Utility.isNotEmpty(psdList)) {
          paymentDao.batchInsertPFSettleDetail(psdList);
        }
        // 3.0 生成广告结算明细
        List<PlantformSettleAdvertPO> psaList = getPlantformSettleAdvertList(pfSettle);
        if(Utility.isNotEmpty(psaList)) {
          paymentDao.batchInsertPFAdvertSettle(psaList);
        }
        // 4.0 批量修改商家结算信息平台结算状态
        paymentDao.pfBatchUpdateSellerSatus(communityId, settleMonth);
        // 5.0 批量修改广告收入信息结算状态
        paymentDao.batchUpdateAdvertIncomeStatus(communityId, settleMonth,AdvertIncomePO.SETTLE_APPLY_DOING);
    }

    @Override
    @Transactional
    public void approveEEPSettle(Long communityId, String settleMonth) throws EdsiException {
        //判断是否已经申请结算
        PlantformUnApply pfu = paymentDao.getEEPApplySettle(communityId, settleMonth);
        if(pfu == null){
            throw new EdsiException("未找到需要申请的结算记录");
        }
        // 1.0 生成结算信息
        PlantformSettlePO pfSettle = getEEPUnSubmitSettle(communityId, settleMonth);
        paymentDao.insertPlantformSettle(pfSettle);
        // 2.0 生成结算明细
        List<PlantformSettleDetailPO> psdList = getEEPSettleDetailList(pfSettle);
        if(Utility.isNotEmpty(psdList)) {
          paymentDao.batchInsertPFSettleDetail(psdList);
        }
        // 3.0 生成广告结算明细
        List<PlantformSettleAdvertPO> psaList = getPlantformSettleAdvertList(pfSettle);
        if(Utility.isNotEmpty(psaList)) {
          paymentDao.batchInsertPFAdvertSettle(psaList);
        }
        // 4.0 批量修改商家结算信息平台结算状态
        paymentDao.eepBatchUpdateSellerSatus(communityId, settleMonth);
        // 5.0 批量修改广告收入信息结算状态
        paymentDao.batchUpdateAdvertIncomeStatus(communityId, settleMonth,AdvertIncomePO.SETTLE_APPLY_DOING);
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

    /**
     * 生成需要插入的平台结算明细
     * 
     * @param pfSettle
     * @return
     */
    private List<PlantformSettleDetailPO> getPfSettleDetailList(PlantformSettlePO pfSettle) throws EdsiException {
        List<PlantformSettleDetailPO> psdList = new ArrayList<PlantformSettleDetailPO>();
        List<SellerSettlePO> ssList = getPfUnSettleDetail(pfSettle.getCommunityId(), pfSettle.getSettleMonth());
        if (Utility.isNotEmpty(ssList)) {
            PlantformSettleDetailPO psd = null;
            for (SellerSettlePO ss : ssList) {
                psd = new PlantformSettleDetailPO(edsSystemService.getId());
                psd.setPlantformSettleId(pfSettle.getPlantformSettleId());
                psd.setSellerSettleId(ss.getSellerSettleId());
                psdList.add(psd);
            }
        }
        return psdList;
    }

    /**
     * 生成需要插入的物业结算明细
     * 
     * @param pfSettle
     * @return
     */
    private List<PlantformSettleDetailPO> getEEPSettleDetailList(PlantformSettlePO pfSettle) throws EdsiException {
        List<PlantformSettleDetailPO> psdList = new ArrayList<PlantformSettleDetailPO>();
        List<SellerSettlePO> ssList = getEEPUnSettleDetail(pfSettle.getCommunityId(), pfSettle.getSettleMonth());
        if (Utility.isNotEmpty(ssList)) {
            PlantformSettleDetailPO psd = null;
            for (SellerSettlePO ss : ssList) {
                psd = new PlantformSettleDetailPO(edsSystemService.getId());
                psd.setPlantformSettleId(pfSettle.getPlantformSettleId());
                psd.setSellerSettleId(ss.getSellerSettleId());
                psdList.add(psd);
            }
        }
        return psdList;
    }

    private PlantformSettlePO getPfUnSubmitSettle(Long communityId, String settleMonth) throws EdsiException {
        PlantformUnApply pfu = paymentDao.getPfApplySettle(communityId, settleMonth);
        CommunityPO community = edsCommunityService.getById(communityId);
        PlantformSettlePO pfs = toPlatformSettlePO(pfu, community);
        return pfs;
    }

    private PlantformSettlePO getEEPUnSubmitSettle(Long communityId, String settleMonth) throws EdsiException {
        PlantformUnApply pfu = paymentDao.getEEPApplySettle(communityId, settleMonth);
        CommunityPO community = edsCommunityService.getById(communityId);
        PlantformSettlePO pfs = toPlatformSettlePO(pfu, community);
        return pfs;
    }

    private PlantformSettlePO toPlatformSettlePO(PlantformUnApply pfu, CommunityPO community) throws EdsiException {
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
        pfs.setCommunityProfit(new BigDecimal(Tools.multiply(totalProfit, community.getCommissionPercent() + "")));
        pfs.setCreateTime(new Date());
        pfs.setNeedSettleAmount(pfu.getCsAmount());
        pfs.setPlantformProfit(new BigDecimal(Tools.subtract(totalProfit, pfs.getCommunityProfit().doubleValue() + "")));
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

    @Override
    public Pagination getPfHandleSettleApply(String subcomId, Long communityId, Pagination pagination,
            PlantformSettlePO plantformSettlePO) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<PlantformSettlePO> ssList = paymentDao.getPfHandleSettleApply(subcomId, communityId, pagination, rb);
        pagination.setList(ssList);
        return pagination;
    }

    @Override
    public Pagination getEEPHandleSettleApply(String subcomId, Long communityId, Pagination pagination,
            PlantformSettlePO plantformSettlePO) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<PlantformSettlePO> ssList = paymentDao.getEEPHandleSettleApply(subcomId, communityId, pagination, rb);
        pagination.setList(ssList);
        return pagination;
    }

    @Override
    public Pagination getPfFinishSettleApply(String subcomId, Long communityId, Pagination pagination,
            PlantformSettlePO plantformSettlePO) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<PlantformSettlePO> ssList = paymentDao.getPfFinishSettleApply(subcomId, communityId, pagination, rb);
        pagination.setList(ssList);
        return pagination;
    }

    @Override
    public Pagination getEEPFinishSettleApply(String subcomId, Long communityId, Pagination pagination,
            PlantformSettlePO plantformSettlePO) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<PlantformSettlePO> ssList = paymentDao.getEEPFinishSettleApply(subcomId, communityId, pagination, rb);
        pagination.setList(ssList);
        return pagination;
    }

    @Override
    public Pagination getRelatedSellerSettleApply(Long plantformSettleId, Pagination pagination,
            SellerSettlePO sellerSettlePO) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<SellerSettlePO> ssList = paymentDao.getRelatedSellerSettleApply(plantformSettleId, pagination, rb);
        setSellerInfo(ssList);
        setCommunityInfo(ssList);
        pagination.setList(ssList);
        return pagination;
    }

    @Override
    public PlantformSettlePO getPlantformSettle(Long plantformSettleId) throws EdsiException {
        return paymentDao.getPlantformSettle(plantformSettleId);
    }

    @Override
    public boolean checkPfSettleStatus(Long plantformSettleId, String oldStatus, String newStatus) throws EdsiException {
        PlantformSettlePO pfSettle = paymentDao.getPlantformSettle(plantformSettleId);
        if (pfSettle != null) {
            String settleStatus = pfSettle.getSettleStatus();
            if (settleStatus.equals(oldStatus)) {
                if (PlantformSettlePO.SETTLE_STATUS_0.equals(oldStatus)
                        && PlantformSettlePO.SETTLE_STATUS_1.equals(newStatus)) {// 平台申请-物业同意
                    return true;
                } else if (PlantformSettlePO.SETTLE_STATUS_0.equals(oldStatus)
                        && PlantformSettlePO.SETTLE_STATUS_2.equals(newStatus)) {// 平台申请-物业不同意
                    return true;
                } else if (PlantformSettlePO.SETTLE_STATUS_1.equals(oldStatus)
                        && PlantformSettlePO.SETTLE_STATUS_3.equals(newStatus)) {// 物业同意-物业确认打款
                    return true;
                } else if (PlantformSettlePO.SETTLE_STATUS_3.equals(oldStatus)
                        && PlantformSettlePO.SETTLE_STATUS_4.equals(newStatus)) {// 物业确认打款-平台确认收款
                    return true;
                } else if (PlantformSettlePO.SETTLE_STATUS_3.equals(oldStatus)
                        && PlantformSettlePO.SETTLE_STATUS_5.equals(newStatus)) {// 物业确认打款-平台收款有异议
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @Transactional
    public void changePfStatus(Long plantformSettleId, String settleStatus, String remark) throws EdsiException {
        PlantformSettlePO pfSettle = paymentDao.getPlantformSettle(plantformSettleId);
        if (PlantformSettlePO.SETTLE_STATUS_1.equals(settleStatus)) {// 处理物业同意结算事宜
            pfSettle.setFinanceAgreeTime(new Date());
        } else if (PlantformSettlePO.SETTLE_STATUS_2.equals(settleStatus)) {// 处理物业财务不同意结算
            pfSettle.setFinanceRefuseTime(new Date());
            pfSettle.setFinanceRefuseReason(remark);
        } else if (PlantformSettlePO.SETTLE_STATUS_3.equals(settleStatus)) {// 处理物业财务确认打款
            pfSettle.setFinanceSureTime(new Date());
            paymentDao.batchUpdateAdvertIncomeStatus(pfSettle.getCommunityId(),pfSettle.getSettleMonth(),AdvertIncomePO.SETTLE_APPLY_DONE);
        } else if (PlantformSettlePO.SETTLE_STATUS_4.equals(settleStatus)) {// 处理商家确认收款
            pfSettle.setPlantformSureTime(new Date());
        } else if (PlantformSettlePO.SETTLE_STATUS_5.equals(settleStatus)) {// 处理商家收款有异议
            pfSettle.setPlantformDissentReason(remark);
            pfSettle.setPlantformDissentTime(new Date());
        }
        pfSettle.setSettleStatus(settleStatus);
        paymentDao.updatePfSettle(pfSettle);
    }

    @Override
    public boolean batchCheckPfSettleStatus(String pfSettleIds, String oldStatus, String newStatus)
            throws EdsiException {
        if (Utility.isNotEmpty(pfSettleIds)) {
            String[] pfSettleIdAry = pfSettleIds.split(",");
            for (String psId : pfSettleIdAry) {
                Long plantformSettleId = Long.valueOf(psId);
                if (!checkPfSettleStatus(plantformSettleId, oldStatus, newStatus)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void batchAgreePfSettle(String pfSettleIds, String settleStatus) throws EdsiException {
        if (Utility.isNotEmpty(pfSettleIds)) {
            String[] pfSettleIdAry = pfSettleIds.split(",");
            for (String psId : pfSettleIdAry) {
                Long plantformSettleId = Long.valueOf(psId);
                changePfStatus(plantformSettleId, settleStatus, "");
            }
        }
    }

    @Override
    public String batchCheckPfApproveSettle(String pfCommunityIdMonths) throws EdsiException {
        if (Utility.isNotBlank(pfCommunityIdMonths)) {
            String[] communityIdMonths = pfCommunityIdMonths.split(",");
            for (String communityIdMonth : communityIdMonths) {
                String[] items = communityIdMonth.split(":");
                PlantformUnApply pua = paymentDao.getPfApplySettle(Long.valueOf(items[0]), items[1]);
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
                PlantformUnApply pua = paymentDao.getEEPApplySettle(Long.valueOf(items[0]), items[1]);
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
    public List<PlantformSettlePO> getPfExportSettleData(String exportCode, String subcomId, Long communityId, PlantformSettlePO ps)
            throws EdsiException {
        return getPfExportSettle(exportCode, subcomId, communityId);
    }

    private List<PlantformSettlePO> getPfExportSettle(String exportCode, String subcomId, Long communityId)
            throws EdsiException {
        List<PlantformSettlePO> pfList = new ArrayList<PlantformSettlePO>();
        if ("0".equals(exportCode)) {
            pfList = getUnSubmitSettleList(subcomId);
        } else if ("1".equals(exportCode)) {
            pfList = paymentDao.getPfHandleSettleApply(subcomId, communityId);
        } else if ("2".equals(exportCode)) {
            pfList = paymentDao.getPfFinishSettleApply(subcomId, communityId);
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
        List<PlantformUnApply> puaList = paymentDao.getPlantformWaitApplySettle(subcomId);
        if (Utility.isNotEmpty(puaList)) {
            fillCommunityInfo(puaList);
            convert2PlantformSettle(psList, puaList);
        }
        return psList;
    }

    private List<PlantformSettlePO> getEEPUnSubmitSettleList(Long communityId) throws EdsiException {
        List<PlantformSettlePO> psList = new ArrayList<PlantformSettlePO>();
        List<PlantformUnApply> puaList = paymentDao.getEEPWaitApplySettle(communityId);
        if (Utility.isNotEmpty(puaList)) {
            fillCommunityInfo(puaList);
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
        return getPlantformExportBean(pfList, communityId, exportCode);
    }

    @Override
    public PlantformExportBean getEEPExportSettleDetailData(String exportCode, String subcomId, Long communityId) throws EdsiException {
        List<PlantformSettlePO> pfList = getEEPExportSettle(exportCode, subcomId, communityId);
        return getEEPExportBean(pfList, communityId, exportCode);
    }

    private PlantformExportBean getPlantformExportBean(List<PlantformSettlePO> pfList, Long communityId,
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
                    List<SellerSettlePO> ssList = paymentDao.getPfUnSettleDetail(pf.getCommunityId(),pf.getSettleMonth());
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
                    List<SellerSettlePO> ssList = paymentDao.getPfSettleDetail(pf.getPlantformSettleId());
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

    private PlantformExportBean getEEPExportBean(List<PlantformSettlePO> pfList, Long communityId,
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
                    List<SellerSettlePO> ssList = paymentDao.getEEPUnSettleDetail(pf.getCommunityId(),pf.getSettleMonth());
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
                    List<SellerSettlePO> ssList = paymentDao.getPfSettleDetail(pf.getPlantformSettleId());
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
    public SellerSettlePO getFinishSettleSum(String pfType, SellerSettlePO sellerSettle) throws EdsiException {
        return paymentDao.getFinishSettleSum(pfType, sellerSettle);
    }

    @Override
    public List<Long> getSettledOrderIds(List<Long> orderIds) throws EdsiException {
        return paymentDao.getSettledOrderIds(orderIds);
    }

	@Override
	public Pagination getAdvertIncomeList(AdvertIncomePO ai,Pagination pagination) throws EdsiException {
		RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
		List<AdvertIncomePO> aiList = paymentDao.getAdvertIncomeList(ai,pagination,rb);
		pagination.setList(aiList);
		return pagination;
	}

    @Override
    public Pagination getAdvertIncomesByCommId(AdvertIncomePO ai, Pagination pagination) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<AdvertIncomePO> aiList = paymentDao.getAdvertIncomesByCommId(ai, pagination, rb);
        pagination.setList(aiList);
        return pagination;
    }

    @Override
	public AdvertIncomePO getAdvertIncomeById(Long id) throws EdsiException {
		return paymentDao.getAdvertIncomeById(id);
	}

	@Override
	public void saveAdvertIncome(AdvertIncomePO ai,List<AdvertOut> aoList) throws EdsiException {
		if(ai.getId() == null){//新增
			//1 设置主键
			ai.setId(edsSystemService.getId());
			//2 设置广告支出主键，并计算广告总支出
			if(Utility.isNotEmpty(aoList)){
				Double totalOutAmt = 0d;
				for (AdvertOut ao : aoList) {
					ao.setAdIncomeId(ai.getId());
					paymentDao.insertAdvertOut(ao);
					totalOutAmt = Tools.add(totalOutAmt,ao.getOutAmt());
				}
				ai.setOutAmount(totalOutAmt);
			}
			ai.setIsCommPayee(isCommPayee(ai.getCommunityId()));
			paymentDao.insertAdvertIncome(ai);
		} else {//修改
			//1 设置广告支出主键，并计算广告总支出
			if(Utility.isNotEmpty(aoList)){
				Double totalOutAmt = 0d;
				for (AdvertOut ao : aoList) {
					if(ao.getId() == null){//插入数据
						ao.setAdIncomeId(ai.getId());
						paymentDao.insertAdvertOut(ao);
					} else {
						paymentDao.updateAdvertOut(ao);
					}
					totalOutAmt = Tools.add(totalOutAmt,ao.getOutAmt());
				}
				ai.setOutAmount(totalOutAmt);
			}
			paymentDao.updateAdvertIncome(ai);
		}		
	}

    private String isCommPayee(long communityId) throws EdsiException {
        String advertAt = edsCommunityService.getById(communityId).getAdvertAt();
        if ("2".equals(advertAt)) {
            return "1";
        } else {
            return "0";
        }
    }

	@Override
	public List<AdvertOut> getAdvertOutByIncomeId(Long incomeId) throws EdsiException {
		return paymentDao.getAdvertOutByIncomeId(incomeId);
	}

    @Override
    public Pagination getPfUnSettleAdvertList(Long communityId, String settleMonth,
            Pagination pagination, AdvertIncomePO advertIncomePO) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<AdvertIncomePO> psaList = paymentDao.getPfUnSettleAdvertList(communityId,settleMonth,pagination, rb);
        pagination.setList(psaList);
        return pagination;
    }

    @Override
    public Pagination getPfSettleAdvertList(Long plantformSettleId, Pagination pagination, AdvertIncomePO advertIncomePO) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<AdvertIncomePO> psaList = paymentDao.getPfSettleAdvertList(plantformSettleId,pagination,rb);
        pagination.setList(psaList);
        return pagination;
    }

    @Override
    public List<CountPO> statAdvert(StatQueryCond osqc) {
        return paymentDao.statAdvert(osqc);
    }

    @Override
    public Boolean checkSellerIsSubmitted(Long sellerId,Long communityId, String settleMonth) {
        int submitCount = paymentDao.checkSellerIsSubmitted(sellerId,communityId, settleMonth);
        return submitCount > 0;
    }
}
