package root.report.control.da;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.auth.RoleModel;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.service.CubeService;
import root.report.service.DictService;
import root.report.util.ExecuteSqlUtil;
import root.report.util.JsonUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/cube")
public class CubeControl extends RO {

    private static Logger log = Logger.getLogger(CubeControl.class);

    @Autowired
    CubeService cubeService;

    //查询所有的cube 记录
    @RequestMapping(value = "/getAllCube", produces = "text/plain;charset=UTF-8")
    public String getAllDictName(@RequestBody String pJson) {
       // SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try {
            // TODO 是否需要分页？
          //  List<Map> mapList = sqlSession.selectList("cube.getAllCube");
           // return SuccessMsg("",JSON.toJSONString(mapList,JsonUtil.features));
            JSONObject obj = (JSONObject) JSON.parse(pJson);
            Map<String,Object> map = new HashMap<String,Object>();
            Long total = 0L;
            RowBounds bounds = null;
            if(obj==null){
                bounds = RowBounds.DEFAULT;
            }else {
                int currentPage = Integer.valueOf(obj.getIntValue("pageNum"));
                int perPage = Integer.valueOf(obj.getIntValue("perPage"));
                if (1 == currentPage || 0 == currentPage) {
                    currentPage = 0;
                } else {
                    currentPage = (currentPage - 1) * perPage;
                }
                bounds = new PageRowBounds(currentPage, perPage);
            }
            map.put("cube_name",  obj.get("cube_name")==null?"":obj.getString("cube_name"));
            List<Map<String,Object>> list = DbFactory.Open(DbFactory.FORM).selectList("cube.getAllCube",map,bounds);
            if(obj!=null){
                total = ((PageRowBounds)bounds).getTotal();
            }else{
                total = Long.valueOf(list.size());
            }
            Map<String,Object> map3 =new HashMap<String,Object>();
            map3.put("list",list);
            map3.put("total",total);
            return SuccessMsg("",map3);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }


     //返回数据字典的定义头，out
    @RequestMapping(value = "/getCubeByID/{cube_id}", produces = "text/plain;charset=UTF-8")
    public String getCubeByID(@PathVariable("cube_id") String cube_id) {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try {
            Map map = sqlSession.selectOne("cube.getCubeById",Integer.parseInt(cube_id));
            return SuccessMsg("",map);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    /**
     * 功能描述: 接收JSON格式参数，往func_dict跟func_dict_out 中插入相关数据
     *          往da_cube 当中插入对应数据
     */
    @RequestMapping(value = "/createCube", produces = "text/plain;charset=UTF-8")
    public String createCube(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try {
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            String uuid = this.cubeService.createCuBe(sqlSession,jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("新增cube记录成功",uuid);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/updateCube", produces = "text/plain;charset=UTF-8")
    public String updateCube(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try {
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            this.cubeService.updateCuBe(sqlSession,jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("修改cube记录成功","");
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }

    // 批量删除 da_cube 表的记录
    @RequestMapping(value = "/deleteCube", produces = "text/plain;charset=UTF-8")
    public String deleteCube(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try {
            sqlSession.getConnection().setAutoCommit(false);
            JSONArray jsonArray = JSON.parseArray(pJson);
            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int cube_id = jsonObject.getInteger("cube_id");
                sqlSession.delete("cube.deleteCubeById",cube_id);
            }
            sqlSession.getConnection().commit();
            return SuccessMsg("删除成功","");
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }


    @RequestMapping(value = "/getCubeValueByID/{qry_id}", produces = "text/plain;charset=UTF-8")
    public String getCubeValueByID(@PathVariable("qry_id") String qry_id,@RequestBody String pjson) {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("qry_id",qry_id);
        try {
            Map<String,Object> map = new HashMap<String,Object>();
            List<Map> mapIn = sqlSession.selectList("cube.getCubeInById",param);
            List<Map> mapOut = sqlSession.selectList("cube.getCubeOutById",param);
            map.put("in",mapIn);
            map.put("out",mapOut);
            return SuccessMsg("",map);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }




}
