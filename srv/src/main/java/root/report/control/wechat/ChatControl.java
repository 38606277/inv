package root.report.control.wechat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.service.webchat.ChatService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/reportServer/chat")
public class ChatControl extends RO {

    private static Logger log = Logger.getLogger(ChatControl.class);

    @Autowired
    private ChatService chatService;

    @RequestMapping(value = "/getChatByuserID", produces = "text/plain;charset=UTF-8")
    public String getFunctionByID(@RequestBody String pJson) {
        Map<String,Object> result = new HashMap();
        try {
            JSONObject obj=JSON.parseObject(pJson);
            List<Map<String,Object>> aResult = null;
            Long totalSize = 0L;
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
            map.put("from_userId",obj.get("from_userId"));
            map.put("to_userId",obj.get("to_userId"));
            aResult = DbFactory.Open(DbFactory.FORM).selectList("chat.getchatInfoByID", map,bounds);
//            Collections.sort(aResult, new Comparator<Map<String, Object>>() {
//                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
//                    Date name1 = (Date) o1.get("message_time");//name1是从你list里面拿出来的一个
//                    Date name2 = (Date) o2.get("message_time"); //name1是从你list里面拿出来的第二个name
//                    return name1.compareTo(name2);
//                }
//            });
            totalSize = ((PageRowBounds)bounds).getTotal();
            Map maps=new HashMap<>();
            maps.put("data",aResult);
            maps.put("totald",totalSize);
            //            List<Map<String, String>> list  = dictService.getDictValueByID(dict_id);
            return JSON.toJSONString(maps);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }


    @RequestMapping(value = "/createChat", produces = "text/plain;charset=UTF-8")
    public String createChat(@RequestBody String pJson) throws Exception, DocumentException, SAXException, SQLException {
            SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            String func_id =chatService.saveMessage(sqlSession,pJson);
            return SuccessMsg("新增报表成功",func_id);
        }catch (Exception ex){
            ex.printStackTrace();
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/deleteChat", produces = "text/plain;charset=UTF-8")
    public String deleteFunction(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject obj =  JSON.parseObject(pJson);
            String namespace = "";
           chatService.deleteChat(sqlSession,obj.getInteger("message_id"));
            sqlSession.getConnection().commit();
            return SuccessMsg("删除报表成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            return ExceptionMsg(ex.getMessage());
        }
    }

}
