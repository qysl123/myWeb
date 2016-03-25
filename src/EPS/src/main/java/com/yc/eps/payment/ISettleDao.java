/*
 * E社区
 * Copyright (c) 2014 成都翼承科技 All Rights Reserved.
 */
package com.yc.eps.payment;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import com.yc.commons.mybatis.pagination.vo.Pagination;
import com.yc.edsi.payment.PlantformSettlePO;
import com.yc.edsi.payment.PlantformUnApply;
import com.yc.edsi.payment.SellerPaymentPO;
import com.yc.edsi.payment.SellerSettlePO;

/**
 * @author <a href="mailto:zhouc@yichenghome.com">Zhou Chao</a>
 * @version 2.0
 * @since 2015年1月29日
 */
public interface ISettleDao {
    /**
     * 获取所有待申请商家结算信息(取截止上月月底的数据)
     * 
     * @param sellerId
     * @return
     */
    List<SellerSettlePO> getSellerWaitApplySettleList(@Param("sellerId")Long sellerId);

    SellerSettlePO getSellerWaitApplySettle(@Param("sellerId")Long sellerId,
            @Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
    /**
     * 批量修改商家交易信息的商家结算状态
     * 
     * @param sellerId
     * @param communityId
     * @param settleMonth
     */
    void batchUpdateApplyStatus(@Param("sellerId")Long sellerId,@Param("communityId")Long communityId
            ,@Param("settleMonth")String settleMonth);
    
    /**
     * 获取待申请的平台结算信息
     * 
     * @param pagination
     * @param rb
     * @return
     */
    List<PlantformUnApply> getPlantformWaitApplySettle(@Param("subcomId")String subcomId, 
            Pagination pagination,RowBounds rb);
    
    /**
     * 获取待申请的物业结算信息
     * 
     * @param pagination
     * @param rb
     * @return
     */
    List<PlantformUnApply> getEEPWaitApplySettle(@Param("communityId")Long communityId,
            Pagination pagination,RowBounds rb);
    
    /**
     * 获取待申请的平台结算信息（不分页）
     * 
     * @param pagination
     * @param rb
     * @return
     */
    List<PlantformUnApply> getPlantformWaitApplySettle(@Param("subcomId")String subcomId);
    
    /**
     * 获取待申请的物业结算信息（不分页）
     * 
     * @param pagination
     * @param rb
     * @return
     */
    List<PlantformUnApply> getEEPWaitApplySettle(@Param("communityId")Long communityId);

    List<SellerSettlePO> getPlatformSettleDetail(@Param("communityId")Long communityId, @Param("settleMonth")String settleMonth, Pagination pagination, RowBounds rb);

    List<SellerSettlePO> getPlatformSettleDetail(@Param("communityId")Long communityId, @Param("settleMonth")String settleMonth);

    SellerSettlePO getSellerSettle(@Param("sellerId")Long sellerId, @Param("communityId")Long communityId, @Param("settleMonth")String settleMonth);

    List<SellerPaymentPO> getSellerSettleDetail(@Param("sellerId")Long sellerId, @Param("communityId")Long communityId, @Param("settleMonth")String settleMonth,
            Pagination pagination, RowBounds rb);
    
    List<SellerPaymentPO> getSellerSettleDetail(@Param("sellerId")Long sellerId, @Param("communityId")Long communityId, @Param("settleMonth")String settleMonth);
    
    /**
     * 获取该物业的未申请平台结算
     * 
     * @param communityId
     * @param settleMonth
     * @return
     */
    PlantformUnApply getPfApplySettle(@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
    /**
     * 获取该物业的未申请物业结算
     * 
     * @param communityId
     * @param settleMonth
     * @return
     */
    PlantformUnApply getEEPApplySettle(@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
    /**
     * 批量修改商家交易信息的平台结算状态
     * 
     * @param communityId
     * @param settleMonth
     */
    void batchUpdatePfApplyStatus(@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
    /**
     * 批量修改商家交易信息的物业结算状态
     * 
     * @param communityId
     * @param settleMonth
     */
    void batchUpdateEEPApplyStatus(@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
    /**
     * 获取需要处理的平台结算（平台与物业的结算，分页）
     * 
     * @param communityId (可选参数，传空则查询平台所有需要处理的申请，传具体数值则查询对应物业的结算申请)
     * @param pagination
     * @param rb
     * @return
     */
    List<PlantformSettlePO> getPfHandleSettleApply(@Param("subcomId")String subcomId, @Param("communityId")Long communityId,Pagination pagination,RowBounds rb);
    
    /**
     * 获取需要处理的平台结算（平台与物业的结算，不分页）
     * 
     * @param communityId
     * @return
     */
    List<PlantformSettlePO> getPfHandleSettleApply(@Param("subcomId")String subcomId, @Param("communityId")Long communityId);
    
    /**
     * 获取已经完成的平台结算（平台与物业的结算，分页）
     * 
     * @param communityId (可选参数，传空则查询平台所有需要处理的申请，传具体数值则查询对应物业的结算申请)
     * @param pagination
     * @param rb
     * @return
     */
    List<PlantformSettlePO> getPfFinishSettleApply(@Param("subcomId")String subcomId, @Param("communityId")Long communityId,Pagination pagination,RowBounds rb);
    
    /**
     * 获取已经完成的平台结算（平台与物业的结算，不分页）
     * 
     * @param communityId (可选参数，传空则查询平台所有需要处理的申请，传具体数值则查询对应物业的结算申请)
     * @return
     */
    List<PlantformSettlePO> getPfFinishSettleApply(@Param("subcomId")String subcomId, @Param("communityId")Long communityId);

    PlantformUnApply getEEPWaitApplySettleSum(@Param("communityId")Long communityId);
}
