package root.report.leeutils;

import com.alibaba.fastjson.JSON;

/**
 * @author
 */
public final class JsonTool {

	public static String formatJson(final Object obj) {		
		return JSON.toJSONString(obj,true);
	}

}
