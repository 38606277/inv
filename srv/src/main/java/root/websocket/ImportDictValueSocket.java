package root.websocket;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import root.report.db.DbFactory;
import root.report.service.DictService;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Date: 2018/10/25 10:11
 * @Description: 注入dictSocket接口
 */
@ServerEndpoint(value = "/websocket/dictSocket")
@Component
public class ImportDictValueSocket {

    private static Logger log = Logger.getLogger(ImportDictValueSocket.class);

    private DictService dictService;
    //此处是解决无法注入的关键
    private static ApplicationContext applicationContext;
    public static void setApplicationContext(ApplicationContext applicationContext) {
        ImportDictValueSocket.applicationContext = applicationContext;
    }

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<ImportDictValueSocket> webSocketSet = new CopyOnWriteArraySet<ImportDictValueSocket>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        /*System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
        try {
            sendMessage("当前在线人数"+getOnlineCount()+",当前sessionID:"+this.session.getId());
        } catch (IOException e) {
            System.out.println("IO异常");
        }*/
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
        log.info("来自客户端的消息:" + message);
        // 调用  TestTwo的 导入方法，一旦连接成功 ， 则 持续通信
        try {
            dictService = applicationContext.getBean(DictService.class);
            String result = dictService.importFuncDictValueByDictId(session,Integer.parseInt(message));   // 需要加锁与否？
            synchronized (result){
                log.info("结果为"+result);
                sendMessage(result);   // 返回  "over"
                session.close();  // 关闭掉
            }
        }catch (Exception e){
            log.error(e.getMessage());
            sendMessage(e.getMessage());
        }

        //群发消息
      /*  for (ImportDictValueSocket item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    /**
     * 发生错误时调用
     @OnError
     public void onError(Session session, Throwable error) {
     System.out.println("发生错误");
     error.printStackTrace();
     }  **/


     public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
     }

     /**
      * 群发自定义消息
      * */
    public static void sendInfo(String message) throws IOException {
        for (ImportDictValueSocket item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        ImportDictValueSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        ImportDictValueSocket.onlineCount--;
    }
}
