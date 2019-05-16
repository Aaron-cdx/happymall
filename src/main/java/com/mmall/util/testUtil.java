package com.mmall.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * @author caoduanxi
 * @2019/5/7 17:00
 */
public class testUtil {
    //使用joda-time来对时间做格式转换

    //str -> Date
    public static Date strToDate(String dateTimeStr, String dateFormatStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(dateFormatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    //Date -> str
    public static String dateToStr(Date date, String dateFormatStr){
        //首先判断时间是否为空
        if(date == null){
            return "";
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(dateFormatStr);
    }

    public static void main(String[] args) {
        System.out.println(testUtil.strToDate("2019-05-07 17:06:29","yyyy-MM-dd HH:mm:ss"));
        System.out.println(testUtil.dateToStr(new Date(),"yyyy-MM-dd HH:mm:ss"));
    }

}
