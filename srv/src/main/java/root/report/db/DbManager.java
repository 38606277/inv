package root.report.db;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.util.ErpUtil;
import root.report.util.XmlUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

@RestController
@RequestMapping("/reportServer/DBConnection")
public class DbManager
{
    private static final Logger log = Logger.getLogger(DbManager.class);
    private static ErpUtil erpUtil = new ErpUtil();
    private static final String DB_CONFIG_PATH = System.getProperty("user.dir")+"/config/DBConfig.xml";

    @RequestMapping(value="/ListAll",produces = "text/plain;charset=UTF-8")

//    TODO 这里没有错误处理
    public String getAllDBConnections(){
        JSONArray array = new JSONArray();
        Document dom = null;
        try{
            dom = XmlUtil.parseXmlToDom(new FileInputStream(DB_CONFIG_PATH));
        }catch (Exception e){
            log.error("解析DBConfig.xml异常!");
            e.printStackTrace();
        } 
        List<Element> dbs = dom.selectNodes("/DBConnection/DB");
        JSONObject obj = null;
        for (Element element:dbs){
           obj = new JSONObject(true);
           obj.put("name", element.selectSingleNode("name").getText());
           obj.put("driver", element.selectSingleNode("driver").getText());
           obj.put("dbtype", element.selectSingleNode("dbtype").getText());
           obj.put("url", element.selectSingleNode("url").getText());
           obj.put("username", element.selectSingleNode("username").getText());
           obj.put("password", element.selectSingleNode("password").getText());
           obj.put("maxPoolSize", element.selectSingleNode("maxPoolSize").getText());
           obj.put("minPoolSize", element.selectSingleNode("minPoolSize").getText());
           array.add(obj);
        }
        
        return array.toJSONString();
    }
//    todo 这里没有错误处理
    @RequestMapping(value="/save",produces = "text/plain;charset=UTF-8")
    public String save(@RequestBody String pJson)
    {
        JSONObject retObj = new JSONObject();
        JSONObject obj = (JSONObject) JSON.parse(pJson);
        Document dom = null;
        try
        {
            dom = XmlUtil.parseXmlToDom(new FileInputStream(DB_CONFIG_PATH));
        }
        catch(Exception e)
        {
            log.error("解析DBConfig.xml异常!");
            e.printStackTrace();
            retObj.put("retCode", false);
            retObj.put("retMsg", "解析DBConfig.xml异常");
        }
        if(dom.selectSingleNode("/DBConnection/DB[name='"+obj.getString("name")+"']")==null)
        {
            Element root = dom.getRootElement();
            
            Element db = root.addElement("DB");
            Element name = db.addElement("name");
            Element driver = db.addElement("driver");
            Element dbtype = db.addElement("dbtype");
            Element url = db.addElement("url");
            Element username = db.addElement("username");
            Element password = db.addElement("password");
            Element maxPoolSize = db.addElement("maxPoolSize");
            Element minPoolSize = db.addElement("minPoolSize");
            
            name.setText(obj.getString("name"));
            driver.setText(obj.getString("driver"));
            dbtype.setText(obj.getString("dbtype"));
            url.setText(obj.getString("url"));
            
            String encryptPwd = "";
            try
            {
                encryptPwd = erpUtil.encode(obj.getString("password"));
            }
            catch(Exception e)
            {
                retObj.put("retCode", false);
                retObj.put("retMsg", "数据库密码加密异常");
                return retObj.toJSONString();
            }
            username.setText(obj.getString("username"));
            password.setText(encryptPwd);
            maxPoolSize.setText("50");
            minPoolSize.setText("3");
            
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            format.setIndent(true); //设置是否缩进
            format.setIndent("   "); //以空格方式实现缩进
            format.setNewlines(true); //设置是否换行
            
            XMLWriter writer = null;
            try
            { 
                writer = new XMLWriter(new FileOutputStream(DB_CONFIG_PATH), format);
                log.debug("新增db连接:"+db.asXML());
                writer.write(dom);
                writer.flush();
                writer.close();
                retObj.put("retCode", true);
                retObj.put("retMsg", "新增成功");
            } 
            catch (IOException e)
            {
                log.error("新增db连接异常!");
                e.printStackTrace();
                retObj.put("retCode", false);
                retObj.put("retMsg", "新增db连接错误异常");
            }
        }
        else
        {
            retObj.put("retCode", false);
            retObj.put("retMsg", "已存在相同名称的数据库连接");
        }
        
        return retObj.toJSONString();
    }

