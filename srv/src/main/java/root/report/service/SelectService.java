package root.report.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import root.configure.AppConstants;

import java.io.File;
import java.io.FileInputStream;

public class SelectService {

    private JSONObject metaData=null;

    public Element getSelectElement() {
        return selectElement;
    }

    public void setSelectElement(Element selectElement) {
        this.selectElement = selectElement;
    }

    private Element selectElement=null;
    public JSONObject getMetaData() {
        return metaData;
    }

    public void setMetaData(JSONObject metaData) {
        this.metaData = metaData;
    }

    public static  SelectService Load(String className,String selectID){

        String result = "";
        // 根据名称查找对应的模板文件
        String usersqlPath = AppConstants.getUserSqlPath() + File.separator + className + ".xml";
        JSONObject commentObj=null;
        Element aSelect=null;
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
            aSelect = (Element) root.selectSingleNode("//select[@id='" + selectID + "']");

            String aJsonString = "";
            for (int j = 0; j < aSelect.nodeCount(); j++) {
                Node node1 = aSelect.node(j);
                if (node1.getNodeTypeName().equals("Comment")) {
                    aJsonString = node1.getStringValue();
                    break;
                }
            }

            commentObj = JSON.parseObject(aJsonString);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SelectService selectService=new SelectService();
        selectService.setMetaData(commentObj);
        selectService.setSelectElement(aSelect);


        return  selectService;
    }

    public String  getAll(){
      return  "";
    }
    public String  getSQL()
    {
      return  "";
    }

    public String  getDb()
    {

        String db=metaData.getString("db");
        return db;
    }
    public String getSelectType()
    {
        String statementType = metaData.getString("type");
        if(statementType==null){
            statementType = this.getSelectElement().attributeValue("statementType");
            if (statementType == null) {
                statementType = "sql";
            } else if (statementType.equals("CALLABLE")) {
                statementType = "proc";
            }
        }

        return statementType;
    }

    public JSONArray getIn()
    {
        JSONArray in=metaData.getJSONArray("in");
        return in;

    }

    public JSONArray getOut()
    {
        JSONArray out=metaData.getJSONArray("out");
        return out;

    }




}
