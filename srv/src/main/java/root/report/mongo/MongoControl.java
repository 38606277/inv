package root.report.mongo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.configure.AppConstants;
import root.report.common.BaseControl;
import root.report.db.MongoSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/reportServer/mongo")
public class MongoControl extends BaseControl{

	//校验并返回输入内容
	@RequestMapping(value = "/getInputOutputParas", produces = "text/plain;charset=UTF-8")
	public String getInputOutputParas(@RequestBody String pJson) {
		JSONObject retJson = this.doExecute(()->this.validate(pJson));
		retJson.put("content", JSON.parse(pJson));
		return retJson.toJSONString();
	}


	//保存数据查询描述元数据
	@RequestMapping(value = "/saveMetaData", produces = "text/plain;charset=UTF-8")
	public String saveMetaData(@RequestBody String pJson) {
		return this.doExecute(()->this.saveJson(pJson)).toJSONString();
	}

	//解析元数据，执行查询
	@RequestMapping(value = "/run", produces = "text/plain;charset=UTF-8")
	public String run(@RequestBody String pJson) {
		return this.doExecuteWithInnerReturn(()->this.runSql(pJson)).toJSONString();
	}

	private void validate(String pJson){
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) JSON.parse(pJson);
		} catch (Exception e) {
			throw new RuntimeException("解析输入内容出错;");
		}
		if(null != jsonObject){
			String collection = jsonObject.getString("collection");
			String in = jsonObject.getString("in");
			String out = jsonObject.getString("out");
			//校验输入内容
			if(StringUtils.isBlank(collection)){
				throw new RuntimeException("查询集合不能为空;");
			}
			if(StringUtils.isBlank(out)){
				throw new RuntimeException("输出参数不能为空;");
			}
		}
	}

	private void saveJson(String pJson) throws IOException{
		JSONObject metadata = (JSONObject)JSONObject.parse(pJson);
		String dbName = metadata.getString("db");
		String filePath = AppConstants.getMongoTemplate()+File.separator+dbName+".json";

		File file = new File(filePath);
		if(!file.exists())file.createNewFile();
		JSONArray array = null;
		if(file.length()>0){
			int length = Integer.parseInt(String.valueOf(file.length()));
			byte[] bytes = new byte[length];
			IOUtils.readFully(new FileInputStream(file), bytes);
			array = (JSONArray) JSONArray.parse(new String(bytes, "GBK"));
			for(int i=0;i<array.size();i++){
				JSONObject json = array.getJSONObject(i);
				//如果这个对象已经存在， 那么删除该对象
				if(metadata.getString("id").equals(json.getString("id"))){
					array.remove(i);
					break;
				}
			}
		}else{
			array = new JSONArray();
		}
		array.add(metadata);
		IOUtils.write(JSON.toJSONString(array, Boolean.TRUE).getBytes("GBK"), new FileOutputStream(file));
	}

	private JSON runSql(String pJson){
		JSONObject requestJson = (JSONObject)JSONObject.parse(pJson);
		String dbName = requestJson.getString("db");
		String collection = requestJson.getString("collection");
		MongoCollection<Document> col = MongoSession.getDb(dbName).getCollection(collection);
		JSONArray in = requestJson.getJSONArray("in");
		JSONArray out = requestJson.getJSONArray("out");
		//处理查询条件
		JSONObject filter = new JSONObject();
		if(in != null){
			for(int i=0;i<in.size();i++){
				JSONObject condition = in.getJSONObject(i).getJSONObject("condition");
				Set<String> keys = condition.keySet();
				for(String key : keys){
					filter.put(key, condition.get(key));
				}
			}
		}
		if(out == null || out.size()==0) throw new RuntimeException("输出列不能为空！");
		//处理输出
		JSONObject output = new JSONObject();
		JSONObject outputParam = null;
		String outKey = null;
		for(int i=0;i<out.size();i++){
			outputParam = out.getJSONObject(i);
			if(StringUtils.isBlank(outKey = outputParam.getString("projection"))){
				outKey = outputParam.getString("id");
			}
			output.put(outKey, 1);
		}
		output.put("_id", 0);
		FindIterable<Document> it = col.find(Document.parse(filter.toJSONString())).projection(Document.parse(output.toJSONString()));
		JSONArray array = new JSONArray();
		it.iterator().forEachRemaining((x)->array.add(x));
		return array;
	}
	public static void main(String[] args) throws IOException {
		String pJson = "{db:'mongo',metadata:{id:'pre_assessment_query1', name:'22212121212'}}";
		MongoControl c = new MongoControl();
		c.saveJson(pJson);
	}
}
