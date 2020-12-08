package root.report.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public abstract class BaseControl extends RO{
	
	protected static String MESSAGE = "message";
	protected static String SUCCESS = "success";
	
	/**
	 * 该方法主要用于封装返回的JSON对象，给返回对象添加succes和message属性
	 * @param reps
	 * @return
	 */
	protected JSONObject doExecute(ReportExecuter reps){
		JSONObject retJson = new JSONObject();
		try {
			reps.execute();
			retJson.put(SUCCESS, Boolean.TRUE);
		} catch (Exception e) {
			e.printStackTrace();
			retJson.put(SUCCESS, Boolean.FALSE);
			retJson.put(MESSAGE, e.getMessage());
		}
		return retJson;
	}
	
	protected JSONObject doExecuteWithInnerReturn(ReportExecuterWithReturn<JSON> reps){
		JSONObject retJson = new JSONObject();
		try {
			JSON result = reps.execute();
			retJson.put(SUCCESS, Boolean.TRUE);
			retJson.put("result", result);
		} catch (Exception e) {
			e.printStackTrace();
			retJson.put(SUCCESS, Boolean.FALSE);
			retJson.put(MESSAGE, e.getMessage());
		}
		return retJson;
	}
	
	protected String doExecuteWithROReturn(ReportExecuterWithReturn<?> reps){
		try {
			Object result = reps.execute();
			return this.SuccessMsg("操作成功", result);
		} catch (Exception e) {
			e.printStackTrace();
			return this.ExceptionMsg(e.getMessage());
		}
	}
}
