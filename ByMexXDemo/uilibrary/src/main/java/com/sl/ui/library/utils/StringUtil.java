package com.sl.ui.library.utils;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static boolean isNickName(String str) {
        String regExp = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]{1,12}$";

        Pattern p = Pattern.compile(regExp);

        Matcher m = p.matcher(str);

        return m.find();
    }

    // 国标码和区位码转换常�?
    static final int GB_SP_DIFF = 160;
    // 存放国标�?��汉字不同读音的起始区位码
    static final int[] secPosValueList = {1601, 1637, 1833, 2078, 2274, 2302,
            2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858, 4027,
            4086, 4390, 4558, 4684, 4925, 5249, 5600};
    // 存放国标�?��汉字不同读音的起始区位码对应读音
    static final char[] firstLetter = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'w', 'x',
            'y', 'z'};

    // 获取�?��字符串的拼音�?
    public static String getFirstLetter(String oriStr) {
        String str = oriStr.toLowerCase();
        StringBuffer buffer = new StringBuffer();
        char ch;
        char[] temp;
        for (int i = 0; i < str.length(); i++) { // 依次处理str中每个字�?
            ch = str.charAt(i);
            temp = new char[]{ch};
            byte[] uniCode = new String(temp).getBytes();
            if (uniCode[0] < 128 && uniCode[0] > 0) { // 非汉�?
                buffer.append(temp);
            } else {
                buffer.append(convert(uniCode));
            }
        }
        return buffer.toString();
    }

    /**
     * 获取�?��汉字的拼音首字母�?GB码两个字节分别减�?60，转换成10进制码组合就可以得到区位�?
     * 例如汉字“你”的GB码是0xC4/0xE3，分别减�?xA0�?60）就�?x24/0x43
     * 0x24转成10进制就是36�?x43�?7，那么它的区位码就是3667，在对照表中读音为�?n�?
     */
    static char convert(byte[] bytes) {
        char result = '-';
        int secPosValue = 0;
        int i;
        for (i = 0; i < bytes.length; i++) {
            bytes[i] -= GB_SP_DIFF;
        }
        secPosValue = bytes[0] * 100 + bytes[1];
        for (i = 0; i < 23; i++) {
            if (secPosValue >= secPosValueList[i]
                    && secPosValue < secPosValueList[i + 1]) {
                result = firstLetter[i];
                break;
            }
        }
        return result;
    }

    /*
     * 校验字符串的合法
     * true :有效
     * false: 无效
     */
    public static boolean checkStr(String str) {
        if (null == str)
            return false;

        if ("null".equalsIgnoreCase(str) || "nul".equalsIgnoreCase(str)) {
            return false;
        }

        return str.trim().length() > 0;
    }


    // 判断电话号码格式是否正确
    public static boolean isMobileNO(String mobiles) {
        if (!checkStr(mobiles) || mobiles.length() != 11)
            return false;
        return isNumeric(mobiles) & mobiles.startsWith("1");

    }

    /*
     * 判断字符串是否含有数字
     */
    public static boolean isContainsNum(String content) {
        if (!checkStr(content))
            return false;
        boolean isDigit = false;
        for (int i = 0; i < content.length(); i++) { // 循环遍历字符串
            if (Character.isDigit(content.charAt(i))) { // 用char包装类中的判断数字的方法判断每一个字符
                isDigit = true;
            }
            if (Character.isLetter(content.charAt(i))) { // 用char包装类中的判断字母的方法判断每一个字符
                // isLetter = true;
            }
        }
        return isDigit;
    }

    /*
     * 是否为汉字
     */
    public static boolean isChineseChar(String str) {
        if (!checkStr(str))
            return false;
        boolean temp = false;
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            temp = true;
        }
        return temp;
    }

    public static String formatMoney(String s) {// , int len
        if (!checkStr(s))
            return "";
        int len = s.length();
        NumberFormat formater = null;
        double num = Double.parseDouble(s);
        if (len == 0) {
            formater = new DecimalFormat("###,###");

        } else {
            StringBuffer buff = new StringBuffer();
            buff.append("###,###.");
            for (int i = 0; i < len; i++) {
                buff.append("#");
            }
            formater = new DecimalFormat(buff.toString());
        }
        String result = formater.format(num);
        /*
         * if (result.indexOf(".") == -1) { result = "￥" + result + ".00"; }
         * else { result = "￥" + result; }
         */
        return result;
    }

    /*
     * 检查是否为小数
     */
    public static boolean isPointNum(String value) {
        if (!checkStr(value))
            return false;
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /*
     * 检查是否为整数数
     */
    public static boolean isIntNum(String value) {
        if (!checkStr(value))
            return false;
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /*
     * 检查是否数字
     */
    public static boolean isDoubleNum(String value) {
        if (!checkStr(value))
            return false;
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static String getStringsByList(ArrayList<String> list) {
        if (null == list || list.size() <= 0)
            return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i) + ",");
        }
        return sb.toString();
    }

    public static boolean isHttpUrl(String url) {
        if (!checkStr(url))
            return false;
        return url.startsWith("http");
    }

    /*
     * "[{\"1\":12.7},{\"3\":13.8},{\"60\":14.8}]",
     */
    public static String getJSONObjectStr(JSONObject obj) {
        if (null == obj)
            return "";
        Iterator<String> iterator = obj.keys();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = obj.opt(key);
            String bb = key + "个月" + value + "%";
            sb.append(bb + "、");
        }
        if (sb.length() > 0) {
            sb = sb.deleteCharAt(sb.lastIndexOf("、"));
        }
        return sb.toString();
    }

    public static boolean checkEmail(String email) {
        if (null == email) {
            return false;
        }
        return email.contains("@") && email.contains(".");
    }


    public static String[] split(String str, String regex) {
        if (str.contains(regex)) {
            return str.split(regex);
        }
        return null;
    }

    /*
     * 通用的判断是否为数字，包含，0.000000070000,1.0.，888hhhh等正则校验
     * https://blog.csdn.net/u013066244/article/details/53197756
     */
    public static boolean isNumeric(String str) {
        if (!checkStr(str))
            return false;
        // 该正则表达式可以匹配所有的数字 包括负数
        Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toPlainString();
        } catch (Exception e) {
            System.out.println("Exception==" + e.getMessage());
            return false;//异常 说明包含非数字。
        }
        Matcher isNum = pattern.matcher(bigStr); // matcher是全匹配
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static int getPointStep(String str) {
        if (!isNumeric(str) || !str.contains(".")) {
            return 0;
        }
        str = str.substring(str.indexOf(".") + 1);
        System.out.println("str is " + str);
        return null != str ? str.length() : 0;//ss[1].length();
    }

    public static boolean isNumericAndroidLenght(String str) {
        if (str != null && str.trim().length() <= 6) {
            return true;
        }
        return false;
    }
}
