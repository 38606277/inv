package root.report.datastorage.hive;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;
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
@RequestMapping("/reportServer/HiveMetadataV2")
public class HiveMetadata extends RO
{
    private static final Logger log = Logger.getLogger(HiveMetadata.class);
    private static ErpUtil erpUtil = new ErpUtil();
    private static final String DB_CONFIG_PATH = System.getProperty("user.dir")+"/config/DBConfig.xml";

    private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    private static String url = "jdbc:hive2://172.17.5.132:10000/default";
    private static String url2 = "jdbc:hive2://172.17.5.132:10000/";
    private static String user = "root";
    private static String password ="123456";
    private static String sql="";
    private static ResultSet res;

    private static Connection conn;
    private static Statement stmt;

    public void init() throws ClassNotFoundException, SQLException {
        Class.forName(driverName);
        conn= DriverManager.getConnection(url,user,password);
        stmt=conn.createStatement();

    }
    public void init(String url,String user,String password) throws ClassNotFoundException, SQLException {
        Class.forName(driverName);
        conn= DriverManager.getConnection(url,user,password);
        stmt=conn.createStatement();

    }
    public void initV2(String databaseName) throws ClassNotFoundException, SQLException {
        Class.forName(driverName);
        conn= DriverManager.getConnection(url2+databaseName,user,password);
        stmt=conn.createStatement();

    }

    public void initForGetTable(String url,String user,String password,String databaseName) throws ClassNotFoundException, SQLException {
        Class.forName(driverName);
        conn= DriverManager.getConnection(url+databaseName,user,password);
        stmt=conn.createStatement();

    }

    // 创建数据库
    @RequestMapping(value = "/createDatabase", produces = "text/plain;charset=UTF-8")
    public  void createDatabase() throws SQLException {
        sql = "create database hive_jdbc";
        System.out.println("Running:"+ sql);
        stmt.execute(sql);
    }

    // 查询所有数据库
    @RequestMapping(value = "/showDatabases", produces = "text/plain;charset=UTF-8")
    public void showDatabases() throws  ClassNotFoundException, SQLException {
        if(conn==null){
            init();
        }

        sql="show databases";
        System.out.println("Running:"+sql);
        res = stmt.executeQuery(sql);
        while (res.next()){
            System.out.println(res.getString(1));
        }
    }

    @RequestMapping(value = "/getHiveDatabases", produces = "text/plain;charset=UTF-8")
    public  List<String>  getHiveDatabases() throws  ClassNotFoundException, SQLException {
        if(conn==null){
            init();
        }
        sql="show databases";
        System.out.println("Running:"+sql);
        res = stmt.executeQuery(sql);
        List<String> databaseNameList= new ArrayList<>();
        while (res.next()){
            System.out.println(res.getString(1));
            databaseNameList.add(res.getString(1));
        }

        return databaseNameList;
    }

    @RequestMapping(value = "/getHiveDatabasesV2", produces = "text/plain;charset=UTF-8")
    public  List<String>  getHiveDatabases(String url,String user,String password) throws  ClassNotFoundException, SQLException {
        if(conn==null){
            init(url,user,password);
        }
        sql="show databases";
        System.out.println("Running:"+sql);
        res = stmt.executeQuery(sql);
        List<String> databaseNameList= new ArrayList<>();
        while (res.next()){
            System.out.println(res.getString(1));
            databaseNameList.add(res.getString(1));
        }
//        try {
//            //destory();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return databaseNameList;
    }




    // 查询数据库所有表
    @RequestMapping(value = "/getHiveTables", produces = "text/plain;charset=UTF-8")
    public List<String> getHiveTables(@RequestBody JSONObject pJson) throws  ClassNotFoundException, SQLException {
        String databaseName=pJson.getString("databaseName");
        initV2(databaseName);
        sql="show tables";
        System.out.println("Running:"+sql);
        res = stmt.executeQuery(sql);
        List<String> tableNameList= new ArrayList<>();
        while (res.next()){
            System.out.println(res.getString(1));
            tableNameList.add(res.getString(1));
        }
//        try {
//            destory();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return tableNameList;
    }


    // 查询数据库下所有表
    @RequestMapping(value = "/getHiveTablesV2", produces = "text/plain;charset=UTF-8")
    public List<String> getHiveTables(String url, String username,String password,String databaseName) throws  ClassNotFoundException, SQLException {
//        String databaseName=pJson.getString("databaseName");
        initForGetTable( url,  username, password,databaseName);
        sql="show tables";
        System.out.println("Running:"+sql);
        res = stmt.executeQuery(sql);
        List<String> tableNameList= new ArrayList<>();
        while (res.next()){
            System.out.println(res.getString(1));
            tableNameList.add(res.getString(1));
        }
//        try {
//            destory();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return tableNameList;
    }

