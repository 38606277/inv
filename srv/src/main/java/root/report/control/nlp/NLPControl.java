package root.report.control.nlp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.db.DbManager;
import root.report.service.NLPService;
import root.report.util.ErpUtil;

import java.sql.*;
import java.util.*;


@RestController
@RequestMapping("/reportServer/nlp")
public class NLPControl extends RO {
    private static final Logger log = Logger.getLogger(NLPControl.class);
    DbManager dbManager=new DbManager();

    @Autowired
    NLPService nlpService;
    private static ErpUtil erpUtil = new ErpUtil();

    @RequestMapping(value = "/getResult/{aText}", produces = "text/plain;charset=UTF-8")
    public String GetResult(@PathVariable("aText") String aText) {

//        CustomDictionary.add("供应商信息");
//        CoNLLSentence coNLLSentence= HanLP.parseDependency("查询华为的供应商信息");
//        System.out.println(coNLLSentence);
//        return  SuccessMsg("",new HashMap<Object,String>());

        try{

           Map map=nlpService.ExecNLP(aText);
           return  SuccessMsg("",map);

        }catch(Exception ex) {
            return  ExceptionMsg(ex.getMessage());
        }

    }

    @RequestMapping(value = "/getTable", produces = "text/plain;charset=UTF-8")
    public String getTable(@RequestBody String dbname) throws SQLException,Exception {
        JSONObject obj = JSON.parseObject(dbManager.getDBConnectionByName(dbname));
        String dbtype = obj.getString("dbtype");
        List<String> tableList = new ArrayList<String>();
        Connection conn=null;
        if(dbtype.equals("Oracle")){
            try {
                 conn= dbManager.getConnection(dbname);
                 tableList = this.getTableNameList(conn,dbname);
            } catch (SQLException e) {
                if(null!=conn) {
                    conn.close();
                }
                e.printStackTrace();
            }finally {
                if(null!=conn) {
                    conn.close();
                }
            }
        }else if(dbtype.equals("Mysql")){
            try {
                conn = dbManager.getConnection(dbname);
               // DatabaseMetaData dbMetaData = conn.getMetaData();
                Statement stmt = conn.createStatement();
                stmt.executeQuery("use "+dbname);
                ResultSet rs =  stmt.executeQuery("SHOW TABLES ");
                if (null != rs) {
                    while (rs.next()) {
                        tableList.add(rs.getString(1));
                    }
                }
                if (null != conn) {
                    conn.close();
                }
            }catch (Exception e){
                if (null != conn) {
                    conn.close();
                }
                e.printStackTrace();
            }finally {
                if (null != conn) {
                    conn.close();
                }
            }
        }
        return SuccessMsg("修改数据成功",tableList);
    }

