package fast.common.gmdReplay.pserver;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.citi.gmd.client.config.GMDConfig;

public class TestGMDSITConfigLoader {
	
	@Test
	public void testLoadConfigWithValidFilePath2() {
		GMDSITConfigLoader configLoader = new GMDSITConfigLoader();
		String pathname = "src/test/resources/test2.ini";
		GMDConfig config = configLoader.loadConfig(pathname );
		assertNotNull(config);
	}
	
	@Test
	public void testLoadConfigWithValidFilePath3() {
		GMDSITConfigLoader configLoader = new GMDSITConfigLoader();
		String pathname = "src/test/resources/test3.ini";
		GMDConfig config = configLoader.loadConfig(pathname );
		assertNotNull(config);
	}
	
	@Test
	public void testLoadConfigWithValidFilePath4() {
		GMDSITConfigLoader configLoader = new GMDSITConfigLoader();
		String pathname = "src/test/resources/test4.ini";
		GMDConfig config = configLoader.loadConfig(pathname );
		assertNotNull(config);
	}
	
}
