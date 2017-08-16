package com.tgb.demo.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;


import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 * Created by L on 2017-07-29.
 */
public class ElasticSearchUtilsImp {
    private static String cluster_name = null;// 实例名称
    private static String cluster_serverip = null;// elasticSearch服务器ip
    private static String indexname = null;// 索引名称

    static {
        try {
            // 读取db.properties文件
            Properties props = new Properties();
            InputStream in = ElasticSearchUtilsImp.class.getResourceAsStream("/elasticsearch.properties");
            props.load(in);// 加载文件

            // 读取信息
            cluster_name = props.getProperty("cluster_name");
            cluster_serverip = props.getProperty("cluster_serverip");
            indexname = props.getProperty("indexname");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("加载数据库配置文件出错！");
        }
    }

    /**
     * 返回一个到ElasticSearch的连接客户端
     *
     * @return
     */
    private static TransportClient getClient() {
        Settings settings = Settings.builder().put("cluster.name", cluster_name).build();// 设置集群名称
        @SuppressWarnings("unchecked")
        TransportClient client = new PreBuiltTransportClient(settings);// 创建client



        //Settings settings1 = Settings.settingsBuilder().put("cluster.name",cluster_name).build();
        //Client client1 =  TransportClient.builder().addPlugin(DeleteByQueryPlugin.class).settings(settings).build().addTransportAddresses(address1,address2);
        try {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(cluster_serverip), 9300));// 增加地址和端口
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("ElasticSearch连接失败！");
        }

        return client;
    }

    /**
     * 将Map转换成builder
     *
     * @param mapParam
     * @return
     * @throws Exception
     */
    private static XContentBuilder createMapJson(Map<String, String> mapParam) throws Exception {
        XContentBuilder source = XContentFactory.jsonBuilder().startObject();

        for (Map.Entry<String, String> entry : mapParam.entrySet()) {
            source.field(entry.getKey(), entry.getValue());
        }

        source.endObject();

        return source;
    }

    /**
     * 将实体转换成json
     *
     * @param entity 实体
     //* @param fieldNameParm 实体中待转换成json的字段
     * @return 返回json
     * @throws Exception
     */
    private static XContentBuilder createEntityJson(Object entity, String... methodNameParm) throws Exception {
        // 创建json对象, 其中一个创建json的方式
        XContentBuilder source = XContentFactory.jsonBuilder().startObject();

        try {
            for (String methodName : methodNameParm) {

                if (!methodName.startsWith("get")) {
                    throw new Exception("不是有效的属性！");
                }

                Method method = entity.getClass().getMethod(methodName, null);
                String fieldValue = (String) method.invoke(entity, null);
                String fieldName = StringUtils.toLowerCaseFirstOne(methodName.replace("get", ""));// 去掉“get”，并将首字母小写

                // 避免和elasticSearch中id字段重复
                if (fieldName == "_id") {
                    fieldName = "id";
                }

                source.field(fieldName, fieldValue);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.out.println("未找到方法！");
        }

        source.endObject();

        return source;
    }

    /**
     * 将一个Map格式的数据（key,value）插入索引 （私有方法）
     *
     * @param type 类型（对应数据库表）
     * @param docId id，对应elasticSearch中的_id字段
     * @param mapParam Map格式的数据
     * @return
     */
    public static boolean addMapDocToIndex(String type, String docId, Map<String, String> mapParam) {
        boolean result = false;

        TransportClient client = getClient();
        XContentBuilder source = null;
        try {
            source = createMapJson(mapParam);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 存json入索引中
        IndexResponse response = null;
        if (docId == null) {
            // 使用默认的id
            response = client.prepareIndex(indexname, type).setSource(source).get();
        } else {
            response = client.prepareIndex(indexname, type, docId).setSource(source).get();
        }

        // 插入结果获取
        String index = response.getIndex();
        String gettype = response.getType();
        String id = response.getId();
        long version = response.getVersion();
        RestStatus status = response.status();

        String strResult = "新增文档成功：" + index + " : " + gettype + ": " + id + ": " + version + ": " + status.getStatus();
        System.out.println(strResult);

        if (status.getStatus() == 201) {
            result = true;
        }

        // 关闭client
        client.close();

        return result;
    }

    /**
     * 将一个实体存入到默认索引的类型中（指定_id，一般是业务数据的id，及elasticSearch和关系型数据使用同一个id，方便同关系型数据库互动）
     * （私有方法）
     *
     * @param type 类型（对应数据库表）
     * @param docId id，对应elasticSearch中的_id字段
     * @param entity 要插入的实体
     * @param methodNameParm 需要将实体中哪些属性作为字段
     * @return
     */
    public static boolean addEntityDoc(String type, String docId, Object entity, String... methodNameParm) {
        boolean result = false;

        TransportClient client = getClient();
        XContentBuilder source = null;
        try {
            source = createEntityJson(entity, methodNameParm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 存json入索引中
        IndexResponse response = null;
        if (docId == null) {
            // 使用默认的id
            response = client.prepareIndex(indexname, type).setSource(source).get();
        } else {
            response = client.prepareIndex(indexname, type, docId).setSource(source).get();
        }

        // 插入结果获取
        String index = response.getIndex();
        String gettype = response.getType();
        String id = response.getId();
        long version = response.getVersion();
        RestStatus status = response.status();

        String strResult = "新增文档成功：" + index + " : " + gettype + ": " + id + ": " + version + ": " + status.getStatus();
        System.out.println(strResult);

        if (status.getStatus() == 201) {
            result = true;
        }

        // 关闭client
        client.close();

        return result;
    }

    /**
     * 删除文档
     *
     * @param type 类型（对应数据库表）
     * @param docId 类型中id
     * @return
     */
    public static boolean deleteDoc(String type, String docId) {
        boolean result = false;

        TransportClient client = getClient();
        DeleteResponse deleteresponse = client.prepareDelete(indexname, type, docId).get();

        System.out.println("删除结果：" + deleteresponse.getResult().toString());
        if (deleteresponse.getResult().toString() == "DELETED") {
            result = true;
        }

        // 关闭client
        client.close();

        return result;
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
        String strResult = "";
        boolean result = false;

        TransportClient client = getClient();

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(indexname);
        updateRequest.type(type);
        updateRequest.id(docId);
        try {
            updateRequest.doc(createMapJson(updateParam));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            strResult = client.update(updateRequest).get().getResult().toString();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println(strResult);

        if (strResult == "UPDATED") {
            result = true;
        }

        return result;
    }

    /**
     * TODO or查询命中条数
     * @param type 类型
     * @param shouldMap 查询条件
     * @return
     */
    public static int multiOrSearchDocCount(String type, Map<String, String> shouldMap) {
        TransportClient client = getClient();

        return 0;
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
        TransportClient client = getClient();

        // 高亮
        HighlightBuilder hiBuilder = new HighlightBuilder();
        hiBuilder.preTags("<span style=\"color:red\">");
        hiBuilder.postTags("</span>");
        hiBuilder.field(fieldName);

        QueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery(fieldName, keyword);

        SearchRequestBuilder responsebuilder = client.prepareSearch(indexname).setTypes(type);
        responsebuilder.setQuery(queryBuilder);
        responsebuilder.highlighter(hiBuilder);
        responsebuilder.setFrom(from);
        responsebuilder.setSize(size);
        responsebuilder.setExplain(true);

        SearchResponse myresponse = responsebuilder.execute().actionGet();
        SearchHits searchHits = myresponse.getHits();

        // 总命中数
        long total = searchHits.getTotalHits();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("total", total);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < searchHits.getHits().length; i++) {
            Map<String, HighlightField> highlightFields = searchHits.getHits()[i].getHighlightFields();

            // 段高亮
            HighlightField titleField = highlightFields.get(fieldName);
            Map<String, Object> source = searchHits.getHits()[i].getSource();
            if (titleField != null) {
                Text[] fragments = titleField.fragments();
                String name = "";
                for (Text text : fragments) {
                    name += text;
                }
                source.put(fieldName, name);
            }

            list.add(source);
        }
        map.put("rows", list);

        return map;
    }

    /**
     * or条件查询高亮
     *
     * @param type 类型
     * @param shouldMap or条件和值
     * @param from 开始行数
     * @param size 每页大小
     * @return
     */
    public static Map<String, Object> multiOrSearchDocHigh(String type, Map<String, String> shouldMap, int from,
                                                           int size) {
        TransportClient client = getClient();

        SearchRequestBuilder responsebuilder = client.prepareSearch(indexname).setTypes(type);
        responsebuilder.setFrom(from);
        responsebuilder.setSize(size);
        responsebuilder.setExplain(true);

        // 高亮
        HighlightBuilder hiBuilder = new HighlightBuilder();
        hiBuilder.preTags("<span style=\"color:red\">");
        hiBuilder.postTags("</span>");

        // 高亮每个字段
        for (String key : shouldMap.keySet()) {
            hiBuilder.field(key);
        }

        responsebuilder.highlighter(hiBuilder);

        if (null != shouldMap && shouldMap.size() > 0) {
            // 创建一个查询
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

            // 这里查询的条件用map传递
            for (String key : shouldMap.keySet()) {
                queryBuilder.should(QueryBuilders.matchPhraseQuery(key, shouldMap.get(key)));// or连接条件
            }
            // 查询
            responsebuilder.setQuery(queryBuilder);
        }

        SearchResponse myresponse = responsebuilder.execute().actionGet();
        SearchHits searchHits = myresponse.getHits();

        // 总命中数
        long total = searchHits.getTotalHits();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("total", total);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < searchHits.getHits().length; i++) {
            Map<String, HighlightField> highlightFields = searchHits.getHits()[i].getHighlightFields();
            Map<String, Object> source = searchHits.getHits()[i].getSource();

            for (String key : shouldMap.keySet()) {
                // 各个段进行高亮
                HighlightField titleField = highlightFields.get(key);
                if (titleField != null) {
                    Text[] fragments = titleField.fragments();
                    String name = "";
                    for (Text text : fragments) {
                        name += text;
                    }
                    source.put(key, name);
                }
            }

            list.add(source);
        }
        map.put("rows", list);

        return map;
    }

    /**
     * 搜索
     *
     * @param type 类型
     * @param fieldName 待搜索的字段
     * @param keyword 待搜索的关键词
     * @param from 开始行数
     * @param size 每页大小
     * @return
     */
    public static Map<String, Object> searchDoc(String type, String fieldName, String keyword, int from, int size) {
        List<String> hitResult = new ArrayList<String>();

        TransportClient client = getClient();

        QueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery(fieldName, keyword);

        SearchRequestBuilder responsebuilder = client.prepareSearch(indexname).setTypes(type);
        responsebuilder.setQuery(queryBuilder);
        responsebuilder.setFrom(from);
        responsebuilder.setSize(size);
        responsebuilder.setExplain(true);

        SearchResponse myresponse = responsebuilder.execute().actionGet();
        SearchHits hits = myresponse.getHits();
        for (int i = 0; i < hits.getHits().length; i++) {
            hitResult.add(hits.getHits()[i].getSourceAsString());
        }

        // 将命中结果转换成Map输出
        Map<String, Object> modelMap = new HashMap<String, Object>(2);
        modelMap.put("total", hitResult.size());
        modelMap.put("rows", hitResult);

        return modelMap;
    }

    /**
     * 多个条件进行or查询
     *
     * @param type 类型
     * @param shouldMap 进行or查询的段和值
     * @param from 开始行数
     * @param size 每页大小
     * @return
     */
    public static Map<String, Object> multiOrSearchDoc(String type, Map<String, String> shouldMap, int from, int size) {
        List<String> hitResult = new ArrayList<String>();

        TransportClient client = getClient();

        SearchRequestBuilder responsebuilder = client.prepareSearch(indexname).setTypes(type);
        responsebuilder.setFrom(from);
        responsebuilder.setSize(size);
        responsebuilder.setExplain(true);

        if (null != shouldMap && shouldMap.size() > 0) {
            // 创建一个查询
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

            // 这里查询的条件用map传递
            for (String key : shouldMap.keySet()) {
                queryBuilder.should(QueryBuilders.matchPhraseQuery(key, shouldMap.get(key)));// or连接条件
            }
            // 查询
            responsebuilder.setQuery(queryBuilder);
        }

        SearchResponse myresponse = responsebuilder.execute().actionGet();
        SearchHits hits = myresponse.getHits();
        for (int i = 0; i < hits.getHits().length; i++) {
            hitResult.add(hits.getHits()[i].getSourceAsString());
        }

        // 将命中结果转换成Map输出
        Map<String, Object> modelMap = new HashMap<String, Object>(2);
        modelMap.put("total", hitResult.size());
        modelMap.put("rows", hitResult);

        return modelMap;
    }

    /**
     * 多个条件进行and查询
     *
     * @param type 类型
     * @param mustMap 进行and查询的段和值
     * @param from 开始行数
     * @param size 每页大小
     * @return
     */
    public static Map<String, Object> multiAndSearchDoc(String type, Map<String, String> mustMap, int from, int size) {
        List<String> hitResult = new ArrayList<String>();

        TransportClient client = getClient();

        SearchRequestBuilder responsebuilder = client.prepareSearch(indexname).setTypes(type);
        responsebuilder.setFrom(from);
        responsebuilder.setSize(size);
        responsebuilder.setExplain(true);

        if (null != mustMap && mustMap.size() > 0) {
            // 创建一个查询
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

            // 这里查询的条件用map传递
            for (String key : mustMap.keySet()) {
                queryBuilder.must(QueryBuilders.matchPhraseQuery(key, mustMap.get(key)));// and查询
            }
            // 查询
            responsebuilder.setQuery(queryBuilder);
        }

        SearchResponse myresponse = responsebuilder.execute().actionGet();
        SearchHits hits = myresponse.getHits();
        for (int i = 0; i < hits.getHits().length; i++) {
            hitResult.add(hits.getHits()[i].getSourceAsString());
        }

        // 将命中结果转换成Map输出
        Map<String, Object> modelMap = new HashMap<String, Object>(2);
        modelMap.put("total", hitResult.size());
        modelMap.put("rows", hitResult);

        return modelMap;
    }
}
