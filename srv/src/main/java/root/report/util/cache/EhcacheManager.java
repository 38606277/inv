package root.report.util.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: pccw
 * @Date: 2018/11/16 16:36
 * @Description:
 */
public class EhcacheManager {

    private static Logger log = Logger.getLogger(EhcacheManager.class);

    private static Cache cache;

    // 默认的系统的缓存节点位置
    public static String NATIVE_CACHE_NODE = "mybatis-ys-cache";

    public static synchronized  Cache getCache() {
        if(cache == null) initCache();
        return cache;
    }


    private static void initCache() {
        URL url = EhcacheManager.class.getClassLoader().getResource("ehcache.xml");
        CacheManager cm =  CacheManager.create(url);
       /* InputStream in = EhcacheManager.class.getClassLoader().getResourceAsStream("/ehcache.xml");
        CacheManager cm = CacheManager.create(in);*/
        cache = cm.getCache("mybatis-ys-cache");
    }

    /**
     *
     * 功能描述:
     *     往 指定节点当中增加指定的元素值
     * @param:
     * @return:
     * @auther: pccw
     * @date: 2018/11/23 14:44
     */
    public static void addCacheElement(String elementName,Object elementValue){
        log.info("往ehcache当中装入缓存...");
        Cache cache = EhcacheManager.getCache();    // 得到默认节点的
        Element oldEhcacheElement =  EhcacheManager.getCache().get(elementName);
        Element newEhcacheElement = new Element(elementName,elementValue);
        if(oldEhcacheElement!=null && oldEhcacheElement.getObjectValue()!=null){
            // 代表有值，需要先删除
            log.info("删除原有的"+elementName+"节点");
            cache.replace(oldEhcacheElement,newEhcacheElement);
        }else {
            cache.put(newEhcacheElement);
        }
        log.info("增加cache:"+elementName+"成功");
    }

    /**
     *
     * 功能描述:
     *  得到系统 mybatis-ys-cache 当中的所有缓存对象的名称
     * @param:
     * @return:
     * @auther: pccw
     * @date: 2018/11/23 15:16
     */
    public static List getAllCacheName(){
        Cache cache = EhcacheManager.getCache();    // 得到默认节点的
        return cache.getKeys();
    }

    /**
     *
     * 功能描述:
     *      根据cache 当中的key得到 element对象
     * @param:
     * @return:
     * @auther: pccw
     * @date: 2018/11/23 15:40
     */
    public static Object getElementValueByKey(String key){
        Cache cache = EhcacheManager.getCache();    // 得到默认节点的
        Element element = cache.get(key);
        if(element!=null && element.getObjectValue()!=null){
            return element.getObjectValue();
        }else {
            log.info("Ehcache:无对应的值.");
            return null;
        }
    }

    /**
     *
     * 功能描述:
     *      移除缓存当中的指定节点
     * @param:
     * @return:
     * @auther: pccw
     * @date: 2018/11/23 15:37
     */
    public static void removeElement(String elementName){
        Cache cache = EhcacheManager.getCache();    // 得到默认节点的
        Element oldEhcacheElement =  EhcacheManager.getCache().get(elementName);
        if(oldEhcacheElement!=null){
            cache.removeElement(oldEhcacheElement);
            log.info("Ehcache:成功移除名称为"+elementName+"的元素.");
        }else {
            log.info("Ehcache:不存在此元素,无需移除.");
        }

    }
}
