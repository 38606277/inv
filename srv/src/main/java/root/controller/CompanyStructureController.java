package root.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.DbSession;
import root.report.common.RO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组织架构
 */
@RestController
@RequestMapping(value = "/reportServer/companyStructure")
public class CompanyStructureController extends RO {


    /**
     * 获取组织架构下 递归所有 以递归层级形式返回
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getAllChildrenRecursionByCode", produces = "text/plain;charset=UTF-8")
    public String getAllChildrenRecursionByCode(@RequestBody JSONObject pJson)  {
        List<Map<String, Object>> list = getByParentCode(pJson);
        if(list!=null && 0 <list.size()){
            for(Map<String,Object> child : list){
                recursion(child);
            }
        }
        return SuccessMsg("", list);
    }

    /**
     * 递归 - 层级形式
     * @param parentMap
     */
    private void recursion(Map<String,Object> parentMap){
        HashMap<String,Object> tempMap = new HashMap<>();
        tempMap.put("parent_code",parentMap.get("code"));
        List<Map<String,Object>>  list =  getByParentCode(tempMap);
        if(list != null && 0 < list.size()){
            parentMap.put("children",list);
            for(Map<String,Object> child : list){
                recursion(child);
            }
        }
    }

    /**
     * 通过 parentCode 获取
     * @param pJson
     * @return
     */
    public List<Map<String, Object>> getByParentCode(Map<String,Object> pJson){
        return DbSession.selectList("company_structure.getByParentCode", pJson);
    }


}
