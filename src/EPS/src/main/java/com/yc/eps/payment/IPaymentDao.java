package com.yc.eps.payment;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import com.yc.commons.mybatis.pagination.vo.Pagination;
import com.yc.edsi.count.CountPO;
import com.yc.edsi.count.StatQueryCond;
import com.yc.edsi.payment.AdvertIncomePO;
import com.yc.edsi.payment.AdvertOut;
import com.yc.edsi.payment.OwnerAccount;
import com.yc.edsi.payment.OwnerPaymentPO;
import com.yc.edsi.payment.OwnerPaymentStatisticsPO;
import com.yc.edsi.payment.PlantformSettleAdvertPO;
import com.yc.edsi.payment.PlantformSettleDetailPO;
import com.yc.edsi.payment.PlantformSettlePO;
import com.yc.edsi.payment.PlantformUnApply;
import com.yc.edsi.payment.SellerPaymentPO;
import com.yc.edsi.payment.SellerSettleDetailPO;
import com.yc.edsi.payment.SellerSettlePO;

public interface IPaymentDao {

    void insertOwnerPayment(OwnerPaymentPO ownerPaymentPO);

    Double getOwnerAccountBalance(Map<String, Object> sqlMap);

    OwnerPaymentPO getOwnerPaymentPO(Map<String, Object> sqlMap);
    
    int getOwnerAccountListCount(Map<String, Object> sqlMap);

    List<OwnerPaymentPO> getOwnerAccountList(Map<String, Object> sqlMap);

    OwnerPaymentPO getOwnerAccount(Map<String, Object> sqlMap);

    void insertSellerPayment(SellerPaymentPO sellerPaymentPO);
    
    /**
     * 批量修改商家交易信息申请结算状态
     * @param sellerId
     * @param communityId
     * @param settleMonth
     */
    void batchUpdateApplyStatus(@Param("sellerId")Long sellerId,@Param("communityId")Long communityId
    		,@Param("settleMonth")String settleMonth);
    
    /**
     * 批量修改商家交易结算完成状态为已结算
     * @param sellerSettleId
     */
    void batchUpdateFinishStatus(@Param("sellerSettleId")Long sellerSettleId);

    /**
     * 根据票据号查询该票据号的列表，主要用于判重
     * 
     * @param billPO
     * @return
     */
    List<OwnerPaymentPO> getListByBillNo(String billNo);

    int getAllOwnerAccountListCount(@Param("op")OwnerPaymentPO ownerPaymentPO,@Param("rangePropertyIds")String rangePropertyIds,
    		@Param("propertyIds")List<Long> propertyIds);

    List<OwnerPaymentPO> getAllOwnerAccountList(@Param("op")OwnerPaymentPO ownerPaymentPO,@Param("rangePropertyIds")String rangePropertyIds,
    		@Param("propertyIds")List<Long> propertyIds);

    /**
     * 用户充值统计
     * 
     * @param sqlMap
     * @return
     */
    List<OwnerPaymentStatisticsPO> getRechargeStatistics(Map<String, Object> sqlMap);

    /**
     * 上月统计明细
     * 
     * @param sqlMap
     * @return
     */
    List<OwnerPaymentStatisticsPO> getStatisticsDetail(Map<String, Object> sqlMap);

    /**
     * 计算条数
     * 
     * @param sqlMap
     */
    int getRechargeStatisticsCount(Map<String, Object> sqlMap);

    /**
     * 充值提现明细
     * 
     * @param sqlMap
     * @return
     */
    List<OwnerPaymentStatisticsPO> getReceivableStatistics(Map<String, Object> sqlMap);

    /**
     * 通过订单Id获取支付信息
     * 
     * @param orderId
     * @return
     */
    OwnerPaymentPO getOwnerPaymentInfo(@Param("orderId") Long orderId, @Param("communityId")Long communityId);

    /**
     * 修改未结算账户流水的票据号
     * 
     * @param sqlMap
     * @return
     */
    void updateBillNo(Map<String, Object> sqlMap);
    
	/**
	 * 获取结算月没有结算的订单详细信息（分页）
	 * @param sellerId
	 * @param communityId
	 * @param settleMonth
	 * @param page  分页对象
	 * @param rb
	 * @return
	 */
	List<SellerPaymentPO> getWaitApplySettleDetail(@Param("sellerId")Long sellerId,@Param("communityId")Long communityId,
			@Param("settleMonth")String settleMonth,Pagination page,RowBounds rb);
	
