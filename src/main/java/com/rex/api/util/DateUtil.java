package com.rex.api.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Author lzw
 * Create 2021/7/26
 * Description 日期工具类
 */
public class DateUtil {

    public static boolean dateCompare(Date oldDate, Date newDate) {
        return dateCompare(oldDate,newDate,0);
    }
    /*
     * Author lzw
     * Description  判断旧的日期加上天数是否小于新的日期
     * Date 2021/7/26
     * Param [oldDate, newDate, day]
     * return boolean
     **/
    public static boolean dateCompare(Date oldDate, Date newDate, int day) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = format.format(oldDate);
        //日期转string
        Date sDate = null;
        try {
            sDate = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        assert sDate != null;
        cal.setTime(sDate);
        //增加7天
        cal.add(Calendar.DAY_OF_MONTH, day);
        //Calendar转为Date类型
        Date date = cal.getTime();

        return date.compareTo(newDate) < 0;
    }

    /*
     * Author lzw
     * Description 将日期转成数字格式: yyyyMMdd
     * Date 2021/7/26
     * Param [date]
     * return java.lang.Integer
     **/
    public static Integer dateValueOfInteger(Date date){
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        return Integer.valueOf(sf.format(new Date()));
    }
    /*
     * Author lzw
     * Description 传入Date和格式返回String 类型的日期
     * Date 2021/8/17
     * Param [date]
     * return java.lang.String
     **/
    public static String dateCompareString(Date date,String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
}
