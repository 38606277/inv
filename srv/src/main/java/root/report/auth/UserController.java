package root.report.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import root.form.user.UserModel;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.sys.SysContext;
import root.report.util.ErpUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reportServer/user")
public class UserController extends RO{
	
    private static final Logger log = Logger.getLogger(UserController.class);

	@RequestMapping(value="/showUser",method=RequestMethod.POST,produces = "text/plain;charset=UTF-8")
	public String toIndex(HttpServletRequest request, Model model) {
		int userId = Integer.parseInt(request.getParameter("id"));

		model.addAttribute("user", "wj");
		return "showUser";
	}

	/**
	 * UserCode 用户ID
	 * Pwd 用户密码des3加密后的
	 * @param pJson
	 * @return 成功返回Y 否则返回N
	 */
	@RequestMapping(value="/login",method=RequestMethod.POST,produces = "text/plain;charset=UTF-8")
	public String login(@RequestBody String pJson) {
		log.debug("调用服务：/user/login");
		Map<String, Object> map = new HashMap<String, Object>();
		try{
			JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
			String userCode = jsonObject.getString("UserCode");
			//转换成小写
			userCode = userCode!=null?userCode.trim().toLowerCase():userCode;
			String passWord = jsonObject.getString("Pwd");
            ErpUtil erpUtil = new ErpUtil();
            passWord = erpUtil.decode(passWord);
			//查询用户信息
			UserModel userModel = DbFactory.Open(DbFactory.FORM).selectOne("formUser.getUserInfoByUserId", userCode);
			List<Map<String, Object>> rList = new ArrayList<Map<String, Object>>();
			//用户不存在
			if(userModel==null) {
				map.put("LOGINRESULT", "InvalidUser");
				return JSON.toJSONString(map);
			}//如果是erp用户，则到Erp中验证密码
			else if(userModel!=null&&"erp".equals(userModel.getRegisType())){
        		map.put("userCode", userCode);
        		map.put("pwd", passWord);
        		map = DbFactory.Open(DbFactory.SYSTEM).selectOne("role.loginUser", map);
			}
			//如果是本地用户，则验证密码
			else if(userModel!=null&&"local".equals(userModel.getRegisType())){
                String encryptPwd = erpUtil.encode(passWord);
			    if(encryptPwd.equals(userModel.getEncryptPwd())) {
			        map.put("LOGINRESULT", "Y");
			    } else {
			        map.put("LOGINRESULT", "N");
			    }
			}
            int isAdmin = userModel.getIsAdmin();
            String regisType = userModel.getRegisType();
            map.put("isAdmin", isAdmin);
            map.put("import", regisType);
            return JSON.toJSONString(map);
		} catch (Exception e) {
			log.error("登录异常："+e.getMessage());
			e.printStackTrace();
			map.put("LOGINRESULT","Exception");
			map.put("Message",e.getMessage());
			return JSON.toJSONString(map);
		} finally {
			//由于不走拦截器,需手动关闭连接
			DbFactory.close(DbFactory.SYSTEM);
			DbFactory.close(DbFactory.FORM);
		}
	}

