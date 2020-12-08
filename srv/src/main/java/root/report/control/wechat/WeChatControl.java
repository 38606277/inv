package  root.report.control.wechat;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import root.report.service.WeChatService;
import root.report.service.webchat.WeChatUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class WeChatControl{
    @Autowired
    private WeChatService weChatService;


    /**
     * 处理微信服务器发来的get请求，进行签名的验证
     *
     * signature 微信端发来的签名
     * timestamp 微信端发来的时间戳
     * nonce     微信端发来的随机字符串
     * echostr   微信端发来的验证字符串
     */
    @RequestMapping(value = "wechat",method=RequestMethod.GET)
    public void validate(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("success");
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostr = request.getParameter("echostr");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            if (WeChatUtil.checkSignature(signature, timestamp, nonce)) {
                out.write(echostr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
    /**
     * 此处是处理微信服务器的消息转发的
     */
    @RequestMapping(value = "wechat",method=RequestMethod.POST)
    public void processMsg(HttpServletRequest request,HttpServletResponse response) {
        // 调用核心服务类接收处理请求
        response.setCharacterEncoding("utf-8");
        PrintWriter out = null;
        try {
            String message= weChatService.processRequest(request);
            System.out.println(message);
            out = response.getWriter();
            out.write(message);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        out.close();


    }
}