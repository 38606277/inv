package root.report.datastorage.mysql;

import cn.hutool.db.Entity;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import root.report.leeutils.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.web.bind.annotation.*;
import root.report.auth.RoleModel;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.leeutils.DbManagerHutool;
import root.report.leeutils.IDUtil;
import root.report.leeutils.TreeBuilder;
import root.report.sys.SysContext;
import root.report.util.ErpUtil;
import root.report.util.XmlUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@RestController
@RequestMapping("/reportServer/DBConnection2")
public class MysqlMetadata extends RO
{
    private static final Logger log = Logger.getLogger(MysqlMetadata.class);
    private static ErpUtil erpUtil = new ErpUtil();
    private static final String DB_CONFIG_PATH = System.getProperty("user.dir")+"/config/DBConfig.xml";

   /* @RequestMapping(value="/GetByName",produces = "text/plain;charset=UTF-8")
    public String getDBConnectionByName(@RequestBody String name)
    {
        JSONObject obj = new JSONObject(true);
        Document dom = null;
        try
        {
            dom = XmlUtil.parseXmlToDom(new FileInputStream(DB_CONFIG_PATH));
        } 
        catch (Exception e)
        {
            log.error("解析DBConfig.xml异常!");
            e.printStackTrace();
        } 
        Node node = dom.selectSingleNode("/DBConnection/DB[name='"+name+"']");
        if(node!=null)
        {
            obj.put("name", node.selectSingleNode("name").getText());
            obj.put("driver", node.selectSingleNode("driver").getText());
            obj.put("dbtype", node.selectSingleNode("dbtype").getText());
            obj.put("url", node.selectSingleNode("url").getText());
            obj.put("username", node.selectSingleNode("username").getText());
            obj.put("password", node.selectSingleNode("password").getText());
            obj.put("maxPoolSize", node.selectSingleNode("maxPoolSize").getText());
            obj.put("minPoolSize", node.selectSingleNode("minPoolSize").getText());
        }
        
        return obj.toJSONString();
    }
*/
    @RequestMapping(value="/test",produces = "text/plain;charset=UTF-8")
    public String testConnection(@RequestBody String pJson)
    {
        JSONObject retObj = new JSONObject();
       // JSONArray objArr = (JSONArray)JSONObject.parse(pJson);
        JSONObject dbObj =JSONObject.parseObject(pJson);// objArr.getJSONObject(0);
        //前台更改DB页面,如果测试连通性,需先对密码解密
//        if(objArr.size()>1&&"update".equals(objArr.getString(1)))
//        {
//            //解密之前需要判断是否有对密码进行更改,如果更改则无需解密
//            String last_password = JSONObject.parseObject(getDBConnectionByName(dbObj.getString("name")))
//                    .getString("password");
//            String decryptPwd = "";
//            if(last_password.equals(dbObj.getString("password")))
//            {
//                try
//                {
//                    decryptPwd = erpUtil.decode(dbObj.getString("password"));
//                    dbObj.put("password", decryptPwd);
//                }
//                catch(Exception e)
//                {
//                    retObj.put("retCode", false);
//                    retObj.put("retMsg", "数据库密码解密异常");
//                    return retObj.toJSONString();
//                }
//            }
//        }
        Connection conn = null;
        try
        {
            Class.forName(dbObj.getString("driver"));
            String dbType = dbObj.getString("dbtype");
            if("Mysql".equals(dbType))
            {
                dbObj.put("url", dbObj.getString("url")+"?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8");
            }
            conn = DriverManager.getConnection(dbObj.getString("url"),
                    dbObj.getString("username"),dbObj.getString("password"));
            Statement stat = conn.createStatement();
            ResultSet set = null;
            if("DB2".equals(dbType))
            {
                set = stat.executeQuery("select 1 from sysibm.sysdummy1");
            }
            else
            {
                set = stat.executeQuery("select 1 from dual");
            }
            if(set!=null&&set.next()&&"1".equals(set.getString("1")))
            {
                retObj.put("retCode", "true");
                retObj.put("retMsg", "连接成功");
            }
            else
            {
                retObj.put("retCode", "false");
                retObj.put("retMsg", "连接失败");
            }
        }
        catch(Exception e)
        {
            log.error("测试数据库连接失败");
            retObj.put("retCode", "false");
            retObj.put("retMsg", e.toString());
            e.printStackTrace();
        }
        finally
        {
            if(conn!=null)
            {
                try
                {
                    conn.close();
                } 
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return retObj.toJSONString();
    }
    
/*
    public Connection getConnection(String dbName) throws Exception{
		JSONObject obj = JSON.parseObject(getDBConnectionByName(dbName));
		String driver = obj.getString("driver");
		String url = obj.getString("url");
		if(driver.indexOf("mysql")!=-1){
			url+="?serverTimezone=UTC&useSSL=true&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&rewriteBatchedStatements=true";
		}
		String username = obj.getString("username");
		String password = erpUtil.decode(obj.getString("password"));
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, username, password);
		return conn;
	}
*/

	public static void main(String[] args) throws SQLException {
        JSONObject jsonObject= new JSONObject();
        MysqlMetadata mysqlMetadata = new MysqlMetadata();

//         mysqlMetadata.getTables(  "jdbc:mysql://192.168.206.49:3306/",   "appuser",   "123456",  "aaljz");
//        JSONObject fiedNames=  mysqlMetadata.getStructure("aaljz", "aggregation_idir_internet_clues_t");
//       String fieldNames= fiedNames.getString("structure");

//        jsonObject=  mysqlMetadata.getAllDatabase("jdbc:mysql://192.168.206.49:3306/",   "appuser",   "123456");
//        jsonObject.getString("databaseNames");


//        mysqlMetadata.insertMysqlMetadata();
        System.out.println("========");
        System.out.println("========");
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
                  tableNameList= getTablesV3( jdbcurl,  username, password, databaseName);
                  for(int j=0;j<tableNameList.size();j++){
                      String tableName=tableNameList.get(j);
                      insertMysqlMetadataV2( tableName,databaseName);
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

    @RequestMapping(value = "/getAllDatabase", method = RequestMethod.POST)
    public JSONObject getAllDatabase(final String url,final String user,final String password) throws SQLException {

        List<String> arr = new ArrayList<>();
        arr=DbManagerHutool.getDatabases(url,user,password);
        JSONObject msg = new JSONObject();
        msg.put("databaseNames",arr);
        return msg;
    }

    /**
     * 获取数据库下所有表
     * */
    @RequestMapping(value = "/getTables", produces = "text/plain;charset=UTF-8")
    public @ResponseBody String getTables(final String url, final String user, final String password,final String dbConnName)throws SQLException  {

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

        try{
            final List<String> list = DbManagerHutool.getTableNameNew(jdbcUrl,user,password);
            SuccessMsg("data",list);
            return SuccessMsg("查询成功", list);
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }


    @RequestMapping(value = "/getTablesV2", method = RequestMethod.POST)
    public JSONObject getTablesV2(final String url, final String user, final String password,final String dbConnName) throws SQLException {
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
        final List<String> list = DbManagerHutool.getTableNameNew(jdbcUrl,user,password);
        final JSONObject msg = new JSONObject();
        msg.put("list", list);
        return msg;
    }

    /**
     * 获取数据库下所有表
     * */
    @RequestMapping(value = "/getTablesV3", method = RequestMethod.POST)
    public List<String>  getTablesV3(final String url, final String user, final String password,final String dbConnName) throws SQLException {
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
        final List<String> list = DbManagerHutool.getTableNameNew(jdbcUrl,user,password);

        return list;
    }


    /**
     * 获取表结构
     * */
    @RequestMapping(value = "/getStructure", method = RequestMethod.POST)
    public JSONObject getStructure(final String dbConnName,final String tableName) throws SQLException {
        Entity e = DbManagerHutool.getTableInfoVer2(dbConnName, tableName);

        JSONObject msg = new JSONObject();
        msg.put("structure", e.getFieldNames());
        return msg;
    }
    /**
     * 将表信息插入元数据信息管理表
     * */
    @RequestMapping(value = "/insertMysqlMetadata", produces = "text/plain; charset=utf-8")
    public String insertMysqlMetadata(@RequestBody JSONObject pJson)
    {
//        String currentUser = SysContext.getRequestUser().getUserName();
        java.util.Date date=new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String str = sdf.format(date);
        JSONObject obj = new JSONObject();

        String url="";
        String user="";
        String password="";
        String dbConnName="";

        try{
            getTables(url,  user, password, dbConnName);
//            RoleModel roleModel = JSONObject.parseObject(pJson, RoleModel.class);
//            roleModel.setCreatedBy(currentUser);
//            roleModel.setCreatedDate(date.toString());
//            DbFactory.Open(DbFactory.FORM).insert("role.addRole", roleModel);
            //===============================================================
            Map param = new HashMap<>();

            param.put("table_id", 11111);
            param.put("table_name", "aggregation_idir_internet_clues_t");
            param.put("table_desc", "测试一");
            param.put("table_catalog", "测试目录");
            param.put("table_type", "table_type");
            param.put("host", "host");

            param.put("url", "url");
            param.put("data_count", "data_count");
            param.put("data_source", "data_source");
            param.put("create_date", str);

            param.put("create_by", "lee");
            param.put("update_date", str);
            param.put("update_by", "update_by");
            param.put("data_update_date", str);

            DbFactory.Open(DbFactory.FORM).insert("mysqlmetadata.insertMysqlMetadata",param);
            obj.put("result", "success");
        }catch(Exception e){
            log.error("新增角色失败!");
            obj.put("result", "error");
            obj.put("errMsg", "新增角色失败!");
            e.printStackTrace();
        }
        return JSON.toJSONString(obj);
    }

    @RequestMapping(value = "/insertMysqlMetadataV2", produces = "text/plain; charset=utf-8")
    public String insertMysqlMetadataV2(final String tableName,final String dbName)
    {
//        String currentUser = SysContext.getRequestUser().getUserName();
        java.util.Date date=new Date();
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
            param.put("dbtype_id", "mysql");
            param.put("source_id", "内部数据");
            param.put("host_id", dbName);

            param.put("url", "url");
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


    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public JSONObject insert(final String dbConnName,final String tableName) throws SQLException {
        Entity e = DbManagerHutool.getTableInfoVer2(dbConnName, tableName);
        JSONObject msg = new JSONObject();
        msg.put("structure", e.getFieldNames());
        return msg;
    }

    @RequestMapping(value = "/getTableNamesByCatalog", produces = "text/plain;charset=UTF-8")
    public @ResponseBody
    String getTableNamesByCatalog(@RequestBody JSONObject pJson) {
        String catalog = pJson.getString("catalog");
        List<Map> authTypeList =new ArrayList<>();
        try{
            authTypeList = DbFactory.Open(DbFactory.FORM).selectList("mysqlmetadata.getTableNamesByCatalog",catalog);
            System.out.println("===");
            SuccessMsg("data", authTypeList);
            return SuccessMsg("查询成功", "success");
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }


    @RequestMapping(value = "/getAllTableList", produces = "text/plain;charset=UTF-8")
    public @ResponseBody String getAllTableList() {
        try{
            List<Map> authTypeList = DbFactory.Open(DbFactory.FORM)
                    .selectList("mysqlmetadata.getTableNames");
            System.out.println("==========");
            return SuccessMsg("查询成功", authTypeList);
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }


    @RequestMapping(value = "/getOrgTree", produces = "text/plain;charset=UTF-8")
    public String getOrgTree(@RequestBody JSONObject pJson)  {
        JSONObject jsonTree=new JSONObject();
//        List<Node> nodes= DbSession.selectList("fndOrg.getAll", pJson);
        List<Map> treeNodeList = DbFactory.Open(DbFactory.FORM).selectList("mysqlmetadata.getTreeNode");
        List<Node> nodes= new ArrayList<>();
        for(int i=0;i<treeNodeList.size();i++){
               Map treeNode =treeNodeList.get(i);
               String id= treeNode.get("id").toString();
               String pid=  treeNode.get("pid").toString();
               String name= treeNode.get("name").toString();
               Node node =new Node();
               node.setId(id);
               node.setPid(pid);
               node.setName(name);
               nodes.add(node);
        }

        // 拼装树形json字符串
        List<Node>  result= new TreeBuilder().buildTree(nodes);
        System.out.println(result.toString());
        return SuccessMsg("",result);
    }

    @RequestMapping(value="/getDataBytableName",produces = "text/plain;charset=UTF-8")
    public String getDataBytableName(@RequestBody JSONObject pJson)  {
        String tableName = pJson.getString("tableName");

        Map<String,String> map = new HashMap<String,String>();
        map.put("tableName", tableName);
        List<Map> authList = DbFactory.Open("form").selectList("mysqlmetadata.getTableNames",map);
        return JSON.toJSONString(authList);
    }

    @RequestMapping(value="/dataFormatConversion",produces = "text/plain;charset=UTF-8")
    public String dataFormatConversion(@RequestBody JSONObject pJson)  {
        String tableName = pJson.getString("tableName");
        String indexName = pJson.getString("indexName");
        Map<String,String> map = new HashMap<String,String>();
        map.put("tableName", tableName);
        map.put("indexName", indexName);
        List<Map> authList = DbFactory.Open("form").selectList("mysqlmetadata.getTableNames",map);
        List<Map> keyList = DbFactory.Open("form").selectList("mysqlmetadata.getIndexName",map);

        JSONArray resultArray=new JSONArray();

        for(int i=0;i<keyList.size();i++){
              Map key=keyList.get(i);
              String keyString= key.get(indexName).toString();
              JSONObject objRecord=new JSONObject();
              for(int j=0;j<authList.size();j++){
                 Map mapRecord= authList.get(j);
                 if(keyString.equals(mapRecord.get(indexName).toString())){
                       objRecord.put(indexName,mapRecord.get(indexName).toString());
                       objRecord.put(mapRecord.get("year").toString(),mapRecord.get("amount").toString());
                 }
              }
              resultArray.add(objRecord);
        }

        String result =JSON.toJSONString(resultArray);

        System.out.println("============");
        return JSON.toJSONString(resultArray);
    }


    @RequestMapping(value="/statisticsTableRecordsNumber",produces = "text/plain;charset=UTF-8")
    public String statisticsTableRecordsNumber(@RequestBody JSONObject pJson)  throws SQLException {
        String dbName = pJson.getString("dbName");
        String jdbcurl = pJson.getString("jdbcurl");
        String username = pJson.getString("username");
        String password = pJson.getString("password");
        Map<String,String> map = new HashMap<String,String>();

        List<String> tableNameList=new ArrayList<>();
        tableNameList= getTablesV3( jdbcurl,  username, password, dbName);

        for(int j=0;j<tableNameList.size();j++){
            String tableName=tableNameList.get(j);
            map.put("tableName", tableName);
            map.put("dbName", dbName);
            List<Map> tableRecordsNumber = DbFactory.Open("form").selectList("mysqlmetadata.tableRecordsNumber",map);
            String data_count2="";
            if(tableRecordsNumber.size()>0){
                Map<String,String> datacountMap  =tableRecordsNumber.get(0);
                Object value2 =  datacountMap.get("recordsnumber");
                data_count2=value2.toString();
            }
            map.put("data_count", data_count2);
            DbFactory.Open("form").selectList("mysqlmetadata.modifyDataCount",map);

        }
        return "success";
    }

    @RequestMapping(value="/statisticsRecordsNumberByDataBaseType",produces = "text/plain;charset=UTF-8")
    public String statisticsRecordsNumberByDataBaseType(@RequestBody JSONObject pJson)  throws SQLException {
        String dbName = pJson.getString("dbName");
        String dbType = pJson.getString("dbType");

        Map<String,String> map = new HashMap<String,String>();

        map.put("dbType", dbType);
        map.put("dbName", dbName);
        List<Map> databaseRecordsNumber = DbFactory.Open("form").selectList("mysqlmetadata.statisticsRecordsNumberByDataBaseType",map);
        String data_count2="";
        if(databaseRecordsNumber.size()>0){
            Map<String,String> datacountMap  =databaseRecordsNumber.get(0);
            Object value2 =  datacountMap.get("totalnum");
            data_count2=value2.toString();
        }
        return data_count2;
    }


    @RequestMapping(value="/statisticsAllRecordsNumber",produces = "text/plain;charset=UTF-8")
    public String statisticsAllRecordsNumber(@RequestBody JSONObject pJson)  throws SQLException {
        Map<String,String> map = new HashMap<String,String>();
        List<Map> databaseRecordsNumber = DbFactory.Open("form").selectList("mysqlmetadata.statisticsAllRecordsNumber",map);
        String data_count2="";
        if(databaseRecordsNumber.size()>0){
            Map<String,String> datacountMap  =databaseRecordsNumber.get(0);
            Object value2 =  datacountMap.get("totalnum");
            data_count2=value2.toString();
        }
        return data_count2;
    }

    /**
     * 获取表结构
     * */
    @RequestMapping(value = "/getStructureV2", method = RequestMethod.POST)
    public String getStructure(@RequestBody JSONObject pJson) throws SQLException {
//        String dbName = pJson.getString("dbName");
        final String dbName=pJson.getString("dbName");
        final String tableName=pJson.getString("tableName");
        JSONArray fields=new JSONArray();
        fields= DbManagerHutool.getTableInfoVer3(dbName, tableName);
        return fields.toString();
    }

}
