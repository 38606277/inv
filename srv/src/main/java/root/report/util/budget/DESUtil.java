package root.report.util.budget;

import java.net.URLEncoder;

public class DESUtil {

	private static final byte[] PASSWORD_CRYPT_KEY = { 0x61, 0x73, 0x64, 0x66, 0x67, 0x68, 0x6a, 0x6b, 0x66, 0x67, 0x67, 0x66, 0x64, 0x67, 0x6b, 0x77,
		0x65, 0x77, 0x65, 0x57, 0x45, 0x40, 0x23, 0x40 };
	private static final byte[] PASSWORD_IV = { 0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xab, (byte) 0xcd, (byte) 0xef };

	/**
	 * Description 加密
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String data) throws Exception {
		byte[] encryptCBC = DESedeHelper.encryptCBC(data.getBytes("UTF-8"), PASSWORD_CRYPT_KEY, PASSWORD_IV, "PKCS5Padding");
		String out = Base64Helper.encode(encryptCBC);
		String para = URLEncoder.encode(out, "UTF-8");
		return para;
	}
	
}
