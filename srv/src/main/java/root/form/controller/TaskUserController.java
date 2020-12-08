package root.form.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;

@RestController
public class TaskUserController extends RO {


    //创建的用户
    public String CreateTaskUser() {

        return SuccessMsg("", "");
    }

    //删除任务下的用户
    public String DeleteTaskUser() {

        return SuccessMsg("", "");
    }

    //获取当前任务下的用户
    public String GetTaskUser() {

        return SuccessMsg("", "");
    }


}
