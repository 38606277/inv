package root.report.control.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.service.FunctionService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import root.report.util.SQLFormatUtil;
import root.report.util.SQLUtils;

@RestController
@RequestMapping("/reportServer/utils")
public class SQLTools extends RO {

    private static Logger log = Logger.getLogger(SQLUtils.class);

    @RequestMapping(value = "/formatSQL", produces = "text/plain;charset=UTF-8")
    public String formatSQL(@RequestBody String pJson) {


        try{
            JSONObject jsonObject=JSONObject.parseObject(pJson);

            // String aformatSQl=SQLUtils.formatMySql(jsonObject.getString("sql"));
            // System.out.println(aformatSQl);
            //  新的format方法
            String aformatSQl = new SQLFormatUtil().format(jsonObject.getString("sql"));
            // System.out.println(aformatSQl);
            return  SuccessMsg("",aformatSQl);
        }catch (Exception ex){
            return  ExceptionMsg(ex.getMessage());

        }

    }




}
