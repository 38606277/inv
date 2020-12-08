/*
package root.report.control.question;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.service.QuestionService;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/questions")
public class QuestionControl extends RO {

    private static Logger log = Logger.getLogger(QuestionControl.class);

    @Autowired
    public QuestionService questionService;


    @RequestMapping(value = "/getQuestionsList", produces = "text/plain; charset=utf-8")
    public String getQuestionsList(@RequestBody String pJson)
    {
        try{
            JSONObject obj=JSON.parseObject(pJson);
            List<Map> aResult = null;
            Long totalSize = 0L;
            try {
                Map map = new HashMap();
                RowBounds bounds = null;
                if(obj==null){
                    bounds = RowBounds.DEFAULT;
                }else{
                    int startIndex=obj.getIntValue("pageNum");
                    int perPage=obj.getIntValue("perPage");
                    if(startIndex==1 || startIndex==0){
                        startIndex=0;
                    }else{
                        startIndex=(startIndex-1)*perPage;
                    }
                    bounds = new PageRowBounds(startIndex, perPage);
                    map.put("startIndex",startIndex);
                    map.put("perPage",perPage);
                }
                map.put("ai_question",obj.getString("searchDictionary"));
                aResult = DbFactory.Open(DbFactory.FORM).selectList("question.getQuestionList", map,bounds);
                if(obj!=null){
                    totalSize = ((PageRowBounds)bounds).getTotal();
                }else{
                    totalSize = Long.valueOf(aResult.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map maps=new HashMap<>();
            maps.put("list",aResult);
            maps.put("totald",totalSize);
            return JSON.toJSONString(maps);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }
    //返回数据字典的定义头，out
    @RequestMapping(value = "/getQuestionsByID", produces = "text/plain;charset=UTF-8")
    public String getQuestionsByID(@RequestBody int id) {
       // JSONObject obj=JSON.parseObject(pjson);
        Map map=new HashMap<>();
        map.put("ai_question_id",id);
        try{
            Map jsonObject = (Map) DbFactory.Open(DbFactory.FORM).selectOne("question.getQuestionByPKID", map);
            return SuccessMsg("查询成功",jsonObject);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    */
