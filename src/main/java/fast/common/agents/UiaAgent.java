package fast.common.agents;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import fast.common.context.CommonStepResult;
import fast.common.context.StepResult;
import fast.common.context.UiaStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

/**
 * The {@code UiaAgent} class defines various common actions for automating
 * tests of windows desktop UI using <tt>White Driver</tt>.
 * 
 * <p>
 * The actions in this class required control information captured by <a href=
 * "https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/UIA+Spy+Tool">UIA
 * Spy Tool</a>
 * </p>
 * 
 * <p>
 * The basic actions includes: click, type, check box, ...
 * </p>
 * 
 * <p>
 * More information for using a UiaAgent can see:
 * <p>
 
 */
public class UiaAgent extends Agent {
	
	private final static String UIA_GETTEXT_PATTERN ="%s\r\nGetValue\r\nText\r\n";
	private final static String UIA_VOID_PATTERN="\r\nVoid";

	private FastLogger logger;
	private UiaDriver driver = null;

	/**
	 * Constructs a new UiaAgent with default configuration file (config.yml)
	 * and custom configuration files to fetch required parameters.
	 * 
	 * @param name
	 *            a string for naming the creating UiaAgent
	 * @param agentParams
	 *            a map to get the required parameters for creating a UiaAgent
	 * @param configurator
	 *            a Configurator instance to provide configuration info for the
	 *            actions of the UiaAgent
	 * @since 1.5
	 */
	public UiaAgent(String name, Map agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		logger = FastLogger.getLogger(String.format("%s:WhiteAgent", _name));
		_agentParams = agentParams;
	}

	protected UiaDriver getOrStartWhiteDriver() {
		if (driver != null)
			return driver;

		driver = new UiaDriver(_agentParams.get("uiadriver").toString(), _agentParams.get("uiRepo").toString(),
				_agentParams.getOrDefault("quietMode", "").toString(),
				_agentParams.getOrDefault("startUpTimeout", "0").toString());
		startWhiteDriver();

		return driver;
	}

	private void startWhiteDriver() {
		if (!driver.start()) {
			logger.info("Failed to start UIA driver");
		}
	}

	/**
	 * <p>
	 * Sets the time out duration for UiaAgent.
	 * 
	 * @param timeout
	 *            the timeout duration in seconds
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#setTimeout(String, int)
	 * @see fast.common.agents.WebBrowserAgent#setTimeout(int)
	 */
	public void setTimeout(int timeout) throws Exception {
		getOrStartWhiteDriver().run("Timeout\r\nSetValue\r\nBusyTimeout\r\n" + (timeout * 1000));
	}

	/**
	 * <p>
	 * Launches the specified application with the given path
	 * 
	 * @param appPath
	 *            the path of the application to launch
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#launchDesktopGUI(String, String)
	 */
	public void launchGui(String appPath) throws Exception {
		getOrStartWhiteDriver().run("\r\nlaunch\r\n" + appPath);
	}

