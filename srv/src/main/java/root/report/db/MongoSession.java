package root.report.db;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import java.util.HashMap;
import java.util.Map;

public class MongoSession {
	private static Map<String, MongoClient> mongoMap = new HashMap<String, MongoClient>();

	public static MongoClient getClient(String dbName){
		if(mongoMap.get(dbName) == null){
			DbManager dbManager = new DbManager();
			String dbJson = dbManager.getDBConnectionByName(dbName);
			JSONObject dbObj = (JSONObject)JSONObject.parse(dbJson);
			String url = dbObj.getString("url");
			String[] address = url.split(":");
			MongoClient client = new MongoClient(address[0],Integer.parseInt(address[1]));
			mongoMap.put(dbName, client);
		}
		return mongoMap.get(dbName);
	}

	public static MongoDatabase getDb(String dbName){
		MongoClient client = MongoSession.getClient(dbName);
		return client.getDatabase(dbName);
	}
}
