
package fast.common.cipher;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import fast.common.logging.FastLogger;

/**
 * Library for private key generation
 * 
 * @author lc37141
 *
 */
public class PrivateKeyGen {
	private static FastLogger logger = FastLogger.getLogger("PrivateKeyGen");
	
	/**
	 * Get private key from file
	 * @param filePath
	 * @return
	 */
	public static String getKey(String filePath) {
		File file = new File(filePath);
		if(file.canRead()) {
			try {
				return Files.readAllLines(Paths.get(filePath)).get(0);
			} catch (Exception e) {				
				logger.error("Failed to read the key file with exception :\n" + e.getMessage());
			}		
		}
		return null;
	}	
}
