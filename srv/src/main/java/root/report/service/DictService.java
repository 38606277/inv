package root.report.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.PageRowBounds;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultCDATA;
import org.dom4j.tree.DefaultComment;
import org.dom4j.tree.DefaultElement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;
import root.configure.AppConstants;
import root.configure.MybatisCacheConfiguration;
import root.report.db.DbFactory;
import root.report.util.JsonUtil;
import root.report.util.ThreadPoolExecutorUtil;
import root.report.util.XmlUtil;

import javax.websocket.Session;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Service
public class DictService {

    private static Logger log = Logger.getLogger(DictService.class);


    public List<Map<String,String>> getAllDictName(){
        List<Map<String,String>> resultList = new  ArrayList<Map<String,String>>();
        try
        {
            SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
            resultList=sqlSession.selectList("dict.getAllDictName");
            return resultList;

        }catch (Exception ex){

            throw  ex;
        }
    }

    public List<Map<String,String>> getDictValueByID(String dict_id){
        List<Map<String,String>> resultList = new  ArrayList<Map<String,String>>();
        try
        {
            SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
            int dictID = Integer.parseInt(dict_id);
            Map<String,Object> map = new HashMap<>();
            map.put("dict_id",dictID);
            resultList=sqlSession.selectList("dict.getDictValueByID",map);
            return resultList;

        }catch (Exception ex){

            throw  ex;
        }
    }

    /**
     * 功能描述: 根据JSON数据解析 对应数据，生成func_dict记录
     */
    public String createFuncDict(SqlSession sqlSession,JSONObject jsonObject){
        Map<String,Object> map  = new HashMap<>();
        map.put("dict_name",jsonObject.getString("dict_name"));
        map.put("dict_desc",jsonObject.getString("dict_desc"));
        map.put("dict_db",jsonObject.getString("dict_db"));
        String escapeSQL = jsonObject.getString("dict_sql");
        escapeSQL = escapeSQL.replace("'","\\'").replace("{","\\{").replace("}","\\}");
        map.put("dict_sql",escapeSQL);
        map.put("loaddata_mode",jsonObject.getString("loaddata_mode"));
        // map.put("loaddata_state",jsonObject.getString("loaddata_state")); // 默认给0 ： 表示未同步
        map.put("loaddata_state","0");
        sqlSession.insert("dict.createFuncDict",map);
        return String.valueOf(map.get("id"));
    }

    /**
     * 功能描述: 根据JSON数据解析对应数据，生成func_dict_out 记录
     */
    public void createFuncDictOut(SqlSession sqlSession,JSONObject jsonObject,String dict_id){
        Map<String,Object> map  = new HashMap<>();
        map.put("dict_id",dict_id);
        JSONArray jsonArray = jsonObject.getJSONArray("out");
        for(int i=0;i<jsonArray.size();i++){
            JSONObject outJson = jsonArray.getJSONObject(i);
            map.put("out_id",outJson.getString("out_id"));
            map.put("out_name",outJson.getString("out_name"));
            map.put("datatype",outJson.getString("datatype"));
            sqlSession.insert("dict.createFuncDictOut",map);
        }
    }

    // 功能描述: 根据 dict_id 批量删除 func_dict的信息
    public void deleteFuncDict(SqlSession sqlSession,int dict_id){
           sqlSession.delete("dict.deleteFuncDict",dict_id);
    }

    // 功能描述: 根据 dict_id删除了字典值
    public void deleteDictValueByDictID(SqlSession sqlSession,int dict_id){
        sqlSession.delete("dict.deleteDictValueByDictID",dict_id);
    }

    // 功能描述: 根据 dict_id 和 out_id 批量删除 func_dict的信息
    public void deleteFuncDictOut(SqlSession sqlSession,int dict_id){
            Map<String,Object> map=new HashMap();
            map.put("dict_id",dict_id);
            sqlSession.delete("dict.deleteFuncDictOut",map);
    }

