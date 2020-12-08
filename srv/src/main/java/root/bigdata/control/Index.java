package root.bigdata.control;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;
import root.report.db.DbFactory;

import java.util.List;
import java.util.Map;

@RestController
public class Index extends RO {

    //得到所有指标目录
    @RequestMapping(value = "/reportServer/index/getIndexCatalog", produces = "text/plain;charset=UTF-8")
    public String getIndexCatalog(@RequestBody JSONObject pJson)  {
        JSONObject aJson= JSON.parseObject("{FLEX_VALUE_SET_ID:6}");
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("flexValue.getFlexValuesTree",aJson);
        return SuccessMsg("",list);

    }

    //得到所指标值列表
    @RequestMapping(value = "/reportServer/index/getIndexValue", produces = "text/plain;charset=UTF-8")
    public String getIndexValue(@RequestBody JSONObject pJson)  {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("index.getIndexValue",pJson);

        return SuccessMsg("",list);

    }
    //得到所指标值列表
    @RequestMapping(value = "/reportServer/index/getIndexValueWithColumn", produces = "text/plain;charset=UTF-8")
    public String getIndexValueWithColumn(@RequestBody JSONObject pJson)  {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("index.getIndexValueWithColumn",pJson);

        return SuccessMsg("",list);

    }


}
