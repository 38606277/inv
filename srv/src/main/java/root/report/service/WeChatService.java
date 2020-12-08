package root.report.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import root.report.service.webchat.WeChatContant;
import root.report.service.webchat.WeChatUtil;
import root.report.util.ArticleItem;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 核心服务类
 */
@Service
public class WeChatService {

    @Autowired
    NLPService nlpService;

    public String processRequest(HttpServletRequest request) {
        // xml格式的消息数据
        String respXml = null;
        // 默认返回的文本消息内容
        String respContent;
        try {
            // 调用parseXml方法解析请求消息
            Map<String,String> requestMap = WeChatUtil.parseXml(request);
            // 消息类型
            String msgType = (String) requestMap.get(WeChatContant.MsgType);
            String mes = null;
            // 文本消息
            if (msgType.equals(WeChatContant.REQ_MESSAGE_TYPE_TEXT)) {
                mes =requestMap.get(WeChatContant.Content).toString();
                if(mes!=null){

//                    String resultMsg
                    Map map=nlpService.ExecNLP(mes);
                    List<Map<String, Object>> list=(List)map.get("list");
                    StringBuilder sb=new StringBuilder();
                    for (int i=0;i<list.size();i++)
                    {
                        sb.append(list.get(i).get("VENDOR_NAME")+"\n");

                    }
//                    respXml = WeChatUtil.sendTextMsg(requestMap, sb.toString());
                    List<ArticleItem> items = new ArrayList<>();
                    ArticleItem item = new ArticleItem();
                    item.setTitle("为您找到以下数据：");

//                    String a="<p><a>www.baidu.com</a></p>";
                    item.setDescription(sb.toString());
//                    item.setPicUrl("http://changhaiwx.pagekite.me/photo-wall/a/iali11.jpg");
                    item.setUrl("http://www.baidu.com");
                    items.add(item);


                    respXml = WeChatUtil.sendArticleMsg(requestMap, items);
                }else if("我的信息".equals(mes)){
//                    Map<String, String> userInfo = getUserInfo(requestMap.get(WeChatContant.FromUserName));
//                    System.out.println(userInfo.toString());
//                    String nickname = userInfo.get("nickname");
//                    String city = userInfo.get("city");
//                    String province = userInfo.get("province");
//                    String country = userInfo.get("country");
//                    String headimgurl = userInfo.get("headimgurl");
//                    List<ArticleItem> items = new ArrayList<>();
//                    ArticleItem item = new ArticleItem();
//                    item.setTitle("你的信息");
//                    item.setDescription("昵称:"+nickname+"  地址:"+country+" "+province+" "+city);
//                    item.setPicUrl(headimgurl);
//                    item.setUrl("http://www.baidu.com");
//                    items.add(item);
//
//                    respXml = WeChatUtil.sendArticleMsg(requestMap, items);
                }
            }
            // 图片消息
            else if (msgType.equals(WeChatContant.REQ_MESSAGE_TYPE_IMAGE)) {
                respContent = "您发送的是图片消息！";
                respXml = WeChatUtil.sendTextMsg(requestMap, respContent);
            }
            // 语音消息
            else if (msgType.equals(WeChatContant.REQ_MESSAGE_TYPE_VOICE)) {
                respContent = "您发送的是语音消息！";
                respXml = WeChatUtil.sendTextMsg(requestMap, respContent);
            }
            // 视频消息
            else if (msgType.equals(WeChatContant.REQ_MESSAGE_TYPE_VIDEO)) {
                respContent = "您发送的是视频消息！";
                respXml = WeChatUtil.sendTextMsg(requestMap, respContent);
            }
            // 地理位置消息
            else if (msgType.equals(WeChatContant.REQ_MESSAGE_TYPE_LOCATION)) {
                respContent = "您发送的是地理位置消息！";
                respXml = WeChatUtil.sendTextMsg(requestMap, respContent);
            }
            // 链接消息
            else if (msgType.equals(WeChatContant.REQ_MESSAGE_TYPE_LINK)) {
                respContent = "您发送的是链接消息！";
                respXml = WeChatUtil.sendTextMsg(requestMap, respContent);
            }
            // 事件推送
            else if (msgType.equals(WeChatContant.REQ_MESSAGE_TYPE_EVENT)) {
                // 事件类型
                String eventType = (String) requestMap.get(WeChatContant.Event);
                // 关注
                if (eventType.equals(WeChatContant.EVENT_TYPE_SUBSCRIBE)) {
                    respContent = "谢谢您的关注！";
                    respXml = WeChatUtil.sendTextMsg(requestMap, respContent);
                }
                // 取消关注
                else if (eventType.equals(WeChatContant.EVENT_TYPE_UNSUBSCRIBE)) {
                    // TODO 取消订阅后用户不会再收到公众账号发送的消息，因此不需要回复
                }
                // 扫描带参数二维码
                else if (eventType.equals(WeChatContant.EVENT_TYPE_SCAN)) {
                    // TODO 处理扫描带参数二维码事件
                }
                // 上报地理位置
                else if (eventType.equals(WeChatContant.EVENT_TYPE_LOCATION)) {
                    // TODO 处理上报地理位置事件
                }
                // 自定义菜单
                else if (eventType.equals(WeChatContant.EVENT_TYPE_CLICK)) {
                    // TODO 处理菜单点击事件
                }
            }
            mes = mes == null ? "不知道你在干嘛" : mes;
            if(respXml == null)
                respXml = WeChatUtil.sendTextMsg(requestMap, mes);
            System.out.println(respXml);
            return respXml;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }
}