    @RequestMapping(value = "/getAll", produces = "text/plain;charset=UTF-8")
    public String getAll(@RequestBody String pJson) {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
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
                map.put("table_name", obj.get("dbname") == null ?null : obj.getString("dbname").trim());
            }
            List<Map> mapList = sqlSession.selectList("nlp.getAll",map,bounds);
            if(obj!=null){
                total = ((PageRowBounds)bounds).getTotal();
            }else{
                total = Long.valueOf(mapList.size());
            }
            Map<String,Object> map3 =new HashMap<String,Object>();
            map3.put("list",mapList);
            map3.put("total",total);
            return SuccessMsg("",map3);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }
    @RequestMapping(value = "/getColumnList", produces = "text/plain;charset=UTF-8")
    public String getColumnList(@RequestBody String pjson) throws Exception,SQLException{
        JSONObject obj = JSON.parseObject(pjson);
        String dbname = obj.getString("dbname");
        JSONObject objtwo = JSON.parseObject(dbManager.getDBConnectionByName(dbname));
        String dbtype = objtwo.getString("dbtype");
        String tableName = obj.getString("tableName");
        List<HashMap<String,Object>> ColumnList = new ArrayList<HashMap<String,Object>>();
        Connection conn=null;
        SqlSession session= DbFactory.Open(DbFactory.FORM);
        Map param=new HashMap<>();
        List<Map<String, Object>> listOld = new ArrayList<Map<String, Object>>();
        param.put("table_db",dbname);
        param.put("table_name",tableName);
        //查询已经保存的值
        Map mm= session.selectOne("nlp.selectQryTable",param);
        if(null!=mm){
            listOld=session.selectList("nlp.getqryTableFiled",mm);
        }
        if(dbtype.equals("Oracle")){
            try {
                conn= dbManager.getConnection(dbname);
//                ColumnList = this.getColumnNameList(conn,dbname,tableName);
                    Statement stmt = conn.createStatement();
                    String sql=
                            "select "+
                                    "         comments as \"COMMENTS\","+
                                    "         a.column_name \"COLUMN_NAME\","+
                                    "         a.DATA_TYPE as \"DATA_TYPE\","+
                                    "        DECODE (a.data_precision, null,DECODE (a.data_type, 'CHAR', a.char_length,'VARCHAR'," +
                                    "       a.char_length, 'VARCHAR2', a.char_length, 'NVARCHAR2', a.char_length, 'NCHAR', a.char_length,a.data_length),a.data_precision)\n" +
                                    "              AS COLUMN_SIZE,"+
                                    "         decode(c.column_name,null,'FALSE','TRUE') as \"PRIMARY\","+
                                    "         decode(a.NULLABLE,'N','NO','Y','YES','') as \"NULLABLE\""+
                                    "   from "+
                                    "       all_tab_columns a, "+
                                    "       all_col_comments b,"+
                                    "       ("+
                                    "        select a.constraint_name, a.column_name"+
                                    "          from user_cons_columns a, user_constraints b"+
                                    "         where a.constraint_name = b.constraint_name"+
                                    "               and b.constraint_type = 'P'"+
                                    "               and a.table_name = '"+tableName+"'"+
                                    "       ) c"+
                                    "   where "+
                                    "     a.Table_Name=b.table_Name "+
                                    "     and a.column_name=b.column_name"+
                                    "     and a.Table_Name='"+tableName+"'"+
                                    "     and a.owner=b.owner "+
                                    "     and a.owner='"+dbname.toUpperCase()+"'"+
                                    "     and a.COLUMN_NAME = c.column_name(+)" +
                                    "  order by a.COLUMN_ID";
                    System.out.println(sql);
                    ResultSet rs = stmt.executeQuery(sql);
                    if(rs != null) {
                        while (rs.next()) {
                            HashMap<String, Object> map = new HashMap<String, Object>();
                            map.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                            map.put("DATA_TYPE", rs.getString("DATA_TYPE"));
                            map.put("COLUMN_SIZE", rs.getString("COLUMN_SIZE"));
                            map.put("COMMENTS", rs.getString("COMMENTS"));
                            map.put("PRIMARY", rs.getString("PRIMARY"));
                            map.put("NULLABLE", rs.getString("NULLABLE"));
                            if(null!=listOld&& listOld.size()>0){
                                for (int j=0;j<listOld.size();j++) {
                                    String se = listOld.get(j).get("field_name").toString();
                                    if (se.equals(rs.getString("COLUMN_NAME"))) {
                                        map.put("FIELD_NLP1", listOld.get(j).get("field_nlp1"));
                                        map.put("FIELD_NLP2", listOld.get(j).get("field_nlp2"));
                                        map.put("FIELD_NLP3", listOld.get(j).get("field_nlp3"));
                                        map.put("FIELD_NLP4", listOld.get(j).get("field_nlp4"));
                                    }
                                }
                            }else{
                                map.put("FIELD_NLP1", null);
                                map.put("FIELD_NLP2", null);
                                map.put("FIELD_NLP3", null);
                                map.put("FIELD_NLP4", null);
                            }
                            ColumnList.add(map);
                        }
                    }
            } catch (SQLException e) {
                if(null!=conn) {
                    conn.close();
                }
                e.printStackTrace();
            }finally {
                if(null!=conn) {
                    conn.close();
                }
            }
        }else if(dbtype.equals("Mysql")){
            String sql = "select * from " + tableName;

            try {
                conn= dbManager.getConnection(dbname);
                DatabaseMetaData dbmd  = conn.getMetaData();
                ResultSet primaryKeyResultSet = dbmd.getPrimaryKeys(dbname,null,tableName);
                List plist=new ArrayList<>();
                while(primaryKeyResultSet.next()){
                    String primaryKeyColumnName = primaryKeyResultSet.getString("COLUMN_NAME");
                    plist.add(primaryKeyColumnName);
                    System.out.print("数据库名称:" + primaryKeyColumnName);
                }

                ResultSet colRet = dbmd.getColumns(null,"%", tableName,"%");

                while(colRet.next()) {
                    String pk="FALSE";
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("COLUMN_NAME", colRet.getString("COLUMN_NAME"));
                    map.put("DATA_TYPE", colRet.getString("TYPE_NAME"));
                    map.put("COLUMN_SIZE", colRet.getInt("COLUMN_SIZE"));
                    map.put("COMMENTS", colRet.getString("REMARKS"));
                    map.put("PRIMARY", "FALSE");
                    if(plist.size()>0) {
                        for (int i=0;i<plist.size();i++){
                            if(plist.get(i).equals(colRet.getString("COLUMN_NAME"))){
                                pk="TRUE";
                            }
                        }
                    }
                    map.put("PRIMARY",pk);
                    map.put("NULLABLE", colRet.getString("IS_NULLABLE"));
                    if(null!=listOld&& listOld.size()>0){
                        for (int j=0;j<listOld.size();j++) {
                            String se = listOld.get(j).get("field_name").toString();
                            if (se.equals(colRet.getString("COLUMN_NAME"))) {
                                map.put("FIELD_NLP1", listOld.get(j).get("field_nlp1"));
                                map.put("FIELD_NLP2", listOld.get(j).get("field_nlp2"));
                                map.put("FIELD_NLP3", listOld.get(j).get("field_nlp3"));
                                map.put("FIELD_NLP4", listOld.get(j).get("field_nlp4"));
                            }
                        }
                    }else{
                            map.put("FIELD_NLP1", null);
                            map.put("FIELD_NLP2", null);
                            map.put("FIELD_NLP3", null);
                            map.put("FIELD_NLP4", null);
                     }
                    ColumnList.add(map);
                    }

//                Statement stmt = conn.createStatement();
//                ResultSet rsComments = stmt.executeQuery("show full columns from " + tableName);
//                while (rsComments.next()) {
//                    HashMap<String, Object> map = new HashMap<String, Object>();
//                    map.put("COLUMN_NAME", rsComments.getString("Field"));
//                    String type= rsComments.getString("Type");
//
//                    int fi= type.indexOf("(");
//                    type= type.replace("unsigned zerofill","");
//                    String oldtype=type.trim();
//                    String types =null;
//                    String columnlength =null;
//                    if(fi>-1) {
//                        types=type.substring(0, fi);
//                        columnlength = oldtype.substring(fi + 1, oldtype.length()-1);
//                    }else{
//                        types=type;
//                        columnlength=null;
//                    }
//                    map.put("DATA_TYPE",types);
//                    map.put("COLUMN_SIZE",columnlength);
//                    map.put("COMMENTS",rsComments.getString("Comment"));
//                    map.put("PRIMARY",rsComments.getString("Key").equals("PRI")?"TRUE":"FALSE");
//                    map.put("NULLABLE",rsComments.getObject("Null"));
//                    map.put("FIELD_NLP1",null);
//                    map.put("FIELD_NLP2",null);
//                    map.put("FIELD_NLP3",null);
//                    map.put("FIELD_NLP4",null);
//                    ColumnList.add(map);
//                }

//                PreparedStatement ps = conn.prepareStatement(sql);
//                ResultSet rs = ps.executeQuery();
//                ResultSetMetaData meta = rs.getMetaData();
//                int columeCount = meta.getColumnCount();
//                log.info(sql);
//                for (int i = 1; i < columeCount + 1; i++) {
//                   HashMap<String, Object> map = new HashMap<String, Object>();
//                    map.put("COLUMN_NAME", meta.getColumnName(i));
//                    map.put("DATA_TYPE", meta.getColumnTypeName(i));
//                    map.put("COLUMN_SIZE",meta.getScale(i));
//                    map.put("COMMENTS","");
//                    map.put("PRIMARY","");
//                    map.put("NULLABLE", "");
//                    map.put("FIELD_NLP1",null);
//                    map.put("FIELD_NLP2",null);
//                    map.put("FIELD_NLP3",null);
//                    map.put("FIELD_NLP4",null);
//                    ColumnList.add(map);
//                }
            }catch (Exception e){
                if(null!=conn) {
                    conn.close();
                }
                e.printStackTrace();
            }finally {
                if(null!=conn) {
                    conn.close();
                }
            }
        }
        return SuccessMsg("修改数据成功",ColumnList);
    }
    public List getTableNameList(Connection conn,String dbName) throws SQLException {
        DatabaseMetaData dbmd = conn.getMetaData();
        //访问当前用户ANDATABASE下的所有表
        ResultSet rs = dbmd.getTables("null", dbName.toUpperCase(), "%", new String[] { "TABLE" });
        List tableNameList = new ArrayList();
        while (rs.next()) {
            tableNameList.add(rs.getString("TABLE_NAME"));
        }
        return tableNameList;
    }
    // 获取数据表中所有列的列名，并添加到列表结构中。
    public List getColumnNameList(Connection conn, String dbname,String tableName)throws SQLException {
        List<HashMap<String,String>> columns = new ArrayList<HashMap<String,String>>();
        try{
            Statement stmt = conn.createStatement();
            String sql=
                    "select "+
                            "         comments as \"COMMENTS\","+
                            "         '' as \"FIELD_NLP1\","+
                            "          '' as \"FIELD_NLP2\","+
                            "          '' as \"FIELD_NLP3\","+
                            "          '' as \"FIELD_NLP4\","+
                            "         a.column_name \"COLUMN_NAME\","+
                            "         a.DATA_TYPE as \"DATA_TYPE\","+
                            "        DECODE (a.data_precision, null,DECODE (a.data_type, 'CHAR', a.char_length,'VARCHAR'," +
                            "       a.char_length, 'VARCHAR2', a.char_length, 'NVARCHAR2', a.char_length, 'NCHAR', a.char_length,a.data_length),a.data_precision)\n" +
                            "              AS COLUMN_SIZE,"+
                            "         decode(c.column_name,null,'FALSE','TRUE') as \"PRIMARY\","+
                            "         decode(a.NULLABLE,'N','NO','Y','YES','') as \"NULLABLE\""+
                            "   from "+
                            "       all_tab_columns a, "+
                            "       all_col_comments b,"+
                            "       ("+
                            "        select a.constraint_name, a.column_name"+
                            "          from user_cons_columns a, user_constraints b"+
                            "         where a.constraint_name = b.constraint_name"+
                            "               and b.constraint_type = 'P'"+
                            "               and a.table_name = '"+tableName+"'"+
                            "       ) c"+
                            "   where "+
                            "     a.Table_Name=b.table_Name "+
                            "     and a.column_name=b.column_name"+
                            "     and a.Table_Name='"+tableName+"'"+
                            "     and a.owner=b.owner "+
                            "     and a.owner='"+dbname.toUpperCase()+"'"+
                            "     and a.COLUMN_NAME = c.column_name(+)" +
                            "  order by a.COLUMN_ID";
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);
            if(rs != null) {
                while (rs.next()) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                    map.put("DATA_TYPE", rs.getString("DATA_TYPE"));
                    map.put("COLUMN_SIZE", rs.getString("COLUMN_SIZE"));
                    map.put("COMMENTS", rs.getString("COMMENTS"));
                    map.put("PRIMARY", rs.getString("PRIMARY"));
                    map.put("NULLABLE", rs.getString("NULLABLE"));
                    columns.add(map);
                }
            }
        }
        catch (SQLException e){
            conn.close();
            e.printStackTrace();
        }finally{
            conn.close();
        }
        // DatabaseMetaData dbmd = conn.getMetaData();
        //ResultSet rs = dbmd.getColumns(null, "%", tableName, "%");
