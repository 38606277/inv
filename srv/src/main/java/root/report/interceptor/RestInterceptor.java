package root.report.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import root.form.user.UserModel;
import root.report.db.DbFactory;
import root.report.db.DbManager;
import root.report.filter.BodyHttpServletRequestWrapper;
import root.report.sys.SysApiLog;
import root.report.sys.SysContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 80845555
 */
public class RestInterceptor extends HandlerInterceptorAdapter {
	private DbManager manager = new DbManager();
	private static ThreadLocal<SysApiLog> logMap = new ThreadLocal<SysApiLog>();
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (request.getHeader("access-control-request-method") != null) {
			return true;
		}
		String credentials = request.getHeader("credentials");
		JSONObject obj = null;
		if (credentials == null) {
			return false;
		} else {
			obj = JSON.parseObject(credentials);
		}
		String userCode = obj.getString("UserCode");
		String pwd = obj.getString("Pwd");
		int isAdmin = 0;
		if (obj.containsKey("isAdmin")) {
			isAdmin = obj.getIntValue("isAdmin");
		}
		// 将用户信息放入上下文
		UserModel userModel = new UserModel();
		userModel.setUserName(userCode);
		userModel.setEncryptPwd(pwd);
		userModel.setIsAdmin(isAdmin);
		SysContext.setRequestUser(userModel);
		this.generateApiInfo(request,handler);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		SysApiLog apiLog = logMap.get();
		if(apiLog!=null){
			Date date = new Date();
			String end_time = sdf.format(date);
			apiLog.setEnd_time(end_time);
			DbFactory.Open(DbFactory.FORM).insert("system.insertSysApiLog", apiLog);
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		closeConn2Datasource();
		clearUserInfo();
	}

	// 每次调用完服务即清空临时用户信息,临时用户信息用于指明服务调用者
	private void clearUserInfo() {
		SysContext.setRequestUser(null);
	}

	// 每次调用MVC服务后释放连接
	private void closeConn2Datasource() {
		String allDBConnections = manager.getAllDBConnections();
		JSONArray array = JSON.parseArray(allDBConnections);
		JSONObject obj = null;
		String dbType = null;
		String dbName = null;
		for (int i = 0; i < array.size(); i++) {
			obj = array.getJSONObject(i);
			dbType = obj.getString("dbtype");
			dbName = obj.getString("name");
			if ("Oracle".equals(dbType) || "Mysql".equals(dbType) || "DB2".equals(dbType)) {
				DbFactory.close(dbName);
			}
		}
	}

	private void generateApiInfo(HttpServletRequest request,Object handler) {
		try {
			SysApiLog api = new SysApiLog();
			JSONObject client = new JSONObject();
			client.put("remoteIP", getIpAddress(request));
			client.put("userName", SysContext.getRequestUser().getUserName());
			String api_code = request.getRequestURI();
			Date date = new Date();
			String begin_time = sdf.format(date);
			api.setApi_code(api_code);
			api.setBegin_time(begin_time);
			api.setClient(client.toJSONString());
			BodyHttpServletRequestWrapper bodyRequestWrapper= new BodyHttpServletRequestWrapper(request);
			String pJson = new String(bodyRequestWrapper.getRequestBody(),"utf-8").replaceAll("\\s*|\t|\r|\n", "");
			if(pJson.length()>1000){
				pJson = pJson.substring(0, 1000);
			}
			api.setIn_param(pJson);
			//HandlerMethod method = (HandlerMethod)handler;
			//RequestMapping  requestMapping = method.getMethod().getAnnotation(RequestMapping.class);
			//api.setApi_name(requestMapping.name());
			logMap.set(api);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}
