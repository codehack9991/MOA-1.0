package fast.common.core;

import static org.powermock.api.mockito.PowerMockito.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.*;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fast.common.agents.Agent;
import fast.common.agents.UiaAgent;
import fast.common.agents.WebBrowserAgent;
import fast.common.cipher.AES;

import org.powermock.reflect.Whitebox;

import com.esotericsoftware.yamlbeans.YamlReader;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Configurator.class })
@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
public class TestConfigurator {

	@Mock
	YamlReader yamlReader;

	@InjectMocks
	Configurator mockconfig;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsSingleThread() throws Exception {

		boolean value = Whitebox.invokeMethod(mockconfig, "IsSingleThread");
		assertEquals(Boolean.TRUE, value);
	}

	@Test
	public void testCreateAgent() throws Exception {

		Map settingsMap = new HashMap();
		Map agent = new HashMap();
		Map webclassname = new HashMap();
		webclassname.put("class_name", "fast.common.agents.WebBrowserAgent");
		agent.put("WebAgent", webclassname);

		Map uiaagent = new HashMap();
		Map uiaclassname = new HashMap();
		uiaclassname.put("class_name", "fast.common.agents.UiaAgent");
		agent.put("DesktopAgent", uiaclassname);

		settingsMap.put("Agents", agent);

		when((Map) yamlReader.read()).thenReturn(settingsMap);
		whenNew(YamlReader.class).withAnyArguments().thenReturn(yamlReader);
		mockconfig.release();
		mockconfig = Configurator.getInstance();

		Agent aclwebAgent = Whitebox.invokeMethod(mockconfig, "createAgent", "WebAgent");
		Agent acluiaAgent = Whitebox.invokeMethod(mockconfig, "createAgent", "DesktopAgent");

		assertTrue(aclwebAgent instanceof WebBrowserAgent);
		assertFalse(aclwebAgent instanceof UiaAgent);
		assertTrue(acluiaAgent instanceof UiaAgent);

	}

	@Test
	public void testDecodeSettings() throws Exception {

		// Map settingsMap = new HashMap();
		// settingsMap.put("secretKeyFile",
		// "//sd-f7c1-b7b1/Shared/privateDiane.txt");
		//
		// when((Map) yamlReader.read()).thenReturn(settingsMap);
		// whenNew(YamlReader.class).withAnyArguments().thenReturn(yamlReader);
		// mockconfig.release();
		// mockconfig = Configurator.getInstance();
		
		AES.SetSecretKey("1234567812345678");
		Map<String, String> map = new HashMap<String, String>();
		map.put("passcode", "3b e6 38 4e 76 3b a7 c2 13 d9 f1 d5 52 99 eb 3b ");
		Whitebox.invokeMethod(mockconfig, "decodeSettings", map);
		assertEquals("passcode", map.get("passcode"));
	}

	@Test
	public void testReadSettings() throws Exception {
		Map<String, String> settingsMap = new HashMap<String, String>();
		settingsMap.put("Agents", "agent");
		settingsMap.put("DesktopGUI", "desktopGui");
		settingsMap.put("Test", "test");

		when((Map) yamlReader.read()).thenReturn(settingsMap);
		whenNew(YamlReader.class).withAnyArguments().thenReturn(yamlReader);
		mockconfig.release();
		mockconfig = Configurator.getInstance();
		
		Map result = Whitebox.invokeMethod(mockconfig, "readSettings", "../config", "config");
		assertEquals(3, result.size());
	}


	@Test
	public void testMergeSettings() throws Exception {

		Map<String, Object> dstMap = new HashMap();
		dstMap.put("1", "a");
		dstMap.put("2", new HashMap<String, String>() {
			{
				put("c", "testc");
				put("d", "testd");
			}
		});
		Map<String, Object> srcmap = new HashMap();
		srcmap.put("2", new HashMap<String, String>() {
			{
				put("d", "testd");
				put("e", "testf");
			}
		});
		srcmap.put("3", "b");
		Whitebox.invokeMethod(mockconfig, "mergeSettings", dstMap, srcmap);
		Map testMap = (Map) dstMap.get("2");
		assertEquals(3, dstMap.size());
		assertEquals(3, testMap.size());
	}


	@Test
	public void testSetSecretKeyFile() throws Exception {
		Map<String, String> settingsMap = new HashMap<String, String>();
		settingsMap.put("secretKeyFile", "./src/test/resources/fast/common/core/privateKey.txt");
		when((Map<String, String>) yamlReader.read()).thenReturn(settingsMap);
		whenNew(YamlReader.class).withAnyArguments().thenReturn(yamlReader);
		mockconfig.release();
		AES.SetSecretKey(null);
		mockconfig = Configurator.getInstance();
		Whitebox.invokeMethod(mockconfig, "setSecretKeyFile");
		String sKey = Whitebox.getInternalState(AES.class, "sKey");
		assertEquals("0123456789abcdef", sKey);
	}

	@Test
	public void testGetGlobalPropertyValue() throws Exception {
		Map<String, String> settingsMap = new HashMap<String, String>();
		settingsMap.put("key", "test");
		String value = Whitebox.invokeMethod(mockconfig, "getGlobalPropertyValue", settingsMap, "key");
		assertEquals("test", value);
	}
	
	@Test
	public void testLoadSubConfigs() throws Exception {

		Map<String, String> settingsMap = new HashMap<String, String>();
		settingsMap.put("subconfigs", "config/subconfig");

		when((Map) yamlReader.read()).thenReturn(settingsMap);
		whenNew(YamlReader.class).withAnyArguments().thenReturn(yamlReader);
		mockconfig.release();
		mockconfig = Configurator.getInstance();
		
		
		Map result = Whitebox.invokeMethod(mockconfig, "readSubConfig", "config/users");
		assertEquals(1, result.size());
		
	}
}