	/**
	 * UserCode 用户ID
	 * Pwd 用户密码des3加密后的
	 * @param pJson
	 * @return 成功返回Y 否则返回N
	 */
	@RequestMapping(value="/Reactlogin",method=RequestMethod.POST,produces = "text/plain;charset=UTF-8")
	public String Reactlogin(@RequestBody String pJson) {
		log.debug("调用服务：/user/login");
		Map<String, Object> map = new HashMap<String, Object>();
		try{
			JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
			String userCode = jsonObject.getString("UserCode");
			//转换成小写
			userCode = userCode!=null?userCode.trim().toLowerCase():userCode;
			String passWord = jsonObject.getString("Pwd");
			ErpUtil erpUtil = new ErpUtil();
			passWord = erpUtil.decode(passWord);
			//查询用户信息
			UserModel userModel = DbFactory.Open(DbFactory.FORM).selectOne("formUser.getUserInfoByUserId", userCode);
			List<Map<String, Object>> rList = new ArrayList<Map<String, Object>>();
			//用户不存在
			if(userModel==null) {
				map.put("LOGINRESULT", "InvalidUser");
				return JSON.toJSONString(map);
			}//如果是erp用户，则到Erp中验证密码
			else if(userModel!=null&&"erp".equals(userModel.getRegisType())){
				String encryptPwd = erpUtil.encode(passWord);
				if(encryptPwd.equals(userModel.getEncryptPwd())) {
					map.put("LOGINRESULT", "Y");
				} else {
					map.put("LOGINRESULT", "N");
				}
				map.put("userCode", userCode);
				map.put("pwd", passWord);
				//map = DbFactory.Open(DbFactory.SYSTEM).selectOne("role.loginUser", map);
			}
			//如果是本地用户，则验证密码
			else if(userModel!=null&&"local".equals(userModel.getRegisType())){
				String encryptPwd = erpUtil.encode(passWord);
				if(encryptPwd.equals(userModel.getEncryptPwd())) {
					map.put("LOGINRESULT", "Y");
				} else {
					map.put("LOGINRESULT", "N");
				}
			}

			map.put("userCode", userCode);
			map.put("pwd", passWord);
			map.put("id", userModel.getId());
			map.put("userId", userModel.getUserId());
			int isAdmin = userModel.getIsAdmin();
			String regisType = userModel.getRegisType();
			map.put("isAdmin", isAdmin);
			map.put("import", regisType);
			map.put("icon", userModel.getIcon());
			Map<String, Object> map2 = new HashMap<String, Object>();
			map2.put("status",0);
			map2.put("data",map);
			map2.put("msg","登录成功");
			return JSON.toJSONString(map2);
		} catch (Exception e) {
			log.error("登录异常："+e.getMessage());
			e.printStackTrace();
			map.put("LOGINRESULT","Exception");
			map.put("Message",e.getMessage());
			return JSON.toJSONString(map);
		} finally {
			//由于不走拦截器,需手动关闭连接
			DbFactory.close(DbFactory.SYSTEM);
			DbFactory.close(DbFactory.FORM);
		}
	}

	/**
	 * @param pwd 用户密码(未加密)
	 * @return 成功返回Y 否则返回N
	 * @throws UnsupportedEncodingException 
	 */
	@RequestMapping(value="/encodePwd",method=RequestMethod.POST,produces = "text/plain;charset=UTF-8")
	public String encodePwd(@RequestBody String pwd) throws UnsupportedEncodingException
	{
		String encodePwd = "";
		JSONObject obj = new JSONObject();
		try {
			ErpUtil erp = new ErpUtil();
			pwd = URLDecoder.decode(pwd,"utf-8");
			encodePwd = erp.encode(pwd);
			obj.put("encodePwd", encodePwd);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			obj.put("encodePwd", "");
		}
		return JSON.toJSONString(obj);
	}

