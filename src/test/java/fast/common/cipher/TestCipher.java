package fast.common.cipher;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import fast.common.cipher.AES;
import fast.common.cipher.PrivateKeyGen;

public class TestCipher {
	private static final String INVALID_PATH = "fast/common/core/privateKey.txt";
	private static final String VALID_PATH = "src/test/resources/fast/common/core/privateKey.txt";
	private static final String EMPTY_FILE_PATH = "src/test/resources/fast/common/core/empty-file.txt";
	private final String sKey = "1234567812345678";
	private final String invalidKey = "123456781234567";

	@Before
	public void setUp() throws Exception {
		AES.SetSecretKey(sKey);
	}

	@Test
	public void testPrivateKeyGenGetKeyFromValidFile() {
		String result = PrivateKeyGen.getKey(VALID_PATH);
		assertNotNull(result);
	}
	
	@Test
	public void testPrivateKeyGenDefaultConstructor() {
		PrivateKeyGen result = new PrivateKeyGen();
		assertNotNull(result);
	}
	
	@Test
	public void testAESDefaultConstructor() {
		AES result = new AES();
		assertNotNull(result);
	}
	
	@Test
	public void testPrivateKeyGenGetKeyFromInvalidFile() {
		String result = PrivateKeyGen.getKey(INVALID_PATH);
		assertNull(result);
	}
	
	@Test
	public void testPrivateKeyGenGetKeyFromEmptyFile() {
		String result = PrivateKeyGen.getKey(EMPTY_FILE_PATH);
		assertNull(result);
	}

	@Test
	public void testEncryption() {
		String plainText = "passcode";
		String expected = "3b e6 38 4e 76 3b a7 c2 13 d9 f1 d5 52 99 eb 3b ";
		String actual = AES.encode(plainText);
		assertEquals(expected, actual);
	}
	
	@Test
	public void testEncryptionWithInvalidKey() {
		AES.SetSecretKey(invalidKey);
		String plainText = "passcode";
		int expected = 0;
		int actual = AES.encode(plainText).length();		
		assertEquals(expected, actual);
	}

	@Test
	public void testDecryption() {
		String plainText = "passcode";
		String cipherText = "3b e6 38 4e 76 3b a7 c2 13 d9 f1 d5 52 99 eb 3b";
		String actual = AES.decode(cipherText);
		assertEquals(plainText, actual);
	}

	@Test
	public void testIsCipherFormat() {
		String encryptedText1 = "2d 05 14 1b cd e9 ed c5 af b6 62 7c 00";
		String encryptedText2 = "2d 05 14 1b cd e9 ed c5 af b6 62 7c 00 65 e4 8g";
		String encryptedText3 = "2d 05 14 1b cd e9 ed c5 af b6 62 7c 00 65 e4 80";
		String encryptedText4 = "2d05141bcde9edc5afb6627c0065e480";
		
		assertFalse(AES.isCipherFormat(encryptedText1));
		assertFalse(AES.isCipherFormat(encryptedText2));
		assertTrue(AES.isCipherFormat(encryptedText3));
		assertFalse(AES.isCipherFormat(encryptedText4));
	}
	
	@Test
	public void testSetSecretKeyFilePathWithInvalidSecretKey() throws IOException {
		AES.SetSecretKey("");
		String plainText = "passcode";
		int actual = AES.encode(plainText).length();
		assertEquals(0, actual);
	}

	@Test
	public void setSecretKeyFilePathWithValidPath() throws IOException {
		String path = VALID_PATH;
		AES.SetSecretKey(null);
		AES.setSecretKeyFilePath(path);
	}

	@Test
	public void setSecretKeyFilePathWithInvalidPathShouldThrowException() {
		String path = INVALID_PATH;
		AES.SetSecretKey(null);
		try {
			AES.setSecretKeyFilePath(path);
		} catch (Exception e) {
		}
	}
}
