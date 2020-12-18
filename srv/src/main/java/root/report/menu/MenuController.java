package root.report.menu;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.DbSession;
import root.report.common.RO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/reportServer/menu")
public class MenuController extends RO {


	/**
	 * 获取树形结构的菜单数据
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/getMenuList", produces = "text/plain;charset=UTF-8")
	public String getMenuList(@RequestBody JSONObject pJson)  {
		Map<String,Object> map = new HashMap<>();
		map.put("func_type", pJson.getString("func_type"));
		//默认查询pid为0的数据
		map.put("func_pid", "0");
		List<Map<String,Object>> nodeList = new ArrayList<>();
		List<Map<String,Object>> menuList = DbSession.selectList("fnd_menu.listMenu",map);
		for(Map<String,Object> menu:menuList){
			nodeList.add(menu);
			map.put("func_pid",menu.get("func_id"));
			List<Map<String,Object>> childList = DbSession.selectList("fnd_menu.listMenu",map);
			nodeList.addAll(childList);
		}
		return SuccessMsg("查询成功",nodeList);
	}




	/**
	 * 获取树形结构的菜单数据
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/getMenuTreeList", produces = "text/plain;charset=UTF-8")
	public String getMenuTreeList(@RequestBody JSONObject pJson)  {
		Map<String,String> map = new HashMap<String,String>();
		map.put("func_type", pJson.getString("func_type"));
		//默认查询pid为0的数据
		map.put("func_pid", "0");
		List<Map<String,Object>> rootList = DbSession.selectList("fnd_menu.listMenu",map);
		showExcelRuleTreeNodeReact(map,rootList);
		return SuccessMsg("查询成功",rootList);
	}

	public void showExcelRuleTreeNodeReact(Map<String,String> map, List<Map<String,Object>> nodeList) {
		for (Map<String,Object> auth : nodeList) {
			map.put("func_pid", auth.get("func_id").toString());
			List<Map<String,Object>> childList = DbSession.selectList("fnd_menu.listMenu",map);
			if(childList.size()>0){
				auth.put("children", childList);
				showExcelRuleTreeNodeReact(map,childList);
			}else{
				auth.put("children", new ArrayList<>());
			}
		}

	}


	/**
	 * 更新数据排序
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/updateMenuTreeListOrder", produces = "text/plain;charset=UTF-8")
	public String updateMenuTreeList(@RequestBody JSONObject pJson)  {

		recursiveUpdateMenuTreeListOrder(pJson.getJSONArray("menuTreeList"));
		return SuccessMsg("保存成功","");
	}

	/**
	 * 遍历列表并更新
	 */
	private void recursiveUpdateMenuTreeListOrder(JSONArray jsonArray){
		if(jsonArray == null || jsonArray.size() == 0){
			return ;
		}
		for(int i = 0 ; i < jsonArray.size() ; i++ ){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			jsonObject.put("order",i+1);
			DbSession.update("fnd_menu.updateMenuOrder",jsonObject);
			recursiveUpdateMenuTreeListOrder(jsonObject.getJSONArray("children"));
		}
	}

	/**
	 * 获取一级菜单列表
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/getRootMenuList", produces = "text/plain;charset=UTF-8")
	public String getRootMenuList(@RequestBody JSONObject pJson)  {
		List<Map<String,Object>> data = DbSession.selectList("fnd_menu.getRootMenuList",pJson);
		return SuccessMsg("获取成功",data);
	}


	/**
	 * 获取菜单信息
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/getMenuInfo", produces = "text/plain;charset=UTF-8")
	public String getMenuInfo(@RequestBody JSONObject pJson)  {
		Map<String,Object> data = DbSession.selectOne("fnd_menu.getMenuInfo",pJson);
		return SuccessMsg("获取成功",data);
	}


	/**
	 * 更新菜单信息
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/updateMenu", produces = "text/plain;charset=UTF-8")
	public String updateMenu(@RequestBody JSONObject pJson)  {
		int id = DbSession.update("fnd_menu.updateMenu",pJson);
		return SuccessMsg("更新成功",id);
	}


	/**
	 * 添加菜单
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/createMenu", produces = "text/plain;charset=UTF-8")
	public String createMenu(@RequestBody JSONObject pJson)  {
		Map<String,Integer> data = DbSession.selectOne("fnd_menu.getMenuLastOrder",pJson);
		pJson.put("order",data == null? 1 : data.get("order") + 1);
		int id = DbSession.insert("fnd_menu.addMenu",pJson);
		return SuccessMsg("添加成功",id);
	}


	/**
	 * 删除菜单
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/deleteMenu", produces = "text/plain;charset=UTF-8")
	public String deleteMenu(@RequestBody JSONObject pJson)  {
		int order = DbSession.delete("fnd_menu.deleteMenu",pJson);
		return SuccessMsg("删除成功",order);
	}



	/**
	 * 删除菜单
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/queryMenu", produces = "text/plain;charset=UTF-8")
	public String queryMenu(@RequestBody JSONObject pJson)  {
		Map<String,Object> menu1 = getMenu("/welcome","welcome","smile");
		Map<String,Object> menu2 = getMenu("/admin","admin","crown");
		Map<String,Object> menu3 = getMenu("/admin/sub-page","sub-page","smile");
		ArrayList<Map<String,Object>> arrayList = new ArrayList<>();
		arrayList.add(menu3);
		menu2.put("children",arrayList);
		Map<String,Object> menu4 = getMenu("/list","list.table-list","table");

		Map<String,Object> menu5 = getMenu("/user/userList","user.user-list","table");
		ArrayList<Map<String,Object>> menuList = new ArrayList<>();
		menuList.add(menu1);
		menuList.add(menu2);
		menuList.add(menu4);
		menuList.add(menu5);
		return SuccessMsg("菜单获取成功",menuList);
	}

	private Map<String,Object> getMenu(String path,String name,String icon){
		Map<String,Object> map  = new HashMap<>();
		map.put("path",path);
		map.put("name",name);
		map.put("icon",icon);
		return map;
	}





}