	/**
	 * <p>
	 * Left click on the specified control
	 * 
	 * @param controlName
	 *            the name of the control to click on
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#clickControl(String, String)
	 * @see fast.common.agents.WebBrowserAgent#clickControl(String)
	 */
	public void clickControl(String controlName) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nClick");
	}

	/**
	 * <p>
	 * Left click on the specified control without focusing on it
	 * 
	 * @param controlName
	 *            the name of the control to click on
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#clickOnControlUnfocus(String,
	 *      String)
	 */
	public void clickControlUnfocus(String controlName) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nClick\r\nVoid\r\nunfocus");
	}

	/**
	 * <p>
	 * Double click on the specified control
	 * 
	 * @param controlName
	 *            the name of the control to click on
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#doubleClickOnControl(String,
	 *      String)
	 */
	public void doubleClickControl(String controlName) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nDoubleClick");
	}

	/**
	 * <p>
	 * Right click on the specified control
	 * 
	 * @param controlName
	 *            the name of the control to click on
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#rightClickOnControl(String,
	 *      String)
	 */
	public void rightClickControl(String controlName) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nRightClick");
	}

	/**
	 * <p>
	 * Lets the running agent to focus on the specified control
	 * 
	 * @param controlName
	 *            the name of the control to focus on
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#focusControl(String, String)
	 */
	public void focusControl(String controlName) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nFocus");
	}

	/**
	 * <p>
	 * Selects an item by the given item value from the specified control
	 * 
	 * @param controlName
	 *            the name of the control to select item
	 * @param itemName
	 *            the value of the item to select
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#selectItem(String, String,
	 *      String)
	 * @see fast.common.agents.WebBrowserAgent#selectItem(String, String)
	 */
	public void selectItem(String controlName, String itemName) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nSelect\r\n" + itemName);
	}

	/**
	 * <p>
	 * Selects an item by the given item value from the combobox
	 * 
	 * @param controlName
	 *            the name of the control to select item
	 * @param itemName
	 *            the value of the item to select
	 * @throws Exception
	 * @since 1.8
	 */
	public void selectItemInCombobox(String controlName, String itemName) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nSelect\r\nVoid\r\n" + itemName);
	}

	/**
	 * <p>
	 * Select or unselect a check box on web page
	 * 
	 * @param controlName
	 *            he name of the control to select or unselect
	 * @param flag
	 *            true or false
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#checkBox(String, String)
	 * @see fast.common.glue.GuiCommonStepDefs#checkBox(String, String, String)
	 */
	public void checkBox(String controlName, String flag) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nSetValue\r\nChecked\r\n" + flag);
	}

	/**
	 * <p>
	 * Presses the specified hot key during the runtime
	 * 
	 * @param keyName
	 *            the hot key to press
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#pressHotKey(String)
	 * @see fast.common.glue.GuiCommonStepDefs#pressHotKey(String, String)
	 */
	public void pressHotKey(String keyName) throws Exception {
		getOrStartWhiteDriver().run("\r\nHotKey\r\n" + keyName + "\r\n");
	}

	/**
	 * Checks whether a control exists in current page
	 * 
	 * @param controlName
	 *            the name of the control to check
	 * @return a StepResult with true or false
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#CheckWhetherExist(String)
	 * @see fast.common.glue.GuiCommonStepDefs#checkElementExist(String, String)
	 */
	public StepResult CheckWhetherExist(String controlName) throws Exception {
		return getOrStartWhiteDriver().run(controlName + "\r\nCheckWhetherExist\r\n\r\nVoid");
	}

	/**
	 * Checks whether a control can be seen in current page
	 * 
	 * @param controlName
	 *            the name of the control to check
	 * @return a StepResult with true or false
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#checkElementVisible(String,
	 *      String)
	 */
	public StepResult CheckWhetherVisible(String controlName) throws Exception {
		return getOrStartWhiteDriver().run(controlName + "\r\nGetValue\r\nVisible\r\n");
	}

	/**
	 * Checks whether a control is enable in current page
	 * 
	 * @param controlName
	 *            the name of the control to check
	 * @return a StepResult with true or false
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#checkControlEnabled(String,
	 *      String)
	 */
	public StepResult checkControlEnabled(String controlName) throws Exception {
		return getOrStartWhiteDriver().run(controlName + "\r\nGetValue\r\nEnabled\r\n");
	}

	/**
	 * <p>
	 * Defines the value for the dynamic index or name variable
	 * 
	 * @param vName
	 *            the dynamic index or name, set up by uia spy tool
	 * @param vValue
	 *            the value to set for the dynamic index or name
	 * @return a UiaStepResult
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#defineVarible(String, String,
	 *      String)
	 */
	public StepResult defineVariable(String vName, String vValue) throws Exception {
		return getOrStartWhiteDriver().run("\r\nDefineVariable\r\n" + vName + "\r\n" + vValue);
	}

	/**
	 * Types the given text into the specified control
	 * 
	 * @param text
	 *            give text to type
	 * @param controlName
	 *            the name of the control to type text into, referenced in the
	 *            GUI repository.
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#typeTextIntoControl(String,
	 *      String, String)
	 */
	public void typeTextIntoControl(String text, String controlName) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nSetValue\r\nText\r\n" + text);
	}

	/**
	 * Types the given date into the specified control
	 * 
	 * @param date
	 *            give date to type
	 * @param controlName
	 *            the name of the control to type date into, referenced in the
	 *            GUI repository.
	 * @throws Exception
	 * @since 1.7
	 * @see fast.common.glue.GuiCommonStepDefs#setDateIntoControl(String,
	 *      String, String)
	 */
	public void setDateIntoControl(String date, String controlName) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nSetValue\r\nDate\r\n" + date);
	}
	
	/**
	 * Checks whether the given text is visible in the specified control
	 * 
	 * @param text
	 *            the text to check
	 * @param controlName
	 *            the name of the control on which to check the text
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#seeTextInControl(String, String,
	 *      String)
	 */
	public void seeTextInControl(String text, String controlName) throws Exception {		
		UiaStepResult result = getOrStartWhiteDriver().run(String.format(UIA_GETTEXT_PATTERN, controlName));
		String actualText = null;
		if (result != null) {
			actualText = result.getFieldValue(UiaStepResult.DefaultField);
			if (actualText != null && actualText.equals(text)) {
				return;
			}
			if(actualText == null && text==null){
				return;
			}
		}

		throw new Exception(String.format("Expected text: %s, Actual text: %s", text, actualText));
	}

	/**
	 * Reads the text from the specified control
	 * 
	 * @param controlName
	 *            he name of the control to read text
	 * @return a StepResult with the text
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#readTextOnControl(String, String,
	 *      String)
	 */
	public StepResult readTextOnControl(String controlName) throws Exception {
		return getOrStartWhiteDriver().run(String.format(UIA_GETTEXT_PATTERN,controlName));
	}

	/**
	 * Counts row number for a data grid control
	 * 
	 * @param controlName
	 *            a data grid control
	 * @return a StepResult with row number
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#getRowCount(String, String,
	 *      String)
	 */
	public StepResult getRowCount(String controlName) throws Exception {
		return getOrStartWhiteDriver().run(controlName + "\r\ngetRowCount\r\n");
	}

	/**
	 * Counts column number for a data grid control
	 * 
	 * @param controlName
	 *            a data grid control
	 * @return a StepResult with column number
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#getColumnCount(String, String,
	 *      String)
	 */
	public StepResult getColumnCount(String controlName) throws Exception {
		return getOrStartWhiteDriver().run(controlName + "\r\ngetColumnCount\r\n");
	}

	public StepResult getWholeTableData(String controlName) throws Exception {
		return getOrStartWhiteDriver().run(controlName + "\r\ngetWholeTableData\r\n");
	}

	public void typeBulkTextIntoControl(String text, String controlName) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nSetValue\r\nBulkText\r\n" + text);
	}

	/**
	 * Types the given text into a win 32 combobox control
	 * 
	 * @param text
	 *            give text to type
	 * @param controlName
	 *            the name of the control to type text into, referenced in the
	 *            GUI repository.
	 * @throws Exception
	 * @since 1.5
	 */
	public void typeTextIntoWin32ComboBoxControl(String text, String controlName) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nSetValue\r\nEditableText\r\n" + text);
	}

	/**
	 * Reads the name from the specified control
	 * 
	 * @param controlName
	 *            the name of the control to read name
	 * @return a StepResult with control name
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#readNameOnControl(String, String,
	 *      String)
	 */
	public StepResult readNameOnControl(String controlName) throws Exception {
		return getOrStartWhiteDriver().run(controlName + "\r\nGetValue\r\nName\r\n");
	}

	/**
	 * Get a row index by a specified cell value and the name of the column
	 * contains the cell value
	 * 
	 * @param controlName
	 *            the name of the specified web element
	 * @param columnName
	 *            the name of the column contains the specified cell value
	 * @param columnValue
	 *            a specified cell value
	 * @return row index
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#getRowIndex(String, String,
	 *      String, String)
	 * @see fast.common.glue.GuiCommonStepDefs#getWebRowIndex(String, String,
	 *      String, String, String)
	 */
	public StepResult getRowIndex(String controlName, String columnName, String columnValue) throws Exception {
		return getOrStartWhiteDriver().run(controlName + "\r\nGetRowIndex\r\n" + columnName + "\r\n" + columnValue);
	}

	/**
	 * <p>
	 * Reads the substring with the given prefix and length from the text showed
	 * on the specified control
	 * 
	 * @param controlName
	 *            the name of the control to read text
	 * @param prefix
	 *            substring starts with this prefix
	 * @param length
	 *            the length of the substring
	 * @return a StepResult with the substring
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#readTextSubstringOnControl(String,
	 *      String, int, String, String)
	 */
	public StepResult readTextSubstringOnControl(String controlName, String prefix, int length) throws Exception {
		UiaStepResult result = getOrStartWhiteDriver().run(String.format(UIA_GETTEXT_PATTERN,  controlName));
		String text = result.getFieldValue("Value");
		int beginIndex = text.indexOf(prefix) + prefix.length();
		int endIndex = beginIndex + length;
		String substring = text.substring(beginIndex, endIndex);
		result.setFieldValue("Value", substring);
		return result;
	}

	/**
	 * Clicks on the specified cell in the data grid
	 * 
	 * @param controlName
	 *            a data grid control
	 * @param cellPosition
	 *            the index of the cell to click in the data grid, in the form
	 *            of "RowIndex,ColumnIndex"
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#clickGridCell(String, String,
	 *      String)
	 */
	public void clickGridCell(String controlName, String cellPosition) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nClick\r\n" + cellPosition + UIA_VOID_PATTERN);
	}

	/**
	 * Right clicks on the specified cell in the data grid
	 * 
	 * @param controlName
	 *            a data grid control
	 * @param cellPosition
	 *            the index of the cell to click in the data grid, in the form
	 *            of "RowIndex,ColumnIndex"
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#rightClickGridCell(String,
	 *      String, String)
	 */
	public void rightClickGridCell(String controlName, String cellPosition) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nRightClick\r\n" + cellPosition + UIA_VOID_PATTERN);
	}

	/**
	 * Set value on the specified cell in the data grid
	 * 
	 * @param controlName
	 *            a data grid control
	 * @param value
	 *            value to set in cell
	 * @param cellPosition
	 *            the index of the cell to click in the data grid, in the form
	 *            of "RowIndex,ColumnIndex"
	 * @throws Exception
	 * @since 1.7
	 * @see fast.common.glue.GuiCommonStepDefs#SetValueGridCell(String, String,
	 *      String,String)
	 */
	public void SetValueGridCell(String controlName, String value, String cellPosition) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nSetValue\r\n" + cellPosition + "\r\n" + value);
	}

	/**
	 * Clicks on the specified point on a control
	 * 
	 * @param controlName
	 *            the name of the control form which to locate the point by its
	 *            offset
	 * @param pointOffsetX
	 *            the X offset of the point
	 * @param pointOffsetY
	 *            the Y offset of the point
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#clickControlPoint(String, String,
	 *      String, String)
	 */
	public void clickControlPoint(String controlName, String pointOffsetX, String pointOffsetY) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nClickOnPoint\r\n\r\n" + pointOffsetX + "," + pointOffsetY);
	}

	/**
	 * Right clicks on the specified point on a control
	 * 
	 * @param controlName
	 *            the name of the control form which to locate the point by its
	 *            offset
	 * @param pointOffsetX
	 *            the X offset of the point
	 * @param pointOffsetY
	 *            the Y offset of the point
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#rightClickControlPoint(String,
	 *      String, String, String)
	 */
	public void rightClickControlPoint(String controlName, String pointOffsetX, String pointOffsetY) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nRightClickOnPoint\r\n\r\n" + pointOffsetX + "," + pointOffsetY);
	}

	/**
	 * Reads text from a specified cell from a data grid
	 * 
	 * @param controlName
	 *            a data grid control
	 * @param cellPosition
	 *            the index of the cell to click in the data grid, in the form
	 *            of "RowIndex,ColumnIndex"
	 * @return a StepResult with text
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.GuiCommonStepDefs#readGridCell(String, String,
	 *      String, String)
	 */
	public StepResult readGridCell(String controlName, String cellPosition) throws Exception {
		return getOrStartWhiteDriver().run(controlName + "\r\nGetValue\r\n" + cellPosition + UIA_VOID_PATTERN);
	}

	public StepResult getSpecificValue(String controlName, String columnHeader, String containsValue) throws Exception {
		return getOrStartWhiteDriver()
				.run(controlName + "\r\nGetSpecificValue\r\n" + columnHeader + "\r\n" + containsValue);
	}

	/**
	 * To fetch a row data on table
	 * 
	 * @param controlName
	 *            a data grid control
	 * @param rowIndex
	 *            the index of row
	 * @return a StepResult with map
	 * @throws Exception
	 * @since 1.6
	 * @see fast.common.glue.GuiCommonStepDefs#getTableRowData(String, int,
	 *      String, String)
	 */
	public StepResult getTableRowData(String controlName, int rowIndex) throws Exception {
		return getOrStartWhiteDriver().run(controlName+"\r\nGetRowData\r\n"+rowIndex+"\r\n");
	}

	/**
	 * To fetch a column data on table
	 * 
	 * @param controlName
	 *            a data grid control
	 * @param columnName
	 *            the name of column
	 * @return a StepResult with map
	 * @throws Exception
	 * @since 1.6
	 * @see fast.common.glue.GuiCommonStepDefs#getTableColumnData(String,
	 *      String, String, String)
	 */
	public StepResult getTableColumnData(String controlName, String columnName) throws Exception {
		return getOrStartWhiteDriver().run(controlName+"\r\nGetColumnData\r\n"+columnName+"\r\n");
	}

	/**
	 * Get property value from control
	 * 
	 * @param controlName
	 *            he name of the control to read text
	 * @return a StepResult with the text
	 * @throws Exception
	 * @since 1.7
	 * @see fast.common.glue.GuiCommonStepDefs#getPropertyOnControl(String,
	 *      String, String,String)
	 */
	public StepResult getPropertyOnControl(String controlName, String target) throws Exception {
		return getOrStartWhiteDriver().run(controlName + "\r\nGetProperty\r\n" + target + "\r\n");
	}

	/**
	 * <p>
	 * Select or unselect a radio box on web page
	 * 
	 * @param controlName
	 *            he name of the control to select or unselect
	 * @param flag
	 *            true or false
	 * @throws Exception
	 * @since 1.7
	 * @see fast.common.glue.GuiCommonStepDefs#selectRadioButton(String, String,
	 *      String)
	 */
	public void selectRadioButton(String controlName, String flag) throws Exception {
		getOrStartWhiteDriver().run(controlName + "\r\nSetValue\r\nIsSelected\r\n" + flag + "\r\n" + UIA_VOID_PATTERN);
	}

	/**
	 * To fetch headers on table
	 * 
	 * @param controlName
	 *            a data grid control
	 * @return a StepResult with map
	 * @throws Exception
	 * @since 1.7
	 * @see fast.common.glue.GuiCommonStepDefs#getTableHeaders(String, String,
	 *      String)
	 */
	public StepResult getTableHeaders(String controlName) throws Exception {
		return this.getTableRowData(controlName, 0);
	}

	/**
	 * Takes screenshot for UI and saves screenshot in folder "ScreenShots"
	 * under project
	 * <p>
	 * For UIAAgent Automation, current screenshot will be captured and stored
	 * if there is an exception
	 * </p>
	 * 
	 * @param TestCaseName
	 *            test case name
	 * @return a CommonStepResult with screenShots path
	 * @throws Exception
	 * @see fast.common.glue.GuiCommonStepDefs#createScreenshot(String, String,
	 *      String)
	 * @since 1.8
	 */
	public StepResult captureScreenshot(String TestCaseName) throws Exception {
		String screenshot = "";

		String time = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_a").format(new Date());
		new File(System.getProperty("user.dir") + "\\ScreenShots\\").mkdir();
		screenshot = System.getProperty("user.dir") + "\\ScreenShots\\" + TestCaseName + time + ".png";
		getOrStartWhiteDriver().run("\r\nTakeScreenShot\r\n"+screenshot+"\r\n");
		logger.info("ScreenShot is created into file " + screenshot);

		CommonStepResult result = new CommonStepResult();
		result.setFieldValue(screenshot);
		return result;
	}

	/**
	 * type text on the specified point on a control
	 *
	 * @param controlName
	 *            the name of the control form which to locate the point by its
	 *            offset
	 * @param pointOffsetX
	 *            the X offset of the point
	 * @param pointOffsetY
	 *            the Y offset of the point
	 * @throws Exception
	 * @since 1.9
	 * @see fast.common.glue.GuiCommonStepDefs#typeTextOnControlWithPoint(String,String, String,String, String)
	 */
	public void typeTextOnControlWithPoint(String text,String controlName, String pointOffsetX, String pointOffsetY) throws Exception {
		clickControlPoint(controlName,pointOffsetX,pointOffsetY);
		getOrStartWhiteDriver().run("\r\nENTERTEXT\r\n" + text + "\r\n");
	}
	
	/**
	 * get color on control
	 *
	 * @param controlName
	 *            the name of the control 
	 * @throws Exception
	 * @since 1.9
	 * @see fast.common.glue.GuiCommonStepDefs#getRgbColorOnControl(String,String, String)
	 */
	public UiaStepResult getColorOnControl(String controlName) throws Exception {
		return getOrStartWhiteDriver().run(controlName+"\r\nGETBORDERCOLOR\r\n\r\n");
	}
	
	/**
	 * <p>
	 * get a check box state
	 * 
	 * @param controlName
	 *            the name of the control to get state from
	 * @throws Exception
	 * @since 1.10
	 * @see fast.common.glue.GuiCommonStepDefs#getCheckBoxState(String, String, String)
	 */
	public UiaStepResult getCheckBoxState(String controlName) throws Exception {
		return getOrStartWhiteDriver().run(controlName + "\r\nGetValue\r\nChecked\r\n");
	}
	
	/**
	 * <p>
	 * get color on control point
	 *
	 * @param controlName, the name of the control 
	 * @param pointOffsetX, the left offset related to control           
	 * @param pointOffsetY, the top offset related to control
	 * @throws Exception
	 * @since 1.9
	 * @see fast.common.glue.GuiCommonStepDefs#getRgbColorOnControlPoint(String,String,String,String, String)
	 */
	public UiaStepResult getColorOnControlPoint(String controlName, String pointOffsetX, String pointOffsetY) throws Exception {
		return getOrStartWhiteDriver().run(controlName+"\r\nGETCOLOR\r\n"+pointOffsetX+","+pointOffsetY+"\r\n");
	}

	@Override
	public void close() throws Exception {
		if (driver != null) {
			driver.close();
			driver = null;
		}
	}

}

