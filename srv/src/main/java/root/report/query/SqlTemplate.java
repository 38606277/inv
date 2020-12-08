package root.report.query;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import root.report.util.XmlUtil;

public class SqlTemplate {
	
	String template = null;
	Document doc=null;
	Element select=null;
	JSONObject comment=null;

	private String namespace;
	private String id;
	private String db;
	private String selectType;
	private JSONArray in;
	private JSONArray out;
	private String sql;
	private String cached;
	private String qryCursorName;
	private String qryHttpUrl;
	private String qryHttpHeader;
	private String qryHttpResBodyArrayName;
	public  SqlTemplate() {}

	public  SqlTemplate(String templateFileName,String selectId) {
		try {
			//解析xml文件
			doc = XmlUtil.parseXmlToDom(templateFileName);
			//查找到Select节点
			select = (Element) doc.selectSingleNode("//select[@id='" + selectId + "']");
			//查找到说明comment节点
			String aJsonString = "";
			for (int j = 0; j < select.nodeCount(); j++) {
				Node node1 = select.node(j);
				if (node1.getNodeTypeName().equals("Comment")) {
					aJsonString = node1.getStringValue();
					break;
				}
			}
			comment=(JSONObject) JSONObject.parse(aJsonString);
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		this.id=selectId;
	}
	
	public String getNamespace() {
		if (namespace==null)
		{
			Element root = doc.getRootElement();
		    namespace= root.attributeValue("namespace");
		}
		return namespace;
	}
	public String getId() {
		return id;
	}

	public String getDb() {
		
		return  comment.getString("db");
		
	}
	public String getSelectType() {
		
		if (selectType==null)
		{
			String statementType = select.attributeValue("statementType");
			if (statementType == null) {
				selectType= "sql";
			} else if (statementType.equals("CALLABLE")) {
				selectType= "proc";
			}
		}
		
		return selectType;
	}
	public JSONArray getIn() {
		
       if (in==null)
    	 in= comment.getJSONArray("in");
		
		return in;
	}
	public JSONArray getOut() {

	   if(out==null)
		  out=comment.getJSONArray("out");
		
		return out;
	}

	// 开辟get set方法 供新接口能赋值对象
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setDb(String db) {
		this.db = db;
		if(comment == null ){
			comment = new JSONObject();
			comment.put("db",db);
		}else {
			comment.put("db",db);
		}

	}

	public void setSelectType(String selectType) {
		this.selectType = selectType;
	}

	public void setIn(JSONArray in) {
		this.in = in;
	}

	public void setOut(JSONArray out) {
		this.out = out;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
	public String getCached() {
		return cached;
	}

	public void setCached(String cached) {
		this.cached = cached;
	}
	public String getQryCursorName() {
		return qryCursorName;
	}

	public void setQryCursorName(String qryCursorName) {
		this.qryCursorName = qryCursorName;
	}


	public String getQryHttpUrl() {
		return qryHttpUrl;
	}

	public void setQryHttpUrl(String qryHttpUrl) {
		this.qryHttpUrl = qryHttpUrl;
	}
	public String getQryHttpHeader() {
		return qryHttpHeader;
	}

	public void setQryHttpHeader(String qryHttpHeader) {
		this.qryHttpHeader = qryHttpHeader;
	}
	public String getQryHttpResBodyArrayName() {
		return qryHttpResBodyArrayName;
	}

	public void setQryHttpResBodyArrayName(String qryHttpResBodyArrayName) {
		this.qryHttpResBodyArrayName = qryHttpResBodyArrayName;
	}
}
