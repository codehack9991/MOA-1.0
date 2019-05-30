package fast.common.agents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import fast.common.context.StepResult;
import fast.common.context.UiaStepResult;
import fast.common.core.Configurator;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TestUiaAgent {
	private UiaAgent uiaAgent = null;
	private UiaAgent uiaAgentSpy = null;
	
	@Mock
	private UiaDriver driver;

	@Before
	public void setUp() throws Exception {			
		when(driver.start()).thenReturn(true);
		
		HashMap<String, Object> agentParams = new HashMap<String, Object>();
		agentParams.put("uiadriver", "uiadriver");
		agentParams.put("uiRepo", "uiRepo");
		uiaAgent = new UiaAgent("TestUiaAgent", agentParams, Configurator.getInstance());		
		uiaAgentSpy = spy(uiaAgent);
		
		Whitebox.setInternalState(uiaAgentSpy, "driver", driver);
		when(uiaAgentSpy.getOrStartWhiteDriver()).thenReturn(driver);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void getOrStartWhiteDriver_returned(){
		uiaAgent.getOrStartWhiteDriver();
	}
	
	@Test
	public void close() throws Exception{
		doNothing().when(driver).close();
		uiaAgentSpy.close();
	}

	@Test
	public void testForSetTimeOut() throws Exception {
		when(driver.run("Timeout\r\nSetValue\r\nBusyTimeout\r\n0")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		when(driver.run(not(eq("Timeout\r\nSetValue\r\nBusyTimeout\r\n0")))).thenThrow(new IllegalArgumentException());
		try{
			uiaAgentSpy.setTimeout(0);
		}catch(Exception ex){
			fail("Exception " + ex.getClass().getName() + " is thrown");
		}
	}

	@Test
	public void launchGui_success() throws Exception {	
		when(driver.run(not(eq("\r\nlaunch\r\nappPath")))).thenThrow(new IllegalArgumentException());
		when(driver.run("\r\nlaunch\r\nappPath")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		try{
			uiaAgentSpy.launchGui("appPath");
		}catch(Exception ex){
			fail("Exception " + ex.getClass().getName() + " is thrown");
		}
	}
	
	@Test 
	public void selectItemInCombobox_passed() throws Exception{
		when(driver.run("ComboBox\r\nSelect\r\nVoid\r\nItem")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.selectItemInCombobox("ComboBox", "Item");
	}

	@Test
	public void testForClickControl() throws Exception {
		when(driver.run(not(eq("Button\r\nClick")))).thenThrow(new IllegalArgumentException());
		when(driver.run("Button\r\nClick")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.clickControl("Button");
	}

	@Test
	public void testForClickControlUnfocus() throws Exception {
		when(driver.run(not(eq("Button\r\nClick\r\nVoid\r\nunfocus")))).thenThrow(new IllegalArgumentException());
		when(driver.run("Button\r\nClick\r\nVoid\r\nunfocus")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.clickControlUnfocus("Button");		
	}

	@Test
	public void testForDoubleClickControl() throws Exception {
		when(driver.run(not(eq("Button\r\nDoubleClick")))).thenThrow(new IllegalArgumentException());
		when(driver.run("Button\r\nDoubleClick")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.doubleClickControl("Button");
	}

	@Test
	public void testForRightClickControl() throws Exception {
		when(driver.run(not(eq("Button\r\nRightClick")))).thenThrow(new IllegalArgumentException());
		when(driver.run("Button\r\nRightClick")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.rightClickControl("Button");
	}

	@Test
	public void testForFocusControl() throws Exception {
		when(driver.run(not(eq("Button\r\nFocus")))).thenThrow(new IllegalArgumentException());
		when(driver.run("Button\r\nFocus")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.focusControl("Button");
	}

	@Test
	public void testForSelectItem() throws Exception {
		when(driver.run(not(eq("list\r\nSelect\r\nfirst")))).thenThrow(new IllegalArgumentException());
		when(driver.run("list\r\nSelect\r\nfirst")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.selectItem("list", "first");
	}

	@Test
	public void testForCheckBox() throws Exception {
		when(driver.run(not(eq("CheckBox\r\nSetValue\r\nChecked\r\ntrue")))).thenThrow(new IllegalArgumentException());
		when(driver.run("CheckBox\r\nSetValue\r\nChecked\r\ntrue")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.checkBox("CheckBox", "true");
	}

	@Test
	public void testForPressHotKey() throws Exception {
		when(driver.run(not(eq("\r\nHotKey\r\nEnter\r\n")))).thenThrow(new IllegalArgumentException());
		when(driver.run("\r\nHotKey\r\nEnter\r\n")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.pressHotKey("Enter");
	}

	@Test
	public void testForChechWhetherExist() throws Throwable {
		when(driver.run("DEQFV_DataGrid_Records\r\nCheckWhetherExist\r\n\r\nVoid")).thenReturn(new UiaStepResult("SUCCESS:True"));		
		StepResult result = uiaAgentSpy.CheckWhetherExist("DEQFV_DataGrid_Records");
		assertEquals("True", result.getFieldValue("Value"));
	}

	@Test
	public void testForChechWhetherVisible() throws Throwable {
		when(driver.run("DEQFV_DataGrid_Records\r\nGetValue\r\nVisible\r\n")).thenReturn(new UiaStepResult("SUCCESS:True"));		
		StepResult result = uiaAgentSpy.CheckWhetherVisible("DEQFV_DataGrid_Records");
		assertEquals("True", result.getFieldValue("Value"));
	}

	@Test
	public void testForCheckControlEnabled() throws Throwable {
		when(driver.run("DEQFV_DataGrid_Records\r\nGetValue\r\nEnabled\r\n")).thenReturn(new UiaStepResult("SUCCESS:True"));		
		StepResult result = uiaAgentSpy.checkControlEnabled("DEQFV_DataGrid_Records");
		assertEquals("True", result.getFieldValue("Value"));
	}

	@Test
	public void testForDefineVariable() throws Throwable {		
		when(driver.run("\r\nDefineVariable\r\ntable\r\nDEQFV_DataGrid_Records")).thenReturn(new UiaStepResult("SUCCESS:True"));		
		StepResult result = uiaAgentSpy.defineVariable("table", "DEQFV_DataGrid_Records");
		assertEquals("True", result.getFieldValue("Value"));
	}

	@Test
	public void testForTypeTextIntoControl() throws Exception {
		when(driver.run(not(eq("textInput\r\nSetValue\r\nText\r\nEnter")))).thenThrow(new IllegalArgumentException());
		when(driver.run("textInput\r\nSetValue\r\nText\r\nEnter")).thenReturn(new UiaStepResult("SUCCESS:Passed"));	

		uiaAgentSpy.typeTextIntoControl("Enter", "textInput");
	}

	@Test
	public void testForTypeBulkTextIntoControl() throws Exception {
		when(driver.run(not(eq("textInput\r\nSetValue\r\nBulkText\r\nEnter")))).thenThrow(new IllegalArgumentException());
		when(driver.run("textInput\r\nSetValue\r\nBulkText\r\nEnter")).thenReturn(new UiaStepResult("SUCCESS:Passed"));

		uiaAgentSpy.typeBulkTextIntoControl("Enter", "textInput");
	}

	@Test
	public void testForSetDateIntoControl() throws Throwable {
		when(driver.run(not(eq("control\r\nSetValue\r\nDate\r\ndate")))).thenThrow(new IllegalArgumentException());
		when(driver.run("control\r\nSetValue\r\nDate\r\ndate")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		
		uiaAgentSpy.setDateIntoControl("date", "control");
	}

	@Test
	public void testForSeeTextInControl_matched() throws Exception {		
		when(driver.run("textInput\r\nGetValue\r\nText\r\n")).thenReturn(new UiaStepResult("SUCCESS:Enter"));
		uiaAgentSpy.seeTextInControl("Enter", "textInput");
		when(driver.run("textInput\r\nGetValue\r\nText\r\n")).thenReturn(new UiaStepResult("SUCCESS"));
		uiaAgentSpy.seeTextInControl(null, "textInput");
	}
	
	@Test
	public void testForSeeTextInControl_notMatched() throws Exception {		
		when(driver.run("textInput\r\nGetValue\r\nText\r\n")).thenReturn(new UiaStepResult("SUCCESS:Input"));
		try{
			uiaAgentSpy.seeTextInControl("Enter", "textInput");
			fail("Expected mismatch exception");
		}catch(Exception ex){
			assertTrue(ex.getMessage().startsWith("Expected"));
		}		
	}

	@Test
	public void testForTypeIntoWin32ComboBoxControl() throws Exception {
		when(driver.run(not(eq("Combox\r\nSetValue\r\nEditableText\r\nEnter")))).thenThrow(new IllegalArgumentException());
		when(driver.run("Combox\r\nSetValue\r\nEditableText\r\nEnter")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.typeTextIntoWin32ComboBoxControl("Enter", "Combox");
	}

	@Test
	public void clickControlPoint() throws Exception {
		when(driver.run(not(eq("controlName\r\nClickOnPoint\r\n\r\n100,100")))).thenThrow(new IllegalArgumentException());
		when(driver.run("controlName\r\nClickOnPoint\r\n\r\n100,100")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.clickControlPoint("controlName", "100", "100");		
	}
	
	@Test
	public void getSpecificValue() throws Throwable {
		when(driver.run("controlName\r\nGetSpecificValue\r\ncolName\r\nContainsValue")).thenReturn(new UiaStepResult("SUCCESS:20"));
		assertEquals("20", uiaAgentSpy.getSpecificValue("controlName", "colName", "ContainsValue").getFieldValue("Value"));		
	}
	
	@Test
	public void rightClickControlPoint() throws Exception {
		when(driver.run(not(eq("controlName\r\nRightClickOnPoint\r\n\r\n100,100")))).thenThrow(new IllegalArgumentException());
		when(driver.run("controlName\r\nRightClickOnPoint\r\n\r\n100,100")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.rightClickControlPoint("controlName", "100", "100");		
	}
	
	@Test
	public void clickDataGridCell() throws Exception {
		when(driver.run(not(eq("DataGrid\r\nClick\r\n0,0\r\nVoid")))).thenThrow(new IllegalArgumentException());
		when(driver.run("DataGrid\r\nClick\r\n0,0\r\nVoid")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.clickGridCell("DataGrid", "0,0");		
	}
	
	@Test
	public void readGridCell() throws Exception {		
		when(driver.run("DataGrid\r\nGetValue\r\n0,0\r\nVoid")).thenReturn(new UiaStepResult("SUCCESS:CellValue"));
		UiaStepResult result = (UiaStepResult) uiaAgentSpy.readGridCell("DataGrid", "0,0");
		assertEquals("CellValue", result.getFieldValue("Value"));
	}
	
	@Test
	public void setValueGridCell() throws Exception {
		when(driver.run(not(eq("DataGrid\r\nSetValue\r\n0,0\r\ncell")))).thenThrow(new IllegalArgumentException());
		when(driver.run("DataGrid\r\nSetValue\r\n0,0\r\ncell")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.SetValueGridCell("DataGrid", "cell", "0,0");
	}
	
	@Test
	public void rightClickDataGridCell() throws Exception {
		when(driver.run(not(eq("DataGrid\r\nRightClick\r\n0,0\r\nVoid")))).thenThrow(new IllegalArgumentException());
		when(driver.run("DataGrid\r\nRightClick\r\n0,0\r\nVoid")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
		uiaAgentSpy.rightClickGridCell("DataGrid", "0,0");
	}

	@Test
	public void readNameOnControl() throws Throwable {
		when(driver.run("control\r\nGetValue\r\nName\r\n")).thenReturn(new UiaStepResult("SUCCESS:DEQFV_DataGrid_Records"));
		StepResult result = uiaAgentSpy.readNameOnControl("control");
		assertEquals("DEQFV_DataGrid_Records", result.getFieldValue("Value"));
	}

	@Test
	public void getPropertyOnControl() throws Throwable {
		when(driver.run("control\r\nGetProperty\r\npropertyName\r\n")).thenReturn(new UiaStepResult("SUCCESS:DEQFV_DataGrid_Records"));		
		StepResult result = uiaAgentSpy.getPropertyOnControl("control", "propertyName");
		assertEquals("DEQFV_DataGrid_Records", result.getFieldValue("Value"));
	}

	@Test
	public void readTextOnControl() throws Throwable {
		when(driver.run(not(eq("control\r\nGetValue\r\nText\r\n")))).thenThrow(new IllegalArgumentException());
		when(driver.run("control\r\nGetValue\r\nText\r\n")).thenReturn(new UiaStepResult("SUCCESS:text"));
		StepResult result = uiaAgentSpy.readTextOnControl("control");
		assertEquals("text", result.getFieldValue("Value"));
	}

	@Test
	public void getWholeTableData() throws Throwable {
		String resultString = "SUCCESS:<table><thead><th>head1</th><th>header2</th><th>header3</th></thead>" + 
				"<tbody><tr><td>A</td><td>20</td><td>99</td></tr></tbody></table>";
		when(driver.run("DEQFV_DataGrid_Records\r\ngetWholeTableData\r\n")).thenReturn(new UiaStepResult(resultString));
		UiaStepResult result = (UiaStepResult)uiaAgentSpy.getWholeTableData("DEQFV_DataGrid_Records");
		assertEquals(1, result.getTableRowCount());
	}

	@Test
	public void getRowIndex() throws Throwable {		
		when(driver.run("DEQFV_DataGrid_Records\r\nGetRowIndex\r\ncolName\r\ncolValue")).thenReturn(new UiaStepResult("SUCCESS:2"));		
		StepResult result = uiaAgentSpy.getRowIndex("DEQFV_DataGrid_Records", "colName", "colValue");
		assertEquals("2", result.getFieldValue("Value"));
	}
	
	@Test
	public void getRowCount() throws Throwable {		
		when(driver.run("DEQFV_DataGrid_Records\r\ngetRowCount\r\n")).thenReturn(new UiaStepResult("SUCCESS:2"));		
		StepResult result = uiaAgentSpy.getRowCount("DEQFV_DataGrid_Records");
		assertEquals("2", result.getFieldValue("Value"));
	}
	
	@Test
	public void getColumnCount() throws Throwable {		
		when(driver.run("DEQFV_DataGrid_Records\r\ngetColumnCount\r\n")).thenReturn(new UiaStepResult("SUCCESS:2"));		
		StepResult result = uiaAgentSpy.getColumnCount("DEQFV_DataGrid_Records");
		assertEquals("2", result.getFieldValue("Value"));
	}

	@Test
	public void testForReadTextSubstringOnControl() throws Throwable {
		when(driver.run("control\r\nGetValue\r\nText\r\n")).thenReturn(new UiaStepResult("SUCCESS:hello world!"));
		StepResult result = uiaAgentSpy.readTextSubstringOnControl("control", "hello ", 5);
		assertEquals("world", result.getFieldValue("Value"));
	}

	@Test
	public void testForGetTableRowData() throws Throwable {		
		String resultString = "SUCCESS:<table><thead><th>Name</th><th>Age</th><th>Score</th></thead>" + 
				"<tbody><tr><td>Fast</td><td>20</td><td>99</td></tr></tbody></table>";
		when(driver.run("DEQFV_DataGrid_Records\r\nGetRowData\r\n1\r\n")).thenReturn(new UiaStepResult(resultString));
		UiaStepResult result = (UiaStepResult)uiaAgentSpy.getTableRowData("DEQFV_DataGrid_Records", 1);		
		assertEquals("99", result.getCellValue("1", "Score"));
	}

	@Test
	public void testForGetTableColumnData() throws Throwable {		
		String resultString = "SUCCESS:<table><thead><th>Name</th><th>Age</th><th>Score</th></thead>" + 
				"<tbody><tr><td>Fast</td><td>20</td><td>99</td></tr></tbody></table>";
		when(driver.run("DEQFV_DataGrid_Records\r\nGetColumnData\r\nName\r\n")).thenReturn(new UiaStepResult(resultString));
		UiaStepResult result = (UiaStepResult) uiaAgentSpy.getTableColumnData("DEQFV_DataGrid_Records", "Name");

		assertEquals("Fast", result.getCellValue("1", "Name"));
	}

	@Test
	public void testSelectRadioButton() throws Exception {
		when(driver.run(not(eq("radiobutton\r\nSetValue\r\nIsSelected\r\ntrue\r\n" + "\r\nVoid")))).thenThrow(new IllegalArgumentException());
		when(driver.run("radiobutton\r\nSetValue\r\nIsSelected\r\ntrue\r\n" + "\r\nVoid")).thenReturn(new UiaStepResult("SUCCESS:PASS"));
		uiaAgentSpy.selectRadioButton("radiobutton", "true");
	}

	@Test
	public void testForGetHeaders() throws Throwable {		
		String resultString = "SUCCESS:<table><thead><th>Name</th><th>Age</th><th>Score</th></thead>" + 
				"<tbody><tr><td>Fast</td><td>20</td><td>99</td></tr></tbody></table>";
		when(driver.run("DEQFV_DataGrid_Records\r\nGetRowData\r\n0\r\n")).thenReturn(new UiaStepResult(resultString));
		UiaStepResult result = (UiaStepResult) uiaAgentSpy.getTableHeaders("DEQFV_DataGrid_Records");

		assertEquals("[Name, Age, Score]",	result.getHeaders().toString());
	}
	
	@Test
	public void testForCaptureScreenShot_passed() throws Exception{
		when(driver.run(startsWith("\r\nTakeScreenShot\r\n"))).thenReturn(new UiaStepResult("SUCCESS"));
		StepResult result = uiaAgentSpy.captureScreenshot("testcase");
		assertNotNull(result.getFieldsValues("Value"));
	}
	@Test
	public void testForTypeTextOnControlWithPoint() throws Exception{
        when(driver.run("controlName\r\nClickOnPoint\r\n\r\n100,100")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
        uiaAgentSpy.clickControlPoint("controlName","100","100");
        when(driver.run("\r\nENTERTEXT\r\n123456\r\n")).thenReturn(new UiaStepResult("SUCCESS:Passed"));
        uiaAgentSpy.typeTextOnControlWithPoint("123456","controlName", "100", "100");
	}
	
	@Test
	public void testForGetColorOnControl() throws Throwable{
        when(driver.run("controlName\r\nGETBORDERCOLOR\r\n\r\n")).thenReturn(new UiaStepResult("SUCCESS:RGB(255,255,255)"));
        StepResult result = uiaAgentSpy.getColorOnControl("controlName");
        assertEquals("RGB(255,255,255)",result.getFieldValue("Value"));
	}
	
	@Test

	public void testForGetCheckBoxState() throws Throwable{
        when(driver.run("controlName\r\nGetValue\r\nChecked\r\n")).thenReturn(new UiaStepResult("SUCCESS:True"));
        StepResult result = uiaAgentSpy.getCheckBoxState("controlName");
        assertEquals("True",result.getFieldValue("Value"));
	}
	public void testForGetColorOnControlPoint() throws Throwable{
        when(driver.run("controlName\r\nGETCOLOR\r\n0,0\r\n")).thenReturn(new UiaStepResult("SUCCESS:RGB(255,255,255)"));
        StepResult result = uiaAgentSpy.getColorOnControlPoint("controlName","0","0");
        assertEquals("RGB(255,255,255)",result.getFieldValue("Value"));
	}
}
