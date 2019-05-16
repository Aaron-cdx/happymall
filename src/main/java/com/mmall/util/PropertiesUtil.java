package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by caoduanxi
 */
/*
这里用来获取mmall.properties中的参数。
加载文件->获取参数
 */
public class PropertiesUtil {
    //这里是要获取参数
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
    //一个属性props
    private static Properties props;
    //属性文件的加载
    //初始化props属性文件:因为需要只加载一次，所以使用静态代码块
    static {
        String fileName = "mmall.properties";
        props = new Properties();
        //加载资源文件 如何加载为与mmall.properties处的文件
        try {
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"UTF-8"));
        } catch (IOException e) {
            //打印日志.输出异常原因
            logger.error("资源文件加载异常",e);
        }
    }
    /*
    两个trim()
    一个是获取到的键值有空格
    一个是获取到的值有空格
     */
    //获取资源文件
    public static String getProperty(String key){
        //获取资源文件的值
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            return StringUtils.EMPTY;
        }
        return value.trim();
    }

    //这个是含有默认值的返回资源文件的获取
    public static String getProperty(String key, String defaultValue){
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            value = defaultValue;
        }
        return value.trim();
    }


}
