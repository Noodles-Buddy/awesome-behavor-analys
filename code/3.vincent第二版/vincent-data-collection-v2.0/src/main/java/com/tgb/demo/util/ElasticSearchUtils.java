package com.tgb.demo.util;

import java.util.Map;

/**
 * @author：Tim
 * @date：2017年5月3日 下午8:24:22
 * @description：ElasticSearch助手类
 */
public class ElasticSearchUtils {

    /**
     * 将一个Map格式的数据（key,value）插入索引（指定_id，一般是业务数据的id，及elasticSearch和关系型数据使用同一个id，方便同关系型数据库互动）
     *
     * @param type 类型（对应数据库表）
     * @param docId id，对应elasticSearch中的_id字段
     * @param mapParam Map格式的数据
     * @return
     */
    public static boolean addDoc(String type, String docId, Map<String, String> mapParam) {
        return ElasticSearchUtilsImp.addMapDocToIndex(type, docId, mapParam);
    }

    /**
     * 将一个Map格式的数据（key,value）插入索引 （使用默认_id）
     *
     * @param type 类型（对应数据库表）
     * @param mapParam Map格式的数据
     * @return
     */
    public static boolean addDoc(String type, Map<String, String> mapParam) {
        return ElasticSearchUtilsImp.addMapDocToIndex(type, null, mapParam);
    }

    /**
     * 将一个实体存入到默认索引的类型中（默认_id）
     *
     * @param type 类型（对应数据库表）
     * @param entity 要插入的实体
     * @param methodNameParm 需要将实体中哪些属性作为字段
     * @return
     */
    public static boolean addDoc(String type, Object entity, String... methodNameParm) {
        return ElasticSearchUtilsImp.addEntityDoc(type, null, entity, methodNameParm);
    }

    /**
     * 将一个实体存入到默认索引的类型中（指定_id，一般是业务数据的id，及elasticSearch和关系型数据使用同一个id，方便同关系型数据库互动）
     *
     * @param type 类型（对应数据库表）
     * @param docId id，对应elasticSearch中的_id字段
     * @param entity 要插入的实体
     * @param methodNameParm 需要将实体中哪些属性作为字段
     * @return
     */
    public static boolean addDoc(String type, String docId, Object entity, String... methodNameParm) {
        return ElasticSearchUtilsImp.addEntityDoc(type, docId, entity, methodNameParm);
    }

    /**
     * 删除文档
     *
     * @param type 类型（对应数据库表）
     * @param docId 类型中id
     * @return
     */
    public static boolean deleteDoc(String type, String docId) {
        return ElasticSearchUtilsImp.deleteDoc(type, docId);
    }

    /**
     * 修改文档
     *
     * @param type 类型
     * @param docId 文档id
     * @param updateParam 需要修改的字段和值
     * @return
     */
    public static boolean updateDoc(String type, String docId, Map<String, String> updateParam) {
        return ElasticSearchUtilsImp.updateDoc(type, docId, updateParam);
    }

    // --------------------以下是各种搜索方法--------------------------

    /**
     * 高亮搜索
     *
     * @param type 类型
     * @param fieldName 段
     * @param keyword 段值
     * @return
     */
    public static Map<String, Object> searchDocHighlight(String type, String fieldName, String keyword) {
        return ElasticSearchUtilsImp.searchDocHighlight(type, fieldName, keyword, 0, 10);
    }

    /**
     * 高亮搜索
     *
     * @param type 类型
     * @param fieldName 段
     * @param keyword 关键词
     * @param from 开始行数
     * @param size 每页大小
     * @return
     */
    public static Map<String, Object> searchDocHighlight(String type, String fieldName, String keyword, int from,
                                                         int size) {
        return ElasticSearchUtilsImp.searchDocHighlight(type, fieldName, keyword, from, size);
    }

    /**
     * or条件查询高亮
     *
     * @param type 类型
     * @param shouldMap or条件和值
     * @return
     */
    public static Map<String, Object> multiOrSearchDocHigh(String type, Map<String, String> shouldMap, int from,
                                                           int size) {
        return ElasticSearchUtilsImp.multiOrSearchDocHigh(type, shouldMap, from, size);
    }

    /**
     * 搜索
     *
     * @param type 类型
     * @param fieldName 待搜索的字段
     * @param keyword 待搜索的关键词
     */
    public static Map<String, Object> searchDoc(String type, String fieldName, String keyword) {
        return ElasticSearchUtilsImp.searchDoc(type, fieldName, keyword, 0, 10);
    }

    /**
     * 多个条件进行or查询
     *
     * @param type 类型
     * @param shouldMap 进行or查询的段和值
     * @return
     */
    public static Map<String, Object> multiOrSearchDoc(String type, Map<String, String> shouldMap) {
        return ElasticSearchUtilsImp.multiOrSearchDoc(type, shouldMap, 0, 10);
    }

    /**
     * 多个条件进行and查询
     *
     * @param type 类型
     * @param mustMap 进行and查询的段和值
     * @return
     */
    public static Map<String, Object> multiAndSearchDoc(String type, Map<String, String> mustMap) {
        return ElasticSearchUtilsImp.multiAndSearchDoc(type, mustMap, 0, 10);
    }
}
