package fast.common.context;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.citi.dna.data.Table;

import cucumber.api.DataTable;
@RunWith(PowerMockRunner.class)
@PrepareForTest(CollectionUtils.class)
public class TestDNAStepResult {
	
	@Mock
	private DataTable expectedTable;
	
	@Mock
	private EvalScope evalScope;
	
	@Mock
	private ScenarioContext scenarioContext;
	
	@Mock
	private Table table;

	@Rule
	public ExpectedException throwns = ExpectedException.none();
	
	@Before
	public void setup(){
	
	}

	@Test
	public void constructWithNullTable() {
		DNAStepResult dnaStepResult = new DNAStepResult(null);
		throwns.expect(RuntimeException.class);
		throwns.expectMessage("There is no table from DNA result!");
		dnaStepResult.getActualTable();
	}
	
	@Test
	public void constructWithTable() {
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		assertEquals(table,Whitebox.getInternalState(dnaStepResult,"actualTable"));
	}
	
	@Test
	public void constructWithNullObject() {
		Object data = null;
		DNAStepResult dnaStepResult = new DNAStepResult(data);
		assertEquals("", dnaStepResult.toString());
	}
	
	@Test
	public void constructWithObject() {
		DNAStepResult dnaStepResult = new DNAStepResult("invalidData");
		assertEquals("invalidData", dnaStepResult.toString());
	}
	
	@Test
	public void constructWithNoParam() {
		DNAStepResult dnaStepResult = new DNAStepResult();
		assertEquals(null, dnaStepResult.toString());
	}
	
	@Test
	public void stringValueOf(){
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		Character[] chars = {'a','b','c'};
		assertEquals("abc", dnaStepResult.stringValueOf(chars));
	}
	
	@Test
	public void getFieldCharacterArrayValue() throws Throwable{
		Table table = mock(Table.class);
		when(table.getSize()).thenReturn(1);
		Character[] array = {'a','b','c'};
		when(table.get("CharacterField", 0)).thenReturn(array);
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		assertEquals("abc",dnaStepResult.getFieldValue("CharacterField"));
	}
	
	@Test
	public void getFieldCharArrayValue() throws Throwable{
		Table table = mock(Table.class);
		when(table.getSize()).thenReturn(1);
		char[] array = {'a','b','c'};
		when(table.get("charField", 0)).thenReturn(array);
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		assertEquals("abc",dnaStepResult.getFieldValue("charField"));
	}
	
	@Test
	public void getFieldNonCharValue() throws Throwable{
		Table table = mock(Table.class);
		when(table.getSize()).thenReturn(1);
		when(table.get("nonCharField", 0)).thenReturn("non char value");
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		assertEquals("non char value",dnaStepResult.getFieldValue("nonCharField"));
	}
	
	@Test
	public void getFieldValueLessThanOne() throws Throwable{
		Table table = mock(Table.class);
		when(table.getSize()).thenReturn(0);
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		assertEquals(null,dnaStepResult.getFieldValue("invalidField"));
	}
	
	@Test
	public void getFieldsCharValues() throws Throwable{
		Table table = mock(Table.class);
		char []array = {'a','b','c'};
		when(table.getSize()).thenReturn(1);
		when(table.get("invalidField", 0)).thenReturn(array);
		ArrayList<String> targetValues = new ArrayList<String>();
		targetValues.add("abc");
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		assertEquals(targetValues,dnaStepResult.getFieldsValues("invalidField"));
	}
	
	@Test
	public void getFieldsNonCharValues() throws Throwable{
		Table table = mock(Table.class);
		when(table.getSize()).thenReturn(1);
		when(table.get("invalidField", 0)).thenReturn("non char value");
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		ArrayList<String> targetValues = new ArrayList<String>();
		targetValues.add("non char value");
		assertEquals(targetValues,dnaStepResult.getFieldsValues("invalidField"));
	}
	
