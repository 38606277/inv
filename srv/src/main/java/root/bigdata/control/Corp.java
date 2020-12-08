package root.bigdata.control;


import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;
import root.report.db.DbFactory;

import java.util.List;
import java.util.Map;

@RestController
public class Corp extends RO {

    @RequestMapping(value = "/reportServer/corp/getAllOrg", produces = "text/plain;charset=UTF-8")
    public String getAllOrg(@RequestBody JSONObject pJson)  {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("corp.getAllOrg",pJson);
        return SuccessMsg("",list);

    }


}