class UiaDriver {
	private static FastLogger logger = FastLogger.getLogger("UiaDriver");
	private static final String PIPE_PATH_PREFIX = "\\\\.\\pipe\\";
	private final static int DEFAULT_CONNECTION_TIMEOUT = 20;
	private String driverPath = null;
	private String uiRepo = null;
	private RandomAccessFile pipe;
	private Process driverProcess;
	private boolean isAlive = false;
	private Thread driverTh = null;
	private boolean quietMode = false;
	private int connectionTimeOut = DEFAULT_CONNECTION_TIMEOUT; 		
	
	public boolean isAlive(){
		return this.isAlive;
	}

	public UiaDriver(String driverPath, String uiRepo) {
		this.driverPath = driverPath;
		this.uiRepo = uiRepo;
	}

	public UiaDriver(String driverPath, String uiRepo, String quietMode) {
		this(driverPath, uiRepo);
		this.quietMode = quietMode != null && quietMode.toLowerCase().equals("true");
	}
	
	public UiaDriver(String driverPath, String uiRepo, String quietMode, String connectionTimeout) {
		this(driverPath, uiRepo, quietMode);
		int timeout=0;
		try{
			timeout= Integer.parseInt(connectionTimeout);
		}catch(NumberFormatException ex){
			timeout=0;
		}
		
		this.connectionTimeOut = timeout<1?DEFAULT_CONNECTION_TIMEOUT:timeout*2;
	}

