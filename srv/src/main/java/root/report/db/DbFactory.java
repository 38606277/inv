package root.report.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInterceptor;
import org.apache.ibatis.logging.log4j.Log4jImpl;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import root.configure.AppConstants;
import root.report.util.ErpUtil;

import java.io.File;
import java.util.*;

public class DbFactory {
    private static final Logger log = Logger.getLogger(DbManager.class);
    public static final String SYSTEM = "system";
    public static final String FORM = "form";
    public static final String BUDGET = "budget";
    private static Map<String, SqlSessionFactory> mapFactory = new HashMap<String, SqlSessionFactory>();
    private static Map<String, ThreadLocal<SqlSession>> map = new HashMap<String, ThreadLocal<SqlSession>>();
    private static ErpUtil erpUtil = new ErpUtil();
    private static DbManager manager = new DbManager();

    // 初始化
    public static void init(String dbName) {
        long t1 = System.nanoTime();
        try {
            JSONObject dbJson = JSONObject.parseObject(manager.getDBConnectionByName(dbName));
            if (dbJson.size() == 0) {
                return;
            }
            String dbtype = dbJson.getString("dbtype");
            SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();

            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUsername(dbJson.getString("username"));
            dataSource.setPassword(erpUtil.decode(dbJson.getString("password")));
            dataSource.setDriverClassName(dbJson.getString("driver"));
            if ("Mysql".equals(dbtype)) {
                dataSource.setUrl(dbJson.getString("url")+"?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&useUnicode=true&autoReconnect=true");
//                dataSource.setUrl(dbJson.getString("url") + "?serverTimezone=Asia/Shanghai&useSSL=true&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&rewriteBatchedStatements=true");


//                dataSource.setUrl(dbJson.getString("url") + "?serverTimezone=Asia/Shanghai&useSSL=true&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&rewriteBatchedStatements=true");
            } else {
                dataSource.setUrl(dbJson.getString("url"));
            }
            dataSource.setMaxWait(10000);//设置连接超时时间10秒
            dataSource.setMaxActive(Integer.valueOf(dbJson.getString("maxPoolSize")));
            dataSource.setInitialSize(Integer.valueOf(dbJson.getString("minPoolSize")));
            dataSource.setTimeBetweenEvictionRunsMillis(60000);//检测数据源空连接间隔时间
            dataSource.setMinEvictableIdleTimeMillis(300000);//连接空闲时间
            dataSource.setTestWhileIdle(true);
            dataSource.setTestOnBorrow(true);
//            if ("Oracle".equals(dbtype)) {
//                dataSource.setPoolPreparedStatements(true);
//            }
//            if ("DB2".equals(dbtype)) {
//                dataSource.setValidationQuery("select 'x' from sysibm.sysdummy1");
//            } else {
//                dataSource.setValidationQuery("select 'x' from dual");
//            }
//            dataSource.setFilters("stat");
//	        List<Filter> filters = new ArrayList<Filter>();
//	        filters.add(new SqlFilter());
//	        dataSource.setProxyFilters(filters);
            dataSource.init();

            //填充数据源
            factoryBean.setDataSource(dataSource);

            //填充SQL文件
            factoryBean.setMapperLocations(getMapLocations(dbtype, dbName));
            Configuration configuration = new Configuration();
            configuration.setCallSettersOnNulls(true);
            //启动SQL日志
            configuration.setLogImpl(Log4jImpl.class);
            configuration.getTypeHandlerRegistry().register(GregorianCalendarTypeHandle.class);
            factoryBean.setConfiguration(configuration);
            factoryBean.setPlugins(getMybatisPlugins(dbtype));
            mapFactory.put(dbJson.getString("name"), factoryBean.getObject());
            long t2 = System.nanoTime();
            log.info("初始化数据库【" + dbName + "】耗时" + String.format("%.4fs", (t2 - t1) * 1e-9));
        } catch (Exception e) {
            log.error("初始化数据库【" + dbName + "】失败!");
            e.printStackTrace();
        }
    }

    public static SqlSession Open(String dbName) {
        return Open(true, dbName);
    }

    // 获取一个session
    public static SqlSession Open(boolean autoCommit, String dbName) {
        ThreadLocal<SqlSession> mybatisTl = map.get(dbName);
        if (mybatisTl == null) {
            SqlSessionFactory factory = mapFactory.get(dbName);
            if (factory == null) {
                init(dbName);
            }
            mybatisTl = new ThreadLocal<SqlSession>();
            mybatisTl.set(mapFactory.get(dbName).openSession(autoCommit));
            map.put(dbName, mybatisTl);
        } else {
            if (mybatisTl.get() == null) {
                mybatisTl.set(mapFactory.get(dbName).openSession(autoCommit));
            }
        }

        return mybatisTl.get();
    }

    // 关闭session
    public static void close(String dbName) {
        ThreadLocal<SqlSession> mybatisTl = map.get(dbName);
        if (mybatisTl != null) {
            SqlSession session = mybatisTl.get();
            if (session != null) {
                session.close();
                mybatisTl.set(null);
            }
        }
    }

    // 回滚
    public static void rollback(String dbName) {
        ThreadLocal<SqlSession> mybatisTl = map.get(dbName);
        if (mybatisTl != null) {
            SqlSession session = mybatisTl.get();
            if (session != null) {
                session.rollback();
            }
        }
    }

    // 提交
    public static void commit(String dbName) {
        ThreadLocal<SqlSession> mybatisTl = map.get(dbName);
        if (mybatisTl != null) {
            SqlSession session = mybatisTl.get();
            if (session != null) {
                session.commit();
            }
        }
    }

    private static Resource[] getMapLocations(String dbType, String dbName) throws Exception {
        String[] locations = new String[4];
        locations[0] = AppConstants.getUserSqlPath();
        locations[1] = AppConstants.getUserFunctionPath();
        locations[2] = AppConstants.getUserDictionaryPath();
        locations[3] = "classpath:mapper/**/*.xml";
        List<Resource> resources = new ArrayList<Resource>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for(String location : locations){
            if (location.startsWith("classpath")) {
                resources.addAll(Arrays.asList(resolver.getResources(location)));
            }else{
                List<File> fileList = getFileList(location, new ArrayList<File>());
                for (int i = 0; fileList != null && i < fileList.size(); i++) {
                    resources.add(new FileSystemResource(fileList.get(i)));
                }
            }
        }
        Resource[] a = new Resource[resources.size()];
        return resources.toArray(a);
    }

    private static List<File> getFileList(String path, List<File> list){
        File[] fileList = new File(path).listFiles();
        if(fileList!=null){
            for(File file:fileList){
                if(file.getName().endsWith(".xml")){
                    list.add(file);
                }else if(file.isDirectory()){
                    getFileList(file.getAbsolutePath(),list);
                }
            }
        }

        return list;
    }

    private static Interceptor[] getMybatisPlugins(String dbType) {
        Interceptor[] ins = new Interceptor[1];
        PageInterceptor pageInceptor = new PageInterceptor();
        Properties pro = new Properties();
        if ("Oracle".equals(dbType)) {
            pro.setProperty("helperDialect", "oracle");
        } else if ("Mysql".equals(dbType)) {
            pro.setProperty("helperDialect", "mysql");
        } else if ("DB2".equals(dbType)) {
            pro.setProperty("helperDialect", "db2");
        }
        pro.setProperty("rowBoundsWithCount", "true");
        pageInceptor.setProperties(pro);
        ins[0] = pageInceptor;
        return ins;
    }

    public static void initializeDB(String dbName) {
        map.remove(dbName);
        mapFactory.remove(dbName);
        init(dbName);
    }
}
