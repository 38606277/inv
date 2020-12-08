package root.configure;

/**
 * 功能描述 : 定义myabtis 2级缓存的值
 */
public class MybatisCacheConfiguration {
    public static String EVICTION_VALUE = "LRU";
    public static String FLUSH_INTERVAL_VALUE = "100000";
    public static String SIZE_VALUE = "1024";
    public static String READONLY_VALUE = "true";
    public static String USE_CACHE_FALSE = "false";
    public static String USE_CACHE_TRUE = "true";
}
