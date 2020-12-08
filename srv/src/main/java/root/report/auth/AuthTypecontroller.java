package root.report.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import root.report.common.RO;
import root.report.db.DbFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

@Controller
@RequestMapping("/reportServer/authType")
public class AuthTypecontroller extends RO {
    @RequestMapping(value = "/getAllAuthTypeList", produces = "text/plain;charset=UTF-8")
    public @ResponseBody String getAllAuthTypeList() {
        try{
            List<Map> authTypeList = DbFactory.Open(DbFactory.FORM)
                    .selectList("authType.getAllAuthTypeList");
            return SuccessMsg("查询成功", authTypeList);
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }

    @RequestMapping(value = "/getAuthTypeListByType", produces = "text/plain;charset=UTF-8")
    public @ResponseBody String getAuthTypeListByType(@RequestBody JSONObject pJson) {
        List<Map<String, Object>> list = new ArrayList<>();
        try{
            String aythTypeName = pJson.getString("authType");
            Map authType = DbFactory.Open(DbFactory.FORM).selectOne("authType.getAuthTypeByName",aythTypeName);

            Statement stat = DbFactory.Open(authType.get("auth_db").toString()).getConnection().createStatement();
            ResultSet set = stat.executeQuery(authType.get("auth_sql").toString());
            ResultSetMetaData rsmd = set.getMetaData();
            int cc = rsmd.getColumnCount();
            while (set.next()) {
                Map<String, Object> retMap = new LinkedHashMap<String, Object>(cc);
                list.add(retMap);
                for (int i = 1; i <= cc; i++) {
                    retMap.put(rsmd.getColumnLabel(i).toLowerCase(), set.getObject(i));
                }
            }
            return SuccessMsg("查询成功", list);
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }


    @RequestMapping(value = "/getAuthTypeListByName", produces = "text/plain;charset=UTF-8")
    public @ResponseBody String getAuthTypeListByName(@RequestBody String name) {
        try{
            Map authType = DbFactory.Open(DbFactory.FORM).selectOne("authType.getAuthTypeByName",name);
            return SuccessMsg("查询成功", authType);
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }
    @RequestMapping(value = "/saveAuthType", produces = "text/plain;charset=UTF-8")
    public @ResponseBody String saveAuthType(@RequestBody String pJson) {
        try{
            JSONObject obj = (JSONObject) JSON.parse(pJson);
            Map param = new HashMap<>();
            param.put("authTypeName", obj.getString("authtype_name"));
            param.put("authTypeDesc", obj.getString("authtype_desc"));
            param.put("authTypeClass", obj.getString("authtype_class"));
            param.put("useObject", obj.getString("use_object"));
            param.put("authDb", obj.getString("auth_db"));
            param.put("authName", obj.getString("auth_name"));
            param.put("authSql", obj.getString("auth_sql"));
            Map authType = DbFactory.Open(DbFactory.FORM).selectOne("authType.getAuthTypeByName",obj.getString("authtype_name"));
            if(authType!=null){
                return ErrorMsg("3000", "权限类型名称重复");
            }
            int num = DbFactory.Open(DbFactory.FORM).insert("authType.addAuthType",param);
            if(num==1){
                return SuccessMsg("添加成功", null);
            }else{
                return ErrorMsg("3000", "添加失败");
            }
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }
    @RequestMapping(value = "/updateAuthType", produces = "text/plain;charset=UTF-8")
    public @ResponseBody String updateAuthType(@RequestBody String pJson) {
        try{
            JSONObject obj = (JSONObject) JSON.parse(pJson);
            Map param = new HashMap<>();
            param.put("authTypeId", obj.getString("authtype_id"));
            param.put("authTypeName", obj.getString("authtype_name"));
            param.put("authTypeDesc", obj.getString("authtype_desc"));
            param.put("authTypeClass", obj.getString("authtype_class"));
            param.put("useObject", obj.getString("use_object"));
            param.put("authDb", obj.getString("auth_db"));
            param.put("authName", obj.getString("auth_name"));
            param.put("authSql", obj.getString("auth_sql"));
            Map authType = DbFactory.Open(DbFactory.FORM).selectOne("authType.getAuthTypeByName",obj.getString("authtype_name"));
            if(authType!=null&&!authType.get("authtype_id").toString().equals(obj.getString("authtype_id").toString())){
                return ErrorMsg("3000", "权限类型名称重复");
            }
            int num = DbFactory.Open(DbFactory.FORM).update("authType.updateAuthType",param);
            if(num==1){
                return SuccessMsg("修改成功", null);
            }else{
                return ErrorMsg("3000", "修改失败");
            }
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }

    }
    @RequestMapping(value="/deleteAuthType",produces = "text/plain;charset=UTF-8")
    public @ResponseBody String deleteAuthType(@RequestBody String name)
    {
        try{
            int num = DbFactory.Open(DbFactory.FORM).delete("authType.deleteAuthTypeByName", name);
            if(num==1){
                return SuccessMsg("删除成功", null);
            }else{
                return ErrorMsg("3000", "删除失败");
            }
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }
    }
}
