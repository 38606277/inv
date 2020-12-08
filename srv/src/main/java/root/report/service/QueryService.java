package root.report.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultCDATA;
import org.dom4j.tree.DefaultComment;
import org.dom4j.tree.DefaultElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;
import root.configure.AppConstants;
import root.configure.MybatisCacheConfiguration;
import root.report.db.DbFactory;
import root.report.query.SqlTemplate;
import root.report.util.ExecuteSqlUtil;
import root.report.util.JsonUtil;
import root.report.util.XmlUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * 功能描述: 对query表的增删改查功能
 */
@Service
public class QueryService {

    public static final String headModel = "-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd";

    private static Logger log = Logger.getLogger(QueryService.class);

    @Autowired
    private RestTemplate restTemplate;
    /**
     * 功能描述: 往query_name表中增加记录
     */
    public String createQueryName(SqlSession sqlSession, JSONObject jsonFunc) throws Exception {
        Map<String, Object> mapFunc = new HashMap<>();   // 必须设定为Object,因为我们想要让其返回自增长类型值
        mapFunc.put("class_id", jsonFunc.getString("class_id"));
        // mapFunc.put("func_id", jsonFunc.getString("func_id"));  // func_id 自增长
        mapFunc.put("qry_name", jsonFunc.getString("qry_name"));
        mapFunc.put("qry_desc", jsonFunc.getString("qry_desc"));
        mapFunc.put("qry_type", jsonFunc.getString("qry_type"));
        String qryfile= jsonFunc.getString("qry_file");
        if(qryfile!=null)
        {
            qryfile=qryfile.replaceAll("/","\\\\");
            qryfile=qryfile.replaceAll("\\\\", "\\\\\\\\");
        }
        mapFunc.put("qry_file", qryfile);
        mapFunc.put("qry_db", jsonFunc.getString("qry_db"));
        mapFunc.put("cached", jsonFunc.getIntValue("cached"));
        String escapeSQL=jsonFunc.getString("qry_sql");
        escapeSQL=escapeSQL.replace("'","\\'").replace("{","\\{").replace("}","\\}");
        mapFunc.put("qry_sql", escapeSQL);
        mapFunc.put("qry_cursor_name", jsonFunc.getString("qry_cursor_name"));
        mapFunc.put("qry_http_url", jsonFunc.getString("qry_http_url"));
        mapFunc.put("qry_http_header", jsonFunc.getString("qry_http_header"));
        mapFunc.put("qry_http_req_body", jsonFunc.getString("qry_http_req_body"));
        mapFunc.put("qry_http_res_body_arrayname", jsonFunc.getString("qry_http_res_body_arrayname"));
        sqlSession.insert("query.createQueryName", mapFunc);
        return mapFunc.get("id").toString();
    }

