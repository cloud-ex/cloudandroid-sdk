package com.sl.ui.library.utils;


import android.util.Log;

import java.math.BigDecimal;

public class BigDecimalUtils {

    //默认除法运算精度
    private static final int DEF_DIV_SCALE = 10;

    /**
     * 提供精确的加法运算。
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static BigDecimal add(String v1, String v2) {
        if (!StringUtil.isNumeric(v1))
            v1 = "0";

        if (!StringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        return new BigDecimal(v1).add(new BigDecimal(v2));
    }

    /**
     * 提供精确的减法运算。
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static BigDecimal sub(String v1, String v2) {

        if (!StringUtil.isNumeric(v1))
            v1 = "0";

        if (!StringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        return new BigDecimal(v1).subtract(new BigDecimal(v2));
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static BigDecimal mul(String v1, String v2) {
        if (!StringUtil.isNumeric(v1))
            v1 = "0";

        if (!StringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        return new BigDecimal(v1).multiply(new BigDecimal(v2));

    }


    /**
     * 提供精确的乘法运算。(TODO 舍入)
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static BigDecimal mul(String v1, String v2, int scale) {

        if (!StringUtil.isNumeric(v1))
            v1 = "0";

        if (!StringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        if (scale < 0)
            scale = 0;
        return new BigDecimal(v1).multiply(new BigDecimal(v2)).setScale(scale, BigDecimal.ROUND_DOWN);

    }


    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * 小数点以后10位，以后的数字四舍五入。
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static BigDecimal div(String v1, String v2) {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字舍入。
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static BigDecimal div(String v1, String v2, int scale) {
        if (!StringUtil.isNumeric(v1))
            v1 = "0";

        if (!StringUtil.isNumeric(v2)) {
            v2 = "0";
        }

        if (0 == compareTo(v2, "0"))
            return new BigDecimal(v1);

        if (scale < 0) {
            scale = 0;
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_DOWN);
    }

    /**
     * 此方法不四舍五入
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度。
     *
     * @param v1    参数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static BigDecimal divForDown(String v1, int scale) {
        if (!StringUtil.checkStr(v1)) {
            v1 = "0";
        }
        if (!StringUtil.isNumeric(v1)) {
            v1 = "0";
        }
        if (scale < 0)
            scale = 0;
        return new BigDecimal(v1).setScale(scale, BigDecimal.ROUND_DOWN);
    }


    /**
     * 此方法四舍五入
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度。
     *
     * @param v1    参数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static BigDecimal divForUp(String v1, int scale) {
        if (!StringUtil.isNumeric(v1)) {
            v1 = "0";
        }
        if (scale < 0)
            scale = 0;

        return new BigDecimal(v1).setScale(scale, BigDecimal.ROUND_UP);
    }


    /**
     * 截取数字
     * 四舍五入
     *
     * @param v1
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return
     */
    public static BigDecimal intercept(String v1, int scale) {

        if (!StringUtil.isNumeric(v1)) {
            v1 = "0";
        }
        if (scale < 0)
            scale = 0;

        return new BigDecimal(v1).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }


