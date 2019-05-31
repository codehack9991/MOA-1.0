package fast.common.utilities;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import fast.common.utilities.SampleAgent.AgentCategory;

public class TestObjectFactory {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_getInstance_byClassAndParams() {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("name", "Sample");
		params.put("timeout", 5);		
		params.put("ratio", 0.5);
		params.put("category", AgentCategory.GUI);		
		
		Object obj = ObjectFactory.getInstance(SampleAgent.class, params);
		assertTrue(obj instanceof SampleAgent);				
		assertEquals(((SampleAgent)obj).getName(), "Sample");
	}
	
	@Test
	public void parseTypeFromString_toLong(){
		Object value = ObjectFactory.parseTypeFromString("20190315", Long.class);
		assertEquals(20190315L, value);
	}
	
	@Test
	public void parseTypeFromString_toDouble(){
		Object value = ObjectFactory.parseTypeFromString("2019.0315", Double.class);
		assertEquals(2019.0315, value);
	}
	
	@Test
	public void parseTypeFromString_toBoolean(){
		Object value = ObjectFactory.parseTypeFromString("false", Boolean.class);
		assertEquals(false, value);
	}
	
	@Test
	public void parseTypeFromString_toDate() {
		Object value = ObjectFactory.parseTypeFromString("2019-03-15", java.sql.Date.class);
		assertEquals("2019-03-15", value.toString());
	}
	
	public enum TestType{
		A,
		B,
		C
	}
	
	@Test
	public void parseTypeFromString_toEnum(){
		Object value = ObjectFactory.parseTypeFromString("A", TestType.class);
		assertEquals(TestType.A, value);
	}
	
	@Test
	public void parseTypeFromString_throwException(){		
		boolean throwException = false;
		try{
			ObjectFactory.parseTypeFromString("A", ObjectFactory.class);
		}catch (Exception ex){
			throwException = true;
		}
		
		assertTrue(throwException);
	}
}
