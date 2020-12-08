package root.report.control.upload;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.service.UploadService;
import root.report.util.ExecuteSqlUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/19.
 */
@RestController
@RequestMapping("/reportServer/uploadFile")
public class UploadController  extends RO {
    private static Logger log = Logger.getLogger(UploadController.class);

    @Autowired
    private UploadService uploadService;

    @RequestMapping(value = "/uploadFile", produces = "text/html;charset=UTF-8")
    public String uplaod(HttpServletRequest req, @RequestParam("file") MultipartFile file) {//1. 接受上传的文件  @RequestParam("file") MultipartFile file
            // 判断文件是否为空
            if (file.isEmpty()) {
                return ExceptionMsg("文件不能为空");
            }
            String pathname=null;
            try {
                pathname= uploadService.upload(file);
            } catch (Exception e) {
                ExceptionMsg(e.getMessage());
            }
            return SuccessMsg("",pathname);
    }
    @RequestMapping(value = "/getAll", produces = "text/plain;charset=UTF-8")
    public String getAll(@RequestBody String pjson) {
        try{
            JSONObject obj= JSON.parseObject(pjson);
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
                 aResult = DbFactory.Open(DbFactory.FORM).selectList("upload.getAll", map,bounds);
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
            maps.put("total",totalSize);
            return SuccessMsg("",maps);
        }catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }
    @RequestMapping(value = "/deleteUpload", produces = "text/plain;charset=UTF-8")
    public String deleteUpload(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            JSONObject json = JSON.parseObject(pJson);
            Map<String,Object> map = new HashMap<>();
            map.put("id",json.getIntValue("id"));
            Map m=DbFactory.Open(DbFactory.FORM).selectOne("upload.getById",map);
            String filepath=m.get("filepath").toString();
            File file = new File(filepath);
            if (file.exists()) {
                if (file.delete()) {
                    DbFactory.Open(DbFactory.FORM).delete("upload.deleteUpload",map);
                    return SuccessMsg("","删除数据成功");
                } else {
                    return ExceptionMsg("删除失败！");
                }
            } else {
                return ExceptionMsg("文件不存在！");
            }
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }
    @RequestMapping(value = "/downloadFile")
    public void downloadFile(@RequestBody String filePath, HttpServletResponse response) throws Exception {
        try {
            uploadService.downloadFile(filePath, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}