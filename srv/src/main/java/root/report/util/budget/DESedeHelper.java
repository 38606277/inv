package root.report.util.budget;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DESedeHelper {

	public static byte[] getEncryptKey(int keySize) throws Exception {
		if (keySize != 112 && keySize != 168)
			throw new Exception("密钥长度只能为112位或168位");
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
			keyGenerator.init(keySize, new SecureRandom());
			SecretKey secretKey = keyGenerator.generateKey();
			return secretKey.getEncoded();
		}
		catch (NoSuchAlgorithmException e) {
			throw new Exception(e);
		}
	}

	public static byte[] encryptCBC(byte[] bytes, byte[] key, byte[] iv, String padding) throws Exception {
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DESede");
			Cipher cipher = Cipher.getInstance("DESede/CBC/" + padding);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
			return cipher.doFinal(bytes);
		}
		catch (InvalidKeyException e) {
			throw new Exception(e);
		}
		catch (InvalidAlgorithmParameterException e) {
			throw new Exception(e);
		}
		catch (NoSuchAlgorithmException e) {
			throw new Exception(e);
		}
		catch (NoSuchPaddingException e) {
			throw new Exception(e);
		}
		catch (IllegalBlockSizeException e) {
			throw new Exception(e);
		}
		catch (BadPaddingException e) {
			throw new Exception(e);
		}
	}

	public static byte[] decyrptCBC(byte[] encryptBytes, byte[] key, byte[] iv, String padding) throws Exception {
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DESede");
			Cipher cipher = Cipher.getInstance("DESede/CBC/" + padding);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
			return cipher.doFinal(encryptBytes);
		}
		catch (InvalidKeyException e) {
			throw new Exception(e);
		}
		catch (InvalidAlgorithmParameterException e) {
			throw new Exception(e);
		}
		catch (NoSuchAlgorithmException e) {
			throw new Exception(e);
		}
		catch (NoSuchPaddingException e) {
			throw new Exception(e);
		}
		catch (IllegalBlockSizeException e) {
			throw new Exception(e);
		}
		catch (BadPaddingException e) {
			throw new Exception(e);
		}
	}

	public static byte[] encryptECB(byte[] bytes, byte[] key, String padding) throws Exception {
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DESede");
			Cipher cipher = Cipher.getInstance("DESede/ECB/" + padding);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			return cipher.doFinal(bytes);
		}
		catch (InvalidKeyException e) {
			throw new Exception(e);
		}
		catch (NoSuchAlgorithmException e) {
			throw new Exception(e);
		}
		catch (NoSuchPaddingException e) {
			throw new Exception(e);
		}
		catch (IllegalBlockSizeException e) {
			throw new Exception(e);
		}
		catch (BadPaddingException e) {
			throw new Exception(e);
		}
	}

	public static byte[] decyrptECB(byte[] encryptBytes, byte[] key, String padding) throws Exception {
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DESede");
			Cipher cipher = Cipher.getInstance("DESede/ECB/" + padding);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			return cipher.doFinal(encryptBytes);
		}
		catch (NoSuchAlgorithmException e) {
			throw new Exception(e);
		}
		catch (NoSuchPaddingException e) {
			throw new Exception(e);
		}
		catch (InvalidKeyException e) {
			throw new Exception(e);
		}
		catch (IllegalBlockSizeException e) {
			throw new Exception(e);
		}
		catch (BadPaddingException e) {
			throw new Exception(e);
		}
	}
}