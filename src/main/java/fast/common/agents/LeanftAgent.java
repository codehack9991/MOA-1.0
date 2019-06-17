package fast.common.agents;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.util.Map;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.TimeUnit;

import javax.swing.text.IconView;

import com.gargoylesoftware.htmlunit.javascript.host.Location;
import com.hp.lft.sdk.CheckedState;
import com.hp.lft.sdk.ClickArgs;
import com.hp.lft.sdk.Keyboard;
import com.hp.lft.sdk.Keys;
import com.hp.lft.sdk.MouseButton;
import com.hp.lft.sdk.Position;

import cucumber.api.java.lu.a;
import fast.common.context.CommonStepResult;
import fast.common.context.StepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;
import junit.framework.Assert;
import microsoft.exchange.webservices.data.core.exception.misc.ArgumentNullException;
/**
 * The {@code LeanftAgent} class defines various common actions for automating tests of windows desktop <tt>Java</tt> UI.
 * 
 * <p>The basic actions includes: click, type, check box, ...</p>

 */
public class LeanftAgent extends Agent {

	private FastLogger _logger;
	private LeanftEngine _engine = null;
	/**
     * Constructs a new <tt>LeanftAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
     *
     * @param   name a string for naming the creating LeanftAgent 
     * @param   agentParams a map to get the required parameters for creating a LeanftAgent 
     * @param   configurator a Configurator instance to provide configuration info for the actions of the LeanftAgent
     * 
     * @since 1.5
     */
	public LeanftAgent(String name, Map agentParams, Configurator configurator) throws Exception {
		super(name, agentParams, configurator);
		_logger = FastLogger.getLogger(String.format("%s:LeanftAgent", _name));
	}

	protected LeanftEngine getOrStartLeanftEngine() throws Exception {
		if (_engine != null) {
			return _engine;
		}

		String[] agentConfig = { "leanftEngine", "modelJar", "appModel"};
		for (String a : agentConfig) {
			Object value = _agentParams.get(a);
			if (value == null || value.toString().equals("")) {
				_logger.error(String.format("The value of '%s' can't be null!", a));
				throw new ArgumentNullException(a);
			}
		}
		_engine = new LeanftEngine(_agentParams.get("leanftEngine").toString(), _agentParams.get("modelJar").toString(),
				_agentParams.get("appModel").toString());
		return _engine;
	}
	/**
	 * <p>Launches the specified application with the given path
	 * @param appPath the path of the application to launch
	 * @throws Exception
	 * @since 1.5
	 * @see  fast.common.glue.GuiCommonStepDefs#launchDesktopGUI(String, String)
	 */
	public void launchGui(String appPath) throws Exception {
		getOrStartLeanftEngine();
		
		Process p = Runtime.getRuntime().exec("cmd /c tasklist ");

		BufferedReader bw = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String str = "";
		StringBuilder sb = new StringBuilder();
		while (true) {
			str = bw.readLine();
			if (str != null) {
				sb.append(str.toLowerCase());
			} else {
				break;
			}
		}
		String fileName = FileSystems.getDefault().getPath(appPath).getFileName().toString();
		String suffix = fileName.substring(fileName.lastIndexOf('.') + 1);  
		if (sb.toString().indexOf(fileName) == -1) {
			if (suffix.equals("exe") || suffix.equals("bat"))
				Runtime.getRuntime().exec(appPath);
			else if (suffix.equals("jar"))
				Runtime.getRuntime().exec("java -jar " + appPath);
		}
		
	}
	/**
	 * Starts leanft agent
	 * @throws Exception
	 */
	public void startLeanftAgent() throws Exception{
		getOrStartLeanftEngine();
	}
	/**
	 * Check whether a control exists.
	 * 
	 * @param objectPath the path of a control
	 * @return stepResult contains check result true or false
	 * @since 1.5
	 * @see  fast.common.glue.GuiCommonStepDefs#checkElementExist(String, String)
	 */
	public StepResult CheckWhetherExist(String objectPath) {
		CommonStepResult result = new CommonStepResult();
		Object s = _engine.getTestObject(objectPath);
		result.setFieldValue(String.valueOf(s != null));
		return result;
	}

