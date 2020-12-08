package root.report.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.RowBounds;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;
import root.configure.AppConstants;
import root.form.user.UserModel;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.service.SelectService;
import root.report.sys.SysContext;
import root.report.util.JsonUtil;
import root.report.util.XmlUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@RestController
@RequestMapping("/reportServer/select")
public class SelectControl extends RO {

    private static Logger log = Logger.getLogger(SelectControl.class);
    @Autowired
    private RestTemplate restTemplate;

	@RequestMapping(value = "/getSelectClass", produces = "text/plain;charset=UTF-8")
	public String getSelectClass()
	{
		String usersqlPath = AppConstants.getUserSqlPath();
		File file = new File(usersqlPath);
		File[] fileList = file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".xml")) {
					return true;
				}
				return false;
			}
		});
		// 构造返回json
		List<Map> list = new ArrayList<Map>();

		for (int i = 0; i < fileList.length; i++) {

			Map<String, String> map = new HashMap<String, String>();
			String filename = fileList[i].getName();
			String name = filename.substring(0, filename.lastIndexOf("."));
			map.put("name", name);
			list.add(map);

		}
		return SuccessMsg("查询成功",list);  //return JSON.toJSONString(list);

	}

	// 取所有报表基本信息
	@RequestMapping(value = "/getSelectName/{selectClassName}", produces = "text/plain;charset=UTF-8")
	public String getSelectName(@PathVariable("selectClassName") String selectClassName) {
	    //String result = "";
		// 根据名称查找对应的模板文件
		String usersqlPath = AppConstants.getUserSqlPath() + File.separator + selectClassName + ".xml";
		try {
			SAXReader sax = new SAXReader();
			sax.setValidation(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);// 这一行是必须要有的
			// 获得dom4j的文档对象
			Document document = sax.read(new FileInputStream(usersqlPath));
			Element root = document.getRootElement();
			// 得到database节点
			List<Element> selects = root.selectNodes("//select");

			// 构造返回json
			List<Map> list = new ArrayList<Map>();

			for (int i = 0; i < selects.size(); i++) {

				Element element = selects.get(i);

				//取出id
				Map<String, String> map = new HashMap<String, String>();
				map.put("name", element.attributeValue("id"));
				//取出数据查询类型
				String statementType = element.attributeValue("statementType");
				if (statementType == null) {
					map.put("type", "sql");
				} else if (statementType.equals("CALLABLE")) {
					map.put("type", "proc");
				}
			   //取出db和描述信息
				String aJsonString = "";
				for (int j = 0; j < element.nodeCount(); j++) {
					Node node1 = element.node(j);
					if (node1.getNodeTypeName().equals("Comment")) {
						aJsonString = node1.getStringValue();
						break;
					}
				}
				JSONObject jsonObject = (JSONObject) JSON.parse(aJsonString);
				map.put("db", jsonObject.getString("db"));
				map.put("desc", jsonObject.getString("desc"));

				list.add(map);
			}
			return SuccessMsg("查询成功",list);
			//result = JSON.toJSONString(list);
		} catch (Exception e) {
			e.printStackTrace();
			return ErrorMsg("2000","查询失败");
		}
		  //return result;

	}

	// 根据SQLID 取入参 出参信息
	@RequestMapping(value = "/getSelectParam/{selectClassId}/{selectID}", produces = "text/plain;charset=UTF-8")
	public String getSelectParam(@PathVariable("selectClassId") String selectClassId,
			@PathVariable("selectID") String selectID) {
		String result = "";
		// 根据名称查找对应的模板文件
		String usersqlPath = AppConstants.getUserSqlPath() + File.separator + selectClassId + ".xml";

		try {
			SAXReader sax = new SAXReader();
			sax.setValidation(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);// 这一行是必须要有的
			// 获得dom4j的文档对象
			Document document = sax.read(new FileInputStream(usersqlPath));
			Element root = document.getRootElement();
			// 得到database节点
			// List<Element> selects = root.selectNodes("//select");
			// 得到database节点
			Element aSelect = (Element) root.selectSingleNode("//select[@id='" + selectID + "']");

			String aJsonString = "";
			for (int j = 0; j < aSelect.nodeCount(); j++) {
				Node node1 = aSelect.node(j);
				if (node1.getNodeTypeName().equals("Comment")) {
					aJsonString = node1.getStringValue();
					break;
				}
			}

			JSONObject commentObj = JSON.parseObject(aJsonString);
            //in参数中带lookup属性的不展示
            JSONArray inArr = commentObj.getJSONArray("in");
            JSONObject inObj = null;
            for (int j = inArr.size()-1; j >=0; j--)
            {
                inObj = inArr.getJSONObject(j);
                if(inObj.get("auth")!=null)
                {
                    if (!inObj.get("auth").toString().equals(""))
                    {
                    	inArr.remove(j);	
                    }
                	
                }
            }
            commentObj.put("in", inArr);
            return SuccessMsg("查询成功",commentObj); //return commentObj.toJSONString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return ErrorMsg("2000","查询失败");
		}
		//return JSON.toJSONString(result);
	}
	
	@RequestMapping(value = "/getSelectAuthList/{userName}", produces = "text/plain;charset=UTF-8")
    public String getSelectAuthList(@PathVariable("userName") String userName) {
	    try{
	    	Map<String,String> map = new HashMap<String,String>();
		    map.put("userName",userName);
		    int isAdmin =  DbFactory.Open(DbFactory.FORM).selectOne("user.isAdmin",userName);
	        if(isAdmin == 1){
	            return this.getSelectClass();
	        }else{
	            List<Map> selectAuthList = DbFactory.Open(DbFactory.FORM).selectList("rule.getSelectAuthList",map);
	            return SuccessMsg("查询成功", selectAuthList);
	        }
		     
		    
	    }catch(Exception ex)
	    {
	    	ex.printStackTrace();
	    	return ErrorMsg("3000", ex.getMessage());
	    }
		
    }
	@RequestMapping(value = "/getSelectAuthListByClass/{userName}/{className}", produces = "text/plain;charset=UTF-8")
	public String getSelectAuthListByClass(@PathVariable("userName") String userName,@PathVariable("className") String className) {
	    
		String result = "";
		 try {
	    Map<String,String> cmap = new HashMap<String,String>();
	    cmap.put("userName",userName);
	    cmap.put("className",className);
	    int isAdmin =  DbFactory.Open(DbFactory.FORM).selectOne("user.isAdmin",userName);
	    List<Map> selectAuthList = DbFactory.Open(DbFactory.FORM).selectList("rule.getSelectAuthListByClass",cmap);
	    for (Map selectAuth : selectAuthList) {
            String[] str = selectAuth.get("name").toString().split("/");
            selectAuth.put("name", str[str.length-1]);
        }
	 // 根据名称查找对应的模板文件
        String usersqlPath = AppConstants.getUserSqlPath() + File.separator + className + ".xml";

       
            SAXReader sax = new SAXReader();
            sax.setValidation(false);
            sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);// 这一行是必须要有的
            // 获得dom4j的文档对象
            Document document = sax.read(new FileInputStream(usersqlPath));
            Element root = document.getRootElement();
            // 得到database节点
            List<Element> selects = root.selectNodes("//select");

            // 构造返回json
            List<Map> list = new ArrayList<Map>();

            for (int i = 0; i < selects.size(); i++) {

                Element element = selects.get(i);

                //取出id
                Map<String, String> map = new HashMap<String, String>();
                map.put("name", element.attributeValue("id"));
                //取出数据查询类型
                String statementType = element.attributeValue("statementType");
                if (statementType == null) {
                    map.put("type", "sql");
                } else if (statementType.equals("CALLABLE")) {
                    map.put("type", "proc");
                }
               //取出db和描述信息
                String aJsonString = "";
                for (int j = 0; j < element.nodeCount(); j++) {
                    Node node1 = element.node(j);
                    if (node1.getNodeTypeName().equals("Comment")) {
                        aJsonString = node1.getStringValue();
                        break;
                    }
                }
                JSONObject jsonObject = (JSONObject) JSON.parse(aJsonString);
                map.put("db", jsonObject.getString("db"));
                map.put("desc", jsonObject.getString("desc"));

                list.add(map);
            }
            if(isAdmin == 1){
                return SuccessMsg("查询成功",list);
            }else{
                List<Map> authMap = new ArrayList<Map>();
                for (Map auth : selectAuthList) {
                    for (Map l : list) {
                        if(l.get("name").equals(auth.get("name"))){
                            authMap.add(l);
                        }
                    }
                }
                return SuccessMsg("查询成功",authMap);
            }
//            List<Map> authMap = new ArrayList<Map>();
//            for (Map auth : selectAuthList) {
//                for (Map l : list) {
//                    if(l.get("name").equals(auth.get("name"))){
//                        authMap.add(l);
//                    }
//                }
//            }
//            return SuccessMsg("查询成功",authMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ErrorMsg("3000", e.getMessage());
        }
      
	}
        
    @RequestMapping(value = "/getSelectClassTree", produces = "text/plain;charset=UTF-8")
    public String getSelectClassTree() {
        String usersqlPath = AppConstants.getUserSqlPath();
        File file = new File(usersqlPath);
        File[] fileList = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        });
        // 构造返回json
        List<Map> list = new ArrayList<Map>();

        for (int i = 0; i < fileList.length; i++) {

            JSONObject authNode = new JSONObject(true);
            String filename = fileList[i].getName();
            String name = filename.substring(0, filename.lastIndexOf("."));
            authNode.put("name", name);
            authNode.put("value", name);
            
            String result = "";
            // 根据名称查找对应的模板文件
            String sqlPath = AppConstants.getUserSqlPath() + File.separator + name + ".xml";

            try {
                SAXReader sax = new SAXReader();
                sax.setValidation(false);
                sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);// 这一行是必须要有的
                // 获得dom4j的文档对象
                Document document = sax.read(new FileInputStream(sqlPath));
                Element root = document.getRootElement();
                // 得到database节点
                List<Element> selects = root.selectNodes("//select");

                // 构造返回json
                List<Map> childlist = new ArrayList<Map>();

                for (int j = 0; j < selects.size(); j++) {

                    Element element = selects.get(j);

                    //取出id
                    Map<String, String> childmap = new HashMap<String, String>();
                    childmap.put("name", element.attributeValue("id"));
                    childmap.put("value", name+"/"+element.attributeValue("id"));
                    childlist.add(childmap);
                }
                authNode.put("children", childlist);
            } catch (Exception e) {
                e.printStackTrace();
            }
            list.add(authNode);

        }
        

        return JSON.toJSONString(list);

    }
    @RequestMapping(value = "/getSelectClassTreeReact", produces = "text/plain;charset=UTF-8")
    public String getSelectClassTreeReact() {
        String usersqlPath = AppConstants.getUserSqlPath();
        File file = new File(usersqlPath);
        File[] fileList = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        });
        // 构造返回json
        List<Map> list = new ArrayList<Map>();

        for (int i = 0; i < fileList.length; i++) {

            JSONObject authNode = new JSONObject(true);
            String filename = fileList[i].getName();
            String name = filename.substring(0, filename.lastIndexOf("."));
            authNode.put("title", name);
            authNode.put("key", name);

            String result = "";
            // 根据名称查找对应的模板文件
            String sqlPath = AppConstants.getUserSqlPath() + File.separator + name + ".xml";

            try {
                SAXReader sax = new SAXReader();
                sax.setValidation(false);
                sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);// 这一行是必须要有的
                // 获得dom4j的文档对象
                Document document = sax.read(new FileInputStream(sqlPath));
                Element root = document.getRootElement();
                // 得到database节点
                List<Element> selects = root.selectNodes("//select");

                // 构造返回json
                List<Map> childlist = new ArrayList<Map>();

                for (int j = 0; j < selects.size(); j++) {

                    Element element = selects.get(j);

                    //取出id
                    Map<String, String> childmap = new HashMap<String, String>();
                    childmap.put("title", element.attributeValue("id"));
                    childmap.put("key", name+"/"+element.attributeValue("id"));
                    childlist.add(childmap);
                }
                authNode.put("children", childlist);
            } catch (Exception e) {
                e.printStackTrace();
            }
            list.add(authNode);

        }


        return JSON.toJSONString(list);

    }
	@RequestMapping(value = "/execSelect/{selectClassName}/{selectID}", produces = "text/plain;charset=UTF-8")
	public String execSelect(@PathVariable("selectClassName") String selectClassName,
			@PathVariable("selectID") String selectID, @RequestBody String pJson){
		System.out.println("开始执行查询:" + "selectClassName:" + selectClassName + "," + "selectID:" + selectID + ","
				+ "pJson:" + pJson + ",");
		long t1 = System.nanoTime();
		JSONObject result = new JSONObject();
		try{
			JSONArray arr = JSON.parseArray(pJson);
			JSONObject params = arr.getJSONObject(0);//查询参数
			JSONObject page = null;
			if(arr.size()>1){
				page = arr.getJSONObject(1);  //分页对象
			}

            SelectService selectService=SelectService.Load(selectClassName,selectID);

			String selectType = selectService.getSelectType();
            String db =selectService.getDb();

			JSONArray jsonArray = params.getJSONArray("in");
			RowBounds bounds = null;
			if(page==null){
				bounds = RowBounds.DEFAULT;
			}else{
                int startIndex=page.getIntValue("startIndex");
                int perPage=page.getIntValue("perPage");
                if(startIndex==1 || startIndex==0){
                    startIndex=0;
                }else{
                    startIndex=(startIndex-1)*perPage;
                }
				bounds = new PageRowBounds(startIndex, perPage);
			}
			Map map = new HashMap();
			if(jsonArray!=null){
			    String value = null,key=null;
			    JSONObject aJsonObject = null;
    			for (int i = 0; i < jsonArray.size(); i++){
                        aJsonObject = (JSONObject) jsonArray.get(i);
                        System.err.println(aJsonObject);
                    //Iterator<String> male_Iterator = aJsonObject.keys();
                    java.util.Iterator it = aJsonObject.entrySet().iterator();

                    while(it.hasNext()) {
                        java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                        key=entry.getKey().toString(); //返回与此项对应的键
                        System.err.println(entry.getKey());
                        value=entry.getValue().toString(); //返回与此项对应的值
                        System.err.println(entry.getValue());
                    }
                    //value = aJsonObject.getString("value")!=null?aJsonObject.getString("value"):aJsonObject.getString("default");
                      map.put(key, value);

    			}
			}
            map.put("name",page.getString("searchResult"));
			map.putAll(this.getSelectSqlDataFilter(selectClassName,selectID));
			List<Map> aResult = new ArrayList<Map>();
			Long totalSize = 0L;
			if (selectType.equals("sql")){
				aResult = DbFactory.Open(db).selectList(selectClassName + "." + selectID, map, bounds);
				if(page!=null){
					totalSize = ((PageRowBounds)bounds).getTotal();
				}else{
					totalSize = Long.valueOf(aResult.size());
				}
			} 
			else if (selectType.equals("proc")){
			    DbFactory.Open(db).select(selectClassName + "." + selectID, map, null);
				aResult = (List<Map>) map.get("p_out_data");
			}else if(selectType.equals("http")){
                return invokeHttpService(selectService,map);
            }
			//将结果集Map的key值统一转换成大写形式
			List<Map> newResult = new ArrayList<Map>();
		    for(Map temp:aResult){
		    	newResult.add(transformUpperCase(temp));
		    }
		    aResult = null;
			result.put("list", newResult);
			result.put("metadata",selectService.getMetaData());
			result.put("totalSize", totalSize);
		}catch (Exception e){
			e.printStackTrace();
			return ExceptionMsg(e.getCause().getMessage());
		}

		long t2 = System.nanoTime();
		System.out.println("结束执行查询:" + "selectClassName:" + selectClassName + "," + "selectID:" + selectID + ","
				+ "pJson:" + pJson + ",\n" + "time:" + String.format("%.4fs", (t2 - t1) * 1e-9));
		return SuccessMsg("查询成功",result);  //JSON.toJSONString(result, features);

	}

    public String invokeHttpService(SelectService selectService,Map<String,String> map){
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<String> requestEntity = new HttpEntity<String>(JSON.toJSONString(map), headers);
        //  执行HTTP请求
        ResponseEntity<String> response = restTemplate.exchange(selectService.getMetaData().getString("url"),
                HttpMethod.POST,
                requestEntity,
                String.class);
        return response.getBody();
    }

	public Map<String, String> getSelectSqlDataFilter(String selectClassName, String selectID)
	{
	    Map<String,String> result = new HashMap<String,String>();
        JSONObject obj = new JSONObject();
        obj.put("namespace", selectClassName);
        obj.put("sqlid", selectID);
        JSONObject sqlObj = JSON.parseObject(qrySelectSqlDetail(obj.toJSONString()));
        JSONArray inArr = sqlObj.getJSONObject("comment").getJSONArray("in");
        List<Map<String,String>> inList = null;
        if(inArr!=null)
        {
            inList = new ArrayList<Map<String,String>>();
            JSONObject inObj = null;
            for(int i=0;i<inArr.size();i++)
            {
                inObj = inArr.getJSONObject(i);
                String id = inObj.getString("id");
                String auth = inObj.getString("auth");
                Map<String,String> map = null;
                if(auth!=null&&!auth.equals(""))
                {
                    map = new HashMap<String,String>();
                    map.put("id", id);
                    map.put("auth", auth);
                    inList.add(map);
                }
            }
        }
        UserModel user = SysContext.getRequestUser();
        Map<String, String> map = null;
        StringBuilder sb = null;
        for (Map<String,String> objMap:inList)
        {
            sb = new StringBuilder();
            map = new HashMap<String,String>();
            map.put("userName", user.getUserName());
            map.put("type", objMap.get("auth"));
            List<Map<String,String>> list = DbFactory.Open(DbFactory.FORM).selectList("rule.getAuthListByConditions", map);
            for (Map<String,String> temp:list)
            {
                sb.append("'"+temp.get("funcId")+"',");
            }
            if(list.size()>0)
            {
                result.put(objMap.get("id"), sb.substring(0, sb.length()-1));
            }
            else
            {
                result.put(objMap.get("id"), null);
            }
        }
        
	    return result;
	}
	
	@RequestMapping(value = "/execSelectAPP/{selectClassName}/{selectID}", produces = "text/plain;charset=UTF-8")
    public String execSelectAPP(@PathVariable("selectClassName") String selectClassName,
            @PathVariable("selectID") String selectID, @RequestBody String pJson) {
        System.out.println("开始执行查询:" + "selectClassName:" + selectClassName + "," + "selectID:" + selectID + ","
                + "pJson:" + pJson + ",");
        long t1 = System.nanoTime();

        List<Map> aResult = new ArrayList<>();
        List resList = new ArrayList<>();
        JSONObject result = new JSONObject();
        

        try {
            String aFileName = selectClassName + ".xml";
            String aNameSpace = getNameSpaceByFile(selectClassName);
            String selectType = getSelectType(selectClassName, selectID);

            String ajson = "";
            JSONArray arr = JSON.parseArray(pJson);
            JSONObject jsonObject = arr.getJSONObject(0);//查询参数
            JSONObject page = null;
            if(arr.size()>1)
            {
                page = arr.getJSONObject(1);  //分页对象
            }
            
            RowBounds bounds = null;
            if(page==null)
            {
                bounds = RowBounds.DEFAULT;
            }
            else
            {
                bounds = new PageRowBounds(page.getIntValue("startIndex"), page.getIntValue("perPage"));
            }

            // 根据名称，查找id
            // String selectName = jsonObject.getString("name");
            // String type = jsonObject.getString("type");
            String db=jsonObject.getString("db");
            JSONArray jsonArray = jsonObject.getJSONArray("in");
            Map map = new HashMap();
            if(jsonArray!=null)
            {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject aJsonObject = (JSONObject) jsonArray.get(i);
                    map.put(aJsonObject.getString("id"), aJsonObject.getString("value")!=null?aJsonObject.getString("value"):aJsonObject.getString("default"));
    
                }
            }
            Long totalSize = 0L;
            if (selectType.equals("sql")) {
                aResult = DbFactory.Open(db).selectList(selectClassName + "." + selectID, map, bounds);
                if(page!=null)
                {
                    totalSize = ((PageRowBounds)bounds).getTotal();
                }
                else
                {
                    totalSize = Long.valueOf(aResult.size());
                }
            } else if (selectType.equals("proc")) {
                DbFactory.Open(db).select(aNameSpace + "." + selectID, map, null);
                aResult = (List<Map>) map.get("p_out_data");
            }
            //将结果集Map的key值统一转换成大写形式
            List<Map> newResult = new ArrayList<Map>();
            for(Map temp:aResult){
                newResult.add(transformUpperCase(temp));
            }
            aResult = null;
            result.put("totalSize", totalSize);
            
            JSONArray outJsonArray = jsonObject.getJSONArray("out");
            for(Map r:newResult){
              List<Map> list = new ArrayList<>();
              JSONObject outParam = new JSONObject();
              for (int i = 0; i < outJsonArray.size(); i++) {
                  JSONObject aJsonObject = (JSONObject) outJsonArray.get(i);
                  JSONObject res = new JSONObject();
                  String name = r.get(aJsonObject.getString("id").toUpperCase())!=null?r.get(aJsonObject.getString("id").toUpperCase()).toString():"";
                  res.put("name", name);
                  list.add(res);
              }
              outParam.put("list", list);
              resList.add(outParam);
            }
            result.put("list", resList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        long t2 = System.nanoTime();
        System.out.println("结束执行查询:" + "selectClassName:" + selectClassName + "," + "selectID:" + selectID + ","
                + "pJson:" + pJson + ",\n" + "time:" + String.format("%.4fs", (t2 - t1) * 1e-9));
        return SuccessMsg("查询成功",result); //JSON.toJSONString(result);

    }
	
	@RequestMapping(value = "/getTemplateDefine/{selectClassName}/{selectName}", produces = "application/json;charset=utf-8")
	public String getTemplateDefine(@PathVariable("selectClassName") String selectClassName,
			@PathVariable("selectName") String selectName) {
		String aJson = null;
		// 根据名称查找对应的模板文件
		String usersqlPath = AppConstants.getUserSqlPath() + File.separator + selectClassName + ".xml";
		;

		try {
			SAXReader sax = new SAXReader();
			sax.setValidation(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);// 这一行是必须要有的
			// 获得dom4j的文档对象
			Document document = sax.read(new FileInputStream(usersqlPath));
			Element root = document.getRootElement();
			// 得到database节点
			List<Element> selects = root.selectNodes("//select/comment()");

			for (int i = 0; i < selects.size(); i++) {

				DefaultComment aComment = (DefaultComment) selects.get(i);

				aJson = aComment.getText();
				JSONObject aObject = (JSONObject) JSON.parse(aJson);

				String aSqlName = aObject.get("name").toString();
				if (aSqlName.equals(selectName)) {
					break;
				}
				aJson = null;
				// 注入类型节点

			}
			// String statementType = element.attributeValue("statementType");
			// if (statementType == null) {
			// map.put("type", "sql");
			// } else if (statementType.equals("CALLABLE")) {
			// map.put("type", "proc");
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}

		return aJson;

	}

	public String getSelectType(String selectClassName, String selectName) {
		String aJson = null;
		// 根据名称查找对应的模板文件
		String usersqlPath = AppConstants.getUserSqlPath() + File.separator + selectClassName + ".xml";
		;

		try {
			SAXReader sax = new SAXReader();
			sax.setValidation(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);// 这一行是必须要有的
			// 获得dom4j的文档对象
			Document document = sax.read(new FileInputStream(usersqlPath));
			Element root = document.getRootElement();
			// 得到database节点
			Element aSelect = (Element) root.selectSingleNode("//select[@id='" + selectName + "']");

			String statementType = aSelect.attributeValue("statementType");

			if (statementType == null) {
				return "sql";
			} else if (statementType.equals("CALLABLE")) {
				return "proc";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return aJson;

	}
    public String getSelectDB(String selectClassId, String selectID) {
        String result = "";
        // 根据名称查找对应的模板文件
        String usersqlPath = AppConstants.getUserSqlPath() + File.separator + selectClassId + ".xml";

        try {
            SAXReader sax = new SAXReader();
            sax.setValidation(false);
            sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);// 这一行是必须要有的
            // 获得dom4j的文档对象
            Document document = sax.read(new FileInputStream(usersqlPath));
            Element root = document.getRootElement();
            // 得到database节点
            // List<Element> selects = root.selectNodes("//select");
            // 得到database节点
            Element aSelect = (Element) root.selectSingleNode("//select[@id='" + selectID + "']");

            String aJsonString = "";
            for (int j = 0; j < aSelect.nodeCount(); j++) {
                Node node1 = aSelect.node(j);
                if (node1.getNodeTypeName().equals("Comment")) {
                    aJsonString = node1.getStringValue();
                    break;
                }
            }

            JSONObject commentObj = JSON.parseObject(aJsonString);
            //in参数中带lookup属性的不展示
            String db = commentObj.getString("db");
            return db;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }

    }

	private String getNameSpaceByFile(String selectClassName) {

		String aNameSpace = "";

		String usersqlPath = AppConstants.getUserSqlPath() + File.separator + selectClassName + ".xml";
		;

		SAXReader sax = new SAXReader();
		sax.setValidation(false);
		Document document = null;
		try {
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);// 这一行是必须要有的

			document = sax.read(new FileInputStream(usersqlPath));
			Element root = document.getRootElement();
			aNameSpace = root.attributeValue("namespace");
		} catch (Exception e) {

			e.printStackTrace();
		}

		return aNameSpace;
	}

	private static SerializerFeature[] features = { SerializerFeature.WriteNullNumberAsZero,
			SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue,
			SerializerFeature.PrettyFormat, SerializerFeature.UseISO8601DateFormat,
			SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty };

	public JSONObject getJsonByName(String pSqlName) {
		JSONObject aJson = null;
		try {
			SAXReader sax = new SAXReader();
			sax.setValidation(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);// 这一行是必须要有的
			// 获得dom4j的文档对象
			Document document = sax.read(getClass().getClassLoader().getResourceAsStream("/usersql/sql1.xml"));
			Element root = document.getRootElement();
			// 得到database节点
			List<Element> selects = root.selectNodes("//select/comment()");

			for (int i = 0; i < selects.size(); i++) {

				DefaultComment aComment = (DefaultComment) selects.get(i);

				String aJsonString = aComment.getText();
				aJson = (JSONObject) JSON.parse(aJsonString);

				String aSqlName = aJson.get("name").toString();
				if (aSqlName.equals(pSqlName)) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aJson;

	}

	// 根据名称返回json
	@RequestMapping(value = "/execTemplate", produces = "application/json;charset=utf-8")
	public String execTemplate(@RequestBody String pJson) {
		// System.out.println(pJson.getCharacterEncoding());

		List<Map> aResult = new ArrayList<>();
		String ajson = "";
		JSONObject jsonNameObject = (JSONObject) JSON.parse(pJson);
		JSONArray jsonNameArray = jsonNameObject.getJSONArray("params");

		// 根据名称，查找id
		String selectName = jsonNameObject.getString("name");
		JSONObject jsonIdObject = getJsonByName(selectName);
		String selectID = jsonIdObject.getString("id");

		JSONArray jsonIdArray = jsonIdObject.getJSONArray("params");
		Map map = new HashMap();
		for (int i = 0; i < jsonIdArray.size(); i++) {
			JSONObject aJsonObject = (JSONObject) jsonIdArray.get(i);
			String id = aJsonObject.getString("id");
			String name = aJsonObject.getString("name");
			String value = "";// jsonNameArray.get
			for (int j = 0; j < jsonNameArray.size(); j++) {
				if (((JSONObject) jsonIdArray.get(j)).getString("name").equals(name)) {
					value = ((JSONObject) jsonNameArray.get(j)).getString("value");
					break;
				}
			}
			map.put(id, value);
		}
		aResult = DbFactory.Open(DbFactory.SYSTEM).selectList("sql1." + selectID, map);
		return JSON.toJSONString(aResult, features);

	}

	// 执行SQL
	@RequestMapping(value = "/execSelect", produces = "text/plain;charset=UTF-8")
	public String execSelect(@RequestBody String pJson) {

		JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
		String selectID = jsonObject.getString("select");

		Connection conn = DbFactory.Open(DbFactory.SYSTEM).getConnection();

		Statement stmt; // 获取Statement
		try {
			stmt = conn.createStatement(); // 实例化Statement对象
			stmt.executeQuery(selectID); // 执行查询语句
			ResultSet rs = stmt.getResultSet(); // 获取查询结果集
			DbFactory.close(DbFactory.SYSTEM);
			return JsonUtil.resultSetToJson(rs);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return "error";

	}

	// 创建一个SelectSQL
	@RequestMapping(value = "/CreateSelect", produces = "text/plain;charset=UTF-8")
	public String CreateSelect(@RequestBody String pJson) {

		JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
		String selectID = jsonObject.getString("select");

		Connection conn = DbFactory.Open(DbFactory.SYSTEM).getConnection();

		Statement stmt; // 获取Statement
		try {
			stmt = conn.createStatement(); // 实例化Statement对象
			stmt.executeQuery(selectID); // 执行查询语句
			ResultSet rs = stmt.getResultSet(); // 获取查询结果集
			DbFactory.close(DbFactory.SYSTEM);
			return JsonUtil.resultSetToJson(rs);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return "error";

	}
	
	
    @RequestMapping(value="/getSelectTree" , produces = "text/plain;charset=UTF-8")
    public String getSelectTree()
    {  
        String usersqlPath = AppConstants.getUserSqlPath();
        File file = new File(usersqlPath);
        File[] fileList = file.listFiles(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name)
            {
                if(name.endsWith(".xml"))
                {
                    return true;
                }
                return false;
            }}
        );
        
        SAXReader reader = null;
        Document document = null;
        DefaultComment content = null;
        Element element = null;
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        try
        {
            for (int i = 0; i < fileList.length; i++)
            {
                try
                {
                    obj = new JSONObject();
                    document = XmlUtil.parseXmlToDom(fileList[i]);
                    log.debug("解析文件:"+fileList[i].getName());
                    //content = (DefaultComment)document.content().get(0);
                    //obj.put("name", content.getText());
                    element = (Element)(document.selectNodes("mapper").get(0));
                    obj.put("name", element.attributeValue("namespace"));
                    Element selElement = null;
                    DefaultComment selContent = null;
                    List<Object> list = null;
                    Object object = null;
                    JSONObject selObj = null;
                    JSONArray selArray = new JSONArray();
                    List<Element> selList = document.selectNodes("mapper/select");
                    
                    for (int j = 0; j < selList.size(); j++)
                    {
                        selElement = selList.get(j);
                        list = selElement.content();
                        for (int k = 0; k < list.size(); k++)
                        {
                            object = list.get(k);
                            if(object instanceof DefaultComment)
                            {
                                selContent = (DefaultComment)object;
                                selObj = JSONObject.parseObject(selContent.getText());
                                selArray.add(selObj);
                            }
                        }
                    }
                    obj.put("list", selArray);
                    array.add(obj);
                }
                catch(DocumentException e)
                {
                    log.error("解析"+fileList[i].getName()+"出现错误.");
                    return new JSONArray().toJSONString();
                }
            }
        }
        catch(SAXException e)
        {
            log.error("SAX解析错误.");
            return new JSONArray().toJSONString();
        }
        return array.toJSONString();
    }

	@RequestMapping(value = "/qrySelectSqlDetail", produces = "text/plain;charset=UTF-8")
	public String qrySelectSqlDetail(@RequestBody String pJson) {
		JSONObject obj = new JSONObject();
		try {
			JSONObject pObj = (JSONObject) JSON.parse(pJson);
			String namespace = pObj.getString("namespace");
			String sqlid = pObj.getString("sqlid");
			String sqlPath = AppConstants.getUserSqlPath() + File.separator + namespace + ".xml";
			Document doc = XmlUtil.parseXmlToDom(sqlPath);
			Element select = (Element) doc.selectSingleNode("/mapper/select[@id='" + sqlid + "']");
			obj.put("namespace", namespace);
			List<Object> list = select.content();
			Object object = null;
			DefaultComment selContent = null;
			String text = "";
			for (int i = 0; i < list.size(); i++) {
				object = list.get(i);
				if (object instanceof DefaultComment) {
					selContent = (DefaultComment) object;
					obj.put("comment", JSON.parse(selContent.getText()));
				}else{
					text+=((Node)object).asXML().replaceAll("&gt;", ">").replaceAll("&lt;", "<");
				}
			}
			obj.put("cdata", text);
		} catch (Exception e) {
			log.error("查询报表信息异常.");
			e.printStackTrace();
		}
		return obj.toJSONString();
	}
	@RequestMapping(value = "/getSelectItem/{searchValue}", produces = "text/plain;charset=UTF-8")
    public String getSelectItem(@PathVariable("searchValue") String searchValue)
    {
	    String result = "";
	    String usersqlPath = AppConstants.getUserSqlPath();
        File file = new File(usersqlPath);
        File[] fileList = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        });
        // 构造返回json
        List<Map> list = new ArrayList<Map>();

        for (int i = 0; i < fileList.length; i++) {

            String filename = fileList[i].getName();
            String name = filename.substring(0, filename.lastIndexOf("."));
            
            // 根据名称查找对应的模板文件
            String sqlPath = AppConstants.getUserSqlPath() + File.separator + name + ".xml";

            try {
                SAXReader sax = new SAXReader();
                sax.setValidation(false);
                sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);// 这一行是必须要有的
                // 获得dom4j的文档对象
                Document document = sax.read(new FileInputStream(sqlPath));
                Element root = document.getRootElement();
                // 得到database节点
                List<Element> selects = root.selectNodes("//select");

                for (int j = 0; j < selects.size(); j++) {
                    Map<String, String> map = new HashMap<String, String>();
                    Element element = selects.get(j);
                    String selectId =element.attributeValue("id");
                    if(selectId.indexOf(searchValue)!=-1){
                        map.put("name", name);
                        map.put("selectId", selectId);
                        list.add(map);
                    }
                }
                //result = JSON.toJSONString(list);
            }catch (Exception ex) {
                ex.printStackTrace();
                return ErrorMsg("2000","查询失败");
            }
        }
        //return result;
        return SuccessMsg("查询成功",list);
    }
	
	private  Map<String, Object> transformUpperCase(Map<String, Object> aResult)
    {
        Map<String, Object> resultMap = new HashMap<>();
        if (aResult == null || aResult.isEmpty()){
            return resultMap;
        }
        Set<String> keySet = aResult.keySet();
        for (String key : keySet){
            String newKey = key.toUpperCase();
            resultMap.put(newKey, aResult.get(key));
        }
        return resultMap;
    }
}
