package root.report.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.*;
import root.form.user.UserModel;
import root.report.db.DbFactory;
import root.report.sys.SysContext;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/rule")
public class RuleController {

    @RequestMapping(value="/getAuthByConditions",produces = "text/plain;charset=UTF-8")
    public String getAuthByConditions(@RequestBody String pJson) throws UnsupportedEncodingException{
        JSONArray obj = (JSONArray)JSONObject.parse(pJson);
        Map<String,String> map = new HashMap<String,String>();
        map.put("userName", (String) obj.get(0));
        map.put("type", (String) obj.get(1));
        List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("rule.getAuthByConditions",map);
        return JSON.toJSONString(authList);
    }
    @RequestMapping(value="/getAuthListByConditions",produces = "text/plain;charset=UTF-8")
    public String getAuthListByConditions(@RequestBody String pJson) throws UnsupportedEncodingException{
        JSONArray obj = (JSONArray)JSONObject.parse(pJson);
        Map<String,String> map = new HashMap<String,String>();
        map.put("userName", (String) obj.get(0));
        map.put("type", (String) obj.get(1));
        List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("rule.getAuthListByConditions",map);
        return JSON.toJSONString(authList);
    }
    @RequestMapping(value="/getAuthByFuncType",produces = "text/plain;charset=UTF-8")
    public String getAuthByFuncType(@RequestBody String pJson) throws UnsupportedEncodingException{
        JSONArray obj = (JSONArray)JSONObject.parse(pJson);
        Map<String,String> map = new HashMap<String,String>();
        map.put("userName", (String) obj.get(0));
        map.put("type", (String) obj.get(1));
        List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("rule.getAuthByFuncType",map);
        return JSON.toJSONString(authList);
    }
    @RequestMapping(value="/getFunRuleList",produces = "text/plain;charset=UTF-8")
    public String getExcelRuleList(@RequestBody JSONObject pJson) throws UnsupportedEncodingException{
        Map<String,String> map = new HashMap<String,String>();
        map.put("type", pJson.getString("type"));
        //默认查询pid为0的数据
        map.put("pid", "0");
        JSONArray tNode = new JSONArray();
        showExcelRuleTreeNode(map,tNode);
        return tNode.toString();
    }
    public void showExcelRuleTreeNode(Map<String,String> map, JSONArray aNode) {
        List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("rule.getExcelRuleList",map);
        for (Map auth : authList) {
            JSONObject authNode = new JSONObject(true);
            authNode.put("name", auth.get("funcName").toString());
            if(map.get("type").equals("webFunc")){
                authNode.put("value", auth.get("funcName").toString());
            }else{
                authNode.put("value", auth.get("funcId").toString());
            }
            aNode.add(authNode);
            map.put("pid", auth.get("funcId").toString());
            List<Map> childExcelRule = DbFactory.Open(DbFactory.FORM).selectList("rule.getExcelRuleList",map);
            if(childExcelRule.size()>0){
                JSONArray nNode = new JSONArray();
                authNode.put("children", nNode);
                showExcelRuleTreeNode(map,nNode);
            }
        }
        
    }
    @RequestMapping(value="/getFunRuleListReact",produces = "text/plain;charset=UTF-8")
    public String getFunRuleListReact(@RequestBody JSONObject pJson) throws UnsupportedEncodingException{
        Map<String,String> map = new HashMap<String,String>();
        map.put("type", pJson.getString("type"));
        //默认查询pid为0的数据
        map.put("pid", "0");
        JSONArray tNode = new JSONArray();
        showExcelRuleTreeNodeReact(map,tNode);
        return tNode.toString();
    }
    public void showExcelRuleTreeNodeReact(Map<String,String> map, JSONArray aNode) {
        List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("rule.getExcelRuleList",map);
        for (Map auth : authList) {
            JSONObject authNode = new JSONObject(true);
            authNode.put("title", auth.get("funcName").toString());
            if(map.get("type").equals("webFunc")){
                authNode.put("key", auth.get("funcName").toString());
            }else{
                authNode.put("key", auth.get("funcId").toString());
            }
            aNode.add(authNode);
            map.put("pid", auth.get("funcId").toString());
            List<Map> childExcelRule = DbFactory.Open(DbFactory.FORM).selectList("rule.getExcelRuleList",map);
            if(childExcelRule.size()>0){
                JSONArray nNode = new JSONArray();
                authNode.put("children", nNode);
                showExcelRuleTreeNodeReact(map,nNode);
            }
        }

    }
    @RequestMapping(value="/getFuncRuleList",produces = "text/plain;charset=UTF-8")
    public String getFuncRuleList(@RequestBody String pJson) throws UnsupportedEncodingException{
        Map<String,String> map = new HashMap<String,String>();
        map.put("userName", "AUTOINSTALL");
        map.put("type", "excel");
        //默认查询pid为0的数据
        map.put("pid", "0");
        JSONArray tNode = new JSONArray();
        showRuleTreeNode(map,tNode);
        return tNode.toString();
    }
    public void showRuleTreeNode(Map<String,String> map, JSONArray aNode) {
        List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("rule.getFuncRuleList",map);
        for (Map auth : authList) {
            JSONObject authNode = new JSONObject(true);
            authNode.put("authId", auth.get("authId"));
            authNode.put("authType", auth.get("authType"));
            authNode.put("funcId", auth.get("funcId"));
            authNode.put("userId", auth.get("userId"));
            authNode.put("userName", auth.get("userName"));
            authNode.put("funcName", auth.get("funcName"));
            authNode.put("funcType", auth.get("funcType"));
            authNode.put("funcPid", auth.get("funcPid"));
            aNode.add(authNode);
            map.put("pid", (String) auth.get("funcId"));
            List<Map> childAuthList = DbFactory.Open(DbFactory.FORM).selectList("rule.getFuncRuleList",map);
            if(childAuthList.size()>0){
                JSONArray nNode = new JSONArray();
                authNode.put("children", nNode);
                showRuleTreeNode(map,nNode);
            }
        }

    }
    @RequestMapping(value="/getDataList",produces = "text/plain;charset=UTF-8")
    public String getDataList() throws UnsupportedEncodingException{
        List<Map> dataRuleList = DbFactory.Open(DbFactory.SYSTEM).selectList("dataRule.getDataList");
        DbFactory.close(DbFactory.SYSTEM);
        List<Map> dataList = new ArrayList<Map>();
        for (Map dataRule : dataRuleList) {
            Map data = new HashMap();
            data.put("name", dataRule.get("NAME").toString());
            data.put("value", dataRule.get("VALUE").toString());
            dataList.add(data);
        }
        return JSON.toJSONString(dataList);
    }
    @RequestMapping(value="/getDepartmentList",produces = "text/plain;charset=UTF-8")
    public String getDepartmentList() throws UnsupportedEncodingException{
        Map<String,String> map = new HashMap<String,String>();
        //默认查询parentId为0的数据
        map.put("parentId", "0");
        JSONArray tNode = new JSONArray();
        showDepartmentTreeNode(map,tNode);
        return tNode.toString();
    }
    public void showDepartmentTreeNode(Map<String,String> map, JSONArray aNode) {
        List<Map> parentDMList = DbFactory.Open(DbFactory.FORM).selectList("rule.getDepartmentList",map);
        for (Map dm : parentDMList) {
            JSONObject dmNode = new JSONObject(true);
            dmNode.put("name", dm.get("name"));
            dmNode.put("value", dm.get("value").toString());
            dmNode.put("parentId", dm.get("parentId").toString());
            aNode.add(dmNode);
            map.put("parentId", dm.get("value").toString());
            List<Map> childAuthList = DbFactory.Open(DbFactory.FORM).selectList("rule.getDepartmentList",map);
            if(childAuthList.size()>0){
                JSONArray nNode = new JSONArray();
                dmNode.put("children", nNode);
                showDepartmentTreeNode(map,nNode);
            }
        }

    }
    @RequestMapping(value="/getDepartmentListByCid",produces = "text/plain;charset=UTF-8")
    public String getDepartmentListByCid(@RequestBody JSONObject pJson) throws UnsupportedEncodingException{
        Map<String,String> map = new HashMap<String,String>();
        //默认查询parentId为0的数据
        map.put("companyCode",  pJson.getString("companyCode"));
        List<Map> departmentList = DbFactory.Open(DbFactory.FORM).selectList("rule.getDepartmentListByCid",map);
        
        UserModel user = SysContext.getRequestUser();
        if(user.getIsAdmin()==1){
            return JSON.toJSONString(departmentList);
        }else{
            map.put("userName", user.getUserName());
            map.put("type", "DM");
            List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("rule.getAuthListByConditions",map);
            if(authList==null||authList.size()==0){
                return JSON.toJSONString(authList);
            }else{
                List<Map> result = new ArrayList<Map>();
                for(Map m:authList){
                    for(Map n:departmentList){
                        if(m.get("funcId").equals(n.get("value").toString())){
                            result.add(n);
                            break;
                        }
                    }
                }
                return JSON.toJSONString(result);
            }
        }
    }
    @RequestMapping(value="/getDataAuthList/{userName}",produces = "text/plain;charset=UTF-8")
    public String getDataAuthList(@PathVariable("userName") String userName) throws UnsupportedEncodingException{
        Map<String,String> map = new HashMap<String,String>();
        map.put("userName",userName);
        List<Map> dataList = DbFactory.Open(DbFactory.FORM).selectList("rule.getDataAuthList",map);
        return JSON.toJSONString(dataList);
    }
    @RequestMapping(value="/saveRules",produces = "text/plain;charset=UTF-8")
    public String saveRules(@RequestBody String pJson) throws UnsupportedEncodingException{
        JSONArray obj = (JSONArray)JSONObject.parse(pJson);
        JSONArray ruleArray = (JSONArray) obj.get(2);
        JSONObject rule = new JSONObject();
        Map<String,String> map = new HashMap<String,String>();
        map.put("userName", (String) obj.get(0));
        map.put("type", (String) obj.get(1));
        DbFactory.Open(DbFactory.FORM).delete("rule.deleteRules",map);
        for (int i = 0; i < ruleArray.size(); i++) {
            System.out.println(ruleArray.get(i).toString());
            map.put("funcName", ruleArray.get(i).toString());
            DbFactory.Open(DbFactory.FORM).insert("rule.addRules",map);
        }
        
        return JSON.toJSONString(null);
    }
    