/**
     * 功能描述: 接收JSON格式参数，往func_dict跟func_dict_out 中插入相关数据
     *//*

    @RequestMapping(value = "/createQuestion", produces = "text/plain;charset=UTF-8")
    public String createDict(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
          String id= this.questionService.createQuestion(sqlSession,jsonObject);

            sqlSession.getConnection().commit();
            // DbFactory.init(DbFactory.FORM);
            return SuccessMsg("创建成功",id);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/updateQuestion", produces = "text/plain;charset=UTF-8")
    public String updateDict(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            this.questionService.updateQuestion(sqlSession,jsonObject);
            sqlSession.getConnection().commit();
            // DbFactory.init(DbFactory.FORM);
            return SuccessMsg("修改成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    // 从json数据当中解析 ，批量删除 func_dict 表跟 func_dict_out 表当中的数据
    @RequestMapping(value = "/deleteQuestion", produces = "text/plain;charset=UTF-8")
    public String deleteDict(@RequestBody int id) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            this.questionService.deleteAnswerByqID(sqlSession,id);
            this.questionService.deleteQuestion(sqlSession,id);
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
    @RequestMapping(value = "/getAnswerListByqID/{question_id}", produces = "text/plain;charset=UTF-8")
    public String getAnswerByqID(@PathVariable("question_id") String question_id,@RequestBody String pjson) {
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
                    int startIndex=obj.getIntValue("pageNum");
                    int perPage=obj.getIntValue("perPage");
                    if(startIndex==1 || startIndex==0){
                        startIndex=0;
                    }else{
                        startIndex=(startIndex-1)*perPage;
                    }
                    bounds = new PageRowBounds(startIndex, perPage);
                    map.put("startIndex",startIndex);
                    map.put("perPage",perPage);
                }
                map.put("answer",obj.getString("answer"));
                map.put("question_id",question_id);
                aResult = DbFactory.Open(DbFactory.FORM).selectList("question.getAnswerList", map,bounds);
                if(obj!=null){
                    totalSize = ((PageRowBounds)bounds).getTotal();
                }else{
                    totalSize = Long.valueOf(aResult.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map maps=new HashMap<>();
            maps.put("list",aResult);
            maps.put("totald",totalSize);
            return JSON.toJSONString(maps);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    //新增字典值
    @RequestMapping(value = "/createAnswer", produces = "text/plain;charset=UTF-8")
    public String createAnswer(@RequestBody String pJson) throws SQLException{
        //源数据加载到内存
        //写入到func_dict_value，返回是进度给前端
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            this.questionService.createAnswer(sqlSession,jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("创建数据成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    //修改字典值
    @RequestMapping(value = "/updateAnswer", produces = "text/plain;charset=UTF-8")
    public String updateAnswer(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            this.questionService.updateAnswer(sqlSession,jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("修改数据成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    // 删除 func_dict_value 信息
    @RequestMapping(value = "/deleteAnswer", produces = "text/plain;charset=UTF-8")
    public String deleteAnswer(@RequestBody int id) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
          //  JSONObject jsonArray = JSON.parseObject(pJson);
            this.questionService.deleteAnswer(sqlSession,id);
            sqlSession.getConnection().commit();
            return SuccessMsg("删除数据成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }
    // 删除 func_dict_value 信息
    @RequestMapping(value = "/deleteAnswerByqID", produces = "text/plain;charset=UTF-8")
    public String deleteAnswerByqID(@RequestBody int id) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
           // JSONObject jsonArray = JSON.parseObject(pJson);
            this.questionService.deleteAnswerByqID(sqlSession,id);
            return SuccessMsg("删除数据成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }
    //返回数据字典的定义头，out
    @RequestMapping(value = "/getAnswerByID", produces = "text/plain;charset=UTF-8")
    public String getAnswerByID(@RequestBody int id) {
     //   JSONObject obj=JSON.parseObject(pjson);
        Map map=new HashMap<>();
        map.put("answer_id",id);
        try{
            Map<String,Object> jsonObject = (Map<String,Object>) DbFactory.Open(DbFactory.FORM).selectOne("question.getAnswerByPKID", map);
            return SuccessMsg("查询成功",jsonObject);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/getDefaultAnswerByQID/{question_id}", produces = "text/plain;charset=UTF-8")
    public String getDefaultAnswerByQID(@PathVariable("question_id") String question_id) {
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("question_id",question_id);
        List aResult = DbFactory.Open(DbFactory.FORM).selectList("question.getDefaultAnswerList", map);
        return SuccessMsg("创建数据成功",aResult);
    }
    @RequestMapping(value = "/saveQuestionAudio/{qid}", produces = "text/plain;charset=UTF-8")
    public String saveQuestionAudio(@PathVariable("qid") String qid,@RequestParam("file") MultipartFile file) throws Exception {
        Map map=new HashMap<>();
        InputStream inputStream=  file.getInputStream();
        byte[] buf=InputStreamToByte(inputStream);
        if(qid==null || qid.equals("null")){
            SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
            try{
                sqlSession.getConnection().setAutoCommit(false);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("fileDataBlob",buf);
                String id=this.questionService.createQuestionAudio(sqlSession,jsonObject);
                sqlSession.getConnection().commit();
                return SuccessMsg("创建数据成功",id);
            }catch (Exception ex){
                sqlSession.getConnection().rollback();
                ex.printStackTrace();
                return ExceptionMsg(ex.getMessage());
            }
        }else{
            SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
            try{
                sqlSession.getConnection().setAutoCommit(false);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ai_question_id",qid);
                jsonObject.put("fileDataBlob",buf);
                this.questionService.updateQuestionAudio(sqlSession,jsonObject);
                sqlSession.getConnection().commit();
                return SuccessMsg("修改数据成功",qid);
            }catch (Exception ex){
                sqlSession.getConnection().rollback();
                ex.printStackTrace();
                return ExceptionMsg(ex.getMessage());
            }
        }

    }

    @RequestMapping(value = "/saveAnswerAudio/{aid}/{qid}", produces = "text/plain;charset=UTF-8")
    public String saveAnswerAudio(@PathVariable("aid") String aid,@PathVariable("qid") int qid,@RequestParam("file") MultipartFile file) throws Exception {
        Map map=new HashMap<>();
        InputStream inputStream=  file.getInputStream();
        byte[] buf=InputStreamToByte(inputStream);
        if(aid==null || aid.equals("null")){
            SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
            try{
                sqlSession.getConnection().setAutoCommit(false);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("question_id",qid);
                jsonObject.put("fileDataBlob",buf);
                String id=this.questionService.createAnswerAudio(sqlSession,jsonObject);
                sqlSession.getConnection().commit();
                return SuccessMsg("创建数据成功",id);
            }catch (Exception ex){
                sqlSession.getConnection().rollback();
                ex.printStackTrace();
                return ExceptionMsg(ex.getMessage());
            }
        }else{
            SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
            try{
                sqlSession.getConnection().setAutoCommit(false);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("answer_id",aid);
                jsonObject.put("fileDataBlob",buf);
                this.questionService.updateAnswerAudio(sqlSession,jsonObject);
                sqlSession.getConnection().commit();
                return SuccessMsg("修改数据成功",aid);
            }catch (Exception ex){
                sqlSession.getConnection().rollback();
                ex.printStackTrace();
                return ExceptionMsg(ex.getMessage());
            }
        }
    }
    */
