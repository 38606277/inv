package root.form.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;

@RestController
public class TaskController extends RO {

    //创建模板任务
    public String CreateTask() {

        //写任务表

        //写任务用户表，处理事务


        return SuccessMsg("", "");
    }

    //修改模板任务
    public String UpdateTask() {

        return SuccessMsg("", "");
    }

    //修改模板任务
    public String DeleteTask() {

        return SuccessMsg("", "");
    }


    //修改模板任务
    @RequestMapping(value = "/UpdateTaskState/{templateID}")
    public String UpdateTaskState() {

        return SuccessMsg("", "");
    }


    //获取当前模板下的任务
    @RequestMapping(value = "/getTask/{templateID}")
    public String GetTask() {

        return SuccessMsg("", "");
    }


    //获取待办任务
    public String GetUnTask() {

        return SuccessMsg("", "");
    }

    //提交任务
    public String CommitTask() {

        return SuccessMsg("", "");
    }

}