    @RequestMapping(value="/saveAuthRules" ,produces = "text/plain;charset=UTF-8")
    public String saveAuthRules(@RequestBody String pJson) throws UnsupportedEncodingException{
        JSONArray obj = (JSONArray)JSONObject.parse(pJson);
        JSONArray ruleArray = (JSONArray) obj.get(2);
        JSONObject rule = new JSONObject();
        Map<String,String> map = new HashMap<String,String>();
        map.put("userName", (String) obj.get(0));
        map.put("type", (String) obj.get(1));
        DbFactory.Open(DbFactory.FORM).delete("rule.deleteRules",map);
        for (int i = 0; i < ruleArray.size(); i++) {
            System.out.println(ruleArray.get(i).toString());
            map.put("funcName", ruleArray.get(i).toString());
            DbFactory.Open(DbFactory.FORM).insert("rule.addAuthRules",map);
        }
        
        
        return JSON.toJSONString(null);
    }

    /**
     * @author  gaoluo
     * date 2018-9-30
     * */
    @RequestMapping(value="/getRoleList",produces = "text/plain;charset=UTF-8")
    public String getRoleList() throws UnsupportedEncodingException{
        List<Map> dataList = DbFactory.Open(DbFactory.FORM).selectList("role.getRoleList");
        return JSON.toJSONString(dataList);
    }

}