/**
       * 输入流转字节流
       * *//*

    private byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        byte[] buffer=new byte[1024];
        int ch;
        while ((ch = is.read(buffer)) != -1) {
            bytestream.write(buffer,0,ch);
        }
        byte data[] = bytestream.toByteArray();
        bytestream.close();
        return data;
    }

    @RequestMapping(value = "/getQuestionAduioBlob")
    public void getQuestionAduioBlob(@RequestBody String id, HttpServletResponse response) throws Exception {
        try{
            Map map=new HashMap<>();
            map.put("ai_question_id",id);
            response.setHeader("Content-Type", "audio/mpeg");
            Map<String,Object> jsonObject = (Map<String,Object>) DbFactory.Open(DbFactory.FORM).selectOne("question.getQuestionByPKID", map);
            byte[] contents = (byte[])jsonObject.get("fileDataBlob");
            System.out.println("内容是:"+contents.length);
            InputStream is = new ByteArrayInputStream(contents);
            response.setHeader("Content-Type", "audio/mpeg");
            OutputStream out = response.getOutputStream();
            int len=0;
            byte[] buf=new byte[1024];
            while((len=is.read(buf,0,1024))!=-1){
                out.write(buf, 0, len);
            }
            out.flush();
            out.close();
        }catch (Exception e){
            System.out.println(e);
        }
    }
    @RequestMapping(value = "/getAnswerAduioBlob")
    public void getAnswerAduioBlob(@RequestBody String id, HttpServletResponse response) throws Exception {
        try{
            Map map=new HashMap<>();
            map.put("answer_id",id);
            response.setHeader("Content-Type", "audio/mpeg");
            Map<String,Object> jsonObject = (Map<String,Object>) DbFactory.Open(DbFactory.FORM).selectOne("question.getAnswerByPKID", map);
            byte[] contents = (byte[])jsonObject.get("fileDataBlob");
            System.out.println("内容是:"+contents.length);
            InputStream is = new ByteArrayInputStream(contents);
            response.setHeader("Content-Type", "audio/mpeg");
            OutputStream out = response.getOutputStream();
            int len=0;
            byte[] buf=new byte[1024];
            while((len=is.read(buf,0,1024))!=-1){
                out.write(buf, 0, len);
            }
            out.flush();
            out.close();
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
*/
