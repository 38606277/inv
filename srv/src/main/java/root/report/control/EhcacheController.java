package root.report.control;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;

import java.net.URL;
import java.util.List;

/**
 * @Auther: pccw
 * @Date: 2018/11/16 16:36
 * @Description:
 */

@RestController
@RequestMapping("/reportServer/cacheds")
public class EhcacheController extends RO {

    private static Logger log = Logger.getLogger(EhcacheController.class);

    private static Cache cache;

    // 默认的系统的缓存节点位置
    public static String NATIVE_CACHE_NODE = "mybatis-ys-cache";

    public static synchronized  Cache getCache() {
        if(cache == null) initCache();
        return cache;
    }


    private static void initCache() {
        URL url = EhcacheController.class.getClassLoader().getResource("ehcache.xml");
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
        Cache cache = EhcacheController.getCache();    // 得到默认节点的
        Element oldEhcacheElement =  EhcacheController.getCache().get(elementName);
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
    @RequestMapping(value = "/getAllCacheName", produces = "text/plain;charset=UTF-8")
    public String  getAllCacheName(){
        Cache cache = EhcacheController.getCache();    // 得到默认节点的

        //return cache.getKeys();
        return SuccessMsg("", cache.getKeys());
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
        Cache cache = EhcacheController.getCache();    // 得到默认节点的
        Element element = cache.get(key);
        if(element!=null && element.getObjectValue()!=null){
            return element.getObjectValue();
        }else {
            log.info("Ehcache:无对应的值.");
            return null;
        }
    }
    @RequestMapping(value = "/getElementValuesByKey", produces = "text/plain;charset=UTF-8")
    public  String getElementValuesByKey(@RequestBody String key){
        JSONObject obj= JSONObject.parseObject(key);
        Cache cache = EhcacheController.getCache();    // 得到默认节点的
        Element element = cache.get(obj.getString("cached_id"));
        if(element!=null && element.getObjectValue()!=null){
           // return element.getObjectValue();
            return SuccessMsg("", element.getObjectValue());
        }else {
            log.info("Ehcache:无对应的值.");
            return SuccessMsg("",null);
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
        Cache cache = EhcacheController.getCache();    // 得到默认节点的
        Element oldEhcacheElement =  EhcacheController.getCache().get(elementName);
        if(oldEhcacheElement!=null){
            cache.removeElement(oldEhcacheElement);
            log.info("Ehcache:成功移除名称为"+elementName+"的元素.");
        }else {
            log.info("Ehcache:不存在此元素,无需移除.");
        }

    }
}
