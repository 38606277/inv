package root.report.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.form.user.UserModel;
import root.report.db.DbFactory;
import root.report.sys.SysContext;
import root.report.util.ErpUtil;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/reportServer/role")
public class RoleController {
    private static final Logger log = Logger.getLogger(RoleController.class);

    @RequestMapping(value = "/getRoleList", produces = "text/plain; charset=utf-8")
    public String getRoleList(@RequestBody String pJson)
    {
        JSONObject obj = (JSONObject) JSON.parse(pJson);
        Map<String,Object> map = new HashMap<String,Object>();
        int currentPage=Integer.valueOf(obj.getIntValue("pageNum"));
        int perPage=Integer.valueOf(obj.getIntValue("perPage"));
        if(1==currentPage|| 0==currentPage){
            currentPage=0;
        }else{
            currentPage=(currentPage-1)*perPage;
        }
        map.put("startIndex", currentPage);
        map.put("perPage",perPage);
        map.put("roleName",  obj.get("roleName")==null?"":obj.getString("roleName"));
        map.put("roleId",  obj.get("roleId")==null?"":obj.getString("roleId"));
        List<RoleModel> rolelist = DbFactory.Open(DbFactory.FORM).selectList("role.getRolesList",map);
        int total=DbFactory.Open(DbFactory.FORM).selectOne("role.countRole", map);
        Map<String,Object> map3 =new HashMap<String,Object>();
        map3.put("list",rolelist);
        map3.put("total",total);
        return JSON.toJSONString(map3);
    }

    @RequestMapping(value = "/getRoleInfoById", produces = "text/plain; charset=utf-8")
    public String getRoleInfoById(@RequestBody int id)
    {
        RoleModel rolemodel = DbFactory.Open(DbFactory.FORM).selectOne("role.getRoleInfoById",id);
        return JSON.toJSONString(rolemodel);
    }

    @RequestMapping(value = "/addRole", produces = "text/plain; charset=utf-8")
    public String addUser(@RequestBody String pJson)
    {
        String currentUser = SysContext.getRequestUser().getUserName();
        Date date=new Date();
        JSONObject obj = new JSONObject();
        try{
            RoleModel roleModel = JSONObject.parseObject(pJson, RoleModel.class);
            roleModel.setCreatedBy(currentUser);
            roleModel.setCreatedDate(date.toString());
            DbFactory.Open(DbFactory.FORM).insert("role.addRole", roleModel);

            obj.put("result", "success");
        }catch(Exception e){
            log.error("新增角色失败!");
            obj.put("result", "error");
            obj.put("errMsg", "新增角色失败!");
            e.printStackTrace();
        }
        return JSON.toJSONString(obj);
    }

    @RequestMapping(value = "/updateRole", produces = "text/plain; charset=utf-8")
    public String updateRole(@RequestBody String pJson)
    {
        String currentUser = SysContext.getRequestUser().getUserName();
        Date date=new Date();
        SimpleDateFormat s=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String curDate;  //当前日期
        curDate = s.format(date);
        String result = "false";
        JSONObject obj = new JSONObject();
        Map<String,Object> map1=new HashMap<String,Object>();
        try{
            RoleModel roleModel = JSONObject.parseObject(pJson, RoleModel.class);
            RoleModel current_roleModel = (RoleModel)DbFactory.Open(DbFactory.FORM).selectOne("role.getRoleInfoById",roleModel.getRoleId());
            roleModel.setCreatedDate(current_roleModel.getCreatedDate());
            roleModel.setCreatedBy(current_roleModel.getCreatedBy());
            roleModel.setLastUpdatedBy(currentUser);
          //  roleModel.setLastUpdatedDate(curDate);
            //修改數據
            DbFactory.Open(DbFactory.FORM).update("role.updateRole", roleModel);

            result = "success";
            obj.put("result", "success");
        }catch(Exception e){
            log.error("修改用户失败!");
            obj.put("result", "error");
            e.printStackTrace();
        }
        return JSON.toJSONString(obj);
    }

    @RequestMapping(value = "/deleteRole", produces = "text/plain; charset=utf-8")
    public String deleteUser(@RequestBody int id)
    {
        String result = "false";
        JSONObject obj = new JSONObject();
        try{
            DbFactory.Open(DbFactory.FORM).delete("role.deleteRole", id);
            result = "success";
            obj.put("result", "success");
        }catch(Exception e){
            log.error("删除用户失败!");
            //e.printStackTrace();
            obj.put("result", "error");
        }
        return JSON.toJSONString(obj);
    }
    @RequestMapping(value = "/getUserListByRoleId", produces = "text/plain; charset=utf-8")
    public String getUserListByRoleId(@RequestBody String roleId)
    {
        Map<String,Object> map =new HashMap<String,Object>();
        map.put("roleId",roleId);
        List rolelist = DbFactory.Open(DbFactory.FORM).selectList("role.getUserListByRoleId",map);
        return JSON.toJSONString(rolelist);
    }
    @RequestMapping(value = "/saveOrupdateUserId", produces = "text/plain; charset=utf-8")
    public String saveOrupdateUserId(@RequestBody String pJson)
    {
        JSONObject obj = (JSONObject) JSON.parse(pJson);
        String roleId = obj.getString("roleId");;
        List<String> userArray = (List<String>) obj.get("userList");

        Map<String,Object> map = new HashMap<String,Object>();
        map.put("role_id", roleId);
        DbFactory.Open(DbFactory.FORM).delete("role.deleteUserRole",map);

        Map<String,Object> resultmap=new HashMap<String,Object>();
        try{
            for (int i = 0; i < userArray.size(); i++) {
                map.put("user_id", userArray.get(i));
                DbFactory.Open(DbFactory.FORM).insert("role.addRoleUser",map);
            }
            resultmap.put("result", "success");
        }catch(Exception e){
            log.error("保存失败!");
            resultmap.put("result", "error");
            e.printStackTrace();
        }
        return JSON.toJSONString(resultmap);
    }


}
