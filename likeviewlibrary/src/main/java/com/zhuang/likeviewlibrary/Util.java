package com.zhuang.likeviewlibrary;

import android.content.Context;

/**
 * Created by zhuang on 2017/10/16.
 */

public class Util {

    /**
     * 增加点赞时，切割点赞数
     * 把当前点赞数切割为两个部分，同时产生一个数值用于替换掉旧的数值
     *
     * @param num
     * @return num1:前部分  num2:后部分  num3:新的数值
     */
    public static String[] cutNumAdd(int num) {
        int source = num;
        double a = 1;
        while (num % 10 == 9) {
            num = (num - 9) / 10;
            a++;
        }
        double multiple = Math.pow(10d, a);
        double c = source % multiple;
        double d = c + 1;
        String num1 = String.valueOf((int) ((source - c) / multiple));
        String num2 = String.valueOf((int) c);
        String num3 = String.valueOf((int) d);
        if (d % 10 == 0 && num3.length() != num2.length()) {
            num2 = "0" + num2;
        }
        String[] arr = {num1, num2, num3};
        return arr;
    }

    /**
     * 取消点赞时，切割点赞数
     *
     * @param num
     * @return
     */
    public static String[] cutNumDel(int num) {
        int source = num;
        double a = 1;
        while (num % 10 == 0) {
            num = num / 10;
            a++;
        }
        double multiple = Math.pow(10d, a);
        double c = source % multiple;
        double d = c - 1;
        String num1 = String.valueOf((int) ((source - c) / multiple));
        String num2 = String.valueOf((int) c);
        String num3 = String.valueOf((int) d);
        if (c % 10 == 0 && num3.length() != num2.length() && !num1.equals("0")) {
            num3 = "0" + num3;
        }
        String[] arr = {num1, num2, num3};
        return arr;
    }

    /**
     * sp px转换
     */
    public static int sp2px(Context context, int spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * dp px转换
     */
    public static int dip2px(Context context, int dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
