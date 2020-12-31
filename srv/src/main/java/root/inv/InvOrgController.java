package root.inv;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.avro.data.Json;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.DbSession;
import root.report.common.RO;
import root.report.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  仓库、门店
 */
@RestController
@RequestMapping(value = "/reportServer/invOrg")
public class InvOrgController extends RO {

//    address: "南昌"
//    contacts: "欧阳嗷嗷"
//    org_name: "省公司"
//    org_pid: 5
//    org_type: "2"


    /**
     * 添加信息
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/add", produces = "text/plain;charset=UTF-8")
    public String add(@RequestBody JSONObject pJson){

        String orgPid = pJson.getString("org_pid");
        boolean isRoot = "0".equals(orgPid); //表示根节点

       String path;
       int level = 1;
        if(isRoot){
            DbSession.insert("inv_org.add", pJson);
            path = "-" + pJson.getIntValue("org_id") + "-";
        }else{
            Map<String,Object> parentCategory =  getOrgById(orgPid);
            if(parentCategory == null || parentCategory.isEmpty()){
                return ErrorMsg("2000","保存失败，上级不存在！");
            }
            DbSession.insert("inv_org.add", pJson);
            level = Integer.parseInt(String.valueOf(parentCategory.get("level"))) +1;
            path = String.valueOf(parentCategory.get("path")) + pJson.getIntValue("org_id") + "-";
        }

        //新增后维护path字段
        pJson.put("path", path);
        pJson.put("level",level);
        DbSession.update("inv_org.updateById",pJson);
        return SuccessMsg("新增成功", pJson);
    }


    /**
     * 删除信息
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/deleteByIds", produces = "text/plain;charset=UTF-8")
    public String deleteByIds(@RequestBody JSONObject pJson){
        String ids = pJson.getString("ids");
        if(ids == null || ids.isEmpty()){
            return ErrorMsg("2000","请选择删除项");
        }
        String[] idArr = ids.split(",");

        for(int i = 0 ; i< idArr.length; i++){
            Map<String, Object> currentCategory =  getOrgById(idArr[i]);
            if(currentCategory != null && !currentCategory.isEmpty()){
                DbSession.delete("inv_org.deleteByIds", currentCategory);
            }
        }

        return SuccessMsg("删除成功", "");
    }

    /**
     * 更新信息
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/updateById", produces = "text/plain;charset=UTF-8")
    public String updateById(@RequestBody JSONObject pJson){

        String orgId = pJson.getString("org_id");
        String orgPid = pJson.getString("org_pid");

        if(StringUtil.isBlank(orgId)){
            return ErrorMsg("2000","保存失败,该记录不存在");
        }

        if(orgId.equals(orgPid)){
            return ErrorMsg("2000","保存失败，不能以自己作为上级节点");
        }

        Map<String,Object> org =  getOrgById(orgId);
        if(org == null || org.isEmpty()){
            return ErrorMsg("2000","保存失败，该记录不存在！");
        }


        String oldPath = String.valueOf(org.get("path"));
        String newPath;
        int level = 1;

        //表示根节点
        if("0".equals(orgPid)){
            newPath = "-" + pJson.getIntValue("org_id") + "-";
        }else{
            Map<String,Object> parentOrg =  getOrgById(orgPid);
            if(parentOrg == null || parentOrg.isEmpty()){
                return ErrorMsg("2000","保存失败，上级不存在！");
            }
            level = Integer.parseInt(String.valueOf(parentOrg.get("level"))) + 1;
            newPath = String.valueOf(parentOrg.get("path")) + pJson.getIntValue("org_id") + "-";
        }

        pJson.put("path",newPath);
        pJson.put("level",level);

        DbSession.update("inv_org.updateById", pJson);

        //更新节点路径
        Map<String,Object> updatePathMap = new HashMap<>();
        updatePathMap.put("old_path",oldPath);
        updatePathMap.put("new_path",newPath);
        DbSession.update("inv_org.updatePath",updatePathMap);

        return SuccessMsg("编辑成功", pJson);
    }

    /**
     * 获取信息
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getById", produces = "text/plain;charset=UTF-8")
    public String getById(@RequestBody JSONObject pJson){
        Map<String, Object> Category = getOrgById(pJson);
        return SuccessMsg("", Category);
    }

    /**
     * 获取信息
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getByPid", produces = "text/plain;charset=UTF-8")
    public String getByPid(@RequestBody JSONObject pJson){
        List<Map<String, Object>> CategoryList = DbSession.selectList("inv_org.getByPid", pJson);
        return SuccessMsg("", CategoryList);
    }

    /**
     * 获取信息 通过关键字
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getByKeyword", produces = "text/plain;charset=UTF-8")
    public String getByKeyword(@RequestBody JSONObject pJson){
        List<Map<String, Object>> CategoryList = DbSession.selectList("inv_org.getByKeyword", pJson);
        return SuccessMsg("", CategoryList);

    }


    /**
     * 获取所有的子节点 递归所有 以列表形式返回
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getAllChildrenListById", produces = "text/plain;charset=UTF-8")
    public String getAllChildrenListById(@RequestBody JSONObject pJson)  {
        List<Map<String,Object>> dataList = new ArrayList<>();
        Map<String, Object> currentCategory =  getOrgById(pJson);
        if(currentCategory!=null &&  0 < currentCategory.size()){
            dataList.add(currentCategory);
            List<Map<String, Object>> CategoryList = DbSession.selectList("inv_org.getByPath", currentCategory);
            if(CategoryList != null && 0 < CategoryList.size()){
                dataList.addAll(CategoryList);
            }
        }

//        List<Map<String,Object>> dataList = new ArrayList<>();
//        Map<String, Object> currentCategory =  getOrgById(pJson);
//        if(currentCategory!=null &&  0 < currentCategory.size()){
//            dataList.add(currentCategory);
//        }
//        recursionList(dataList,pJson);
        return SuccessMsg("", dataList);
    }

    /**
     * 获取下的所有子 递归所有 以递归层级形式返回
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getAllChildrenRecursionById", produces = "text/plain;charset=UTF-8")
    public String getAllChildrenRecursionById(@RequestBody JSONObject pJson)  {
        List<Map<String, Object>> CategoryList = getOrgByPid(pJson);
        if(CategoryList!=null && 0 <CategoryList.size()){
            for(Map<String,Object> child : CategoryList){
                recursion(child);
            }
        }
        return SuccessMsg("", CategoryList);
    }

    /**
     * 检查code是否存在
     * @param pJson
     * @return
     */
    private boolean hasCode(JSONObject pJson){
        Map<String, Object> map = getOrgById(pJson);
        return map!=null && !map.isEmpty();
    }

