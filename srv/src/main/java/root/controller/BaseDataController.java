package root.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.DbSession;
import root.report.common.RO;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * 基础数据
 */
@RestController
@RequestMapping(value = "/reportServer/baseData")
public class BaseDataController extends RO {

    /**
     * 仓库列表
     * @return
     */
    @RequestMapping(value = "/listBaseDataByType", produces = "text/plain;charset=UTF-8")
    public String listStorageByPage(@RequestBody JSONObject pJson) throws UnsupportedEncodingException {
        if(!pJson.containsKey("type")){
            return ErrorMsg("2000","类型不能为空");
        }
        List<Map<String, Object>> gatewayList = DbSession.selectList("base_data.listBaseDataByType", pJson);
        return SuccessMsg("查询成功",gatewayList);
    }


}