	/**
	 * 获取结算月份没有结算的订单详细信息（不分页）
	 * @param sellerId
	 * @param communityId
	 * @param settleMonth
	 * @return
	 */
	List<SellerPaymentPO> getWaitApplySettleDetail(@Param("sellerId")Long sellerId,
			@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
	/**
	 * 根据结算Id获取结算详细信息
	 * @param sellerSettleId
	 * @param page  分页对象
	 * @param rb
	 * @return
	 */
	List<SellerPaymentPO> getSellerSettleDetail(@Param("sellerSettleId")Long sellerSettleId,Pagination page,RowBounds rb);
	
	/**
	 * 根据结算Id获取结算详细信息
	 * @param sellerSettleId
	 * @return
	 */
	List<SellerPaymentPO> getSellerSettleDetail(@Param("sellerSettleId")Long sellerSettleId);

	/**
	 * 插入结算申请
	 * @param approvSettle
	 */
	void insertSellerSettle(@Param("approveSettle")SellerSettlePO approveSettle);
    
	/**
	 * 批量插入申请明细
	 * @param ssDetailList
	 */
	void batchInsertSettleDetail(@Param("ssdList")List<SellerSettleDetailPO> ssDetailList);
    
	/**
	 * 修改结算申请
	 * @param settle
	 */
	void updateSettle(@Param("settle")SellerSettlePO settle);
    
	/**
	 * 获取物业还未处理完毕的商家结算申请(物业端使用,分页)
	 * @param communityId
	 * @return
	 */
	List<SellerSettlePO> getSellerSettleApply(@Param("pfType")String pfType, @Param("ss")SellerSettlePO sellerSettlePO,
			Pagination pagination, RowBounds rb);
	
	/**
	 * 获取物业还未处理完毕的商家结算申请合计
	 * @param communityId
	 * @return
	 */
	SellerSettlePO getSellerSettleSum(@Param("pfType")String pfType, @Param("ss")SellerSettlePO sellerSettlePO);
    
    /**
     * 获取物业还未处理完毕的商家结算申请(物业端使用,不分页)
     * @param communityId
     * @return
     */
    List<SellerSettlePO> getSellerSettleApply(@Param("pfType")String pfType, @Param("ss")SellerSettlePO sellerSettlePO);
	
    /**
     * 通过结算申请Id获取结算信息(商家端使用)
     * @param sellerSettleId
     * @return
     */
	SellerSettlePO getSellerSettle(@Param("sellerSettleId")Long sellerSettleId);
    
	/**
	 * 获取已经提交但未处理完毕的结算信息
	 * @param sellerId
	 * @return
	 */
	List<SellerSettlePO> getSubmitSettlementApply(@Param("sellerId")Long sellerId);
    
	/**
	 * 获取物业待审批的所有交易信息
	 * @param communityId
	 * @return
	 */
	List<SellerPaymentPO> getSellerSettleApplyDetail(@Param("pfType")String pfType, @Param("ss")SellerSettlePO ss);

	/**
	 * 获取已经完成的结算信息
	 * @param communityId
	 * @param ss(使用开始月份和结束月份进行查询)
	 * @param pagination
	 * @param rb
	 * @return
	 */
	List<SellerSettlePO> getFinishSettleApply(@Param("pfType")String pfType, @Param("ss")SellerSettlePO ss,
			Pagination pagination, RowBounds rb);
    
	/**
	 * 获取卖家结算申请历史(完成状态)
	 * @param sellerId
	 * @param pagination
	 * @param rb
	 * @return
	 */
	List<SellerSettlePO> getSettlementApplyHistory(@Param("sellerId")Long sellerId, Pagination pagination, RowBounds rb);
    
	/**
	 * 获取平台未结算的信息
	 * @param pagination
	 * @param rb
	 * @return
	 */
	List<PlantformUnApply> getPlantformWaitApplySettle(@Param("subcomId")String subcomId, 
			Pagination pagination,RowBounds rb);
    
    /**
     * 获取物业未结算的信息
     * @param pagination
     * @param rb
     * @return
     */
    List<PlantformUnApply> getEEPWaitApplySettle(@Param("communityId")Long communityId,
            Pagination pagination,RowBounds rb);
	
	/**
	 * 获取平台未结算的信息
	 * @param pagination
	 * @param rb
	 * @return
	 */
	List<PlantformUnApply> getPlantformWaitApplySettle(@Param("subcomId")String subcomId);
    
    /**
     * 获取物业未结算的信息
     * @param pagination
     * @param rb
     * @return
     */
    List<PlantformUnApply> getEEPWaitApplySettle(@Param("communityId")Long communityId);
	
	/**
	 * 获取平台未结算的详细信息(分页)
	 * @param lastSettleTime
	 * @param curSettleTime
	 * @param communityId
	 * @param pagination
	 * @param rb
	 * @return
	 */
	List<SellerSettlePO> getPfUnSettleDetail(@Param("communityId") Long communityId,@Param("settleMonth")String settleMonth,
			Pagination pagination,RowBounds rb);
    
    /**
     * 获取物业未结算的详细信息(分页)
     * @param lastSettleTime
     * @param curSettleTime
     * @param communityId
     * @param pagination
     * @param rb
     * @return
     */
    List<SellerSettlePO> getEEPUnSettleDetail(@Param("communityId") Long communityId,@Param("settleMonth")String settleMonth,
            Pagination pagination,RowBounds rb);
	
	/**
	 * 获取平台未结算的详细信息(不分页)
	 * @param communityId
	 * @param settleMonth
	 * @return
	 */
	List<SellerSettlePO> getPfUnSettleDetail(@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
    /**
     * 获取物业未结算的详细信息(不分页)
     * @param communityId
     * @param settleMonth
     * @return
     */
    List<SellerSettlePO> getEEPUnSettleDetail(@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
	/**
	 * 获取平台已经提交申请的结算的详细信息
	 * @param plantformSettleId
	 * @param pagination
	 * @param rb
	 * @return
	 */
	List<SellerSettlePO> getPfSettleDetail(@Param("plantformSettleId")Long plantformSettleId,Pagination pagination, RowBounds rb);
	
	/**
	 * 获取平台已经提交申请的结算的详细信息
	 * @param plantformSettleId
	 * @return
	 */
	List<SellerSettlePO> getPfSettleDetail(@Param("plantformSettleId")Long plantformSettleId);
	
    /**
     * 插入平台结算信息
     * @param pfSettle
     */
	void insertPlantformSettle(@Param("pfSettle")PlantformSettlePO pfSettle);
    
	/**
	 * 批量插入平台结算明细
	 * @param psdList
	 */
	void batchInsertPFSettleDetail(@Param("psdList")List<PlantformSettleDetailPO> psdList);
    
	/**
	 * 批量修改商家结算信息（平台申请结算）
	 * @param communityId
	 * @param settleMonth
	 */
	void pfBatchUpdateSellerSatus(@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
    /**
     * 批量修改商家结算信息（物业申请结算）
     * @param communityId
     * @param settleMonth
     */
    void eepBatchUpdateSellerSatus(@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
	/**
	 * 获取需要处理的结算申请（平台与物业的结算）
	 * @param communityId (可选参数，传空则查询平台所有需要处理的申请，传具体数值则查询对应物业的结算申请)
	 * @param pagination
	 * @param rb
	 * @return
	 */
	List<PlantformSettlePO> getPfHandleSettleApply(@Param("subcomId")String subcomId, @Param("communityId")Long communityId,Pagination pagination,RowBounds rb);
    
    /**
     * 获取需要处理的结算申请（物业与平台的结算）
     * @param communityId (可选参数，传空则查询平台所有需要处理的申请，传具体数值则查询对应物业的结算申请)
     * @param pagination
     * @param rb
     * @return
     */
    List<PlantformSettlePO> getEEPHandleSettleApply(@Param("subcomId")String subcomId, @Param("communityId")Long communityId,Pagination pagination,RowBounds rb);
	
	/**
	 * 获取需要处理的结算申请（平台与物业的结算,不分页）
	 * @param communityId
	 * @return
	 */
	List<PlantformSettlePO> getPfHandleSettleApply(@Param("subcomId")String subcomId, @Param("communityId")Long communityId);
    
    /**
     * 获取需要处理的结算申请（物业与平台的结算,不分页）
     * @param communityId
     * @return
     */
    List<PlantformSettlePO> getEEPHandleSettleApply(@Param("subcomId")String subcomId, @Param("communityId")Long communityId);
	
	/**
	 * 获取已经完成的结算信息（平台与物业的结算）
	 * @param communityId (可选参数，传空则查询平台所有需要处理的申请，传具体数值则查询对应物业的结算申请)
	 * @param pagination
	 * @param rb
	 * @return
	 */
	List<PlantformSettlePO> getPfFinishSettleApply(@Param("subcomId")String subcomId, @Param("communityId")Long communityId,Pagination pagination,RowBounds rb);
    
    /**
     * 获取已经完成的结算信息（物业与平台的结算）
     * @param communityId (可选参数，传空则查询平台所有需要处理的申请，传具体数值则查询对应物业的结算申请)
     * @param pagination
     * @param rb
     * @return
     */
    List<PlantformSettlePO> getEEPFinishSettleApply(@Param("subcomId")String subcomId, @Param("communityId")Long communityId,Pagination pagination,RowBounds rb);
	
	/**
	 * 获取平台与物业已经完成的结算申请(不分页)
	 * @param communityId(可选)
	 * @return
	 */
	List<PlantformSettlePO> getPfFinishSettleApply(@Param("subcomId")String subcomId, @Param("communityId")Long communityId);
    
    /**
     * 获取物业与平台已经完成的结算申请(不分页)
     * @param communityId(可选)
     * @return
     */
    List<PlantformSettlePO> getEEPFinishSettleApply(@Param("subcomId")String subcomId, @Param("communityId")Long communityId);
    
    /**
     * 获取平台结算相关的商家结算信息
     * @param plantformSettleId
     * @param pagination
     * @param rb
     * @return
     */
	List<SellerSettlePO> getRelatedSellerSettleApply(@Param("plantformSettleId")Long plantformSettleId,Pagination pagination, RowBounds rb);
    
	/**
	 * 根据平台结算信息Id获取平台结算信息
	 * @param plantformSettleId
	 * @return
	 */
	PlantformSettlePO getPlantformSettle(@Param("plantformSettleId")Long plantformSettleId);
    
	/**
	 * 修改平台结算信息
	 * @param pfSettle
	 */
	void updatePfSettle(@Param("pfSettle")PlantformSettlePO pfSettle);
    
	/**
	 * 获取该物业的未结算申请(平台未申请)
	 * @param communityId
	 * @param settleMonth
	 * @return
	 */
	PlantformUnApply getPfApplySettle(@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
    /**
     * 获取该物业的未结算申请(物业未申请)
     * @param communityId
     * @param settleMonth
     * @return
     */
    PlantformUnApply getEEPApplySettle(@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
	/**
	 * 获取商家待结算的信息(按月划分)
	 * @param sellerId
	 * @return
	 */
	List<SellerSettlePO> getSellerWaitApplySettleList(@Param("sellerId")Long sellerId);
    
	/**
	 * 
	 * @param sellerId
	 * @param communityId
	 * @param settleMonth
	 * @return
	 */
	SellerSettlePO getSellerWaitApplySettle(@Param("sellerId")Long sellerId,
			@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);

	/**
     * 获取物业已经完成的结算的统计信息(和商家结算的)
     * @param communityId
     * @param sellerSettle
     * @return
     */
	SellerSettlePO getFinishSettleSum(@Param("pfType")String pfType, @Param("ss")SellerSettlePO ss);

	/**
	 * 获取订单ID列表中已结算的订单ID列表
	 * 
	 * @param orderIds
	 * @return
	 */
	List<Long> getSettledOrderIds(@Param("orderIds")List<Long> orderIds);
    
	/**
	 * 获取账户现金金额和返现金额
	 * @param ownerId
	 * @param communityId
	 * @return
	 */
	OwnerAccount getAccount(@Param("ownerId")Long ownerId,@Param("communityId")Long communityId);

	/**
	 * 根据订单Id获取商家待结算信息
	 * @param orderId
	 * @return
	 */
	SellerPaymentPO getSellerPaymentByOrderId(@Param("orderId")Long orderId);
    
	/**
	 * 删除代金券充值
	 * @param ownerId
	 * @param cashSendType
	 * @param cashId
	 */
    void delCashPayment(@Param("ownerId") Long ownerId, @Param("cashSendType") String cashSendType,
            @Param("cashId") Long cashId, @Param("communityId") Long communityId);

	/**
	 * 删除支付信息
	 * @param paymentId
	 */
	void delOwnerPayment(@Param("paymentId")Long paymentId,@Param("communityId")Long communityId);

	/**
	 * 删除待结算信息
	 * @param oPaymentId
	 * @param orderId
	 */
	void delSellerPayment(@Param("oPaymentId")Long oPaymentId,@Param("orderId")Long orderId);

	/**
	 * 查询广告收入
	 * @param ai
	 * @param pagination
	 * @param rb
	 * @return
	 */
	List<AdvertIncomePO> getAdvertIncomeList(@Param("ai")AdvertIncomePO ai,Pagination pagination,
			RowBounds rb);

    /**
     * 根据物业ID查询广告收入列表
     * 
     * @param ai
     * @param pagination
     * @param rb
     * @return
     */
    List<AdvertIncomePO> getAdvertIncomesByCommId(@Param("ai") AdvertIncomePO ai, Pagination pagination,
            RowBounds rb);
    
	/**
	 * 获取广告收入详情
	 * @param id
	 * @return
	 */
	AdvertIncomePO getAdvertIncomeById(@Param("id")Long id);
    
	/**
	 * 保存广告收入
	 * @param ai
	 */
	void insertAdvertIncome(@Param("ai")AdvertIncomePO ai);
	
	/**
	 * 修改广告收入
	 * @param ai
	 */
	void updateAdvertIncome(@Param("ai")AdvertIncomePO ai);
    
	/**
	 * 插入支出数据
	 * @param ao
	 */
	void insertAdvertOut(@Param("ao")AdvertOut ao);

	/**
	 * 修改支出数据
	 * @param ao
	 */
	void updateAdvertOut(@Param("ao")AdvertOut ao);
    
	/**
	 * 通过广告收入Id获取相关广告支出信息
	 * @param adIncomeId
	 * @return
	 */
	List<AdvertOut> getAdvertOutByIncomeId(@Param("adIncomeId")Long adIncomeId);
    
	/**
	 * 获取传入物业传入月份待结算广告收入信息
	 * @param communityId
	 * @param settleMonth
	 * @return
	 */
    List<AdvertIncomePO> getSettleAdvertIncomeList(@Param("communityId")Long communityId,
            @Param("settleMonth")String settleMonth);
    
    /**
     * 批量插入参与结算广告收入信息
     * @param psaList
     */
    void batchInsertPFAdvertSettle(@Param("psaList")List<PlantformSettleAdvertPO> psaList);

    /**
     * 批量修改广告收入信息结算状态
     * @param communityId
     * @param settleMonth
     * @param settleApply
     */
    void batchUpdateAdvertIncomeStatus(@Param("communityId")Long communityId,
            @Param("settleMonth")String settleMonth,@Param("settleApply")String settleApply);
    /**
     * 获取传入物业、月份未结算广告信息
     * @param communityId
     * @param settleMonth
     * @param pagination
     * @param rb
     * @return
     */
    List<AdvertIncomePO> getPfUnSettleAdvertList(@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth,
            Pagination pagination, RowBounds rb);
    
    /**
     * 获取传入结算Id对应的结算中或者结算完成的广告收入信息
     * @param plantformSettleId
     * @param pagination
     * @param rb
     * @return
     */
    List<AdvertIncomePO> getPfSettleAdvertList(@Param("plantformSettleId")Long plantformSettleId, Pagination pagination, RowBounds rb);

    /***
     * 获取传入结算Id对应的结算中或者结算完成的广告收入信息
     * @param plantformSettleId
     * @return
     */
    List<AdvertIncomePO> getPfSettleAdvertList(@Param("plantformSettleId")Long plantformSettleId);
    
    /***
     * 通过物业Id和月份获取广告总收入和总支出
     * @param communityId
     * @param settleMonth
     * @return
     */
    AdvertIncomePO getTotalAdvertByComMonth(@Param("communityId")Long communityId,@Param("settleMonth")String settleMonth);
    
    /**
     * 通过平台结算Id获取广告总收入和总支出
     * @param plantformSettleId
     * @return
     */
    AdvertIncomePO getTotalAdvertByPfSettle(@Param("plantformSettleId")Long plantformSettleId);

    /**
     * 按照物业ID及时间统计广告收入
     * 
     * @param countPO
     * @return
     */
    List<CountPO> statAdvert(@Param("osqc") StatQueryCond osqc);
    
    /**
     * 获取订单已经支付次数
     * @param orderId
     * @return
     */
	int checkIfAlreadyPay(@Param("orderId")Long orderId,@Param("communityId")Long communityId);

	/**
	 * 检测商家是否已经提交了结算申请(返回提交的数据行数)
	 * @param sellerId
	 * @param communityId
	 * @param settleMonth
	 * @return
	 */
    int checkSellerIsSubmitted(@Param("sellerId")Long sellerId,@Param("communityId")Long communityId, @Param("settleMonth")String settleMonth);

    void updateSettleByOpid(@Param("oPaymentId") Long oPaymentId, @Param("orderId") Long orderId);
}
