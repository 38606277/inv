package root.report.service.webchat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import root.report.db.DbFactory;
import root.report.util.JsonUtil;
import java.util.*;

@Service
public class ChatService {

    private static Logger log = Logger.getLogger(ChatService.class);
    /**
     * 功能描述: 根据  class_id 查询出 func_name 表当中的信息
     */
    public String  saveMessage(SqlSession sqlSession,String json) throws SAXException, DocumentException {
        JSONObject obj=JSON.parseObject(json);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("from_userId", obj.getInteger("from_userId"));
        param.put("to_userId", obj.getInteger("to_userId"));
        param.put("post_message", obj.getString("post_message"));
        param.put("message_time",null);
        param.put("message_type", obj.getString("message_type"));
        param.put("message_state", obj.getString("message_state"));
        sqlSession.insert("chat.save",param);
        return param.get("message_id").toString();
    }

    /**
     *
     * 功能描述: 针对传递进来的JSONAarray进行批量删除func_in数据
     */
    public void deleteChat(SqlSession sqlSession,int id) {
        sqlSession.delete("chat.deleteById",id);
    }

    /**
     * 功能描述: 根据map结构删除func_in表的记录
     */
    public int deleteChat(SqlSession sqlSession,Map map) {
        return sqlSession.delete("chat.deleteById", map);
    }


}
