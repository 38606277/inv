package root.report.budget;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.*;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.query.SelectControl;
import root.report.sys.SysContext;
import root.report.util.ExecuteSqlUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Auther: pccw
 * @Date: 2018/12/19 14:07
 * @Description:
 */
@RestController
@RequestMapping("/reportServer/deptBudget")
public class BudgetDeptController extends RO {

    // dept 版本的 查询部门明细   建议 ，namespace 为 : 报表展示  sqlid 为 ： 部门明细表_缓存    type为： query
    @RequestMapping(value = "/getlocalOrgList", produces = "text/plain; charset=utf-8")
    public String getOrgList( @RequestBody String pJson) {
        JSONObject result = new JSONObject();
        try{
            JSONObject obj=JSON.parseObject(pJson);
            List<Map> aResult = null;
            Long totalSize = 0L;
            Map<String,Object> param = new HashMap<String,Object>();
            try {
                RowBounds bounds = null;
                JSONObject page=obj.getJSONObject("page");
                if(page==null){
                    bounds = RowBounds.DEFAULT;
                }else{
                    int startIndex=page.getIntValue("currentPage");
                    int perPage=page.getIntValue("perPage");
                    if(startIndex==1 || startIndex==0){
                        startIndex=0;
                    }else{
                        startIndex=(startIndex-1)*perPage;
                    }
                    bounds = new PageRowBounds(startIndex, perPage);
                    param.put("startIndex",startIndex);
                    param.put("perPage",perPage);
                }
                //转换公司和部门Id   (解析前台传递的JSON数据串当中的内容)
                //预算年份
                param.put("budget_year", obj.getIntValue("budget_year"));
                //公司名称(ID)
                if(null!=obj.getString("companycodes") && obj.getString("companycodes").equals("0")){
                    param.put("company_code",null);
                }else{
                    param.put("company_code", obj.getString("companycodes"));
                }
                //部门名称
                param.put("department_name", obj.getString("departmentids"));
                //预算立项编号
                param.put("project_number", obj.getString("project_number"));
                //预算立项名称
                param.put("project_name", obj.getString("project_name"));
                //预算科目名称
                param.put("budget_account_name", obj.getString("budget_account_name"));
                //合同编号
                param.put("document_code", obj.getString("document_code"));
                //合同名称
                param.put("document_description", obj.getString("document_description"));
                //订单号
                param.put("order_number", obj.getString("order_number"));
                //报账单编号
                param.put("bz_document_number", obj.getString("bz_document_number"));
                aResult = DbFactory.Open(DbFactory.FORM).selectList("localBudgetDept.getAll", param,bounds);
                if(param!=null){
                    totalSize = ((PageRowBounds)bounds).getTotal();
                }else{
                    totalSize = Long.valueOf(aResult.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            result.put("list",aResult);
            result.put("totalSize",totalSize);
//            List<Map<String, String>> list  = dictService.getDictValueByID(dict_id);
            //return JSON.toJSONString(maps);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
        return result.toString();

        // 1.  绑定字段含义   namespace   -》 对应的sql的xml文件当中的  namespcae名
        //     sqlid   -> namespace 下的具体sql      type ： 需要执行的操作 如query

        // 2. 对传递的参数解析到 obj 对象上
//        JSONObject obj = new JSONObject();
//        obj.put("namespace", namespace);
//        obj.put("sqlid", sqlid);
//
//        //  3. 获取sql查询所需的数据库  (从 对应的文件位置当中的 comment 注释当中的节点获取到 数据库连接)
//        SelectControl selectControl = new SelectControl();
//        // 我们假设就在原先的位置处
//        JSONObject sqlObj = JSON.parseObject(selectControl.qrySelectSqlDetail(obj.toJSONString()));
//        JSONObject commentObj = sqlObj.getJSONObject("comment");
//        String db = commentObj.getString("db");
//
//
//        // 4. 构造查询参数
//        JSONArray queryparam = new JSONArray();
//        JSONObject paramObj = new JSONObject();
//        paramObj.put("db", db);
//
//        //转换公司和部门Id   (解析前台传递的JSON数据串当中的内容)
//        Map<String,Object> param = new HashMap<String,Object>();
//        //预算年份
//        param.put("budget_year", pJson.getIntValue("budget_year"));
//        //公司名称(ID)
//        prepareCompanyIds(param,pJson);     // ###  组装公司相关信息
//        //部门名称
//        param.put("department_id", pJson.getString("department_id"));
//        //预算立项编号
//        param.put("project_number", pJson.getString("project_number"));
//        //预算立项名称
//        param.put("project_name", pJson.getString("project_name"));
//        //预算科目名称
//        param.put("budget_account_name", pJson.getString("budget_account_name"));
//        //合同编号
//        param.put("document_code", pJson.getString("document_code"));
//        //合同名称
//        param.put("document_description", pJson.getString("document_description"));
//        //订单号
//        param.put("order_number", pJson.getString("order_number"));
//        //报账单编号
//        param.put("bz_document_number", pJson.getString("bz_document_number"));
//        param.put("isadmin", SysContext.getRequestUser().getIsAdmin());
//        param.put("user_permission_code", this.getUserPermissionCode());    // TODO :  权限控制在新SQL 当中无法复现，如何区分 permission_codes 是P C E???
//
//        param.put("user_name", SysContext.getRequestUser().getUserName().toLowerCase());
//
//        //arch_user用户名
//        Map<String,String> map = new HashMap<String,String>();
//        map.put("userName", SysContext.getRequestUser().getUserName().toLowerCase());
//        List<Map<String,String>> result = DbFactory.Open(DbFactory.BUDGET).selectList("budget.getArchUserName",map);
//        param.put("userName", result.size()!=0?result.get(0).get("USERNAME"):"");
//        //将Map参数转换为通用查询所需的参数形式
//        JSONArray paramArr = prepareQueryParam(param);
//        paramObj.put("in",paramArr);
//        queryparam.add(paramObj);
//        if(!type.equals("export")){
//            queryparam.add(pJson.getJSONObject("page"));
//        }
//        return JSONObject.parseObject(selectControl.execSelect(namespace,sqlid,queryparam.toJSONString())).getJSONObject("data").toJSONString();
    }

    // dept 版本的 查询部门明细   建议 ，namespace 为 : 报表展示  sqlid 为 ： 部门明细表_缓存    type为： query
    @RequestMapping(value = "/localExport", produces = "text/plain; charset=utf-8")
    public @ResponseBody
    String export( @RequestBody String pJson) {
        JSONObject result = new JSONObject();
        try {
            JSONObject obj = JSON.parseObject(pJson);

            List<Map> aResult = null;
            Long totalSize = 0L;
            Map<String, Object> param = new HashMap<String, Object>();
            try {
                //预算年份
                param.put("budget_year", obj.getIntValue("budget_year"));
                //公司名称(ID)
                if(null!=obj.getString("companycodes") && obj.getString("companycodes").equals("0")){
                    param.put("company_code",null);
                }else{
                    param.put("company_code", obj.getString("companycodes"));
                }

                //部门名称
                param.put("department_name", obj.getString("departmentids"));
                //预算立项编号
                param.put("project_number", obj.getString("project_number"));
                //预算立项名称
                param.put("project_name", obj.getString("project_name"));
                //预算科目名称
                param.put("budget_account_name", obj.getString("budget_account_name"));
                //合同编号
                param.put("document_code", obj.getString("document_code"));
                //合同名称
                param.put("document_description", obj.getString("document_description"));
                //订单号
                param.put("order_number", obj.getString("order_number"));
                //报账单编号
                param.put("bz_document_number", obj.getString("bz_document_number"));
                aResult = DbFactory.Open(DbFactory.FORM).selectList("localBudgetDept.getAll", param);

            } catch (Exception e) {
                e.printStackTrace();
            }
            result.put("list",aResult);
            result.put("totalSize", totalSize);
        } catch (Exception ex) {
            return ExceptionMsg(ex.getMessage());
        }
        return result.toString();
    }
    // 公司名称？
    private void prepareCompanyIds(Map<String,Object> param,JSONObject pJson)
    {
        StringBuilder sb = new StringBuilder();
        JSONArray companycodes = pJson.getJSONArray("companycodes");   // 得到 companycodes 代码
        if(companycodes!=null){
            if(companycodes.size()>0){
                for (int i=0;i<companycodes.size();i++)
                {
                    sb.append("'"+companycodes.getJSONObject(i).get("value")+"',");
                }
                param.put("companycodes", sb.substring(0, sb.length()-1));
            }else{
                param.put("companycodes", null);
            }
        }else{
            param.put("companycodes",null);
        }
        sb = new StringBuilder();
        JSONArray departmentids = pJson.getJSONArray("departmentids");
        if(departmentids!=null){
            if(departmentids.size()>0)
            {
                for (int i=0;i<departmentids.size();i++)
                {
                    sb.append(departmentids.getJSONObject(i).get("value")+",");
                }
                param.put("departmentids", sb.substring(0, sb.length()-1));
            }else{
                param.put("departmentids", null);
            }
        }else{
            param.put("departmentids",null);
        }
    }

    //  权限控制：  用户能查询出来的数据    --》 权限控制在大SQL 当中会做出筛选。  那么切割到我们这个本地表上怎么做权限控制？
    private String getUserPermissionCode(){
        String userName = SysContext.getRequestUser().getUserName();//当前用户名
        String permissionCode = "E";
        Map<String,Object> paramMap = new HashMap<String,Object>();
        paramMap.put("userName", userName);
        String userPermission = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getUserPermission", paramMap);
        Map<String,String> kindItem = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getKindItem", paramMap);
        if(kindItem != null){
            String detail = (String)kindItem.get("DETAIL");
            if("ALL".equals(detail)){
                permissionCode = "P";
            }else{
                permissionCode = "C";
            }
        }else{
            if(userPermission!=null && !userPermission.isEmpty()){
                permissionCode =  userPermission;
            }
        }
        return permissionCode;
    }

    private JSONArray prepareQueryParam(Map<String,Object> param)
    {
        JSONArray paramArr = new JSONArray();
        Set<String> keys = param.keySet();
        JSONObject obj = null;
        for (String key:keys) {
            obj = new JSONObject();
            obj.put("id", key);
            obj.put("value", param.get(key));
            paramArr.add(obj);
        }
        return paramArr;
    }
}
