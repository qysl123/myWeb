/*
 * E社区
 * Copyright (c) 2014 成都翼承科技 All Rights Reserved.
 */
package com.yc.eps.count;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yc.commons.Utility;
import com.yc.commons.mybatis.pagination.vo.Pagination;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.community.IPropertyService;
import com.yc.edsi.community.PropertyPO;
import com.yc.edsi.count.IOwnerAccountService;
import com.yc.edsi.count.OwnerAccountCountPO;
import com.yc.edsi.owner.IOwnerService;
import com.yc.edsi.owner.OwnerPO;

/**
 * @author <a href="mailto:zhouc@yichenghome.com">Zhou Chao</a>
 * @version 2.0
 * @since 2014年8月12日
 */
@Service
public class OwnerAccountService implements IOwnerAccountService {
	private final static Logger LOGGER = LoggerFactory.getLogger(OwnerAccountService.class);
    
    @Resource
    private IOwnerAccountCountDao ownerAccountCountDao;
    
    @Resource
    private IPropertyService edsPropertyService;
    
    @Resource
    private IOwnerService edsOwnerService;

    /**
     * 内部类，主要用来排序． 按账户余额排序
     * */
	class CompRemainingMoney implements Comparator<OwnerAccountCountPO> {
		public int compare(OwnerAccountCountPO o1, OwnerAccountCountPO o2) {
			if (o1.getRemainingMoney() < o2.getRemainingMoney()) {
				return 1;
			} else if (o1.getRemainingMoney() > o2.getRemainingMoney()) {
				return -1;
			}
			return 0;
		}
	}

	@Override
	public Pagination getOwnerAccountList(Pagination pagination, OwnerAccountCountPO ownerAccountCond)
	        throws EdsiException {
		List<Long> propertyIds = Utility.getSplitList(ownerAccountCond.getRangePropertyIds(), ",");
		List<OwnerAccountCountPO> oacList = ownerAccountCountDao.getOwnerAccountList(ownerAccountCond,
				ownerAccountCond.getRangePropertyIds(), propertyIds);
		
		// 使用纯代码实现表连接、按条件过滤及排序
		oacList = joinQueryAndSort(ownerAccountCond, oacList, ownerAccountCond.getRangePropertyIds());
		
		// 获取合计结果
		OwnerAccountCountPO sumOac = sumOwnerAccount(oacList);

		// 设置分页信息及分页记录
		pagination.setRecondSum(Integer.valueOf(oacList.size()));
		int toIndex = pagination.getStartIndex() + pagination.getPageSize();
		if (toIndex > oacList.size()) toIndex = oacList.size();
		List<OwnerAccountCountPO> noacList = new ArrayList<OwnerAccountCountPO>();
		noacList.addAll(oacList.subList(pagination.getStartIndex(), toIndex));
		
		// 设置合计结果，放在最前面
		noacList.add(0, sumOac);
		
		pagination.setList(noacList);
		return pagination;
	}
	
    private OwnerAccountCountPO sumOwnerAccount(List<OwnerAccountCountPO> oacList) {
    	OwnerAccountCountPO sumOac = new OwnerAccountCountPO();
		if (CollectionUtils.isNotEmpty(oacList)) {
			sumOac.setHouseNumber("合计");
			for(OwnerAccountCountPO oac : oacList) {
				sumOac.setRechargeMoney(sumOac.getRechargeMoney() + oac.getRechargeMoney());
				sumOac.setVoucherMoney(sumOac.getVoucherMoney() + oac.getVoucherMoney());
				sumOac.setSeekMoney(sumOac.getSeekMoney() + oac.getSeekMoney());
				sumOac.setPayment(sumOac.getPayment() + oac.getPayment());
				sumOac.setConsume(sumOac.getConsume() + oac.getConsume());
				sumOac.setRemainingMoney(sumOac.getRemainingMoney() + oac.getRemainingMoney());
			}
		}
	    return sumOac;
    }

