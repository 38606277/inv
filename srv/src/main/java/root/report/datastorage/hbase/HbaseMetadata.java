package root.report.datastorage.hbase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.leeutils.DbManagerHutool;
import root.report.leeutils.IDUtil;
import root.report.util.ErpUtil;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;



@RestController
@RequestMapping("/reportServer/HbaseMetadata")
public class HbaseMetadata extends RO
{
    private static final Logger log = Logger.getLogger(HbaseMetadata.class);

    private static String sql="";
    private static ResultSet res;

    private static Connection conn;
    private static Statement stmt;
    private static ResultSet rs = null;
//    public void init() throws ClassNotFoundException, SQLException {
//        Class.forName(driverName);
//        conn= DriverManager.getConnection(url,user,password);
//        stmt=conn.createStatement();
//
//    }
//    public void init(String url,String user,String password) throws ClassNotFoundException, SQLException {
//        Connection con =   getConnection(url,user,password);
//        stmt=con.createStatement();
//    }


    public void initV2(String url,String user,String password) throws ClassNotFoundException, SQLException {
        conn =   getConnection(url,user,password);
        stmt=conn.createStatement();
    }



    // 查询所有数据库
//    @RequestMapping(value = "/getHbaseTables", produces = "text/plain;charset=UTF-8")
    public List<String> getHbaseTables(@RequestBody JSONObject pJson) throws  ClassNotFoundException, SQLException {
        String url=pJson.getString("url");
        String user=pJson.getString("user");
        String password=pJson.getString("password");
        initV2(url,user,password);
        List<String> tableNameList= new ArrayList<>();
        DatabaseMetaData connMetaData = conn.getMetaData();
        String[] type = {"TABLE","VIEW"};
        ResultSet rs = connMetaData.getTables(null, null, null, type);
        boolean testFlag = false;
        while (rs.next()){
            String tableName=rs.getString("TABLE_NAME");
//            if(tt.equals("TEST")){
//                testFlag = true;
//            }
//            String tp=rs.getString("TABLE_TYPE");
//            System.out.println(" 表的名称 "+tt+"   表的类型 "+tp);
            tableNameList.add(tableName);
        }

        return tableNameList;
    }

//    @RequestMapping(value = "/getHbaseTablesV2", produces = "text/plain;charset=UTF-8")
    public List<String> getHbaseTablesV2(String url ,String user,String password) throws  ClassNotFoundException, SQLException {
//        String databaseName=pJson.getString("databaseName");
        initV2(url,user,password);
        List<String> tableNameList= new ArrayList<>();
        DatabaseMetaData connMetaData = conn.getMetaData();
        String[] type = {"TABLE","VIEW"};
        ResultSet rs = connMetaData.getTables(null, null, null, type);

        while (rs.next()){
            String tableName=rs.getString("TABLE_NAME");
            tableNameList.add(tableName);
        }
        return tableNameList;
    }

    //创建表
    @RequestMapping(value = "/createTable", produces = "text/plain;charset=UTF-8")
    public void createTable() throws SQLException {
        sql= "create table emp(id int,name string,age int) row format delimited fields terminated by ','";
        System.out.println("Running:"+sql);
        stmt.execute(sql);
    }


    //查看表结构
    @RequestMapping(value = "/descTable", produces = "text/plain;charset=UTF-8")
    public void descTable() throws SQLException {
        sql ="desc student";
        System.out.println("Running:"+sql);
        res = stmt.executeQuery(sql);
        while (res.next()){
            System.out.println(res.getString(1)+"\t"+res.getString(2));
        }
    }

/*    // 删除数据库表
    @RequestMapping(value = "/dropTable", produces = "text/plain;charset=UTF-8")
    public void dropTable() throws Exception {
        String sql = "drop table if exists emp";
        System.out.println("Running: " + sql);
        stmt.execute(sql);
    }*/

