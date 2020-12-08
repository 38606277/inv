package root.report.query;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Template {

	
	
	//dataTable1.key=dataTable2.key
	//
	public void ExecTemplate() {
		
		
		
		HashMap<String, JSONObject> dataTable1 = new HashMap<>();
		HashMap<String, JSONObject> dataTable2 = new HashMap<>();
		HashMap<String, JSONObject> dataTable3 = new HashMap<>();
		HashMap<String, JSONObject> dataTable4 = new HashMap<>();

		Iterator iter = dataTable1.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();

			JSONObject row1=(JSONObject)dataTable1.get(key);
			JSONObject row2=(JSONObject)dataTable2.get(key);
			MergeJSONObject(row1, row2);
			
			dataTable3.get(key);
			dataTable4.get(key);

		}
		

	}
	
	private void MergeJSONObject(JSONObject j1,JSONObject j2) {
		
		
	}

}