	@Override
    public List<OwnerAccountCountPO> getAllOwnerAccountList(OwnerAccountCountPO ownerAccountCond) throws EdsiException {
		List<Long> propertyIds = Utility.getSplitList(ownerAccountCond.getRangePropertyIds(), ",");
		List<OwnerAccountCountPO> oacList = ownerAccountCountDao.getOwnerAccountList(ownerAccountCond,
				ownerAccountCond.getRangePropertyIds(), propertyIds);

		// 使用纯代码实现表连接、按条件过滤及排序
		oacList = joinQueryAndSort(ownerAccountCond, oacList, ownerAccountCond.getRangePropertyIds());
		
		// 获取合计结果
		OwnerAccountCountPO sumOac = sumOwnerAccount(oacList);
		
		// 设置合计结果
		oacList.add(sumOac);
		
	    return oacList;
    }

	private List<OwnerAccountCountPO> joinQueryAndSort(OwnerAccountCountPO ownerAccountCond,
			List<OwnerAccountCountPO> oacList,String rangePropertyIds) {
		// 设置业主信息及小区名称
		oacList = rebuildOwnerAccountCountPO(oacList, ownerAccountCond.getCommunityId(), rangePropertyIds);
		
		// 按账户余额降序排列
		if (ownerAccountCond.getSortType() == 4) {
			Comparator<OwnerAccountCountPO> sc = new CompRemainingMoney();
			Collections.sort(oacList, sc);
		}
	    return oacList;
    }

	/**
     * 封装小区信息为MAP
     * 
     * @param communityId
     */
    public Map<String, String> getPropertyNameMap(long communityId,String rangePropertyIds) {
        Map<String, String> propertyMap = new HashMap<String, String>();
        try {
            List<PropertyPO> propertyList = edsPropertyService.getList(new PropertyPO(), communityId,rangePropertyIds, -1, -1);
            for (PropertyPO propertyPO : propertyList) {
            	propertyMap.put(String.valueOf(propertyPO.getPropertyId()), propertyPO.getPropertyName());
            }
        } catch (EdsiException e) {
            LOGGER.error("getPropertyNameMap", e);
        }
        return propertyMap;
    }

    /**
     * 封装业主信息为MAP
     * 
     * @param communityId
     * @return
     */
	private Map<String, OwnerPO> getOwnerMap(long communityId, String rangePropertyIds) {
		Map<String, OwnerPO> ownerMap = new HashMap<String, OwnerPO>();
		try {
			List<OwnerPO> ownerList = edsOwnerService.getOwnersByCommunityId(communityId, rangePropertyIds);
			for (OwnerPO ownerPO : ownerList) {
				ownerMap.put(String.valueOf(ownerPO.getOwnerId()), ownerPO);
			}
		} catch (EdsiException e) {
			LOGGER.error("getOwnerMap", e);
		}
		return ownerMap;
	}

	private List<OwnerAccountCountPO> rebuildOwnerAccountCountPO(List<OwnerAccountCountPO> oacList, 
			long communityId,String rangePropertyIds) {
		List<OwnerAccountCountPO> nOacList = new ArrayList<OwnerAccountCountPO>();
		
		Map<String, String> propNameMap = getPropertyNameMap(communityId,rangePropertyIds);
		Map<String, OwnerPO> ownerMap   = getOwnerMap(communityId,rangePropertyIds);
		if (CollectionUtils.isNotEmpty(oacList)) {
			for(OwnerAccountCountPO oacp : oacList) {
				oacp.setPropertyName(propNameMap.get("" + oacp.getPropertyId()));
				OwnerPO owner = ownerMap.get("" + oacp.getOwnerId());
				if (owner != null) {
					oacp.setCardNo(owner.getCardNo());
				}
				oacp.setVoucherMoney(oacp.calVoucherMoney());// 计算代金劵金额
				oacp.setRemainingMoney(oacp.calRemainingMoney());// 计算账户余额
				nOacList.add(oacp);
			}
		}
		return nOacList;
    }

}
