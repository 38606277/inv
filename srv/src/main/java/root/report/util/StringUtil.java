package root.report.util;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author kevin
 */
public class StringUtil {
	
	/**
	 * 把字符串转成数字:例如A是1, C是3， AA是27
	 */
	public static Integer getLetterIntValue(String str) {
		if(str == null || "".equals(str)) return null;
		final String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		char[] charA = str.toUpperCase().toCharArray();
		int letterValue = 0;
		int idx = charA.length;
		for(char c : charA){//从最高位开始循环计算累计
			idx --;
			letterValue +=  (letters.indexOf(c)+1)*Double.valueOf(Math.pow(26, idx)).intValue();
		}
		return letterValue;
	}
	
	/**
	 * 用正则表达式查找字符串
	 * @param regex
	 * @param content
	 * @param groupIdx
	 * @return
	 */
	public static String findStrWithRegex(String regex, String content, int groupIdx){
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		if(matcher.find()){
			return matcher.group(groupIdx);
		}
		return null;
	}
	public static String fmtMicrometer(String text) {  
        DecimalFormat df = null;  
        if (text.indexOf(".") > 0) {  
            if (text.length() - text.indexOf(".") - 1 == 0) {  
                df = new DecimalFormat("###,##0.");  
            } else if (text.length() - text.indexOf(".") - 1 == 1) {  
                df = new DecimalFormat("###,##0.0");  
            } else {  
                df = new DecimalFormat("###,##0.00");  
            }  
        } else {  
            df = new DecimalFormat("###,##0");  
        }  
        double number = 0.0;  
        try {  
            number = Double.parseDouble(text);  
        } catch (Exception e) {  
            number = 0.0;  
        }  
        return df.format(number);  
    }  

    public static boolean isBlank(String str){
		if(str == null || str.trim().equals("")) return Boolean.TRUE;
		return Boolean.FALSE;
	}

	public static void main(String[] args) {
		System.out.println(getLetterIntValue("ab"));
		int size = getLetterIntValue("AA")-getLetterIntValue("A")+1;
		System.out.println(size);
		System.out.println(fmtMicrometer("123456789.23"));
	}
}