    @RequestMapping(value="/update",produces = "text/plain;charset=UTF-8")
    public String update(@RequestBody String pJson)
    {
        JSONObject retObj = new JSONObject();
        JSONObject obj = (JSONObject) JSON.parse(pJson);
        Document dom = null;
        try
        {
            dom = XmlUtil.parseXmlToDom(new FileInputStream(DB_CONFIG_PATH));
        }
        catch(Exception e)
        {
            log.error("解析DBConfig.xml异常!");
            e.printStackTrace();
            retObj.put("retCode", false);
            retObj.put("retMsg", "解析DBConfig.xml异常");
        }
        Node node = dom.selectSingleNode("/DBConnection/DB[name='"+obj.getString("name")+"']");
        if(node!=null)
        {
            String last_password = node.selectSingleNode("password").getText();
            Element root = dom.getRootElement();
            
            Node name = node.selectSingleNode("name");
            Node driver = node.selectSingleNode("driver");
            Node dbtype = node.selectSingleNode("dbtype");
            Node url = node.selectSingleNode("url");
            Node username = node.selectSingleNode("username");
            Node password = node.selectSingleNode("password");
            
            name.setText(obj.getString("name"));
            driver.setText(obj.getString("driver"));
            dbtype.setText(obj.getString("dbtype"));
            url.setText(obj.getString("url"));
            
            //如果密码未改变,直接保存,否则重新加密
            String encryptPwd = "";
            if(!last_password.equals(obj.getString("password")))
            {
                try
                {
                    encryptPwd = erpUtil.encode(obj.getString("password"));
                }
                catch(Exception e)
                {
                    retObj.put("retCode", false);
                    retObj.put("retMsg", "数据库密码加密异常");
                    return retObj.toJSONString();
                }
            }
            else
            {
                encryptPwd = last_password; 
            }
            username.setText(obj.getString("username"));
            password.setText(encryptPwd);
            
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            format.setIndent(true); //设置是否缩进
            format.setIndent("   "); //以空格方式实现缩进
            format.setNewlines(true); //设置是否换行
            
            XMLWriter writer = null;
            try
            { 
                writer = new XMLWriter(new FileOutputStream(DB_CONFIG_PATH), format);
                log.debug("修改db连接:"+node.asXML());
                writer.write(dom);
                writer.flush();
                writer.close();
                retObj.put("retCode", true);
                retObj.put("retMsg", "修改成功");
            } 
            catch (IOException e)
            {
                log.error("修改db连接异常!");
                e.printStackTrace();
                retObj.put("retCode", false);
                retObj.put("retMsg", "修改db连接异常");
            }
            DbFactory.initializeDB(name.getText());
        }
        else
        {
            retObj.put("retCode", false);
            retObj.put("retMsg", "不存在该db连接");
        }
        return retObj.toJSONString();
    }
    
    @RequestMapping(value="/GetByName",produces = "text/plain;charset=UTF-8")
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
    
    @RequestMapping(value="/Delete",produces = "text/plain;charset=UTF-8")
    public String deleteDBConnectionByName(@RequestBody String name)
    {
        JSONObject retObj = new JSONObject();
        Document dom = null;
        try
        {
            dom = XmlUtil.parseXmlToDom(new FileInputStream(DB_CONFIG_PATH));
        } 
        catch (Exception e)
        {
            log.error("解析DBConfig.xml异常!");
            e.printStackTrace();
            retObj.put("retCode", false);
            retObj.put("retMsg", "解析DBConfig.xml异常");
        }
        Node node = dom.selectSingleNode("/DBConnection/DB[name='"+name+"']");
        String dbtype = "";
        if(node==null)
        {
            retObj.put("retCode", false);
            retObj.put("retMsg", "删除失败,没有相应数据库连接");
        }
        else
        {
            dbtype = node.selectSingleNode("dbtype").getText();
            node.getParent().remove(node);
            
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            format.setIndent(true); //设置是否缩进
            format.setIndent("   "); //以空格方式实现缩进
            format.setNewlines(true); //设置是否换行
            
            XMLWriter writer = null;
            try
            { 
                writer = new XMLWriter(new FileOutputStream(DB_CONFIG_PATH), format);
                log.debug("删除db连接:"+node.asXML());
                writer.write(dom);
                writer.flush();
                writer.close();
                retObj.put("retCode", true);
                retObj.put("retMsg", "删除成功");
            } 
            catch (IOException e)
            {
                log.error("删除db连接异常!");
                e.printStackTrace();
                retObj.put("retCode", false);
                retObj.put("retMsg", "删除失败");
            }
            
            DbFactory.initializeDB(name);
        }
        
        return retObj.toJSONString();
    }
    
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
}
