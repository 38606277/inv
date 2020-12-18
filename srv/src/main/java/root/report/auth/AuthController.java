package root.report.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;
import root.form.user.UserModel;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.service.FunctionService;
import root.report.service.QueryService;
import root.report.sys.SysContext;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

@RestController
@RequestMapping("/reportServer/auth")
public class AuthController extends RO {
    @Autowired
    private QueryService queryService;
    @Autowired
    private FunctionService functionService;

    @RequestMapping(value="/getAuthByConditions",produces = "text/plain;charset=UTF-8")
    public String getAuthByConditions(@RequestBody String pJson) throws UnsupportedEncodingException {
        JSONArray obj = (JSONArray) JSONObject.parse(pJson);
        Map<String,String> map = new HashMap<String,String>();
        map.put("roleId", obj.get(0).toString());
        map.put("type", (String) obj.get(1));
        List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("auth.getAuthByConditions",map);
        return JSON.toJSONString(authList);
    }
    @RequestMapping(value="/getAuthByConditionsTable",produces = "text/plain;charset=UTF-8")
    public String getAuthByConditionsTable(@RequestBody String pJson) throws UnsupportedEncodingException {
        JSONArray obj = (JSONArray) JSONObject.parse(pJson);
        Map<String,String> map = new HashMap<String,String>();
        List<Map> authTypeList = DbFactory.Open(DbFactory.FORM).selectList("authType.getAllAuthTypeList");
        List<Map> listss = new ArrayList<>();
        map.put("roleId", obj.get(0).toString());
        for (int i = 0; i <authTypeList.size() ; i++) {
            Map rule = authTypeList.get(i);
            String aythTypeName = rule.get("value").toString();
            map.put("type", aythTypeName);
            List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("auth.getAuthByConditionsTable",map);
//            String sql="SELECT a.auth_id as authId,a.auth_type as authType, CONCAT('"+aythTypeName+"/',a.func_id) as funcId,a.role_id as roleId,u.role_NAME as roleName,f.func_name as funcName,f.func_type as funcType"
//                    +"from fnd_auth a LEFT JOIN fnd_role u on a.role_id = u.role_ID"
//                    +" LEFT JOIN fnd_func f on a.func_id = f.func_id  where u.role_id = '"+roleId+"' and a.auth_type ='"+aythTypeName+"'";
//            List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("authType.getAuthByConditionsTable", sql);
            listss.addAll(authList);
        }
        return JSON.toJSONString(listss);
    }

