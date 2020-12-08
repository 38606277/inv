package root.report.control.function;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.github.pagehelper.PageRowBounds;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
//import com.mysql.cj.x.json.JsonArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
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
import root.report.service.FunctionService;
import root.report.util.ExecuteSqlUtil;
import root.report.util.FileUtil;
import root.report.util.JsonUtil;
import root.report.util.XmlUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/reportServer/function1")
public class FunctionControl1 extends RO {

    private static Logger log = Logger.getLogger(FunctionControl1.class);

    @Autowired
    private FunctionService functionService;


    /**
     * 功能描述: 查询 func_name 表当中的所有记录
     */
    @RequestMapping(value = "/getAllFunctionName", produces = "text/plain;charset=UTF-8")
    public String getAllFunctionName() {
        List<Map<String, String>> listFunc ;
        try {
            listFunc=functionService.getAllFunctionName();
            return SuccessMsg("", listFunc);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/getFunctionByID/{func_id}", produces = "text/plain;charset=UTF-8")
    public String getFunctionByID(@PathVariable("func_id") String func_id) {
        try{
            JSONObject jsonObject = functionService.getFunctionByID(func_id);
            return  SuccessMsg("",jsonObject);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    // 根据 class_id 查询所有的 func_name 表当中的信息
    @RequestMapping(value = "/getFunctionByClassID/{class_id}", produces = "text/plain;charset=UTF-8")
    public String getFunctionByClassID(@PathVariable("class_id") String class_id) throws DocumentException, SAXException {
        List<Map<String, Object>> listFunc ;
        try{
            int intClassId = Integer.parseInt(class_id);
            listFunc = functionService.getFunctionByClassID(intClassId);
            return  SuccessMsg("",listFunc);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    // 根据 func_id 查询出 func_in 跟func_out 表当中的数据
    @RequestMapping(value = "/getFunctionParam/{func_id}", produces = "text/plain;charset=UTF-8")
    public String getFunctionParam(@PathVariable("func_id") String func_id) {

        try{
            JSONObject jParam = functionService.getFunctionParam(func_id);
            return  SuccessMsg("",jParam);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/createFunction", produces = "text/plain;charset=UTF-8")
    public String createFunction(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonFunc = JSONObject.parseObject(pJson);

            String func_id = functionService.createFunctionName(sqlSession,jsonFunc);

            functionService.createFunctionIn(sqlSession,jsonFunc.getJSONArray("in"),func_id);
            functionService.createFunctionOut(sqlSession,jsonFunc.getJSONArray("out"),func_id);
          /*  functionService.createSqlTemplate(jsonFunc.getString("class_id"),
                    func_id,
                    jsonFunc.getString("func_sql"));*/
            sqlSession.getConnection().commit();
            // DbFactory.init(DbFactory.FORM);
            return SuccessMsg("新增报表成功",func_id);

        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/updateFunction", produces = "text/plain;charset=UTF-8")
    public String updateFunction(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonFunc = JSONObject.parseObject(pJson);

            int func_id=jsonFunc.getInteger("func_id");

            //删除并创建IN表
            functionService.deleteFunctionInForJsonArray(sqlSession,func_id);
            functionService.createFunctionIn(sqlSession,jsonFunc.getJSONArray("in"),String.valueOf(func_id));

            //先删除后创建Out表
            functionService.deleteFunctionOutForJsonArray(sqlSession,func_id);
            functionService.createFunctionOut(sqlSession,jsonFunc.getJSONArray("out"),String.valueOf(func_id));

            //更新主表
            functionService.updateFunctionName(sqlSession,jsonFunc);
           /* functionService.updateSqlTemplate(jsonFunc.getString("class_id"),
                    jsonFunc.getString("func_id"),
                    jsonFunc.getString("func_sql"));*/
           // 移除掉 configuraiton 当中的 mapper
            ExecuteSqlUtil.removeMapperStatement(sqlSession,jsonFunc.getString("func_name"),jsonFunc.getString("func_id"));
            sqlSession.getConnection().commit();
            // DbFactory.init(DbFactory.FORM);
            return SuccessMsg("修改报表成功","");

        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }


    @RequestMapping(value = "/deleteFunction", produces = "text/plain;charset=UTF-8")
    public String deleteFunction(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONArray jsonArray =  JSONObject.parseArray(pJson);
            String namespace = "";
            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                namespace = sqlSession.selectOne("function.getFuncNameById",jsonObject.getIntValue("func_id"));
                functionService.deleteFunctionName(sqlSession,jsonObject.getIntValue("func_id"));
                functionService.deleteFunctionInForJsonArray(sqlSession,jsonObject.getIntValue("func_id"));
                functionService.deleteFunctionOutForJsonArray(sqlSession,jsonObject.getIntValue("func_id"));
              /*  functionService.deleteSqlTemplate(jsonObject.getString("class_id"),
                        jsonObject.getString("func_id")
                        );*/
                ExecuteSqlUtil.removeMapperStatement(sqlSession,namespace,String.valueOf(jsonObject.getIntValue("func_id")));
            }
            sqlSession.getConnection().commit();
            // DbFactory.init(DbFactory.FORM);
            return SuccessMsg("删除报表成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/createQueryOutLink", produces = "text/plain;charset=UTF-8")
    public String createQueryOutLink(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            this.functionService.createFuncOutLink(sqlSession,jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("新增Func_Out_Link记录成功","");
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/deleteQueryOutLink", produces = "text/plain;charset=UTF-8")
    public String deleteQueryOutLink(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            this.functionService.deleteFuncOutLinkByPrimary(sqlSession,jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("删除Func_Out_Link成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/updateQueryOutLink", produces = "text/plain;charset=UTF-8")
    public String updateQueryOutLink(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            this.functionService.deleteFuncOutLinkByPrimary(sqlSession,jsonObject);
            this.functionService.createFuncOutLink(sqlSession,jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("修改Func_Out_Link成功","");
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }

    /**
     * 功能描述:  查询 func_class 表当中的所有记录
     */
    @RequestMapping(value = "/getAllFunctionClass", produces = "text/plain;charset=UTF-8")
    public String getAllFunctionClass() {
        try{
            List<Map<String,String>> list=functionService.getAllFunctionClass(DbFactory.Open(DbFactory.FORM));
            return  SuccessMsg("",list);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    // 往fucn_class这张表插入一条记录
    @RequestMapping(value = "/createFunctionClassInfo", produces = "text/plain;charset=UTF-8")
    public String createFunctionClassInfo(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
        String class_name = jsonObject.getString("class_name");
        String class_id = "";
        try {
            class_id = this.functionService.createFunctionClass(class_name,sqlSession);
            // DbFactory.init(DbFactory.FORM);
        } catch (IOException e) {
            sqlSession.getConnection().rollback();
            e.printStackTrace();
            return ErrorMsg("","插入数据失败");
        }
        return SuccessMsg("插入数据成功",class_id);
    }

    // 往fucn_class这张表删除一条记录
    @RequestMapping(value = "/deleteFunctionClassInfo", produces = "text/plain;charset=UTF-8")
    public String deleteFunctionClassInfo(@RequestBody String pJson){
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
        int class_id = jsonObject.getInteger("class_id");
        int flag = this.functionService.deleteFunctionClassForRelation(class_id,sqlSession);
        if(flag==2) {
            return ErrorMsg("3000","此func_class正在被其他表关联引用,不能删除");
        }/*else {
            // 删除掉 xml文件
            String userSqlPath = AppConstants.getUserFunctionPath() + File.separator + AppConstants.FunctionPrefix+ class_id + ".xml";
            FileUtil.deleteFile(userSqlPath);
            DbFactory.init(DbFactory.FORM);
        }*/
        return SuccessMsg("删除数据成功",null);
    }

    // 往fucn_class这张表修改一条记录
    @RequestMapping(value = "/updateFunctionClassInfo", produces = "text/plain;charset=UTF-8")
    public String updateFunctionClassInfo(@RequestBody String pJson){
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
        String class_name = jsonObject.getString("class_name");
        int class_id = jsonObject.getInteger("class_id");
        int flag = this.functionService.updateFunctionClass(class_id,class_name,sqlSession);
        if(flag!=1){
            return ErrorMsg("3000","修改数据失败");
        }
        return SuccessMsg("修改数据成功",null);
    }


    // 执行excute的代码 ：
    @RequestMapping(value = "/execFunction/{FunctionName}", produces = "text/plain;charset=UTF-8")
    public Object execFunction(@PathVariable("FunctionName") String FunctionName,
                               @RequestBody String pJson) {
        System.out.println("开始执行查询:" + "FunctionName:" + FunctionName + ","
                + "pJson:" + pJson + ",");
        long t1 = System.nanoTime();
        Object aResult = null;
        try {
            // 检查函数名是否存在、参数是否存在
            // 组装 SqlTemplcat

            // step2: 从数据库组装 sql 值等
            SqlTemplate template = new SqlTemplate();
            // 实例化 FunctionID
            String functionName  = FunctionName;
            String functionId = DbFactory.Open(DbFactory.FORM).selectOne("function.getFuncIdByName",functionName);
            functionService.assemblySqlTemplate(template,FunctionName,functionId);   // 填充sql值，数据库入参
            if(StringUtils.isBlank(template.getSql())){
                return ErrorMsg("3000","数据库查询SQL为空,无法继续操作");
            }

            // step4: 对 in 参数进行解析话，若为 formula 表达式类型则要进行多次计算
            JSONArray inTemplate = template.getIn();
            JSONArray inValue = JSONArray.parseArray(pJson);
            Map<String,Object> map = new LinkedHashMap<String,Object>();   // 映射  in_id <---> 值
            Map<String,Boolean> dataParam = new HashMap<String,Boolean>();  // 映射  in_id <---> formula 是否是表达式
            if (inTemplate != null) {
                //  step 4.1 对 数据库的in 跟前台传进来的 pJosn进行映射
                for (int i = 0; i < inTemplate.size(); i++) {
                    JSONObject aJsonObject = (JSONObject) inTemplate.get(i);
                    String id = aJsonObject.getString("in_id");
                    map.put(id, inValue.getString(i));
                    Boolean inFormula = aJsonObject.getIntValue("isformula")==1?true:false;
                    dataParam.put(id, inFormula);
                }
            }

            // step5. 执行每一步骤的 in 映射集，并将其结果相加
            Map<String,Object> funcParamMap = new HashMap<String,Object>();
            List<FuncMetaData> list = new ArrayList<FuncMetaData>();
            acquireFuncMetaData(list,map,funcParamMap,dataParam);
            // step5.1 执行
            if(list.size()!=0){
                aResult = excuteFunc(list,0,funcParamMap,template);
            }else{
                if(template.getSelectType().equals("sql")) {
                    String db = template.getDb();
                    String namespace = template.getNamespace();
                    String funcId = template.getId();
                    // 直接执行
                    List<BigDecimal> resultBigDecimal = (List<BigDecimal>)ExecuteSqlUtil.executeDataBaseSql(template.getSql(),DbFactory.Open(db),namespace,funcId,null,
                            Map.class,BigDecimal.class,map,StatementType.PREPARED,Boolean.TRUE);
                    if(resultBigDecimal!=null && resultBigDecimal.size()>0){
                        aResult = resultBigDecimal.get(0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            aResult = e.getMessage();
        }
        long t2 = System.nanoTime();
        System.out.println("结束执行查询:" + "FunctionClassName:" + FunctionName+ ","
                + "pJson:" + pJson + ",\n" + "time:" + String.format("%.4fs", (t2 - t1) * 1e-9));
        return aResult;
    }

    private BigDecimal excuteFunc(List<FuncMetaData> list, int index, Map<String,Object> paramMap, SqlTemplate template) throws Exception{
        BigDecimal sum = null;
        int size = list.size();
        FuncMetaData meta = list.get(index);
        String[] paramVal = meta.getParamVal();
        String id = meta.getId();
        String expression = meta.getFuncExpression();
        for(String s:paramVal){
            paramMap.put(id, s);
            if(index<size-1){
                sum = excuteFunc(list,index+1,paramMap,template);
            }else{
                if(template.getSelectType().equals("sql")) {
                    String db = template.getDb();
                    String namespace = template.getNamespace();
                    String funcId = template.getId();
                    // 转换成新执行方法
                    // sum = DbFactory.Open(db).selectOne(namespace + "." + funcId, paramMap);

                    List<BigDecimal> resultBigDecimal = (List<BigDecimal>) ExecuteSqlUtil.executeDataBaseSql(template.getSql(),DbFactory.Open(db),namespace,funcId,null,
                            Map.class, BigDecimal.class,paramMap,StatementType.PREPARED,Boolean.TRUE);  // 注意到没传入 bounds 分页参数
                    if(resultBigDecimal!=null && resultBigDecimal.size()>0){
                        sum = resultBigDecimal.get(0);
                    }
                }
            }
            expression = expression.replace(s, sum.toString());
        }
        Object result = null;
        try{
            Expression exp = AviatorEvaluator.compile(expression);
            result = exp.execute();
        }catch(Exception e){
            throw new Exception("参数表达式不合法");
        }
        return new BigDecimal(result.toString()).setScale(2,BigDecimal.ROUND_HALF_UP);
    }
    //获取函数的元数据
    private void acquireFuncMetaData(List<FuncMetaData> list,Map<String,Object> map,Map<String,Object> funcParamMap,Map<String,Boolean> dateParam){
        Set<String> keys = map.keySet();
        for (String key:keys) {
            String value = (String) map.get(key);
            Boolean inFormula = dateParam.get(key);
            if(inFormula!=null&&inFormula){
                FuncMetaData meta = new FuncMetaData();
                meta.setId(key);
                meta.setFuncExpression(value);
                String[] arr = value.split("\\+|\\-|\\*|\\/|\\(|\\)");
                List<String> tempList = new ArrayList<String>();
                for(String temp:arr){
                    if(temp!=null&&!temp.trim().equals("")){
                        tempList.add(temp.trim());
                    }
                }
                String[] paramVal = new String[tempList.size()];
                tempList.toArray(paramVal);
                meta.setParamVal(paramVal);
                list.add(meta);
            }else{
                funcParamMap.put(key, map.get(key));
            }
        }
    }

    @RequestMapping(value = "/getFunctionClass", produces = "text/plain;charset=UTF-8")
    public String getFunctionClass() {
            List<Map<String,String>> fileList=functionService.getAllFunctionClass(DbFactory.Open(DbFactory.FORM));
            List<Map> list = new ArrayList<Map>();
            for (int i = 0; i < fileList.size(); i++) {
                JSONObject authNode = new JSONObject(true);
                String name = fileList.get(i).get("class_name").toString();
                Integer key =  Integer.parseInt(String.valueOf(fileList.get(i).get("class_id")));
                authNode.put("title", name);
                authNode.put("key", key);
                try {
                    List<Map<String,Object>> chiledList = DbFactory.Open(DbFactory.FORM).
                            selectList("function.getFuncNameInfoByClassID",key);
                    if(chiledList.size()>0) {
                        List<Map> childlist = new ArrayList<Map>();
                        for (int j = 0; j < chiledList.size(); j++) {
                            Map<String, Object> childl = chiledList.get(j);
                            Map<String, Object> childmap = new HashMap<String, Object>();
                            childmap.put("title", childl.get("func_name").toString());
                            childmap.put("key", childl.get("func_id").toString());
                            childlist.add(childmap);
                        }
                        authNode.put("children", childlist);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                list.add(authNode);

            }
            return JSON.toJSONString(list);
        }
}
