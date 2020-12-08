package root.report.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.i18n.Exception;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultCDATA;
import org.dom4j.tree.DefaultComment;
import org.dom4j.tree.DefaultElement;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import root.configure.AppConstants;
import root.configure.MybatisCacheConfiguration;
import root.report.db.DbFactory;
import root.report.query.SqlTemplate;
import root.report.util.JsonUtil;
import root.report.util.XmlUtil;

import java.io.*;
import java.lang.*;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class FunctionService {

    public static final String headModel = "-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd";

    private static Logger log = Logger.getLogger(FunctionService.class);

    //  根据 func_id 查找出所有的 func_name、func_class、func_in、func_out 表信息
    public JSONObject getFunctionByID(String func_id) throws SAXException, DocumentException {
        Map<String, String> param = new HashMap<String, String>();
        param.put("func_id", func_id);
        JSONObject jResult = new JSONObject();
        //查找函数定义头
        Map<String, String> mapFunc = new HashMap<String, String>();
        mapFunc = DbFactory.Open(DbFactory.FORM)
                .selectOne("function.getNameByID", param);
        //查找定义的SQL语句，先找到对应的类别，然后打开类别对应的文件，找到相的SQL
        if(mapFunc !=null && !mapFunc.isEmpty()){
            jResult = JSONObject.parseObject(JSON.toJSONString(mapFunc, JsonUtil.features));
        }
        //查找函数定义输入参数
        List<Map<String, String>> inList = DbFactory.Open(DbFactory.FORM)
                .selectList("function.getInByID", param);
        JSONArray inArray = JSONArray.parseArray(JSONArray.toJSONString(inList, JsonUtil.features));
        jResult.put("in", inArray);
        //查找函数定义输出参数
        List<Map<String, String>> outList = DbFactory.Open(DbFactory.FORM)
                .selectList("function.getOutByID", param);
        JSONArray outArray = JSONArray.parseArray(JSONArray.toJSONString(outList, JsonUtil.features));
        jResult.put("out", outArray);
        return jResult;
    }

    /**
     * 功能描述: 根据  class_id 查询出 func_name 表当中的信息
     */
    public List<Map<String,Object>>  getFunctionByClassID(int class_id) throws SAXException, DocumentException {
        List<Map<String,Object>> listFuncName = DbFactory.Open(DbFactory.FORM).
                selectList("function.getFuncNameInfoByClassID",class_id);
        return listFuncName;
    }

    // 根据 func_id 查询出对应的  func_in 跟func_out 表当中的信息
    public  JSONObject  getFunctionParam(String func_id){
        Map<String, String> param = new HashMap<String, String>();
        param.put("func_id", func_id);
        JSONObject jResult = new JSONObject();
        //查找函数定义输入参数
        List<Map<String, String>> inList = DbFactory.Open(DbFactory.FORM)
                .selectList("function.getInByID", param);
        JSONArray inArray = JSONArray.parseArray(JSONArray.toJSONString(inList, JsonUtil.features));
        jResult.put("in", inArray);
        //查找函数定义输出参数
        List<Map<String, String>> outList = DbFactory.Open(DbFactory.FORM)
                .selectList("function.getOutByID", param);
        JSONArray outArray = JSONArray.parseArray(JSONArray.toJSONString(outList, JsonUtil.features));
        jResult.put("out", outArray);
        return jResult;
    }


    public String createFunctionName(SqlSession sqlSession, JSONObject jsonFunc) throws Exception {
            Map<String, Object> mapFunc = new HashMap<>();   // 必须设定为Object,因为我们想要让其返回自增长类型值
            mapFunc.put("class_id", jsonFunc.getString("class_id"));
            // mapFunc.put("func_id", jsonFunc.getString("func_id"));  // func_id 自增长
            mapFunc.put("func_name", jsonFunc.getString("func_name"));
            mapFunc.put("func_desc", jsonFunc.getString("func_desc"));
            mapFunc.put("func_type", jsonFunc.getString("func_type"));
            mapFunc.put("func_db", jsonFunc.getString("func_db"));
            // mapFunc.put("func_sql", jsonFunc.getString("func_sql"));
            String escapeSQL = jsonFunc.getString("func_sql");
            escapeSQL = escapeSQL.replace("'","\\'").replace("{","\\{").replace("}","\\}");
            mapFunc.put("func_sql", escapeSQL);

            sqlSession.insert("function.createFunctionName", mapFunc);

            return mapFunc.get("id").toString();
    }


    public int updateFunctionName(SqlSession sqlSession, JSONObject jsonFunc)
    {
        Map<String, String> mapFunc = new HashMap<>();
        mapFunc.put("class_id", jsonFunc.getString("class_id"));
        mapFunc.put("func_id", jsonFunc.getString("func_id"));
        mapFunc.put("func_name", jsonFunc.getString("func_name"));
        mapFunc.put("func_desc", jsonFunc.getString("func_desc"));
        mapFunc.put("func_type", jsonFunc.getString("func_type"));
        mapFunc.put("func_db", jsonFunc.getString("func_db"));
        String escapeSQL = jsonFunc.getString("func_sql");
        escapeSQL = escapeSQL.replace("'","\\'").replace("{","\\{").replace("}","\\}");
        mapFunc.put("func_sql", escapeSQL);
        return sqlSession.update("function.updateFunctionName", mapFunc);
    }
  /*  public int deleteFunctionName(String aFunID) {

        //拿到sqlSerssion
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        JSONObject jsonFunc = JSONObject.parseObject(aJson);

        try {
            //删除In更新头

            //删除Out

            //删除头

            //删除mybatis配置文件文件


        } catch (java.lang.Exception ex) {

        }
        return "";
    }
    public  String getSqlTemplate(){

    }*/
    public void createSqlTemplate(String TemplateName, String SelectID, String aSQLTemplate) throws DocumentException, SAXException, IOException {

        String namespace = TemplateName;
        String sqlId = SelectID;
        String userSqlPath = AppConstants.getUserFunctionPath() + File.separator +AppConstants.FunctionPrefix+ namespace + ".xml";

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
            newSql.addAttribute("resultType", "BigDecimal");
            newSql.addAttribute("parameterType", "Map");
            //  设置2级缓存
            newSql.addAttribute("useCache", MybatisCacheConfiguration.USE_CACHE_FALSE);
            // newSql.addText(aSQLTemplate);
            addSqlText(newSql,aSQLTemplate);
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

    private void addSqlText(Element select, String sqlText) throws DocumentException{
        String xmlText = "<sql>"+sqlText+"</sql>";
        Document doc = DocumentHelper.parseText(xmlText);
        //获取根节点    
        Element root = doc.getRootElement();
        List<Node> content = root.content();
        for (int i = 0; i < content.size(); i++) {
            Node node = content.get(i);
            select.add((Node)node.clone());
        }
    }

    /**
     * 功能描述:  得到指定文件指定id的 sql内容  ** bool 若为true则代表得到 原string内容，为false则代表只要转义好的sql
     */
    public String getSqlTemplate(String TemplateName, String SelectID,Boolean bool) throws DocumentException, SAXException {

        String namespace = TemplateName;
        String sqlId = SelectID;
        String userSqlPath = AppConstants.getUserFunctionPath() + File.separator +AppConstants.FunctionPrefix+ namespace + ".xml";

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setSuppressDeclaration(true);
        format.setIndentSize(2);
        format.setNewlines(true);
        format.setTrimText(false);

        XMLWriter writer = null;
        Document userDoc = null;
        try {
            userDoc = XmlUtil.parseXmlToDom(userSqlPath);
            Element select = (Element)userDoc.selectSingleNode("//select[@id='"+sqlId+"']");
            StringBuffer tempStr = new StringBuffer();
            if(bool){
                List<Object> list = select.content();
                Object object = null;
                for (int i = 0; i < list.size(); i++) {
                    object = list.get(i);
                    if (object instanceof DefaultElement){
                        // 解析element当中的 内容
                        String text = ((Node)object).asXML();
                        text = text.replaceAll("&lt;","<");
                        text = text.replaceAll("&gt;",">");
                        text = text.replaceAll("&apos;","'");
                        text = text.replaceAll("&quot;","\"");
                        tempStr.append(text);
                    } else{
                        tempStr.append(((Node)object).asXML());
                    }
                }
            }else {
                tempStr.append(select.getTextTrim());
            }
            log.debug("获取到的SQL为:" +tempStr);
            return tempStr.toString();
        } catch (java.lang.Exception e) {
            throw e;
        }
    }

    public String updateSqlTemplate(String TemplateName, String SelectID, String aSQLTemplate) throws DocumentException, SAXException, IOException {

        String namespace = TemplateName;
        String sqlId = SelectID;
        String userSqlPath = AppConstants.getUserFunctionPath() + File.separator +AppConstants.FunctionPrefix+ namespace + ".xml";

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setSuppressDeclaration(true);
        format.setIndentSize(2);
        format.setNewlines(true);
        format.setTrimText(false);

        XMLWriter writer = null;
        Document userDoc = null;
        try {
            userDoc = XmlUtil.parseXmlToDom(userSqlPath);
            Element select = (Element)userDoc.selectSingleNode("//select[@id='"+sqlId+"']");
            select.clearContent();
            this.addSqlText(select,aSQLTemplate);

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

    public void deleteSqlTemplate(String TemplateName, String SelectID) throws DocumentException, SAXException, IOException {

        String namespace = TemplateName;
        String sqlId = SelectID;
        String userSqlPath = AppConstants.getUserFunctionPath() + File.separator +AppConstants.FunctionPrefix+ namespace + ".xml";

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setSuppressDeclaration(true);
        format.setIndentSize(2);
        format.setNewlines(true);
        format.setTrimText(false);

        Document userDoc = null;
        XMLWriter writer = null;
        try {
            userDoc = XmlUtil.parseXmlToDom(userSqlPath);
            moveSqlId(userDoc,sqlId);
            log.debug("删除SQL,其id为:" +userSqlPath+"-"+sqlId);
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
    protected void moveSqlId(Document userDoc, String sqlId)
    {
        List<Element> list = userDoc.selectNodes("//select[@id='"+sqlId+"']");
        for (int i = 0; i < list.size(); i++)
        {
            list.get(i).getParent().remove(list.get(i));
        }
    }

    private void removeBlankNewLine(Node node){
        List<Node> list = ((Element)node).content();
        boolean textOnly = true;
        if(node.getNodeType()==Node.ELEMENT_NODE){
            for(Node temp:list){
                if(temp.getNodeType()!=Node.TEXT_NODE){
                    textOnly = false;
                    break;
                }
            }
        }
        for(Node temp:list){
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
                    Text text =  (Text)temp;
                    String value = text.getText();
                    if(!value.trim().equals("")){
                        //清空右边空白
                        value = value.substring(0,value.indexOf(value.trim().substring(0, 1))+value.trim().length());
                       /* if(textOnly){
                            value+="\n";
                        }*/
                    }else{
                        value = value.trim()+"\n";
                    }
                    text.setText(value);
                    break;
                default:break;
            }
        }
    }

    public void createFunctionIn(SqlSession sqlSession,JSONArray jsonArrayIn,String func_id) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < jsonArrayIn.size(); i++) {
            JSONObject jsonIn = jsonArrayIn.getJSONObject(i);
            map.put("func_id",func_id);
            map.put("in_id", jsonIn.getString("in_id"));
            map.put("in_name", jsonIn.getString("in_name"));
            map.put("datatype", jsonIn.getString("datatype"));
            map.put("dict_id", jsonIn.getString("dict_id"));
            map.put("validate", jsonIn.getString("validate"));
            map.put("default_value", jsonIn.getString("default_value"));
            map.put("isformula", jsonIn.getString("isformula"));
            map.put("authtype_id", jsonIn.getString("authtype_id"));
            sqlSession.insert("function.createFunctionIn", map);
        }
    }

    /**
     * 功能描述: 删除func_in表的记录
     */
    public void updateFunctionIn(SqlSession sqlSession,JSONArray jsonArrayIn) {

        Map<String, String> map = new HashMap<>();
        Map<String, String> deleteMap = new HashMap<>();
        for (int i = 0; i < jsonArrayIn.size(); i++) {
            JSONObject jsonIn = jsonArrayIn.getJSONObject(i);
            deleteMap.put("func_id",jsonIn.getString("func_id"));
            deleteMap.put("in_id",jsonIn.getString("in_id"));
            deleteFunctionIn(sqlSession,deleteMap);   // 先删除后插入
            map.put("func_id",jsonIn.getString("func_id"));
            map.put("in_id", jsonIn.getString("in_id"));
            map.put("in_name", jsonIn.getString("in_name"));
            map.put("datatype", jsonIn.getString("datatype"));
            map.put("dict_id", jsonIn.getString("dict_id"));
            map.put("validate", jsonIn.getString("validate"));
            map.put("default_value", jsonIn.getString("default_value"));
            map.put("isformula", jsonIn.getString("isformula"));
            map.put("authtype_id", jsonIn.getString("authtype_id"));
            sqlSession.insert("function.createFunctionIn", map);
        }
    }

    /**
     *
     * 功能描述: 针对传递进来的JSONAarray进行批量删除func_in数据
     */
    public void deleteFunctionInForJsonArray(SqlSession sqlSession,int funcId) {
        sqlSession.delete("function.deleteFunctionInByFuncId",funcId);
    }

    /**
     * 功能描述: 根据map结构删除func_in表的记录
     */
    public int deleteFunctionIn(SqlSession sqlSession,Map map) {
        return sqlSession.delete("function.deleteFunctionIn", map);
    }

    public void createFunctionOut(SqlSession sqlSession,JSONArray jsonArray,String func_id) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            map.put("func_id", func_id);
            map.put("out_id", jsonObject.getString("out_id"));
            map.put("out_name", jsonObject.getString("out_name"));
            map.put("datatype", jsonObject.getString("datatype"));
            // map.put("link", jsonObject.getString("link"));  // 不能存放前端所有数据 255个大小不够存储，仅存储 link_qry_id即可

            // 往 func_out_link 当中插入对应记录
           /* JSONObject outJsonObject = jsonObject.getJSONObject("link");
            Map<String,Object> outMap = new HashMap<>();
            outMap.put("func_id",func_id);
            outMap.put("out_id",jsonObject.getString("out_id"));
            outMap.put("link_qry_id",outJsonObject.getString("link_qry_id"));
           if(outJsonObject!=null && !outJsonObject.isEmpty()){
                map.put("link",outJsonObject.getString("link_qry_id"));
            }
            JSONArray linkIdJSONArray = outJsonObject.getJSONArray("param");
            if(linkIdJSONArray!=null && !linkIdJSONArray.isEmpty()){
                for(int j=0; j<linkIdJSONArray.size();j++){
                    if(outMap!=null && !outMap.isEmpty()){
                        JSONObject tempJSONObject = linkIdJSONArray.getJSONObject(j);
                        Map<String,Object> insertMap = new HashMap<>();
                        insertMap.putAll(outMap);
                        insertMap.put("link_in_id",tempJSONObject.getString("link_in_id"));
                        insertMap.put("link_in_id_value_type",tempJSONObject.getString("link_in_id_value_type"));
                        insertMap.put("link_in_id_value",tempJSONObject.getString("link_in_id_value"));
                        sqlSession.insert("function.createFuncOutLink",insertMap);
                    }
                }
            }*/
            sqlSession.insert("function.createFunctionOut", map);
        }
    }

    /**
     * 功能描述: 往 func_out_link 表插入数据
     */
    public void createFuncOutLink(SqlSession sqlSession, JSONObject jsonObject) {
        //保存到 func_out_link 表
        //更新 func_out 表的link字段
        int qry_id = jsonObject.getIntValue("func_id");
        int link_qry_id = jsonObject.getIntValue("link_qry_id");
        String out_id = jsonObject.getString("out_id");
        JSONArray jsonArray = jsonObject.getJSONArray("param");
        if (StringUtils.isNotBlank(qry_id+"") && StringUtils.isNotBlank(link_qry_id+"")
                && jsonArray!=null) {
            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject tempJSONObject = jsonArray.getJSONObject(j);
                Map<String, Object> insertMap = new HashMap<>();
                insertMap.put("func_id",qry_id);
                insertMap.put("out_id",out_id);
                insertMap.put("link_qry_id",link_qry_id);
                insertMap.put("link_in_id", tempJSONObject.getString("link_in_id"));
                insertMap.put("link_in_id_value_type", tempJSONObject.getString("link_in_id_value_type"));
                insertMap.put("link_in_id_value", tempJSONObject.getString("link_in_id_value"));
                sqlSession.insert("function.createFuncOutLink", insertMap);
            }
        }
        // 更新qry_out表的link字段
        Map<String,Object> tempMap = new HashMap<>();
        tempMap.put("func_id",qry_id);
        tempMap.put("out_id",out_id);
        Map<String,Object> qryOutMap = sqlSession.selectOne("function.getOutByMap",tempMap);
        String link = String.valueOf(qryOutMap.get("link"));
        if(StringUtils.isNotBlank(link) && "null"!=link){
            link += ",";
            link += link_qry_id;
        }else {
            link = link_qry_id+"";
        }
        qryOutMap.put("link",link);
        sqlSession.update("function.updateFuncOutForLink",qryOutMap);
    }

    // 根据qry_id 删除掉 qry_out_link 表当在的记录
    public void deleteFuncOutLinkByPrimary(SqlSession sqlSession, JSONObject jsonObject) {
        int qry_id = jsonObject.getIntValue("func_id");
        int link_qry_id = jsonObject.getIntValue("link_qry_id");
        String out_id = jsonObject.getString("out_id");
        JSONArray jsonArray = jsonObject.getJSONArray("param");
        if (StringUtils.isNotBlank(qry_id+"") && StringUtils.isNotBlank(link_qry_id+"")
                && jsonArray!=null) {
            String linkFinal = "";
            Map<String,Object>  qryOutFinalMap = new HashMap<>();
            qryOutFinalMap.put("func_id",qry_id);
            qryOutFinalMap.put("out_id",out_id);
            Map<String,Object> qryOutMap = sqlSession.selectOne("function.getOutByMap",qryOutFinalMap);
            linkFinal = String.valueOf(qryOutMap.get("link"));
            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject tempJSONObject = jsonArray.getJSONObject(j);
                Map<String, Object> deleteMap = new HashMap<>();
                deleteMap.put("func_id",qry_id);
                deleteMap.put("out_id",out_id);
                deleteMap.put("link_qry_id",link_qry_id);
                deleteMap.put("link_in_id", tempJSONObject.getString("link_in_id"));
                sqlSession.delete("function.deleteFunctionOutLinkByPrimary", deleteMap);

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
            sqlSession.delete("function.deleteFunctionOut",qryOutFinalMap);
            sqlSession.insert("function.createFunctionOut",qryOutFinalMap);
        }
    }

    /**
     * 功能描述: 修改func_out表的记录
     */
    public void updateFunctionOut(SqlSession sqlSession,JSONArray jsonArrayIn) {
        Map<String, String> map = new HashMap<>();

        for (int i = 0; i < jsonArrayIn.size(); i++) {
            Map<String, Object> deleteMap = new HashMap<>();
            JSONObject jsonOut = jsonArrayIn.getJSONObject(i);
            deleteMap.put("func_id",jsonOut.getString("func_id"));
            deleteMap.put("out_id",jsonOut.getString("out_id"));
            deleteFunctionOut(sqlSession,deleteMap);   // 先删除后插入
            map.put("func_id", jsonOut.getString("func_id"));
            map.put("out_id", jsonOut.getString("out_id"));
            map.put("out_name", jsonOut.getString("out_name"));
            map.put("datatype", jsonOut.getString("datatype"));
            // map.put("link", jsonOut.getString("link"));  // link这个将超过 255个字符，只对其保留 link_qry_id 即可
            JSONObject outJsonObject = jsonOut.getJSONObject("link");
            map.put("link",outJsonObject.getString("link_qry_id"));

            deleteMap.put("link_qry_id",outJsonObject.getIntValue("link_qry_id"));
            // 删除掉 func_out_link 表当中对应的记录
           /* JSONArray linkIdJSONArray = outJsonObject.getJSONArray("param");
            if(linkIdJSONArray!=null && !linkIdJSONArray.isEmpty()){
                for(int j=0; j<linkIdJSONArray.size();j++){
                    if(deleteMap!=null && !deleteMap.isEmpty()){
                        JSONObject tempJSONObject = linkIdJSONArray.getJSONObject(j);
                        Map<String,Object> deleteOutLinkMap = new HashMap<>();
                        deleteOutLinkMap.putAll(deleteMap);
                        deleteOutLinkMap.put("link_in_id",tempJSONObject.getString("link_in_id"));
                        sqlSession.delete("function.deleteFunctionOutLinkByPrimary",deleteOutLinkMap);

                        // 增加 func_out_link 表当中对应的记录
                        deleteOutLinkMap.put("link_in_id_value_type",tempJSONObject.getString("link_in_id_value_type"));
                        deleteOutLinkMap.put("link_in_id_value",tempJSONObject.getString("link_in_id_value"));
                        sqlSession.insert("function.createFuncOutLink",deleteOutLinkMap);
                    }
                }
            }*/

            sqlSession.insert("function.createFunctionOut", map);
        }
    }
    /**
     * 功能描述: 删除func_out表的记录
     */
    public void deleteFunctionOut(SqlSession sqlSession,Map map) {
        sqlSession.delete("function.deleteFunctionOut", map);
    }

    /**
     * 功能描述: 针对传递进来的JSONAarray进行批量删除func_out数据
     */
    public void deleteFunctionOutForJsonArray(SqlSession sqlSession,int func_id) throws SQLException {
        // this.deleteFunctionOutLinkByFuncId(sqlSession,func_id);  // 删除掉 func_out_link 表当中的信息
        sqlSession.delete("function.deleteFunctionOutByFuncId",func_id);
    }

    // 根据 func_id  跟 out_id 删除掉 func_out_link 表相关的记录
    public void deleteFunctionOutLinkByFuncId(SqlSession sqlSession,int func_id){
        sqlSession.delete("function.deleteFunctionOutLinkByFuncId",func_id);
    }

    // 根据 func_out_link 的主键 删除掉 其记录
    public void deleteFunctionOutLinkByPrimary(SqlSession sqlSession,Map map){
        sqlSession.delete("function.deleteFunctionOutLinkByPrimary",map);
    }

    //查找func主表
    public String getFunctionName(Map<String, String> map) {
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        List<Map<String, String>> selectMap = DbFactory.Open(DbFactory.FORM).selectList("function.getFunctionName", map);
        if (selectMap != null && selectMap.size() > 0) {
            resultList.addAll(selectMap);
        } else {
            return "";
        }
        // 默认返回第一个
        return JSONObject.toJSONString(resultList.get(0));
    }

    //查找输入参数
    public String getIn(Map<String, String> map) {
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        List<Map<String, String>> selectMap = DbFactory.Open(DbFactory.FORM).selectList("function.getFunctionName", map);
        if (selectMap != null && selectMap.size() > 0) {
            resultList.addAll(selectMap);
        } else {
            return "";
        }
        // 默认返回第一个
        return JSONObject.toJSONString(resultList.get(0));
    }

    //查询输出参数
    public String getOut(Map<String, String> map) {
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        List<Map<String, String>> selectMap = DbFactory.Open(DbFactory.FORM).selectList("function.getFunctionName", map);
        if (selectMap != null && selectMap.size() > 0) {
            resultList.addAll(selectMap);
        } else {
            return "";
        }
        // 默认返回第一个
        return JSONObject.toJSONString(resultList.get(0));
    }


    public List<Map<String, String>> getAllFunctionName() {
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        resultList = sqlSession.selectList("function.getAllFunctionName");
        return resultList;

    }

    /**
     * 功能描述: 往func_name表增加记录
     */
    public int addFunctionName(List<Map<String, String>> mapList) {
        // 事务管理 放在了controller层,return 0 意味着要进行事务回滚
        for (Map<String, String> tempMap : mapList) {
            int number = DbFactory.Open(DbFactory.FORM).insert("function.addFunctionName", tempMap);
            if (number != 1) {
                return 0;
            }
        }
        return 1;
    }

    /**
     * 功能描述: 根据传递过来的JSONObject，对其解析，然后往func_name表增加记录
     */
    public int addFunctionNameForJson(JSONObject jsonObject) {
        List<Map<String, String>> tempTestMapList = new ArrayList<Map<String, String>>();
        // '${class}', '${name}', '${desc}', '${type}', '${file}', '${url}'
        Map<String, String> tempMap = new HashMap<String, String>();
        JSONObject jsonParse = jsonObject.getJSONObject("comment");
        tempMap.put("class", jsonObject.getString("namespace"));
        tempMap.put("name", jsonObject.getString("id"));
        tempMap.put("desc", jsonParse.getString("desc"));
        tempMap.put("type", jsonParse.getString("type"));
        // tempMap.put("file",null);
        // tempMap.put("url",null);
        tempTestMapList.add(tempMap);
        return this.addFunctionName(tempTestMapList);
    }


    /**
     * 功能描述: 删除func_name当中的记录
     */
    public void deleteFunctionName(SqlSession sqlSession,int funcId) {
        sqlSession.delete("function.deleteFunctionName", funcId);
    }

    /**
     * 功能描述: 新增func_out表的记录
     */
    public int addFunctionOut(List<Map<String, String>> mapList) {
        // 事务管理 放在了controller层,return 0 意味着要进行事务回滚
        for (Map<String, String> tempMap : mapList) {
            int number = DbFactory.Open(DbFactory.FORM).insert("function.addFunctionOut", tempMap);
            if (number != 1) {
                return 0;
            }
        }
        return 1;
    }

    public int addFunctionOutForJson(JSONObject jsonObject, String funcId) {
        JSONObject jsonParse = jsonObject.getJSONObject("comment");
        List<Map<String, String>> funcOutMapList = new ArrayList<Map<String, String>>();
        JSONArray jsonFuncOutArray = jsonParse.getJSONArray("out");
        String funcOutStr = JSONArray.toJSONString(jsonFuncOutArray, SerializerFeature.WriteMapNullValue);
        List<Map> parseFuncOutMap = JSONObject.parseArray(funcOutStr, Map.class);
        int addFuncOutNumber = 0;
        for (Map funcOutMap : parseFuncOutMap) {
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("func_id", funcId);
            paramMap.put("out_id", String.valueOf(funcOutMap.get("id")));
            paramMap.put("out_name", String.valueOf(funcOutMap.get("name")));
            paramMap.put("link", String.valueOf(funcOutMap.get("link")));
            funcOutMapList.add(paramMap);
        }
        return this.addFunctionOut(funcOutMapList);
    }

    // 取函数类别
    public List<Map<String, String>> getAllFunctionClass(SqlSession sqlSession) {
        return sqlSession.selectList("function.getAllFunctionClass");
    }

    // 创建一个函数类别
    public String createFunctionClass(String class_name, SqlSession sqlSession) throws IOException {
        Map<String,Object> map = new HashMap<>();
        map.put("class_name",class_name);
        sqlSession.insert("function.createFunctionClass", map);
        String class_id  = String.valueOf(map.get("id"));
        // 生成 xml文件
       /* String userSqlPath = AppConstants.getUserFunctionPath() + File.separator + AppConstants.FunctionPrefix+class_id + ".xml";
        File file = new File(userSqlPath);   // 自增長ID不會重名
        file.createNewFile();
        Document doc = DocumentHelper.createDocument();
        Element mapper = DocumentHelper.createElement("mapper");
        mapper.addAttribute("namespace",AppConstants.FunctionPrefix+class_id);
        // 开启2级缓存
        // 增加缓存信息  -> 每次sqlSession都会关闭掉，所以一级缓存不起作用，要开启二级缓存
        Element cacheElement = mapper.addElement("cache");
        // eviction="LRU" flushInterval="100000" size="1024" readOnly="true"
        cacheElement.addAttribute("eviction", MybatisCacheConfiguration.EVICTION_VALUE);
        cacheElement.addAttribute("flushInterval",MybatisCacheConfiguration.FLUSH_INTERVAL_VALUE);
        cacheElement.addAttribute("size",MybatisCacheConfiguration.SIZE_VALUE);
        cacheElement.addAttribute("readOnly", MybatisCacheConfiguration.READONLY_VALUE);
        doc.add(mapper);
        doc.addDocType("mapper", headModel, null);
        writeToXml(doc, file);*/
        return AppConstants.FunctionPrefix+class_id;
    }

    private void writeToXml(Document doc, File file) throws IOException {
        //写入XML文件
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        format.setTrimText(false);
        format.setIndent(false);
        format.setExpandEmptyElements(true);  // 设置标签 mapper标签不闭合
        XMLWriter writer = null;
        try
        {
            writer = new XMLWriter(new FileOutputStream(file),format);
            writer.write(doc);
            writer.flush();
            writer.close();
        }catch (java.lang.Exception e){
            log.error("写入XML异常!"+file.getAbsolutePath());
            e.printStackTrace();
        }finally {
            if(writer!=null)
            {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 删除一个函数类别，但要判断是否有func_name 关联func_class的class_id
    // getFuncInfoRelationClass
    public int deleteFunctionClassForRelation(int class_id, SqlSession sqlSession) {
        int i = sqlSession.selectOne("function.getFuncInfoRelationClass",class_id);
        if(i>0){
            return 2;  // 代表 存在关联关系,不能删除
        }
        return sqlSession.delete("function.deleteFunctionClass", class_id);
    }

    // 删除一个函数类别
    public int deleteFunctionClass(int class_id, SqlSession sqlSession) {
        return sqlSession.delete("function.deleteFunctionClass", class_id);
    }

    // 修改一个函数类别
    public int updateFunctionClass(int class_id, String class_name, SqlSession sqlSession) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("class_id", class_id);
        map.put("class_name", class_name);
        // 修改一个函数，传递2个参数
        return sqlSession.update("function.updateFunctionClass", map);
    }

    // 根据 func_id 查找func_name 记录， 组装 getIn,sql,getDb,getNamespace,getId ,getSelectType
    public void assemblySqlTemplate(SqlTemplate sqlTemplate,String namespace,String func_id) throws DocumentException, SAXException {
        JSONObject jsonObject = this.getFunctionByID(func_id);
        JSONArray jsonArrayIn = jsonObject.getJSONArray("in");
        if(jsonArrayIn!=null && !jsonArrayIn.isEmpty()){
            sqlTemplate.setIn(jsonArrayIn);
        }
        sqlTemplate.setDb(jsonObject.containsKey("func_db")?jsonObject.getString("func_db"):"");
        sqlTemplate.setId(func_id);
        sqlTemplate.setSelectType(jsonObject.containsKey("func_type")?jsonObject.getString("func_type"):"");
        // 组装sql -> 改到从 数据库当中去  func_sql
        // sqlTemplate.setSql(getSqlTemplate(namespace,func_id,false));
        sqlTemplate.setSql(jsonObject.containsKey("func_sql")?jsonObject.getString("func_sql"):"");
        sqlTemplate.setNamespace(namespace);
    }



}
