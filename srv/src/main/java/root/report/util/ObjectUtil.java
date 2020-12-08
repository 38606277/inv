package root.report.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import root.report.file.Excel2MysqlForTax.AssetTaxUploadParam;
import root.report.util.annotaion.DBColumn;

public class ObjectUtil {
	
	//或得DBColumn类型注解的值，并把对象值出放到map中返回
	public static Map<String, Object> convertObjectToMap(Object obj) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		Method[] methods = obj.getClass().getDeclaredMethods();
		if(methods != null){
			for(Method m : methods){
				if(m.isAnnotationPresent(DBColumn.class)){
					map.put(m.getDeclaredAnnotation(DBColumn.class).value(), m.invoke(obj));
				}
			}
		}
		return map;
	}
	
	public static void main(String[] args) {
		AssetTaxUploadParam param = new AssetTaxUploadParam("1", "青岛","钟晶","MAY-17","FA");
		try {
			System.out.println(convertObjectToMap(param));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
