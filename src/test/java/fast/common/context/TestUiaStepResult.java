package fast.common.context;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
public class TestUiaStepResult {
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
	public void testForGetColumnCells(){
		UiaStepResult result=new UiaStepResult("SUCCESS:<table><thead><th>ExDiv Date</th><th>Gross Amt</th><th>Net Amt</th><th>Pay Date</th><th>Announcement Date</th><th>CloseOfBooks Date</th><th>Pay Ccy</th><th>Type</th><th>Status</th><th>Source</th><th>Action Type</th><th>Notes</th><th>Announced</th><th>SMCP ID</th></thead><tbody><tr><td>12/27/2022</td><td>0.1594</td><td></td><td></td><td>02/04/2022</td><td></td><td>SEK</td><td></td><td></td><td>BDVD</td><td></td><td></td><td></td><td></td></tr></tbody></table>");		
		List<String> columnCells=result.getColumnCells("ExDiv Date");
		
		assertEquals(1, columnCells.size());
	}
	
	@Test
	public void testForGetRowCells(){
		UiaStepResult result=new UiaStepResult("SUCCESS:<table><thead><th>ExDiv Date</th></thead><tbody><tr><td>03/22/2023</td></tr><tr><td>12/27/2022</td></tr><tr><td>09/22/2022</td></tr><tr><td>06/23/2022</td></tr><tr><td>03/23/2022</td></tr><tr><td>12/28/2021</td></tr><tr><td>09/23/2021</td></tr><tr><td>06/24/2021</td></tr><tr><td>03/24/2021</td></tr><tr><td>12/28/2020</td></tr><tr><td>09/24/2020</td></tr><tr><td>06/25/2020</td></tr><tr><td>03/25/2020</td></tr><tr><td>12/27/2019</td></tr><tr><td>09/26/2019</td></tr><tr><td>06/27/2019</td></tr><tr><td>03/28/2019</td></tr><tr><td>12/27/2018</td></tr><tr><td>09/27/2018</td></tr></tbody></table>");
		List<String> rowCells=result.getRowCells(1);
		assertEquals(1, rowCells.size());
	}
	
	
	@Test
	public void testForGetHeaders(){
		UiaStepResult result=new UiaStepResult("SUCCESS:<table><thead><th>ExDiv Date</th><th>Gross Amt</th><th>Net Amt</th><th>Pay Date</th><th>Announcement Date</th><th>CloseOfBooks Date</th><th>Pay Ccy</th><th>Type</th><th>Status</th><th>Source</th><th>Action Type</th><th>Notes</th><th>Announced</th><th>SMCP ID</th></thead><tbody><tr><td>12/27/2022</td><td>0.1594</td><td></td><td></td><td>02/04/2022</td><td></td><td>SEK</td><td></td><td></td><td>BDVD</td><td></td><td></td><td></td><td></td></tr></tbody></table>");		
		List<String> headers=result.getHeaders();
		
		assertEquals(14, headers.size());
	}

	
	@Test
	public void testForGetHeader(){
		UiaStepResult result=new UiaStepResult("SUCCESS:<table><thead><th>ExDiv Date</th><th>Gross Amt</th><th>Net Amt</th><th>Pay Date</th><th>Announcement Date</th><th>CloseOfBooks Date</th><th>Pay Ccy</th><th>Type</th><th>Status</th><th>Source</th><th>Action Type</th><th>Notes</th><th>Announced</th><th>SMCP ID</th></thead><tbody><tr><td>12/27/2022</td><td>0.1594</td><td></td><td></td><td>02/04/2022</td><td></td><td>SEK</td><td></td><td></td><td>BDVD</td><td></td><td></td><td></td><td></td></tr></tbody></table>");		
		String header=result.getHeader(2);
		
		assertEquals("Gross Amt", header);
	}
	
	@Test
	public void testForGetRowCount(){
		UiaStepResult result=new UiaStepResult("SUCCESS:<table><thead><th>ExDiv Date</th><th>Gross Amt</th><th>Net Amt</th><th>Pay Date</th><th>Announcement Date</th><th>CloseOfBooks Date</th><th>Pay Ccy</th><th>Type</th><th>Status</th><th>Source</th><th>Action Type</th><th>Notes</th><th>Announced</th><th>SMCP ID</th></thead><tbody><tr><td>12/27/2022</td><td>0.1594</td><td></td><td></td><td>02/04/2022</td><td></td><td>SEK</td><td></td><td></td><td>BDVD</td><td></td><td></td><td></td><td></td></tr></tbody></table>");
		assertEquals(1, result.getTableRowCount());
	}
	