	public UiaStepResult run(String uiAction) throws Exception  {
		byte[] data = uiAction.getBytes();
		pipe.write(data);
		logger.debug("Request: " + uiAction);
		StringBuilder responseMessageBuilder = new StringBuilder();
		responseMessageBuilder.append(pipe.readLine());
		while (pipe.length() > 0) {
			responseMessageBuilder.append(pipe.readLine());
		}

		logger.debug("Response: " + responseMessageBuilder.toString());

		UiaStepResult result = new UiaStepResult(responseMessageBuilder.toString());
		if (result.getStatus() == StepResult.Status.Failed) {
			throw new Exception(result.getErrorMessage());
		}

		if (result.getStatus() == StepResult.Status.Skipped) {
			throw new Exception("UI action is skipped");
		}
		return result;
	}

	public boolean start() {
		try {
			String pipeName = String.format("FAST_UIA_Pipe_%s",
					(new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date()));
			if (quietMode) {
				driverProcess = new ProcessBuilder(driverPath, pipeName).start();
				consumeWhiteDriverOutput();
			} else {
				File file = new File(driverPath);
				driverProcess = Runtime.getRuntime().exec(String.format("cmd /k start %s %s", file.getName(), pipeName),
						null, new File(file.getParent()));
			}				
			
			if (driverProcess.getClass().getName().equals("java.lang.Win32Process")
					|| driverProcess.getClass().getName().equals("java.lang.ProcessImpl")) {
				Field f = driverProcess.getClass().getDeclaredField("handle");
				f.setAccessible(true);
			}			
			
			waitPipeOpen(pipeName);
			
			run((new File(uiRepo)).getAbsolutePath());
			isAlive = true;
			return true;
		} catch (Exception e) {
			logger.error("Failed to start the UIA driver process with exception :\n" + e.getMessage());
			return false;
		}
	}
	
