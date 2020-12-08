package root.report.util;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageRowBounds;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.cache.decorators.TransactionalCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.type.TypeHandler;
import org.apache.log4j.Logger;
import org.apache.poi.ss.formula.functions.T;
import org.mybatis.caches.ehcache.LoggingEhcache;
import root.report.db.DbFactory;
import root.report.util.cache.EhcacheManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * @Auther: pccw
 * @Date: 2018/11/9 16:14
 * @Description:
 */
public class ExecuteSqlUtil {

    private static Logger log = Logger.getLogger(ExecuteSqlUtil.class);

    private final static TransactionalCacheManager tcm = new TransactionalCacheManager();

    /**
     *
     * 功能描述:
     *      对符合 mybatis.dtd形式的sql进行动态sql解析并执行，返回Map结构的数据集
     * @param: executeSql 要执行的sql ， sqlSession 数据库会话，namespace 命名空间，mapper_id mapper的ID，bounds 分页参数 ,statementType statement的类型
     * @auther:
     * @date: 2018/11/9 16:17
     */
    public static List<?> executeDataBaseSql(String executeSql, SqlSession sqlSession, String namespace, String mapper_id, RowBounds bounds,
                                             Class<?> inClazz,Class<?> outClazz,Object param,StatementType statementType,Boolean cacheFlag)
    {
       return executeDataBaseSql(executeSql,sqlSession,namespace,mapper_id,bounds,
               inClazz,outClazz,param,statementType,cacheFlag,null,null);
    }
    public static List<?> executeDataBaseSql(String executeSql, SqlSession sqlSession, String namespace, String mapper_id, RowBounds bounds,
                                               Class<?> inClazz,Class<?> outClazz,Object param,StatementType statementType,Boolean cacheFlag,String dbType,String qryCursorName){
        if(statementType==null){
            statementType = StatementType.PREPARED; // 默认为 prepared
        }
        if(cacheFlag==null){
            cacheFlag = false;  // 默认为false 默认不开启缓存
        }

        List<?> list = null;
        List<?> cacheList = null;
        CacheKey cacheKey = null;

        if(bounds==null){
            bounds = new RowBounds();
        }
        //如果是存储过程就不要传分页参数bounds
        if(statementType.equals(StatementType.CALLABLE)){
            bounds = null;
        }
        // 1. 对executeSql 加上script标签
        StringBuffer sb = new StringBuffer();
        sb.append("<script>");
        sb.append(executeSql);
        sb.append("</script>");
        log.info("转换后的sql为:->"+sb.toString());
        Configuration configuration = sqlSession.getConfiguration();
        configuration.setCacheEnabled(true);  // 开启二级缓存?

        LanguageDriver languageDriver = configuration.getDefaultScriptingLanguageInstance();  // 2. languageDriver 是帮助我们实现dynamicSQL的关键
        SqlSource sqlSource = languageDriver.createSqlSource(configuration,sb.toString(),inClazz);  //  泛型化入参
      //  configuration.getCaches().forEach(e -> System.out.print(e.getId()));
        MappedStatement ms = null;

        // 如果我们从 configuration 当中可以取得到的话，则看缓存当中是否存在
        if(configuration.getMappedStatementNames().contains(namespace+"."+mapper_id) && cacheFlag){
            ms = configuration.getMappedStatement(namespace+"."+mapper_id);
        }else {
            log.info("======不存在此mappedStatment,可以构建=====");
        }
        if(ms == null){
            // 构建ms，这个时候 configuration 当中是一定存在ms了
            ms =  newSelectMappedStatement(configuration,namespace+"."+mapper_id,sqlSource,outClazz,statementType,cacheFlag,dbType);
        }

        if(!cacheFlag){
            // 如果不需要缓存  那么直接查询就行 ,并且也不需要装入到缓存当中去
            if(bounds!=null){
                list = sqlSession.selectList(namespace+"."+mapper_id,param,bounds);
                log.info("执行了一次查询");
            }else {
                if(StatementType.CALLABLE.equals(statementType) && dbType.equals("bkeam")) {
                    sqlSession.select(namespace + "." + mapper_id, param, null);
                    Map map = (Map) param;
                    list = (List<Map<String, Object>>) map.get(qryCursorName);
                }else{
                    list = sqlSession.selectList(namespace+"."+mapper_id,param);
                }
                log.info("执行了一次查询");
            }
            return list;
        }else {
            // 组装cache
            cacheKey = sqlSession.getConfiguration().newExecutor(new Transaction() {
                @Override
                public Connection getConnection() throws SQLException {
                    return this.getConnection();
                }
                @Override
                public void commit() throws SQLException {
                    this.getConnection().commit();
                }
                @Override
                public void rollback() throws SQLException {
                    this.getConnection().rollback();
                }
                @Override
                public void close() throws SQLException {
                    this.getConnection().close();
                }
                @Override
                public Integer getTimeout() throws SQLException {
                    return 5000;
                }
            }, ExecutorType.SIMPLE).createCacheKey(ms,param,bounds,ms.getBoundSql(param));

            // 从 ehcache 当中去缓存的值，如果存在则返回不存在 则 查询并装入到缓存
            Element ehcacheElement  = EhcacheManager.getCache().get(cacheKey.toString());
            if(ehcacheElement!=null && ehcacheElement.getObjectValue()!=null){
                cacheList = (List<?>) ehcacheElement.getObjectValue();   // 强转
                log.info("cache hit  缓存命中,命中率为:");
                Element ehcacheElementTotal =  EhcacheManager.getCache().get(cacheKey.toString()+":totalSize");
                if(bounds instanceof  PageRowBounds && ehcacheElementTotal!=null){
                    ((PageRowBounds) bounds).setTotal((Long) ehcacheElementTotal.getObjectValue());
                }
                return cacheList;
            }else {
                if(bounds!=null){
                    list = sqlSession.selectList(namespace+"."+mapper_id,param,bounds);
                    log.info("执行了一次查询,并把结果集装入到缓存当中");
                }else {
                    if(StatementType.CALLABLE.equals(statementType) && dbType.equals("bkeam")) {
                        sqlSession.select(namespace + "." + mapper_id, param, null);
                        Map map = (Map) param;
                        list = (List<Map<String, Object>>) map.get(qryCursorName);
                    }else{
                        list = sqlSession.selectList(namespace + "." + mapper_id, param);
                    }
                    log.info("执行了一次查询,并把结果集装入到缓存当中");
                }
                // 设置缓存分页对象的 total 数量
                Long totalResult = 0L;
                if(bounds instanceof PageRowBounds){
                    totalResult = ((PageRowBounds) bounds).getTotal();
                }
                // 装入缓存
               /* if(cacheKey!=null){
                    tcm.putObject(ms.getCache(),cacheKey,list);
                }*/
                log.info("#############=>测试cacheKey呗重写了没有"+cacheKey.toString());
                // VERSION 3 切到 ehcache 缓存当中 ，cacheKey 的toString 方法已经被重写
                //  默认都配置到 mybatis-ys-cache 这个当中去了
                //  Ehcache ehcache = new Cache(cacheKey.toString(),5000,false,false,10,2);
                Element resultElement = new Element(cacheKey.toString(), list);
                EhcacheManager.getCache().put(resultElement);
                // 设置 总条数
                Element resultElementTotal = new Element(cacheKey.toString()+":totalSize", totalResult);
                EhcacheManager.getCache().put(resultElementTotal);
            }
            // cacheList = (List<?>)tcm.getObject(ms.getCache(),cacheKey);
        }

        return list;
    }

