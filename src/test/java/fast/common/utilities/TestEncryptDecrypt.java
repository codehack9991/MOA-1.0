package fast.common.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestEncryptDecrypt {

	@Test
	public void testEncryptDecrypt() {
		EncryptDecrypt e = new EncryptDecrypt();
		assertNotNull(e);
		String encrypt = EncryptDecrypt.encrypt("hello");
		String decrypt = EncryptDecrypt.decrypt(encrypt);
		assertEquals("hello", decrypt);
	}
}