    /**
     * 精确对比两个数字
     *
     * @param v1 需要被对比的第一个数
     * @param v2 需要被对比的第二个数
     * @return 如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
     */
    public static int compareTo(String v1, String v2) {

        if (!StringUtil.isNumeric(v1))
            v1 = "0";

        if (!StringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        return new BigDecimal(v1).compareTo(new BigDecimal(v2));

    }


    /**
     * 禁用科学计数法
     *
     * @return 返回double类型
     */
    public static double showDNormal(Double data) {
        return Double.valueOf(showSNormal(data));
    }

    /**
     * 禁用科学计数法
     *
     * @return 返回double类型
     */
    public static String showSNormal(Double data) {
        try {
            BigDecimal bigDecimal = new BigDecimal(String.valueOf(data));
            String plainString = bigDecimal.toPlainString();
            return subZeroAndDot(plainString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "0.0";
        }

    }


    /**
     * 禁用科学计数法
     * <p>
     * 补充：toPlainString()
     * No scientific notation is used. This methods adds zeros where necessary.
     * return: a string representation of {@code this} without exponent part
     * <p>
     * IAW, 返回的字符串不会带指数形式
     *
     * @return 返回double类型
     */
    public static String showSNormal(String data) {
        if (!StringUtil.checkStr(data)) {
            return "";
        }

        if (data.contains("\"")) {
            data = stringReplace(data);
        }
        if (!StringUtil.isNumeric(data)) {
            data = "0";
        }
        String plainString = new BigDecimal(data).toPlainString();
        return subZeroAndDot(plainString);
    }


    public static String showNormal(String data) {
        if (!StringUtil.isNumeric(data)) {
            return "0";
        }
        return new BigDecimal(data).toPlainString();
    }

    /**
     * 去掉双引号
     *
     * @param wifiInfo
     * @return
     */
    public static String stringReplace(String wifiInfo) {
        String str = wifiInfo.replace("\"", "");
        return str;
    }

    /**
     * 使用java正则表达式去掉多余的.与0
     *
     * @param s
     * @return
     */
    public static String subZeroAndDot(String s) {
        if (!StringUtil.isNumeric(s))
            return "0";

        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }


    public static String showDepthVolume(String value) {
        if (!StringUtil.isNumeric(value))
            value = "0";

        String temp = new BigDecimal(value).toPlainString();
        if (compareTo(temp, "0.0001") <= 0) {
            return "0.000";
        } else if (compareTo(temp, "1000") >= 0) {
            return formatNumber(temp);
        } else {
            if (temp.contains(".")) {
                return (temp + "00000").substring(0, 5);
            } else {
                String substring = (temp + ".0000").substring(0, 4);
                if (substring.endsWith(".")) {
                    return substring.substring(0, 3);
                } else {
                    return substring;
                }
            }
        }
    }


    public static String formatNumber(String str) {
        Log.d("==111=", "" + str);
        if (!StringUtil.isNumeric(str))
            return "--";
        String number = "";
        BigDecimal b0 = new BigDecimal("1000");
        BigDecimal b1 = new BigDecimal("1000000");
        BigDecimal b2 = new BigDecimal("1000000000");
        BigDecimal temp = new BigDecimal(str);
        if (temp.compareTo(b0) == -1) {
            number = str;
            return showSNormal(number);
        } else if ((temp.compareTo(b0) == 0 || temp.compareTo(b0) == 1) && temp.compareTo(b1) == -1) {
            String substring = temp.divide(b0, 2, BigDecimal.ROUND_DOWN).toString().substring(0, 4);
            if (substring.endsWith(".")) {
                number = substring.substring(0, 3);
            } else {
                number = substring;
            }
            return number + "K";
        } else if (temp.compareTo(b1) >= 0 && temp.compareTo(b2) < 0) {
            Log.d("==111=", "M" + str);
            String substring = temp.divide(b1, 2, BigDecimal.ROUND_DOWN).toString().substring(0, 4);
            if (substring.endsWith(".")) {
                number = substring.substring(0, 3);
            } else {
                number = substring;
            }
            return number + "M";
        } else if (temp.compareTo(b2) >= 0) {
            Log.d("==111=", "B" + str);
            String substring = temp.divide(b2, 2, BigDecimal.ROUND_DOWN).toString().substring(0, 4);
            if (substring.endsWith(".")) {
                number = substring.substring(0, 3);
            } else {
                number = substring;
            }
            return number + "B";
        } else {
            return showSNormal(number);
        }
    }

    public static int compareToDraw(String v1, String v2) {
        if (!StringUtil.isNumeric(v1))
            v1 = "0";

        if (v1.equals("0")) {
            return -1;
        }
        return compareTo(v1, v2);
    }


}