    // cacheFlag 是否开启缓存标志位
    private  static MappedStatement newSelectMappedStatement(Configuration configuration,String msId, SqlSource sqlSource,
                                                             final Class<?> resultType,StatementType statementType,Boolean cacheFlag,String dbType) {
        // 加强逻辑 ： 一定要防止 MappedStatement 重复问题
        MappedStatement msTest = null;
        try{
            synchronized (configuration) {   // 防止并发插入多次
                msTest = configuration.getMappedStatement(msId);
                if (msTest != null) {
                    configuration.getMappedStatementNames().remove(msTest.getId());
                }

            }
        }catch (IllegalArgumentException e){
            log.info("没有此mappedStatment,可以注入此mappedStatement到configuration当中");
        }
        MappedStatement ms = null;
            ms = new MappedStatement.Builder(
                    configuration, msId, sqlSource, SqlCommandType.SELECT)
                    .statementType(statementType)
                    .useCache(false)      // 切断掉 二级缓存 切换到 ehcache 当中去，即是保证执行的时候不去二级缓存找了，直接查询
                    .resultMaps(new ArrayList<ResultMap>() {
                        {
                            add(new ResultMap.Builder(configuration,
                                    "defaultResultMap",
                                    resultType,
                                    new ArrayList<ResultMapping>(0)).build());
                        }
                    })
                    .build();

        synchronized (configuration){
            configuration.addMappedStatement(ms); // 加入到此中去
        }
        return ms;
    }

    // 移除 config 当中的 MapperStatement 对象
    public static void removeMapperStatement(SqlSession sqlSession,String namespace,String mapperId){
        Configuration configuration = sqlSession.getConfiguration();
        if(configuration.getMappedStatementNames().contains(namespace+"."+mapperId)){
            configuration.getMappedStatementNames().remove(namespace+"."+mapperId);
        }
    }

}
