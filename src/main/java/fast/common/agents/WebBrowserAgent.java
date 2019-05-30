package fast.common.agents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fast.common.context.WebStepResult;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import fast.common.context.CommonStepResult;
import fast.common.context.StepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;
/**
 * The {@code WebBrowserAgent} class defines various common actions for automating tests of web UI using <tt>selenium</tt> WebDriver.
 * 
 * <p>The actions in this class required Xpath or CssSelector of the element in the web page </p>
 * 
 * <p>The basic actions includes: open url, click, type, check page element ...</p>
 * 
 * <p>Details information for using a WebBrowserAgent can see: 
 * <p><a href="https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/Web+GUI+Automation+Examples">Examples</a></p>
 * 
 * @author QA Framework Team
 * @since 1.5
 */
public class WebBrowserAgent extends Agent {
    
	/**
	 * Used for adding value of the experimental option 
	 * <p>The field can be specified in configuration file as a key name of WebBrowserAgent
	 * <p>The values under this field are ChromeDriver options
	 * <p>Example in configuration file:
	 * <pre>chromePrefs:</pre>
	 * <blockquote><pre>download.default_directory: XXX</pre></blockquote>
	 * 
	 * @since 1.5
	 */
	public static final String CONFIG_CHROME_PREFS = "chromePrefs";
	/**
	 * Used for running tests with hidden specific browser
	 * <p>Example in configuration file:
	 * <pre>headlessBrowser:chrome</pre>
	 * @since 1.5
	 */
	public static final String CONFIG_HEADLESS_BROWSER = "headlessBrowser";
	/**
	 * Used for specify file path for download action.
	 * <p>The field should be specified as an item under chromePrefs in configuration file</p>
	 * 
	 * @since 1.5
	 */
	public static final String CONFIG_AUTO_DOWNLOAD = "download.default_directory";
	/**
	 * Used for specify dynamic index in xpath expression.
	 * <p>The field should be specified as a dynamic index in xpath expression</p>
	 *
	 * @since 1.9
	 */
	public static final String DYNAMIC_INDEX = "dynamicIndex";
	
	/**
	 * Desired capabilities config key name for WebBrowserAgent	 * 
	 * <p>The values under this field are desired capabilities for kinds of Web driver such as Chrome, IE and Remote Driver
	 * <p>Example in configuration file:
	 * <pre>chromePrefs:</pre>
	 * <blockquote><pre>browserName: Chrome</pre></blockquote>
	 * <blockquote><pre>platform: LINUX</pre></blockquote>
	 * @since 1.5
	 */
	public static final String CONFIG_DESIRED_CAPABILITIES = "desiredCapabilities";
	
	public static final String CONFIG_DESIRED_CAPABILITIES_BROWSER_NAME = "browserName";
	public static final String CONFIG_DESIRED_CAPABILITIES_HEADLESS = "headless";
	public static final String CONFIG_DESIRED_CAPABILITIES_PLATFORM = "platform";
	public static final String LOCATION_VARIABLE_NAME= "<LocationVariable>";
	
	public static final String CONFIG_REPOSITORY_NAME="Repository";
	
	
	public static String PARAM_DRIVER_CLASSNAME="driverClassName";
	public static String PARAM_DRIVER_REMOTEADDRESS="driverRemoteAddress";
	public static String PARAM_WEB_DRIVER_PATH="webDriverPath";
	public static String PARAM_PROXY="proxy";
	public static String PARAM_ARGUMENTS="chromeArgument";
	public static String PARAM_APP_BINARY_PATH="appBinaryPath";
	
	public static String CLASS_CHROME_DRIVER="org.openqa.selenium.chrome.ChromeDriver";
	public static String CLASS_IE_DRIVER="org.openqa.selenium.ie.InternetExplorerDriver";
	
	public static String PROPERTY_CHROME_DRIVER="webdriver.chrome.driver";
	public static String PROPERTY_IE_DRIVER="webdriver.ie.driver";
	
	private FastLogger _logger;
    private Map<?,?> repo;
    private String currentPage;
   

	private WebDriver _driver;
    private int _timeout = 5;
	boolean headless = false;
    String headlessBrowser ;
    
    public WebBrowserAgent(){
    	
    }
    