	@Test
	public void testForGetColumnCount(){
		UiaStepResult result=new UiaStepResult("SUCCESS:<table><thead><th>ExDiv Date</th><th>Gross Amt</th><th>Net Amt</th><th>Pay Date</th><th>Announcement Date</th><th>CloseOfBooks Date</th><th>Pay Ccy</th><th>Type</th><th>Status</th><th>Source</th><th>Action Type</th><th>Notes</th><th>Announced</th><th>SMCP ID</th></thead><tbody><tr><td>12/27/2022</td><td>0.1594</td><td></td><td></td><td>02/04/2022</td><td></td><td>SEK</td><td></td><td></td><td>BDVD</td><td></td><td></td><td></td><td></td></tr></tbody></table>");
		assertEquals(14, result.getTableColumnCount());
	}
	
	@Test
	public void testForGetCellValue() throws Throwable{
		UiaStepResult result=new UiaStepResult("SUCCESS:<table><thead><th>ExDiv Date</th><th>Gross Amt</th><th>Net Amt</th><th>Pay Date</th><th>Announcement Date</th><th>CloseOfBooks Date</th><th>Pay Ccy</th><th>Type</th><th>Status</th><th>Source</th><th>Action Type</th><th>Notes</th><th>Announced</th><th>SMCP ID</th></thead><tbody><tr><td>12/27/2022</td><td>0.1594</td><td></td><td></td><td>02/04/2022</td><td></td><td>SEK</td><td></td><td></td><td>BDVD</td><td></td><td></td><td></td><td></td></tr></tbody></table>");
		assertEquals("12/27/2022", result.getCellValue("1", "ExDiv Date"));
	}
	
	@Test
	public void testForToString(){
		UiaStepResult result=new UiaStepResult(null);
		assertNull(result.toString());
		String rawStr=any(String.class);
		result=new UiaStepResult(rawStr);
		assertEquals(rawStr, result.toString());
	}
	
	@Test
	public void testForGetErrorMessage(){
		UiaStepResult result=new UiaStepResult(null);
		assertNull(result.getErrorMessage());
		String errorMessage=anyString();
		String rawStr="ERROR:"+errorMessage;
		result=new UiaStepResult(rawStr);
		assertEquals(null, result.getErrorMessage());
		errorMessage="error";
		rawStr="ERROR:"+errorMessage;
		result=new UiaStepResult(rawStr);
		assertEquals(errorMessage, result.getErrorMessage());
	}
	
	@Test
	public void testForSetAndGetFieldValue(){
		UiaStepResult result=new UiaStepResult(null);
		assertNull(result.getFieldValue(UiaStepResult.DefaultField));
		String retValue=any(String.class);
		String rawStr="SUCCESS:"+retValue;
		result=new UiaStepResult(rawStr);
		assertEquals("null", result.getFieldValue(UiaStepResult.DefaultField));
		retValue="success";
		rawStr="SUCCESS:"+retValue;
		result=new UiaStepResult(rawStr);
		assertEquals(retValue, result.getFieldValue(UiaStepResult.DefaultField));
		
		result.setFieldValue(UiaStepResult.DefaultField, anyString());
		assertEquals(anyString(),result.getFieldValue(UiaStepResult.DefaultField));
	}
	
	@Test
	public void testForGetFieldsValues(){
		String retValue="success";
		String rawStr="SUCCESS:"+retValue;
		UiaStepResult result=new UiaStepResult(rawStr);
		List<String> expected=new ArrayList<>();
		expected.add(retValue);
		assertEquals(result.getFieldsValues(UiaStepResult.DefaultField),expected);
	}
	
	@Test
	public void testForContains(){
		String retValue="success";
		String rawStr="SUCCESS:"+retValue;
		UiaStepResult result=new UiaStepResult(rawStr);
		Exception exception=null;
		try{
			result.contains(UiaStepResult.DefaultField);
		}catch(Exception ex){
			exception=ex;
		}
		assertNull(exception);
		
	}
	
}
