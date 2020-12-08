package root.report.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultCDATA;
import org.dom4j.tree.DefaultComment;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import root.configure.AppConstants;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.service.FunctionService;
import root.report.util.XmlUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/reportServer/function")
public class FunctionControl extends RO{

	private static Logger log = Logger.getLogger(FunctionControl.class);

	@Autowired
	private FunctionService functionService;

	@RequestMapping(value = "/getAllFunctionName", produces = "text/plain;charset=UTF-8")
	public String getAllFunctionName() {

		return "";//functionService.getAllFunctionName();

	}


	@RequestMapping(value = "/getFunctionByID/{func_id}", produces = "text/plain;charset=UTF-8")
	public String getFunctionByID(@PathVariable("func_id") String func_id) {


		try{
			JSONObject jsonObject=functionService.getFunctionByID(func_id);
			return  SuccessMsg("",jsonObject);
		}catch (Exception ex){
			return ExceptionMsg(ex.getMessage());

		}

	}
	@RequestMapping(value = "/getFunctionByName/{func_Name}", produces = "text/plain;charset=UTF-8")
	public String getFunctionByName(@PathVariable("func_Name") String func_Name) {


		try{
			JSONObject jsonObject=functionService.getFunctionByID(func_Name);
			return  SuccessMsg("",jsonObject);
		}catch (Exception ex){
			return ExceptionMsg(ex.getMessage());

		}

	}

