package root.fnd.control;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.fnd.entity.TreeNode;
import root.fnd.service.TreeBuilder;
import root.report.common.RO;
import root.report.db.DbFactory;

import java.util.List;
import java.util.Map;

@RestController
public class FlexValue extends RO {

    @RequestMapping(value = "/reportServer/FlexValue/getFlexValuesTree", produces = "text/plain;charset=UTF-8")
    public String getFlexValuesTree(@RequestBody JSONObject pJson)  {
        List<TreeNode> treeNodeList = DbFactory.Open(DbFactory.FORM).selectList("flexValue.getFlexValuesTree",pJson);
        List<TreeNode>  result= new TreeBuilder().buildTree(treeNodeList);
        return SuccessMsg("",result);

    }

    @RequestMapping(value = "/getFlexValuesList", produces = "text/plain;charset=UTF-8")
    public String getFlexValuesList(@RequestBody JSONObject pJson)  {
        List<TreeNode> treeNodeList = DbFactory.Open(DbFactory.FORM).selectList("flexValue.getFlexValuesTree",pJson);
        List<TreeNode>  result= new TreeBuilder().buildTree(treeNodeList);
        return SuccessMsg("",result);

    }
    @RequestMapping(value = "/reportServer/hive/get", produces = "text/plain;charset=UTF-8")
    public String get(@RequestBody JSONObject pJson)  {
        List<Map> result = DbFactory.Open("hive").selectList("flexValue.get",pJson);
        return SuccessMsg("",result);

    }

}
