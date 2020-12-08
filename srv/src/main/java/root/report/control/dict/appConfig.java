package root.report.control.dict;

import org.apache.cxf.annotations.DataBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;

@RestController
@RequestMapping("/reportServer/appConfig")
//@Configuration
//@PropertySource(value="file:config/application.properties",encoding = "utf-8")
public class appConfig extends  RO {

    @Value("${webapp.title}")
    private  String title;
    //查询WebApp的title
    @RequestMapping(value = "/getWebAppTitle", produces = "text/plain;charset=UTF-8")
    public String getWebAppTitle() {


        try {
            return SuccessMsg("", title);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }


}


