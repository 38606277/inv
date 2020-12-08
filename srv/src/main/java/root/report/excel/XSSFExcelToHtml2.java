package root.report.excel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.converter.ExcelToHtmlUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.dom4j.DocumentHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import root.configure.WebApplicationContext;
import root.report.excel.customize.XssfExcelToHtmlConverter;
import root.report.excel.customize.XssfExcelToHtmlUtils;
import root.report.file.ReportPackage;
import root.report.query.DictionaryControl;
import root.report.query.SelectControl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

public class XSSFExcelToHtml2
{
    private static final Logger log = Logger.getLogger(XSSFExcelToHtml2.class);
    
    public boolean convertToStaticHtml(File sourceFile, File desFile)
    {
        OutputStream out = null;
        StringWriter writer = null; 
        //转换结果成功标志
        boolean result = true;
        try{
            XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(sourceFile));  
            XssfExcelToHtmlConverter converter = new XssfExcelToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());  
            converter.processWorkbook(workBook);
            
            writer = new StringWriter();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();  
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");  
            serializer.setOutputProperty(OutputKeys.METHOD, "html");  
            serializer.transform(new DOMSource(converter.getDocument()), new StreamResult(writer));  
            
            if(!desFile.getParentFile().exists()){
                desFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(desFile);  
            out.write(writer.toString().getBytes("UTF-8"));  
            out.flush();  
            out.close();  
            writer.close();  
        }
        catch(Exception e){
            result = false;
            log.error("转换「"+sourceFile.getName()+"」 to html异常!");
            e.printStackTrace();
        }
        finally{  
            try{
            	if(out != null){out.close();}
                if(writer != null){writer.close();} 
            }
            catch(IOException e){  
                e.printStackTrace();  
            }
        } 
        return result;
    }
    
    public boolean convertToDynamicHtml(File sourceFile, File desFile, String dynamicReportPath)
    {   
    	//将EXCEL转化为标准HTML
        boolean result = convertToStaticHtml(sourceFile, desFile); 
        if(result){
            org.jsoup.nodes.Document hdom = null;
            OutputStreamWriter os = null;
            FileOutputStream fo = null;
            try{
                hdom = Jsoup.parse(desFile, "utf-8");
                XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(sourceFile));
                //修复html列宽度BUG
                fillingTheRestColgroup(hdom,workBook);
                //固定Table宽度
                fixTableWidth(hdom);
                //修复最右侧合并单元格无右边框 BUG
                fillingTheRightBorderWithColor(hdom,workBook);
                String relativePath = getRelativePath(desFile, dynamicReportPath);
                //添加全局样式
                hdom.select("head").append("<link rel=\"stylesheet\" href=\""+relativePath+"static/style.css\" type=\"text/css\" />");
                Elements h2s = hdom.select("body>h2");
                for (int i = 0; i < h2s.size(); i++){
                    h2s.get(i).remove();
                }
                Elements tables = hdom.select("body>table");
                for (int i = 1; i < tables.size(); i++){
                    tables.get(i).remove();
                }
                hdom.select("body>table>thead>tr>th").empty();
                hdom.select("body>table>tbody tr>th").empty();
                //添加工具条
                addToolBar(hdom);
                //获取批注内容
                ReportPackage report = getInputOutputParams(workBook, desFile, dynamicReportPath);
                List<Map<String,Object>> paramList = prepareParamList(report);
                //添加查询条件
                addQueryParam(hdom,paramList);
                //隐藏Excel中定义的查询条件
                hideSerachCondition(report, hdom);
                //当为1时标志已经执行某个操作
                int flag = 0;
                //增加输出参数
                for (Map<String, Object> map:report.getOutCell()){
                	int blankRowNum = Integer.valueOf(map.get("blankRowNum").toString());
                	int blankColNum = Integer.valueOf(map.get("blankColNum").toString());
                	int x_index = Integer.valueOf(map.get("rowIndex").toString());
                	int y_index = Integer.valueOf(map.get("columnIndex").toString());
                    if("row".equals(map.get("type"))&&flag==0)
                    {
                        hdom.select("body>table>tbody>tr:eq("+(x_index-blankRowNum)+")>td:eq("+(y_index-blankColNum)+")").parents().get(0).attr("name", map.get("namespace")+"."+map.get("sqlid")).attr("style", "display:none;");
                        flag++;
                    }
                    hdom.select("body>table>tbody>tr:eq("+(x_index-blankRowNum)+")>td:eq("+(y_index-blankColNum+1)+")").empty().attr("name",map.get("namespace")+"."+map.get("sqlid")+"."+map.get("id")); 
                }
                String out = getOutTypes(report);
                //添加查询脚本
                StringBuilder js = new StringBuilder();
                js.append("var out = "+out+";\n");
                js.append("var paramList = "+getpareParamList(paramList)+";\n");
                js.append("function getQueryParam()\n");
                js.append("{\n");
                js.append("    return "+getQueryParams(out, report)+";\n");
                js.append("}\n");
                
                hdom.select("body").append("<script type=\"text/javascript\" src=\""+relativePath+"static/jquery.min.js\" ></script>");
                hdom.select("body").append("<script type=\"text/javascript\" src=\""+relativePath+"static/spin.min.js\" ></script>");
                hdom.select("body").append("<script type=\"text/javascript\" src=\""+relativePath+"static/common.js\" ></script>");
                hdom.select("body").append("<script type=\"text/javascript\">\n"+js.toString()+"</script>");
                fo = new FileOutputStream(desFile,false);
                os = new OutputStreamWriter(fo, "utf-8");  
                os.write(hdom.html());
                os.close();
                fo.close();
            }catch(Exception e){
                result = false;
                //删除标准HTML
                desFile.delete();
                log.error("转换「"+desFile.getName()+"」 to html异常!");
                e.printStackTrace();
            }finally{
                try{
                	if(os!=null){os.close();}
                	if(fo!=null){fo.close();}
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    //隐藏Excel中设置查询条件的一整行
    private void hideSerachCondition(ReportPackage report, org.jsoup.nodes.Document hdom){
        for (Map<String, Object> map:report.getInCell()){
            int blankRowNum = Integer.valueOf(map.get("blankRowNum").toString());
            int x_index = Integer.valueOf(map.get("rowIndex").toString());
            //取输入框当前行的行高样式
            hdom.select("body>table>tbody>tr:eq("+(x_index-blankRowNum)+")").attr("style", "display:none;");
        }
    }
    //输出HTML参数部分
    private void addQueryParam(org.jsoup.nodes.Document hdom, List<Map<String, Object>> paramList){
    	org.dom4j.Element ul = DocumentHelper.createElement("ul");
    	ul.addAttribute("class", "select");
    	org.dom4j.Element li = null;
    	org.dom4j.Element dl = null;
    	org.dom4j.Element dt = null;
    	org.dom4j.Element dd = null;
    	org.dom4j.Element a = null;
    	org.dom4j.Element i = null;
    	JSONArray arr = null;
    	for(Map<String, Object> temp:paramList){
    		li = DocumentHelper.createElement("li");
    		li.addAttribute("class", "select-list li_hidden");
    		dl = DocumentHelper.createElement("dl");
    		dl.addAttribute("name", temp.get("id").toString());
    		dt = DocumentHelper.createElement("dt");
    		dt.setText(temp.get("text").toString()+"：");
    		dl.add(dt);
    		dd = DocumentHelper.createElement("dd");
    		dd.addAttribute("class", "select-all selected");
    		a = DocumentHelper.createElement("a");
    		a.addText("全部");
    		dd.add(a);
    		dl.add(dd);
    		arr = (JSONArray)(temp.get("value"));
    		JSONObject obj = null;
    		for(int y=0;y<arr.size();y++){
    			obj = arr.getJSONObject(y);
	    		dd = DocumentHelper.createElement("dd");
	    		a = DocumentHelper.createElement("a");
	    		a.addAttribute("name", obj.get("VALUE").toString());
	    		i = DocumentHelper.createElement("i");
	    		a.add(i);
	    		a.addText(obj.get("NAME").toString());
	    		dd.add(a);
	    		dl.add(dd);
    		}
    		li.add(dl);
    		dl = DocumentHelper.createElement("dl");
    		dd = DocumentHelper.createElement("dd");
    		dd.addAttribute("class","more");
    		a = DocumentHelper.createElement("a");
    		a.addText("更多");
    		dd.add(a);
    		dl.add(dd);
    		dd = DocumentHelper.createElement("dd");
    		dd.addAttribute("class","multi");
    		a = DocumentHelper.createElement("a");
    		a.addText("多选");
    		dd.add(a);
    		dl.add(dd);
    		li.add(dl);
    		ul.add(li);
    	}
    	li = DocumentHelper.createElement("li");
		li.addAttribute("class", "select-result");
		dl = DocumentHelper.createElement("dl");
		dt = DocumentHelper.createElement("dt");
		dt.addText("已选条件：");
		dl.add(dt);
		dd = DocumentHelper.createElement("dd");
		dd.addAttribute("class", "select-no");
		dd.addText("暂时没有选择过滤条件");
		dl.add(dd);
		li.add(dl);
		ul.add(li);
    	hdom.select("#loading").after(ul.asXML());
    }
    //输出js	的paramList变量值
    private String getpareParamList(List<Map<String, Object>> paramList){
    	StringBuilder sb = new StringBuilder();
    	sb.append("[");
    	JSONArray arr = null;
    	for(Map<String, Object> temp:paramList){
    		sb.append("{id:'"+temp.get("id")+"',text:'"+temp.get("text")+"',value:[");
    		arr = (JSONArray)(temp.get("value"));
    		JSONObject obj = null;
    		for(int i=0;i<arr.size();i++){
    			obj = arr.getJSONObject(i);
    			sb.append("{name:'"+obj.get("NAME")+"',value:'"+obj.get("VALUE")+"'},");
    		}
    		if(arr.size()>0){
    			sb.deleteCharAt(sb.length()-1);
    		}
    		sb.append("]},");
    	}
    	if(paramList.size()>0){
			sb.deleteCharAt(sb.length()-1);
		}
    	sb.append("]");
    	return sb.toString();
    }
    //封装查询参数值集合 
    private List<Map<String, Object>> prepareParamList(ReportPackage report){
    	List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
    	List<Map<String, Object>> inCell = report.getInCell();
    	Map<String, Object> map = null;
    	for(Map<String, Object> temp:inCell){
    		if(temp.get("paramType")==null){
	    		map = new HashMap<String, Object>();
	    		String namespace = temp.get("namespace").toString();
	    		String sqlid = temp.get("sqlid").toString();
	    		String text = temp.get("text").toString();
	    		String id = temp.get("id").toString();
	    		String lookup = temp.get("lookup").toString();
	    		map.put("id", namespace+"."+sqlid+"."+id);
	    		map.put("text", text);
	    		
	    		JSONArray arr = new JSONArray();
	    		JSONObject obj = new JSONObject();
	    		obj.put("db", getFieldInfo(lookup.split("/")[0]+"."+lookup.split("/")[1],"","dictionary").get("db"));
	    		arr.add(obj);
	    		map.put("value", JSONObject.parseArray(new DictionaryControl().execlDictionary(lookup.split("/")[0],lookup.split("/")[1],null)));
	    		result.add(map);
    		}
    	}
    	return result;
    }
    //添加工具条
    private void addToolBar(org.jsoup.nodes.Document hdom)
    {
    	hdom.select("body").prepend("<div></div>");
    	hdom.select("body div").append("<input type=\"button\" value=\"   查询   \" onclick=\"_query(null)\"></input><span>|</span>");
    	hdom.select("body div").append("<input type=\"button\" value=\"   首页   \" onclick=\"firstPage()\"></input><span>|</span>");
    	hdom.select("body div").append("<input type=\"button\" value=\"   上一页   \" onclick=\"lastPage()\"></input><span>|</span>");
    	hdom.select("body div").append("<input type=\"button\" value=\"   下一页   \" onclick=\"nextPage()\"></input><span>|</span>");
    	hdom.select("body div").append("<input type=\"button\" value=\"   末页   \" onclick=\"endPage()\"></input><span>|</span>");
    	hdom.select("body div").append("<input type=\"button\" value=\"   打印   \" onclick=\"printReport()\"></input><span>|</span>");
    	hdom.select("body div").append("<font>第</font><input type=\"text\" class=\"currentPage\" value=\"0\"><font>页,每页</font>");
    	hdom.select("body div").append("<input type=\"text\" class=\"perPage\" ><font>条记录,总0页</font>");
    	hdom.select("body div").after("<div id=\"loading\"></div>");
    	
    }
    //输出查询参数
    //需要out做参数,是因为join表或从表可能没有查询参数.report参数,用来获取IN查询变量的属性
    private String getQueryParams(String out, ReportPackage report){
        StringBuilder queryParams = new StringBuilder();
        List<Map<String, Object>> in = report.getInCell();
        //存放两种类型(first,row)的所有报表查询属性
        Map<String,List<String>> map = new HashMap<String,List<String>>();
        //主从查询从表查询参数取值,此处注意所有表查询参数唯一
        Map<String,String> attachMap = new HashMap<String,String>();
        JSONObject outObj = JSON.parseObject(out);
        Set<String> outTypes = outObj.keySet();
        JSONObject typeObj = null;
        for (String type:outTypes){
            typeObj = outObj.getJSONObject(type);
            //查询IN参数集合是否至少包含报表查询所需的一个查询参数(针对join表)
            boolean isContain = false;
            String namespace = typeObj.get("namespace").toString();
            String sqlid = typeObj.get("sqlid").toString();
            for (Map<String, Object> param:in){
                String key = type+"."+namespace+"."+sqlid+"."+param.get("db");
                if(namespace.equals(param.get("namespace"))&&sqlid.equals(param.get("sqlid"))){
                   isContain = true;
                   List<String> list = null;
                   if(map.get(key)==null){
                       list = new ArrayList<String>();
                   }else{
                       list = map.get(key);
                   }
                   if(param.get("paramType")!=null){
                	   Map<String, Object> main = (Map<String, Object>) param.get("main");
                	   String main_namespace = main.get("namespace").toString();
                	   String main_sqlid = main.get("sqlid").toString();
                	   String main_id = main.get("id").toString();
                   	   attachMap.put(key, param.get("id")+"="+main_namespace+"."+main_sqlid+"."+main_id);
                   }
                   list.add(param.get("id").toString());
                   map.put(key, list);
                }
            }
            if(!isContain){
                String key = type+"."+namespace+"."+sqlid+"."+
                             getDBName(namespace,sqlid);
                map.put(key, new ArrayList<String>());
            }
            if(type.equals("row")){
                JSONArray joinArray = outObj.getJSONObject("row").getJSONArray("join");
                if(joinArray!=null){
                    for (int i = 0; i < joinArray.size(); i++){
                        JSONObject joinObj = joinArray.getJSONObject(i);
                        //查询IN参数集合是否至少包含报表查询所需的一个查询参数(针对join表)
                        boolean joinIsContainQryParam = false;
                        String joinNamespace = joinObj.get("namespace").toString();
                        String joinSqlid = joinObj.get("sqlid").toString();
                        for (Map<String, Object> param:in){
                            String key = type+"."+joinNamespace+"."+joinSqlid+"."+param.get("db");
                            if(param.get("paramType")!=null){
                               Map<String, Object> main = (Map<String, Object>) param.get("main");
                          	   String main_namespace = main.get("namespace").toString();
                          	   String main_sqlid = main.get("sqlid").toString();
                          	   String main_id = main.get("id").toString();
                               attachMap.put(key, param.get("id")+"="+main_namespace+"."+main_sqlid+"."+main_id);
                            }
                            if(joinNamespace.equals(param.get("namespace"))&&joinSqlid.equals(param.get("sqlid"))){
                                joinIsContainQryParam = true;
                               List<String> list = null;
                               if(map.get(key)==null){
                                   list = new ArrayList<String>();
                               }else{
                                   list = map.get(key);
                               }
                               list.add(param.get("id").toString());
                               map.put(key, list);
                            }
                        }
                        if(!joinIsContainQryParam){
                            String key = type+"."+joinNamespace+"."+joinSqlid+"."+
                                         getDBName(joinNamespace,joinSqlid);
                            map.put(key, new ArrayList<String>());
                        }
                    }
                }
            }
        }
        queryParams.append("{");
        for (String key:map.keySet()){
            if(key.startsWith("first")){
                List<String> list = map.get(key);
                String[] arr = key.split("\\.");
                queryParams.append(arr[0]+":{'"+arr[1]+"."+arr[2]+"':{db:'"+arr[3]+"',in:[");
                for (String id:list){
                    queryParams.append("{id:'"+id+"',value:getParamValue('"+arr[1]+"."+arr[2]+"."+id+"')},");  
                }
                List<String> inList = getSqlInParamList(arr[1],arr[2]);
                for (int i = 0; i < inList.size(); i++){
                    inList.removeAll(list);
                    for (int j = 0; j < inList.size(); j++){
                    	queryParams.append("{id:'"+inList.get(i)+"',value:''},");
                    }
                }
                if(inList.size()!=0||list.size()!=0){
                	queryParams.deleteCharAt(queryParams.length()-1);
                }
                queryParams.append("]}},");
            }
        }
        //row结果集个数
        int count = 0;
        for (String key:map.keySet()){
            if(key.startsWith("row")){
                count++;
                if(count==1){
                    queryParams.append("row:{");
                }
                List<String> list = map.get(key);
                String[] arr = key.split("\\.");
                queryParams.append("'"+arr[1]+"."+arr[2]+"':{db:'"+arr[3]+"',in:[");
                String attachValue = attachMap.get(key);
                String attach_id = null;
                if(attachValue!=null){
                	attach_id = attachValue.split("=")[0];
                	String value = attachValue.split("=")[1];
                	String[] value_arr = value.split("\\.");
                	queryParams.append("{id:'"+attach_id+"',value:getParamValue('"+value_arr[0]+"."+value_arr[1]+"."+value_arr[2]+"')},");
                }
                for (String id:list){
                	if(id!=null&&!id.equals(attach_id)){
                		queryParams.append("{id:'"+id+"',value:getParamValue('"+arr[1]+"."+arr[2]+"."+id+"')},");
                	}
                }
                List<String> inList = getSqlInParamList(arr[1],arr[2]);
                for (int i = 0; i < inList.size(); i++){
                    inList.removeAll(list);
                    for (int j = 0; j < inList.size(); j++){
                    	queryParams.append("{id:'"+inList.get(i)+"',value:''},");
                    }
                }
                if(attachValue!=null||inList.size()!=0||list.size()!=0){
                	queryParams.deleteCharAt(queryParams.length()-1);
                }
                queryParams.append("]},");
            }
        }
        queryParams.deleteCharAt(queryParams.length()-1);
        queryParams.append("}}");
        return queryParams.toString();
    }
    //获取指定sql输入参数列表
    private List<String> getSqlInParamList(String namespace, String sqlid)
    {
        List<String> inList = new ArrayList<String>();
        JSONObject obj = new JSONObject();
        obj.put("namespace", namespace);
        obj.put("sqlid", sqlid);
        SelectControl selectControl = WebApplicationContext.getBean(SelectControl.class);
        String qrySelectSqlDetail = selectControl.qrySelectSqlDetail(obj.toJSONString());
        JSONObject commentObj = JSON.parseObject(qrySelectSqlDetail).getJSONObject("comment");
        JSONArray inArr = commentObj.getJSONArray("in");
        for (int i = 0; i < inArr.size(); i++){
            inList.add(inArr.getJSONObject(i).getString("id"));
        }
        return inList;
    }
    
    //输出类型和输出字段
    private String getOutTypes(ReportPackage report)
    {
        StringBuilder sb = new StringBuilder();
        List<Map<String, Object>> out = report.getOutCell();
        //存放两种类型(first,row)的查询属性
        Map<String,List<String>> map = new HashMap<String,List<String>>();
        Map<String,String> linkMap = new HashMap<String,String>();
        Map<String,List<String>> joinMap = new HashMap<String,List<String>>();
        for (Map<String, Object> target:out){
        	//如果是JOIN表属性,讲相关属性存放在joinMap里,否则存放在map里
            if(target.get("joinkey")!=null){
                String key = target.get("joinkey")+"."+target.get("namespace")+"."+target.get("sqlid");
                List<String> idList = null;
                if(joinMap.get(key)==null){
                    idList = new ArrayList<String>();
                }else{
                    idList = joinMap.get(key);
                }
                idList.add(target.get("id").toString());
                joinMap.put(key, idList);
            }else{
                String key = target.get("type")+"."+target.get("namespace")+"."+target.get("sqlid");
                List<String> idList = null;
                if(map.get(key)==null){
                    idList = new ArrayList<String>();
                }else{
                    idList = map.get(key);
                }
                idList.add(target.get("id").toString());
                map.put(key, idList);
                
                if(target.get("link")!=null){
                    if(linkMap.get(key)==null){
                        linkMap.put(key, target.get("link").toString());
                    }else{
                        linkMap.put(key, linkMap.get(key)+","+target.get("link"));
                    }
                }
            }
        }
        sb.append("{");
        //输出到html内容out变量
        for (String key:map.keySet()){
            List<String> idList = map.get(key);
            String[] arr = key.split("\\.");
            sb.append(arr[0]+":{namespace:'"+arr[1]+"',sqlid:'"+arr[2]+"',id:[");
            for (String id:idList){
                sb.append("'"+id+"',");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append("]");
            if(arr[0].equals("row")){
                sb.append(",join:[");
                for (String joinKey:joinMap.keySet()){
                    List<String> joinIdList = joinMap.get(joinKey);
                    String[] joinArr = joinKey.split("\\.");
                    sb.append("{namespace:'"+joinArr[1]+"',sqlid:'"+joinArr[2]+"',joinkey:'"+joinArr[0]+"',id:[");
                    for (String id:joinIdList){
                        sb.append("'"+id+"',");
                    }
                    sb.deleteCharAt(sb.length()-1);
                    sb.append("]},");
                }
                if(joinMap.size()>0){
                    sb.deleteCharAt(sb.length()-1);
                }
                sb.append("]");
            }
            if(linkMap.get(key)!=null){
                sb.append(",link:["+linkMap.get(key)+"]");
            }
            sb.append("},");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("}");
        return sb.toString();
    }
    
    //获取超链接信息,只针对输出字段的超链接
    private String getLinkInfo(JSONObject link, String field, File desFile, String dynamicReportPath)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{field:'"+getFieldInfo(field, "out","query").get("id")+"',dest:'"+getRelativePath(desFile, dynamicReportPath)+link.getString("dest").replaceAll("(.xlsx|.xls)", ".html")+"',param:[");
        JSONArray arr = link.getJSONArray("param");
        String[] param = null;
        for (int i = 0; i < arr.size(); i++){
            sb.append("{");
            param = arr.getString(i).split("=");
            sb.append("desField:'"+param[0].substring(0, param[0].lastIndexOf(".")+1)+getFieldInfo(param[0], "in","query").get("id")+"',srcField:'"+getFieldInfo(param[1], "out","query").get("id")+"'");
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }
    //根据field字段获取字段名称,field字段组成:namespace.sqlid.columnname,type取值in/out
    private Map<String, Object> getFieldInfo(String field, String type, String sqlType)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        String[] arr = field.split("\\.");
        JSONObject obj = new JSONObject();
        obj.put("namespace", arr[0]);
        obj.put("sqlid", arr[1]);
        String qrySelectSqlDetail = null;
		if(sqlType.equals("query")){
            SelectControl selectControl = WebApplicationContext.getBean(SelectControl.class);
			qrySelectSqlDetail = selectControl.qrySelectSqlDetail(obj.toJSONString());
		}else if(sqlType.equals("dictionary")){
			qrySelectSqlDetail = new DictionaryControl().qryDictionaryDetail(obj.toJSONString());
		}
		JSONObject commentObj = JSON.parseObject(qrySelectSqlDetail).getJSONObject("comment");
        JSONArray param = null;
        if("in".equals(type)){
            param = commentObj.getJSONArray("in");
            for (int i = 0; i < param.size(); i++){
                JSONObject in = param.getJSONObject(i);
                if(arr[2].equals(in.getString("name"))){
                    map.put("id", in.getString("id"));
                    map.put("lookup", in.getString("lookup"));
                    break;
                }
            }
        }else if("out".equals(type)){
            param = commentObj.getJSONArray("out");
            for (int i = 0; i < param.size(); i++){
                JSONObject in = param.getJSONObject(i);
                if(arr[2].equals(in.getString("name"))){
                    map.put("id", in.getString("id"));
                    break;
                }
            }
        }
        
        map.put("db", commentObj.getString("db"));
        return map;
    }
    //根据namspace和sqlid获取db信息
    private String getDBName(String namespace, String sqlid)
    {
        JSONObject obj = new JSONObject();
        obj.put("namespace", namespace);
        obj.put("sqlid", sqlid);
        SelectControl selectControl = WebApplicationContext.getBean(SelectControl.class);
        String qrySelectSqlDetail = selectControl.qrySelectSqlDetail(obj.toJSONString());
        JSONObject commentObj = JSON.parseObject(qrySelectSqlDetail).getJSONObject("comment");
        return commentObj.getString("db");
    }
    private String getRelativePath(File desFile,String dynamicReportPath)
    {
        String relativePath = "";
        String subPath = desFile.getAbsolutePath().replace(dynamicReportPath, "");
        int count = getCount(subPath);
        if(count==1){
            relativePath = "./"; 
        }else if(count==2){
            relativePath = "../";
        }else{
           while(count-1>0){
               relativePath+="../";
               count--;
           }
        }
        
        return relativePath;
    }
    //获取字符串中斜杠分隔符的个数
    private int getCount(String s){
        int count = 0;
        int index = -1;
        while((index = s.indexOf(File.separator))!=-1){
            count++;
            s = s.substring(index + 1);
        }
        return count;
    }
    
    /**
     * 合并单元格处理--加入list
     * 
     * @param sheet
     * @return
     */
     private List<CellRangeAddress> getCombineCell(XSSFSheet sheet) 
     {
         List<CellRangeAddress> list = new ArrayList<CellRangeAddress>();
         // 获得一个 sheet 中合并单元格的数量
         int sheetmergerCount = sheet.getNumMergedRegions();
         // 遍历合并单元格
         for (int i = 0; i < sheetmergerCount; i++) {
             // 获得合并单元格加入list中
             CellRangeAddress ca = sheet.getMergedRegion(i);
             list.add(ca);
         }
         return list;
     }
     //获取合并单元格合并的列数
     private int getCellCombineColNum(XSSFCell cell,List<CellRangeAddress> list)
     {
         int firstR = 0;
         //int lastR = 0;
         int firstC = 0;
         int lastC = 0;
         for(CellRangeAddress combineCell: list){
             // 获得合并单元格的起始行, 结束行, 起始列, 结束列
             firstR = combineCell.getFirstRow();
             //lastR = combineCell.getLastRow();
             firstC = combineCell.getFirstColumn();
             lastC = combineCell.getLastColumn();
             if (cell.getRowIndex()==firstR&&cell.getColumnIndex()==firstC){
                 return lastC-firstC;
             }
         }
         return 0;
     }
     //获取批注内容
     private ReportPackage getInputOutputParams(XSSFWorkbook wk, File desFile, String dynamicReportPath)
     {
         ReportPackage report = new ReportPackage();
         List<Map<String, Object>> inCell = new ArrayList<Map<String, Object>>();
         List<Map<String, Object>> outCell = new ArrayList<Map<String, Object>>();
         XSSFSheet sheet = wk.getSheetAt(0);
         //获取合并单元格
         List<CellRangeAddress> combineCells = getCombineCell(sheet);
         int lastrow = sheet.getLastRowNum();
         int blankRowNum = 0;
         for (int i = 0; i <= lastrow; i++){
             //每读一行,即初始化合并的列数
             int blankColNum = 0;
             XSSFRow row = sheet.getRow(i);
             if(row==null||(row!=null&&row.getCTRow().getHidden())){
                 blankRowNum++;
             }else{
                 int lastCell = row.getLastCellNum();
                 for(int j = 0; j <= lastCell; j++){
                     XSSFCell cell = row.getCell(j);
                     if(cell!=null){
                         XSSFComment hssfComment = cell.getCellComment();
                         Map<String, Object> map = new HashMap<String, Object>();
                         if(hssfComment!=null){
                             map.put("rowIndex", cell.getRowIndex());
                             map.put("columnIndex", cell.getColumnIndex());
                             //当前行的前空白行数，用于定位该备注在HTML中位置
                             map.put("blankRowNum",blankRowNum);
                             //当前列的前空白列数，用于定位该备注在HTML中位置
                             map.put("blankColNum",blankColNum);
                             String comment = hssfComment.getString().getString();
                             JSONObject commentObj = JSON.parseObject(comment);
                             String func = commentObj.getString("func");
                             String field = null;
                             Map<String, Object> param = null;
                             if("in".equals(func)){
                                 field = commentObj.getString("field");
                                 if(field!=null){
                                     param = getFieldInfo(field, "in", "query");
                                     map.put("id", param.get("id"));
                                     map.put("text", field.substring(field.lastIndexOf(".")+1));
                                     map.put("lookup", param.get("lookup"));
                                     map.put("namespace", field.split("\\.")[0]);
                                     map.put("sqlid", field.split("\\.")[1]);
                                     map.put("db", getFieldInfo(field, "in", "query").get("db"));
                                     inCell.add(map);
                                 }else{
                                     JSONArray arr = commentObj.getJSONArray("fields");
                                     //主表参数属性
                                     Map<String,Object> main = null;
                                     //临时map
                                     Map<String,Object> middle= null;
                                     for (int k = 0; k < arr.size(); k++){
                                         field = arr.getString(k);
                                         param = getFieldInfo(field, "in", "query");
                                         if(k==0){
                                        	 main= new HashMap<String, Object>();
                                        	 main.putAll(map);
                                        	 main.put("id", param.get("id"));
                                        	 main.put("text", field.substring(field.lastIndexOf(".")+1));
                                        	 main.put("lookup", param.get("lookup"));
                                             
                                        	 main.put("namespace", field.split("\\.")[0]);
                                        	 main.put("sqlid", field.split("\\.")[1]);
                                        	 main.put("db", getFieldInfo(field, "in","query").get("db"));
                                        	 inCell.add(main);
                                    	 }else{
                                    		 middle = new HashMap<String, Object>();
                                    		 middle.putAll(map);
                                    		 middle.put("id", param.get("id"));
                                    		 middle.put("text", field.substring(field.lastIndexOf(".")+1));
                                    		 middle.put("lookup", param.get("lookup"));
                                    		 middle.put("namespace", field.split("\\.")[0]);
                                    		 middle.put("sqlid", field.split("\\.")[1]);
                                    		 middle.put("db", getFieldInfo(field, "in","query").get("db"));
                                        	 //paramType取值“attach”,表示从表参数
                                    		 middle.put("paramType", "attach");
                                    		 middle.put("main", main);
                                    		 inCell.add(middle);
                                         }
                                     }
                                 }
                             }else{
                                 field = commentObj.getString("field");
                                 param = getFieldInfo(field, "out","query");
                                 map.put("type", func.split("\\.")[1]);
                                 map.put("id", param.get("id"));
                                 map.put("namespace", field.split("\\.")[0]);
                                 map.put("sqlid", field.split("\\.")[1]);
                                 if(commentObj.getJSONObject("link")!=null){
                                     map.put("link", getLinkInfo(commentObj.getJSONObject("link"), field, desFile, dynamicReportPath));
                                 }
                                 if(commentObj.getString("join")!=null){
                                     String joinValue = commentObj.getString("join");
                                     map.put("joinkey", getFieldInfo(joinValue.split("=")[0],"out","query").get("id"));
                                 }
                                 outCell.add(map);
                             }
                         }
                         //当前单元格为合并单元格时,需要取其合并的单元格数,以便下一个批准单元格能准确读获取到它的坐标
                         blankColNum+=getCellCombineColNum(cell,combineCells);
                     }
                 }
             }
         }
         report.setInCell(inCell);
         report.setOutCell(outCell);
         return report;
     }
     //固定table宽度
     private void fixTableWidth(org.jsoup.nodes.Document hdom)
     {
    	 Elements cols = hdom.select("body>table>colgroup>col[width]");
    	 int totalWidth = 0;
    	 for(Element col:cols){
    		 totalWidth+=Integer.valueOf(col.attr("width"));
		 }
    	 hdom.select("body>table").attr("width", String.valueOf(totalWidth));
     }
     
     private void fillingTheRestColgroup(org.jsoup.nodes.Document hdom, XSSFWorkbook workBook)
     {
         int f = workBook.getSheetAt(0).getFirstRowNum();
         int l = workBook.getSheetAt(0).getLastRowNum();
         int maxColumnNum = 1;
         //获取总的列数
         for(int i = f;i<=l;i++){
             if(workBook.getSheetAt(0).getRow(i)!=null&&workBook.getSheetAt(0).getRow(i).getLastCellNum()>maxColumnNum){
                 maxColumnNum =  workBook.getSheetAt(0).getRow(i).getLastCellNum();
             }
         }
         //实际HTML中的colgroup的列数
         int colsize = hdom.select("body>table>colgroup>col[width]").size();
         for(int y = colsize + 1;y<=maxColumnNum;y++){
             hdom.select("body>table>colgroup").append("<col width=\""+ExcelToHtmlUtils.getColumnWidthInPx(workBook.getSheetAt(0).getColumnWidth(y-1))+"\">"); 
             hdom.select("body>table>thead>tr").append("<th></th>"); 
         }
     }
     
     private void fillingTheRightBorderWithColor(org.jsoup.nodes.Document hdom, XSSFWorkbook workBook)
     {
         List<Map<String, String>> cells = new ArrayList<Map<String, String>>();
         XSSFSheet sheet = workBook.getSheetAt(0);
         //获取合并单元格
         List<CellRangeAddress> combineCells = getCombineCell(sheet);
         int lastrow = sheet.getLastRowNum();
         int blankRowNum = 0;
         XSSFRow row =null;
         for (int i = 0; i <= lastrow; i++){
             //每读一行,即初始化合并的列数
             int blankColNum = 0;
             row = sheet.getRow(i);
             if(row==null||(row!=null&&row.getCTRow().getHidden())){
                 blankRowNum++;
             }else{
                 int lastCell = row.getLastCellNum();
                 for(int j = 0; j <= lastCell; j++){
                     XSSFCell cell = row.getCell(j);
                     Map<String, String> map = null;
                     StringBuilder rightBorderStyle = null;
                     XSSFCellStyle cellStyle = null;
                     XSSFColor color = null;
                     if(cell!=null){
                         //当前单元格为合并单元格时,需要取其合并的单元格数,以便下一个批准单元格能准确读获取到它的坐标
                         int combineNum = getCellCombineColNum(cell,combineCells);
                         if(combineNum>0){
                             map = new HashMap<String, String>();
                             map.put("rowIndex", String.valueOf(cell.getRowIndex()));
                             map.put("columnIndex", String.valueOf(cell.getColumnIndex()));
                             map.put("blankRowNum",String.valueOf(blankRowNum));
                             map.put("blankColNum",String.valueOf(blankColNum));
                             //获取合并后的右边框的样式
                             cellStyle = row.getCell(j+combineNum).getCellStyle();
                             short borderRight = cellStyle.getBorderRightEnum().getCode();
                             if (borderRight == BorderStyle.NONE.getCode()){
                                blankColNum+=combineNum;
                                continue; 
                             }
                             rightBorderStyle = new StringBuilder();
                             rightBorderStyle.append("border-right:");
                             rightBorderStyle.append(XssfExcelToHtmlUtils.getBorderWidth(cellStyle.getBorderRightEnum().getCode()));
                             rightBorderStyle.append(' ');
                             rightBorderStyle.append( XssfExcelToHtmlUtils.getBorderStyle(cellStyle.getBorderRightEnum().getCode()));
                             color = cellStyle.getRightBorderXSSFColor();
                             if (color != null){
                                 rightBorderStyle.append(' ');
                                 rightBorderStyle.append(getRGBHex(color.getRGB()));
                             }
                             map.put("style", rightBorderStyle.toString());
                             cells.add(map);
                         }
                         blankColNum+=combineNum;
                     }
                 }
             }
         }
         
         int x_index = 0;
         int y_index = 0;
         int blankRowNum1 = 0;
         int blankColNum1 = 0;
         for(Map<String, String> map:cells){
             x_index = Integer.valueOf(map.get("rowIndex"));
             y_index = Integer.valueOf(map.get("columnIndex"));
             blankRowNum1 = Integer.valueOf(map.get("blankRowNum"));
             blankColNum1 = Integer.valueOf(map.get("blankColNum"));
             hdom.select("body>table>tbody>tr:eq("+(x_index-blankRowNum1)+")>td:eq("+(y_index-blankColNum1+1)+")").attr("style", map.get("style"));
         }
     }
     
     private String getRGBHex( byte[] rgb) {
         StringBuffer sb = new StringBuffer();
         sb.append('#');
         if(rgb == null){
            return "black";
         }
         for(byte c:rgb){
            int i = (int)c;
            if(i < 0) {
               i += 256;
            }
            String cs = Integer.toHexString(i);
            if(cs.length() == 1){
               sb.append('0');
            }
            sb.append(cs);
         }
         String result = sb.toString();
         if(result.equals("#ffffff"))
             return "white";
         if(result.equals("#c0c0c0"))
             return "silver";
         if(result.equals("#808080"))
             return "gray";
         if(result.equals( "#000000"))
             return "black";
         return result;
      }
}
