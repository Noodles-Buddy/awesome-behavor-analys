package com.tgb.demo.util;

import java.util.Map;
import net.sf.json.JSONObject;

/**
 * @author：Tim
 * @date：2017年5月6日 上午11:56:37
 * @description：字符串助手类
 */
public class StringUtils {

    /**
     * 将Map转换成json字符串
     *
     * @param map Map<String, Object>格式数据
     * @return json数据
     */
    public static String map2String(Map<String, Object> map) {
        return JSONObject.fromObject(map).toString();
    }

    /**
     * 首字母转小写
     *
     * @param s 待转换的字符串
     * @return
     */
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }
}