    // 释放资源
    @RequestMapping(value = "/destory", produces = "text/plain;charset=UTF-8")
    public void destory() throws Exception {
        if ( res != null) {
            res.close();
        }
        if (stmt != null) {
            stmt.close();
        }
        if (conn != null) {
            conn.close();
        }
    }

    @RequestMapping(value = "/getDatabaseList", produces = "text/plain;charset=UTF-8")
    public @ResponseBody String getDatabaseList(@RequestBody JSONObject pJson) {
        List<Map<String, Object>> list = new ArrayList<>();
        try{
            String jdbcurl = pJson.getString("jdbcurl");
            String username = pJson.getString("username");
            String password = pJson.getString("password");

            List<String> databaseList = new ArrayList<>();
            databaseList=DbManagerHutool.getDatabases(jdbcurl,username,password);

            for(int i=0;i<databaseList.size();i++){
                String databaseName=databaseList.get(i);
                List<String> tableNameList=new ArrayList<>();


            }
            JSONObject msg = new JSONObject();
            return SuccessMsg("查询成功", list);
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }



    /**
     * 创建Phoenix连接
     * @param ip zookeeper地址
     * @param hadoopCommonUrl HadoopCommon的url
     * @param schema 查询的Schema前缀（没有可为空字符串）
     * @return 成功后的连接信息
     * @throws ClassNotFoundException E
     * @throws SQLException E
     */
    public Connection connectionPhoenix(String ip, String hadoopCommonUrl, String schema) throws ClassNotFoundException, SQLException {
        System.setProperty("hadoop.home.dir", hadoopCommonUrl);
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        Connection conn = DriverManager.getConnection("jdbc:phoenix:" + "172.17.5.132:2181,172.17.5.133:2181,172.17.5.166:2181");
        if (!StringUtils.isEmpty(schema)) {
            conn.setSchema(schema);
        }
        return conn;
    }

    /**
     * 发送SQL语句查询信息
     * @param conn 创建好的Phoenix连接
     * @param schema 要查询的所属Schema（如果有前缀就就加，没有就不加）
     * @param sql 要发送的SQL语句（select * from tableName）
     * @return 查询后的结果
     * @throws SQLException E
     */
    public ResultSet sqlPhoenix(Connection conn, String schema, String sql) throws SQLException {
        conn.setSchema(schema);
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    public  Connection getConnection() {
        try {

            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
            return DriverManager.getConnection("jdbc:phoenix"+"172.17.5.132:2181,172.17.5.133:2181,172.17.5.166:2181");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public  Connection getConnection(String url,String user,String password) {
        try {
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
//            conn= DbFactory.Open("hbase").getConnection();
            conn= DriverManager.getConnection( url, user, password);
            return conn;
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }



    public List<String> getTablesList(String url,String user,String password) throws SQLException {
        Statement stmt = null;
        ResultSet rset = null;
        try {
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection con =  DriverManager.getConnection(url,user,password);
        stmt = con.createStatement();

        DatabaseMetaData connMetaData = con.getMetaData();
        String[] type = {"TABLE","VIEW"};
        String[] type2 = {"VIEW"};
        ResultSet rs = connMetaData.getTables(null, null, null, type);
//        ResultSet rs2 = connMetaData.getTables(null, null, null, type2);
        boolean testFlag = false;

        List<String> databaseList = new ArrayList<>();
        while (rs.next()){
            String tt=rs.getString("TABLE_NAME");

            String tp=rs.getString("TABLE_TYPE");
            System.out.println(" 表的名称 "+tt+"   表的类型 "+tp);
            databaseList.add(tt);
        }

//        PreparedStatement statement = con.prepareStatement("select * from testlee1022");
//        rset = statement.executeQuery();
//        while (rset.next()) {
//            System.out.println(rset.getString("mycolumn"));
//        }
//        statement.close();
        stmt.close();
        con.close();
        return databaseList;
    }

    @RequestMapping(value = "/synchronizeHbaseMetadata", produces = "text/plain;charset=UTF-8")
    public @ResponseBody String synchronizeHbaseMetadata(@RequestBody JSONObject pJson) {
        List<Map<String, Object>> list = new ArrayList<>();
        try{
            String jdbcurl = pJson.getString("jdbcurl");
            String username = pJson.getString("username");
            String password = pJson.getString("password");

            List<String> tableNameList=new ArrayList<>();
            tableNameList=getHbaseTablesV2(jdbcurl,username,password);
            for(int j=0;j<tableNameList.size();j++){
                String tableName=tableNameList.get(j);
                insertHbaseMetadata( tableName,"hbase",jdbcurl);
            }
            JSONObject msg = new JSONObject();


            return SuccessMsg("查询成功", list);
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }

    @RequestMapping(value = "/synchronizeHbaseMetadataV2", produces = "text/plain;charset=UTF-8")
    public @ResponseBody String synchronizeHbaseMetadataV2(@RequestBody JSONObject pJson) {
        List<Map<String, Object>> list = new ArrayList<>();
        try{
            String jdbcurl = pJson.getString("jdbcurl");
            String username = pJson.getString("username");
            String password = pJson.getString("password");

            List<String> tableNameList=new ArrayList<>();
            tableNameList=getTablesList(jdbcurl,username,password);
            for(int j=0;j<tableNameList.size();j++){
                String tableName=tableNameList.get(j);
                insertHbaseMetadata( tableName,"hbase",jdbcurl);
            }
            JSONObject msg = new JSONObject();

//            Map authType = DbFactory.Open(DbFactory.FORM).selectOne("authType.getAuthTypeByName",aythTypeName);
//
//            Statement stat = DbFactory.Open(authType.get("auth_db").toString()).getConnection().createStatement();
//            ResultSet set = stat.executeQuery(authType.get("auth_sql").toString());
//            ResultSetMetaData rsmd = set.getMetaData();
//            int cc = rsmd.getColumnCount();
//            while (set.next()) {
//                Map<String, Object> retMap = new LinkedHashMap<String, Object>(cc);
//                list.add(retMap);
//                for (int i = 1; i <= cc; i++) {
//                    retMap.put(rsmd.getColumnLabel(i).toLowerCase(), set.getObject(i));
//                }
//            }
            return SuccessMsg("查询成功", list);
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }

    @RequestMapping(value = "/insertHbaseMetadata", produces = "text/plain; charset=utf-8")
    public String insertHbaseMetadata(final String tableName,final String table_type,String url)
    {
//        String currentUser = SysContext.getRequestUser().getUserName();
        Date date=new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = sdf.format(date);
        JSONObject obj = new JSONObject();

        try{
            long id =IDUtil.getId();
            Map param = new HashMap<>();

            param.put("table_id", id);
            param.put("table_name", tableName);
            param.put("table_desc", "todo");
            param.put("catalog_id", 1);
            param.put("dbtype_id", table_type);
            param.put("source_id", "内部数据");
            param.put("host_id", "hbase");

            param.put("url", url);
            param.put("data_count", "data_count");

            param.put("create_date", str);

            param.put("create_by", "lee");
            param.put("update_date", str);
            param.put("update_by", "update_by");
            param.put("data_update_date", str);

            DbFactory.Open(DbFactory.FORM).insert("mysqlmetadata.insertMysqlMetadata",param);
            obj.put("result", "success");
            DbFactory.close(DbFactory.FORM);
        }catch(Exception e){
            log.error("表信息插入元数据表失败!");
            obj.put("result", "error");
            obj.put("errMsg", "表信息插入元数据表失败!");
            e.printStackTrace();
        }
        return JSON.toJSONString(obj);
    }

    public  void create(String url,String user,String password)  {

        try {
            initV2( url, user, password);
            String createSql = "CREATE TABLE user (id varchar PRIMARY KEY,name varchar ,passwd varchar)";
            PreparedStatement ps = conn.prepareStatement(createSql);
            ps.execute();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void  upsert(String url,String user,String password) {
        try {
            initV2( url, user, password);
            String upsertSql = "upsert into user(id, name, passwd) values(?, ?, ?)";
//        String[] param = {"1", "张三", "123456"};
            String[] param = {"2", "李四", "111111"};
            PreparedStatement ps = conn.prepareStatement(upsertSql);
            for (int i = 1; i <= param.length; i++) {
                ps.setString(i, param[i - 1]);
            }
            ps.executeUpdate();
            conn.commit(); // you must commit
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void query(String url,String user,String password) {
        try {
            initV2( url, user, password);
            String sql = "select * from user";
            String[] param = null;

            PreparedStatement ps = conn.prepareStatement(sql);
            if (param != null) {
                for (int i = 1; i <= param.length; i++) {
                    ps.setString(i, param[i - 1]);
                }
            }

            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int colLength = meta.getColumnCount();
            List<String> colName = new ArrayList<>();
            for (int i = 1; i <= colLength; i++) {
                colName.add(meta.getColumnName(i));
            }

            List<String[]> result = new ArrayList<>();
            String[] colArr;
            while (rs.next()) {
                colArr = new String[colLength];
                for (int i = 0; i < colLength; i++) {
                    colArr[i] = rs.getString(colName.get(i));
                }
                result.add(colArr);
            }
            ps.close();
            System.out.println(JSON.toJSONString(result));
            conn.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/queryDataByTableName", produces = "text/plain;charset=UTF-8")
    public  List<String[]> queryDataByTableName(@RequestBody JSONObject pJson) {
        String jdbcurl = pJson.getString("jdbcurl");
        String username = pJson.getString("username");
        String password = pJson.getString("password");
        String tableName= pJson.getString("tableName");
        List<String[]> result = new ArrayList<>();
        try {
            initV2( jdbcurl, username, password);
            String sql = "select * from  "+tableName;
            String[] param = null;

            PreparedStatement ps = conn.prepareStatement(sql);
            if (param != null) {
                for (int i = 1; i <= param.length; i++) {
                    ps.setString(i, param[i - 1]);
                }
            }

            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int colLength = meta.getColumnCount();
            List<String> colName = new ArrayList<>();
            for (int i = 1; i <= colLength; i++) {
                colName.add(meta.getColumnName(i));
            }


            String[] colArr;
            while (rs.next()) {
                colArr = new String[colLength];
                for (int i = 0; i < colLength; i++) {
                    colArr[i] = rs.getString(colName.get(i));
                }
                result.add(colArr);
            }
            ps.close();
            System.out.println(JSON.toJSONString(result));
            conn.close();

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value="/getDataBytableName",produces = "text/plain;charset=UTF-8")
    public String getDataBytableName(@RequestBody JSONObject pJson) throws UnsupportedEncodingException {
        String tableName = pJson.getString("tableName");

        Map<String,String> map = new HashMap<String,String>();
        map.put("tableName", tableName);
        List<Map> authList = DbFactory.Open("hbase").selectList("hbasemetadata.getTableNames",map);
        return JSON.toJSONString(authList);
    }

    @RequestMapping(value="/statisticsTableRecordsNumber",produces = "text/plain;charset=UTF-8")
    public String statisticsTableRecordsNumber(@RequestBody JSONObject pJson)    throws  ClassNotFoundException, SQLException {
        String dbName = pJson.getString("dbName");
        String jdbcurl = pJson.getString("jdbcurl");
        String username = pJson.getString("username");
        String password = pJson.getString("password");
        Map<String,String> map = new HashMap<String,String>();

        List<String> tableNameList=new ArrayList<>();
//        tableNameList= getTablesV3( jdbcurl,  username, password, dbName);
        tableNameList=getHbaseTablesV2(jdbcurl,username,password);
        for(int j=0;j<tableNameList.size();j++){
            String tableName=tableNameList.get(j);
            map.put("tableName", tableName);
            map.put("dbName", dbName);
            List<Map> tableRecordsNumber = DbFactory.Open("hbase").selectList("hbasemetadata.tableRecordsNumber",map);
            String data_count2="";
            if(tableRecordsNumber.size()>0){
                Map<String,String> datacountMap  =tableRecordsNumber.get(0);
                Object value2 =  datacountMap.get("RECORDSNUMBER");
                data_count2=value2.toString();
            }
            map.put("data_count", data_count2);
            DbFactory.Open("form").selectList("mysqlmetadata.modifyDataCount",map);

        }


        return "success";
    }

    //查看表结构
    @RequestMapping(value = "/getTableStructure", produces = "text/plain;charset=UTF-8")
    public String getTableStructure(@RequestBody JSONObject pJson) throws SQLException ,ClassNotFoundException{
//        final String url=pJson.getString("jdbcurl");
//        final String username=pJson.getString("username");
//        final String password=pJson.getString("password");
//        final String databaseName=pJson.getString("dbName");

        final String tableName=pJson.getString("tableName");
        Map<String,String> map = new HashMap<String,String>();
        map.put("tableName", tableName);
        List<Map> tablefields = DbFactory.Open("hbase").selectList("hbasemetadata.getTableStructure",map);
        JSONArray fields =new JSONArray();

       for(int i=1;i<tablefields.size();i++){
           Map<String,String> fieldmap=tablefields.get(i);
            JSONObject field=new JSONObject();
            field.put("fieldName",fieldmap.get("COLUMN_NAME"));
            field.put("columnFamily",fieldmap.get("COLUMN_FAMILY"));
//            field.put(res.getString(1),res.getString(2));
//            System.out.println(res.getString(1)+"\t"+res.getString(2));
            fields.add(field);
        }
        System.out.println(fields.toString());
        return fields.toString();
    }

    public static void main(String[] args) throws SQLException {
//        Properties props = new Properties();
//        props.setProperty("phoenix.query.timeoutMs","1200000");
//        props.setProperty("hbase.rpc.timeout","1200000");
//        props.setProperty("hbase.client.scanner.timeout.period","1200000");
        String url="jdbc:phoenix:" + "172.17.5.132,172.17.5.133,172.17.5.166:2181?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT";
        String username="admin";
        String password="admin";
//        Connection conn      = DriverManager.getConnection(url, props);

//        Statement stmt = null;
//        ResultSet rset = null;
//        try {
//            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        Connection con =  DriverManager.getConnection("jdbc:phoenix:" + "172.17.5.132,172.17.5.133,172.17.5.166:2181?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT","admin","admin");
//
//        stmt = con.createStatement();
//
//        DatabaseMetaData connMetaData = con.getMetaData();
//
//        String[] type = {"TABLE","VIEW"};
//        String[] type2 = {"VIEW"};
//        ResultSet rs = connMetaData.getTables(null, null, null, type);
////        ResultSet rs2 = connMetaData.getTables(null, null, null, type2);
//        boolean testFlag = false;
//        while (rs.next()){
//            String tt=rs.getString("TABLE_NAME");
//
//            String tp=rs.getString("TABLE_TYPE");
//            System.out.println(" 表的名称 "+tt+"   表的类型 "+tp);
//        }
//
////        PreparedStatement statement = con.prepareStatement("select * from testlee1022");
////        rset = statement.executeQuery();
////        while (rset.next()) {
////            System.out.println(rset.getString("mycolumn"));
////        }
////        statement.close();
//        stmt.close();
//        con.close();
         HbaseMetadata hbaseMetadata = new HbaseMetadata();
//         hbaseMetadata.create(url,username,password);

//        hbaseMetadata.upsert(url,username,password);
        hbaseMetadata.query(url,username,password);
         System.out.println("====");
    }

}