    @RequestMapping(value = "/showTablesV2", produces = "text/plain;charset=UTF-8")
    public void showTablesV2(@RequestBody JSONObject pJson) throws  ClassNotFoundException, SQLException {
        String databaseName=pJson.getString("databaseName");
        //initV2(databaseName);
        if(conn==null){
            init();
        }
        sql="use "+databaseName+";"+"show tables ;";
        System.out.println("Running:"+sql);
        res = stmt.executeQuery(sql);
        while (res.next()){
            System.out.println(res.getString(1));
        }
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


    //加载数据
    @RequestMapping(value = "/loadData", produces = "text/plain;charset=UTF-8")
    public void loadData() throws SQLException {
        String filePath = "'/opt/emp.test'";
        sql="load data local inpath "+filePath+" "+"into table emp";
        System.out.println("Running:"+sql);
        stmt.execute(sql);
    }

    //查询数据
    @RequestMapping(value = "/selectData", produces = "text/plain;charset=UTF-8")
    public  List<String[]> selectData() throws SQLException,ClassNotFoundException {
        if(conn==null){
            init();
        }
        List<String[]> result = new ArrayList<>();
        sql = "select * from employee";
        System.out.println("Running:"+sql);
        res = stmt.executeQuery(sql);
        String[] param = null;
        PreparedStatement ps = conn.prepareStatement(sql);
        if (param != null) {
            for (int i = 1; i <= param.length; i++) {
                ps.setString(i, param[i - 1]);
            }
        }

        res = ps.executeQuery();
        ResultSetMetaData meta = res.getMetaData();
        int colLength = meta.getColumnCount();
        List<String> colName = new ArrayList<>();
        for (int i = 1; i <= colLength; i++) {
            colName.add(meta.getColumnName(i));
        }
        String[] colArr;
        while (res.next()) {
            colArr = new String[colLength];
            for (int i = 0; i < colLength; i++) {
                colArr[i] = res.getString(colName.get(i));
            }
            result.add(colArr);
        }

  /*      while (res.next()){
            System.out.println(res.getString("id") + "\t\t" + res.getString("name") + "\t\t" + res.getString("age"));
        }*/

        return result;
    }



    // 统计查询（会运行mapreduce作业）
    @RequestMapping(value = "/countData", produces = "text/plain;charset=UTF-8")
    public void countData() throws Exception {
        sql = "select count(1) from emp";
        System.out.println("Running: " + sql);
        res = stmt.executeQuery(sql);
        while (res.next()) {
            System.out.println(res.getInt(1) );
        }
    }

    // 删除数据库
    @RequestMapping(value = "/dropDatabase", produces = "text/plain;charset=UTF-8")
    public void dropDatabase() throws Exception {
        String sql = "drop database if exists hive_jdbc_test";
        System.out.println("Running: " + sql);
        stmt.execute(sql);
    }

/*    // 删除数据库表
    @RequestMapping(value = "/dropTable", produces = "text/plain;charset=UTF-8")
    public void dropTable() throws Exception {
        String sql = "drop table if exists emp";
        System.out.println("Running: " + sql);
        stmt.execute(sql);
    }*/

    // 释放资源
    @RequestMapping(value = "/dropTable", produces = "text/plain;charset=UTF-8")
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
//                tableNameList= getTablesV3( jdbcurl,  username, password, databaseName);
//                for(int j=0;j<tableNameList.size();j++){
//                    String tableName=tableNameList.get(j);
//                    insertMysqlMetadataV2( tableName);
//                }

            }
            JSONObject msg = new JSONObject();


