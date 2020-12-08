/*
package root.report.leedemo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.DbSession;
import root.report.common.RO;
import root.report.util.Node;
import root.report.util.TreeBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/reportServer/org")
public class OrgController extends RO {

	*/
/**
	 * 通过org_pid获取组织信息
	 * @param pJson
	 * @return
	 *//*

	@RequestMapping(value = "/listByOrgPid", produces = "text/plain;charset=UTF-8")
	public String listByOrgPid(@RequestBody JSONObject pJson)  {
		List<Map<String, Object>> gatewayList = DbSession.selectList("fndOrg.listByOrgPid", pJson);
		return SuccessMsg("查询成功",gatewayList);
	}


	*/
/**
	 * 通过org_pid获取树结构数据
	 * @param pJson
	 * @return
	 *//*

	@RequestMapping(value = "/listOrgTreeByOrgPid", produces = "text/plain;charset=UTF-8")
	public String listOrgTreeByOrgPid(@RequestBody JSONObject pJson)  {
		List<Map<String, Object>> gatewayList = DbSession.selectList("fndOrg.listOrgTreeByOrgPid", pJson);
		return SuccessMsg("查询成功",gatewayList);
	}


	*/
/**
	 * 添加组织信息
	 * @param pJson
	 * @return
	 *//*

	@RequestMapping(value = "/addOrg", produces = "text/plain;charset=UTF-8")
	public String addOrg(@RequestBody JSONObject pJson)  {
		DbSession.insert("fndOrg.addOrg", pJson);
		return SuccessMsg("保存成功","");
	}

	*/
/**
	 * 添加组织信息
	 * @param pJson
	 * @return
	 *//*

	@RequestMapping(value = "/createOrg", produces = "text/plain;charset=UTF-8")
	public String createOrg(@RequestBody JSONObject pJson)  {
		//pJson中放pid,org_name:"未命名"
		DbSession.insert("fndOrg.createOrg", pJson);
		String org_id=pJson.getString("org_id");
		return SuccessMsg("保存成功",org_id);
	}


	*/
/**
	 * 通过org_id更新组织信息
	 * @param pJson
	 * @return
	 *//*

	@RequestMapping(value = "/updateOrgByOrgId", produces = "text/plain;charset=UTF-8")
	public String updateOrgByOrgId(@RequestBody JSONObject pJson)  {
		DbSession.update("fndOrg.updateOrgByOrgId", pJson);
		return SuccessMsg("保存成功","");
	}


	*/
/**
	 * 通过org_id删除组织信息
	 * @param pJson
	 * @return
	 *//*

	@RequestMapping(value = "/deleteByOrgId", produces = "text/plain;charset=UTF-8")
	public String deleteByOrgId(@RequestBody JSONObject pJson)  {
		DbSession.delete("fndOrg.deleteByOrgId", pJson);
		return SuccessMsg("删除成功","");
	}

	*/
/**
	 * 通过org_id删除组织信息
	 * @param pJson
	 * @return
	 *//*

	@RequestMapping(value = "/deleteByOrgIds", produces = "text/plain;charset=UTF-8")
	public String deleteByOrgIds(@RequestBody JSONObject pJson)  {

		JSONArray jsonArray = pJson.getJSONArray("org_ids");
		if(jsonArray == null){
			return ErrorMsg("2000","请选择需要删除的组织");
		}

		for(int i = 0 ; i < jsonArray.size(); i++){
			DbSession.delete("fndOrg.deleteByOrgId", jsonArray.getJSONObject(i));
		}
		return SuccessMsg("删除成功","");
	}



	@RequestMapping(value = "/getOrgTree", produces = "text/plain;charset=UTF-8")
	public String getOrgTree(@RequestBody JSONObject pJson)  {
		JSONObject jsonTree=new JSONObject();
		List<Node> nodes= DbSession.selectList("fndOrg.getAll", pJson);
		// 拼装树形json字符串
		List<Node>  result= new TreeBuilder().buildTree(nodes);
		return SuccessMsg("",result);
	}

	@RequestMapping(value = "/getOrgByID", produces = "text/plain;charset=UTF-8")
	public String getOrgByID(@RequestBody JSONObject pJson)  {
		Map<String,Object> result= DbSession.selectOne("fndOrg.getByOrgID", pJson);
		return SuccessMsg("",result);
	}

}
*/
