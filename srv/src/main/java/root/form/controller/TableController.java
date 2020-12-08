package root.form.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;

@RestController
@RequestMapping(value = "/reportServer/Table")
public class TableController extends RO {


    //通过模板创建表
    @RequestMapping(value = "/CreateTable")
    public String CreateTable() {

        //保存到表定义

        //创建物理表

        return SuccessMsg("", "");

    }

    //修改模板和表的对应
    @RequestMapping(value = "/UpdateTable")
    public String UpdateTable() {


        return SuccessMsg("", "");

    }

    //删除表
    @RequestMapping(value = "/DeleteTable")
    public String DeleteTable() {


        return SuccessMsg("", "");

    }

    //保存数据
    public String SaveData() {


        return SuccessMsg("", "");
    }


    //查询当前模板下的数据
    @RequestMapping(value = "/getData/{templateID}")
    public String GetData(String templateID) {

        //根据模板找到对应的表名

        //根据模板ID找到表中的数据，返回所有字段

        return SuccessMsg("", "");

    }

    //查询模板和任务下的数据
    @RequestMapping(value = "/getData/{templateID}/{taskID}")
    public String GetData(String templateID, String taskID) {


        return SuccessMsg("", "");

    }

    //查询模板-》任务-》用户下的数据
    @RequestMapping(value = "/getData/{templateID}/{taskID}/{userID}")
    public String GetData(String templateID, String taskID, String userID) {


        return SuccessMsg("", "");

    }


}
