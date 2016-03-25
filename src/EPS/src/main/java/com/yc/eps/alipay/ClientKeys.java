package com.yc.eps.alipay;

/*
 * Copyright (C) 2010 The MobileSecurePay Project
 * All right reserved.
 * author: shiqun.shi@alipay.com
 * 
 *  提示：如何获取安全校验码和合作身份者id
 *  1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *  2.点击“商家服务”(https://b.alipay.com/order/myorder.htm)
 *  3.点击“查询合作者身份(pid)”、“查询安全校验码(key)”
 */

//
// 请参考 Android平台安全支付服务(msp)应用开发接口(4.2 RSA算法签名)部分，并使用压缩包中的openssl RSA密钥生成工具，生成一套RSA公私钥。
// 这里签名时，只需要使用生成的RSA私钥。
// Note: 为安全起见，使用RSA私钥进行签名的操作过程，应该尽量放到商家服务器端去进行。
public final class ClientKeys {

    // 合作身份者id，以2088开头的16位纯数字
    private static final String DEFAULT_PARTNER = "2088311195597362";

    // 收款支付宝账号
    private static final String DEFAULT_SELLER = "2088311195597362";

    // 商户私钥，自助生成
    private static final String DEFAULT_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMkQ6i1PG5Iv11YqJkt3uMvDWqk7e9fmneAbwZhtB4AFhJ6eJuBHeLVSVmAzSh2y4wawK+iNnEBdALQzRjVWZ3ENrG3+NKgyNzogsXnNif+Rx70uNySaLfmCiWz0HLFy6k7PIhn+1StlZKGzjvqQU1Mg/17+Q1KDm+wxXNQLCTvVAgMBAAECgYEAnvR7XdSThG/D+n+aBMGiW5yWeQHot1e/aiQnWfygGErEfK2lKFv4hsiQNr91kaMRrgDqxY/mfk/INa3vjE1BuozrulgsmobowGqrKGYSjgdhdb/qBVEej8Dl7ovTDR/ztES4X+Nx2G5CHq+wV23cDLupwY9pD3GJ2vWxXuMeqIUCQQDpLreET2L92MdWwF1s397DFYgV2kM3adDSy95xik2qPl0mvYzH73PKLd0ehMJtLirs3uooxaRQ8GRImkj53CG/AkEA3L2sVm8cpB0wxkcW6B+PrfvqyPs0cujB/+aCvyQbKltVt3MrTIA4FSVmgsDFLFfN/x3vcyqMWpPKrLrK9k8fawJARMUCWoMPLs4/+9W/t9xOVjqDZ+525GDQtVpqwBGJ1iuOTV6Zpl14SKg+DkIZFRoIjtMvO9cOHtDGwqK94vKJAQJASjbzBDRjOsIdrewkxXInCmXSMDRVE/UxlGnZt43aHEmM7hqoihXpxSJH7toE8L2sooNcCCpb+fAKijxbFKjRdQJAVZ90ddAHJJmi6QWWkL+BmyWe8jkVkZjTmZfBKYa6CHGugnsi6X/ChSJo4lGue6GiKX7+N7oCXvjGEw6kmIbJrw==";

    // public static final String DEFAULT_PUBLIC =
    // "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDJEOotTxuSL9dWKiZLd7jLw1qpO3vX5p3gG8GYbQeABYSenibgR3i1UlZgM0odsuMGsCvojZxAXQC0M0Y1VmdxDaxt/jSoMjc6ILF5zYn/kce9Ljckmi35gols9ByxcupOzyIZ/tUrZWShs476kFNTIP9e/kNSg5vsMVzUCwk71QIDAQAB";

    private static final String DEFAULT_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

    public ClientKeys(boolean isDefault) {
        if (isDefault) {
            this.partner = DEFAULT_PARTNER;
            this.seller = DEFAULT_SELLER;
            this.privateKey = DEFAULT_PRIVATE;
            this.publicKey = DEFAULT_PUBLIC_KEY;
        }
    }

    private String partner;
    private String seller;
    private String privateKey;
    private String publicKey;

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

}