    /**
     * 功能描述:   往query_in表插入数据
     */
    public void createQueryIn(SqlSession sqlSession, JSONArray jsonArrayIn, String qry_id) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < jsonArrayIn.size(); i++) {
            JSONObject jsonIn = jsonArrayIn.getJSONObject(i);
            map.put("qry_id", qry_id);
            map.put("in_id", jsonIn.getString("in_id"));
            map.put("in_name", jsonIn.getString("in_name"));
            map.put("datatype", jsonIn.getString("datatype"));
            map.put("dict_id", jsonIn.getString("dict_id"));
            map.put("validate", jsonIn.getString("validate"));
            map.put("default_value", jsonIn.getString("default_value"));
            map.put("authtype_id", jsonIn.getString("authtype_id"));
            map.put("render", jsonIn.getString("render"));
            sqlSession.insert("query.createQueryIn", map);
        }
    }

    /**
     * 功能描述: 往 query_out表插入数据
     */
    public void createQueryOut(SqlSession sqlSession, JSONArray jsonArray, String qry_id) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            map.put("qry_id", qry_id);
            map.put("out_id", jsonObject.getString("out_id"));
            map.put("out_name", jsonObject.getString("out_name"));
            map.put("datatype", jsonObject.getString("datatype"));
            // map.put("link", jsonObject.getString("link"));
            map.put("width", jsonObject.getString("width"));    // double类型的数据，在xml文件要指定jdbc类型
            map.put("render", jsonObject.getString("render"));
            map.put("link", null);//创建时不再插入link

            sqlSession.insert("query.createQueryOut", map);

        }

    }

    /**
     * 功能描述: 往 query_link表插入数据
     */
    public void createQueryOutLink(SqlSession sqlSession, JSONObject jsonObject) {
        //保存到qry_out_link表
        //更新qry_out表的link字段
        int qry_id = jsonObject.getIntValue("qry_id");
        int link_qry_id = jsonObject.getIntValue("link_qry_id");
        String out_id = jsonObject.getString("out_id");
        JSONArray jsonArray = jsonObject.getJSONArray("param");
        if (StringUtils.isNotBlank(qry_id+"") && StringUtils.isNotBlank(link_qry_id+"")
                && jsonArray!=null) {
            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject tempJSONObject = jsonArray.getJSONObject(j);
                Map<String, Object> insertMap = new HashMap<>();
                insertMap.put("qry_id",qry_id);
                insertMap.put("out_id",out_id);
                insertMap.put("link_qry_id",link_qry_id);
                insertMap.put("link_in_id", tempJSONObject.getString("link_in_id"));
                insertMap.put("link_in_id_value_type", tempJSONObject.getString("link_in_id_value_type"));
                insertMap.put("link_in_id_value", tempJSONObject.getString("link_in_id_value"));
                sqlSession.insert("query.createQueryOutLink", insertMap);
            }
        }
        // 更新qry_out表的link字段
        Map<String,Object> tempMap = new HashMap<>();
        tempMap.put("qry_id",qry_id);
        tempMap.put("out_id",out_id);
        Map<String,Object> qryOutMap = sqlSession.selectOne("query.getOutByMap",tempMap);
        String link = String.valueOf(qryOutMap.get("link"));
        if(StringUtils.isNotBlank(link) && "null"!=link){
            link += ",";
            link += link_qry_id;
        }else {
            link = link_qry_id+"";
        }
        qryOutMap.put("link",link);
        sqlSession.update("query.updateQueryOutForLink",qryOutMap);
    }

    /**
     * 功能描述: 新增query包下的对应的mapper映射文件中的sql语句
     */
    public void createSqlTemplate(String TemplateName, String SelectID, String aSQLTemplate) throws DocumentException, SAXException, IOException {

        String namespace = TemplateName;
        String sqlId = SelectID;
        String userSqlPath = AppConstants.getUserSqlPath() + File.separator + AppConstants.QueryPrefix + namespace + ".xml";

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setSuppressDeclaration(true);
        format.setIndentSize(2);
        format.setNewlines(true);
        format.setTrimText(false);

        XMLWriter writer = null;
        Document userDoc = null;
        try {
            userDoc = XmlUtil.parseXmlToDom(userSqlPath);

            Element root = (Element) userDoc.selectSingleNode("/mapper");
            Element newSql = root.addElement("select");
            newSql.addAttribute("id", sqlId);
            newSql.addAttribute("resultType", "Map");
            newSql.addAttribute("parameterType", "Map");
            //  设置2级缓存
            newSql.addAttribute("useCache", MybatisCacheConfiguration.USE_CACHE_FALSE);
            // newSql.addText(aSQLTemplate);
            addSqlText(newSql, aSQLTemplate);

            log.debug("新增SQL:" + newSql.asXML());
            writer = new XMLWriter(new FileOutputStream(userSqlPath), format);
            //删除空白行
            Element rootEle = userDoc.getRootElement();
            this.removeBlankNewLine(rootEle);
            writer.write(userDoc);
            writer.flush();
            writer.close();
        } catch (java.lang.Exception e) {
            throw e;
        }
    }

    // 往指定节点当中增加内容
    private void addSqlText(Element select, String sqlText) throws DocumentException {
        String xmlText = "<sql>" + sqlText + "</sql>";
        Document doc = DocumentHelper.parseText(xmlText);
        //获取根节点    
        Element root = doc.getRootElement();
        List<Node> content = root.content();
        for (int i = 0; i < content.size(); i++) {
            Node node = content.get(i);
            select.add((Node) node.clone());
        }
    }

    /**
     * 功能描述:  修改 query_name 表当中的记录
     */
    public int updateQueryName(SqlSession sqlSession, JSONObject jsonFunc) {
        Map<String, Object> mapFunc = new HashMap<>();
        mapFunc.put("class_id", jsonFunc.getString("class_id"));
        mapFunc.put("qry_id", jsonFunc.getString("qry_id"));
        mapFunc.put("qry_name", jsonFunc.getString("qry_name"));
        mapFunc.put("qry_desc", jsonFunc.getString("qry_desc"));
        mapFunc.put("qry_type", jsonFunc.getString("qry_type"));
       String qryfile= jsonFunc.getString("qry_file");
        qryfile=qryfile.replaceAll("/","\\\\");
        qryfile=qryfile.replaceAll("\\\\", "\\\\\\\\");
        mapFunc.put("qry_file", qryfile);
        mapFunc.put("qry_db", jsonFunc.getString("qry_db"));
        mapFunc.put("cached", jsonFunc.getIntValue("cached"));
        String escapeSQL=jsonFunc.getString("qry_sql");
        escapeSQL=escapeSQL.replace("'","\\'").replace("{","\\{").replace("}","\\}");
        mapFunc.put("qry_sql", escapeSQL);
        mapFunc.put("qry_cursor_name", jsonFunc.getString("qry_cursor_name"));
        mapFunc.put("qry_http_url", jsonFunc.getString("qry_http_url"));
        mapFunc.put("qry_http_header", jsonFunc.getString("qry_http_header"));
        mapFunc.put("qry_http_req_body", jsonFunc.getString("qry_http_req_body"));
        mapFunc.put("qry_http_res_body", jsonFunc.getString("qry_http_res_body"));
        mapFunc.put("qry_http_res_body_arrayname", jsonFunc.getString("qry_http_res_body_arrayname"));
        return sqlSession.update("query.updateQueryName", mapFunc);
    }

    /**
     * 功能描述: 删除 query_in表当中的一条记录 （map中包含主键信息)
     */
    public int deleteQueryIn(SqlSession sqlSession, Map map) {
        return sqlSession.delete("query.deleteQueryIn", map);
    }

    /**
     * 功能描述: 删除func_in表的记录
     */
    public void updateQueryIn(SqlSession sqlSession, JSONArray jsonArrayIn) {
        Map<String, String> map = new HashMap<>();

        for (int i = 0; i < jsonArrayIn.size(); i++) {
            JSONObject jsonIn = jsonArrayIn.getJSONObject(i);
            map.put("qry_id", jsonIn.getString("qry_id"));
            map.put("in_id", jsonIn.getString("in_id"));
            map.put("in_name", jsonIn.getString("in_name"));
            map.put("datatype", jsonIn.getString("datatype"));
            map.put("dict_id", jsonIn.getString("dict_id"));
            map.put("validate", jsonIn.getString("validate"));
            map.put("default_value", jsonIn.getString("default_value"));
            map.put("authtype_id", jsonIn.getString("authtype_id"));
            sqlSession.insert("query.createQueryIn", map);
        }
    }


    /**
     * 功能描述: 删除qry_out表的记录
     */
    public void deleteQueryOut(SqlSession sqlSession, Map map) {
        sqlSession.delete("query.deleteQueryOut", map);
    }

    /**
     * 功能描述: 修改qry_out表的记录
     */
    public void updateQueryOut(SqlSession sqlSession, JSONArray jsonArrayIn) {
        Map<String, String> map = new HashMap<>();
        Map<String, Object> deleteMap = new HashMap<>();
        for (int i = 0; i < jsonArrayIn.size(); i++) {
            JSONObject jsonOut = jsonArrayIn.getJSONObject(i);
            deleteMap.put("qry_id", jsonOut.getString("qry_id"));
            deleteMap.put("out_id", jsonOut.getString("out_id"));
            deleteQueryOut(sqlSession, deleteMap);   // 先删除后插入

            map.put("qry_id", jsonOut.getString("qry_id"));
            map.put("out_id", jsonOut.getString("out_id"));
            map.put("out_name", jsonOut.getString("out_name"));
            map.put("datatype", jsonOut.getString("datatype"));
            map.put("link", jsonOut.getString("link"));   // link 长度只有255，不能存放所有数据，存放  link_qry_id 即可
            map.put("width", jsonOut.getString("width"));    // double类型的数据，在xml文件要指定jdbc类型
            map.put("render", jsonOut.getString("render"));


            if (!jsonOut.getString("link").equals("{}")) {
                JSONObject outJsonObject = jsonOut.getJSONObject("link");
                map.put("link", outJsonObject.getString("link_qry_id"));
                deleteMap.put("link_qry_id", outJsonObject.getIntValue("link_qry_id"));
                // 删除掉 func_out_link 表当中对应的记录
                JSONArray linkIdJSONArray = outJsonObject.getJSONArray("param");
                if (linkIdJSONArray != null && !linkIdJSONArray.isEmpty()) {
                    for (int j = 0; j < linkIdJSONArray.size(); j++) {
                        if (deleteMap != null && !deleteMap.isEmpty()) {
                            JSONObject tempJSONObject = linkIdJSONArray.getJSONObject(j);
                            Map<String, Object> deleteOutLinkMap = new HashMap<>();
                            deleteOutLinkMap.putAll(deleteMap);
                            deleteOutLinkMap.put("link_in_id", tempJSONObject.getString("link_in_id"));
                            sqlSession.delete("query.deleteQueryOutLinkByPrimary", deleteOutLinkMap);

                            // 增加 func_out_link 表当中对应的记录
                            deleteOutLinkMap.put("link_in_id_value_type", tempJSONObject.getString("link_in_id_value_type"));
                            deleteOutLinkMap.put("link_in_id_value", tempJSONObject.getString("link_in_id_value"));
                            sqlSession.insert("query.createQueryOutLink", deleteOutLinkMap);
                        }
                    }
                }

            }


            sqlSession.insert("query.createQueryOut", map);
        }
    }

    /**
     * 功能描述:  得到指定文件指定id的 sql内容  ,若bool为true则代表保留原格式，为false则代表只要sql不要转义
     */
    public String getSqlTemplate(String TemplateName, String SelectID, Boolean bool) throws DocumentException, SAXException {

        String namespace = TemplateName;
        String sqlId = SelectID;
        String userSqlPath = AppConstants.getUserSqlPath() + File.separator + AppConstants.QueryPrefix + namespace + ".xml";

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setSuppressDeclaration(true);
        format.setIndentSize(2);
        format.setNewlines(true);
        format.setTrimText(false);

        XMLWriter writer = null;
        Document userDoc = null;
        try {
            userDoc = XmlUtil.parseXmlToDom(userSqlPath);
            Element select = (Element) userDoc.selectSingleNode("//select[@id='" + sqlId + "']");
            StringBuffer tempStr = new StringBuffer();
            if (bool) {
                // tempStr = select.getStringValue();   // 按照原格式取出
                List<Object> list = select.content();
                Object object = null;
                DefaultComment selContent = null;
                DefaultCDATA selCdata = null;
                for (int i = 0; i < list.size(); i++) {
                    object = list.get(i);
                    if (object instanceof DefaultElement){
                        // 解析element当中的 内容
                        // object.
                        String text = ((Node)object).asXML();
                        // 转义回去
                        text = text.replaceAll("&lt;","<");
                        text = text.replaceAll("&gt;",">");
                        text = text.replaceAll("&apos;","'");
                        text = text.replaceAll("&quot;","\"");
                        tempStr.append(text);
                    }else{
                        tempStr.append(((Node)object).asXML());
                    }
                }
            } else {
                tempStr.append(select.getTextTrim());   // 编译了一些html代码，导致不是原格式了，输入无格式的sql
            }
            log.debug("获取到的SQL为:" + tempStr);
            return tempStr.toString();
        } catch (java.lang.Exception e) {
            throw e;
        }
    }

    /**
     * 功能描述: 修改query包下的对应的mapper映射文件中的sql语句
     */
    public String updateSqlTemplate(String TemplateName, String SelectID, String aSQLTemplate) throws DocumentException, SAXException, IOException {

        String namespace = TemplateName;
        String sqlId = SelectID;
        String userSqlPath = AppConstants.getUserSqlPath() + File.separator + AppConstants.QueryPrefix + namespace + ".xml";

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setSuppressDeclaration(true);
        format.setIndentSize(2);
        format.setNewlines(true);
        format.setTrimText(false);

        XMLWriter writer = null;
        Document userDoc = null;
        try {
            userDoc = XmlUtil.parseXmlToDom(userSqlPath);
            Element select = (Element) userDoc.selectSingleNode("//select[@id='" + sqlId + "']");
            select.clearContent();
            this.addSqlText(select, aSQLTemplate);

            log.debug("修改SQL:" + select.asXML());
            writer = new XMLWriter(new FileOutputStream(userSqlPath), format);
            //删除空白行
            Element rootEle = userDoc.getRootElement();
            this.removeBlankNewLine(rootEle);
            writer.write(userDoc);
            writer.flush();
            writer.close();
            return "";
        } catch (java.lang.Exception e) {
            throw e;
        }
    }

    /**
     * 功能描述: 删除qry_name当中的记录
     */
    public void deleteQueryName(SqlSession sqlSession, int qry_id) {
        sqlSession.delete("query.deleteQueryName", qry_id);
    }

    /**
     * 功能描述: 针对传递进来的JSONAarray进行批量删除qry_in数据
     */
    public void deleteQueryInForJsonArray(SqlSession sqlSession, int qry_id) {
        sqlSession.delete("query.deleteQueryInByFuncId", qry_id);
    }

    /**
     * 功能描述: 针对传递进来的JSONAarray进行批量删除func_out数据
     */
    public void deleteQueryOutForJsonArray(SqlSession sqlSession, int qry_id) {
        this.deleteQueryOutLinkByQryId(sqlSession, qry_id);
        sqlSession.delete("query.deleteQueryOutByFuncId", qry_id);
    }

    // 根据qry_id 删除掉 qry_out_link 表当在的记录
    public void deleteQueryOutLinkByQryId(SqlSession sqlSession, int qry_id) {
        sqlSession.delete("query.deleteQueryOutLinkByQryId", qry_id);
    }

    // 根据qry_id 删除掉 qry_out_link 表当在的记录
    public void deleteQueryOutLinkByPrimary(SqlSession sqlSession, JSONObject jsonObject) {
        int qry_id = jsonObject.getIntValue("qry_id");
        int link_qry_id = jsonObject.getIntValue("link_qry_id");
        String out_id = jsonObject.getString("out_id");
        JSONArray jsonArray = jsonObject.getJSONArray("param");
        if (StringUtils.isNotBlank(qry_id+"") && StringUtils.isNotBlank(link_qry_id+"")
                && jsonArray!=null) {
            String linkFinal = "";
            Map<String,Object>  qryOutFinalMap = new HashMap<>();
            qryOutFinalMap.put("qry_id",qry_id);
            qryOutFinalMap.put("out_id",out_id);
            Map<String,Object> qryOutMap = sqlSession.selectOne("query.getOutByMap",qryOutFinalMap);
            linkFinal = String.valueOf(qryOutMap.get("link"));
            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject tempJSONObject = jsonArray.getJSONObject(j);
                Map<String, Object> deleteMap = new HashMap<>();
                deleteMap.put("qry_id",qry_id);
                deleteMap.put("out_id",out_id);
                deleteMap.put("link_qry_id",link_qry_id);
                deleteMap.put("link_in_id", tempJSONObject.getString("link_in_id"));
                sqlSession.delete("query.deleteQueryOutLinkByPrimary", deleteMap);

                if(StringUtils.isNotBlank(linkFinal) && "null"!=linkFinal){
                    // 以逗号分隔，删除掉匹配到的那个
                    String[] a = linkFinal.split(",");
                    String b = link_qry_id+"";
                    String result = "";
                    for(String temp : a){
                        if(b.equals(temp)){
                            continue;
                        }else {
                            result += (temp+",");
                        }
                    }
                    if(result.length()>0){
                        linkFinal = result.substring(0,result.length()-1);
                    }else {
                        linkFinal = "";
                    }
                }

            }
            // 还需要把 qry_out 表的 link的 跟本次记录关联的删除掉
            qryOutFinalMap = qryOutMap;
            qryOutFinalMap.put("link",linkFinal);
            sqlSession.delete("query.deleteQueryOut",qryOutFinalMap);
            sqlSession.insert("query.createQueryOut",qryOutFinalMap);
        }
    }

    public void deleteSqlTemplate(String TemplateName, String SelectID) throws DocumentException, SAXException, IOException {

        String namespace = TemplateName;
        String sqlId = SelectID;
        String userSqlPath = AppConstants.getUserSqlPath() + File.separator + AppConstants.QueryPrefix + namespace + ".xml";

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setSuppressDeclaration(true);
        format.setIndentSize(2);
        format.setNewlines(true);
        format.setTrimText(false);

        Document userDoc = null;
        XMLWriter writer = null;
        try {
            userDoc = XmlUtil.parseXmlToDom(userSqlPath);
            moveSqlId(userDoc, sqlId);
            log.debug("删除SQL,其id为:" + userSqlPath + "-" + sqlId);
            writer = new XMLWriter(new FileOutputStream(userSqlPath), format);
            //删除空白行
            Element root = userDoc.getRootElement();
            removeBlankNewLine(root);
            writer.write(userDoc);
            writer.flush();
            writer.close();
        } catch (java.lang.Exception e) {
            throw e;
        }
    }

    //移除某个节点
    protected void moveSqlId(Document userDoc, String sqlId) {
        List<Element> list = userDoc.selectNodes("//select[@id='" + sqlId + "']");
        for (int i = 0; i < list.size(); i++) {
            list.get(i).getParent().remove(list.get(i));
        }
    }

    private void removeBlankNewLine(Node node) {
        List<Node> list = ((Element) node).content();
        boolean textOnly = true;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            for (Node temp : list) {
                if (temp.getNodeType() != Node.TEXT_NODE) {
                    textOnly = false;
                    break;
                }
            }
        }
        for (Node temp : list) {
            int nodeType = temp.getNodeType();
            switch (nodeType) {
                case Node.ELEMENT_NODE:
                    removeBlankNewLine(temp);
                    break;
                case Node.CDATA_SECTION_NODE:
                    break;
                case Node.COMMENT_NODE:
                    break;
                case Node.TEXT_NODE:
                    Text text = (Text) temp;
                    String value = text.getText();
                    if (!value.trim().equals("")) {
                        //清空右边空白
                        value = value.substring(0, value.indexOf(value.trim().substring(0, 1)) + value.trim().length());
                       /* if (textOnly) {
                            value += "\n";
                        }*/
                    } else {
                        value = value.trim() + "\n";
                    }
                    text.setText(value);
                    break;
                default:
                    break;
            }
        }
    }

    // 创建一个qry函数类别
    public String createQueryClass(String class_name,String img_file, SqlSession sqlSession) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("class_name", class_name);