	@Test
	public void getCellCharValue() throws Throwable{
		Table table = mock(Table.class);
		when(table.getSize()).thenReturn(1);
		char []array = {'a','b','c'};
		when(table.get("invalidColumnName", 0)).thenReturn(array);
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		assertEquals(String.valueOf(array),dnaStepResult.getCellValue("1","invalidColumnName"));
	}
	
	@Test
	public void getCellNonCharValue() throws Throwable{
		Table table = mock(Table.class);
		when(table.getSize()).thenReturn(1);
		when(table.get("invalidColumnName", 0)).thenReturn("non char value");
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		assertEquals("non char value",dnaStepResult.getCellValue("1","invalidColumnName"));
	}
	
	@Test
	public void getCellValueSizeLessThanOne() throws Throwable{
		Table table = mock(Table.class);
		when(table.getSize()).thenReturn(0);
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		assertEquals(null,dnaStepResult.getCellValue("1","invalidColumnName"));
	}
	
//	@Test
//	public void test_convertTableToFulltable(){
//		List<List<String>> tableRaw = new ArrayList<>();
//		List<String> list = new ArrayList<>();
//		list.add("test");
//		tableRaw.add(list);
////		when(evalScope.processVariables(any())).thenReturn("test");
//		when(evalScope.evaluateString(any())).thenReturn("test");
//		when(evalScope.processString(any())).thenReturn("test");
//		when(scenarioContext.processString(any())).thenReturn(evalScope.processString("test"));
//		DataTable dataTable = null;
//		DNAStepResult dnaStepResult = new DNAStepResult(table);
//		System.out.println(dnaStepResult.convertTableToFulltable(dataTable, scenarioContext));
//	}
	
	@Test
	public void test_check() throws Exception{
		List<Map<Object, Object>> maps = new ArrayList<>();
		Map<Object, Object> map = new HashMap<>();
		map.put("testKey", "testValue");
		maps.add(map);
		when(expectedTable.asMaps(any(), any())).thenReturn(maps);
		String []str = {"a","b"};
		when(table.getColumnNames()).thenReturn(str);
		when(table.getSize()).thenReturn(1);
		Collection missingColumns = mock(Collection.class);
		PowerMockito.mockStatic(CollectionUtils.class);
		when(CollectionUtils.subtract(any(), any())).thenReturn(missingColumns);
		System.out.println(missingColumns.isEmpty());
		System.out.println(missingColumns.size());
		when(missingColumns.size()).thenReturn(1);
		System.out.println(missingColumns.size());
		Object col = Mockito.mock(Object.class);
		
		PowerMockito.whenNew(Object.class).withNoArguments().thenReturn(col);
		when(col.toString()).thenReturn("test");
		DNAStepResult dnaStepResult = new DNAStepResult(table);
	}
	
	@Test
	public void test_check_mapSizeException(){
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		throwns.expect(RuntimeException.class);
		throwns.expectMessage("Method check. Invalid table format.");
		dnaStepResult.check(expectedTable);
	}
	
	@Test
	public void test_check_tableSizeException(){
		List<Map<Object, Object>> maps = new ArrayList<>();
		Map<Object, Object> map = new HashMap<>();
		map.put("testKey", "testValue");
		maps.add(map);
		when(expectedTable.asMaps(any(), any())).thenReturn(maps);
		String []str = {"a","b"};
		when(table.getColumnNames()).thenReturn(str);
		Collection missingColumns = mock(Collection.class);
		PowerMockito.mockStatic(CollectionUtils.class);
		when(CollectionUtils.subtract(any(), any())).thenReturn(missingColumns);
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		
		when(table.getSize()).thenReturn(0);
		throwns.expect(RuntimeException.class);
		throwns.expectMessage("Method check. There are no rows in the actualTable ");
		dnaStepResult.check(expectedTable);
	}
	
	@Test
	public void testGetActualTable(){
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		assertEquals(table,dnaStepResult.getActualTable());
	}
	
	@Test
	public void getActualTableToString(){
		DNAStepResult dnaStepResult = new DNAStepResult(table);
		assertEquals(table.toString(),dnaStepResult.toString());
	}
}