//        List<Map<String,Object>> columnNameList = new ArrayList();
//        while (rs.next()) {
//            Map<String,Object> m=new HashMap<>();
//            m.put("COLUMN_NAME",rs.getString("COLUMN_NAME"));
//            m.put("DATA_TYPE",rs.getString("TYPE_NAME"));
//            m.put("COLUMN_SIZE",rs.getObject("COLUMN_SIZE"));
////            m.put("DATA_TYPE",rs.getString("DATA_TYPE"));
//            m.put("NULLABLE",rs.getObject("IS_NULLABLE"));
//            m.put("DATA_DEFAULT",rs.getObject("COLUMN_DEF"));
////            m.put("ORDINAL_POSITION",rs.getObject("ORDINAL_POSITION"));
//
////            m.put("BUFFER_LENGTH",rs.getObject("BUFFER_LENGTH")==null?null:rs.getString("BUFFER_LENGTH"));
////            m.put("DECIMAL_DIGITS",rs.getString("DECIMAL_DIGITS"));
//            m.put("REMARKS",rs.getObject("REMARKS"));
//            columnNameList.add(m);
//        }
        return columns;
    }
    @RequestMapping(value = "/updateColumn", produces = "text/plain;charset=UTF-8")
    public String updateColumn(@RequestBody String pjson) throws Exception {
        JSONObject obj = JSON.parseObject(pjson);
        String dbname = obj.getString("dbname");
//        JSONObject objtwo = JSON.parseObject(dbManager.getDBConnectionByName(dbname));
//        String dbtype = objtwo.getString("dbtype");
        String tableName = obj.getString("tableName");
        JSONArray tableList =obj.getJSONArray("columnList");
        boolean istrue=true;
        SqlSession session= DbFactory.Open(DbFactory.FORM);
        Map param=new HashMap<>();
        try {
            param.put("table_db",dbname);
            param.put("table_name",tableName);
            //先删除再保存
            Map mm= session.selectOne("nlp.selectQryTable",param);
            session.delete("nlp.deleteQryTableFiled", mm);
            session.delete("nlp.deleteQryTableID",  mm);
            param.put("table_nlp1",obj.get("table_nlp1")==null?null:obj.getString("table_nlp1"));
            param.put("table_nlp2",obj.get("table_nlp2")==null?null:obj.getString("table_nlp2"));
            param.put("table_nlp3",obj.get("table_nlp3")==null?null:obj.getString("table_nlp3"));
            param.put("table_nlp4",obj.get("table_nlp4")==null?null:obj.getString("table_nlp4"));
            session.insert("nlp.createQueryTable", param);
            String table_id= param.get("table_id").toString();
                for (int i = 0; i < tableList.size(); i++) {
                    Map m = (Map) tableList.get(i);
                    Map paramt = new HashMap<>();
                    paramt.put("table_id", table_id);
                    paramt.put("field_name", m.get("COLUMN_NAME"));
                    paramt.put("field_nlp1", m.get("FIELD_NLP1"));
                    paramt.put("field_nlp2",  m.get("FIELD_NLP2"));
                    paramt.put("field_nlp3",  m.get("FIELD_NLP3"));
                    paramt.put("field_nlp4",  m.get("FIELD_NLP4"));
                    paramt.put("type", m.get("DATA_TYPE"));
                    paramt.put("length", m.get("COLUMN_SIZE"));
                    paramt.put("not_null", m.get("NULLABLE"));
                    paramt.put("key",  m.get("PRIMARY").equals("TRUE")?1:0);
                    paramt.put("reference_table_id", table_id);
                    paramt.put("reference_fields",  m.get("COLUMN_NAME"));
                    session.insert("nlp.createQueryTableField", paramt);
                }

        } catch (Exception e) {
                istrue=false;
                if(null!=session) {
                 //   session.close();
                }
                e.printStackTrace();
            }finally {
                if(null!=session) {
                   // session.close();
                }
            }


//        if(dbtype.equals("Oracle")){
//            Connection conn=null;
//            try {
//                conn= dbManager.getConnection(dbname);
//                Statement stmt = conn.createStatement();
//                for (int i=0;i<tableList.size();i++){
//                    Map m= (Map) tableList.get(i);
//                    String sql = "comment on column " + tableName + "." + m.get("COLUMN_NAME")+ " is '" + m.get("COMMENTS") + "'";
//                    stmt.execute(sql);
//                }
//            } catch (ClassNotFoundException e) {
//                istrue=false;
//                if(null!=conn) {
//                    conn.close();
//                }
//                e.printStackTrace();
//            }finally {
//                if(null!=conn) {
//                    conn.close();
//                }
//            }
//        }
        return SuccessMsg("修改数据成功",istrue);
    }
    @RequestMapping(value = "/deleteTC", produces = "text/plain;charset=UTF-8")
    public String deleteTC(@RequestBody String pjson) throws Exception {
        JSONObject obj = JSON.parseObject(pjson);
        String tid = obj.getString("tid");
        boolean istrue=true;
        SqlSession session= DbFactory.Open(DbFactory.FORM);
        Map param=new HashMap<>();
        try {
            param.put("table_id",tid);
            session.delete("nlp.deleteQryTableFiled", param);
            session.delete("nlp.deleteQryTableID",  param);

        } catch (Exception e) {
            istrue=false;
            if(null!=session) {
                //   session.close();
            }
            e.printStackTrace();
        }finally {
            if(null!=session) {
                // session.close();
            }
        }
        return SuccessMsg("删除数据成功",istrue);
    }
    @RequestMapping(value = "/getInfoByTableId", produces = "text/plain;charset=UTF-8")
    public String getInfoByTableId(@RequestBody String pjson) throws Exception {
        JSONObject objson = JSON.parseObject(pjson);
        String table_id = objson.getString("table_id");
        SqlSession session= DbFactory.Open(DbFactory.FORM);
        Map param=new HashMap<>();
        param.put("table_id",table_id);
        Map m=session.selectOne("nlp.getqryTable",param);
        String dbname=m.get("table_db").toString();
        JSONObject obj = JSON.parseObject(dbManager.getDBConnectionByName(dbname));
        String dbtype = obj.getString("dbtype");
        List<String> tableList = new ArrayList<String>();
        Connection conn=null;
        if(dbtype.equals("Oracle")){
            try {
                conn= dbManager.getConnection(dbname);
                tableList = this.getTableNameList(conn,dbname);
            } catch (SQLException e) {
                if(null!=conn) {
                    conn.close();
                }
                e.printStackTrace();
            }finally {
                if(null!=conn) {
                    conn.close();
                }
            }
        }else if(dbtype.equals("Mysql")){
            try {
                conn = dbManager.getConnection(dbname);
                // DatabaseMetaData dbMetaData = conn.getMetaData();
                Statement stmt = conn.createStatement();
                stmt.executeQuery("use "+dbname);
                ResultSet rs =  stmt.executeQuery("SHOW TABLES ");
                if (null != rs) {
                    while (rs.next()) {
                        tableList.add(rs.getString(1));
                    }
                }
                if (null != conn) {
                    conn.close();
                }
            }catch (Exception e){
                if (null != conn) {
                    conn.close();
                }
                e.printStackTrace();
            }finally {
                if (null != conn) {
                    conn.close();
                }
            }
        }
        Map ms=new HashMap<>();
        ms.put("db",m);
        ms.put("tableList",tableList);
        return SuccessMsg("删除数据成功",ms);
    }
}
