package root.form.constant;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ColumnType {
    private static class Tuple{
        private String jdbcType;
        private String dbType;

        private Tuple() {
        }

        private Tuple(String jdbcType, String dbType) {
            this.jdbcType = jdbcType;
            this.dbType = dbType;
        }
    }

    private static final Map<String, Tuple> columnType = new LinkedHashMap<>(3);

    public static String STRING = "字符串";
    public static String NUMBER = "数字";
    public static String DATE = "日期";

    //初始化所有数据类型
    static{
        columnType.put(STRING,new Tuple("VARCHAR","varchar"));
        columnType.put(NUMBER,new Tuple("DOUBLE","double"));
        columnType.put(DATE,new Tuple("TIMESTAMP","datetime"));
    }

    public static String getJdbcType(String userType){
        Tuple tuple = columnType.get(userType);
        if(tuple == null) return null;
        return tuple.jdbcType;
    }

    public static String getDbType(String userType){
        Tuple tuple = columnType.get(userType);
        if(tuple == null) return userType;
        return tuple.dbType;
    }

    public static Set<String> listAllColumnTypes(){
        return columnType.keySet();
    }
}
