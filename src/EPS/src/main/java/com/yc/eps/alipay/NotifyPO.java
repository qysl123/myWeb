package com.yc.eps.alipay;

public class NotifyPO {

    private int notifyId;

    private String sign;

    private String notifyData;

    private String ipAddr;

    private String createTime;

    private String tradeStatus;// 交易状态

    private String totalFee;// 交易金额

    private String subject;// 商品名称

    private String outTradeNo;

    private String notifyRegTime;// 通知时间

    private String tradeNo;// 支付宝交易号

    private String paymentType;// 支付类型,1：商品购买;8：机票购买

    private String price;// 商品单价,如果请求时使用的是total_fee，那么price等于total_fee；如果请求时使用的是price，那么对应请求时的price参数，原样通知回来。

    private String quantity;// 购买数量,如果请求时使用的是total_fee，那么quantity等于1；如果请求时使用的是quantity，那么对应请求时的quantity参数，原样通知回来。

    private String sellerId;// 卖家支付宝账户号,以2088开头的纯16位数字

    private String sellerEmail;// 卖家支付宝账号

    private String buyerId;// 买家支付宝账户号

    private String buyerEmail;// 买家支付宝账号

    private String discount;// 折扣金额。支付宝系统会把discount的值加到交易金额上，如果需要折扣，本参数为负数。

    private String useCoupon;// 是否使用红包

    private String isTotalFeeSdjust;// 交易金额是否修改过

    private String gmtCreate;// 交易创建时间

    private String gmtPayment;// 买家付款时间

    private String gmtClose;// 交易结束时间

    private String outChannelType;// 支付渠道组合信息

    private String outChannelAmount;// 支付金额组合信息

    public int getNotifyId() {
        return notifyId;
    }

    public void setNotifyId(int notifyId) {
        this.notifyId = notifyId;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getNotifyData() {
        return notifyData;
    }

    public void setNotifyData(String notifyData) {
        this.notifyData = notifyData;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(String tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public String getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(String totalFee) {
        this.totalFee = totalFee;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getNotifyRegTime() {
        return notifyRegTime;
    }

    public void setNotifyRegTime(String notifyRegTime) {
        this.notifyRegTime = notifyRegTime;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getUseCoupon() {
        return useCoupon;
    }

    public void setUseCoupon(String useCoupon) {
        this.useCoupon = useCoupon;
    }

    public String getIsTotalFeeSdjust() {
        return isTotalFeeSdjust;
    }

    public void setIsTotalFeeSdjust(String isTotalFeeSdjust) {
        this.isTotalFeeSdjust = isTotalFeeSdjust;
    }

    public String getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(String gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getGmtPayment() {
        return gmtPayment;
    }

    public void setGmtPayment(String gmtPayment) {
        this.gmtPayment = gmtPayment;
    }

    public String getGmtClose() {
        return gmtClose;
    }

    public void setGmtClose(String gmtClose) {
        this.gmtClose = gmtClose;
    }

    public String getOutChannelType() {
        return outChannelType;
    }

    public void setOutChannelType(String outChannelType) {
        this.outChannelType = outChannelType;
    }

    public String getOutChannelAmount() {
        return outChannelAmount;
    }

    public void setOutChannelAmount(String outChannelAmount) {
        this.outChannelAmount = outChannelAmount;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("notifyId : ");
        sb.append(notifyId);
        sb.append(",sign : ");
        sb.append(sign);
        sb.append(",notifyData : ");
        sb.append(notifyData);
        sb.append(",ipAddr : ");
        sb.append(ipAddr);
        sb.append(",createTime : ");
        sb.append(createTime);

        return sb.toString();
    }

}