	/**
	 * <p>Left click on the specified control
	 * 
	 * @param objectPath the path of the control
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#clickControl(String, String)
	 * @see fast.common.agents.WebBrowserAgent#clickControl(String)
	 */
	public void clickControl(String objectPath) throws Exception {
		_engine.execute(objectPath, "click");
	}
	/**
	 * Close a control
	 * 
	 * @param objectPath the path of the control
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#closeLftControl(String, String)
	 */
	public void closeLftControl(String objectPath) throws Exception {
		_engine.execute(objectPath, "close");
	}
	/**
	 * Presses the specified hot key by leanft agent
	 * @param objectPath the path of the specified control
	 * @param key the hot key to send
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#pressLftKey(String, String, String)
	 */
	public void pressLftKey(String objectPath, String key) throws Exception {
		_engine.execute(objectPath, "sendKeys", key);
	}
	/**
	 *<p>Reads the text from the specified control and then save it into a variable 
	 * 
	 * @param objectPath the path of the control 
	 * @return a stepResult stores text result
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#readTextOnControl(String, String, String)
	 */
	public StepResult readTextOnControl(String objectPath) {
		CommonStepResult result = new CommonStepResult();
		try {
			Object s = _engine.execute(objectPath, "getText");
			result.setFieldValue(String.valueOf(s));
		} catch (Exception e) {
			result.setErrorMessage(e.getMessage());
			_logger.error("Failed to read the text on control " + objectPath);
		}
		return result;
	}
	/**
	 * See actual text in control based on using readTextOnControl(String) method
	 * @param text the text to check
	 * @param objectPath the path of the specified control
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#seeTextInControl(String, String, String)
	 */
	public void seeTextInControl(String text, String objectPath) throws Exception {
		CommonStepResult result = (CommonStepResult) this.readTextOnControl(objectPath);
		String actualText = null;
        if (result != null){
        	actualText = result.getFieldValue(CommonStepResult.DefaultField);
        	if(actualText != null && actualText.equals(text)){
        		return ;
        	}
        }
        throw new Exception (String.format("Expected text: %s, Actual text: %s", text, actualText));
    }
	/**
	 * Types the given text into the specified control
	 * 
	 * @param value the text to type
	 * @param objectPath the path of the specified control
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#typeTextIntoControl(String, String, String)
	 */
	public void typeTextIntoControl(String value, String objectPath) throws Exception {
		_engine.execute(objectPath, "setText", value);
	}
	/**
	 * Selects an item by the given item value from the specified control
	 * 
	 * @param objectPath the path of the control to select item
	 * @param item the value of the item to select
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#selectItem(String, String, String)
	 */
	public void selectItem(String objectPath, String item) throws Exception {
		String[] itemArray = { item };

		_engine.execute(objectPath, "select", new Object[] { itemArray }, new Class<?>[] { String[].class });
	}
	/**
	 * <p>Double click on the specified control 
	 * 
	 * @param objectPath the path of the control to click
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#doubleClickOnControl(String, String)
	 */
	public void doubleClickControl(String objectPath) throws Exception {
		_engine.execute(objectPath, "doubleClick");
	}
	/**
	 * <p>Right click on the specified control 
	 * 
	 * @param objectPath the path of the control to click
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#rightClickOnControl(String, String)
	 */
	public void rightClickControl(String objectPath) throws Exception {
		_engine.execute(objectPath, "click", MouseButton.RIGHT);
	}
	/**
	 *  <p>Clicks on the specified point from its control
	 * 
	 * @param objectPath the path of the specified control
	 * @param offsetX the X offset of the point
	 * @param offsetY the Y offset of the point
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#clickControlPoint(String, String, String, String)
	 */
	public void clickControlPoint(String objectPath, String offsetX, String offsetY) throws Exception {
		Point point = new Point(Integer.parseInt(offsetX), Integer.parseInt(offsetY));
		com.hp.lft.sdk.Location location = new com.hp.lft.sdk.Location(Position.TOP_LEFT, point);
		ClickArgs clickArgs = new ClickArgs(MouseButton.LEFT, location);
		_engine.execute(objectPath, "click", clickArgs);
	}
	/**
	 * Sets the status of the specified check box
	 * <p>flag = "true", select the check box; flag = "false", unselect the check box</p>
	 * @param objectPath the path of the specified control
	 * @param flag "true" or "false" 
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#checkBox(String, String, String)
	 */
	public void checkBox(String objectPath, String flag) throws Exception {
		_engine.execute(objectPath, "setState",
				new Object[] { flag.equalsIgnoreCase(flag) ? CheckedState.CHECKED : CheckedState.UNCHECKED },
				new Class<?>[] { CheckedState.class });
	}
	/**
	 * Uses specified model
	 * 
	 * @param modelName the model to use
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#useModel(String, String)
	 */
	public void useModel(String modelName){
		_engine.setCurrentModel(modelName);
	}
	@Override
	public void close() throws Exception {
		if (_engine != null) {
			_engine.close();
			_engine = null;
		}
	}
	@Override
	public void afterException() throws Exception {
		_engine.createScreenShot();
	}
}
