package com.yc.eps.alipay;

import com.yc.edsi.payment.third.AlipayRSAPO;

public class Test {

    public static void main(String args[]) {
//        AlipayRSAPO alipayRSAPO = new AlipayRSAPO();
//        alipayRSAPO.setSubject("subject");
//        alipayRSAPO.setBody("body");
//        alipayRSAPO.setTotalFee(123);
//
//        String info = Test.getNewOrderInfo(alipayRSAPO);
//        String sign = Rsa.sign(info, ClientKeys.DEFAULT_PRIVATE);
//        try {
//            sign = URLEncoder.encode(sign, Constant.CHARSET);
//        } catch (UnsupportedEncodingException e) {
//        }
//
//        info += "&sign=\"" + sign + "\"&" + "sign_type=\"RSA\"";
//
//        System.out.println(info);

    }

    public static String getNewOrderInfo(AlipayRSAPO alipayRSAPO) {
        return null;
//        StringBuilder sb = new StringBuilder();
//        // 合作者身份ID,不可空
//        sb.append("partner=\"");
//        // 卖家支付宝账号，不可空
//        sb.append(ClientKeys.DEFAULT_PARTNER);
//        sb.append("\"&seller_id=\"");
//        sb.append(ClientKeys.DEFAULT_SELLER);
//        sb.append("\"&out_trade_no=\"");
//        sb.append(System.currentTimeMillis());
//        sb.append("\"&subject=\"");
//        sb.append(alipayRSAPO.getSubject());
//        sb.append("\"&body=\"");
//        sb.append(alipayRSAPO.getBody());
//        sb.append("\"&total_fee=\"");
//        sb.append(alipayRSAPO.getTotalFee());
//        sb.append("\"&notify_url=\"");
//
//        // 网址需要做URL编码
//        try {
//            sb.append(URLEncoder.encode("http://notify.java.jpxx.org/index.jsp", Constant.CHARSET));
//        } catch (UnsupportedEncodingException e) {
//        }
//        // 接口名称，固定值，不可空
//        sb.append("\"&service=\"mobile.securitypay.pay");
//        sb.append("\"&_input_charset=\"UTF-8");
//
//        // 支付类型。默认值为：1（商品购买）。不可空
//        sb.append("\"&payment_type=\"1");
//
//        // 未付款交易的超时时间，可空
//        sb.append("\"&it_b_pay=\"30m");
//        sb.append("\"");
//
//        return new String(sb);

    }
}
