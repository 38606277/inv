package root.report.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @Auther: pccw
 * @Date: 2018/11/5 15:34
 * @Description:
 */
public class DictInsertTask implements Runnable {

    private static Logger log = Logger.getLogger(DictInsertTask.class);

    // 通过构造函数 对下序值进行赋值
    private StringBuffer stringBuffer = new StringBuffer();     //  拼装的SQL
    private List<Map> mapList;              //  遍历的List
    private String dictId;                  //  dict_id值
    private PreparedStatement pre;          //   数据库preStateMent
    private String prefix;                 //   拼装SQL的前缀

    public DictInsertTask(List<Map> paramMapList,String dict_id,PreparedStatement paramPre,String paramPrefix){
        this.mapList = paramMapList;
        this.dictId = dict_id;
        this.pre = paramPre;
        this.prefix = paramPrefix;
    }

    @Override
    public void run() {
            String name = Thread.currentThread().getName();
            long threadId = Thread.currentThread().getId();
            log.info("thread name: "+name+",id为"+threadId+"执行了一次");
            for (Map tempMap : mapList) {
                // 构建SQL后缀
                stringBuffer.append("(" +dictId+",'"+ tempMap.get("code") + "'," + "'" + tempMap.get("name") + "'),");
            }
            String sql = prefix + stringBuffer.substring(0, stringBuffer.length() - 1);  // 构建完整SQL
            try {
                pre.addBatch(sql);   // 添加执行SQL
                pre.executeBatch();  // 执行操作
                log.debug("---执行完一段sql批量插入语句");
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
}