    /**
     * 检查是否有子元素
     * @param pJson
     * @return
     */
    private boolean hasChildren(JSONObject pJson){
        List<Map<String, Object>> CategoryList =  getOrgByPid(pJson);
        return CategoryList!=null && 0 < CategoryList.size();
    }

    /**
     * 获取信息
     * @param orgId
     * @return
     */
    private Map<String, Object>  getOrgById(String orgId){
        Map<String,Object> map = new HashMap<>();
        map.put("org_id",orgId);
        return DbSession.selectOne("inv_org.getById", map);
    }

    /**
     * 获取信息
     * @param pJson
     * @return
     */
    private Map<String, Object>  getOrgById(Map<String,Object> pJson){
        return DbSession.selectOne("inv_org.getById", pJson);
    }

    /**
     * 获取信息
     * @param pJson
     * @return
     */
    public List<Map<String, Object>> getOrgByPid(Map<String,Object> pJson){
        return DbSession.selectList("inv_org.getByPid", pJson);
    }

    /**
     * 递归 - 层级形式
     * @param parentMap
     */
    private void recursion(Map<String,Object> parentMap){
        HashMap<String,Object> tempMap = new HashMap<>();
        tempMap.put("org_pid",parentMap.get("org_id"));
        List<Map<String,Object>>  CategoryList =  getOrgByPid(tempMap);
        if(CategoryList != null && 0 < CategoryList.size()){
            parentMap.put("children",CategoryList);
            for(Map<String,Object> child : CategoryList){
                recursion(child);
            }
        }
    }

    /**
     * 递归 - 列表形式
     * @param dataList
     * @param parentMap
     */
    private void recursionList(List<Map<String,Object>> dataList,Map<String,Object> parentMap){
        HashMap<String,Object> tempMap = new HashMap<>();
        tempMap.put("org_pid",parentMap.get("org_id"));
        List<Map<String,Object>>  CategoryList =  getOrgByPid(tempMap);
        if(CategoryList != null && 0 < CategoryList.size()){
            dataList.addAll(CategoryList);
            for(Map<String,Object> child : CategoryList){
                recursionList(dataList,child);
            }
        }
    }

}
