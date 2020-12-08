package root.report.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: pccw
 * @Date: 2018/11/23 14:06
 * @Description:
 */
@Service
public class DashboardService {

    public String createDashBoard(SqlSession sqlSession,JSONObject jsonObject){
        // 1. 对json进行解析
        Map<String,Object> map = new HashMap<>();
        // map.put("dashboard_id",jsonObject.getIntValue("dashboard_id")); dashboard_id 自增长
        map.put("class_id",jsonObject.getString("class_id"));
        map.put("dashboard_name",jsonObject.getString("dashboard_name"));
        map.put("tempate",jsonObject.getIntValue("tempate"));
        map.put("dashboard_desc",jsonObject.getString("dashboard_desc"));
        map.put("param",jsonObject.getString("param"));
        map.put("icon",jsonObject.getString("icon"));

        // 2. 插入到数据库
        sqlSession.insert("dashboard.createDashBoard",map);

        // 43. 返回自增长主键
        return map.get("id").toString();
    }

    public void updateDashBoard(SqlSession sqlSession,JSONObject jsonObject){
        // 1. 对json进行解析
        Map<String,Object> map = new HashMap<>();
        map.put("dashboard_id",jsonObject.getIntValue("dashboard_id"));  // dashboard_id 自增长
        map.put("class_id",jsonObject.getString("class_id"));
        map.put("dashboard_name",jsonObject.getString("dashboard_name"));
        map.put("tempate",jsonObject.getIntValue("tempate"));
        map.put("dashboard_desc",jsonObject.getString("dashboard_desc"));
        map.put("param",jsonObject.getString("param"));
        map.put("icon",jsonObject.getString("icon"));

        // 2. 插入到数据库
        sqlSession.update("dashboard.updateDashBoard",map);
    }
}
