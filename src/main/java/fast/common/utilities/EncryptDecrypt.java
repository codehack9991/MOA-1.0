package fast.common.utilities;

import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.binary.Base64;

import fast.common.logging.FastLogger;

public class EncryptDecrypt {
	private static FastLogger _logger = FastLogger.getLogger("EncryptDecrypt");

	public static String encrypt(String encryptInputString) {
		String encstr = null;
		try {
			byte[] encryptArray = Base64.encodeBase64(encryptInputString
					.getBytes());
			encstr = new String(encryptArray, "UTF-8");

		} catch (UnsupportedEncodingException e) {
			_logger.error("Failed to encrypt with exception: \n" + e.getMessage());
		}
		return encstr;
	}

	public static String decrypt(String decryptInputString) {
		String decstr = null;
		try {
			byte[] dectryptArray = decryptInputString.getBytes();
			byte[] decarray = Base64.decodeBase64(dectryptArray);
			decstr = new String(decarray, "UTF-8");

		} catch (UnsupportedEncodingException e) {
			_logger.error("Failed to decrypt with exception: \n" + e.getMessage());
		}
		return decstr;
	}
	
}
