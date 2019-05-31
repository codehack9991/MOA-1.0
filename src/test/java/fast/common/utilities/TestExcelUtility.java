package fast.common.utilities;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import fast.common.htmlReport.FastReporter;
import junit.framework.Assert;

public class TestExcelUtility {
	
		@Before
		public void setUp() throws Exception {
		}
		
		@Test
		public void testGetXlDataAtRowsColumnsAsString() {
			String workbookName = "MarginCallSummaryAllReports_sg43243.xlsx";
			int[] rows = {12,13,14};
			int[] cols = {2};
			ArrayList<String> testData = ExcelUtility.getXlDataAtRowsColumnsAsString(
					System.getProperty("user.dir") + "/src/test/resources/fast/common/utilities/resources/" + workbookName,
					"MarginCallSummaryAll", rows, cols);
			FastReporter.logFile(testData.toString());
			Assert.assertEquals("2018", testData.get(0)); /*True row numbers*/
		}
		
		@Test
		public void getXlRowDataAsHashMapSearchByKey(){
			String workbookName = "MarginCallSummaryAllReports_sg43243.xlsx";
			String filePath = System.getProperty("user.dir")+
					"/src/test/resources/fast/common/utilities/resources/"+workbookName;
			String sheetName = "Sheet1"; 
			Map<String, String> key_value = ExcelUtility.getXlRowDataAsHashMapSearchByKey(filePath, sheetName, "Trade ID", "26473380");
			FastReporter.logFile(Arrays.asList(key_value).toString());
			Assert.assertEquals("26473380", key_value.get("Trade ID"));
			Assert.assertEquals("2198521.4", key_value.get("MTM"));
			Assert.assertEquals("100063000",key_value.get("Notional 1"));
		}
		
		
		@Test
		public void getXlRowDataAsString(){
			String workbookName = "MarginCallSummaryAllReports_sg43243.xlsx";
			String filePath = System.getProperty("user.dir")+
					"/src/test/resources/fast/common/utilities/resources/"+workbookName;
			String sheetName = "MarginCallSummaryAll"; 
			int afterColumnNumber = 1; 
			int rowNumber = 19;
			ArrayList<String> xlClientNameList = ExcelUtility.getXlRowDataAsString(filePath, sheetName, afterColumnNumber, rowNumber);
			FastReporter.logFile(xlClientNameList.toString());
			Assert.assertEquals("5180", xlClientNameList.get(0));
			Assert.assertEquals("-631026745.54999995", xlClientNameList.get(3));
		}
		
		@Test
	    public void getXlColumnData()
	    {  
			String workbookName = "MarginCallSummaryAllReports_sg43243.xlsx";
			String gfcid = ExcelUtility.getXlColumnData(System.getProperty("user.dir") + "/src/test/resources/fast/common/utilities/resources/" + workbookName,
					"MarginCallSummaryAll",3, "DYMON ASIA MULTI-STRATEGY MASTER FUND");
			FastReporter.logFile("getXlColumnData -- "+gfcid);
			Assert.assertEquals("1019666863", gfcid);
			String netmargincall = ExcelUtility.getXlColumnData(System.getProperty("user.dir") + "/src/test/resources/fast/common/utilities/resources/" + workbookName,
					"MarginCallSummaryAll",5, "1019666863");
			FastReporter.logFile("getNetMargin -- "+netmargincall);
			Assert.assertEquals("6031848.06", netmargincall);
			netmargincall = ExcelUtility.getXlColumnData(System.getProperty("user.dir") + "/src/test/resources/fast/common/utilities/resources/" + workbookName,
					"MarginCallSummaryAll",5, "1013386583");
			FastReporter.logFile("getNetMargin -- "+netmargincall);
			Assert.assertEquals("-914220897.83", netmargincall);
	    }
		@Test
	    public void testGetXlColumnDataAsString()
	    {  
			String workbookName = "MarginCallSummaryAllReports_sg43243.xlsx";
	    	ArrayList<String> xlClientNameList = ExcelUtility.getXlColumnDataAsString(System.getProperty("user.dir") 
	    			+ "/src/test/resources/fast/common/utilities/resources/" + workbookName,
					"MarginCallSummaryAll", 11, 1);	
	    	FastReporter.logFile(xlClientNameList.toString());
	    	Assert.assertEquals("APAC TEST-QUANTEDGE GLOBAL FUND", xlClientNameList.get(1)); /*True row numbers, but list starts with 0*/
	    }
		
		
	    
