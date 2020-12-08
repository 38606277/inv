package root.form.datatable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.db.DbFactory;
import root.report.sys.SysContext;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author wangjian
 *
 */
@RestController
@RequestMapping("/reportServer")
public class ExcelDataControl {

	private static final Logger log = Logger.getLogger(ExcelDataControl.class);
	/**
	 * *取所有的报表定义，返回以下json格式
	 * @return
	 * {
	 *    id:order_herder,
	 *    name:order_name
	 * }
	 */
	@RequestMapping(value = "/ExcelData/getAll", produces = "text/plain; charset=utf-8")
	public String getAll() {
		List<Map> allTable;
		try {
			String userName = SysContext.getRequestUser().getUserName();
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("userName",userName);
			allTable = DbFactory.Open(DbFactory.FORM).selectList("table.getAllFormTable",map);
		} catch (Exception ex) {
			return ex.toString();
		}
		return JSON.toJSONString(allTable);
	}
	
	@RequestMapping(value = "/ExcelData/getTableSchema", produces = "text/plain; charset=utf-8")
	public String getTableSchema(String tableName) {
		List<Map> allTableColumn;
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			SqlSession session = GetSqlSessionFromTable(tableName,map,true);
			String mapId = "table.GetTableSchema";
			allTableColumn = session.selectList(mapId,map);
		} catch (Exception ex) {
			return ex.toString();
		}
		return JSON.toJSONString(allTableColumn);
	}
	
	
	@RequestMapping(value = "/ExcelData/getDataFromFormTableName", produces = "text/plain; charset=utf-8")
	public String getDataFromFormTableName(String tableName) {
		List<Map> allDatas;
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			SqlSession session = GetSqlSessionFromTable(tableName,map,true);
			allDatas = session.selectList("table.getDataFromFormTableName",map);
		} catch (Exception ex) {
			return ex.toString();
		}
		return JSON.toJSONString(allDatas);
	}
	
	private SqlSession GetSqlSessionFromTable(String tableName,Map<String,Object> map,boolean autoCommit)
	{
		String[] splitTableName = tableName.split("\\.");
		String db = splitTableName[0];
		String tName = splitTableName[1];
		if(map.containsKey("tableName")) {
			map.remove("tableName");
		}
		map.put("tableName", tName);
		SqlSession session = null;
		if(db.equalsIgnoreCase("form")) {
			session = DbFactory.Open(autoCommit, DbFactory.FORM);
		}else {
			session = DbFactory.Open(autoCommit, DbFactory.SYSTEM);
		}
		return session;
	}
	
	private List<TableEntitySql> toEntity(JSONArray jArray,String tableName,String keyName, HashSet<String> ColumnNames){
		List<TableEntitySql> tableEntities = new ArrayList<TableEntitySql>();
		for(int i = 0;i<jArray.size();i++)
		{
			TableEntitySql tableEntity = new TableEntitySql();
			tableEntity.setTableName(tableName);
			List<ColumnEntitySql> columnEntities = new ArrayList<ColumnEntitySql>();
			String objStr = jArray.get(i) + "";
			JSONObject object = JSON.parseObject(objStr);
			Set<Entry<String,Object>> entrySet = object.entrySet();
			for(Entry<String,Object> entry : entrySet)
			{
				ColumnEntitySql columnEntity = new ColumnEntitySql();
				if(ColumnNames.contains(entry.getKey()))
				{
					columnEntity.setOperateIfNull("");
					columnEntity.setDbColName(entry.getKey());
					String value = entry.getValue().toString();
					columnEntity.setValue(value);
					columnEntities.add(columnEntity);
				}
			}
			tableEntity.setCols(columnEntities);
			tableEntity.setKeyName(keyName);
			tableEntities.add(tableEntity);
		}
		return tableEntities;
	}

	@RequestMapping(value = "/ExcelData/setDataFromFormTableName", produces = "text/plain; charset=utf-8")
	public String setDataFromFormTableName(@RequestBody String dtInfo) {
		
		JSONObject jsonObject = (JSONObject) JSON.parseObject(dtInfo);
		String tableName = (String)jsonObject.get("tableName");
		if(tableName == null || tableName.equals(""))
		{
			return "";
		}
		List<LinkedHashMap> allProperties;
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			SqlSession session = GetSqlSessionFromTable(tableName,map,true);
			allProperties = session.selectList("table.GetTableSchema",map);
			String keyName = "";
			
			HashSet<String> ColumnNames = new HashSet<String>();
			int index = 0;
			for(LinkedHashMap property : allProperties)
			{
				String pName = property.get("COLUMN_NAME").toString();
				String pType = property.get("COLUMN_KEY").toString();
				if(pType.equals("0")){
					keyName = pName;
				}
				ColumnNames.add(pName);
			}
			String dbName = tableName.split("\\.")[0];
			DbFactory.close(dbName);
			SqlSession sqlSession = GetSqlSessionFromTable(tableName,map,false); 
			
			try{
				String tName = tableName.split("\\.")[1];
				String removeString = (String)jsonObject.get("remove");
				//JSONArray removeArray = JSON.parseArray(removeString);
				sqlSession.delete("table.DeleteTable",map);	
//				String modifyString = (String)jsonObject.get("modify");
//				if(modifyString != null){
//					JSONArray modifyArray = JSON.parseArray(modifyString);
//					if(modifyArray.size() > 0){
//						List<TableEntitySql> tableEntities = toEntity(modifyArray,tName,keyName, ColumnNames);
//						for(TableEntitySql entity : tableEntities){
//							sqlSession.update("table.modifyDBEntityBatch",entity);	
//						}
//					}
//				}
				String addString = (String)jsonObject.get("add");
				if(addString != null){
					JSONArray addArray = JSON.parseArray(addString);
					if(addArray.size() > 0){
						List<TableEntitySql> tableEntities = toEntity(addArray,tName,keyName, ColumnNames);
						for(TableEntitySql entity : tableEntities){
							sqlSession.insert("table.insertDBEntityBatch",entity);	
						}
					}
					
				}
				sqlSession.commit(true);
			}
			catch(Exception ex){
				sqlSession.rollback(true);
				return ex.getMessage();
			}
			
			
		} catch (Exception ex) {
			return ex.getMessage();
		}
		return "";
	}
}