	/**
	 * @param pwd 用户密码(未加密)
	 * @return 成功返回Y 否则返回N
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value="/encodePwdReact",method=RequestMethod.POST,produces = "text/plain;charset=UTF-8")
	public String encodePwdReact(@RequestBody String pwd) throws UnsupportedEncodingException
	{
		String encodePwd = "";
		JSONObject jsonObject = (JSONObject) JSON.parse(pwd);
		String pwdv = jsonObject.getString("Pwd");
		try {
			ErpUtil erp = new ErpUtil();
			pwdv = URLDecoder.decode(pwdv,"utf-8");
			encodePwd = erp.encode(pwdv);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("status",0);
		map2.put("data",encodePwd);
		map2.put("msg","登录成功");
		return JSON.toJSONString(map2);
	}


	@RequestMapping(value = "/getUserListRows",method=RequestMethod.POST, produces = "text/plain; charset=utf-8")
    public String getUserListRows(@RequestBody String pJson){
        JSONArray obj = (JSONArray)JSONObject.parse(pJson);
        Map<String,Object> map = new HashMap<String,Object>();
        if(obj.get(0)==null){
            map.put("userName", "");
        }else{
            map.put("userName", obj.get(0));
        }
        int totalRows = DbFactory.Open(DbFactory.FORM).selectOne("user.getUserListRows",map);
        return JSON.toJSONString(totalRows);
    }
    
    @RequestMapping(value="/getUserList",method=RequestMethod.POST,produces = "text/plain;charset=UTF-8")
    public String getUserList(@RequestBody String pJson) throws UnsupportedEncodingException{
        JSONArray obj = (JSONArray)JSONObject.parse(pJson);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("startIndex", ((JSONObject)obj.get(0)).get("startIndex"));
        map.put("perPage", ((JSONObject)obj.get(0)).get("perPage"));
        if(obj.get(1)==null){
            map.put("userName", "");
        }else{
            map.put("userName", obj.get(1));
        }
        List<Map> userList = DbFactory.Open(DbFactory.FORM).selectList("user.getAllUser",map);
        return JSON.toJSONString(userList);
    }
    
    @RequestMapping(value="/isAdmin/{credentials}",method=RequestMethod.POST,produces = "text/plain;charset=UTF-8")
    public String isAdmin(@PathVariable("credentials") String credentials) throws UnsupportedEncodingException
    {
        JSONObject json = (JSONObject)JSONObject.parse(credentials);
        String userCode = (String)json.get("UserCode");
        int isAdmin =  DbFactory.Open(DbFactory.FORM).selectOne("user.isAdmin",userCode);
       
        return JSON.toJSONString(isAdmin);
    }
    
    @RequestMapping(value="/qryMenuList",produces = "text/plain;charset=UTF-8")
    public @ResponseBody String qryMenuList()
    {
    	Map<String,String> map = new HashMap<String,String>();
    	map.put("userName", SysContext.getRequestUser().getUserName());
    	List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("user.qryMenuList",map);
        return JSON.toJSONString(authList);
    }
    
    @RequestMapping(value="/getBudgetReport",produces = "text/plain;charset=UTF-8")
    public String getBudgetReport()
    {
    	Map<String,String> map = new HashMap<String,String>();
    	map.put("userName", SysContext.getRequestUser().getUserName());
    	List<Map<String,Object>> authList = null;
    	//if(SysContext.getRequestUser().getIsAdmin()==1){
    		authList = DbFactory.Open(DbFactory.FORM).selectList("user.qryAllBudgetReportMenu",map);
    	//}else{
    	//	authList = DbFactory.Open(DbFactory.FORM).selectList("user.qryBudgetReportMenu",map);
    	//}
    	JSONArray arr = new JSONArray();
    	getBudgetmenu(arr,authList,103);
        return JSON.toJSONString(arr);
    }
    
    @RequestMapping(value="/modifyPasswd",produces = "text/plain;charset=UTF-8")
    public String modifyPasswd(@RequestBody JSONObject pJson){
    	String user_name = pJson.getString("user_name");
    	String origin_pwd = pJson.getString("origin_pwd");
    	String modify_pwd = pJson.getString("modify_pwd");
    	UserModel userModel = DbFactory.Open(DbFactory.FORM).selectOne("formUser.getUserInfoByUserId", user_name);
    	try{
    		JSONObject passwdObj = JSONObject.parseObject(encodePwd(origin_pwd));
	    	if(passwdObj.getString("encodePwd").equals(userModel.getEncryptPwd())){
	    		passwdObj = JSONObject.parseObject(encodePwd(modify_pwd));
	    		userModel.setEncryptPwd(passwdObj.getString("encodePwd"));
	    		DbFactory.Open(DbFactory.FORM).update("formUser.updateUser",userModel);
	    	}else{
	    		return ErrorMsg("2001", "修改失败,旧密码错误");
	    	}
    	}catch(Exception e){
    		return ErrorMsg("2000", "修改失败,密码加密错误");
    	}
    	return SuccessMsg("修改成功", null);
    }
    
    private void getBudgetmenu(JSONArray arr,List<Map<String,Object>> authList,int parentId){
    	JSONObject obj = null;
    	List<Map<String,Object>> list = authList.stream().parallel().filter(m->{
			if((Integer)m.get("func_pid")==parentId){
				return true;
			}
			return false;
	    }).collect(Collectors.toList());
		for (Map<String,Object> temp:list) {
			obj = new JSONObject();
			int func_id = (Integer)temp.get("func_id");
			int func_pid = (Integer)temp.get("func_pid");
			obj.put("func_id", func_id);
			obj.put("func_name", (String)temp.get("func_name"));
			
			List<Map<String,Object>> childList = authList.stream().parallel().filter(m->{
				if((Integer)m.get("func_pid")==func_id){
					return true;
				}
				return false;
		    }).collect(Collectors.toList());
			
			boolean isReportFile = childList==null||childList.size()==0?true:false;
			obj.put("isReportFile", isReportFile);
			if(!isReportFile){
				JSONArray childArr = new JSONArray();
				obj.put("children", childArr);
				getBudgetmenu(childArr,authList,func_id);
			}
			arr.add(obj);
		}
    }
}