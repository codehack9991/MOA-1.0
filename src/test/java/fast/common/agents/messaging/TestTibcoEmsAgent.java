package fast.common.agents.messaging;

import static org.junit.Assert.*;

import org.junit.Test;

import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Map;

public class TestTibcoEmsAgent {

	@Test
	public void testReceive_usingMock() {
		TibcoEmsAgent mockAgent = mock(TibcoEmsAgent.class);
		
		ArrayList<Object> responseMessages = new ArrayList<Object>();
		responseMessages.add("Test 1");	
		
		try {
			when(mockAgent.receive()).thenReturn(new MessagingStepResult(responseMessages));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		
		try {
			assertEquals(1, mockAgent.receive().getMessages().size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
	}
	
	@SuppressWarnings("resource")
	@Test(expected=RuntimeException.class)
	public void testStart_throwAnException() throws Throwable{
		Configurator configurator = mock(Configurator.class);
		when(Configurator.getStringOr(any(Map.class), any(String.class), any(String.class))).thenReturn(null);
		
		Map agentParams = mock(Map.class);
		when(agentParams.get(any(String.class))).thenReturn(null);
				
		TibcoEmsAgent agent = new TibcoEmsAgent("Test", agentParams, configurator);
		agent.start();
	}

}