		@Test
	    public void testGetExcelSheet(){
			String workbookName = "MarginCallSummaryAllReports_sg43243.xlsx";
			LinkedHashMap<Integer, List<String>> excelMap= ExcelUtility.getExcelSheet(System.getProperty("user.dir") 
	    			+ "/src/test/resources/fast/common/utilities/resources/" + workbookName, "MarginCallSummaryAll");
			FastReporter.logFile(excelMap.toString());
			Assert.assertEquals(true, excelMap.get(15).get(0).contains("BRAHMAN")); /*True row numbers*/
		}
		
		@Test
	    public void testverifyNumberOfSheetsAndName() throws InvalidFormatException, IOException{
			String pathforwb1 = System.getProperty("user.dir") 
	    			+ "/src/test/resources/fast/common/utilities/resources/10531_Margin Call Report_2018-02-22.xlsx";
	    	String pathforwb2= System.getProperty("user.dir") 
	    			+ "/src/test/resources/fast/common/utilities/resources/Copy of 10531_Margin Call Report_2018-02-22.xlsx";
	    	XSSFWorkbook wb1, wb2;
	    	wb1 = new XSSFWorkbook(new File(pathforwb1));
	    	wb2 = new XSSFWorkbook(new File(pathforwb2));
			HashMap<Boolean, ArrayList<String>> booksMap = ExcelUtility.verifyNumberOfSheetsAndName(wb1, wb2);
			FastReporter.logFile(booksMap.toString());
			Assert.assertEquals(true, booksMap.get(true).contains("Summary by Legal Entity"));
		}
		@Test
	    public void testVerifySheetPresent(){
			String workbookName = "MarginCallSummaryAllReports_sg43243.xlsx";
			if(ExcelUtility.verifySheetPresent(System.getProperty("user.dir") 
	    			+ "/src/test/resources/fast/common/utilities/resources/" + workbookName, "MarginCallSummaryAll"))
				FastReporter.logFile("Sheet present");
			
			else FastReporter.logFile("verify sheet present error");
			
			Assert.assertEquals(true, ExcelUtility.verifySheetPresent(System.getProperty("user.dir") 
	    			+ "/src/test/resources/fast/common/utilities/resources/" + workbookName, "MarginCallSummaryAll"));
		}
		@Test
	    public void testExcelFilter() throws IOException{
			ArrayList<String> filterBy = new ArrayList<String>();
			filterBy.add("CREDIT DEFAULT");
			ArrayList<Integer> cloumnIndexes = new ArrayList<Integer>();
			cloumnIndexes.add(3);
			String workbookName = "MarginCallSummaryAllReports_sg43243.xlsx";
			ExcelUtility.excelFilter(System.getProperty("user.dir") 
	    			+ "/src/test/resources/fast/common/utilities/resources/" + workbookName, "Sheet1", filterBy, cloumnIndexes, "A1", "M40");
			FastReporter.logFile("please check filtered file");
		}
		
		@Test
	    public void testgetExcelSheetByUniqueKey() throws Exception{
			String pathforwb1 = System.getProperty("user.dir") 
	    			+ "/src/test/resources/fast/common/utilities/resources/10531_Margin Call Report_2018-02-22.xlsx";
	    	XSSFWorkbook wb1;
	    	ArrayList<String> keyColumns = new ArrayList<String>();
	    	keyColumns.add("Deal ID");
	    	HashMap<String, LinkedHashMap<Integer, List<String>>> excelMap1 = ExcelUtility.getExcelSheetUsingUniqueKey(pathforwb1, "Derivative Trade Details", keyColumns, 12);
	    	FastReporter.logFile(excelMap1.toString());
	    	System.out.println();
	    	System.out.println(excelMap1.get("1040T40521").get(0));
	    	System.out.println(excelMap1.get("1040T40521").get(1));
	    	Assert.assertEquals("CBNA", excelMap1.get("1040T40521").get(14).get(0));
		}
		
		@Test
		public void loadCsv_returnedArray() throws IOException{
			String filePath = System.getProperty("user.dir") + "/src/test/resources/fast/common/utilities/sample.csv";
			ArrayList<String[]> result = ExcelUtility.loadCsv(filePath, ",", StandardCharsets.UTF_8);
			assertEquals("200.0", result.get(1)[2]);
		}
	} 