	private void waitPipeOpen(String pipeName) throws Exception {
		Exception e=null;
		int times=1;
		do{
			try{
				pipe = new RandomAccessFile(PIPE_PATH_PREFIX + pipeName, "rw");
				e=null;
				break;
			}
			catch(Exception ex){
				logger.warn(String.format("The %d times tempt to connect to pipe failed. Sleep 0.5s", times));
				e=ex;
				Thread.sleep(500);				
			}
		}while(times++<connectionTimeOut);
		
		if(e!=null){
			logger.error(String.format("Failed to connect to pipe %s after %d tries", pipeName, connectionTimeOut));
			throw e;
		}
	}

	private void consumeWhiteDriverOutput() {
		driverTh = new Thread(new Runnable() {
			@Override
			public void run() {
				BufferedReader br = new BufferedReader(new InputStreamReader(driverProcess.getInputStream()));
				try {
					String line =null;
					do{
						line=br.readLine();
						Thread.sleep(0);
					}while (pipe != null && line != null) ;
					br.close();
				} catch (IOException | InterruptedException e) {
					logger.info("consumeWhiteDriverOutput:" + e.getMessage());
				}
			}
		});
		driverTh.start();
	}

	public void close() {
		isAlive = false;
		try {
			if (pipe != null) {
				pipe.close();
				pipe = null;
			}
		} catch (Exception e) {
			logger.error("Failed to close the UIA driver process with exception :\n" + e.getMessage());
		}

		if (driverProcess != null)
			driverProcess.destroy();
	}
}
