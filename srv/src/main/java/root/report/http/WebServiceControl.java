package root.report.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.tree.DefaultComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.configure.AppConstants;
import root.report.util.XmlUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/WebService")
public class WebServiceControl {

	private static final Logger log = Logger.getLogger(WebServiceControl.class);

	@RequestMapping(value = "/getWebServiceClass", produces = "text/plain;charset=UTF-8")
    public String getSelectClass()
	{
        String usersqlPath = AppConstants.getWebServicePath();
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
        // 构造返回JSON
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        for (int i = 0; i < fileList.length; i++) {

            Map<String, String> map = new HashMap<String, String>();
            String filename = fileList[i].getName();
            String name = filename.substring(0, filename.lastIndexOf("."));
            map.put("name", name);
            list.add(map);

        }
        return JSON.toJSONString(list);
    }
	
	//获取所有webService方法
	@SuppressWarnings("unchecked")
    @RequestMapping(value = "/getServiceListByClass/{webServiceClass}", produces = "text/plain;charset=UTF-8")
    public String getWebService(@PathVariable("webServiceClass") String webServiceClass)
    {
        String fileName = webServiceClass + ".xml";
        Document dom = getWebServiceDocument(fileName);
        JSONArray services = new JSONArray();
        List<DefaultComment> webServices = dom.selectNodes("/ws/WebService/comment()");
        for (DefaultComment comment:webServices)
        {
            JSONObject commonObj = JSON.parseObject(comment.getText());
            services.add(commonObj.getString("service"));
        }
        return services.toJSONString();
    }
    
	//取指定WebService方法详情
	@RequestMapping(value = "/getWebServiceParam/{webServiceClass}/{webServiceID}", produces = "text/plain;charset=UTF-8")
	public String getWebServiceOperation(@PathVariable("webServiceClass") String webServiceClass,
	        @PathVariable("webServiceID") String webServiceID)
	{
	    String fileName = webServiceClass + ".xml";
        Document dom = getWebServiceDocument(fileName);
        DefaultComment comment = (DefaultComment)dom.selectObject("/ws/WebService[id="+webServiceID+"]/comment()");
        return comment.getText();
	}
	
	@RequestMapping(value = "/execWebService/{webServiceClass}/{webServiceID}", produces = "text/plain;charset=UTF-8")
	public String execWebService(@PathVariable("webServiceClass") String webServiceClass,
			//@PathVariable("webServiceID") String webServiceID, @RequestBody String pJson) throws Exception
	        @PathVariable("webServiceID") String webServiceID  ) throws Exception
	{
//	    JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
//	    org.apache.cxf.endpoint.Client client = dcf.createClient(new URL("http://localhost:8080/reportServer/WeatherWebService.wsdl"));
//	    
//	    Object[] objects=client.invoke("getSupportCity","武汉");
//	    //输出调用结果
//	    System.out.println(Arrays.toString(objects));
	    return "ok";
	}

	private Document getWebServiceDocument(String fileName)
    {
        String webServicePath = AppConstants.getWebServicePath();
        String webServiceFilePath = webServicePath + File.pathSeparator + fileName;
        File file = new File(webServiceFilePath);
        Document dom = null;
        try
        {
            dom = XmlUtil.parseXmlToDom(file);
        } 
        catch (Exception e)
        {
            log.error("解析WebService.xml文件报错");
            e.printStackTrace();
        }
        
        return dom;
    }

}
