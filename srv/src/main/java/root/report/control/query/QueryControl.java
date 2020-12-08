package root.report.control.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
import root.report.service.QueryService;
import root.report.util.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/reportServer/query")
public class QueryControl extends RO {

    private static Logger log = Logger.getLogger(QueryControl.class);

    @Autowired
    private QueryService queryService;



    @RequestMapping(value = "/getAllQueryName", produces = "text/plain;charset=UTF-8")
    public String getAllQueryName() {
        List<Map<String, String>> listFunc = new ArrayList<>();
        try {
            listFunc = queryService.getAllQueryName();
            return SuccessMsg("", listFunc);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }


    @RequestMapping(value = "/getQueryByID/{qry_id}", produces = "text/plain;charset=UTF-8")
    public String getQueryByID(@PathVariable("qry_id") String qry_id) {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            JSONObject jsonObject = queryService.getQueryByID(sqlSession,qry_id);
            return  SuccessMsg("",jsonObject);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    // 根据 class_id 查询所有的 func_name 表当中的信息
    @RequestMapping(value = "/getQueryByClassID/{class_id}", produces = "text/plain;charset=UTF-8")
    public String getQueryByClassID(@PathVariable("class_id") String class_id) throws DocumentException, SAXException {

        List<Map<String, Object>> listFunc = new ArrayList<>();
        int intClassId = Integer.parseInt(class_id);
        try {
            listFunc = queryService.getQueryByClassID(intClassId);
            return SuccessMsg("", listFunc);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }

    }


    // 根据 func_id 查询出 func_in 跟func_out 表当中的数据
    @RequestMapping(value = "/getQueryParam/{qry_id}", produces = "text/plain;charset=UTF-8")
    public String getQueryParam(@PathVariable("qry_id") String qry_id) {


        try {
            JSONObject jsonQryParam = queryService.getQueryParam(qry_id);
            return SuccessMsg("", jsonQryParam);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }

    }

//    // 根据 func_id 查询出 func_in 跟func_out 表当中的数据
//    @RequestMapping(value = "/getQueryByChineseName/{qry_name}", produces = "text/plain;charset=UTF-8")
//    public String getQueryByChineseName(@PathVariable("qry_name") String qry_name) {
//
//
//        try {
//            Map<String, Object> query= queryService.getQueryByName(qry_name);
//            return SuccessMsg("", query);
//        } catch (Exception ex){
//            return ExceptionMsg(ex.getMessage());
//        }
//
//    }

    @RequestMapping(value = "/createQuery", produces = "text/plain;charset=UTF-8")
    public String createQuery(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            String qry_id = queryService.createQueryName(sqlSession,jsonFunc);


            queryService.createQueryIn(sqlSession,jsonFunc.getJSONArray("in"),qry_id);
            queryService.createQueryOut(sqlSession,jsonFunc.getJSONArray("out"),qry_id);
            /*queryService.createSqlTemplate(jsonFunc.getString("class_id"),
                                                String.valueOf(qry_id),
                                              jsonFunc.getString("qry_sql"));*/
            sqlSession.getConnection().commit();
           //  DbFactory.init(DbFactory.FORM);
            return SuccessMsg("新增报表成功",qry_id);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/updateQuery", produces = "text/plain;charset=UTF-8")
    public String updateQuery(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonQuery = JSONObject.parseObject(pJson);
            int qry_id=jsonQuery.getInteger("qry_id");

            //删除并创建IN表
            queryService.deleteQueryInForJsonArray(sqlSession,qry_id);
            queryService.createQueryIn(sqlSession,jsonQuery.getJSONArray("in"),String.valueOf(qry_id));

            //先删除后创建Out表
            queryService.deleteQueryOutForJsonArray(sqlSession,qry_id);
            queryService.createQueryOut(sqlSession,jsonQuery.getJSONArray("out"),String.valueOf(qry_id));

            //更新主表
            queryService.updateQueryName(sqlSession,jsonQuery);

            //更新SQL文件
          /*  queryService.updateSqlTemplate(jsonQuery.getString("class_id"),
                    jsonQuery.getString("qry_id"),
                    jsonQuery.getString("qry_sql"));*/
            ExecuteSqlUtil.removeMapperStatement(sqlSession,AppConstants.QueryPrefix +jsonQuery.getString("class_id"),jsonQuery.getString("qry_id"));
            sqlSession.getConnection().commit();
           //  DbFactory.init(DbFactory.FORM);
            return SuccessMsg("修改报表成功","");

        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }


    @RequestMapping(value = "/deleteQuery", produces = "text/plain;charset=UTF-8")
    public String deleteQuery(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONArray jsonArray =  JSONObject.parseArray(pJson);
            String namespace = "";
            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int qry_id=jsonObject.getInteger("qry_id");
                namespace = sqlSession.selectOne("query.getQueryNameById",jsonObject.getIntValue("qry_id"));
                queryService.deleteQueryName(sqlSession,qry_id);
                queryService.deleteQueryInForJsonArray(sqlSession,qry_id);
                queryService.deleteQueryOutForJsonArray(sqlSession,qry_id);
                /*queryService.deleteSqlTemplate(jsonObject.getString("class_id"),
                        jsonObject.getString("qry_id")
                );*/
                ExecuteSqlUtil.removeMapperStatement(sqlSession,namespace,String.valueOf(jsonObject.getIntValue("qry_id")));
            }

            sqlSession.getConnection().commit();
            // DbFactory.init(DbFactory.FORM);
            return SuccessMsg("删除报表成功",null);

        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }
    // 根据 class_id 查询所有的 func_name 表当中的信息
    @RequestMapping(value = "/getQueryOutLink/{qry_id}/{out_id}", produces = "text/plain;charset=UTF-8")
    public String getQueryOutLink(@PathVariable("qry_id") String qry_id,
                                    @PathVariable("out_id") String out_id) throws DocumentException, SAXException {

        List<Map<String, String>> listFunc = new ArrayList<>();
        try {
            listFunc = queryService.getQueryOutLink(qry_id,out_id);
            return SuccessMsg("", listFunc);
        } catch (Exception ex){
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
            this.queryService.createQueryOutLink(sqlSession,jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("新增Out_Link记录成功","");
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
            this.queryService.deleteQueryOutLinkByPrimary(sqlSession,jsonObject);
            this.queryService.createQueryOutLink(sqlSession,jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("修改Out_Link成功","");
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }


    @RequestMapping(value = "/deleteQueryOutLink", produces = "text/plain;charset=UTF-8")
    public String deleteQueryOutLink(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            this.queryService.deleteQueryOutLinkByPrimary(sqlSession,jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("删除Out_Link成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }


    @RequestMapping(value = "/uploadImg", produces = "text/plain;charset=UTF-8")
    public String uploadImg(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
        int class_Id = jsonObject.getInteger("calssid");
        String img_file = jsonObject.getString("img_file");
       int  flag= this.queryService.updateQueryClass(class_Id,null,img_file,sqlSession);
        if(flag!=1){
            return ErrorMsg("3000","修改数据失败");
        }
        return SuccessMsg("修改数据成功",null);
    }
    // 往qry_class这张表插入一条记录 并生成mapper.xml文件
    @RequestMapping(value = "/createQueryClassInfo", produces = "text/plain;charset=UTF-8")
    public String createQueryClassInfo(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
        String class_name = jsonObject.getString("class_name");
        String img_file = jsonObject.getString("img_file");
        String class_id = "";
        try {
            class_id = this.queryService.createQueryClass(class_name,img_file,sqlSession);
            // DbFactory.init(DbFactory.FORM);
        } catch (IOException e) {
            sqlSession.getConnection().rollback();
            e.printStackTrace();
            return ErrorMsg("","插入数据失败");
        }
        return SuccessMsg("插入数据成功",class_id);
    }

    // 在 qry_class 这张表删除一条记录
    @RequestMapping(value = "/deleteQueryClassInfo", produces = "text/plain;charset=UTF-8")
    public String deleteQueryClassInfo(@RequestBody String pJson){
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
        int class_id = jsonObject.getInteger("class_id");
        int flag = this.queryService.deleteQueryClassForRelation(class_id,sqlSession);
        if(flag==2){
            return ErrorMsg("3000","此func_class正在被其他表关联引用,不能删除");
        }/*else {
            // 删除掉 xml文件
            String userSqlPath = AppConstants.getUserSqlPath() + File.separator + AppConstants.QueryPrefix+ class_id + ".xml";
            FileUtil.deleteFile(userSqlPath);
            DbFactory.init(DbFactory.FORM);
        }*/
        return SuccessMsg("删除数据成功",null);
    }

    // 往fucn_class这张表修改一条记录
    @RequestMapping(value = "/updateQueryClassInfo", produces = "text/plain;charset=UTF-8")
    public String updateQueryClassInfo(@RequestBody String pJson){
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
        String class_name = jsonObject.getString("class_name");
        int class_id = jsonObject.getInteger("class_id");
        int flag = this.queryService.updateQueryClass(class_id,class_name,null,sqlSession);
        if(flag!=1){
            return ErrorMsg("3000","修改数据失败");
        }
        return SuccessMsg("修改数据成功",null);
    }

    @RequestMapping(value = "/getAllQueryClass", produces = "text/plain;charset=UTF-8")
    public String getAllQueryClass() {
        try{
            List<Map<String,String>> list = queryService.getAllQueryClass(DbFactory.Open(DbFactory.FORM));
            return  SuccessMsg("",list);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    //获取函数的元数据
    private void acquireFuncMetaData(List<FuncMetaData> list,Map<String,Object> map,Map<String,Object> funcParamMap){
        Set<String> keys = map.keySet();
        for (String key:keys) {
            funcParamMap.put(key, map.get(key));
        }
    }

    // 执行sql 得到结果
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
                    sum = DbFactory.Open(db).selectOne(namespace + "." + funcId, paramMap);
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

    // 执行excute的代码 ： 版本2
    @RequestMapping(value = "/execQuery/{QueryClassName}/{QueryID}", produces = "text/plain;charset=UTF-8")
    public String execQuery(@PathVariable("QueryClassName") String queryClassName,
                               @PathVariable("QueryID") String queryID, @RequestBody String pJson) {
        System.out.println("开始执行查询:" + "selectClassName:" + queryClassName + "," + "selectID:" + queryID + ","
                + "pJson:" + pJson + ",");
        long t1 = System.nanoTime();

        JSONObject result = null;
        try{
            result = new JSONObject(this.queryService.executeSql(queryClassName,queryID,pJson));
/*            JSONArray arr = JSON.parseArray(pJson);
            JSONObject params = arr.getJSONObject(0);//查询参数
            JSONObject page = null;
            if(arr.size()>1){
                page = arr.getJSONObject(1);  //分页对象
            }
            SqlTemplate template = new SqlTemplate();
            queryService.assemblySqlTemplateTwo(template,queryClassName,queryID);
            if(StringUtils.isBlank(template.getSql())){
                return ErrorMsg("3000","数据库查询SQL为空,无法继续操作");
            }
            // 输入参数放入map中
            // JSONArray inTemplate = template.getIn();

            JSONObject objin=params.getJSONObject("in");
            RowBounds bounds = null;
            if(page==null || page.size()==0){
                bounds = RowBounds.DEFAULT;
            }else{
                int startIndex=page.getIntValue("startIndex");
                int perPage=page.getIntValue("perPage");
                if(startIndex==1 || startIndex==0){
                    startIndex=0;
                }else{
                    startIndex=(startIndex-1)*perPage;
                }
                bounds = new PageRowBounds(startIndex, perPage);
            }
            Map map = new HashMap();
            if(objin!=null){
                String value = null,key=null;
                java.util.Iterator it = objin.entrySet().iterator();
                while(it.hasNext()) {
                    java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                    key=entry.getKey().toString(); //返回与此项对应的键
                    value=entry.getValue().toString(); //返回与此项对应的值
                    map.put(key, value);
                }
            }
            List<Map> aResult = new ArrayList<Map>();
            Long totalSize = 0L;
            String db = template.getDb();
            String namespace = template.getNamespace();
            String qryId = template.getId();
            // 改写掉  用新的方式 VERSION TWO 版本
            // aResult = DbFactory.Open(db).selectList(namespace + "." + qryId, map, bounds);
            SqlSession targetSqlSession = DbFactory.Open(db);
            // 强转成自己想要的类型
            aResult = (List<Map>) ExecuteSqlUtil.executeDataBaseSql(template.getSql(),targetSqlSession,namespace,qryId,bounds,
                    Map.class,Map.class,map,StatementType.PREPARED,true);
            List<Map<String, Object>> newList = new ArrayList<Map<String,Object>>();
            //将集合遍历
            for(int i=0;i<aResult.size();i++) {
                //循环new  map集合
                Map<String, Object> obdmap = new HashMap<String, Object>();
                Set<String> se = aResult.get(i).keySet();
                for (String set : se) {
                    //在循环将大写的KEY和VALUE 放到新的Map
                    obdmap.put(set.toUpperCase(), aResult.get(i).get(set));
                }
                //将Map放进List集合里
                newList.add(obdmap);
            }
            if(page!=null && page.size()!=0){
                totalSize = ((PageRowBounds)bounds).getTotal();
            }else{
                totalSize = Long.valueOf(newList.size());
            }
            result.put("list", newList);
            result.put("totalSize", totalSize);*/
        }catch (Exception e){
            e.printStackTrace();
            return ExceptionMsg(e.getCause().getMessage());
        }

        long t2 = System.nanoTime();
        System.out.println("结束执行查询:" + "QueryClassName:" + queryClassName + "," + "selectID:" + queryID + ","
                + "pJson:" + pJson + ",\n" + "time:" + String.format("%.4fs", (t2 - t1) * 1e-9));
        return SuccessMsg("",result);

    }

    // 执行excute的代码 ： 版本2
    @RequestMapping(value = "/execqueryToExcel/{QueryClassName}/{QueryID}", produces = "text/plain;charset=UTF-8")
    public String execqueryToExcel(@PathVariable("QueryClassName") String queryClassName,
                            @PathVariable("QueryID") String queryID, @RequestBody String pJson) {
        System.out.println("开始执行查询:" + "selectClassName:" + queryClassName + "," + "selectID:" + queryID + ","
                + "pJson:" + pJson + ",");
       JSONObject result = null;
        Map m=new HashMap<>();
        try{
            result = new JSONObject(this.queryService.executeSql(queryClassName,queryID,pJson));
            List<Map<String,Object>> outList= (List<Map<String, Object>>) result.get("out");
            List titles=new ArrayList<>();
            List column =new ArrayList<>();
            for(int i=0;i<outList.size();i++){
                Map oMap=outList.get(i);
                titles.add(oMap.get("out_name"));
                String outID=oMap.get("out_id").toString();
                column.add(outID.toUpperCase());
            }
            m= ExportExcel.exportExcel("数据查询","数据查询", titles, column,(List<Object>) result.get("list"));
            m.put("filetype","file");
        }catch (Exception e){
            e.printStackTrace();
            return ExceptionMsg(e.getCause().getMessage());
        }
        return SuccessMsg("",m);

    }
    // 执行excute的代码 ：
    @RequestMapping(value = "/execQueryOld/{QueryClassName}/{QueryID}", produces = "text/plain;charset=UTF-8")
    public String execQueryOld(@PathVariable("QueryClassName") String queryClassName,
                               @PathVariable("QueryID") String queryID, @RequestBody String pJson) {
        System.out.println("开始执行查询:" + "selectClassName:" + queryClassName + "," + "selectID:" + queryID + ","
                + "pJson:" + pJson + ",");
        long t1 = System.nanoTime();

        JSONObject result = new JSONObject();
        try{
            JSONArray arr = JSON.parseArray(pJson);
            JSONObject params = arr.getJSONObject(0);//查询参数
            JSONObject page = null;
            if(arr.size()>1){
                page = arr.getJSONObject(1);  //分页对象
            }
            SqlTemplate template = new SqlTemplate();
            queryService.assemblySqlTemplate(template,queryClassName,queryID);
            // 输入参数放入map中
           // JSONArray inTemplate = template.getIn();
            JSONArray jsonArray = params.getJSONArray("in");
            RowBounds bounds = null;
            if(page==null){
                bounds = RowBounds.DEFAULT;
            }else{
                int startIndex=page.getIntValue("startIndex");
                int perPage=page.getIntValue("perPage");
                if(startIndex==1 || startIndex==0){
                    startIndex=0;
                }else{
                    startIndex=(startIndex-1)*perPage;
                }
                bounds = new PageRowBounds(startIndex, perPage);
            }
            Map map = new HashMap();
            if(jsonArray!=null){
                String value = null,key=null;
                JSONObject aJsonObject = null;
                for (int i = 0; i < jsonArray.size(); i++){
                    aJsonObject = (JSONObject) jsonArray.get(i);
                    java.util.Iterator it = aJsonObject.entrySet().iterator();
                    while(it.hasNext()) {
                        java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                        key=entry.getKey().toString(); //返回与此项对应的键
                        value=entry.getValue().toString(); //返回与此项对应的值
                    }
                    map.put(key, value);
                }
            }
//            map.put("name",page.getString("searchResult"));
            List<Map> aResult = new ArrayList<Map>();
            Long totalSize = 0L;
            String db = template.getDb();
            String namespace = template.getNamespace();
            String qryId = template.getId();
            aResult = DbFactory.Open(db).selectList(namespace + "." + qryId, map, bounds);
            if(page!=null){
                totalSize = ((PageRowBounds)bounds).getTotal();
            }else{
                totalSize = Long.valueOf(aResult.size());
            }
            result.put("list", aResult);
            result.put("totalSize", totalSize);
        }catch (Exception e){
            e.printStackTrace();
            return ExceptionMsg(e.getCause().getMessage());
        }

//            Object aResult = null;
//        try {
//            // String usersqlPath = AppConstants.getUserSqlPath() + File.separator + queryClassName + ".xml";
//            SqlTemplate template = new SqlTemplate();
//            queryService.assemblySqlTemplate(template,queryClassName,queryID);
//            // 输入参数放入map中
//            JSONArray inTemplate = template.getIn();
//            JSONArray inValue = JSONArray.parseArray(pJson);
//
//            Map<String,Object> map = new LinkedHashMap<String,Object>();
//            if (inTemplate != null) {
//                for (int i = 0; i < inTemplate.size(); i++) {
//                    JSONObject aJsonObject = (JSONObject) inTemplate.get(i);
//                    String id = aJsonObject.getString("in_id");
//                    map.put(id, inValue.getString(i));
//                }
//            }
//            Map<String,Object> qryParamMap = new HashMap<String,Object>();
//            List<FuncMetaData> list = new ArrayList<FuncMetaData>();
//            acquireFuncMetaData(list,map,qryParamMap);
//            if(list.size()!=0){
//                aResult = excuteFunc(list,0,qryParamMap,template);
//            }else{
//                    String db = template.getDb();
//                    String namespace = template.getNamespace();
//                    String qryId = template.getId();
//                    aResult = DbFactory.Open(db).selectOne("qry_"+namespace + "." + qryId, map);
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            aResult=e.getMessage();
//        }

        long t2 = System.nanoTime();
        System.out.println("结束执行查询:" + "QueryClassName:" + queryClassName + "," + "selectID:" + queryID + ","
                + "pJson:" + pJson + ",\n" + "time:" + String.format("%.4fs", (t2 - t1) * 1e-9));
        return JSON.toJSONString(result);

    }
    /**
     * gaoluo
     * */
    @RequestMapping(value = "/getQueryClassTree", produces = "text/plain;charset=UTF-8")
    public String getQueryClassTree() {
        List<Map<String,String>> fileList = queryService.getAllQueryClass(DbFactory.Open(DbFactory.FORM));
        List<Map> list = new ArrayList<Map>();
        for (int i = 0; i < fileList.size(); i++) {
            JSONObject authNode = new JSONObject(true);
            String name = fileList.get(i).get("class_name").toString();
            Integer key =  Integer.parseInt(String.valueOf(fileList.get(i).get("class_id")));
            authNode.put("title", name);
            authNode.put("key", key);
            try {
                List<Map<String,Object>> chiledList = DbFactory.Open(DbFactory.FORM).
                        selectList("query.getQueryNameInfoByClassID",key);
                if(chiledList.size()>0) {
                    List<Map> childlist = new ArrayList<Map>();
                    for (int j = 0; j < chiledList.size(); j++) {
                        Map<String, Object> childl = chiledList.get(j);
                        Map<String, Object> childmap = new HashMap<String, Object>();
                        childmap.put("title", childl.get("qry_name").toString());
                        childmap.put("key", childl.get("qry_id").toString());
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


    /**
     * gaoluo
     * */
    @RequestMapping(value = "/getAuthTree", produces = "text/plain;charset=UTF-8")
    public String getAuthTree(@RequestBody String pjson) {
        JSONObject obj=JSONObject.parseObject(pjson);
        int userId=obj.getInteger("userId");
        List<Map<String,String>> fileList = queryService.getAuthTree(DbFactory.Open(DbFactory.FORM),userId);
        List<String> f=new ArrayList<String>();
        List<String> pid=new ArrayList<String>();
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        for (int i = 0; i < fileList.size(); i++) {
            String auth_type=fileList.get(i).get("auth_type");
            String func_id=fileList.get(i).get("func_id");
            if(auth_type.equals("select")){
                try {
                    List<Map<String,Object>> childId=new ArrayList<Map<String,Object>>();
                    Map<String,Object> parMap = queryService.getAllQueryClassByClassId(Integer.parseInt(func_id));
                    if(null!=parMap){
                        for (int ii = 0; ii < fileList.size(); ii++) {
                            String auth_typetwo=fileList.get(ii).get("auth_type");
                            String qry_id=fileList.get(ii).get("func_id");
                            if(auth_typetwo.equals("select")) {
                                Map<String, Object> map = queryService.getQueryNameByClassIdQryId(Integer.parseInt(func_id), Integer.parseInt(qry_id));
                                if(null!=map) {
                                    childId.add(map);
                                }
                            }
                        }
                        parMap.put("children",childId);
                        list.add(parMap);
                    }
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
            if(auth_type.equals("function")){
                try {

                    Map<String,Object> parMap = queryService.getFunctionClassByClassId(Integer.parseInt(func_id));
                    List<Map<String,Object>> childId=new ArrayList<Map<String,Object>>();
                    if(null!=parMap){
                        for (int iii = 0; iii < fileList.size(); iii++) {
                            String auth_typeFunc=fileList.get(iii).get("auth_type");
                            String function_id=fileList.get(iii).get("func_id");
                            if(auth_typeFunc.equals("function")) {
                                Map<String, Object> map = queryService.getFunctionNameByClassIdPId(Integer.parseInt(func_id), Integer.parseInt(function_id));
                                if(null!=map) {
                                    childId.add(map);
                                }
                            }
                        }
                        parMap.put("children",childId);
                        list.add(parMap);
                    }

                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        }

        return JSON.toJSONString(list);
    }

    //查询所有的cube 记录
    @RequestMapping(value = "/getAllQueryNameList", produces = "text/plain;charset=UTF-8")
    public String getAllQueryNameList(@RequestBody String pJson) {
        try {
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
            map.put("qry_name",  obj.get("qry_name")==null?"":obj.getString("qry_name"));
            List<Map<String,Object>> list = DbFactory.Open(DbFactory.FORM).selectList("query.getAllQueryNameList",map,bounds);
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

    //格式化SQL
    @RequestMapping(value = "/sqlFormat", produces = "text/plain;charset=UTF-8")
    public String sqlFormat(@RequestBody String pJson) {
        SQLFormatUtil formatUtil=new SQLFormatUtil();
        String newSql= formatUtil.format(pJson);
        return  SuccessMsg("",newSql);
    }
}
