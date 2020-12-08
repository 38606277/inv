package root.form.datatable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import root.report.db.DbFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangjian
 *
 */
@RestController
@RequestMapping("/reportServer")
public class DataTableControl {

	private static final Logger log = Logger.getLogger(DataTableControl.class);
	/**
	 * *取所有的报表定义，返回以下json格式
	 * @return
	 * {
	 *    id:order_herder,
	 *    name:order_name
	 * }
	 */
	@RequestMapping(value = "/DataTable/getAll", produces = "text/plain; charset=utf-8")
	public String getAll() {
		List<Map> allTable;
		try {
			allTable = DbFactory.Open(DbFactory.FORM).selectList("table.getAll");
		} catch (Exception ex) {
			return ex.toString();
		}
		return JSON.toJSONString(allTable);
	}
	
	/**
	 * APP端获取table列表
	 * @return
	 */
	@RequestMapping(value = "/DataTable/getAllTable", produces = "text/plain; charset=utf-8")
    public String getAllTable() {
        List<Map> allTable;
        List<Map> dataList = new ArrayList<Map>();
        try {
            allTable = DbFactory.Open(DbFactory.FORM).selectList("table.getAllTable");
            for (Map dataRule : allTable) {
                Map data = new HashMap();
                data.put("name", dataRule.get("name").toString());
                data.put("value", dataRule.get("id").toString());
                dataList.add(data);
            }
        } catch (Exception ex) {
            return ex.toString();
        }
        return JSON.toJSONString(dataList);
    }
	
	/**
	 * *取所有的列定义，返回以下json格式
	 * @return
	 * {
	 *    id:order_herder,
	 *    name:order_name,
	 *    data_type:string
	 * }
	 */
	@RequestMapping(value = "/DataTable/getCols/{pTableId}", produces = "text/plain; charset=utf-8")
	public String getCols(@PathVariable("pTableId") String pTableId) {
		List<Map> columns;
		try {
			columns = DbFactory.Open(DbFactory.FORM).selectList("table.getCols",pTableId);
		} catch (Exception ex) {
			System.out.println(ex);
			return ex.toString();
		}
		return JSON.toJSONString(columns);
	}
	
	/**
	 * 加载数据表格，返回以下json格式
	 * @return
	 * {
	 *    name:,
	 *    field:[{
	 *    	id:,
	 *    	name:
	 *    }]
	 * }
	 */
	@RequestMapping(value = "/DataTable/getDataTableByTable/{tableId}", produces = "text/plain; charset=utf-8")
	public String getDataTable(@PathVariable("tableId") String tableId) {
		Map tableinfo;
		List<Map> tableColumns;
		try {
			tableinfo = DbFactory.Open(DbFactory.FORM).selectOne("table.getTableById",tableId);
			System.out.println("tableinfo: "+tableinfo.toString());
			log.info("tableinfo: "+tableinfo.toString());
			tableColumns = DbFactory.Open(DbFactory.FORM).selectList("table.getTableColumnsById",tableId);
			System.out.println("tableColumns: "+tableColumns.toString());
			log.info("tableColumns: "+tableColumns.toString());
			tableinfo.put("field", JSON.toJSONString(tableColumns));
		} catch (Exception ex) {
			System.out.println(ex);
			return ex.toString();
		}
		return JSON.toJSONString(tableinfo);
	}
	
