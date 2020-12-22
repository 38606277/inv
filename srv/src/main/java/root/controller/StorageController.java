package root.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.DbSession;
import root.report.common.RO;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/reportServer/storage")
public class StorageController extends RO {

    /**
     * 添加仓库
     *
     * @return
     */
    @RequestMapping(value = "/addStorage", produces = "text/plain;charset=UTF-8")
    public String addStorage(@RequestBody JSONObject pJson) throws UnsupportedEncodingException {
        try{
            DbSession.insert("storage.addStorage", pJson);
            return SuccessMsg("保存成功", pJson.get("id").toString());
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/deleteStorage", produces = "text/plain;charset=UTF-8")
    public String deleteStorage(@RequestBody JSONObject pJson) throws UnsupportedEncodingException {
        try{
            DbSession.delete("storage.deleteStorage", pJson);
            return SuccessMsg("删除成功", "");

        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/updateStorage", produces = "text/plain;charset=UTF-8")
    public String updateStorage(@RequestBody JSONObject pJson) throws UnsupportedEncodingException {
        DbSession.update("storage.updateStorage", pJson);
        return SuccessMsg("编辑成功", "");
    }


    /**
     * 仓库列表
     *
     * @return
     */
    @RequestMapping(value = "/listStorageByPage", produces = "text/plain;charset=UTF-8")
    public String listStorageByPage(@RequestBody JSONObject pJson) throws UnsupportedEncodingException {
        int currentPage = Integer.valueOf(pJson.getString("pageNum"));
        int perPage = Integer.valueOf(pJson.getString("perPage"));

        if (1 == currentPage || 0 == currentPage) {
            currentPage = 0;
        } else {
            currentPage = (currentPage - 1) * perPage;
        }
        pJson.put("startIndex", currentPage);
        pJson.put("perPage", perPage);
        List<Map<String, Object>> gatewayList = DbSession.selectList("storage.listStorageByPage", pJson);
        int total = DbSession.selectOne("storage.countStorageByPage", pJson);
        Map<String, Object> map3 = new HashMap<String, Object>();
        map3.put("list", gatewayList);
        map3.put("total", total);
        return SuccessMsg("查询成功",map3);
    }

}
