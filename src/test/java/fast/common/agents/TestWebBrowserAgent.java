package fast.common.agents;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;


import fast.common.core.Configurator;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class TestWebBrowserAgent {

    private String name;
    private Configurator configurator;
    private Map agentParams;

    @Mock
    TakesScreenshot takesScreenshot;

    @Mock
    Actions action;

    @Mock
    WebElement el;

    @Mock
    WebDriver driver;

    @InjectMocks
    WebBrowserAgent agent = new WebBrowserAgent();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        name = "nonMeaningName";
        configurator = Configurator.getInstance();
        agentParams = mock(Map.class);
        when(agentParams.get("headless")).thenReturn("no");
        when(agentParams.get("headlessBrowser")).thenReturn("chrome");
        when(agentParams.get("webRepo")).thenReturn("repos/sample_web");
        when(agentParams.get("driverClassName")).thenReturn("org.sqlite.JDBC");
        when(agentParams.get("driverRemoteAddress")).thenReturn("invalidDriverRemoteAddress");
        when(agentParams.get("webDriverPath")).thenReturn("invalidWebDriverPath");
        when(agentParams.get("proxy")).thenReturn("invalidProxy");
        when(agentParams.get("chromeArgument")).thenReturn("invalidChromeArgument");
    }


    @After
    public void tearDown() throws Exception {
        agent.close();
    }

    @Test
    public void constructWithParams() {
        agent = new WebBrowserAgent(name, agentParams, configurator);
        assertEquals("nonMeaningName", Whitebox.getInternalState(agent, "_name"));
    }

    @Test
    public void testReadRepo() throws Exception {
        when(agentParams.get("webRepo")).thenReturn("./testdata/web");
        agent = new WebBrowserAgent(name, agentParams, configurator);

        assertTrue(agent.getRepo().containsKey("SearchPage"));
        assertTrue(agent.getRepo().containsKey("SearchResultsPage"));
    }

    @Test
    public void TestGetRepoElementValue() throws Exception {
        when(agentParams.get("webRepo")).thenReturn("./testdata/web");
        agent = new WebBrowserAgent(name, agentParams, configurator);
        agent.setCurrentPage("SearchPage");
        assertTrue(agent.getRepoElementValue("SearchTextbox").equals("//input[@id='searchInput']"));
    }

    @Test
    public void TestGetRepoElementValueDotposGreaterThanZero() throws Exception {
        when(agentParams.get("webRepo")).thenReturn("./testdata/web");
        agent = new WebBrowserAgent(name, agentParams, configurator);
        assertTrue(agent.getRepoElementValue("TestPage.test").equals("//input[@id='test']"));
    }

    @Test
    public void TestGetRepoElementValueWithException() throws Exception {
        when(agentParams.get("webRepo")).thenReturn("./testdata/web");
        agent = new WebBrowserAgent(name, agentParams, configurator);
        thrown.expect(Exception.class);
        thrown.expectMessage("Cannot find control repo for page TestInvalidPage");
        agent.getRepoElementValue("TestInvalidPage.test");
    }

    @Test
    public void testIsXpath() throws Exception {
        when(agentParams.get(any(String.class))).thenReturn(null);
        agent = new WebBrowserAgent("Test", agentParams, configurator);
        assertTrue(agent.isXpath("//input[@id='searchInput']"));
        assertTrue(!agent.isXpath("#searchInput"));
    }

    @Test
    public void testGetOrCreateDriver() throws Exception {
        when(agentParams.get("webRepo")).thenReturn("./testdata/web");
        when(agentParams.get(WebBrowserAgent.PARAM_DRIVER_CLASSNAME)).thenReturn("org.openqa.selenium.chrome.ChromeDriver");
        when(agentParams.get(WebBrowserAgent.PARAM_WEB_DRIVER_PATH)).thenReturn("testdata/chromedriver.exe");
        when(agentParams.get(WebBrowserAgent.PARAM_APP_BINARY_PATH)).thenReturn("xxx.exe");
        when(agentParams.containsKey(WebBrowserAgent.PARAM_APP_BINARY_PATH)).thenReturn(true);

        Exception exception = null;
        try {
            agent = new WebBrowserAgent("Test", agentParams, configurator);
            agent.getOrCreateWebDriver();
        } catch (Exception ex) {
            exception = ex;
        }
        assertNotNull(exception);
    }

    @Test
    public void getOrCreateDriverHeadlessTrue() throws Exception {

        when(agentParams.get("headless")).thenReturn("no");
        Map map = new HashMap<>();
        map.put("browserName", "Chrome");
        map.put("platform", "windows");
        when(agentParams.get("desiredCapabilities")).thenReturn(map);
        when(agentParams.get("driverClassName")).thenReturn("org.openqa.selenium.chrome.ChromeDriver");
        agent = new WebBrowserAgent(name, agentParams, configurator);
    }

    @Test
    public void testGetOrCreateDriverWithHeadless() throws Exception {
        when(agentParams.get("headless")).thenReturn("yes");
        when(agentParams.get("webRepo")).thenReturn("./testdata/web");
        when(agentParams.get(WebBrowserAgent.PARAM_DRIVER_CLASSNAME)).thenReturn("org.openqa.selenium.chrome.ChromeDriver");
        when(agentParams.get(WebBrowserAgent.PARAM_WEB_DRIVER_PATH)).thenReturn("testdata/chromedriver.exe");
        when(agentParams.get(WebBrowserAgent.PARAM_APP_BINARY_PATH)).thenReturn("xxx.exe");
        when(agentParams.containsKey(WebBrowserAgent.PARAM_APP_BINARY_PATH)).thenReturn(true);

        Exception exception = null;
        try {
            agent = new WebBrowserAgent("Test", agentParams, configurator);
            agent.getOrCreateWebDriver();
        } catch (Exception ex) {
            exception = ex;
        }
        assertNotNull(exception);
    }

    @Test
    public void testSetRepo() {
        Map repo = new HashMap<>();
        repo.put("invalidKey", "invalidValue");
        agent.setRepo(repo);
        assertEquals("invalidValue", agent.getRepo().get("invalidKey"));
    }

    @Test
    public void testGetRepo() {
        Map repo = new HashMap<>();
        repo.put("invalidKey", "invalidValue");
        agent.setRepo(repo);
        assertEquals("invalidValue", agent.getRepo().get("invalidKey"));
    }

    @Test
    public void testSetCurrentPage() {
        agent.setCurrentPage("invalidPage");
        assertEquals("invalidPage", agent.getCurrentPage());
    }

    @Test
    public void testGetCurrentPage() {
        agent.setCurrentPage("invalidPage");
        assertEquals("invalidPage", agent.getCurrentPage());
    }

    @Test
    public void testDefineLocationVariable() throws Exception {
        when(agentParams.get("webRepo")).thenReturn("./testdata/web");
        agent = new WebBrowserAgent(name, agentParams, configurator);
        assertEquals("[//input[@id='test']invalidValue]", agent.defineLocationVariable("TestPage.testTwo", "invalidValue").toString());
    }

    @Test
    public void testSetTimeout() {
        agent.setTimeout(6);
    }


    @Test
    public void openUrl_success() throws Exception {
        String url = "http://test";
        doNothing().when(driver).get(url);
        agent.openUrl(url);
    }

    @Test
    public void testGetDriver() throws Exception {
        assertEquals(driver, agent.getDriver());
    }

    @Test
    public void testSetDriver() throws Exception {
        agent.setDriver(driver);
        assertEquals(driver, agent.getDriver());
    }

    @Test
    public void testGetCurrentUrl() throws Exception {
        when(driver.getCurrentUrl()).thenReturn("testUrl");
        assertEquals("[testUrl]", agent.getCurrentUrl().toString());
    }

    @Test
    public void testGetText() throws Exception {
        Map repo = new HashMap<>();
        Map repoInner = new HashMap<>();
        repoInner.put("test", "//input[@id='test']");
        repo.put("TestPage", repoInner);
        agent.setRepo(repo);
        System.out.println(agent.getRepoElementValue("TestPage.test"));
        WebDriverWait wait = mock(WebDriverWait.class);
        PowerMockito.whenNew(WebDriverWait.class).withArguments(driver, (long) 5).thenReturn(wait);
        when(wait.until(any())).thenReturn("test");
    }

    @Test
    public void testPressHotKey() throws Exception {
        PowerMockito.whenNew(Actions.class).withArguments(driver).thenReturn(action);
        doNothing().when(action).perform();
        agent.pressHotKey("CONTROL+A");
        agent.pressHotKey("ALT");
        agent.pressHotKey("CONTROL+SPACE");
    }

    @Test
    public void testPressHotKeyOnControl() throws Exception {
        Map<String, Map<String, String>> repo = new HashMap<>();
        Map<String, String> repoInner = new HashMap<>();
        repoInner.put("test", "//input[@id='test']");
        repo.put("TestPage", repoInner);
        agent.setRepo(repo);
        when(driver.findElement(any(By.class))).thenReturn(el);
        doNothing().when(el).sendKeys(any(CharSequence.class));
        doNothing().when(el).sendKeys(any(CharSequence.class), any(CharSequence.class));
        agent.pressHotKeyOnControl("ALT", "TestPage.test");
        agent.pressHotKeyOnControl("CONTROL+A", "TestPage.test");
        agent.pressHotKeyOnControl("CONTROL+SPACE", "TestPage.test");
    }

    @Test
    public void testPressHotKeyOnControlWithIndex() throws Exception {
        Map<String, Map<String, String>> repo = new HashMap<>();
        Map<String, String> repoInner = new HashMap<>();
        repoInner.put("testDynamic", "//input[@id='dynamicIndex']");
        repo.put("TestPage", repoInner);
        agent.setRepo(repo);
        when(driver.findElement(any(By.class))).thenReturn(el);
        doNothing().when(el).sendKeys(any(CharSequence.class));
        doNothing().when(el).sendKeys(any(CharSequence.class), any(CharSequence.class));
        agent.pressHotKeyOnControlWithIndex("ALT", "TestPage.testDynamic", "test");
        agent.pressHotKeyOnControlWithIndex("CONTROL+A", "TestPage.testDynamic", "test");
    }

    @Test
    public void testReadAttributValueWithIndex() throws Exception {
        Map<String, Map<String, String>> repo = new HashMap<>();
        Map<String, String> repoInner = new HashMap<>();
        repoInner.put("testDynamic", "//input[@id='dynamicIndex']");
        repo.put("TestPage", repoInner);
        agent.setRepo(repo);
        when(driver.findElement(any(By.class))).thenReturn(el);
        el = mock(WebElement.class, withSettings().name("elementName"));
        when(el.getAttribute("name")).thenReturn("elementName");
        agent.readAttributValueWithIndex("TestPage.testDynamic", "name", "test");
    }

    @Test
    public void testReadTextWithIndex() throws Throwable {
        Map<String, Map<String, String>> repo = new HashMap<>();
        Map<String, String> repoInner = new HashMap<>();
        repoInner.put("testDynamic", "//input[@id='dynamicIndex']");
        repo.put("TestPage", repoInner);
        agent.setRepo(repo);
        when(driver.findElement(any(By.class))).thenReturn(el);
        agent.readTextWithIndex("TestPage.testDynamic", "test");
    }

    @Test
    public void testLocateElements() throws Exception {
        Map<String, Map<String, String>> repo = new HashMap<>();
        Map<String, String> repoInner = new HashMap<>();
        repoInner.put("test", "//input[@id='test']");
        repo.put("TestPage", repoInner);
        agent.setRepo(repo);
        when(driver.findElement(any(By.class))).thenReturn(el);
        assertEquals("", agent.readAllTextOnControl("TestPage.test").toString());
    }

    @Test
    public void testReadAllTextOnControl() throws Exception {
        Map<String, Map<String, String>> repo = new HashMap<>();
        Map<String, String> repoInner = new HashMap<>();
        repoInner.put("test", "//input[@id='test']");
        repo.put("TestPage", repoInner);
        agent.setRepo(repo);
        when(agentParams.get("webRepo")).thenReturn("./testdata/web");
        agent.readAllTextOnControl("TestPage.test");
        assertEquals("", agent.readAllTextOnControl("TestPage.test").toString());
    }

}