    // 功能描述 : 修改 func_dict_out 表的信息
    public void updateFuncDictOut(SqlSession sqlSession,JSONObject jsonObject){
        JSONArray jsonArray = jsonObject.getJSONArray("out");
        Map<String,Object> map  = new HashMap<>();
        map.put("dict_id",jsonObject.getIntValue("dict_id"));
        for(int i=0;i<jsonArray.size();i++){
            JSONObject deleteJson = jsonArray.getJSONObject(i);
            map.put("out_id",deleteJson.getString("out_id"));
            sqlSession.delete("dict.deleteFuncDictOut",map);
        }
        String dict_id = String.valueOf(jsonObject.getIntValue("dict_id"));
        this.createFuncDictOut(sqlSession,jsonObject,dict_id);
    }

    // 功能描述 : 修改 func_dict 表的信息
    public  void updateFuncDict(SqlSession sqlSession,JSONObject jsonObject){
        Map<String,Object> map  = new HashMap<>();
        map.put("dict_id",jsonObject.getIntValue("dict_id"));
        map.put("dict_name",jsonObject.getString("dict_name"));
        map.put("dict_desc",jsonObject.getString("dict_desc"));
        map.put("dict_db",jsonObject.getString("dict_db"));
        String escapeSQL = jsonObject.getString("dict_sql");
        escapeSQL = escapeSQL.replace("'","\\'").replace("{","\\{").replace("}","\\}");
        map.put("dict_sql",escapeSQL);
        map.put("loaddata_mode",jsonObject.getString("loaddata_mode"));
        map.put("loaddata_state",jsonObject.getString("loaddata_state"));
        sqlSession.update("dict.updateFuncDict",map);
    }

    // 功能描述 : 修改 func_dict 表的信息 :主要修改掉 loaddata_state 的状态 1 : 正在导入. 2 导入完毕，3: 导入失败
    public  void updateFuncDictForState(SqlSession sqlSession,int dict_id,String state){
        Map<String,Object>  map =   sqlSession.selectOne("dict.getFuncDictInfoByDictId",dict_id);
        map.put("loaddata_state",state);
        sqlSession.update("dict.updateFuncDict",map);
    }

    // 功能描述 : 根据dict_id 查询 func_dict信息
    public JSONObject getFuncDictInfo(SqlSession sqlSession,int dict_id) throws SAXException, DocumentException {
        Map<String,Object>  map =   sqlSession.selectOne("dict.getFuncDictInfoByDictId",dict_id);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dict_id",map.get("dict_id"));
        jsonObject.put("dict_name",map.get("dict_name"));
        jsonObject.put("dict_desc",map.get("dict_desc"));
        jsonObject.put("dict_db",map.get("dict_db"));
        jsonObject.put("loaddata_mode",map.get("loaddata_mode"));
        jsonObject.put("loaddata_state",map.get("loaddata_state"));
        // jsonObject.put("dict_sql",this.getSqlTemplate("数据字典",String.valueOf(map.get("dict_id")),true));
        jsonObject.put("dict_sql",map.get("dict_sql"));

        List<Map<String,Object>> listMap = sqlSession.selectList("dict.getFuncDictOutInfoByDicId",dict_id);
        jsonObject.put("out",listMap);

        return jsonObject;
    }

    // 根据传递进来的 dict_id 查询 对应的namespace当中的sql并执行得到 结果插入到 func_dict_value表
    public void createFuncDictValueByDictId(SqlSession sqlSession,int dict_id) throws SQLException, SAXException, DocumentException {
        //
        Statement st = null;
        try{
            // dict_id 关联查询到 func_dict 查找dict_db 这个
            String dbName = sqlSession.selectOne("dict.getDictDbByDictId",dict_id);
            if(StringUtils.isBlank(dbName)) throw new RuntimeException("此DictId所对应的数据库为空,无法操作!");

            // 初始化对应的数据库
            SqlSession sourceSqlSession = DbFactory.Open(dbName);
            st = sourceSqlSession.getConnection().createStatement();
            String sql = this.getSqlTemplate("数据字典",String.valueOf(dict_id),false);
            if(StringUtils.isNotBlank(sql)){
                List<Map<String,Object>> list = new ArrayList<>();
                ResultSet rs = st.executeQuery(sql);
                while(rs.next()){
                    String code = rs.getString("code");
                    String name = rs.getString("name");
                    Map<String,Object> tempMap = new HashMap<>();
                    tempMap.put("dict_id",dict_id);
                    tempMap.put("value_code",code);
                    tempMap.put("value_name",name);
                    list.add(tempMap);
                    // System.out.println(id+","+name);
                }
                if(list!=null && !list.isEmpty()){
                    this.createFuncDictValue(sqlSession,list);
                }
            }
        }catch (Exception e){
            throw e;
        }
    }

