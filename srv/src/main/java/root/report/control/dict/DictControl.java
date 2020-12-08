package root.report.control.dict;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DataBarFormatting;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;
import root.configure.AppConstants;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.query.FuncMetaData;
import root.report.query.SqlTemplate;
import root.report.service.DictService;
import root.report.service.FunctionService;
import root.report.util.ExecuteSqlUtil;
import root.report.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/reportServer/dict")
public class DictControl extends RO {

    private static Logger log = Logger.getLogger(DictControl.class);

    @Autowired
    public DictService dictService;



    //查询所有的数据字典
    @RequestMapping(value = "/getAllDictName", produces = "text/plain;charset=UTF-8")
    public String getAllDictName() {

        List<Map<String, String>> list ;
        try {
            list=dictService.getAllDictName();
            return SuccessMsg("", list);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }
    @RequestMapping(value = "/getDictList", produces = "text/plain; charset=utf-8")
    public String getDictValueList(@RequestBody String pJson)
    {
        Map data=null;
        try {
            data=dictService.getDictValueList(pJson);
            return SuccessMsg("", data);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }

    }

     //返回数据字典的定义头，out
    @RequestMapping(value = "/getDictByID/{dict_id}", produces = "text/plain;charset=UTF-8")
    public String getDictByID(@PathVariable("dict_id") String dict_id) {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            JSONObject jsonObject = this.dictService.getFuncDictInfo(sqlSession,Integer.parseInt(dict_id));
            return SuccessMsg("查询成功",jsonObject);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    /**
     * 功能描述: 接收JSON格式参数，往func_dict跟func_dict_out 中插入相关数据
     */
    @RequestMapping(value = "/createDict", produces = "text/plain;charset=UTF-8")
    public String createDict(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            String dict_id  = this.dictService.createFuncDict(sqlSession,jsonObject);
            this.dictService.createFuncDictOut(sqlSession,jsonObject,dict_id);
            // 往 数据字典.xml 当中 插入 指定SQL
            // this.dictService.createSqlTemplate("数据字典",dict_id,jsonObject.getString("dict_sql"));

            sqlSession.getConnection().commit();
            // DbFactory.init(DbFactory.FORM);
            return SuccessMsg("创建字典信息成功",dict_id);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/updateDict", produces = "text/plain;charset=UTF-8")
    public String updateDict(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            this.dictService.updateFuncDictOut(sqlSession,jsonObject);
            this.dictService.updateFuncDict(sqlSession,jsonObject);

            // 往 数据字典.xml 当中修改 指定SQL
           //  this.dictService.updateSqlTemplate("数据字典",String.valueOf(jsonObject.getIntValue("dict_id")),jsonObject.getString("dict_sql"));

            sqlSession.getConnection().commit();
            // DbFactory.init(DbFactory.FORM);
            return SuccessMsg("修改字典信息成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    // 从json数据当中解析 ，批量删除 func_dict 表跟 func_dict_out 表当中的数据
    @RequestMapping(value = "/deleteDict", produces = "text/plain;charset=UTF-8")
    public String deleteDict(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONArray jsonArray =  JSONObject.parseArray(pJson);

            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int dict_id=jsonObject.getInteger("dict_id");
                //删除值表，删除out表，删除模板,删除主表，
                this.dictService.deleteDictValueByDictID(sqlSession,dict_id);
                this.dictService.deleteFuncDictOut(sqlSession,dict_id);
                this.dictService.deleteFuncDict(sqlSession,dict_id);
                // 往数据字典.xml 当中删除 指定SQL
                // this.dictService.deleteSqlTemplate("数据字典",String.valueOf(dict_id));
            }

            sqlSession.getConnection().commit();
            // DbFactory.init(DbFactory.FORM);
            return SuccessMsg("删除字典信息成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }


    //查询数据字典的值
    @RequestMapping(value = "/getDictValueByID/{dict_id}", produces = "text/plain;charset=UTF-8")
    public String getDictValueByID(@PathVariable("dict_id") String dict_id,@RequestBody String pjson) {
        try{
            JSONObject obj=JSON.parseObject(pjson);
            List<Map> aResult = null;
            Long totalSize = 0L;
            try {
                 Map map = new HashMap();
                RowBounds bounds = null;
                if(obj==null){
                    bounds = RowBounds.DEFAULT;
                }else{
                    int startIndex=obj.getIntValue("pageNumd");
                    int perPage=obj.getIntValue("perPaged");
                    if(startIndex==1 || startIndex==0){
                        startIndex=0;
                    }else{
                        startIndex=(startIndex-1)*perPage;
                    }
                    bounds = new PageRowBounds(startIndex, perPage);
                    map.put("startIndex",startIndex);
                    map.put("perPage",perPage);
                }
                map.put("dict_id",dict_id);
                map.put("searchDictionary",obj.getString("searchDictionary"));

                //  在 func_dict 当中 查找  loaddate_mode 如果是  remote -》 执行sql
                int dictId = Integer.parseInt(dict_id);
                Map<String,Object> funcDictResult = DbFactory.Open(DbFactory.FORM).
                        selectOne("dict.getFuncDictInfoByDictId",dictId);
                String model = String.valueOf(funcDictResult.get("loaddata_mode"));
                if(StringUtils.isNotBlank(model)){
                    if("remote".equals(model)){
                        // 得到sql 执行
                        if(funcDictResult.get("dict_sql")!=null){
                            aResult = (List<Map>) ExecuteSqlUtil.executeDataBaseSql(funcDictResult.get("dict_sql").toString(),
                                    DbFactory.Open(funcDictResult.get("dict_db").toString()),
                                    funcDictResult.get("dict_name").toString(),funcDictResult.get("dict_id").toString(),bounds,
                                    null,Map.class,null,
                                    StatementType.PREPARED,Boolean.TRUE);
                        }
                    }else {
                        aResult = DbFactory.Open(DbFactory.FORM).selectList("dict.getDictValueByID", map,bounds);
                    }
                }

                if(obj!=null){
                    totalSize = ((PageRowBounds)bounds).getTotal();
                }else{
                    totalSize = Long.valueOf(aResult.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map maps=new HashMap<>();
            maps.put("data",aResult);
            maps.put("totald",totalSize);
//            List<Map<String, String>> list  = dictService.getDictValueByID(dict_id);
            return JSON.toJSONString(maps);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }




    //初始化导入  代码注释行
    @RequestMapping(value = "/initImportDictValue/{dict_id}", produces = "text/plain;charset=UTF-8")
    public String importDictValue(@PathVariable("dict_id") String dict_id) {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            int dictId = Integer.parseInt(dict_id);
            // String result = this.dictService.importFuncDictValueByDictId(dictId);
            String result = this.dictService.importFuncDictValue(dictId);
            if("1".equals(result)){
                this.dictService.updateFuncDictForState(sqlSession,dictId,"2");
                sqlSession.getConnection().commit();
                return  SuccessMsg("导入成功",null);
            }else {
                this.dictService.updateFuncDictForState(sqlSession,dictId,"3");
                sqlSession.getConnection().rollback();
                return ErrorMsg("3000",result);
            }
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }
    //增量数据导入
    @RequestMapping(value = "/changeImportDictValue/{dict_id}", produces = "text/plain;charset=UTF-8")
    public String changeDictValue(@PathVariable("dict_id") String dict_id) {
        //源数据加载到内存

        //写入到func_dict_value，返回是进度给前端



        return "未实现";
    }

    //新增字典值
    @RequestMapping(value = "/createDictValue", produces = "text/plain;charset=UTF-8")
    public String createDictValue(@RequestBody String pJson) throws SQLException{
        //源数据加载到内存
        //写入到func_dict_value，返回是进度给前端
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            int dict_id = jsonObject.getIntValue("dict_id");
            this.dictService.createFuncDictValueByDictId(sqlSession,dict_id);
            sqlSession.getConnection().commit();
            return SuccessMsg("导入数据成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }
    //修改字典值
    @RequestMapping(value = "/createFuncDictValue", produces = "text/plain;charset=UTF-8")
    public String createFuncDictValue(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            JSONObject jsonObject = JSON.parseObject(pJson);
            this.dictService.createFuncDictValue2(sqlSession,jsonObject);
            return SuccessMsg("创建数据成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }
    //修改字典值
    @RequestMapping(value = "/updateDictValue", produces = "text/plain;charset=UTF-8")
    public String updateDictValue(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            this.dictService.updateFuncDictValue(sqlSession,jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("修改数据成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    // 删除 func_dict_value 信息
    @RequestMapping(value = "/deleteDictValue", produces = "text/plain;charset=UTF-8")
    public String deleteDictValue(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONArray jsonArray = JSON.parseArray(pJson);
            this.dictService.deleteFuncDictValue(sqlSession,jsonArray);
            sqlSession.getConnection().commit();
            return SuccessMsg("删除数据成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }
    // 删除 func_dict_value 信息
    @RequestMapping(value = "/deleteDictValueByIDCode", produces = "text/plain;charset=UTF-8")
    public String deleteDictValueByIDCode(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            JSONObject jsonArray = JSON.parseObject(pJson);
            this.dictService.deleteFuncDictValueByIDCode(sqlSession,jsonArray);
            return SuccessMsg("删除数据成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }
    //返回数据字典的定义头，out
    @RequestMapping(value = "/getDictValueByDictID", produces = "text/plain;charset=UTF-8")
    public String getDictValueByDictID(@RequestBody String pjson) {
        JSONObject obj=JSON.parseObject(pjson);
        Map map=new HashMap<>();
        map.put("dict_id",obj.getIntValue("dict_id"));
        map.put("value_code",obj.get("value_code"));
        try{
            Map jsonObject = this.dictService.getDictValueByDictID(map);
            return SuccessMsg("查询成功",jsonObject);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }
}
