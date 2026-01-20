package com.ruoyi.common.utils;

/**
 * 敏感信息脱敏工具类
 * 
 * @author ruoyi
 */
public class SensitiveInfoUtils {

    /**
     * 手机号脱敏
     * 保留前3位和后4位，中间用*代替
     * 例如：138****1234
     * 
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 身份证号脱敏
     * 保留前6位和后4位，中间用*代替
     * 例如：110101********1234
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (StringUtils.isEmpty(idCard)) {
            return idCard;
        }
        return idCard.replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2");
    }

    /**
     * 姓名脱敏
     * 保留姓氏，其他用*代替
     * 例如：张**
     * 
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String maskName(String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*" + name.substring(name.length() - 1);
    }

    /**
     * 邮箱脱敏
     * 保留前1位和域名，中间用*代替
     * 例如：z***@example.com
     * 
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    /**
     * 银行卡号脱敏
     * 保留前4位和后4位，中间用*代替
     * 例如：6222****8888
     * 
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String maskBankCard(String bankCard) {
        if (StringUtils.isEmpty(bankCard)) {
            return bankCard;
        }
        return bankCard.replaceAll("(\\d{4})\\d+(\\d{4})", "$1****$2");
    }

    /**
     * 地址脱敏
     * 保留前6位，其他用*代替
     * 例如：北京市朝阳区********
     * 
     * @param address 地址
     * @return 脱敏后的地址
     */
    public static String maskAddress(String address) {
        if (StringUtils.isEmpty(address)) {
            return address;
        }
        if (address.length() <= 6) {
            return address;
        }
        return address.substring(0, 6) + "********";
    }

    /**
     * 自定义脱敏
     * 
     * @param str          原始字符串
     * @param startLength  保留开始长度
     * @param endLength    保留结束长度
     * @param maskLength   掩码长度
     * @return 脱敏后的字符串
     */
    public static String maskCustom(String str, int startLength, int endLength, int maskLength) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        if (startLength + endLength >= str.length()) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, startLength));
        for (int i = 0; i < maskLength; i++) {
            sb.append("*");
        }
        sb.append(str.substring(str.length() - endLength));
        return sb.toString();
    }
}