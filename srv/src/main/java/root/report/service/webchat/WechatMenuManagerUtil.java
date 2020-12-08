package root.report.service.webchat;


import root.report.service.webchat.Button;
import root.report.service.webchat.ClickButton;
import root.report.service.webchat.Menu;
import root.report.service.webchat.ViewButton;

/**
 * @Description: 菜单管理器类
 * @Parameters:
 * @Return:
 * @Create Date: 2018年9月28日下午4:33:24
 * @Version: V1.00
 * @author: 来日可期
 */
public class WechatMenuManagerUtil {
    /**
     * @Description: 定义菜单结构
     * @Parameters:
     * @Return:
     * @Create Date: 2018年9月28日下午5:36:08
     * @Version: V1.00
     * @author: 来日可期
     */
    public Menu getMenu(){

        ViewButton firstViewButton = new ViewButton();
        firstViewButton.setName("自助查询");
        firstViewButton.setType("view");
        firstViewButton.setUrl("http://132.232.111.110/app/index.html");

        ClickButton firstClickButton = new ClickButton();
        firstClickButton.setName("联系我们");
        firstClickButton.setKey("function");
        firstClickButton.setType("click");



        Menu menu = new Menu();
        Button[] boButtons = {firstViewButton,firstClickButton,};
        menu.setButton(boButtons);

        return menu;
    }
}

