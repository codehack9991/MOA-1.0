package fast.common.testng;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestFastTestngStep {
	
	private FastTestngStep fastTestngStep;
	private List<String> messages = new ArrayList<>();
	private String stepName = "testStepName";
	private long startTime = (long)100;
	
	@Before
	public void setUp() {
		fastTestngStep = new FastTestngStep(stepName, startTime, messages);
	}


	@Test
	public void testSetStepName() {
		fastTestngStep.setStepName(stepName);
		assertEquals("testStepName", fastTestngStep.getStepName());
	}
	
	@Test
	public void testGetStepName() {
		fastTestngStep.setStepName(stepName);
		assertEquals("testStepName", fastTestngStep.getStepName());
	}
	
	@Test
	public void testSetStartTime() {
		fastTestngStep.setStartTime(startTime);
		assertEquals((long)100, fastTestngStep.getStartTime());
	}
	
	@Test
	public void testGetStartTime() {
		fastTestngStep.setStartTime(startTime);
		assertEquals((long)100, fastTestngStep.getStartTime());
	}
	
	@Test
	public void testSetMessage() {
		fastTestngStep.setMessages(messages);
		assertEquals(messages, fastTestngStep.getMessages());
	}
	
	@Test
	public void testSetMessageWithNull() {
		fastTestngStep.setMessages(null);
		assertTrue(fastTestngStep.getMessages().isEmpty());
	}
	
	@Test
	public void testGetMessage() {
		fastTestngStep.setMessages(messages);
		assertEquals(messages, fastTestngStep.getMessages());
	}
	
	@Test
	public void coonstructFastTestngStepWithNoParam(){
		fastTestngStep = new FastTestngStep();
		assertEquals(messages, fastTestngStep.getMessages());
	}
	
	@Test
	public void constructFastTestngStep(){
		fastTestngStep = new FastTestngStep(stepName, startTime, messages);
		assertEquals(messages, fastTestngStep.getMessages());
	}
	
	@Test
	public void constructFastTestngStepWithNullMessage(){
		fastTestngStep = new FastTestngStep(stepName, startTime, null);
		assertTrue(fastTestngStep.getMessages().isEmpty());
	}
	
	@Test
	public void testToString(){
		assertEquals("FastTestngStep [stepName=testStepName, startTime=100, messages=[]]",fastTestngStep.toString());
	}
	
	@Test
	public void testAddMessage(){
		fastTestngStep.addMessage("testStringNew");
		assertTrue(fastTestngStep.getMessages().contains("testStringNew"));
	}
}
