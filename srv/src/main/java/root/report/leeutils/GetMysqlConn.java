package root.report.leeutils;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.db.ds.simple.SimpleDataSource;
import cn.hutool.setting.Setting;
import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.text.StringSubstitutor;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author
 */
public class GetMysqlConn {
    /**
     * 获取连接
     * @param dbConnName
     * @return
     * @throws SQLException
     */
    public static DataSource getConn(final String dbConnName) throws SQLException {
        //具体的配置参数请参阅Druid官方文档
        final Map<String, String> configMap = readString("getMysqlConn", dbConnName);
        return new SimpleDataSource(configMap.get("url"),configMap.get("username"),configMap.get("password"));
    }


    public static DataSource getConn(final String sourceName, final String dbConnName) throws SQLException {
        //具体的配置参数请参阅Druid官方文档
        DruidDataSource ds2 = new DruidDataSource();
        final Map<String, String> configMap = readString(sourceName, dbConnName);
        ds2.setUrl(configMap.get("url"));
        ds2.setUsername(configMap.get("username"));
        ds2.setPassword(configMap.get("password"));

        return ds2;
    }


    /**
     * 获取配置文件参数
     * @param group
     * @param database
     * @return
     */
    public static Map<String,String> readString(final String group,final String database) {
        Setting setting = new Setting("config/db.setting", CharsetUtil.CHARSET_UTF_8, true);
        Map<String,String> databaseMap=new HashMap<>(16);
        databaseMap.put("database",database);
        StringSubstitutor sub = new StringSubstitutor(databaseMap);
        final Setting settingConfig = setting.getSetting(group);
        final Map<String,String> map = new TreeMap<String,String>();
        for(String key:settingConfig.keySet()) {
            map.put(key, sub.replace(settingConfig.getStr(key)));
        }
        return map;
    }
}
