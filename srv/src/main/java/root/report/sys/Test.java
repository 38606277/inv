package root.report.sys;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


@RestController
@RequestMapping("/reportServer")
public class Test {
	
	// 计算余额
		@RequestMapping(value = "/test", produces = "text/plain;charset=UTF-8")
		public String test() {
		   return "service is ok";
		}
		
		
		public void md5code() {
			
			
		}
		
//		//@org.junit.Test
//		public String getMd5(String str) {
//	        String s = str;
//	        if (s == null) {
//	            return "";
//	        } else {
//	            String value = null;
//	            MessageDigest md5 = null;
//	            try {
//	                md5 = MessageDigest.getInstance("MD5");
//	            } catch (NoSuchAlgorithmException ex) {
//	                ex.printStackTrace();
//	            }
//	            sun.misc.BASE64Encoder baseEncoder = new sun.misc.BASE64Encoder();
//	            try {
//	                value = baseEncoder.encode(md5.digest(s.getBytes("utf-8")));
//	            } catch (Exception ex) {
//	                ex.printStackTrace();
//	            }
//	            return value;
//	        }
//	    }
		
		
//		public static void main(String[] args) {
////			Test nMd5 = new Test();
////	        String value = nMd5.getMd5("wj");
//	        System.out.println(value);
//	    }

}