//        img_file=img_file.replaceAll("/","\\\\");
//        img_file=img_file.replaceAll("\\\\", "\\\\\\\\");
//        map.put("img_file", img_file);
        sqlSession.insert("query.createQueryClass", map);
        String class_id = String.valueOf(map.get("id"));
        // 生成 xml文件
       /* String userSqlPath = AppConstants.getUserSqlPath() + File.separator + AppConstants.QueryPrefix + class_id + ".xml";
        File file = new File(userSqlPath);   // 自增長ID不會重名
        file.createNewFile();
        Document doc = DocumentHelper.createDocument();
        Element mapper = DocumentHelper.createElement("mapper");
        mapper.addAttribute("namespace", AppConstants.QueryPrefix + class_id);
        // 增加缓存信息  -> 每次sqlSession都会关闭掉，所以一级缓存不起作用，要开启二级缓存
        Element cacheElement = mapper.addElement("cache");
        // eviction="LRU" flushInterval="100000" size="1024" readOnly="true"
        cacheElement.addAttribute("eviction", MybatisCacheConfiguration.EVICTION_VALUE);
        cacheElement.addAttribute("flushInterval", MybatisCacheConfiguration.FLUSH_INTERVAL_VALUE);
        cacheElement.addAttribute("size", MybatisCacheConfiguration.SIZE_VALUE);
        cacheElement.addAttribute("readOnly", MybatisCacheConfiguration.READONLY_VALUE);
        doc.add(mapper);
        doc.addDocType("mapper", headModel, null);
        writeToXml(doc, file);*/
        return AppConstants.QueryPrefix + class_id;
    }

    // 把doc节点当中的信息写入到指定file文件当中去
    private void writeToXml(Document doc, File file) throws IOException {
        //写入XML文件
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        format.setTrimText(false);
        format.setIndent(false);
        format.setExpandEmptyElements(true);  // 设置标签 mapper标签不闭合
        XMLWriter writer = null;
        try {
            writer = new XMLWriter(new FileOutputStream(file), format);
            writer.write(doc);
            writer.flush();
            writer.close();
        } catch (java.lang.Exception e) {
            log.error("写入XML异常!" + file.getAbsolutePath());
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 删除一个函数类别，但要判断是否有qry_name 关联qry_class的class_id
    // getFuncInfoRelationClass
    public int deleteQueryClassForRelation(int class_id, SqlSession sqlSession) {
        int i = sqlSession.selectOne("query.getQueryInfoRelationClass", class_id);
        if (i > 0) {
            return 2;  // 代表 存在关联关系,不能删除
        }
        return sqlSession.delete("query.deleteQueryClass", class_id);
    }

    // 修改一个函数类别
    public int updateQueryClass(int class_id,String class_name ,String img_file, SqlSession sqlSession) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("class_id", class_id);
        map.put("class_name", class_name);
        img_file=img_file.replaceAll("/","\\\\");
        img_file=img_file.replaceAll("\\\\", "\\\\\\\\");
        map.put("img_file", img_file);
        // 修改一个函数，传递2个参数
        return sqlSession.update("query.updateQueryClass", map);
    }

    /**
     * 功能描述:  根据qry_id 查找qry表相关的信息
     */
    public JSONObject getQueryByIDVerTwo(SqlSession sqlSession, String qry_id) throws SAXException, DocumentException {
        Map<String, String> param = new HashMap<String, String>();
        param.put("qry_id", qry_id);
        JSONObject jResult = new JSONObject();

        // 查找qry_name
        Map<String, String> mapFunc = new HashMap<String, String>();
        mapFunc = sqlSession.selectOne("query.getNameByID", param);
        //查找定义的SQL语句，先找到对应的类别，然后打开类别对应的文件，找到相的SQL
        if (mapFunc != null && !mapFunc.isEmpty()) {
            jResult = JSONObject.parseObject(JSON.toJSONString(mapFunc, JsonUtil.features));
        }

        //查找函数定义输入参数 qry_in
        List<Map<String, String>> inList = sqlSession.selectList("query.getInByID", param);
        JSONArray inArray = JSONArray.parseArray(JSONArray.toJSONString(inList, JsonUtil.features));
        jResult.put("in", inArray);

        //查找函数定义输出参数 qry_out
        List<Map<String, String>> outList = sqlSession.selectList("query.getOutByID", param);
        JSONArray outArray = JSONArray.parseArray(JSONArray.toJSONString(outList, JsonUtil.features));
        jResult.put("out", outArray);

        return jResult;
    }



    /**
     * 功能描述:  根据qry_id 查找qry表相关的信息
     */
    public JSONObject getQueryByID(SqlSession sqlSession, String qry_id) throws SAXException, DocumentException {
        Map<String, String> param = new HashMap<String, String>();
        param.put("qry_id", qry_id);
        JSONObject jResult = new JSONObject();

        // 查找qry_name
        Map<String, String> mapFunc = new HashMap<String, String>();
        mapFunc = sqlSession.selectOne("query.getNameByID", param);
        //查找定义的SQL语句，先找到对应的类别，然后打开类别对应的文件，找到相的SQL
        if (mapFunc != null && !mapFunc.isEmpty()) {
            jResult = JSONObject.parseObject(JSON.toJSONString(mapFunc, JsonUtil.features));
        }

        //查找函数定义输入参数 qry_in
        List<Map<String, String>> inList = sqlSession.selectList("query.getInByID", param);
        JSONArray inArray = JSONArray.parseArray(JSONArray.toJSONString(inList, JsonUtil.features));
        jResult.put("in", inArray);

        //查找函数定义输出参数 qry_out
        List<Map<String, String>> outList = sqlSession.selectList("query.getOutByID", param);
        JSONArray outArray = JSONArray.parseArray(JSONArray.toJSONString(outList, JsonUtil.features));
        jResult.put("out", outArray);

        return jResult;
    }
    /**
     * 功能描述:  根据qry_id 查找qry表相关的信息
     */
    public List<Map<String, Object>> getOutByQryID(String qry_id) throws SAXException, DocumentException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        //查找函数定义输出参数 qry_out
        Map<String, String> param = new HashMap<String, String>();
        param.put("qry_id", qry_id);
        List<Map<String, Object>> outList = sqlSession.selectList("query.getOutByID", param);
        if(outList.size()>0) {
            for (int i = 0; i < outList.size(); i++) {
                Map<String, Object> m = outList.get(i);
                if (m != null) {
                    String value = null, key = null;
                    java.util.Iterator it = m.entrySet().iterator();
                    while (it.hasNext()) {
                        java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                        key = entry.getKey().toString(); //返回与此项对应的键
                        if (key.equalsIgnoreCase("out_id")) {
                            if(entry.getValue()!=null) {
                                value = entry.getValue().toString().toUpperCase(); //返回与此项对应的值
                                m.put(key, value);
                            }
                        }
                    }
                }
            }
        }
        return outList;
    }


    /**
     * 功能描述: 根据  class_id 查询出 func_name 表当中的信息
     */
    public List<Map<String, Object>> getQueryByClassID(int class_id) throws SAXException, DocumentException {
        JSONObject jResult = new JSONObject();
        List<Map<String, Object>> listQueryName = DbFactory.Open(DbFactory.FORM).
                selectList("query.getQueryNameInfoByClassID", class_id);
        return listQueryName;
    }



    public JSONObject getQueryParam(String qry_id) {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);

        Map<String, String> param = new HashMap<String, String>();
        param.put("qry_id", qry_id);
        JSONObject jResult = new JSONObject();

        //查找函数定义输入参数 qry_in
        List<Map<String, String>> inList = sqlSession.selectList("query.getInByID", param);
        JSONArray inArray = JSONArray.parseArray(JSONArray.toJSONString(inList, JsonUtil.features));
        jResult.put("in", inArray);

        //查找函数定义输出参数 qry_out
        List<Map<String, String>> outList = sqlSession.selectList("query.getOutByID", param);
        JSONArray outArray = JSONArray.parseArray(JSONArray.toJSONString(outList, JsonUtil.features));
        jResult.put("out", outArray);

        return jResult;
    }
    public List<Map<String, String>> getQueryOutLink(String qry_id,String out_id) {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);

        Map<String, String> param = new HashMap<String, String>();
        param.put("qry_id", qry_id);
        param.put("out_id", out_id);
        //查找函数定义输出参数 qry_out
        List<Map<String, String>> outList = sqlSession.selectList("query.getQueryOutLink", param);

        return outList;
    }


    /**
     * 功能描述: 查找 qry_name所有记录
     */
    public List<Map<String, String>> getAllQueryName() {
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        resultList = sqlSession.selectList("query.getAllQueryName");
        return resultList;
    }

    // 取函数类别
    public List<Map<String, String>> getAllQueryClass(SqlSession sqlSession) {
        return sqlSession.selectList("query.getAllQueryClass");
    }

    // 根据 qry_id 查找 qry_name 记录， 组装 getIn,sql,getDb,getNamespace,getId ,getSelectType
    public void assemblySqlTemplateTwo(SqlTemplate sqlTemplate, String namespace, String qry_id) throws DocumentException, SAXException {
        JSONObject jsonObject = this.getQueryByIDVerTwo(DbFactory.Open(DbFactory.FORM), qry_id);
        JSONArray jsonArrayIn = jsonObject.getJSONArray("in");
        if (jsonArrayIn != null && !jsonArrayIn.isEmpty()) {
            sqlTemplate.setIn(jsonArrayIn);
        }
        sqlTemplate.setDb(jsonObject.containsKey("qry_db") ? jsonObject.getString("qry_db") : "");
        sqlTemplate.setId(qry_id);
        sqlTemplate.setSelectType(jsonObject.containsKey("qry_type") ? jsonObject.getString("qry_type") : "");
        // 组装sql
        sqlTemplate.setSql(jsonObject.getString("qry_sql"));
        sqlTemplate.setCached(jsonObject.getString("cached"));
        sqlTemplate.setQryCursorName(jsonObject.containsKey("qry_cursor_name") ? jsonObject.getString("qry_cursor_name") : "");
        sqlTemplate.setQryHttpHeader(jsonObject.containsKey("qry_http_header") ? jsonObject.getString("qry_http_header") : "");
        sqlTemplate.setQryHttpUrl(jsonObject.containsKey("qry_http_url") ? jsonObject.getString("qry_http_url") : "");
        sqlTemplate.setQryHttpResBodyArrayName(jsonObject.containsKey("qry_http_res_body_arrayname") ? jsonObject.getString("qry_http_res_body_arrayname") : "");
        sqlTemplate.setNamespace(AppConstants.QueryPrefix +namespace);
    }

    // 根据 qry_id 查找 qry_name 记录， 组装 getIn,sql,getDb,getNamespace,getId ,getSelectType
    public void assemblySqlTemplate(SqlTemplate sqlTemplate, String namespace, String qry_id) throws DocumentException, SAXException {
        JSONObject jsonObject = this.getQueryByID(DbFactory.Open(DbFactory.FORM), qry_id);
        JSONArray jsonArrayIn = jsonObject.getJSONArray("in");
        if (jsonArrayIn != null && !jsonArrayIn.isEmpty()) {
            sqlTemplate.setIn(jsonArrayIn);
        }
        sqlTemplate.setDb(jsonObject.containsKey("qry_db") ? jsonObject.getString("qry_db") : "");
        sqlTemplate.setId(qry_id);
        sqlTemplate.setSelectType(jsonObject.containsKey("qry_type") ? jsonObject.getString("qry_type") : "");
        // 组装sql
        sqlTemplate.setSql(jsonObject.getString("qry_sql"));
        sqlTemplate.setCached(jsonObject.getString("cached"));
        sqlTemplate.setQryCursorName(jsonObject.containsKey("qry_cursor_name") ? jsonObject.getString("qry_cursor_name") : "");
        sqlTemplate.setQryHttpHeader(jsonObject.containsKey("qry_http_header") ? jsonObject.getString("qry_http_header") : "");
        sqlTemplate.setQryHttpUrl(jsonObject.containsKey("qry_http_url") ? jsonObject.getString("qry_http_url") : "");
        sqlTemplate.setQryHttpResBodyArrayName(jsonObject.containsKey("qry_http_res_body_arrayname") ? jsonObject.getString("qry_http_res_body_arrayname") : "");

        sqlTemplate.setNamespace(AppConstants.QueryPrefix +namespace);
    }
    public List<Map<String, String>> getAuthTree(SqlSession sqlSession,int user_id) {
        if(user_id==1){
            return sqlSession.selectList("query.getAuthTreeAll");
        }else{
            return sqlSession.selectList("query.getAuthTree",user_id);
        }
    }

    public Map<String, Object> getAllFuncClassByClassId(int class_id) throws SAXException, DocumentException {
        Map<String, Object> listQueryName = DbFactory.Open(DbFactory.FORM).
                selectOne("query.getAllFuncClassByClassId", class_id);
        return listQueryName;
    }
    public Map<String, Object> getAllQueryClassByClassId(int class_id) throws SAXException, DocumentException {
        Map<String, Object> listQueryName = DbFactory.Open(DbFactory.FORM).
                selectOne("query.getAllQueryClassByClassId", class_id);
        return listQueryName;
    }
    public Map<String, Object> getQueryNameByClassIdQryId(int class_id,int qry_id) throws SAXException, DocumentException {
        Map<String,Integer> param=new HashMap<String,Integer>();
        param.put("class_id",class_id);
        param.put("qry_id",qry_id);
        Map<String, Object> listQueryName = DbFactory.Open(DbFactory.FORM).
                selectOne("query.getQueryNameByClassIdQryId", param);
        return listQueryName;
    }

    public Map<String, Object> getFunctionClassByClassId(int class_id) throws SAXException, DocumentException {
        Map<String, Object> listQueryName = DbFactory.Open(DbFactory.FORM).
                selectOne("query.getAllFunctionClassByClassId", class_id);
        return listQueryName;
    }
    public Map<String, Object> getFunctionNameByClassIdPId(int class_id,int func_id) throws SAXException, DocumentException {
        Map<String,Integer> param=new HashMap<String,Integer>();
        param.put("class_id",class_id);
        param.put("func_id",func_id);
        Map<String, Object> listQueryName = DbFactory.Open(DbFactory.FORM).
                selectOne("query.getFunctionNameByClassIdPId", param);
        return listQueryName;
    }
    public List<Map<String, Object>> getQueryByName(String qry_name) {
        Map<String,String> param=new HashMap<String,String>();
        param.put("qry_name",qry_name);
        List<Map<String, Object>> listQueryName = DbFactory.Open(DbFactory.FORM)
                                           .selectList("query.getQueryByName", param);
        return listQueryName;
    }
    public Map<String, Object> getQueryByChineseName(String qry_name) {
        Map<String,String> param=new HashMap<String,String>();
        param.put("qry_name",qry_name);
        Map<String, Object> listQueryName = DbFactory.Open(DbFactory.FORM).
                selectOne("query.getQueryByChineseName", param);
        return listQueryName;
    }
    public List<Map<String, Object>> getQueryByOutName(String out_name) {
        Map<String,String> param=new HashMap<String,String>();
        param.put("out_name",out_name);
        List<Map<String, Object>> listQueryName = DbFactory.Open(DbFactory.FORM).
                selectList("query.getQueryByOutName", param);
        return listQueryName;
    }

    // 执行sql
    public Map executeSql(String queryClassName,String queryID,String pJson) throws Exception {
        Map<String,Object> result = new HashMap();
        try {
            SqlTemplate template = new SqlTemplate();
            this.assemblySqlTemplateTwo(template, queryClassName, queryID);
            if (StringUtils.isBlank(template.getSql()) && "sql".equals(template.getSelectType())) {
                throw new Exception("数据库查询SQL为空,无法继续操作");
            }
            String qryType = template.getSelectType();
            if (qryType.equals("sql")) {
                result = this.querySql(pJson, template);
            } else if (qryType.equals("procedure")) {
                result = this.queryProcedure(pJson, template);
            } else if (qryType.equals("http")) {
                result = this.queryHttp(pJson, template);
            }
            //加入输出参数定义
            result.put("out",this.getOutByQryID(queryID));
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
   //查询sql类型的自定义查询
    public Map querySql(String pJson,SqlTemplate template) throws IOException{
        Map<String,Object> result = new HashMap();
        try {
            List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
            List<Map> aResult = new ArrayList<Map>();
            Long totalSize = 0L;
            JSONArray arr = JSON.parseArray(pJson);
            JSONObject params = arr.getJSONObject(0);//查询参数
            JSONObject page = null;
            if (arr.size() > 1) {
                page = arr.getJSONObject(1);  //分页对象
            }
            JSONObject objin = params.getJSONObject("in");
            RowBounds bounds = null;
            if (page == null || page.size() == 0) {
                bounds = RowBounds.DEFAULT;
            } else {
                int startIndex = page.getIntValue("startIndex");
                int perPage = page.getIntValue("perPage");
                if (startIndex == 1 || startIndex == 0) {
                    startIndex = 0;
                } else {
                    startIndex = (startIndex - 1) * perPage;
                }
                bounds = new PageRowBounds(startIndex, perPage);
            }
            Map map = new HashMap();
            if (objin != null) {
                String value = null, key = null;
                java.util.Iterator it = objin.entrySet().iterator();
                while (it.hasNext()) {
                    java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                    key = entry.getKey().toString(); //返回与此项对应的键
                    value = entry.getValue().toString(); //返回与此项对应的值
                    map.put(key, value);
                }
            }
            String db = template.getDb();
            String namespace = template.getNamespace();
            String qryId = template.getId();
            Boolean cached = false;
            if (null != template.getCached() && "1".equals(template.getCached())) {
                cached = true;
            }
            // 强转成自己想要的类型
            SqlSession targetSqlSession = DbFactory.Open(db);
            aResult = (List<Map>) ExecuteSqlUtil.executeDataBaseSql(template.getSql(), targetSqlSession, namespace, qryId, bounds,
                    Map.class, Map.class, map, StatementType.PREPARED, cached, db, null);
            //将集合遍历
            for (int i = 0; i < aResult.size(); i++) {
                //循环new  map集合
                Map<String, Object> obdmap = new HashMap<String, Object>();
                Set<String> se = aResult.get(i).keySet();
                for (String set : se) {
                    //在循环将大写的KEY和VALUE 放到新的Map
                    obdmap.put(set.toUpperCase(), aResult.get(i).get(set));
                }
                //将Map放进List集合里
                newList.add(obdmap);
            }
            if (page != null && page.size() != 0) {
                totalSize = ((PageRowBounds) bounds).getTotal();
            } else {
                totalSize = Long.valueOf(newList.size());
            }
            result.put("list", newList);
            result.put("totalSize", totalSize);
        }catch (Exception e){
            throw e;
        }
        return result;
    }
    //查询存储过程类型的自定义查询
    public Map queryProcedure(String pJson,SqlTemplate template) throws IOException{
        Map<String,Object> result = new HashMap();
        try{
            List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
            List<Map> aResult = new ArrayList<Map>();
            Long totalSize = 0L;
            JSONArray arr = JSON.parseArray(pJson);
            JSONObject params = arr.getJSONObject(0);//查询参数
            JSONObject page = null;
            if (arr.size() > 1) {
                page = arr.getJSONObject(1);  //分页对象
            }
            JSONObject objin = params.getJSONObject("in");
            RowBounds bounds = null;
    //                if (page == null || page.size() == 0) {
    //                    bounds = RowBounds.DEFAULT;
    //                } else {
    //                    int startIndex = page.getIntValue("startIndex");
    //                    int perPage = page.getIntValue("perPage");
    //                    if (startIndex == 1 || startIndex == 0) {
    //                        startIndex = 0;
    //                    } else {
    //                        startIndex = (startIndex - 1) * perPage;
    //                    }
    //                    bounds = new PageRowBounds(startIndex, perPage);
    //                }
            Map map = new HashMap();
            if (objin != null) {
                String value = null, key = null;
                java.util.Iterator it = objin.entrySet().iterator();
                while (it.hasNext()) {
                    java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                    key = entry.getKey().toString(); //返回与此项对应的键
                    value = entry.getValue().toString(); //返回与此项对应的值
                    map.put(key, value);
                }
            }
            String db = template.getDb();
            String namespace = template.getNamespace();
            String qryId = template.getId();
            Boolean cached = false;
            if (null != template.getCached() && "1".equals(template.getCached())) {
                cached = true;
            }
            String sqlPro = "{call " + template.getSql() + "}";
            SqlSession targetSqlSession = DbFactory.Open(db);
            String qryCursorName = template.getQryCursorName();
            map.put(template.getQryCursorName(), new ArrayList<Map<String, Object>>());
            newList = (List<Map<String, Object>>) ExecuteSqlUtil.executeDataBaseSql(sqlPro, targetSqlSession, namespace, qryId, null,
                    Map.class, Map.class, map, StatementType.CALLABLE, cached, db, qryCursorName);
            totalSize = Long.valueOf(newList.size());
            result.put("list", newList);
            result.put("totalSize", totalSize);
        }catch (Exception e){
            throw e;
        }
        return result;
    }
    //查询http链接类型的自定义查询
    public Map queryHttp(String pJson,SqlTemplate template) throws IOException {
        Map<String,Object> result = new HashMap();
        try{
            List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
            List<Map> aResult = new ArrayList<Map>();
            Long totalSize = 0L;
            String results = "";
            // 创建httpclient对象
            CloseableHttpClient httpClient = HttpClients.createDefault();
            // 创建post方式请求对象
            HttpPost httpPost = new HttpPost(template.getQryHttpUrl().trim());
            // 设置参数到请求对象中
            StringEntity stringEntity = new StringEntity(pJson, ContentType.APPLICATION_JSON);
            stringEntity.setContentEncoding("utf-8");
            httpPost.setEntity(stringEntity);
            if(null!=template.getQryHttpHeader() && !"".equals(template.getQryHttpHeader())) {
                String[] arrlist = template.getQryHttpHeader().split("\\n");
                for (int i = 0; i < arrlist.length; i++) {
                    String arrlistV = arrlist[i];
                    if(arrlistV.indexOf(":")>1) {
                        String arrlistV1 = arrlistV.substring(0, arrlistV.indexOf(":"));
                        String arrlistVa = arrlistV.substring(arrlistV.indexOf(":") + 1, arrlistV.length());
                        httpPost.addHeader(arrlistV1, arrlistVa);
                        // httpPost.addHeader("credentials",template.getQryHttpHeader());
                    }
                }
            }
            // 执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = httpClient.execute(httpPost);
            // 获取结果实体
            // 判断网络连接状态码是否正常(0--200都数正常)
            if (response.getStatusLine().getStatusCode() == 200) {
                results = EntityUtils.toString(response.getEntity(), "utf-8");
                String paramvalue= template.getQryHttpResBodyArrayName();
                if(null==paramvalue || "".equals(paramvalue)){
                    JSONArray jsonObj=JSON.parseArray(results);
                    if(null!=jsonObj){
                        try {
                            aResult= (List)jsonObj;
                        }catch (Exception e) {
                            aResult =null;
                        }
                    }
                }else{
                    String[] arrp=paramvalue.split("\\.");
                    int arrl=arrp.length;
                    if(arrl==1){
                        if(null!=arrp[0]) {
                            JSONObject jsonObj = JSON.parseObject(results);
                            if (null != jsonObj.get(arrp[0])) {
                                try {
                                    aResult = (List<Map>) jsonObj.get(arrp[0]);
                                } catch (Exception e) {
                                    aResult = null;
                                }
                            }
                        }
                    }else{
                        JSONObject jsonObj = JSON.parseObject(results);
                        JSONObject o= (JSONObject) jsonObj.get(arrp[0]);
                        if(null!=o) {
                            for (int i = 1; i < arrl; i++) {
                                if ((i + 1) == arrl) {
                                    aResult = (List<Map>) o.get(arrp[arrl - 1]);
                                } else {
                                    o = JSON.parseObject(arrp[i]);
                                }
                            }
                        }
                    }
                }
                if(null!=aResult) {
                    for (int i = 0; i < aResult.size(); i++) {
                        //循环new  map集合
                        Map<String, Object> obdmap = new HashMap<String, Object>();
                        Set<String> se = aResult.get(i).keySet();
                        for (String set : se) {
                            //在循环将大写的KEY和VALUE 放到新的Map
                            obdmap.put(set.toUpperCase(), aResult.get(i).get(set));
                        }
                        //将Map放进List集合里
                        newList.add(obdmap);
                    }
                }
            }
            // 释放链接
            response.close();
            result.put("list", newList);
            result.put("totalSize", totalSize);
        }catch (Exception e){
            throw e;
        }
        return result;
    }

}