    @RequestMapping(value="/getAuthListByConditions",produces = "text/plain;charset=UTF-8")
    public String getAuthListByConditions(@RequestBody String pJson) throws UnsupportedEncodingException{
        JSONArray obj = (JSONArray)JSONObject.parse(pJson);
        Map<String,String> map = new HashMap<String,String>();
        map.put("roleId", obj.get(0).toString());
        map.put("type", (String) obj.get(1));
        List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("auth.getAuthListByConditions",map);
        return JSON.toJSONString(authList);
    }
    @RequestMapping(value="/getAuthByFuncType",produces = "text/plain;charset=UTF-8")
    public String getAuthByFuncType(@RequestBody String pJson) throws UnsupportedEncodingException{
        JSONArray obj = (JSONArray)JSONObject.parse(pJson);
        Map<String,String> map = new HashMap<String,String>();
        map.put("roleId",  obj.get(0).toString());
        map.put("type", (String) obj.get(1));
        List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("auth.getAuthByFuncType",map);
        return JSON.toJSONString(authList);
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
        List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("auth.getExcelRuleList",map);
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
            List<Map> childExcelRule = DbFactory.Open(DbFactory.FORM).selectList("auth.getExcelRuleList",map);
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
        List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("auth.getFuncRuleList",map);
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
            List<Map> childAuthList = DbFactory.Open(DbFactory.FORM).selectList("auth.getFuncRuleList",map);
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
        List<Map> parentDMList = DbFactory.Open(DbFactory.FORM).selectList("auth.getDepartmentList",map);
        for (Map dm : parentDMList) {
            JSONObject dmNode = new JSONObject(true);
            dmNode.put("name", dm.get("name"));
            dmNode.put("value", dm.get("value").toString());
            dmNode.put("parentId", dm.get("parentId").toString());
            aNode.add(dmNode);
            map.put("parentId", dm.get("value").toString());
            List<Map> childAuthList = DbFactory.Open(DbFactory.FORM).selectList("auth.getDepartmentList",map);
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
        List<Map> departmentList = DbFactory.Open(DbFactory.FORM).selectList("auth.getDepartmentListByCid",map);

        UserModel user = SysContext.getRequestUser();
        if(user.getIsAdmin()==1){
            return JSON.toJSONString(departmentList);
        }else{
            map.put("userName", user.getUserName());
            map.put("type", "DM");
            List<Map> authList = DbFactory.Open(DbFactory.FORM).selectList("auth.getAuthListByConditions",map);
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
        List<Map> dataList = DbFactory.Open(DbFactory.FORM).selectList("auth.getDataAuthList",map);
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
        DbFactory.Open(DbFactory.FORM).delete("auth.deleteRules",map);
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
        Map<String,String> map = new HashMap<String,String>();
        String type= obj.get(1).toString();
        map.put("roleId",obj.get(0).toString());
        map.put("type",type);
        if(!type.equalsIgnoreCase("table")){
            DbFactory.Open(DbFactory.FORM).delete("auth.deleteRules",map);
            for (int i = 0; i < ruleArray.size(); i++) {
                map.put("funcName", ruleArray.get(i).toString());
                DbFactory.Open(DbFactory.FORM).insert("auth.addAuthRules",map);
            }
        }else{
            List<Map> authTypeList = DbFactory.Open(DbFactory.FORM).selectList("authType.getAllAuthTypeList");
            for (int i = 0; i <authTypeList.size();i++) {
                Map rule = authTypeList.get(i);
                String aythType = rule.get("value").toString();
                map.put("type",aythType);
                DbFactory.Open(DbFactory.FORM).delete("auth.deleteRules",map);
            }
            for (int ii = 0; ii < ruleArray.size(); ii++) {
                String vals=ruleArray.get(ii).toString();
                String[] arr=null;
                arr=vals.split("/");
                if(arr.length>0){
                    map.put("type",arr[0]);
                    map.put("funcName", arr[1]);
                    DbFactory.Open(DbFactory.FORM).insert("auth.addAuthRules",map);
                }
            }
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
    /**
     * @author  gaoluo
     * date 2018-10-16
     * */
    @RequestMapping(value="/getRoleListByUserId",produces = "text/plain;charset=utf-8")
    public String getRoleListByUserId(@RequestBody String pJson) throws UnsupportedEncodingException{
        JSONObject obj = JSONObject.parseObject(pJson);
        List<Map> dataList = DbFactory.Open(DbFactory.FORM).selectList("role.getRoleList");
        Map<String,Object> map =new HashMap<String,Object>();
        map.put("userId",obj.get("userid").toString());
        List rolelist = DbFactory.Open(DbFactory.FORM).selectList("role.getRoleListByUserId",map);
        Map<String,Object> maps =new HashMap<String,Object>();
        maps.put("roleList",dataList);
        maps.put("userroleList",rolelist);
        return JSON.toJSONString(maps);
    }
    //数据权限合并
    @RequestMapping(value = "/getAllAuthTypeList", produces = "text/plain;charset=UTF-8")
    public @ResponseBody  String getAllAuthTypeList() {
        try{
            List<Map> authTypeList = DbFactory.Open(DbFactory.FORM).selectList("authType.getAllAuthTypeList");
            List<Map<String, Object>> listss = new ArrayList<>();
            for (int s = 0; s <authTypeList.size() ; s++) {
                List<Map<String, Object>> list = new ArrayList<>();
                Map rule =authTypeList.get(s);
                String aythTypeName =rule.get("value").toString();
                Map m=new HashMap<>();
                m.put("key",aythTypeName+"/"+aythTypeName);
                m.put("title",aythTypeName);
                Map authType = DbFactory.Open(DbFactory.FORM).selectOne("authType.getAuthTypeByName",aythTypeName);

                Statement stat = DbFactory.Open(authType.get("auth_db").toString()).getConnection().createStatement();
                ResultSet set = stat.executeQuery(authType.get("auth_sql").toString());
                ResultSetMetaData rsmd = set.getMetaData();
                int cc = rsmd.getColumnCount();
                while (set.next()) {
                    Map<String, Object> retMap = new LinkedHashMap<String, Object>(cc);
                    list.add(retMap);
                    for (int i = 1; i <= cc; i++) {
                        String names=rsmd.getColumnLabel(i).toLowerCase();
                        if(names.equalsIgnoreCase("key")){
                            String  val =aythTypeName+"/"+ set.getObject(i);
                            retMap.put(rsmd.getColumnLabel(i).toLowerCase(),val);
                        }else {
                            retMap.put(rsmd.getColumnLabel(i).toLowerCase(), set.getObject(i));
                        }
                    }
                }
                m.put("children",list);
                listss.add(m);
            }
            return SuccessMsg("查询成功", listss);
        }catch(Exception ex){
            ex.printStackTrace();
            return ErrorMsg("3000", ex.getMessage());
        }
    }

    @RequestMapping(value="/getMenuList",produces = "text/plain;charset=UTF-8")
    public String getMenuList(@RequestBody String pJson) throws UnsupportedEncodingException, SAXException, DocumentException {
        JSONObject obj = JSONObject.parseObject(pJson);
        Map m=new HashMap<>();
        int userId=obj.getInteger("userId");
        List<Map<String, Object>> dataList=null;
        if(userId==1){
            m.put("pid", 0);
            dataList = DbFactory.Open(DbFactory.FORM).selectList("auth.getMenuAll", m);
            for (int i = 0; i < dataList.size(); i++) {
                m.put("pid", dataList.get(i).get("func_id"));
                List<Map<String, Object>> subList = DbFactory.Open(DbFactory.FORM).selectList("auth.getMenuAll", m);
                List<Map<String, Object>> subListtwo = new ArrayList<Map<String, Object>>();
                String func = dataList.get(i).get("func_id").toString();
                if (func.equals("1001")) {
                    subListtwo = this.getSubMenuAllList();
                }
                subList.addAll(subListtwo);
                dataList.get(i).put("children", subList);
            }
        }else {
            m.put("userId", userId);
            m.put("pid", 0);
            dataList = DbFactory.Open(DbFactory.FORM).selectList("auth.getMenuByUserId", m);
            for (int i = 0; i < dataList.size(); i++) {
                m.put("pid", dataList.get(i).get("func_id"));
                List<Map<String, Object>> subList = DbFactory.Open(DbFactory.FORM).selectList("auth.getMenuByUserId", m);
                List<Map<String, Object>> subListtwo = new ArrayList<Map<String, Object>>();
                String func = dataList.get(i).get("func_id").toString();
                if (func.equals("1001")) {
                    subListtwo = this.getSubMenuList(userId);
                }
                subList.addAll(subListtwo);
                dataList.get(i).put("children", subList);
            }
        }
        return JSON.toJSONString(dataList);
    }

    public List<Map<String,Object>> getSubMenuList(int userId) throws DocumentException, SAXException {
        List<Map<String,String>> fileList = queryService.getAuthTree(DbFactory.Open(DbFactory.FORM),userId);
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        for (int i = 0; i < fileList.size();i++) {
            String auth_type=fileList.get(i).get("auth_type");
            String func_id=fileList.get(i).get("func_id");
            //select 查询
            if(auth_type.equals("select")){
                    List<Map<String,Object>> childId=new ArrayList<Map<String,Object>>();
                    Map<String,Object> parMap = queryService.getAllQueryClassByClassId(Integer.parseInt(func_id));
                    if(null!=parMap){
                        for (int ii = 0; ii < fileList.size(); ii++){
                            String auth_typetwo=fileList.get(ii).get("auth_type").toString();
                            String qry_id=fileList.get(ii).get("func_id");
                            if(auth_typetwo.equals("select")) {
                                Map<String, Object> map = queryService.getQueryNameByClassIdQryId(Integer.parseInt(func_id), Integer.parseInt(qry_id));
                                if(null!=map) {
                                    childId.add(map);
                                }
                            }
                        }
                        parMap.put("children",childId);
                        list.add(parMap);
                    }
            }
            //function 函数
            if(auth_type.equals("function")){
                    Map<String,Object> parMap = queryService.getFunctionClassByClassId(Integer.parseInt(func_id));
                    List<Map<String,Object>> childId=new ArrayList<Map<String,Object>>();
                    if(null!=parMap){
                        for (int iii = 0; iii < fileList.size(); iii++) {
                            String auth_typeFunc=fileList.get(iii).get("auth_type").toString();
                            String function_id=fileList.get(iii).get("func_id");
                            if(auth_typeFunc.equals("function")) {
                                Map<String, Object> map = queryService.getFunctionNameByClassIdPId(Integer.parseInt(func_id), Integer.parseInt(function_id));
                                if(null!=map) {
                                    childId.add(map);
                                }
                            }
                        }
                        parMap.put("children",childId);
                        list.add(parMap);
                    }

            }
        }

        return list;
    }
    /**
     * 超级管理员全查询
     * select
     * function
     * */
    public List<Map<String,Object>> getSubMenuAllList(){
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        List<Map<String,String>> SelectList = queryService.getAllQueryClass(DbFactory.Open(DbFactory.FORM));
        for (int i = 0; i < SelectList.size(); i++) {
            JSONObject authNode = new JSONObject(true);
            String name = SelectList.get(i).get("class_name").toString();
            Integer key =  Integer.parseInt(String.valueOf(SelectList.get(i).get("class_id")));
            authNode.put("name", name);
            authNode.put("value", key);
            try {
                List<Map<String,Object>> chiledList = DbFactory.Open(DbFactory.FORM).
                        selectList("query.getQueryNameInfoByClassID",key);
                if(chiledList.size()>0) {
                    List<Map> childlist = new ArrayList<Map>();
                    for (int j = 0; j < chiledList.size(); j++) {
                        Map<String, Object> childl = chiledList.get(j);
                        Map<String, Object> childmap = new HashMap<String, Object>();
                        childmap.put("name", childl.get("qry_name").toString());
                        childmap.put("value", childl.get("qry_id").toString());
                        childlist.add(childmap);
                    }
                    authNode.put("children", childlist);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            list.add(authNode);
        }
        //function 函数
        List<Map<String,String>> FunctionList=functionService.getAllFunctionClass(DbFactory.Open(DbFactory.FORM));
        for (int i = 0; i < FunctionList.size(); i++) {
            JSONObject authNode = new JSONObject(true);
            String name = FunctionList.get(i).get("class_name").toString();
            Integer key =  Integer.parseInt(String.valueOf(FunctionList.get(i).get("class_id")));
            authNode.put("name", name);
            authNode.put("value", key);
            try {
                List<Map<String,Object>> chiledList = DbFactory.Open(DbFactory.FORM).
                        selectList("function.getFuncNameInfoByClassID",key);
                if(chiledList.size()>0) {
                    List<Map> childlist = new ArrayList<Map>();
                    for (int j = 0; j < chiledList.size(); j++) {
                        Map<String, Object> childl = chiledList.get(j);
                        Map<String, Object> childmap = new HashMap<String, Object>();
                        childmap.put("name", childl.get("func_name").toString());
                        childmap.put("value", childl.get("func_id").toString());
                        childlist.add(childmap);
                    }
                    authNode.put("children", childlist);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            list.add(authNode);
        }

        return list;
    }

     //获取菜单 最新版
    @RequestMapping(value="/getMenuListNew",produces = "text/plain;charset=UTF-8")
    public String getMenuListNew(@RequestBody String pJson) throws UnsupportedEncodingException, SAXException, DocumentException {
        JSONObject obj = JSONObject.parseObject(pJson);
        Map m=new HashMap<>();
        int userId=obj.getInteger("userId");
        List<Map<String, Object>> dataList=null;
        if(userId==1){
            m.put("pid", 0);
            dataList = DbFactory.Open(DbFactory.FORM).selectList("auth.getMenuAll", m);
            for (int i = 0; i < dataList.size(); i++) {
                m.put("pid", dataList.get(i).get("func_id"));
                List<Map<String, Object>> subList = DbFactory.Open(DbFactory.FORM).selectList("auth.getMenuAll", m);
                dataList.get(i).put("children", subList);
            }
        }else {
            m.put("userId", userId);
            m.put("pid", 0);
            dataList = DbFactory.Open(DbFactory.FORM).selectList("auth.getMenuByUserId", m);
            for (int i = 0; i < dataList.size(); i++) {
                m.put("pid", dataList.get(i).get("func_id"));
                List<Map<String, Object>> subList = DbFactory.Open(DbFactory.FORM).selectList("auth.getMenuByUserId", m);
                dataList.get(i).put("children", subList);
            }
        }
        return SuccessMsg("",dataList);
    }

    //获取菜单 最新版
    @RequestMapping(value="/getMenuLisToAntdPro",produces = "text/plain;charset=UTF-8")
    public String getMenuLisToAntdPro(@RequestBody String pJson) throws UnsupportedEncodingException, SAXException, DocumentException {
        JSONObject obj = JSONObject.parseObject(pJson);
        Map m=new HashMap<>();
        int userId=obj.getInteger("userId");
        List<Map<String, Object>> dataList=null;
        if(userId==1){
            m.put("pid", 0);
            dataList = DbFactory.Open(DbFactory.FORM).selectList("auth.getMenuAll", m);
            for (int i = 0; i < dataList.size(); i++) {
                m.put("pid", dataList.get(i).get("func_id"));
                List<Map<String, Object>> subList = DbFactory.Open(DbFactory.FORM).selectList("auth.getMenuAll", m);
                dataList.get(i).put("children", subList);
            }

        }else {
            m.put("userId", userId);
            m.put("pid", 0);
            dataList = DbFactory.Open(DbFactory.FORM).selectList("auth.getMenuByUserId", m);
            for (int i = 0; i < dataList.size(); i++) {
                m.put("pid", dataList.get(i).get("func_id"));
                List<Map<String, Object>> subList = DbFactory.Open(DbFactory.FORM).selectList("auth.getMenuByUserId", m);
                dataList.get(i).put("children", subList);
            }
        }
        return SuccessMsg("",dataList);
    }


    //根据数据查询获取数据classId
    @RequestMapping(value="/getClassId",produces = "text/plain;charset=UTF-8")
    public String getClassId(@RequestBody String pJson)  {
        JSONObject obj = JSONObject.parseObject(pJson);
        Map param=new HashMap<>();
        param.put("user_id",obj.getInteger("userId"));
        List<Map<String, Object>> listQueryName = DbFactory.Open(DbFactory.FORM).selectList("query.getClassId",param);
        return SuccessMsg("",listQueryName);
    }
    //根据classId查询获取数据
    @RequestMapping(value="/getQryNameByClassId",produces = "text/plain;charset=UTF-8")
    public String getQryNameByClassId(@RequestBody String pJson)  {
        JSONObject obj = JSONObject.parseObject(pJson);
        List<Map<String,Object>> chiledList=null;
        if(obj.getString("auth_type").equals("select")){
            chiledList = DbFactory.Open(DbFactory.FORM).selectList("query.getQueryNameInfoByClassID",obj.getInteger("class_id"));
        }
        return SuccessMsg("",chiledList);
    }

    //查询所有的cube 记录
    @RequestMapping(value = "/getAllCube", produces = "text/plain;charset=UTF-8")
    public String getAllCube(@RequestBody String pJson) {
        try {
            List<Map> mapList = DbFactory.Open(DbFactory.FORM).selectList("cube.getAllCubeforAuth");
            return SuccessMsg("", mapList);
        }catch (Exception e){
            return ExceptionMsg(e.getMessage());
        }
    }
    //查询所有的cube 记录
    @RequestMapping(value = "/getAllDashBoard", produces = "text/plain;charset=UTF-8")
    public String getAllDashBoard(@RequestBody String pJson) {
        try {
            List<Map> mapList = DbFactory.Open(DbFactory.FORM).selectList("dashboard.getAllDashboardforAuth");
            return SuccessMsg("",mapList);
        }catch (Exception e){
            return ExceptionMsg(e.getMessage());
        }
    }
    //根据数据查询获取数据classId
    @RequestMapping(value="/getCubeListInAuth",produces = "text/plain;charset=UTF-8")
    public String getCubeListInAuth(@RequestBody String pJson)  {
        JSONObject obj = JSONObject.parseObject(pJson);
        Map param=new HashMap<>();
        param.put("user_id",obj.getInteger("userId"));
        List<Map<String, Object>> listQueryName = DbFactory.Open(DbFactory.FORM).selectList("auth.getCubeListInAuth",param);
        return SuccessMsg("",listQueryName);
    }
    //根据数据查询获取数据classId
    @RequestMapping(value="/getDashboardListInAuth",produces = "text/plain;charset=UTF-8")
    public String getDashboardListInAuth(@RequestBody String pJson)  {
        JSONObject obj = JSONObject.parseObject(pJson);
        Map param=new HashMap<>();
        param.put("user_id",obj.getInteger("userId"));
        List<Map<String, Object>> listQueryName = DbFactory.Open(DbFactory.FORM).selectList("auth.getDashboardListInAuth",param);
        return SuccessMsg("",listQueryName);
    }
}
