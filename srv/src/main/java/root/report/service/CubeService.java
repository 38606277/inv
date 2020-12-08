package root.report.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther:
 * @Date: 2018/11/23 10:02
 * @Description:
 */
@Service
public class CubeService {

    private static Logger log = Logger.getLogger(CubeService.class);

    /**
     *
     * 功能描述:
     *      往 da_cube 表当中插入记录
     * @param:
     * @return:
     * @auther: pccw
     * @date: 2018/11/23 10:05
     */
    public String createCuBe(SqlSession sqlSession, JSONObject jsonObject){
        // 1. 对json进行解析
        Map<String,Object> map = new HashMap<>();
        // map.put("cube_id",jsonObject.getIntValue("cube_id")); cube_id 自增长
        map.put("cube_name",jsonObject.getString("cube_name"));
        map.put("cube_desc",jsonObject.getString("cube_desc"));
        map.put("qry_id",jsonObject.getIntValue("qry_id"));
        map.put("icon",jsonObject.getString("icon"));
        map.put("class_name",jsonObject.getString("class_name"));

        // 2. 对 sql 进行编译
        String sql = jsonObject.getString("cube_sql");
        if(null!=sql && !"".equals(sql)){
            sql = sql.replace("'","\\'").replace("{","\\{").replace("}","\\}");
        }
        map.put("cube_sql",sql);

        // 3. 插入到数据库
        sqlSession.insert("cube.createCube",map);

        // 4. 返回自增长主键
        return map.get("id").toString();
    }

    /**
     *
     * 功能描述:
     *      往 da_cube 表当中修改记录
     * @param:
     * @return:
     * @auther: pccw
     * @date: 2018/11/23 10:05
     */
    public void updateCuBe(SqlSession sqlSession, JSONObject jsonObject){
        // 1. 对json进行解析
        Map<String,Object> map = new HashMap<>();
        map.put("cube_id",jsonObject.getIntValue("cube_id"));
        map.put("cube_name",jsonObject.getString("cube_name"));
        map.put("cube_desc",jsonObject.getString("cube_desc"));
        map.put("qry_id",jsonObject.getIntValue("qry_id"));
        map.put("icon",jsonObject.getString("icon"));
        map.put("class_name",jsonObject.getString("class_name"));

        // 2. 对 sql 进行编译
        String sql = jsonObject.getString("cube_sql");
        if(null!=sql && !"".equals(sql)){
            sql = sql.replace("'","\\'").replace("{","\\{").replace("}","\\}");
        }
        map.put("cube_sql",sql);

        // 3. 插入到数据库
        sqlSession.update("cube.updateCube",map);
    }

}
