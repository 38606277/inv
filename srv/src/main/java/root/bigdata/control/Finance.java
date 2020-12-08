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
public class Finance extends RO {

    @RequestMapping(value = "/reportServer/finance/getDataList", produces = "text/plain;charset=UTF-8")
    public String getDataTable(@RequestBody JSONObject pJson)  {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("dataView.getDataView",pJson);
        return SuccessMsg("",list);

    }

    @RequestMapping(value = "/reportServer/finance/getAllCorp", produces = "text/plain;charset=UTF-8")
    public String getAllCorp(@RequestBody JSONObject pJson)  {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("finance.getAllCorp",pJson);
        return SuccessMsg("",list);

    }

    @RequestMapping(value = "/reportServer/finance/getFinReport", produces = "text/plain;charset=UTF-8")
    public String getFinReport(@RequestBody JSONObject pJson)  {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("finance.getFinReport",pJson);
        return SuccessMsg("",list);

    }

    @RequestMapping(value = "/reportServer/finance/getStudents", produces = "text/plain;charset=UTF-8")
    public String getStudents(@RequestBody JSONObject pJson)  {
        List<Map> list = DbFactory.Open("hive").selectList("finance.getStudents",pJson);
        return SuccessMsg("",list);

    }



    @RequestMapping(value = "/reportServer/finance/gethbase", produces = "text/plain;charset=UTF-8")
    public String gethbase(@RequestBody JSONObject pJson)  {
        List<Map> list = DbFactory.Open("hbase").selectList("finance.getHbase",pJson);
        return SuccessMsg("",list);

    }

    @RequestMapping(value = "/reportServer/finance/getRevenueTop10", produces = "text/plain;charset=UTF-8")
    public String getRevenueTop10(@RequestBody JSONObject pJson)  {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("finance.getRevenueTop10",pJson);
        return SuccessMsg("",list);

    }
    @RequestMapping(value = "/reportServer/finance/getNetProfitTop10", produces = "text/plain;charset=UTF-8")
    public String getNetProfitTop10(@RequestBody JSONObject pJson)  {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("finance.getNetProfitTop10",pJson);
        return SuccessMsg("",list);

    }

    @RequestMapping(value = "/reportServer/finance/getTotalProfitTop10", produces = "text/plain;charset=UTF-8")
    public String getTotalProfitTop10(@RequestBody JSONObject pJson)  {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("finance.getTotalProfitTop10",pJson);
        return SuccessMsg("",list);

    }
}
