package root.report.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportPackage
{
    /**
     * 用来存放不同输出类别的相关信息,其中column为输出字段
     * eg:{
     *       first:{namespace:'',sqlid:'',id:['','']},
     *       row:{namespace:'',sqlid:'',id:['','']}
     *    };
     */
    private Map<String, Object> out = new HashMap<String, Object>();
    /**
     * 存储excel中带输入参数单元格相关属性
     * eg:{
     *       namespace:'',sqlid:'',blankRowNum:'',blankColNum:'',id:'',name:'',
     *       db:'','link.dest':'','link.param':''
     *    }
     */
    private List<Map<String, Object>> inCell;
    /**
     * 存储excel中带输出参数单元格相关属性
     * 同上
     */
    private List<Map<String, Object>> outCell;
    
    public Map<String, Object> getOut()
    {
        return out;
    }
    public void setOut(Map<String, Object> out)
    {
        this.out = out;
    }
    public List<Map<String, Object>> getInCell()
    {
        return inCell;
    }
    public void setInCell(List<Map<String, Object>> inCell)
    {
        this.inCell = inCell;
    }
    public List<Map<String, Object>> getOutCell()
    {
        return outCell;
    }
    public void setOutCell(List<Map<String, Object>> outCell)
    {
        this.outCell = outCell;
    }
   
}