    /**
     * Constructs a new <tt>WebBrowserAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
     *
     * @param   name a string for naming the creating WebBrowserAgent 
     * @param   agentParams a map to get the required parameters for creating a WebBrowserAgent 
     * @param   configurator a Configurator instance to provide configuration info for the actions of the WebBrowserAgent
     * 
     * @since 1.5
     */
    public WebBrowserAgent(String name, Map<?,?> agentParams, Configurator configurator) {
        super(name, agentParams, configurator);
        _logger = FastLogger.getLogger(String.format("%s:WebBrowserAgent", _name));
        _agentParams = agentParams;
        Object headlessObj = _agentParams.get(CONFIG_DESIRED_CAPABILITIES_HEADLESS);
		if (headlessObj != null) {
			headless = headlessObj.toString().equalsIgnoreCase("YES");
		}
		Object headlessBrowserObj = _agentParams.get(CONFIG_HEADLESS_BROWSER);
		if (headlessBrowserObj != null) {
			headlessBrowser = headlessBrowserObj.toString();
		}
        Object webRepo = _agentParams.get("webRepo");
        
        if(webRepo == null){
        	repo = (Map<?,?>) configurator.getSettingsMap().get(CONFIG_REPOSITORY_NAME);
        }
		else {
			try {
				repo = (Map<?,?>) readRepo(_agentParams.get("webRepo").toString()).get(CONFIG_REPOSITORY_NAME);
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}
    /**
     * Returns a map of all web elements' Xpath or CssSelector load from configuration file
     * @return a map
     * <p>Repo information starts with a keyword "Repository" in configuration file
     * <p>A repo contains various pages information.
     * <p>A page contains various web elements,in particular,"trait" is a required keyword and its value can be any Xpath or CssSelector in this page
     * <p>Example:
     * <blockquote><pre>Repository:</pre>
     * <pre>&nbsp;&nbsp;LoginPage:</pre>
     * <pre>&nbsp;&nbsp;&nbsp;&nbsp;trait: //input[@id="soeid"]</pre>
     * <pre>&nbsp;&nbsp;&nbsp;&nbsp;usernameTextbox: //input[@id="soeid"]</pre></blockquote>
     * @since 1.5
     */
    public Map<?,?> getRepo() {
		return repo;
	}

    /**
     * Sets elements' Xpath or CssSelector for actions
     * @param repo a map contains various web elements.Key is a element's name, value is the element's Xpath or CssSelector
     * 
     * @since 1.5
     */
	public void setRepo(Map<?,?> repo) {
		this.repo = repo;
	}
	
	/**
	 * @return current web page
	 * 
	 * @since 1.5
	 */
	public String getCurrentPage() {
		return currentPage;
	}
	
	/**
	 * Sets a page as current page
	 * @param currentPage name of a page 
	 * 	
	 * <p>Example:
	 * <blockquote><pre>webBrowserAgent.setCurrentPage("LoginPage");</pre></blockquote>
	 * @since 1.5
	 */
    
	public void setCurrentPage(String currentPage) {
		this.currentPage = currentPage;
	}
    protected Map<?,?> readRepo(String repoPath) throws FileNotFoundException, YamlException {
		
		File dir = new File(repoPath);
       
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(); 
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					repo = readRepo(files[i].getPath());
				} else if (files[i].isFile()) {
					Map fs = (Map<?,?>) new YamlReader(new FileReader(files[i].getPath())).read();
					if (repo == null) {
						repo = new HashMap<>();
						repo.putAll(fs);
						_logger.info(String.format("Loaded additional repo from: '%s'", files[i].getPath()));
					}
					else {
						((Map) repo.get(CONFIG_REPOSITORY_NAME)).putAll((Map) fs.get(CONFIG_REPOSITORY_NAME));
						_logger.info(String.format("Loaded additional repo from: '%s'", files[i].getPath()));
					}
				}
			}
		}
		else if (dir.isFile()){
			repo = (Map<?,?>) new YamlReader(new FileReader(dir.getPath())).read();
			_logger.info(String.format("Loaded additional repo from: '%s'", dir.getPath()));
		}
        return repo; 
		
	}

    protected WebDriver getOrCreateWebDriver() throws Exception {
    	if (_driver != null)
            return _driver;    	    
    	
    	String driverClassName = _agentParams.get(PARAM_DRIVER_CLASSNAME).toString();
        Object driverRemoteAddressObj = _agentParams.get(PARAM_DRIVER_REMOTEADDRESS);
        Object webDriverPath = _agentParams.get(PARAM_WEB_DRIVER_PATH);
		Object proxyAddress = _agentParams.get(PARAM_PROXY);
		Object arguments = _agentParams.get(PARAM_ARGUMENTS);	

		if (headless) {
			if(headlessBrowser.equalsIgnoreCase("CHROME")){
				System.setProperty(PROPERTY_CHROME_DRIVER, webDriverPath.toString());
				Class<?> class_ = Class.forName(driverClassName);
				Constructor<?> ctor = class_.getConstructor(Capabilities.class); 
				ChromeOptions options = new ChromeOptions();
				options.addArguments("--headless");		
				options.addArguments("--start-maximized");
				if(arguments != null && !"".equals(arguments.toString())){
					options.addArguments(arguments.toString());
				}
				if(_agentParams.get(CONFIG_CHROME_PREFS) != null){
					Map<String, Object> chromePrefs = (Map<String, Object>) _agentParams.get(CONFIG_CHROME_PREFS);
					chromePrefs.putAll(chromePrefs);
					if (chromePrefs.get(CONFIG_AUTO_DOWNLOAD) != null
							&& !chromePrefs.get(CONFIG_AUTO_DOWNLOAD).toString().isEmpty()) {
						String path = chromePrefs.get(CONFIG_AUTO_DOWNLOAD).toString();
						path = new File(path).getAbsolutePath();
						chromePrefs.put(CONFIG_AUTO_DOWNLOAD, path);
					}
					options.setExperimentalOption("prefs", chromePrefs);
				}			    

				options.setExperimentalOption("useAutomationExtension", false);
				
				DesiredCapabilities crcapabilities = DesiredCapabilities.chrome();
			    crcapabilities.setCapability(ChromeOptions.CAPABILITY, options);
			    crcapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			    crcapabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
				
			    _driver = (WebDriver)ctor.newInstance(crcapabilities);
			    
				return _driver;
			}
			DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
			if (proxyAddress != null) {
				Proxy proxy = new Proxy();
				proxy.setHttpProxy(proxyAddress.toString()); // set your local proxy
				capabilities.setCapability(CapabilityType.PROXY, proxy);
			}
			if(headlessBrowser.equalsIgnoreCase("IE")){
				capabilities.setVersion(org.openqa.selenium.remote.BrowserType.IE);
				}
				_driver = new HtmlUnitDriver(capabilities); 
				((HtmlUnitDriver) _driver).setJavascriptEnabled(false);
				_driver.manage().window().maximize();

			return _driver;
		}
        
		if (driverRemoteAddressObj == null) {

			if (driverClassName.equals(CLASS_CHROME_DRIVER)) {							
				
				System.setProperty(PROPERTY_CHROME_DRIVER, webDriverPath.toString());
				Class<?> class_ = Class.forName(driverClassName);
				Constructor<?> ctor = class_.getConstructor(ChromeOptions.class);
				ChromeOptions options = new ChromeOptions();
				
				if(_agentParams.containsKey(PARAM_APP_BINARY_PATH)){
					Object appBinaryPath = _agentParams.get(PARAM_APP_BINARY_PATH);
					options.setBinary(appBinaryPath.toString());
				}
				
				options.addArguments("--start-maximized");
				
				if (arguments != null && !"".equals(arguments.toString())) {
					for (String aString : arguments.toString().split(";")) {
						options.addArguments(aString);
					}
				}
				if(_agentParams.get(CONFIG_CHROME_PREFS) != null && !_agentParams.get(CONFIG_CHROME_PREFS).toString().isEmpty()){
					Map<String, Object> chromePrefs = (Map<String, Object>) _agentParams.get(CONFIG_CHROME_PREFS);

					chromePrefs.putAll(chromePrefs);
					if (chromePrefs.get(CONFIG_AUTO_DOWNLOAD) != null
							&& !chromePrefs.get(CONFIG_AUTO_DOWNLOAD).toString().isEmpty()) {
						String path = chromePrefs.get(CONFIG_AUTO_DOWNLOAD).toString();
						path = new File(path).getAbsolutePath();
						chromePrefs.put(CONFIG_AUTO_DOWNLOAD, path);
					}

					options.setExperimentalOption("prefs", chromePrefs);
				}	
				options.setExperimentalOption("useAutomationExtension", false);
				_driver = (WebDriver) ctor.newInstance(options);

			} else if (driverClassName.equals(CLASS_IE_DRIVER)) {
				System.setProperty(PROPERTY_IE_DRIVER, webDriverPath.toString());
				DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
				dc.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
				dc.setCapability("ignoreProtectedModeSettings", true);
				Class<?> class_ = Class.forName(driverClassName);
				Constructor<?> ctor = class_.getConstructor(Capabilities.class);
				_driver = (WebDriver) ctor.newInstance(dc);
				_driver.manage().window().maximize();
			}
     
        }
        else {
            String driverRemoteAddress = driverRemoteAddressObj.toString();
            DesiredCapabilities desiredCapabilities = new DesiredCapabilities();            
            Map<?,?> desiredCapabilitiesParams = (Map<?,?>)_agentParams.get(CONFIG_DESIRED_CAPABILITIES);         
            desiredCapabilities.setBrowserName(desiredCapabilitiesParams.get(CONFIG_DESIRED_CAPABILITIES_BROWSER_NAME).toString());            
            switch((desiredCapabilitiesParams.get(CONFIG_DESIRED_CAPABILITIES_PLATFORM)).toString().toLowerCase()){
            case "linux":
            	desiredCapabilities.setPlatform(Platform.LINUX);
            	break;
            case "windows":
            	desiredCapabilities.setPlatform(Platform.WINDOWS);
            	break;    
            default:
            	desiredCapabilities.setPlatform(Platform.ANY);
            	break;
            }            
            Class<?> class_ = Class.forName(driverClassName);
            Constructor<?> ctor = class_.getConstructor(URL.class, Capabilities.class);
            _driver= (WebDriver) ctor.newInstance(new URL(driverRemoteAddress),desiredCapabilities );
            _driver.manage().window().maximize();
        }

        return _driver;
    }
    
    protected String getRepoElementValue(String name) throws Exception {
        // "name" can be page name, can be current page's control can be page.control
		int dotpos = name.indexOf('.');
		String pageName;
		String controlName;
		if (dotpos < 0) { // no dot => use currentPage
			pageName = currentPage;
			controlName = name;
		}
        else {
            pageName = name.substring(0,dotpos);
            controlName = name.substring(dotpos+1);
        }

		Map<?,?> pageControlsRepo = (Map<?,?>)repo.get(pageName);
        if(pageControlsRepo == null){
        	throw new Exception(String.format("Cannot find control repo for page %s", pageName));
        }
        return pageControlsRepo.get(controlName).toString();
    }
    public CommonStepResult defineLocationVariable(String name,String value) throws Exception{
    	String path = getRepoElementValue(name);
    	String  xpath=path;
		if(path.contains(LOCATION_VARIABLE_NAME))
    		xpath = path.replaceAll(LOCATION_VARIABLE_NAME, value);
		CommonStepResult result = new CommonStepResult();
		result.setFieldValue(xpath);
		return result;
    }
    @Override
    public void close() throws Exception {
        if(_driver != null) {
            _driver.quit();            
            _driver = null;
        }
    }
    /**
     * <p>Sets the time out duration for webBrowserAgent.
     * @param timeout the timeout duration in seconds
     * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#setTimeout(String, int)	 
     * @see fast.common.agents.UiaAgent#setTimeout(int)
     */
    public void setTimeout(int timeout) {
        _timeout = timeout;
    }

    @Override
	protected void afterException() throws Exception {
		if(headless)
			return;
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String fileName = dateFormat.format(date) + ".png";
		File scrFile = ((TakesScreenshot) getOrCreateWebDriver()).getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(scrFile, new File(".\\ScreenShots\\" + fileName));
		
	}
    /**
     * Checks whether a string is a Xpath format
     * @param s Xpath
     * @return true or false
     * <p>Example:
	 * <blockquote><pre>webBrowserAgent.isXpath("//*[@id="logon-form"]/input[1]");</pre></blockquote>
	 * @since 1.5
     */
  	public boolean isXpath(String s) {
		return s.startsWith("/") || s.startsWith("//");
	}
  	
  	/**
  	 * Gets currently used web driver 
  	 * @return web driver
  	 * <p>Required to specify driverClassName and webDriverPath</p>
  	 * <p>Example in configuration file:
	 * <pre>&nbsp;&nbsp;driverClassName: 'org.openqa.selenium.ie.InternetExplorerDriver'</pre>
     * <pre>&nbsp;&nbsp;webDriverPath: drivers/webdriver/IEDriverServer.exe</pre>
  	 * @throws Exception
  	 * @since 1.5
  	 * @see #setDriver(WebDriver)
  	 */
  
    public WebDriver getDriver() throws Exception {
    	return getOrCreateWebDriver();
    }
    /**
     * Sets specified driver
     * @param driver given driver
     * @since 1.5
     * @see #getDriver()
     */
    public void setDriver(WebDriver driver){
    	this._driver = driver;
    }

    /**
     * Takes screenshot for UI and saves screenshot in folder "ScreenShots" under project
     * <p>For Web Browser Automation, current screenshot will be captured and stored if there is an exception</p>
     * @param TestCaseName test case name
     * @return a CommonStepResult with screenShots path 
     * @throws Exception
     * @see fast.common.glue.GuiCommonStepDefs#createScreenshot(String, String, String)
     * @since 1.5
     */
	public StepResult captureScreenshot(String TestCaseName) throws Exception {
		String screenshot = "";
		try {
			String time = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_a")
					.format(new Date());
			File scrFile = ((TakesScreenshot) getOrCreateWebDriver())
					.getScreenshotAs(OutputType.FILE);
			new File(System.getProperty("user.dir") + "\\ScreenShots\\").mkdir();
			screenshot = System.getProperty("user.dir")
					+ "\\ScreenShots\\" + TestCaseName + time + ".png";
			FileUtils.copyFile(scrFile, new File(screenshot));
			_logger.info("ScreenShot is created into file "+screenshot);				
		} catch (IOException e) {
			_logger.error(e.getMessage());
		}
		CommonStepResult result = new CommonStepResult();
		result.setFieldValue(screenshot);
		return result;
	}

	/**
	 * Opens the specified web page with the given url.
	 * @param url the address of the web page to open
	 * @throws Exception
	 * @see fast.common.glue.GuiCommonStepDefs#openUrl(String, String)
	 * @since 1.5
	 */
    public void openUrl(String url) throws Exception {
        getOrCreateWebDriver().get(url);
    }
    
    /**
     * Gets url of current page
     * @return a CommonStepResult with url
     * @throws Exception
     * @see fast.common.glue.GuiCommonStepDefs#getCurrentUrl(String)
     * @since 1.5
     */
    public StepResult getCurrentUrl() throws Exception {
        String value = getOrCreateWebDriver().getCurrentUrl();
        CommonStepResult result = new CommonStepResult();
		result.setFieldValue(value);
		return result;
    }
    
    /**
     * Gets the innerText of a web element
     * @param controlName the name of the specified web element
     * @return a string format content of a web element
     * @throws Exception
     * @since 1.5
     * @see #readTextOnControl(String)
     */
    public String getText(String controlName) throws Exception {
        String xpath = getRepoElementValue(controlName);
        WebElement el = _waitElementVisible(xpath);
        return el.getText();
    }


    private WebElement _waitElementVisible(String xpath) throws Exception {
        WebDriverWait wait = new WebDriverWait(getOrCreateWebDriver(), (long)_timeout);
		WebElement el = null;
		if (isXpath(xpath)) {
			el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
		} else {
			el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(xpath)));
		}
		return el;
    }

    private WebElement _waitElementClickable(String xpath) throws Exception {
        WebDriverWait wait = new WebDriverWait(getOrCreateWebDriver(), _timeout);
        WebElement el = null;
		if (isXpath(xpath)) {
        	el = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        } else {
			el = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(xpath)));
		}
        return el;
    }
    
	/**
	 * Opens the specified web page with the given url
	 * @param pageName url of a specified web
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#onPage(String, String, String)
	 */
    public void onPage(String pageName) throws Exception {
        String controlName = pageName + ".trait";
        String xpath = getRepoElementValue(controlName);
        _waitElementVisible(xpath);
        currentPage = pageName;
    }
    
    /**
     * Checks whether a web element can be found in the current page
     * @param controlName the name of the specified web element
     * @throws Exception
     * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#seeControl(String, String)
     */
    public void seeControl(String controlName) throws Exception {
        String xpath = getRepoElementValue(controlName);
        _waitElementVisible(xpath);
    }

    /**
     * Left click on the specified web element 
     * @param controlName the name of the specified web element
     * @throws Exception
     * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#clickControl(String, String)
     * @see fast.common.agents.UiaAgent#clickControl(String)
     */
    public void clickControl(String controlName) throws Exception {
        String xpath = getRepoElementValue(controlName);
        WebElement el = null;
		try {
			el = _waitElementClickable(xpath);
		} catch (Exception ex) {
			_logger.error("Exception occurs when clickControl. " + ex.getMessage());
			throw ex;
		}
        if( el != null ){
        	Actions actions=new Actions(_driver);
    		actions.click(el).perform();
        }		
    }
    
    /**
     * Right click on the specified web element 
     * @param controlName the name of the specified web element
     * @throws Exception
     * @since 1.6
     * @see fast.common.glue.GuiCommonStepDefs#rightClickOnControl(String, String)
     */
    public void rightClickControl(String controlName) throws Exception {
        String xpath = getRepoElementValue(controlName);
        WebElement el = null;
		try {
			el = _waitElementClickable(xpath);
		} catch (Exception ex) {
			_logger.error("Exception occurs when rightClickControl. " + ex.getMessage());
			throw ex;
		}
        if( el != null ){
        	Actions actions=new Actions(_driver);
    		actions.contextClick(el).perform();
        }
    }
    
    /**
     * Double click on the specified web element 
     * @param controlName the name of the specified web element
     * @throws Exception
     * @since 1.6
     * @see fast.common.glue.GuiCommonStepDefs#doubleClickOnControl(String, String)
     */
    public void doubleClickControl(String controlName) throws Exception {
        String xpath = getRepoElementValue(controlName);
        WebElement el = null;
		try {
			el = _waitElementClickable(xpath);
		} catch (Exception ex) {
			_logger.error("Exception occurs when doubleClickControl. " + ex.getMessage());
			throw ex;
		}
        if( el != null ){
        	Actions actions=new Actions(_driver);
    		actions.doubleClick(el).perform();
        }
    }
    /**
     * Types the given text into the specified web element
     * @param text context to type
     * @param controlName the name of the specified web element
     * @throws Exception
     * @since 1.5
     * @see  fast.common.glue.GuiCommonStepDefs#typeTextIntoControl(String, String, String)
     */
    public void typeTextIntoControl(String text, String controlName) throws Exception {
        String xpath = getRepoElementValue(controlName);
        WebElement el = _waitElementVisible(xpath);
        el.sendKeys(text);
    }
    /**
     * Checks whether the given text is visible in the specified web element
     * @param text the given context
     * @param controlName the name of the specified web element
     * @throws Exception
     * @since 1.5
     * @see  fast.common.glue.GuiCommonStepDefs#seeTextInControl(String, String, String)
     */
    public void seeTextInControl(String text, String controlName) throws Exception {
        String xpath = getRepoElementValue(controlName);
        xpath = xpath + String.format("[contains(text(), '%s')]", text);
        _waitElementVisible(xpath);
    }
    
    /**
     * Checks whether the given text is a cell value in the specified data grid
     * @param text the given context
     * @param controlName the name of data grid
     * @param cellAttributeName the name of the attribute of a cell
     * @param cellAttributeValue the value of the attribute of a cell
     * @throws Exception
     * @since 1.5
     */
    public void seeCellValueInDataGridRow(String text, String controlName, String cellAttributeName, String cellAttributeValue) throws Exception {
    	String xpath = getRepoElementValue(controlName);
    	WebElement e = _waitElementVisible(xpath);    
    	List<WebElement> children = e.findElements(By.tagName("div"));
    	    	
    	for(int i=0; i< children.size(); i++){
    		WebElement child = children.get(i);
    		if( child.getAttribute(cellAttributeName) == cellAttributeValue){
    			if(child.getText() != text){
    				throw new Exception("cell value is not matched");
    			}
    			else {
    				return;
    			}
    		}
    	}
    	
    	throw new Exception("cell is not found");
    }
    /**
     * Click alert button
     * @param alertButtonName the name of the alert button
     * @throws Exception
     * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#checkClickAlert(String, String)
     */
	public void checkClickAlert(String alertButtonName) throws Exception {
		String xpath = getRepoElementValue(alertButtonName);
		WebElement el = null;
		try {
			el = _waitElementVisible(xpath);
		} catch (Exception ex) {
			_logger.error("Exception occurs when checkClickAlert. " + ex.getMessage());
		}
		if (el != null)
			el.click();
	}
	/**
	 * Clears the content of the specified control
	 * @param controlName the name of the specified element to clear
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#clearInputControl(String, String)
	 */
    public void clearInputControl(String controlName) throws Exception {
        
        String xpath = getRepoElementValue(controlName);
        WebElement el = _waitElementVisible(xpath);
        el.clear();
       
    }
    /**
     * Switch to a specified iframe
     * @param frameName the name of a iframe to switch to
     * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#switchFrame(String, String)
     */
	public void switchFrame(String frameName) {
		_driver.switchTo().frame(frameName);
	}
	/**
	 * Switch to default content from iframe
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#switchToDefault(String)
	 */
	public void switchToDefault() {
		_driver.switchTo().defaultContent();
	}
	/**
	 * Moves the mouse to a specified element
	 * @param controlName the element to move to
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#moveTo(String, String)
	 */
    public void moveTo(String controlName) throws Exception{
		String xpath = (isXpath(controlName)) ? controlName : getRepoElementValue(controlName);
        WebElement el = _waitElementVisible(xpath);
        Actions action = new Actions(_driver);
        action.moveToElement(el).perform();	    
    
    }
    /**
     * Click a control by given css selector
     * @param controlName the name of the web element to click
     * @throws Exception
     * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#clickControlByCss(String, String)
     */
    public void clickControlByCss(String controlName) throws Exception{
    	String css = getRepoElementValue(controlName);
        WebElement el =_driver.findElement(By.cssSelector(css));
        el.click();
    }
    /**
     * Selects an item by a given value from the specified control
     * @param controlName the name of the web element to select
     * @param optionValue the value of item to be selected
     * @throws Exception
     * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#selectItem(String, String, String)
     */
    public void selectItem(String controlName, String optionValue) throws Exception{
    	String xpath = getRepoElementValue(controlName);
        WebElement el = _waitElementVisible(xpath);
        Select sel = new Select(el);
        sel.selectByValue(optionValue);     
    }
    /**
     * Selects an item by a given value from the specified control
     * @param controlName the name of the web element to deselect
     * @throws Exception
     * @since 1.7
     * @see fast.common.glue.GuiCommonStepDefs#deselectAllItems(String,String)
     */

    public void deselectAllItems(String controlName) throws Exception{
    	String xpath = getRepoElementValue(controlName);
        WebElement el = _waitElementVisible(xpath);
        Select sel = new Select(el);
        sel.deselectAll();   
    }
    public StepResult getFirstSelectedValue(String controlName) throws Exception{
    	String xpath = getRepoElementValue(controlName);
        WebElement el = _waitElementVisible(xpath);
        Select sel = new Select(el);
        WebElement selectedItem = sel.getFirstSelectedOption();
		CommonStepResult result = new CommonStepResult();
		result.setFieldValue(selectedItem.getText());
		return result;
    }
    
    /**
     * Select or unselect a check box on web page
     * @param controlName the name of the web element to select
     * @param flag true is to select, false is to unselect
     * @throws Exception
     * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#checkBox(String, String, String)
     * @see fast.common.glue.GuiCommonStepDefs#checkCheckBox(String, String)
     * @see fast.common.glue.GuiCommonStepDefs#uncheckCheckBox(String, String)
     */
    public void checkBox(String controlName, String flag) throws Exception{
		String xpath = getRepoElementValue(controlName);
		WebElement el = _waitElementVisible(xpath);
		boolean isChecked = el.isSelected();
		if (!String.valueOf(isChecked).equals(flag)) {
			el.click();
		}
    }
    
    /**
     * Counts the number of specified web elements with same Xpath or css selector
     * @param controlName the name of web elements to count
     * @return a CommonStepResult with elements number
     * @throws Exception
     * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#counterElement(String, String)
     */
    public StepResult counterElement(String controlName) throws Exception{
    	String xpath = getRepoElementValue(controlName);
    	List<WebElement> elements;
		if (isXpath(xpath)) {
			elements = _driver.findElements(By.xpath(xpath));
		} else {
			elements = _driver.findElements(By.cssSelector(xpath));
		}
		CommonStepResult result = new CommonStepResult();
		result.setFieldValue(String.valueOf(elements.size()));
		return result;
    }
    /**
     * Click a control with a dynamic index on web page
     * @param index the specified index to replace keyword DYNAMIC_INDEX in Xpath
     * @param controlName the name of the web element
     * @throws Throwable
     * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#clickControlWithIndex(String, String, String)
     */
    public void clickWithIndex(String index, String controlName) throws Throwable{
    	String path = getRepoElementValue(controlName);
    	String xpath = path.replaceAll(DYNAMIC_INDEX, index);
    	WebElement el = _waitElementVisible(xpath);
    	el.click();
    }

    /**
     * Click a web element contains a specified value 
     * @param value  the specified value to identify a web element
     * @param controlName the name of the specified web element
     * @throws Exception
     * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#clickWithSpecialValue(String, String, String)
     */
    public void clickWithSpecialValue(String value, String controlName) throws Exception{
    	String path = getRepoElementValue(controlName);
    	String xpath = path.replaceAll("SpecialValue", value);
    	WebElement el = _waitElementVisible(xpath);
    	el.click();
    }
    
  
    /**
     * Presses the specified hot key during the runtime
     * <p>Also supports combined hot key</p>
     * <p>If input keys are combined keys, the first key would be modifier keys, such as CONTROL, ALT, SHIFT, etc.</p>
     * @param keyName the name of the hot key to press
     * <p>Example:
     * <blockquote><pre>webBrowserAgent.pressHotKey("ENTER")</pre></blockquote>
     * <blockquote><pre>webBrowserAgent.pressHotKey("CONTROL+A")</pre></blockquote>
     * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#pressHotKey(String, String)
     */
	public void pressHotKey(String keyName) {
		Actions actions = new Actions(_driver);
		if (!keyName.contains("+")) {
			actions.sendKeys(Keys.valueOf(keyName)).perform();  
		} else {
			String[] keys = keyName.split("\\+");
			Keys firstKey = Keys.valueOf(keys[0].trim());
			String sencondPart = keys[1].trim();
			if(sencondPart.length() == 1){//text key length will only be 1 (Q,W,E,R...)				
				actions.keyDown(firstKey).sendKeys(sencondPart.toLowerCase()).keyUp(firstKey).perform();
			}else{//non-text key length will be more than 1 (SHIFT,CONTROL,SPACE...)
				Keys secondKey = Keys.valueOf(sencondPart);
				actions.keyDown(firstKey).sendKeys(secondKey).keyUp(firstKey).perform();
			}
		}
	}
	
    /**
     * Presses the specified hot key on giving control
     * <p>Also supports combined hot key</p>
     * @param keyName the name of the hot key to press
     * @param controlName the name of the control to press key on
     * <p>Example:
     * <blockquote><pre>webBrowserAgent.pressHotKeyOnControl("ENTER","searchInput")</pre></blockquote>
     * <blockquote><pre>webBrowserAgent.pressHotKeyOnControl("CONTROL+V","searchInput")</pre></blockquote>
     * @throws Exception 
     * @since 1.7
     * @see fast.common.glue.GuiCommonStepDefs#pressHotKeyOnControl(String, String, String)
     */
	public void pressHotKeyOnControl(String keyName, String controlName) throws Exception {
		pressHotKeyOnControlWithIndex(keyName,controlName,"");
	}
	
	/**
	 * Grabs a web element and drag it on a given offset of a target element
	 * @param controlName1  a web element will be moving
	 * @param controlName2 a target element
	 * @param x horizontal move offset of the target element
	 * @param y vertical move offset of the target element
	 * @throws Exception
	 * @since 1.5
	 * @see #dragAndDropTo(String, String)
     * @see fast.common.glue.GuiCommonStepDefs#dragAndDropTo(String, String, String)
     * @see fast.common.glue.GuiCommonStepDefs#dragAndDrop(String, String, String, String, String)
	 */
    public void dragAndDrop(String controlName1, String controlName2, String x, String y) throws Exception{
    	String xpath1 = getRepoElementValue(controlName1);
    	WebElement dragElement = locateElement(xpath1);
    	
    	String xpath2 = getRepoElementValue(controlName2);
    	WebElement dropElement = locateElement(xpath2);
    	org.openqa.selenium.Point initialPosition = dropElement.getLocation();
    	Actions a = new Actions(_driver);	
    	org.openqa.selenium.Point targetPosition = new org.openqa.selenium.Point(initialPosition.getX() + Integer.parseInt(x), initialPosition.getY() + Integer.parseInt(y));
    	a.dragAndDropBy(dragElement, targetPosition.getX(), targetPosition.getY()).perform();

    }
    /**
	 * Grabs a web element and drag it N times with a offset 
	 * @param controlName  a web element will be moving
	 * @param times time to drag
	 * @param x horizontal move offset
	 * @param y vertical move offset
	 * @throws Exception
	 * @since 1.7
	 * @see #dragAndDropTo(String, String)
     * @see fast.common.glue.GuiCommonStepDefs#dragNTimes(String, String, String, String, String)
	 */
	public void dragNTimes(String controlName, String times, String x, String y)
			throws Exception {
		String xpath1 = getRepoElementValue(controlName);
		WebElement dragElement = locateElement(xpath1);
		Actions a = new Actions(_driver);
		int count = Integer.parseInt(times);
		a.moveToElement(dragElement).clickAndHold(dragElement);
		for (int i = 0; i < count; i++) {
			a.moveByOffset(Integer.parseInt(x), Integer.parseInt(y))
					.pause(1000);
		}
		a.release().perform();

	}
    
    /**
     * Grabs a web element and drag it on a target area or element
     * @param controlName1 a web element will be moving 
     * @param controlName2 a target area or element
     * @throws Exception
     * @since 1.5
     * @see #dragAndDrop(String, String, String, String)
     * @see fast.common.glue.GuiCommonStepDefs#dragAndDropTo(String, String, String)
     * @see fast.common.glue.GuiCommonStepDefs#dragAndDrop(String, String, String, String, String)
     */
    public void dragAndDropTo(String controlName1, String controlName2) throws Exception{
    	String xpath1 = getRepoElementValue(controlName1);
    	WebElement dragElement = locateElement(xpath1);
    	
    	String xpath2 = getRepoElementValue(controlName2);
    	WebElement dropElement = locateElement(xpath2);
    	Actions a = new Actions(_driver);	
    	a.dragAndDrop(dragElement, dropElement).perform();
    }
    
    /**
     * Get a row index by a specified cell value and the index of the column contains the cell value
     * @param controlName the name of the specified web element
     * @param columnNumber the index of the column contains the specified cell value
     * @param rowValue a specified cell value in a row
     * @return row index
     * @throws Exception
     * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#getRowIndex(String, String, String, String)
     * @see fast.common.glue.GuiCommonStepDefs#getWebRowIndex(String, String, String, String, String)
     */
	public StepResult getRowIndex(String controlName, String columnNumber, String rowValue) throws Exception {
		String path = getRepoElementValue(controlName);
		WebElement table = locateElement(path);
		List<WebElement> rows = table.findElements(By.tagName("tr"));
		String index = null;
		for (int i = 0; i < rows.size(); i++) {
			WebElement element = getCell(rows.get(i), Integer.parseInt(columnNumber));

			if (element != null && element.getText().equals(rowValue)) {
				index = String.valueOf(i + 1);
				break;
			}
		}
		CommonStepResult result = new CommonStepResult();
		result.setFieldValue(index);
		return result;

	}
    
	private WebElement getCell(WebElement row, int cell) {
		List<WebElement> cells;
		WebElement target = null;
		if (!row.findElements(By.tagName("th")).isEmpty()) {
			cells = row.findElements(By.tagName("th"));
			target = cells.get(cell - 1);
		}
		if (!row.findElements(By.tagName("td")).isEmpty()) {
			cells = row.findElements(By.tagName("td"));
			target = cells.get(cell - 1);
		}
		return target;

	}
	/**
	 * Moves scroll bar until to see the specified web element 
	 * @param controlName the specified web element 
	 * @param offset the offset relative to the web element
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#scrollTo(String, String, String)
	 */
	public void scrollTo(String controlName, String offset) throws Exception {
		String path = getRepoElementValue(controlName);
		WebElement el = locateElement(path);
		Actions actions = new Actions(_driver);
		actions.dragAndDropBy(el, 0, Integer.parseInt(offset)).perform();
	}    
	
	public String getDocumentReadyState(){
		JavascriptExecutor js = (JavascriptExecutor) _driver;				
		String getStateScript = "return document.readyState";
		return (String) js.executeScript(getStateScript);
	}

	public void waitDocumentReady(int retryMS, int _timeout) throws InterruptedException {
		int retries = 0;
		String state = null;
		do{
			if(retries * retryMS > _timeout * 1000){
				throw new TimeoutException();
			}else{
				Thread.sleep(retryMS);
				state = getDocumentReadyState();
			}
			retries ++;
		}while( !state.equals("complete") );
	}
	/**
	 * Scroll page to specified position
	 * @param widthOffset width offset
	 * @param heightOffset height offset
	 * @throws InterruptedException
	 * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#scrollPage(String, int, int)
	 */
	public void scrollPage(int widthOffset, int heightOffset) throws InterruptedException {
		JavascriptExecutor js = (JavascriptExecutor) _driver;				
		waitDocumentReady(100,_timeout);	
		String script = "window.scrollTo(" + widthOffset + "," + heightOffset + ")";	
		js.executeScript(script);
	}
	
	 /**
     * Performs click action on the specified web element without check whether the element is clickable.
     * @param controlName the name of the specified web element
     * <p>Example:
     * <blockquote><pre>webBrowserAgent.clickControlOnHidden("searchButton")</pre></blockquote>
     * @throws Exception
     * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#clickControlOnHiddenElement(String, String)
     */
	public void clickControlOnHidden(String controlName) throws Exception {
		String path = getRepoElementValue(controlName);
		WebElement el = locateElement(path);
		Actions actions = new Actions(_driver);
		actions.moveToElement(el).click().perform();
	}
	/**
	 * Find the first web element by Xpath or css selector
	 * @param path Xpath or css selector of a a web element
	 * @return first matching element on the current page
	 * @since 1.5
	 */
	public WebElement locateElement(String path) {
		WebElement el;
		if (isXpath(path)) {
			el = _driver.findElement(By.xpath(path));
		} else {
			el = _driver.findElement(By.cssSelector(path));
		}
		return el;
	}
	/**
	 * Get attribute value of a web element
	 * @param controlName the name of the specified web element
	 * @param attributName the name of the attribute 
	 * @return attribute value
	 * <p>Example:
     * <blockquote><pre>StepResult result = webBrowserAgent.readAttributValue("usernameTextbox","name")</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#readAttributValue(String, String, String, String)
	 */
	
	public StepResult readAttributValue(String controlName, String attributName) throws Exception{
		String path = getRepoElementValue(controlName);
		WebElement el = locateElement(path);
		String value = el.getAttribute(attributName);
		CommonStepResult result = new CommonStepResult();
		result.setFieldValue(value);
		return result;
	}
	/**
	 * Checks a web element does not exist in current page
	 * @param controlName  the name of the specified web element
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#checkElementNotExist(String, String)
	 */
	public void checkElementNotExist(String controlName) throws Exception {
		String path = getRepoElementValue(controlName);
		WebElement el = null;
		try {
			el = locateElement(path);
		} catch (Exception e) {
			_logger.info("The element with path " + path + "does not exist!");
		}
		Assert.assertNull(el);
	}

	/**
	 * Checks whether a web element exists in current page
	 * @param controlName the name of the specified web element
	 * @return a CommonStepResult with the check result
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#checkElementExist(String, String)
	 */
	public StepResult CheckWhetherExist(String controlName) throws Exception {
		CommonStepResult result = new CommonStepResult();		
		try {
			String path = getRepoElementValue(controlName);	
			locateElement(path);
			result.setFieldValue("true");
			_logger.info("Found element [" + controlName + "]");
		} catch (Exception e) {
			result.setFieldValue("false");
			_logger.info("Element [" + controlName + "] does not exist!");
		}		
		return result;
	}
	/**
	 * Move back in a web browser
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#navigateBack(String)
	 */
	public void navigateBack() {
		_driver.navigate().back();
	}
	/**
	 * Refresh current page
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#refresh(String)
	 */
	public void refresh(){
		_driver.navigate().refresh();
	}
	/**
	 * Move forward in a web browser
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#forward(String)
	 */
	public void forward(){
		_driver.navigate().forward();
	}
	/**
	 * Executes local javaScript file
	 * @param scriptPath file path of script file
	 * @return execution results
	 * <p>Example:
     * <blockquote><pre>StepResult result = webBrowserAgent.generateScript("repos\testjs.js")</pre></blockquote>
	 * @throws IOException
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#generateScript(String, String, String)
	 */
	public StepResult generateScript(String scriptPath) throws IOException{
		
		File f = new File(scriptPath);
        BufferedReader br = null;
        FileReader fileReader = null;
		String ret = null;
		
		try {
			fileReader = new FileReader(f);
			br = new BufferedReader(fileReader);
			String line = null;
			StringBuilder sb = new StringBuilder((int) f.length());
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			ret = sb.toString();
		} catch (FileNotFoundException e) {
			_logger.error("File is not found with path : " + scriptPath);
		}
		finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					_logger.error("BufferedReader closed with exception");
				}
			}
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					_logger.error("BufferedReader closed with exception");
				}
			}
		}
		
		JavascriptExecutor js = (JavascriptExecutor) _driver;
		Object object = js.executeScript(ret);
		String value = object == null ? null : object.toString();
		CommonStepResult result = new CommonStepResult();
		result.setFieldValue(value);
		return result;
	}
	
	/**
     * Gets the innerText of a specified web element
     * @param controlName the name of the specified web element
     * @return a CommonStepResult with the content of a web element
     * @throws Exception
     * @since 1.5
     * @see #getText(String) 
     * @see fast.common.glue.GuiCommonStepDefs#readTextOnControl(String, String, String)
     */
	public StepResult readTextOnControl(String controlName) throws Exception{
		String path = (isXpath(controlName)) ? controlName : getRepoElementValue(controlName);
		WebElement el = locateElement(path);
		String value = el.getText();
		CommonStepResult result = new CommonStepResult();
		result.setFieldValue(value);
		return result;
	}

	/**
	 * Gets the css value of a web element
	 * @param controlName the name of the specified web element
	 * @param cssAttribute css attribute type
	 * @return a CommonStepResult with css value of the specified web element
	 * <p>Example:
     * <blockquote><pre>StepResult result=webBrowserAgent.readCssValue("usernameTextbox","font-size")</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
     * @see fast.common.glue.GuiCommonStepDefs#readCssValue(String, String, String, String)
	 */
	
	public StepResult readCssValue(String controlName, String cssAttribute) throws Exception {
		String path = getRepoElementValue(controlName);
		WebElement el = locateElement(path);
		String value = el.getCssValue(cssAttribute);

		CommonStepResult result = new CommonStepResult();
		result.setFieldValue(value);
		return result;
	}
	/**
	 * Handles alert/popup dialog with operations: accept or dismiss or show
	 * @param operation accept or dismiss or show
	 * @throws Exception
	 * @since 1.6
	 * @see fast.common.glue.GuiCommonStepDefs#alertOperation(String, String)
	 */
	public void alertOperation(String operation) throws Exception {
		Thread.sleep(500);
		Alert alert = _driver.switchTo().alert();
		
		switch(operation.toLowerCase()){
		case "accept":
			alert.accept();
			break;
		case "dismiss":
			alert.dismiss();
			break;
		case "show":
			_logger.info("Alert Message: "+alert.getText());
			break;
		default:
			_logger.error("Operation Not Supported! Please input [accept|dismiss|show].");
			throw new IllegalArgumentException("Invalid Operation! - ["+operation+"]");			
		}
	}
	
	/**
	 * create a new tab and open a new page with giving url
	 * @param url to open a new page
	 * @since 1.7
	 * @see fast.common.glue.GuiCommonStepDefs#createNewTab(String, String)
	 */
	public void createNewTab(String url) {
		JavascriptExecutor js = (JavascriptExecutor) _driver;
		String script = "window.open(\""+url+"\")";	
		js.executeScript(script);
	}
	
	/**
	 * change from current tab to next newer tab
	 * @since 1.7
	 * @see fast.common.glue.GuiCommonStepDefs#changeToNextTab(String)
	 */
	public void changeToNextTab() {
		ArrayList<String> handles = new ArrayList<>(_driver.getWindowHandles());
		if(handles.isEmpty()){
			throw new NullPointerException("No tab currently.");
		}
		int currentWindowIndex = handles.indexOf(_driver.getWindowHandle());
		if(currentWindowIndex == handles.size() - 1){
			throw new NullPointerException("No next tab.");
		}
		String nextHandler = handles.get(currentWindowIndex + 1);
		_driver.switchTo().window(nextHandler);		
	}
	
	/**
	 * change from current tab to last older tab
	 * @since 1.7
	 * @see fast.common.glue.GuiCommonStepDefs#changeToLastTab(String)
	 */
	public void changeToLastTab() {
		ArrayList<String> handles = new ArrayList<>(_driver.getWindowHandles());
		if(handles.isEmpty()){
			throw new NullPointerException("No tab currently.");
		}
		int currentWindowIndex = handles.indexOf(_driver.getWindowHandle());
		if(currentWindowIndex == 0){
			throw new NullPointerException("No last tab.");
		}
		String nextHandler = handles.get(currentWindowIndex - 1);
		_driver.switchTo().window(nextHandler);			
	}
	
	/**
	 * close current tab
	 * @since 1.7
	 * @see fast.common.glue.GuiCommonStepDefs#closeCurrentTab(String)
	 */
	public void closeCurrentTab() {
		if(_driver != null) {
            if(_driver.getWindowHandles().size() == 1){
            	_driver.quit();
            	_driver = null;
            }else{
            	_driver.close();
            }           
        }
	}

	/**
	 * Presses the specified hot key on giving control with index
	 * <p>Also supports combined hot key</p>
	 *
	 * @param keyName     the name of the hot key to press
	 * @param controlName the name of the control to press key on
	 *                    <p>Example:
	 *                    <blockquote><pre>webBrowserAgent.pressHotKeyOnControl("ENTER","searchInput","index")</pre></blockquote>
	 *                    <blockquote><pre>webBrowserAgent.pressHotKeyOnControl("CONTROL+V","searchInput","index")</pre></blockquote>
	 * @throws Exception
	 * @see fast.common.glue.GuiCommonStepDefs#pressHotKeyOnControlWithIndex(String, String, String, String)
	 * @since 1.9
	 */
	public void pressHotKeyOnControlWithIndex(String keyName, String controlName, String index) throws Exception {
		String path = (isXpath(controlName)) ? controlName : getRepoElementValue(controlName);
		String xpath = index.isEmpty() ? path :path.replaceAll(DYNAMIC_INDEX, index);
		WebElement el = locateElement(xpath);
			if (!keyName.contains("+")) {
				el.sendKeys(Keys.valueOf(keyName));
			} else {
				String[] keys = keyName.split("\\+");
				Keys firstKey = Keys.valueOf(keys[0].trim());
				String sencondPart = keys[1].trim();
				if (sencondPart.length() == 1) {//text key length will only be 1 (Q,W,E,R...)
					el.sendKeys(firstKey, sencondPart.toLowerCase());
				} else {//non-text key length will be more than 1 (SHIFT,CONTROL,SPACE...)
					Keys secondKey = Keys.valueOf(sencondPart);
					el.sendKeys(firstKey, secondKey);
				}
			}
	}

	/**
	 * Get attribute value of a web element with Index
	 *
	 * @param controlName  the name of the specified web element
	 * @param attributName the name of the attribute
	 * @return attribute value
	 * <p>Example:
	 * <blockquote><pre>StepResult result = webBrowserAgent.readAttributValue("usernameTextbox","name")</pre></blockquote>
	 * @throws Exception
	 * @see fast.common.glue.GuiCommonStepDefs#readAttributValueWithIndex(String, String, String, String,String)
	 * @since 1.9
	 */
	public StepResult readAttributValueWithIndex(String controlName, String attributName,String index) throws Exception {
		String path = getRepoElementValue(controlName);
		String xpath = path.replaceAll(DYNAMIC_INDEX, index);
		WebElement el = locateElement(xpath);
		String value = el.getAttribute(attributName);
		CommonStepResult result = new CommonStepResult();
		result.setFieldValue(value);
		return result;
	}

	/**
	 * read a control with a dynamic index on web page
	 *
	 * @param index       the specified index to replace keyword DYNAMIC_INDEX in Xpath
	 * @param controlName the name of the web element
	 * @throws Throwable
	 * @see fast.common.glue.GuiCommonStepDefs#readTextWithIndex(String, String, String, String)
	 * @since 1.9
	 */
	public StepResult readTextWithIndex(String controlName, String index) throws Throwable {
		String path = (isXpath(controlName)) ? controlName : getRepoElementValue(controlName);
		String xpath = path.replaceAll(DYNAMIC_INDEX, index);
		WebElement el = locateElement(xpath);
		String value = el.getText();
		CommonStepResult result = new CommonStepResult();
		result.setFieldValue(value);
		return result;
	}

	/**
	 * type a control with a dynamic index on web page
	 *
	 * @param index the specified index to replace keyword "dynamicIndex" in Xpath
	 * @param controlName the name of the web element
	 * @throws Throwable
	 * @see fast.common.glue.GuiCommonStepDefs#typeControlWithIndex(String, String, String, String)
	 * @since 1.9
	 */
	public void typeTextWithIndex(String index, String controlName, String text) throws Throwable {
		String path = (isXpath(controlName)) ? controlName : getRepoElementValue(controlName);
		String xpath = path.replaceAll(DYNAMIC_INDEX, index);
		WebElement el = _waitElementVisible(xpath);
		el.sendKeys(text);
	}

	/**
	 * Gets all  the innerText of a specified web element
	 *
	 * @param controlName the name of the specified web element
	 * @return a CommonStepResult with the content of a web element
	 * @throws Exception
	 * @see #getText(String)
	 * @see fast.common.glue.GuiCommonStepDefs#readAllTextOnControl(String, String)
	 * @since 1.9
	 */
	public StepResult readAllTextOnControl(String controlName) throws Exception {
		String path = (isXpath(controlName)) ? controlName : getRepoElementValue(controlName);
		ArrayList<String> values = new ArrayList<>();
		List<WebElement> elements = locateElements(path);
		for (WebElement el:elements) {
			values.add(el.getText());
		}
        WebStepResult result = new WebStepResult();
		result.setFieldValues(values);
		return result;
	}

	/**
	 * Return the list of web element by Xpath or cssselector
	 *
	 * @param path Xpath or css selector of aList webelements
	 * @return list of elements on the current page
	 * @since 1.9
	 */
	protected List<WebElement> locateElements(String path) {
		List<WebElement> elements;
		if (isXpath(path)) {
			elements = _driver.findElements(By.xpath(path));
		} else {
			elements = _driver.findElements(By.cssSelector(path));
		}
		return elements;
	}
	
	/**
     * get a check box state on web page
     * @param controlName the name of the web element 
     * @throws Exception
     * @since 1.10
     * @see fast.common.glue.GuiCommonStepDefs#getCheckBoxState(String, String, String)
     */
    public StepResult getCheckBoxState(String controlName) throws Exception{
		String xpath = getRepoElementValue(controlName);
		WebElement el = _waitElementVisible(xpath);
		boolean isChecked = el.isSelected();
		WebStepResult result = new WebStepResult();
		result.setFieldValue("Value", String.valueOf(isChecked));
		return result;
		
    }

}

