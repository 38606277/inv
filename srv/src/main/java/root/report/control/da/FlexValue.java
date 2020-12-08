package root.report.control.da;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import root.report.common.RO;
import root.report.db.DbFactory;

import java.util.Map;

public class FlexValue  extends RO {

    //根据字典ID返回字典中所有的值
    @RequestMapping(value = "/flexValue/getValueByID", produces = "text/plain;charset=UTF-8")
    public String getAllValueByID(@RequestBody JSONObject pJson){
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try {
            Map map = sqlSession
                    .selectOne("flexValue.getAllValueByID",pJson.getString("flex_value_set_id"));
            return SuccessMsg("",map);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }



}

