package com.zk.common;

import com.zk.base.AES;
import jodd.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Tools.class);

    public static String getStatusDisplay(String status) {
        if ("1".equals(status)) {
            return "正常";
        } else if ("2".equals(status)) {
            return "停用";
        } else {
            return "已删除";
        }
    }

    /**
     * 如果传入的字符串长度是11，则有可能是手机号，屏蔽中间4位数字
     * 
     * @param name
     * @return
     */
    public static String getDispNickName(String name) {
        if (StringUtils.isNotBlank(name) && isMobile(name)) {
            return name.substring(0, 3) + "****" + name.substring(7, 11);
        } else {
            return name;
        }
    }

    public static String format2MoneyStr(Double d) {
        DecimalFormat format = new DecimalFormat("0.00");
        return format.format(d);
    }

    public static Double format2Money(Double d) {
        BigDecimal b = new BigDecimal(d);
        return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static Double format2Long(Double d) {
        BigDecimal b = new BigDecimal(d);
        return b.setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static int getRandomInt(int min, int max) {
        if (min < 0) {
            min = 0;
        }
        return (new Random()).nextInt(max - min + 1) + min;
    }

    public static String getMd5(String src) {
        try {
            MessageDigest messageDigest = null;
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(src.getBytes("UTF-8"));
            return bytes2HexString(messageDigest.digest());
        } catch (Exception e) {
        }
        return null;
    }

    public static String getToken(String... args) {
        if (args == null || args.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder("");
        for (String var : args) {
            sb.append(var);
            sb.append("-");
        }
        sb.append(System.currentTimeMillis());
        return getMd5UpperString(sb.toString());
    }

    /**
     * 返回大写的MD5
     * 
     * @param src
     * @return
     */
    public static String getMd5UpperString(String src) {
        try {
            return getMd5(src).toUpperCase();
        } catch (Exception e) {
        }
        return null;
    }

    public static String bytes2HexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length * 2);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp);
        }
        return sb.toString();
    }
    
    public static byte[] HexStr2Byte(String hexStr) {  
        if (hexStr.length() < 1)  {            
            return null;  
        }
        byte[] result = new byte[hexStr.length()/2];  
        for (int i = 0;i< hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);  
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);  
            result[i] = (byte) (high * 16 + low);  
        }  
        return result;  
    }  

    public static int parseInt(String s, int defaultVal) {
        int result = defaultVal;
        try {
            result = Integer.parseInt(s);
        } catch (Exception e) {

        }
        return result;
    }

    public static long parseLong(String s, long defaultVal) {
        long result = defaultVal;
        try {
            result = Long.parseLong(s);
        } catch (Exception e) {

        }
        return result;
    }

    public static double parseDouble(String s, double defaultVal) {
        double result = defaultVal;
        try {
            result = Double.parseDouble(s);
        } catch (Exception e) {

        }
        return result;
    }

    public static String add(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return (b1.add(b2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()) + "";
    }

    public static BigDecimal addDecimal(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.add(b2).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double subtract(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.subtract(b2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static BigDecimal subtractDecimal(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.subtract(b2).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static double subtract(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double multiply(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double multiply(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.multiply(b2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static BigDecimal multiplyDecimal(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.multiply(b2).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static double divide(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double divide(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String double2LongStr(double dou) {
        Double d = dou;
        return String.valueOf(d.longValue());
    }

    public static byte[] compressImage(InputStream inputStream, int maxImgWidth, int maxImgHeight, Boolean stretch) {
        ByteArrayOutputStream out = null;
        try {
            Image image = ImageIO.read(inputStream);
            int imageWidth = image.getWidth(null);
            int imageHeight = image.getHeight(null);

            if (stretch) {
                if (maxImgWidth != 0) {
                    if (maxImgHeight != 0) {
                        // 宽高都有，直接赋值
                        imageWidth = maxImgWidth;
                        imageHeight = maxImgHeight;
                    } else {
                        // 只有宽度没有高度
                        if (imageWidth > maxImgWidth) {
                            imageHeight = maxImgWidth * imageHeight / imageWidth;
                            imageWidth = maxImgWidth;
                        }
                    }
                } else {
                    if (maxImgHeight != 0) {
                        // 只有高度，没有宽度
                        if (imageHeight > maxImgHeight) {
                            imageWidth = maxImgHeight * imageWidth / imageHeight;
                            imageHeight = maxImgHeight;
                        }
                    }
                }
            } else {
                if (maxImgWidth > 0 && maxImgHeight > 0) {
                    double sx = (double) maxImgWidth / imageWidth;
                    double sy = (double) maxImgHeight / imageHeight;

                    if (sx < sy) {
                        imageWidth = (int) (sx * imageWidth);
                        imageHeight = (int) (sx * imageHeight);
                    } else {
                        imageWidth = (int) (sy * imageWidth);
                        imageHeight = (int) (sy * imageHeight);
                    }
                } else if (maxImgWidth > 0) {
                    imageHeight = maxImgWidth * imageHeight / imageWidth;
                    imageWidth = maxImgWidth;
                } else if (maxImgHeight > 0) {
                    imageWidth = maxImgHeight * imageWidth / imageHeight;
                    imageHeight = maxImgHeight;
                }
            }
            BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
            bufferedImage.getGraphics().drawImage(image.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH),
                    0, 0, null);

            out = new ByteArrayOutputStream();
            IIOImage outputImage = new IIOImage(bufferedImage, null, null);
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(out);
            writer.setOutput(ios);
            JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) writer.getDefaultWriteParam();
            //以下两行代码是质量压缩的代码，暂时屏蔽，如果需要压缩，打开注释即可
//            jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
//            jpegParams.setCompressionQuality(0.99f);
            IIOMetadata iioMetadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(bufferedImage), jpegParams);
            writer.write(iioMetadata, outputImage, jpegParams);

            byte[] result = out.toByteArray();
            writer.dispose();
            return result;
        } catch (IOException e) {
            LOGGER.error(Tools.class.getName(), e);

        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }

    /** 图片格式：JPG */
    private static final String PICTRUE_FORMATE_JPG = "jpg";

    /**
     * 添加图片水印
     * 
     * @param srcImg
     *            目标图片路径，如：C://myPictrue//1.jpg
     * @param waterImg
     *            水印图片路径，如：C://myPictrue//logo.png
     * @param x
     *            水印图片距离目标图片左侧的偏移量，如果x<0, 则在正中间
     * @param y
     *            水印图片距离目标图片上侧的偏移量，如果y<0, 则在正中间
     * @param alpha
     *            透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明)
     */
    public final static void pressImage(String srcImg, String waterImg, String destImg, int x, int y, float alpha)
            throws Exception {
        Image image = null;
        if (srcImg.toLowerCase().indexOf("http") == 0) {
            image = ImageIO.read(new URL(srcImg));
        } else {
            File file = new File(srcImg);
            image = ImageIO.read(file);
        }
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);

        Image waterImage = ImageIO.read(new File(waterImg)); // 水印文件
        int width_1 = waterImage.getWidth(null);
        int height_1 = waterImage.getHeight(null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));

        int widthDiff = width - width_1;
        int heightDiff = height - height_1;
        if (x < 0) {
            x = widthDiff / 2;
        } else if (x > widthDiff) {
            x = widthDiff;
        }
        if (y < 0) {
            y = heightDiff / 2;
        } else if (y > heightDiff) {
            y = heightDiff;
        }
        g.drawImage(waterImage, x, y, width_1, height_1, null); // 水印文件结束
        g.dispose();
        File destFile = new File(destImg);
        ImageIO.write(bufferedImage, PICTRUE_FORMATE_JPG, destFile);
    }

    /**
     * 清除html代码
     * <p>
     * 所有包括在'<'与'>'之间的内容全部都会被清除掉,并返回
     * </P>
     * 
     * @param args
     * @param replaceNull
     *            是否替换空格等制表符
     * @return String
     */
    public static String clearHTMLToString(String str, boolean replaceNull) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        str = str.replaceAll("<[^>]+>", "");
        if (replaceNull) {
            str = str.replaceAll("\\s*|\t|\r|\n", "");
        }

        str = str.replaceAll("&hellip;", "…");
        str = str.replaceAll("&ldquo;", "“");
        str = str.replaceAll("&rdquo;", "”");
        str = str.replaceAll("&quot;", "\"");
        str = str.replaceAll("&quot；", "\"");
        str = str.replaceAll("&mdash;", "—");
        str = str.replaceAll("&middot;", "");
        str = str.replaceAll("&middot；", "");
        str = str.replaceAll("&lsquo;", "");
        str = str.replaceAll("&rsquo;", "");
        str = str.replaceAll("&lt;", "<");
        str = str.replaceAll("&gt;", ">");
        str = str.replaceAll("&nbsp;", " ");
        return str;
    }

    public static String getCellVal(HSSFCell cell) {
        String result = "";
        if (cell == null) {
            return result;
        }
        int ct = cell.getCellType();
        switch (ct) {
            case HSSFCell.CELL_TYPE_STRING:
                result = cell.getStringCellValue();
                break;
            case HSSFCell.CELL_TYPE_NUMERIC:
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    result = cell.getDateCellValue().toString();
                } else {
                    result = new BigDecimal(cell.getNumericCellValue()).toString();
                }
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                result = String.valueOf(cell.getNumericCellValue());
                break;
        }
        return result;
    }

    public static String d2l(String dStr) {
        double d = 0.0d;
        try {
            d = Double.parseDouble(dStr);
        } catch (Exception e) {
        }
        long l = (long) d;
        return String.valueOf(l);
    }

    public static boolean isMobile(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1-9][3-9][0-9]{9}$"); // 验证手机号
        m = p.matcher(str);
        b = m.matches();
        return b;
    }

    private final static double PI = 3.14159265358979323; // 圆周率
    private final static double R  = 6371229;            // 地球的半径

    public static double getDistance(double longt1, double lat1, double longt2, double lat2) {
        double x, y, distance;
        x = (longt2 - longt1) * PI * R * Math.cos(((lat1 + lat2) / 2) * PI / 180) / 180;
        y = (lat2 - lat1) * PI * R / 180;
        distance = Math.hypot(x, y);
        return distance;
    }

    public static String removeParanthese(String sources) {
        int startIndex = sources.indexOf("(");
        if (startIndex == -1) {
            return sources;
        }
        return sources.substring(0, startIndex);
    }

    public static String removeBracket(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        if (str.indexOf("[") == 0) {
            str = str.substring(1);
        }
        if (str.indexOf("]") == (str.length() - 1)) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * 加密
     * @param content  需要加密的内容
     * @param encryptKey 加密密钥
     * @return
     * @throws Exception
     */
    public static String enAES(String content, String encryptKey) throws Exception {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(encryptKey.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return bytes2HexString(result).toUpperCase(Locale.ENGLISH); // 加密
        } catch (Exception e) {
            throw new Exception("AES加密失败,出现异常",e);
        }
    }

    /**
     * 解密
     * @param content  待解密内容
     * @param decryptKey 解密密钥
     * @return
     * @throws Exception 
     */
    public static String deAES(String content, String decryptKey) throws Exception {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(decryptKey.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(HexStr2Byte(content));
            return new String(result); // 加密
        } catch (Exception e) {
            throw new Exception("AES解密失败,出现异常",e);
        }
    }

    /**
     * AES加密
     * @param content  待加密内容
     * @param encryptKey 加密密钥
     * @return
     * @throws Exception
     */
    public static String enBase64AES(String content, String encryptKey) throws Exception {
        try {
            if (StringUtils.isBlank(encryptKey)) {
                encryptKey = "1234567890123456";
            }
            SecretKeySpec sKeySpec = new SecretKeySpec(encryptKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec);// 初始化
            byte[] result = cipher.doFinal(content.getBytes("utf-8"));
            return Base64.encodeToString(result); // 加密
        } catch (Exception e) {
            throw new Exception("AES加密失败,出现异常",e);
        }
    }

    /**
     * AES解密
     * @param content  待解密内容
     * @param decryptKey 解密密钥
     * @return
     * @throws Exception 
     */
    public static String deBase64AES(String content, String decryptKey) throws Exception {
        try {
            if (StringUtils.isBlank(decryptKey)) {
                decryptKey = "1234567890123456";
            }
            SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(Base64.decode(content));
            return new String(result); // 加密
        } catch (Exception e) {
            throw new Exception("AES解密失败,出现异常",e);
        }
    }
    
    /**
     * AES加密
     * @param content  待加密内容
     * @param encryptKey 加密密钥
     * @return
     * @throws Exception
     */
    public static String encodeBase64AES(String content, String encryptKey) throws Exception {
        return AES.encrypt(content, encryptKey);
    }

    /**
     * AES解密
     * @param content  待解密内容
     * @param decryptKey 解密密钥
     * @return
     * @throws Exception
     */
    public static String decodeBase64AES(String content, String decryptKey) throws Exception {
        return AES.detrypt(content, decryptKey);
    }
    
    /** 
     * Description 根据键值进行加密 
     * @param data  
     * @param key  加密键byte数组 
     * @return 
     * @throws Exception 
     */  
    public static String encryptByDES(String data, String key) throws Exception {  
        byte[] bt = encryptByDES(data.getBytes(), key.getBytes());  
        String strs = Base64.encodeToString(bt);  
        return strs;  
    }
    
    /** 
     * Description 根据键值进行加密 
     * @param data 
     * @param key  加密键byte数组 
     * @return 
     * @throws Exception 
     */  
    private static byte[] encryptByDES(byte[] data, byte[] key) throws Exception {  
        // 生成一个可信任的随机数源  
        SecureRandom sr = new SecureRandom();  

        // 从原始密钥数据创建DESKeySpec对象  
        DESKeySpec dks = new DESKeySpec(key);  

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象  
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");  
        SecretKey securekey = keyFactory.generateSecret(dks);  

        // Cipher对象实际完成加密操作  
        Cipher cipher = Cipher.getInstance("DES");  

        // 用密钥初始化Cipher对象  
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);  

        return cipher.doFinal(data);  
    }
}
