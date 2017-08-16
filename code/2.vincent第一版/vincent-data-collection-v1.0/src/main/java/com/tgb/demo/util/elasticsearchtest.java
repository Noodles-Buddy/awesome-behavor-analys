package com.tgb.demo.util;

import java.util.Map;

/**
 * Created by L on 2017-07-29.
 */
public class elasticsearchtest {
    public static void main(String[] args) {
        Map<String, Object> shouldMap = ElasticSearchUtils.searchDoc("blog", "article", "1");
        System.out.println(shouldMap.toString());
    }
}