            return SuccessMsg("查询成功", list);
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }


    /**
     * 获取数据库下所有表
     * */
    @RequestMapping(value = "/getTablesByDbName", method = RequestMethod.POST)
    public List<String>  getTablesByDbName(final String url, final String user, final String password,final String dbConnName) throws SQLException {
        String jdbcUrl;
        final String con = "?";
        if(url.contains(con)){
            String jdbc = url.substring(0, url.indexOf(con));
            String jdbc2 = jdbc.substring(0, jdbc.lastIndexOf("/"));
            jdbcUrl = jdbc2+dbConnName;
        }else{
            String jdbc = url.substring(0, url.lastIndexOf("/")+1);
            jdbcUrl =jdbc + dbConnName;
        }

//        List<String> tableNames=showTables();
        List<String> tableNamesList=null;
        return tableNamesList;
    }

    @RequestMapping(value = "/synchronizeHiveMetadata", produces = "text/plain;charset=UTF-8")
    public @ResponseBody String synchronizeHiveMetadata(@RequestBody JSONObject pJson) {
        List<Map<String, Object>> list = new ArrayList<>();
        try{
            String jdbcurl = pJson.getString("jdbcurl");
            String username = pJson.getString("username");
            String password = pJson.getString("password");

            List<String> databaseList = new ArrayList<>();
            databaseList=getHiveDatabases(jdbcurl,  username, password);

            for(int i=0;i<databaseList.size();i++){
                String databaseName=databaseList.get(i);
                List<String> tableNameList=new ArrayList<>();
                tableNameList= getHiveTables(jdbcurl,  username, password, databaseName);
                for(int j=0;j<tableNameList.size();j++){
                    String tableName=tableNameList.get(j);
                    insertHiveMetadata( tableName,"hive",jdbcurl,databaseName);
                }

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

    @RequestMapping(value = "/insertHiveMetadata", produces = "text/plain; charset=utf-8")
    public String insertHiveMetadata(final String tableName,final String table_type,String url,String databaseName)
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
            param.put("host_id", databaseName);

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

    @RequestMapping(value="/getDataBytableName",produces = "text/plain;charset=UTF-8")
    public String getDataBytableName(@RequestBody JSONObject pJson) throws UnsupportedEncodingException {
        String tableName = pJson.getString("tableName");

        Map<String,String> map = new HashMap<String,String>();
        map.put("tableName", tableName);
        List<ArrayList> authList = DbFactory.Open("hive").selectList("hivemetadata.getTableNames",map);
        return JSON.toJSONString(authList);
    }

    @RequestMapping(value="/getHiveDatabasesV3",produces = "text/plain;charset=UTF-8")
    public String getHiveDatabasesV3(@RequestBody JSONObject pJson) throws UnsupportedEncodingException {
        String tableName = pJson.getString("tableName");

        Map<String,String> map = new HashMap<String,String>();
        map.put("tableName", tableName);
        List<Map> authList = DbFactory.Open("hive").selectList("hivemetadata.getTableNames",map);
        return JSON.toJSONString(authList);
    }

    @RequestMapping(value = "/statisticsTableRecordsNumber", produces = "text/plain;charset=UTF-8")
    public @ResponseBody String statisticsTableRecordsNumber(@RequestBody JSONObject pJson) {
        List<Map<String, Object>> list = new ArrayList<>();
        try{
            String jdbcurl = pJson.getString("jdbcurl");
            String username = pJson.getString("username");
            String password = pJson.getString("password");
            String dbName = pJson.getString("dbName");
            Map<String,String> map = new HashMap<>();
            List<String> tableNameList=new ArrayList<>();
            tableNameList= getHiveTables(jdbcurl,  username, password, dbName);
            for(int j=0;j<tableNameList.size();j++){
                String tableName=tableNameList.get(j);

                map.put("tableName", tableName);
                map.put("dbName", dbName);
                List<Map> tableRecordsNumber = DbFactory.Open("hive").selectList("hivemetadata.tableRecordsNumber",map);
                String data_count2="";
                if(tableRecordsNumber.size()>0){
                    Map<String,String> datacountMap  =tableRecordsNumber.get(0);
                    Object value2 =  datacountMap.get("recordsnumber");
                    data_count2=value2.toString();
                }
                map.put("data_count", data_count2);
                DbFactory.Open("form").selectList("mysqlmetadata.modifyDataCount",map);
            }
            JSONObject msg = new JSONObject();


            return SuccessMsg("成功统计hive表记录数", list);
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }


    //查看表结构
    @RequestMapping(value = "/getTableStructure", produces = "text/plain;charset=UTF-8")
    public String getTableStructure(@RequestBody JSONObject pJson) throws SQLException ,ClassNotFoundException{
        final String url=pJson.getString("jdbcurl");
        final String username=pJson.getString("username");
        final String password=pJson.getString("password");
        final String databaseName=pJson.getString("dbName");
        initForGetTable( url,  username, password,databaseName);
//        final String dbName=pJson.getString("dbName");
        final String tableName=pJson.getString("tableName");
        sql ="desc  "+tableName;
        System.out.println("Running:"+sql);
        res = stmt.executeQuery(sql);
//        List<Map<String,String>> fields=new ArrayList<Map<String,String>>();
        JSONArray fields =new JSONArray();
        while (res.next()){
            JSONObject field=new JSONObject();
            field.put("fieldName",res.getString(1));
            field.put("fieldType",res.getString(2));
//            field.put(res.getString(1),res.getString(2));
//            System.out.println(res.getString(1)+"\t"+res.getString(2));
            fields.add(field);
        }
        System.out.println(fields.toString());
        return fields.toString();
    }

    public  static void main(String[] args) throws ClassNotFoundException,SQLException{
        HiveMetadata hiveMetadata =new HiveMetadata();
        hiveMetadata.selectData();
    }

}