	@RequestMapping(value = "/getFunctionClass", produces = "text/plain;charset=UTF-8")
	public String getFunctionClass() {
		String usersqlPath = AppConstants.getUserFunctionPath();
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
            // 根据名称查找对应的模板文件
            String sqlPath = AppConstants.getUserFunctionPath() + File.separator + name + ".xml";

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
	@RequestMapping(value = "/getFunctionClassReact", produces = "text/plain;charset=UTF-8")
	public String getFunctionClassReact() {
		String usersqlPath = AppConstants.getUserFunctionPath();
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
			// 根据名称查找对应的模板文件
			String sqlPath = AppConstants.getUserFunctionPath() + File.separator + name + ".xml";

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
	// 取所有报表基本信息
	@RequestMapping(value = "/getFunctionName/{FunctionClass}", produces = "text/plain;charset=UTF-8")
	public String getFunctionName(@PathVariable("FunctionClass") String selectClassName) {
		String result = "";
		// 根据名称查找对应的模板文件
		String usersqlPath = AppConstants.getUserFunctionPath() + File.separator + selectClassName + ".xml";
		;

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

				// 取出id
				Map<String, String> map = new HashMap<String, String>();
				map.put("name", element.attributeValue("id"));
				// 取出db和描述信息
				String aJsonString = "";
				for (int j = 0; j < element.nodeCount(); j++) {
					Node node1 = element.node(j);
					if (node1.getNodeTypeName().equals("Comment")) {
						aJsonString = node1.getStringValue();
						break;
					}
				}
				JSONObject jsonObject = (JSONObject) JSON.parse(aJsonString);

				String selectType=jsonObject.getString("type");
				map.put("type",jsonObject.getString("type"));
				map.put("desc", jsonObject.getString("desc"));

				if (selectType==null||selectType.equals("sql")||selectType.equals("proc"))
				{
					map.put("db", jsonObject.getString("db"));
				}



				list.add(map);
			}
			result = JSON.toJSONString(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

	// 根据SQLID 取入参 出参信息
	@RequestMapping(value = "/getFunctionParam/{FunctionClassId}/{FunctionID}", produces = "text/plain;charset=UTF-8")
	public String getFunctionParam(@PathVariable("FunctionClassId") String FunctionClassId,
			@PathVariable("FunctionID") String FunctionID) {
		String result = "";
		try {
			// 执行函数
			String usersqlPath = AppConstants.getUserFunctionPath() + File.separator + FunctionClassId
					+ ".xml";
			SqlTemplate template = new SqlTemplate(usersqlPath, FunctionID);
			// 输入参数放入map中
			return template.comment.toJSONString();

		} catch (Exception ex) {
			ex.printStackTrace();

		}
		return result;
	}

	@RequestMapping(value = "/saveUserSql", produces = "text/plain;charset=UTF-8")
    public String saveUserSql(@RequestBody String pJson)
    {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
			JSONObject jsonObject = (JSONObject) JSON.parse(pJson,Feature.OrderedField);
        	String namespace = jsonObject.getString("namespace");
			String sqlId = jsonObject.getString("id");
            JSONObject commonObj = jsonObject.getJSONObject("comment");
			String type = commonObj.getString("type");
			String userSqlPath =AppConstants.getUserFunctionPath()+File.separator + namespace + ".xml";

            OutputFormat format = OutputFormat.createPrettyPrint();
			format.setSuppressDeclaration(true);
			format.setIndentSize(2);
			format.setNewlines(true);
			format.setTrimText(false);

            XMLWriter writer = null;
            Document userDoc = XmlUtil.parseXmlToDom(userSqlPath);
            boolean checkResult = checkIsContainsSqlId(userDoc, sqlId);
            if(checkResult) return ExceptionMsg("已经存在相同的报表ID");
            Element root = (Element)userDoc.selectSingleNode("/mapper");
            Element newSql = root.addElement("select");
            newSql.addAttribute("id", sqlId);
            if("sql".equals(type)){
                newSql.addAttribute("resultType", "BigDecimal");
				newSql.addAttribute("parameterType", "Map");
				newSql.addComment(JSONObject.toJSONString(commonObj, features)+"\n");
				String cdata = jsonObject.getString("cdata");
				addSqlText(newSql,cdata);
            }else if ("proc".equals(type)){
                newSql.addAttribute("statementType", "CALLABLE");
				newSql.addComment(JSONObject.toJSONString(commonObj, features)+"\n");
				String cdata = jsonObject.getString("cdata");
				addSqlText(newSql,cdata);
            }else if("http".equals(type)) {
				newSql.addComment(JSONObject.toJSONString(commonObj, features)+"\n");
            }

            log.debug("新增SQL:"+newSql.asXML());
            writer = new XMLWriter(new FileOutputStream(userSqlPath), format);
            //删除空白行
            Element rootEle = userDoc.getRootElement();
            removeBlankNewLine(rootEle);
            writer.write(userDoc);
            writer.flush();
            writer.close();
            //
			// functionService.insertRecordsToFunc(jsonObject,sqlSession);

            //重置该DB连接
            DbFactory.init(commonObj.getString("db"));
        }catch (Exception e){
			Throwable cause = e;
			String message = null;
			while((message = cause.getMessage())==null){
				cause = cause.getCause();
			}

			return ExceptionMsg(message);
        }finally {
            try {
                sqlSession.getConnection().setAutoCommit(true);
            }catch(Exception e) {
                Throwable cause = e;
                String message = null;
                while((message = cause.getMessage())==null){
                    cause = cause.getCause();
                }
                return ExceptionMsg(message);
            }
		}

        return SuccessMsg("新增报表成功",null);
    }

	@RequestMapping(value = "/saveUserSql/V2", produces = "text/plain;charset=UTF-8")
	public String saveUserSqlV2(@RequestBody String pJson) throws SQLException {
		SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
		try{
			//写入fnc_name,写入func_in,func_out
			sqlSession.getConnection().setAutoCommit(false);
			//写入配置文件

			JSONObject jsonObject = (JSONObject) JSON.parse(pJson,Feature.OrderedField);
			String namespace = jsonObject.getString("namespace");
			String sqlId = jsonObject.getString("id");
			JSONObject commonObj = jsonObject.getJSONObject("comment");
			String type = commonObj.getString("type");
			String userSqlPath =AppConstants.getUserFunctionPath()+File.separator + namespace + ".xml";

			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setSuppressDeclaration(true);
			format.setIndentSize(2);
			format.setNewlines(true);
			format.setTrimText(false);

			XMLWriter writer = null;
			Document userDoc = XmlUtil.parseXmlToDom(userSqlPath);
			boolean checkResult = checkIsContainsSqlId(userDoc, sqlId);
			if(checkResult) return ExceptionMsg("已经存在相同的报表ID");
			Element root = (Element)userDoc.selectSingleNode("/mapper");
			Element newSql = root.addElement("select");
			newSql.addAttribute("id", sqlId);
			if("sql".equals(type)){
				newSql.addAttribute("resultType", "BigDecimal");
				newSql.addAttribute("parameterType", "Map");
				newSql.addComment(JSONObject.toJSONString(commonObj, features)+"\n");
				String cdata = jsonObject.getString("cdata");
				addSqlText(newSql,cdata);
			}else if ("proc".equals(type)){
				newSql.addAttribute("statementType", "CALLABLE");
				newSql.addComment(JSONObject.toJSONString(commonObj, features)+"\n");
				String cdata = jsonObject.getString("cdata");
				addSqlText(newSql,cdata);
			}else if("http".equals(type)) {
				newSql.addComment(JSONObject.toJSONString(commonObj, features)+"\n");
			}

			log.debug("新增SQL:"+newSql.asXML());
			writer = new XMLWriter(new FileOutputStream(userSqlPath), format);
			//删除空白行
			Element rootEle = userDoc.getRootElement();
			removeBlankNewLine(rootEle);
			writer.write(userDoc);
			writer.flush();
			writer.close();

			// functionService.insertRecordsToFunc(jsonObject,sqlSession);
			sqlSession.getConnection().commit();
			//重置该DB连接
			DbFactory.init(commonObj.getString("db"));
		}catch (Exception e){
			sqlSession.getConnection().rollback();
			return ExceptionMsg(e.getMessage());
		}
		return SuccessMsg("新增报表成功",null);
	}

	@RequestMapping(value = "/modifyUserSql", produces = "text/plain;charset=UTF-8")
    public String modifyUserSql(@RequestBody String pJson) throws SQLException {
		SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
			sqlSession.getConnection().setAutoCommit(false);
			JSONObject jsonObject = (JSONObject) JSON.parse(pJson,Feature.OrderedField);
            String namespace = jsonObject.getString("namespace");
            JSONObject commonObj = jsonObject.getJSONObject("comment");
			String type = commonObj.getString("type");
            String sqlId = jsonObject.getString("id");
            String cdata = jsonObject.getString("cdata");
            String userSqlPath =AppConstants.getUserFunctionPath()+File.separator + namespace + ".xml";
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            format.setTrimText(false);
            format.setIndent(false);
            XMLWriter writer = null;
            Document userDoc = XmlUtil.parseXmlToDom(userSqlPath);

            Element select = (Element)userDoc.selectSingleNode("//select[@id='"+sqlId+"']");
            select.clearContent();
            select.addComment(JSONObject.toJSONString(commonObj, features)+"\n");
            addSqlText(select, cdata);
            log.debug("修改报表:"+select.asXML());
            writer = new XMLWriter(new FileOutputStream(userSqlPath), format);
            //删除空白行
            Element root = userDoc.getRootElement();
            removeBlankNewLine(root);
            writer.write(userDoc);
            writer.flush();
            writer.close();
			// 往func_name,func_in,func_out当中插入对应记录
			// functionService.insertRecordsToFunction(jsonObject,sqlSession);
			sqlSession.getConnection().commit();
            DbFactory.init(commonObj.getString("db"));
        }catch (Exception e){
			sqlSession.getConnection().rollback();
			return ExceptionMsg(e.getMessage());
        }
        return SuccessMsg("修改报表成功",null);
    }
	//删除报表
	@RequestMapping(value = "/moveUserSql", produces = "text/plain;charset=UTF-8")
	public String moveUserSql(@RequestBody String pJson) throws SQLException {
		SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
		try{
			sqlSession.getConnection().setAutoCommit(false);
			JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
			String namespace = jsonObject.getString("namespace");
			String sqlId = jsonObject.getString("id");
			String userSqlPath =AppConstants.getUserFunctionPath()+File.separator + namespace + ".xml";

			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			format.setTrimText(false);
			format.setIndent(false);
			XMLWriter writer = null;
			Document userDoc = XmlUtil.parseXmlToDom(userSqlPath);

			//重置DB连接
			JSONObject newObj = new JSONObject();
			newObj.put("namespace", namespace);
			newObj.put("sqlid", sqlId);

			// this.functionService.deleteRecordsToFunction(jsonObject,sqlSession);

			JSONObject selectObj = JSONObject.parseObject(this.qryFunctionDetail(newObj.toJSONString()));
			sqlSession.getConnection().commit();
			DbFactory.init(selectObj.getJSONObject("comment").getString("db"));

			//删除该节点
			moveSqlId(userDoc,sqlId);

			log.debug("删除报表:命名空间【"+namespace+"】,报表ID【"+sqlId+"】");
			writer = new XMLWriter(new FileOutputStream(userSqlPath), format);
			//删除空白行
			Element root = userDoc.getRootElement();
			removeBlankNewLine(root);
			writer.write(userDoc);
			writer.flush();
			writer.close();


		} catch (Exception e) {
			sqlSession.getConnection().rollback();
			return ErrorMsg("3000", e.getMessage());
		}
		return SuccessMsg("操作成功", null);
	}

	// 查找所有func_class
	@RequestMapping(value = "/getAllFunctionClassInfo", produces = "text/plain;charset=UTF-8")
	public String getAllFunctionClassInfo(){
		SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
		List<Map<String,String>> list = this.functionService.getAllFunctionClass(sqlSession);
		return SuccessMsg("",JSONObject.toJSON(list));
	}

	/*// 往fucn_class这张表插入一条记录
	@RequestMapping(value = "/addFunctionClassInfo", produces = "text/plain;charset=UTF-8")
	public String addFunctionClassInfo(@RequestBody String pJson){
		SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
		JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
		String class_name = jsonObject.getString("class_name");
		int flag = this.functionService.addFunctionClass(class_name,sqlSession);
		if(flag!=1){
			return ErrorMsg("","插入数据失败");
		}
		return SuccessMsg("插入数据成功",null);
	}

    // 往fucn_class这张表删除一条记录
    @RequestMapping(value = "/deleteFunctionClassInfo", produces = "text/plain;charset=UTF-8")
    public String deleteFunctionClassInfo(@RequestBody String pJson){
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
        int class_id = jsonObject.getInteger("class_id");
        // TODO 先要关联查询func_name表，如果存在关联记录，则不允许删除此
        int flag = this.functionService.deleteFunctionClass(class_id,sqlSession);
        if(flag!=1){
            return ErrorMsg("3000","删除数据失败");
        }
        return SuccessMsg("删除数据成功",null);
    }

	// 往fucn_class这张表修改一条记录
	@RequestMapping(value = "/updateFunctionClassInfo", produces = "text/plain;charset=UTF-8")
	public String updateFunctionClassInfo(@RequestBody String pJson){
		SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
		JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
		String class_name = jsonObject.getString("class_name");
		int class_id = jsonObject.getInteger("class_id");
		int flag = this.functionService.updateFunctionClass(class_id,class_name,sqlSession);
		if(flag!=1){
			return ErrorMsg("3000","修改数据失败");
		}
		return SuccessMsg("修改数据成功",null);
	}*/

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
							if(textOnly){
								value+="\n";
							}
						 }else{
							 value = value.trim()+"\n";
						 }
						 text.setText(value);
						 break;
					 default:break;
				 }
		 }
    }

	@RequestMapping(value = "/execFunction/{FunctionClassName}/{FunctionID}", produces = "text/plain;charset=UTF-8")
	public String execFunction(@PathVariable("FunctionClassName") String FunctionClassName,
			@PathVariable("FunctionID") String FunctionID, @RequestBody String pJson) {
		System.out.println("开始执行查询:" + "selectClassName:" + FunctionClassName + "," + "selectID:" + FunctionID + ","
				+ "pJson:" + pJson + ",");
		long t1 = System.nanoTime();
		Object aResult = null;
		try {

			// 检查函数名是否存在

			//检查参数

			// 执行函数
			String usersqlPath = AppConstants.getUserFunctionPath() + File.separator + FunctionClassName
					+ ".xml";
			SqlTemplate template = new SqlTemplate(usersqlPath, FunctionID);
			// 输入参数放入map中
			JSONArray inTemplate = template.getIn();
			JSONArray inValue = JSONArray.parseArray(pJson);

			Map<String,Object> map = new LinkedHashMap<String,Object>();
			Map<String,Boolean> dataParam = new HashMap<String,Boolean>();
			if (inTemplate != null) {
				for (int i = 0; i < inTemplate.size(); i++) {
					JSONObject aJsonObject = (JSONObject) inTemplate.get(i);
					String id = aJsonObject.getString("id");
					map.put(id, inValue.getString(i));
					Boolean inFormula = aJsonObject.getBoolean("in_formula");
					dataParam.put(id, inFormula);
				}
			}
			Map<String,Object> funcParamMap = new HashMap<String,Object>();
			List<FuncMetaData> list = new ArrayList<FuncMetaData>();
			acquireFuncMetaData(list,map,funcParamMap,dataParam);
			if(list.size()!=0){
				aResult = excuteFunc(list,0,funcParamMap,template);
			}else{
				if(template.getSelectType().equals("sql")) {
					String db = template.getDb();
					String namespace = template.getNamespace();
					String funcId = template.getId();
					aResult = DbFactory.Open(db).selectOne(namespace + "." + funcId, map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			aResult=e.getMessage();
		}

		long t2 = System.nanoTime();
		System.out.println("结束执行查询:" + "FunctionClassName:" + FunctionClassName + "," + "selectID:" + FunctionID + ","
				+ "pJson:" + pJson + ",\n" + "time:" + String.format("%.4fs", (t2 - t1) * 1e-9));
		return JSON.toJSONString(aResult, features);

	}

	private BigDecimal excuteFunc(List<FuncMetaData> list,int index,Map<String,Object> paramMap,SqlTemplate template) throws Exception{
		BigDecimal sum = null;
		int size = list.size();
		FuncMetaData meta = list.get(index);
		String[] paramVal = meta.getParamVal();
		String id = meta.getId();
		String expression = meta.getFuncExpression();
		for(String s:paramVal){
			paramMap.put(id, s);
			if(index<size-1){
				sum = excuteFunc(list,index+1,paramMap,template);
			}else{
				if(template.getSelectType().equals("sql")) {
					String db = template.getDb();
					String namespace = template.getNamespace();
					String funcId = template.getId();
					sum = DbFactory.Open(db).selectOne(namespace + "." + funcId, paramMap);
				}
			}
			expression = expression.replace(s, sum.toString());
		}
		Object result = null;
		try{
			Expression exp = AviatorEvaluator.compile(expression);
			result = exp.execute();
		}catch(Exception e){
			throw new Exception("参数表达式不合法");
		}
		return new BigDecimal(result.toString()).setScale(2,BigDecimal.ROUND_HALF_UP);
	}
	//获取函数的元数据
	private void acquireFuncMetaData(List<FuncMetaData> list,Map<String,Object> map,Map<String,Object> funcParamMap,Map<String,Boolean> dateParam){
		Set<String> keys = map.keySet();
		for (String key:keys) {
			String value = (String) map.get(key);
			Boolean inFormula = dateParam.get(key);
			if(inFormula!=null&&inFormula){
				FuncMetaData meta = new FuncMetaData();
				meta.setId(key);
				meta.setFuncExpression(value);
				String[] arr = value.split("\\+|\\-|\\*|\\/|\\(|\\)");
				List<String> tempList = new ArrayList<String>();
				for(String temp:arr){
					if(temp!=null&&!temp.trim().equals("")){
						tempList.add(temp.trim());
					}
				}
				String[] paramVal = new String[tempList.size()];
				tempList.toArray(paramVal);
				meta.setParamVal(paramVal);
				list.add(meta);
			}else{
				funcParamMap.put(key, map.get(key));
			}
		}
	}

	private static SerializerFeature[] features = { SerializerFeature.WriteNullNumberAsZero,
			SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue,
			SerializerFeature.PrettyFormat, SerializerFeature.UseISO8601DateFormat,
			SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty };

	@RequestMapping(value = "/qryFunctionDetail", produces = "text/plain;charset=UTF-8")
	public String qryFunctionDetail(@RequestBody String pJson) {
		JSONObject obj = new JSONObject();
		try {
			JSONObject pObj = (JSONObject) JSON.parse(pJson);
			String namespace = pObj.getString("namespace");
			String sqlid = pObj.getString("sqlid");
			String category = pObj.getString("category");
			String sqlPath =AppConstants.getUserFunctionPath()+File.separator + namespace + ".xml";
			Document doc = XmlUtil.parseXmlToDom(sqlPath);
			Element select = (Element) doc.selectSingleNode("/mapper/select[@id='" + sqlid + "']");
			obj.put("namespace", namespace);
			List<Object> list = select.content();
			Object object = null;
			DefaultComment selContent = null;
			DefaultCDATA selCdata = null;
			String text = "";
			for (int i = 0; i < list.size(); i++) {
				object = list.get(i);
				if (object instanceof DefaultComment) {
					selContent = (DefaultComment) object;
					obj.put("comment", JSON.parse(selContent.getText()));
				}else{
					text+=((Node)object).asXML();
				}
			}
			obj.put("cdata", text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj.toJSONString();
	}

	@RequestMapping(value = "/getAllFunctionClass", produces = "text/plain;charset=UTF-8")
	public String getAllFunctionClass()
    {
        String usersqlPath = AppConstants.getUserFunctionPath();
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
	@RequestMapping(value = "/getFunctionAuthList/{userName}", produces = "text/plain;charset=UTF-8")
    public String getFunctionAuthList(@PathVariable("userName") String userName) {
        try{
            Map<String,String> map = new HashMap<String,String>();
            map.put("userName",userName);
            int isAdmin =  DbFactory.Open(DbFactory.FORM).selectOne("user.isAdmin",userName);
            if(isAdmin == 1){
                return this.getAllFunctionClass();
            }else{
                List<Map> functionAuthList = DbFactory.Open(DbFactory.FORM).selectList("rule.getFunctionAuthList",map);
                return SuccessMsg("查询成功", functionAuthList);
            }


        }catch(Exception ex)
        {
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }
    @RequestMapping(value = "/getFunctionAuthListByClass/{userName}/{className}", produces = "text/plain;charset=UTF-8")
    public String getFunctionAuthListByClass(@PathVariable("userName") String userName,@PathVariable("className") String className) {

        String result = "";
         try {
        Map<String,String> cmap = new HashMap<String,String>();
        cmap.put("userName",userName);
        cmap.put("className",className);
        int isAdmin =  DbFactory.Open(DbFactory.FORM).selectOne("user.isAdmin",userName);
        List<Map> functionAuthList = DbFactory.Open(DbFactory.FORM).selectList("rule.getFunctionAuthListByClass",cmap);
        for (Map functionAuth : functionAuthList) {
            String[] str = functionAuth.get("name").toString().split("/");
            functionAuth.put("name", str[str.length-1]);
        }
     // 根据名称查找对应的模板文件
        String usersqlPath = AppConstants.getUserFunctionPath() + File.separator + className + ".xml";

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
			for (Map auth : functionAuthList) {
				for (Map l : list) {
					if(l.get("name").equals(auth.get("name"))){
						authMap.add(l);
					}
				}
			}
			return SuccessMsg("查询成功",authMap);
		}
        } catch (Exception e) {
            e.printStackTrace();
            return ErrorMsg("3000", e.getMessage());
        }

    }

}