	@RequestMapping(value = "/DataTable/addDataTable", produces = "text/plain; charset=utf-8")
	public String addDataTable(@RequestBody String dtInfo){
		//获取新增保存参数
		String retCode = "true";
		String retMsg = "新增数据表成功";
		JSONObject jsonObject = (JSONObject) JSON.parseObject(dtInfo);
		String tableName = (String)jsonObject.get("name");
		String tableDesc = (String)jsonObject.get("desc");
		int num = DbFactory.Open(DbFactory.FORM).selectOne("table.getTableByName" ,tableName);
		if(num==0){
			Map tableColmn = new HashMap<>();
			tableColmn.put("name", tableName);
			tableColmn.put("desc", tableDesc);
			num = DbFactory.Open(DbFactory.FORM).insert("table.addDataTable",tableColmn);
			if(num!=1){
				retCode = "false";
				retMsg = "新增数据表失败";
			}else{
				String tableid = tableColmn.get("table_id").toString();
				JSONArray jsonArray  = (JSONArray) jsonObject.get("field");
				StringBuffer sql = new StringBuffer();
				Map tabledefind = new HashMap<>();
				int size = jsonArray.size();
				for(int index = 0; index < size; index++) {
					JSONObject json = (JSONObject) JSON.parseObject(jsonArray.get(index).toString());
					tabledefind.put("id", tableid);
					tabledefind.put("name", json.get("name"));
					tabledefind.put("desc", json.get("desc"));
					tabledefind.put("type", json.get("type"));
					tabledefind.put("isnull", json.get("isnull"));
					sql.append(json.get("name")+" ").append(json.get("desc")+" ");
					if("true".equals(json.get("isnull"))){
					    sql.append("is not null ");
					}
					sql.append(",");
					if(DbFactory.Open(DbFactory.FORM).insert("table.addDataTableDefine",tabledefind)!=1){
						retCode = "false";
						retMsg = "新增数据表失败";
					}
				}  
				System.out.println(sql.toString());
			}
		}else{
			retCode = "false";
			retMsg = "数据已存在,请核实";
		}
		Map result = new HashMap<>();
		result.put("retCode", retCode);
		result.put("retMsg", retMsg);
		return JSON.toJSONString(result);
	}
	@Transactional
	@RequestMapping(value = "/DataTable/modifyDataTable", produces = "text/plain; charset=utf-8")
	public String modifyDataTable(@RequestBody String dtInfo) throws Exception {
		//获取新增保存参数
		String retCode = "true";
		String retMsg = "修改数据表成功";
		JSONObject jsonObject = (JSONObject) JSON.parseObject(dtInfo);
		String tableId = (String)jsonObject.get("id");
		String tableName = (String)jsonObject.get("name");
		String tableDesc = (String)jsonObject.get("desc");
		Map tableColmn = new HashMap<>();
		tableColmn.put("id", tableId);
		tableColmn.put("name", tableName);
		tableColmn.put("desc", tableDesc);
		int num = DbFactory.Open(DbFactory.FORM).update("table.updateDataTable",tableColmn);
		if(num!=1){
			retCode = "false";
			retMsg = "修改数据表失败";
		}else{
			String tableid = tableColmn.get("id").toString();
			DbFactory.Open(DbFactory.FORM).delete("table.deleteDataTableDefine",tableId);
			JSONArray jsonArray  =(JSONArray)jsonObject.get("field");
			Map tabledefind = new HashMap<>();
			int size = jsonArray.size();
			for(int index = 0; index < size; index++) {
				JSONObject json = (JSONObject) JSON.parseObject(jsonArray.get(index).toString());
				tabledefind.put("id", tableid);
				tabledefind.put("name", json.get("name"));
				tabledefind.put("desc", json.get("desc"));
				tabledefind.put("type", json.get("type"));
				tabledefind.put("isnull", json.get("isnull"));
				if(DbFactory.Open(DbFactory.FORM).insert("table.addDataTableDefine",tabledefind)!=1){
					retCode = "false";
					retMsg = "修改数据表失败";
				}
			}  
		}
		Map result = new HashMap<>();
		result.put("retCode", retCode);
		result.put("retMsg", retMsg);
		return JSON.toJSONString(result);
	}
	@RequestMapping(value = "/DataTable/deleteByTable/{tableId}", produces = "text/plain; charset=utf-8")
	public String deleteByTable(@PathVariable("tableId") String tableId) throws Exception {
		//获取新增保存参数
		String retCode = "true";
		String retMsg = "删除数据表成功";
		int num = DbFactory.Open(DbFactory.FORM).delete("table.deleteDataTableDefine",tableId);
		if(DbFactory.Open(DbFactory.FORM).delete("table.deleteDataTable",tableId)!=1){
			retCode = "false";
			retMsg = "删除数据表失败";
		}
		Map result = new HashMap<>();
		result.put("retCode", retCode);
		result.put("retMsg", retMsg);
		return JSON.toJSONString(result);
	}
	
}