    // 往 func_dict_value 表中插入记录
    public void createFuncDictValue(SqlSession sqlSession,List<Map<String,Object>> list){
        for(int i=0;i<list.size();i++){
            Map<String,Object> map = list.get(i);
            sqlSession.insert("dict.createFuncDictValue",map);
        }
    }

    // 得到命名空间的SQL
    public String getSqlTemplate(String TemplateName, String SelectID,Boolean bool) throws DocumentException, SAXException {

        String namespace = TemplateName;
        String sqlId = SelectID;
        String userSqlPath = AppConstants.getUserDictionaryPath() + File.separator + namespace + ".xml";

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
            }else {
                tempStr.append(select.getTextTrim());   // 编译了一些html代码，导致不是原格式了，输入无格式的sql
            }
            log.debug("获取到的SQL为:" +tempStr);
            return tempStr.toString();
        } catch (java.lang.Exception e) {
            throw e;
        }
    }
    // 功能描述 : 根据dict_id 查询 func_dict信息
    public String getDictIdByValue(String value_name)  {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        Map<String,Object> map = new HashMap<>();
        map.put("value_name",value_name);

        Map<String,Object> dict = sqlSession.selectOne("dict.getDictIdByValue",map);


        return  dict.get("dict_id").toString();
    }

    // 修改 func_dict_value 表当中的记录
    public void createFuncDictValue2(SqlSession sqlSession,JSONObject jsonObject){
        // 只更新  value_name 即可
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> maps = new HashMap<>();
        map.put("dict_id",jsonObject.getIntValue("dict_id"));
        map.put("value_code",jsonObject.getString("value_code"));
        maps= DbFactory.Open(DbFactory.FORM).selectOne("dict.getDictValueByDictID",map);
        if(null==maps) {
            map.put("value_name", jsonObject.getString("value_name"));
            map.put("value_pid", jsonObject.get("value_pid") == null ? null : jsonObject.getIntValue("value_pid"));
            map.put("abbr_name1", jsonObject.getString("abbr_name1"));
            map.put("abbr_name2", jsonObject.getString("abbr_name2"));
            map.put("attribute1", jsonObject.getString("attribute1"));
            map.put("attribute2", jsonObject.getString("attribute2"));
            map.put("attribute3", jsonObject.getString("attribute3"));
            map.put("attribute4", jsonObject.getString("attribute4"));
            map.put("attribute5", jsonObject.getString("attribute5"));
            map.put("attribute6", jsonObject.getString("attribute6"));
            map.put("attribute7", jsonObject.getString("attribute7"));
            map.put("attribute8", jsonObject.getString("attribute8"));
            sqlSession.insert("dict.createFuncDictValue", map);
        }
    }

    // 修改 func_dict_value 表当中的记录
    public void updateFuncDictValue(SqlSession sqlSession,JSONObject jsonObject){
        // 只更新  value_name 即可
        Map<String,Object> map = new HashMap<>();
        map.put("dict_id",jsonObject.getIntValue("dict_id"));
        map.put("value_code",jsonObject.getString("value_code"));
        map.put("value_name",jsonObject.getString("value_name"));
        map.put("abbr_name1",jsonObject.getString("abbr_name1"));
        map.put("abbr_name2",jsonObject.getString("abbr_name2"));
        sqlSession.update("dict.updateFuncDictValue",map);
    }

    // 删除 func_dict_value 表当中的记录
    public void deleteFuncDictValue(SqlSession sqlSession,JSONArray jsonArray){
        Map<String,Object> map = new HashMap<>();
        for(int i=0;i<jsonArray.size();i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            map.put("dict_id",jsonObject.getIntValue("dict_id"));
            map.put("value_code",jsonObject.getString("value_code"));
            sqlSession.delete("dict.deleteFuncDictValue",map);
        }
    }
    public void deleteFuncDictValueByIDCode(SqlSession sqlSession,JSONObject jsonObj){
        Map<String,Object> map = new HashMap<>();
        map.put("dict_id",jsonObj.getIntValue("dict_id"));
        map.put("value_code",jsonObj.getString("value_code"));
        sqlSession.delete("dict.deleteFuncDictValue",map);

    }

    public void createSqlTemplate(String TemplateName, String SelectID, String aSQLTemplate) throws DocumentException, SAXException, IOException {

        String namespace = TemplateName;
        String sqlId = SelectID;
        String userSqlPath = AppConstants.getUserDictionaryPath() + File.separator + namespace + ".xml";

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

    // 往指定节点当中增加内容
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

    public String updateSqlTemplate(String TemplateName, String SelectID, String aSQLTemplate) throws DocumentException, SAXException, IOException {

        String namespace = TemplateName;
        String sqlId = SelectID;
        String userSqlPath = AppConstants.getUserDictionaryPath() + File.separator + namespace + ".xml";

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
        String userSqlPath = AppConstants.getUserDictionaryPath() + File.separator +namespace + ".xml";

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
                            // value+="\n";   // 历史版本可能字符串里面没有\n 而现在又有了
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

    //  websocekt 版本
    public String importFuncDictValueByDictId(Session session, int dict_id){

        // 先决条件 ： 根据 dict_id 得到 sourceSql
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        String dbName = sqlSession.selectOne("dict.getDictDbByDictId",dict_id);
        if(StringUtils.isBlank(dbName)) return("此DictId所对应的数据库为空,无法操作!");
        final int countRow = 1000;
        final List<Map<String, Object>> list = new ArrayList<>();

        // 初始化对应的数据库
        SqlSession sourceSqlSession = DbFactory.Open(dbName);
        if(sourceSqlSession==null)  return("数据库无法连接,无法操作!");
        String sourceSql = null;
        try {
            sourceSql = this.getSqlTemplate("数据字典",String.valueOf(dict_id),false);
        } catch (Exception e){
            log.info(e.getMessage());
            return "无法从xml文件得到对应的sql,无法查询!";
        }

        Long begin = new Date().getTime();
        Connection conn  = sqlSession.getConnection();
        final int poolSize = 5;
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(poolSize);
        fetchBySql(sourceSqlSession,sourceSql,rs -> {
            try {
                String prefix = "INSERT INTO func_dict_value (dict_id,value_code,value_name) VALUES ";
                final StringBuffer suffix = new StringBuffer();
                // 设置事务为非自动提交
                conn.setAutoCommit(false);   //  非提交能减少日志的生成,从而加快执行速度
                PreparedStatement pst = (PreparedStatement) conn.prepareStatement("");
                List<Map> mapList = new ArrayList<>();
                if (rs != null) {
                    rs.last();      // 移动到最后面
                    BigDecimal result = new BigDecimal((double) rs.getRow()/5000).setScale(0, BigDecimal.ROUND_UP);
                    int countSize = result.intValue();
                    try {
                        session.getBasicRemote().sendText(String.valueOf(countSize));    // 第一次直接发送给websocket 客户端1个 本次执行总数统计.
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    rs.first();  // 移动到最前面   // 坑处1，如果使用了firt的话，那么直接达到了第一条，则不要使用 while(rs.next())
                    if(rs!=null && StringUtils.isNotBlank(rs.getInt("code")+"")){
                        Map<String, Object> map = new HashMap<>();
                        map.put("code",rs.getInt("code"));
                        map.put("name",rs.getString("name"));
                        mapList.add(map);
                    }
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code",rs.getInt("code"));
                        map.put("name",rs.getString("name"));
                        mapList.add(map);
                        if(rs.getRow()%5000==0){
                            //  执行批量插入操作
                            List<Map> finalMapList = mapList;
                            final StringBuffer sb = new StringBuffer();
                            Future<String> stringFuture = fixedThreadPool.submit(
                                    new Callable<String>() {
                                        @Override
                                        public String call() throws Exception {
                                            String name = Thread.currentThread().getName();
                                            long threadId = Thread.currentThread().getId();
                                            log.info("thread name: "+name+",id为"+threadId+"执行了一次");
                                            for (Map tempMap : finalMapList) {
                                                // 构建SQL后缀
                                                sb.append("(" +dict_id+",'"+ tempMap.get("code") + "'," + "'" + tempMap.get("name") + "'),");
                                            }
                                            return "success";
                                        }
                                    }
                            );
                            mapList = new ArrayList<>();
                            try {
                                String s = stringFuture.get();
                                if ("success".equals(s)) {
                                    String sql = prefix + sb.substring(0, sb.length() - 1);  // 构建完整SQL
                                    pst.addBatch(sql);   // 添加执行SQL
                                    pst.executeBatch();  // 执行操作
                                    session.getBasicRemote().sendText("success");
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // 执行完之后 mapList 是个 不足5000个的，这个时候我们再去执行一次 添加操作
                    List<Map> finalMapList = mapList;
                    System.out.println("mapList大小为:"+mapList.size());
                    System.out.println("finalMapList:"+finalMapList.size());
                    final StringBuffer sb = new StringBuffer();
                    Future<String> stringFuture = fixedThreadPool.submit(
                            new Callable<String>() {
                                @Override
                                public String call() throws Exception {
                                    String name = Thread.currentThread().getName();
                                    long threadId = Thread.currentThread().getId();
                                    log.info("thread name: "+name+"id为"+threadId+"执行了一次");
                                    for (Map tempMap : finalMapList) {
                                        // 构建SQL后缀
                                        sb.append("(" +dict_id+",'"+ tempMap.get("code") + "'," + "'" + tempMap.get("name") + "'),");
                                    }
                                    return "success";
                                }
                            }
                    );
                    try {
                        String s = stringFuture.get();
                        if ("success".equals(s)) {
                            String sql = prefix + sb.substring(0, sb.length() - 1);  // 构建完整SQL
                            pst.addBatch(sql);   // 添加执行SQL
                            pst.executeBatch();  // 执行操作
                            session.getBasicRemote().sendText("success");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    conn.commit();
                    pst.close();
                    conn.close();
                }
                Long end = new Date().getTime();
                log.info("条数据从远程库导入到本地花费时间 : " + (end - begin)  + " ms");
            } catch (SQLException e) {
                // e.printStackTrace();  // 批量执行遇到异常直接 用log打印，不要中断
                log.info(e.getMessage());
            }finally {
                fixedThreadPool.shutdown();   // 一定要shutdown  否则线程只是被回收到了线程池
            }
        });
        return "over";
    }

    // 测试使用 fetch 按照指定规格读取数据  ,  源表为 test_dict 目标表名为 test_import
    public void fetchBySql(SqlSession sqlSession,String sourceSql,Consumer<ResultSet> consumer){
        // 通过 conn 得到 真正的连接（要解析 数据库名，从而真正的 数据库连接通道）
        // 而在我们这里则不需要这么复杂，前面过程都由DbFactory 完成了，传递进来的sqlSession 就是一个 连接好的通道
        Connection conn = sqlSession.getConnection();
        PreparedStatement stm = null;
        // 开始时间
        Long begin = new Date().getTime();
        String sql = sourceSql;
        try {
            final int countRow = 1000;
            stm = conn.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            // stm.setFetchSize(Integer.MIN_VALUE);      // 设置游标  1000 每次读取一千行数据
            stm.setFetchSize(countRow);
            stm.setFetchDirection(ResultSet.FETCH_REVERSE);     //
            ResultSet rs = stm.executeQuery();
            // rs.setFetchSize(Integer.MIN_VALUE);
            consumer.accept(rs);    // 把 当前的 rs 传递给消费者
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String importFuncDictValueByDictId(int dict_id){

        // 先决条件 ： 根据 dict_id 得到 sourceSql
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        this.updateFuncDictForState(sqlSession,dict_id,"1");  // 正在导入
        String dbName = sqlSession.selectOne("dict.getDictDbByDictId",dict_id);
        if(StringUtils.isBlank(dbName)) return("此DictId所对应的数据库为空,无法操作!");
        final int countRow = 1000;
        final List<Map<String, Object>> list = new ArrayList<>();

        // 初始化对应的数据库
        SqlSession sourceSqlSession = DbFactory.Open(dbName);
        if(sourceSqlSession==null)  return("数据库无法连接,无法操作!");
        String sourceSql = null;
        try {
            sourceSql = this.getSqlTemplate("数据字典",String.valueOf(dict_id),false);
        } catch (Exception e){
            log.info(e.getMessage());
            return "无法从xml文件得到对应的sql,无法查询!";
        }

        Long begin = new Date().getTime();
        Connection conn  = sqlSession.getConnection();
        final int poolSize = 5;
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(poolSize);
        fetchBySql(sourceSqlSession,sourceSql,rs -> {
            try {
                String prefix = "INSERT INTO func_dict_value (dict_id,value_code,value_name) VALUES ";
                final StringBuffer suffix = new StringBuffer();
                // 设置事务为非自动提交
                conn.setAutoCommit(false);   //  非提交能减少日志的生成,从而加快执行速度
                PreparedStatement pst = (PreparedStatement) conn.prepareStatement("");
                List<Map> mapList = new ArrayList<>();
                if (rs != null) {
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code",rs.getInt("code"));
                        map.put("name",rs.getString("name"));
                        mapList.add(map);
                        if(rs.getRow()%5000==0){
                            //  执行批量插入操作
                            List<Map> finalMapList = mapList;
                            final StringBuffer sb = new StringBuffer();
                            Future<String> stringFuture = fixedThreadPool.submit(
                                    new Callable<String>() {
                                        @Override
                                        public String call() throws Exception {
                                            String name = Thread.currentThread().getName();
                                            long threadId = Thread.currentThread().getId();
                                            log.info("thread name: "+name+",id为"+threadId+"执行了一次");
                                            for (Map tempMap : finalMapList) {
                                                // 构建SQL后缀
                                                sb.append("(" +dict_id+",'"+ tempMap.get("code") + "'," + "'" + tempMap.get("name") + "'),");
                                            }
                                            return "success";
                                        }
                                    }
                            );
                            mapList = new ArrayList<>();
                            try {
                                String s = stringFuture.get();
                                if ("success".equals(s)) {
                                    String sql = prefix + sb.substring(0, sb.length() - 1);  // 构建完整SQL
                                    pst.addBatch(sql);   // 添加执行SQL
                                    pst.executeBatch();  // 执行操作
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // 执行完之后 mapList 是个 不足5000个的，这个时候我们再去执行一次 添加操作
                    List<Map> finalMapList = mapList;
                    log.info("mapList大小为:"+mapList.size());
                    log.info("finalMapList:"+finalMapList.size());
                    final StringBuffer sb = new StringBuffer();
                    Future<String> stringFuture = fixedThreadPool.submit(
                            new Callable<String>() {
                                @Override
                                public String call() throws Exception {
                                    String name = Thread.currentThread().getName();
                                    long threadId = Thread.currentThread().getId();
                                    log.info("thread name: "+name+"id为"+threadId+"执行了一次");
                                    for (Map tempMap : finalMapList) {
                                        // 构建SQL后缀
                                        sb.append("(" +dict_id+",'"+ tempMap.get("code") + "'," + "'" + tempMap.get("name") + "'),");
                                    }
                                    return "success";
                                }
                            }
                    );
                    try {
                        String s = stringFuture.get();
                        if ("success".equals(s)) {
                            String sql = prefix + sb.substring(0, sb.length() - 1);  // 构建完整SQL
                            pst.addBatch(sql);   // 添加执行SQL
                            pst.executeBatch();  // 执行操作
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    conn.commit();
                    pst.close();
                }
                Long end = new Date().getTime();
                log.info("条数据从远程库导入到本地花费时间 : " + (end - begin)  + " ms");
            } catch (SQLException e) {
                // e.printStackTrace();  // 批量执行遇到异常直接 用log打印，不要中断
                log.info(e.getMessage());
            }finally {
                fixedThreadPool.shutdown();   // 一定要shutdown  否则线程只是被回收到了线程池
            }
        });
        return "1";
    }

    // 第三版 改写成使用ThreadPoolExecutor  完成线程调用
    public String importFuncDictValue(int dict_id){
        // 先决条件 ： 根据 dict_id 得到 sourceSql
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        this.updateFuncDictForState(sqlSession,dict_id,"1");  // 正在导入
        String dbName = sqlSession.selectOne("dict.getDictDbByDictId",dict_id);
        if(StringUtils.isBlank(dbName)) return("此DictId所对应的数据库为空,无法操作!");
        // 初始化对应的数据库
        SqlSession sourceSqlSession = DbFactory.Open(dbName);
        if(sourceSqlSession==null)  return("数据库无法连接,无法操作!");
        String sourceSql = null;
        try {
            sourceSql = this.getSqlTemplate("数据字典",String.valueOf(dict_id),false);
        } catch (Exception e){
            log.info(e.getMessage());
            return "无法从xml文件得到对应的sql,无法查询!";
        }
        Long begin = new Date().getTime();
        Connection conn  = sqlSession.getConnection();
        try {
            conn.setAutoCommit(false);   //  非提交能减少日志的生成,从而加快执行速度
            PreparedStatement pst = (PreparedStatement) conn.prepareStatement("");
            fetchBySql(sourceSqlSession,sourceSql,rs -> {
                String prefix = "INSERT INTO func_dict_value (dict_id,value_code,value_name) VALUES ";
                // String prefix = "INSERT INTO test_import (dict_id,code,name) VALUES ";
                List<Map> mapList = new ArrayList<>();
                try {
                    if (rs != null) {
                        while (rs.next()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("code", rs.getString("value_code"));
                            map.put("name", rs.getString("value_name"));
                            mapList.add(map);
                            if (rs.getRow() % 5000 == 0) {
                                //  执行批量插入操作
                                List<Map> finalMapList = mapList;
                                // 也可以使用 getInstance().execute  ,但是使用submit 可以返回 Future ,可以为后面的 websocket服务
                                ThreadPoolExecutorUtil.getInstance().submit(new DictInsertTask(finalMapList, String.valueOf(dict_id), pst, prefix));
                                mapList = new ArrayList<>();
                            }
                        }
                        // 执行完之后 mapList 是个 不足5000个的，这个时候我们再去执行一次 添加操作
                        List<Map> finalMapList = mapList;
                        log.info("mapList大小为:" + mapList.size());
                        log.info("finalMapList:" + finalMapList.size());
                        ThreadPoolExecutorUtil.getInstance().submit(new DictInsertTask(finalMapList, String.valueOf(dict_id), pst, prefix));
                        conn.commit();
                    }
                    Long end = new Date().getTime();
                    log.info("条数据从远程库导入到本地花费时间 : " + (end - begin) + " ms");
                } catch (SQLException e) {
                    log.info(e.getMessage());   // 传输过程出错-》不处理，让其继续导入，因为可能是导入了重复的数据
                }
            });
            if( pst!=null && !pst.isClosed()){
                pst.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "1";
    }
    public Map getDictValueList(String pJson){
        Map<String,Object> map3 =new HashMap<String,Object>();
        try {
            JSONObject obj = (JSONObject) JSON.parse(pJson);
            Map<String,Object> map = new HashMap<String,Object>();
            Long total = 0L;
            RowBounds bounds = null;
            if(obj==null){
                bounds = RowBounds.DEFAULT;
            }else {
                int currentPage = Integer.valueOf(obj.getIntValue("pageNum"));
                int perPage = Integer.valueOf(obj.getIntValue("perPage"));
                if (1 == currentPage || 0 == currentPage) {
                    currentPage = 0;
                } else {
                    currentPage = (currentPage - 1) * perPage;
                }
                bounds = new PageRowBounds(currentPage, perPage);
            }
            map.put("dict_id",obj.get("dictId"));
            map.put("value_name",  obj.get("value_name")==null?"":obj.getString("value_name"));
            List<Map<String,Object>> list = DbFactory.Open(DbFactory.FORM).selectList("dict.getDictValueByID",map,bounds);
            if(obj!=null){
                total = ((PageRowBounds)bounds).getTotal();
            }else{
                total = Long.valueOf(list.size());
            }

            map3.put("list",list);
            map3.put("total",total);
            return map3;
        }catch (Exception ex){
            ex.printStackTrace();
            return map3;
        }
    }

    public Map getDictValueByDictID(Map m) {
        return DbFactory.Open(DbFactory.FORM).selectOne("dict.getDictValueByDictID",m);
    }
}
