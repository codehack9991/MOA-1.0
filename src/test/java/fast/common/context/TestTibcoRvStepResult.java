package fast.common.context;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

public class TestTibcoRvStepResult {

	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void constructWithString() throws Throwable {
		TibcoRvStepResult result = new TibcoRvStepResult("SYMBOL=C|PRICE=50|SIDE=BUY");
		assertEquals("C", result.getFieldValue("SYMBOL"));
	}

	@Test
	public void constructWithTibrvMsg() throws Throwable {
		TibrvMsg msg = new TibrvMsg();
		msg.add("SYM", "C");
		TibcoRvStepResult result = new TibcoRvStepResult(msg);
		assertEquals("C", result.getFieldValue("SYM"));
	}
	
	@Test
	public void toString_success() throws Throwable {
		TibrvMsg msg = new TibrvMsg();
		msg.add("SYM", "C");
		msg.add("SIDE", "BUY");
		TibcoRvStepResult result = new TibcoRvStepResult(msg);
		assertEquals("SYM=C|SIDE=BUY\n", result.toString());
	}
	
	@Test
	public void getFieldValue_returnNull() throws Throwable {
		TibrvMsg msg = new TibrvMsg();
		TibcoRvStepResult result = new TibcoRvStepResult(msg);
		assertNull(result.getFieldValue("SYM"));
	}
	
	@Test
	public void getFieldsValues_returnNull() throws Throwable {
		TibrvMsg msg = new TibrvMsg();
		msg.add("SYM", "C");
		msg.add("SIDE", "BUY");
		TibcoRvStepResult result = new TibcoRvStepResult(msg);
		assertEquals(1, result.getFieldsValues("SYM").size());
	}
}
