package fast.common.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilterColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilters;

import com.cet.citi.automation.framework.reader.excel.ExcelFileReader;
import com.monitorjbl.xlsx.StreamingReader;

import fast.common.htmlReport.AssertionFailure;
import fast.common.htmlReport.FastReporter;
import fast.common.logging.FastLogger;

public class ExcelUtility extends ExcelFileReader {
	
	public static FastLogger logger = FastLogger.getLogger("ExcelUtilityLogger");
	
	/**
	 * @author sg43243
	 * @param filePath - full file path, including file name
	 * @param sheetName - name of the sheet
	 * @return
	 * Returns cell value at (rows, columns)
	 */
	@SuppressWarnings({"deprecation"})
	public static ArrayList<String> getXlDataAtRowsColumnsAsString(String filePath,
			String sheetName, int[] rowNumber, int[] columnNumber) {

		XSSFWorkbook workbook_xssf = null;
		Sheet sheet = null;
		Row row;
		Cell cell;
		ArrayList<String> cellValueList = new ArrayList<String>();
		InputStream excelInputStream = null;
		
		try {
			excelInputStream = new FileInputStream(filePath);
			workbook_xssf = new XSSFWorkbook(excelInputStream);
			FormulaEvaluator formulaEvaluator = workbook_xssf.getCreationHelper().createFormulaEvaluator();
			sheet = workbook_xssf.getSheet(sheetName);
			int xRows = sheet.getLastRowNum();

			/**
			 * Row Numbers and Column Numbers are arrays Iterating through rows
			 * to get the columns for that rowNumber CoulmnNumber[] combination
			 */
			if (rowNumber.length <= xRows) {
				for (int i : rowNumber) {
					row = sheet.getRow(i-1);
					for (int j : columnNumber) {
						cell = row.getCell(j);
						if (cell != null) {
							try {
								switch (formulaEvaluator.evaluate(cell).getCellType()) {
									//trying to replace deprecated methods with latest methods from the library
									case Cell.CELL_TYPE_STRING :
										cellValueList.add(cell.getStringCellValue());
										break;
									
									case Cell.CELL_TYPE_NUMERIC : 
										cellValueList.add(String.valueOf((double) cell.getNumericCellValue())
												.replaceAll("\\.0*$", ""));
										break;
									
									case Cell.CELL_TYPE_BLANK :
										cellValueList.add("null"); //places value as null if cell is blank
										break;
									
								}
							} catch (NullPointerException nullex) { //null check while retrieving excel values.
								cellValueList.add("null");
							}
						}
					}
				}
			}
		}

		catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		finally {
			try {
				if (excelInputStream != null)
					excelInputStream.close();
				
				if (workbook_xssf != null)
					workbook_xssf.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		
		return cellValueList;
	}
	
	

	/**
	 * @author pk83719
	 * @param AfterColumnNumber - ColumnNumber indexed from 0
	 * @param rowNumber - RowNumber indexed from 0
	 * 
	 */
	@SuppressWarnings("deprecation")
	public static ArrayList<String> getXlRowDataAsString(String filePath,
			String sheetName, int afterColumnNumber, int rowNumber) {
		
		XSSFWorkbook workbook_xssf=null;
		Sheet sheet = null;
		String cellValue=null;
		ArrayList<String> rowDataList = new ArrayList<String>();
		InputStream excelInputStream=null;
		
		try {
			excelInputStream = new FileInputStream(filePath);
			workbook_xssf = new XSSFWorkbook(excelInputStream);
			sheet = workbook_xssf.getSheet(sheetName);
			int columnCount = 1;
			
			try{
				Row row = sheet.getRow(rowNumber);
				for(Cell cell : row){
					if(afterColumnNumber>=columnCount){
						columnCount++;
						continue;
						}
					cell.setCellType(Cell.CELL_TYPE_STRING);
					switch (cell.getCellTypeEnum())
					{
					case STRING:
						cellValue = cell.getRichStringCellValue().toString();
						rowDataList.add(cellValue);
						break;
					default:
						break;
					}
				}
			}catch(NullPointerException nullcell){
				logger.warn("Skipping empty cell.");
				cellValue = "null";
			}
			finally {
				try {
					if (excelInputStream != null)
						excelInputStream.close();
					if(workbook_xssf != null)
						workbook_xssf.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.error(String.format("Excel sheet row not retrieved. Excel | %s | Sheet | %s | Row | %s", filePath, sheetName, rowNumber));
		}
		
		return rowDataList;
	}
	
	
	/**
	 * @author pk83719
	 * @param columnName - ColumnName(Primary Key) to search the key
	 * @param key - Key to be searched in that Column
	 * 
	 * @return HashMap of all ColumnName - Value
	 *
	 */
	@SuppressWarnings("deprecation")
	public static Map<String, String> getXlRowDataAsHashMapSearchByKey(String filePath,
			String sheetName, String columnName, String key) {
		
		XSSFWorkbook workbook_xssf=null;
		Sheet sheet = null;
		Map<String, String> rowData = new HashMap<String, String>();
		InputStream excelInputStream=null;
		
		try {
			excelInputStream = new FileInputStream(filePath);
			workbook_xssf = new XSSFWorkbook(excelInputStream);
			sheet = workbook_xssf.getSheet(sheetName);
			Row header = sheet.getRow(0);
			int columnNumberForPK = 0;
			int rowNumber = 0;
			for(Cell cell: header){
				cell.setCellType(Cell.CELL_TYPE_STRING);
				if(columnName.equals(cell.getRichStringCellValue().toString())){
					columnNumberForPK = cell.getColumnIndex();
				}
			}
			
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()){
				Row row = rowIterator.next();
				Cell cell = row.getCell(columnNumberForPK);
				if(key.equals(cell.getRichStringCellValue().toString())){
					rowNumber = cell.getRowIndex();
					break;
				}
			}
			
			Row data = sheet.getRow(rowNumber);
			
			Iterator<Cell> cellIterator = header.iterator();
			while(cellIterator.hasNext()){
				Cell headerCell = cellIterator.next();
				Cell dataCell = data.getCell(headerCell.getColumnIndex());
				dataCell.setCellType(Cell.CELL_TYPE_STRING);
				rowData.put(headerCell.getRichStringCellValue().toString(), dataCell.getRichStringCellValue().toString());
			}
			
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.error(String.format("Excel sheet row not retrieved. Excel | %s | Sheet | %s | ColumnName | %s | Key | %s", filePath, sheetName, columnName, key));
		} 
		finally {
			try {
				if (excelInputStream != null)
					excelInputStream.close();
				if (workbook_xssf != null)
					workbook_xssf.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		return rowData;
	}
	
	
	/**
	 * @author sg43243
	 * @param filePath - full file path, including file name
	 * @param sheetName - name of the sheet
	 * @return
	 * Returns cell value at (one rows, multiple columns)
	 */
	@SuppressWarnings("deprecation")
	public static ArrayList<String> getXlColumnDataAsString(String filePath,
			String sheetName, int afterRowNumber, int column) {

		XSSFWorkbook workbook_xssf=null;
		Sheet sheet = null;
		String cellValue=null;
		ArrayList<String> columnDataList = new ArrayList<String>();
		InputStream excelInputStream=null;
		try {
			excelInputStream = new FileInputStream(filePath);
			workbook_xssf = new XSSFWorkbook(excelInputStream);
			sheet = workbook_xssf.getSheet(sheetName);
			Iterator<Row> rows = sheet.rowIterator(); // Now we have rows ready from the sheet
			int rowcount=1;
			
			while (rows.hasNext()) {
				Row row = (Row) rows.next();
				if(rowcount > afterRowNumber) {
					Cell cell = row.getCell(column);
					if (cell != null) {
						try {
							switch (cell.getCellType()) {
								case Cell.CELL_TYPE_STRING :
									cellValue = (cell.getRichStringCellValue().getString());
									columnDataList.add(cellValue);
									break;
								case Cell.CELL_TYPE_BLANK :
									break;
								}
							} catch (NullPointerException nullCell) {
								logger.warn("Skipping Empty Cell");
							}
						}
					}
				else rowcount++;
			}
			
		} catch (Exception e) {
			throw new AssertionFailure(String.format("Excel sheet column not retrieved. Excel | %s | Sheet | %s | Column | %s", filePath, sheetName, column));
		}
		
		finally {
			try {
				if (excelInputStream != null)
					excelInputStream.close();
				if(workbook_xssf != null)
					workbook_xssf.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		
		return columnDataList;
	}
	
	
	
	/**
	 * 
	 * @param filePath - full file path, including file name
	 * @param sheetName - name of the sheet
	 * @param searchInColumn - column number to be searched in
	 * @param searchByKey - a key to search for a particular row 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getXlColumnData(String filePath,
			String sheetName, int searchInColumn, String searchByKey) {

		XSSFWorkbook workbook_xssf=null;
		Sheet sheet = null;
		String cellValue=null;
		String columnData = null;
		InputStream excelInputStream=null;
		
		try {
			excelInputStream = new FileInputStream(filePath);
			workbook_xssf = new XSSFWorkbook(excelInputStream);
			sheet = workbook_xssf.getSheet(sheetName);

			Iterator<Row> rows = sheet.rowIterator(); // Now we have rows ready from the sheet
			int rowcount=0;
			int saveRow=0;
			
			whilerowloop:
			while (rows.hasNext()) {
				Row row = (Row) rows.next();
				Iterator<Cell> cells = row.cellIterator();

				while (cells.hasNext()) {
					Cell cell = (Cell) cells.next();
					if (cell != null) {
						try {
							switch (cell.getCellType()) {
								case Cell.CELL_TYPE_STRING :
									cellValue = (cell.getRichStringCellValue().getString());
									if(cellValue.equalsIgnoreCase(searchByKey)) {
										saveRow=rowcount;
										break whilerowloop;
									} break;
								case Cell.CELL_TYPE_NUMERIC:
									cellValue = String.valueOf(new BigDecimal(cell.getNumericCellValue()).toPlainString());
									break;
								case Cell.CELL_TYPE_BLANK :
									break;
								}
							} catch (NullPointerException nullCell) {
								cellValue="null";
								continue;
							}
						}
				}
				rowcount++;	
			}
			
			int cellType = sheet.getRow(saveRow).getCell(searchInColumn).getCellType();
			switch(cellType) {
			
			case Cell.CELL_TYPE_STRING:
				columnData = sheet.getRow(saveRow).getCell(searchInColumn).getStringCellValue();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				columnData = String.valueOf(BigDecimal.valueOf((double) sheet.getRow(saveRow).getCell(searchInColumn).getNumericCellValue()));
				break;
			case Cell.CELL_TYPE_BLANK:
				columnData = "null";
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				columnData = String.valueOf(sheet.getRow(saveRow).getCell(searchInColumn).getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_FORMULA:
				columnData = String.valueOf(sheet.getRow(saveRow).getCell(searchInColumn).getCellFormula());
				break;
			}
						
		} catch (Exception e) {
			throw new AssertionFailure(String.format("Excel sheet cell value not retrieved for key | %s | Excel | %s | Sheet | %s | Column | %s", searchByKey, filePath, sheetName, searchInColumn));
		}
		
		finally {
			try {
				if (excelInputStream != null)
					excelInputStream.close();
				if(workbook_xssf != null)
					workbook_xssf.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		
		return columnData;
	}

	/**
	 * @param filePath- full file path, including file name
	 * @param sheetName - name of the sheet
	 * @return Map (rownumber, list of cell values in one row)
	 */
	public static LinkedHashMap<Integer, List<String>> getExcelSheet(String filePath, String sheetName) {
		
		LinkedHashMap<Integer, List<String>> excelMap = new LinkedHashMap<Integer, List<String>>();
		InputStream fileInputStream = null;
		Workbook workbook = null;
		Sheet sheet;
		Row row;
		Cell cell;

		// Used streaming reader for very large files
		try {
			fileInputStream = new FileInputStream(new File(filePath)); 
			workbook = StreamingReader.builder()
			        .rowCacheSize(10000)    // number of rows to keep in memory (defaults to 10)
			        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
			        .open(fileInputStream);
			
			
			sheet = workbook.getSheet(sheetName);

			Iterator<Row> rows = sheet.rowIterator();
			
			while (rows.hasNext()) {
				row = (Row) rows.next();
				Iterator<Cell> cells = row.cellIterator();

				List<String> excelData = new LinkedList<String>();
				while (cells.hasNext()) {
					cell = (Cell) cells.next();
					String value = getStringCellValue(cell);
					excelData.add(value);
				}
				excelMap.put(row.getRowNum()+1, excelData);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			try {
				if (fileInputStream != null)
					fileInputStream.close();
				if(workbook != null)
					workbook.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		return excelMap;
	}

	/**
	 * @param cell
	 * @return gets the cell value as "String"
	 * Converts numeric value to BigDecimal before returning as String
	 */
	@SuppressWarnings("deprecation")
	public static String getStringCellValue(Cell cell) {
		String cellValue = "";
		try {
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING :
					cellValue = cell.getStringCellValue();
					break;
				case Cell.CELL_TYPE_NUMERIC :
					double myval =  cell.getNumericCellValue();
					BigDecimal myBigVal = BigDecimal.valueOf(myval);
					cellValue = String.valueOf(myBigVal.setScale(2, BigDecimal.ROUND_HALF_EVEN));
					break;
				case Cell.CELL_TYPE_BOOLEAN :
					cellValue = (cell.getBooleanCellValue()) ? "TRUE" : "FALSE";
					break;
				case Cell.CELL_TYPE_BLANK :
					break;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return cellValue.trim();
	}

	/**
	 * @param workbookOne - Workbook type - Pass Workbook
	 * @param workbookTwo - Workbook type - Pass workbook
	 * @return Map(Boolean - check for number of sheets matches in workbooks, List of names of sheets present) 
	 */
	public static HashMap<Boolean, ArrayList<String>> verifyNumberOfSheetsAndName(Workbook workbookOne, Workbook workbookTwo) {
		boolean matches = false;
		int numSheetsWbOne, numSheetsWbTwo;
		numSheetsWbOne = workbookOne.getNumberOfSheets();
		numSheetsWbTwo = workbookTwo.getNumberOfSheets();
		int numberOfSheets;
		HashMap<Boolean, ArrayList<String>> sheetMap = new HashMap<Boolean, ArrayList<String>>();
		ArrayList<String> sheets = new ArrayList<String>();
		try {
			if (numSheetsWbOne == numSheetsWbTwo) {
				matches = true;
				numberOfSheets = numSheetsWbOne;
			}
			else 
				numberOfSheets = (numSheetsWbOne > numSheetsWbTwo) ? numSheetsWbTwo : numSheetsWbOne;
			
			for (int i = 0; i < numberOfSheets; i++) {
				if (workbookOne.getSheetAt(i).getSheetName().toString()
						.equals(workbookTwo.getSheetAt(i).getSheetName().toString())) {
					matches = true;
					sheets.add(workbookOne.getSheetAt(i).getSheetName().toString());
				}
	
				else {
					matches = false;
					sheets.add(workbookTwo.getSheetAt(i).getSheetName().toString());
				}
				
			}
			
			sheetMap.put(matches, sheets);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		finally{
			try {
				if (workbookOne != null)
					workbookOne.close();
				if(workbookTwo != null)
					workbookTwo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sheetMap;
	}
	
	/**
	 * @param filePath - path of the file including filename
	 * @param sheetName
	 * @return
	 */
	public static boolean verifySheetPresent(String filePath, String sheetName) {
		boolean isSheetPresent = false;
		Workbook workbook=null;
		InputStream excelInputStream=null;
		int numberOfSheets;
		try {
			excelInputStream = new FileInputStream(filePath);
			workbook = new XSSFWorkbook(excelInputStream);
			numberOfSheets = workbook.getNumberOfSheets();
			
			for (int i = 0; i < numberOfSheets ; i++) {
				if (workbook.getSheetAt(i).getSheetName().toString().equals(sheetName)) {
					isSheetPresent = true;
					break;
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		finally {
			try {
				if (excelInputStream != null)
					excelInputStream.close();
				if(workbook != null)
					workbook.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		
		return isSheetPresent;
	}
	
	/**
	 * Filter by the list of values at column
	 * @param filepath - full path of the file including fileName
	 * @param sheetName
	 * @param filterBy
	 * @param cloumnIndexes
	 * @param startCellReference
	 * @param endCellReference
	 * @throws IOException
	 */

	public static void excelFilter(String filepath, String sheetName, ArrayList<String> filterBy, ArrayList<Integer> cloumnIndexes, String startCellReference, String endCellReference) {
		XSSFWorkbook filterWorkbook = null;
		XSSFSheet sheet = null;
		InputStream excelInputStream = null;
		FileOutputStream fileOutputStream = null;
		
		try {
			excelInputStream = new FileInputStream(filepath);
			filterWorkbook = new XSSFWorkbook(excelInputStream);
			sheet = filterWorkbook.getSheet(sheetName);
		
		/* to be configured accordingly, depending on the sheet data */
		sheet.setAutoFilter(CellRangeAddress.valueOf(startCellReference+":"+endCellReference));
		
		CTAutoFilter sheetFilter = sheet.getCTWorksheet().getAutoFilter();
		CTFilterColumn filterColumnCT = sheetFilter.insertNewFilterColumn(0);
				
		filterColumnCT.setColId(0L);
		/* Add Multiple Filters on a Single Column */
		CTFilters listofFilters=filterColumnCT.addNewFilters();
		
		/* Add this to a list for comparison */
		for(String filterByText: filterBy) {
			CTFilter thisFilter = listofFilters.addNewFilter();
			thisFilter.setVal(filterByText);
		}
		
		XSSFRow row = null;
		/* Loop through Rows and Apply Filter */
		for (Row thisrow : sheet) {
			for (Cell thiscell : thisrow) {
				if (cloumnIndexes.contains(thiscell.getColumnIndex())
					&& !filterBy.contains(thiscell.getStringCellValue())) {
					row = (XSSFRow) thiscell.getRow();
					row.getCTRow().setHidden(true);
				}
			}
			
			if(thisrow.getRowNum() == 0)
				row.getCTRow().setHidden(false);
		}
		fileOutputStream = new FileOutputStream(new File(filepath));
		filterWorkbook.write(fileOutputStream);
		
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		finally {
			try {
				if(fileOutputStream != null)
					fileOutputStream.close();
				if(filterWorkbook != null)
					filterWorkbook.close();
				if(excelInputStream != null)
					excelInputStream.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			
		}
	}
	
	/**
	 * Takes unique values for reference
	 * Uses unique key to identify the row
	 * @param filePath - full file path, including file name
	 * @param sheetName
	 * @param keyColumn - list of column names to be used as the unique keys
	 * @param headerRow - header row (can be any row if the excel sheet has pre defined text in inital set of rows)
	 * @return
	 * @throws Exception
	 */
	public static HashMap<String, LinkedHashMap<Integer, List<String>>> getExcelSheetUsingUniqueKey(String filePath, String sheetName, ArrayList<String> keyColumn, int headerRow) throws Exception {
		HashMap<String, LinkedHashMap<Integer, List<String>>> excelOuterMap = new HashMap<String, LinkedHashMap<Integer, List<String>>>();
		LinkedHashMap<Integer, List<String>> excelInnerMap = new LinkedHashMap<Integer, List<String>>();
		Workbook workbook = null;
		Sheet sheet;
		Row row = null;
		Cell cell;
		String uniqueKey = "";
		InputStream excelInputStream = null;

		try {
			excelInputStream = new FileInputStream(new File(filePath));
			workbook = StreamingReader.builder()
			        .rowCacheSize(10000)    // number of rows to keep in memory (defaults to 10)
			        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
			        .open(excelInputStream);
			
			sheet = workbook.getSheet(sheetName);
			ArrayList<Integer> keyId = getHeaderPosition(sheet, keyColumn, headerRow);
			Iterator<Row> rows = sheet.rowIterator();
			while (rows.hasNext()) {
				row = (Row) rows.next();
				excelInnerMap = new LinkedHashMap<Integer, List<String>>();
				if(row != null) {
					Iterator<Cell> cells = row.cellIterator();
					uniqueKey="";
						for(int i : keyId) {
							try{
								uniqueKey= uniqueKey+row.getCell(i).getStringCellValue().toString();
							}
							catch(Exception e) {
								logger.error(String.format("Not able to create a unique key for row | %s | Sheet | %s", row.getRowNum(), sheetName));
							}
						}
					List<String> excelData = new LinkedList<String>();
					int cellcount=0;
					while (cells.hasNext()) {
						cell = (Cell) cells.next();
						String value = getStringCellValue(cell);
						if(value.equals("") && cellcount == 0) {
							cellcount++;
							continue;
						}
						else excelData.add(value);
						cellcount++;
					}
					
					excelInnerMap.put(row.getRowNum(), excelData);
					excelOuterMap.put(uniqueKey, excelInnerMap);
				}
				
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			try {
				if (excelInputStream != null)
					excelInputStream.close();
				if(workbook != null)
					workbook.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		return excelOuterMap;
	}
	
	/**
	 * Returns an arraylist for positions for the header columns - unique
	 * @param excelsheet
	 * @param headername
	 * @param headerRow
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public static ArrayList<Integer> getHeaderPosition(Sheet excelsheet, ArrayList<String> headername, int headerRow) throws IOException,NullPointerException {
		Row row = null;
		Cell cell;
		int count=0;
		int cellCount = 0;
		ArrayList<Integer> uniqueColumns = new ArrayList<Integer>();
		
		Iterator<Row> r = excelsheet.rowIterator();
		mainloop:
		while (r.hasNext()) {
			if(count==headerRow) {
				for(String s : headername) {
					if(row!=null){
						Iterator<Cell> cells = row.cellIterator();
						cellCount=0;
						while(cells.hasNext()) {
							try{
								cell = (Cell) cells.next();
								if(cell.getCellType() != Cell.CELL_TYPE_BLANK) {
									if(cell.getStringCellValue().equals(s)) {
										uniqueColumns.add(cellCount);
										break;
										}
									else cellCount++;
								}
							}
							catch(Exception e) {
								cellCount++;
							}
						}
					}
				}
				break mainloop;
			}
			else {
				count++;
				row = (Row) r.next();
			}
		}
		return uniqueColumns;
	}
	
	/**
	 * @author sg43243
	 * @param filePath - full file path, including file name
	 * @param sheetName - name of the sheet
	 * @return
	 * Returns cell value at (one row, all columns)
	 */
	@SuppressWarnings("deprecation")
	public static ArrayList<String> getXlRowDataAsString(String filePath,
			String sheetName, int rowNumber) {

		Workbook workbook_xssf=null;
		Sheet sheet = null;
		String cellValue=null;
		ArrayList<String> rowDataList = new ArrayList<String>();
		InputStream excelInputStream=null;
		try {
			excelInputStream = new FileInputStream(filePath);
			workbook_xssf = StreamingReader.builder()
			        .rowCacheSize(10000)    // number of rows to keep in memory (defaults to 10)
			        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
			        .open(excelInputStream);
			sheet = workbook_xssf.getSheet(sheetName);
			Iterator<Row> rows = sheet.rowIterator(); // Now we have rows ready from the sheet
			int rowcount=1;
			
			while (rows.hasNext()) {
				Row row = (Row) rows.next();
				if(rowcount == rowNumber) {
					Iterator<Cell> cells = row.cellIterator();
					while (cells.hasNext()) {
						Cell cell = (Cell) cells.next();
						if (cell != null) {
							try {
								switch (cell.getCellType()) {
									case Cell.CELL_TYPE_STRING :
										cellValue = (cell.getRichStringCellValue().getString());
										rowDataList.add(cellValue);
										break;
									case Cell.CELL_TYPE_NUMERIC:
										rowDataList.add(String.valueOf(BigDecimal.valueOf((double) cell.getNumericCellValue())));
										break;
									case Cell.CELL_TYPE_BLANK :
										break;
									}
								} catch (NullPointerException nullCell) {
									logger.warn("Skipping empty cell.");
								}
							}
						}
					break;
					}
				else rowcount++;
			}
			
		} catch (Exception e) {
			throw new AssertionFailure(String.format("Excel sheet row not retrieved. Excel | %s | Sheet | %s | Row | %s", filePath, sheetName, rowNumber));
		}
		finally {
			try {
				if (excelInputStream != null)
					excelInputStream.close();
				if(workbook_xssf != null)
					workbook_xssf.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		
		return rowDataList;
	}
	
	/**
	 * <p> load CSV file to memory
	 * @param fileName - full CSV file path, including file name
	 * @param separator - separator of columns
	 * @return ArrayList<String[]>
	 * @throws IOException 
	 */
	public static ArrayList<String[]> loadCsv(String fileName, String separator, Charset charset) throws IOException{
		ArrayList<String[]> result = new ArrayList<String[]>();
		BufferedReader reader = null;
		try{
			reader = Files.newBufferedReader(FileSystems.getDefault().getPath(fileName), charset);
			while(reader.ready()){
				String line = reader.readLine();
				String[] fields = line.split(separator);
				result.add(fields);
			}
		}finally{
			if(reader != null){
				reader.close();
			}
		}
		
		return result;
	}
}

