package fast.common.context;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class TestMessagingStepResult {
	MessagingStepResult result = null;

	@Before
	public void setUp() throws Exception {
		ArrayList<Object> messages = new ArrayList<Object>();
		messages.add("received message 1");
		messages.add("received message 2");
		result = new MessagingStepResult(messages);
	}

	@Test
	public void constructArguments_messagesListIsSet() {		
		assertEquals(2, result.getMessages().size());
	}
	
	@Test
	public void constructWithoutArguments_noMessages(){
		MessagingStepResult nullMessageStepResult = new MessagingStepResult();
		assertNull(nullMessageStepResult.getMessages());
	}

	@Test
	public void toString_returnEmpty(){		
		assertEquals("", result.toString());
	}
	
	@Test
	public void getFieldValue_returnNull() throws Throwable{		
		assertNull(result.getFieldValue("Value"));
	}
	
	@Test
	public void getFieldsValues_returnNull() throws Throwable{		
		assertNull(result.getFieldsValues("Value"));
	}
}
