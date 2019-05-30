package fast.common.agents;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doNothing;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import fast.common.core.Configurator;
import fast.common.gmdReplay.GMDReplayDataHelper;

@RunWith(MockitoJUnitRunner.class)
public class TestDataComparatorAgent {
	private String name;
	
	@Mock
	private GMDReplayDataHelper _dataHelper;
	
	@InjectMocks
	private DataComparatorAgent agent = new DataComparatorAgent();
	
	private Configurator configurator;
	private Map agentParams;
	
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void setup() throws Exception{
	}
	
	@Test
	public void setMode(){
		agent.set_mode(1);
		assertEquals(1,agent.get_mode());
	}
	
	@Test
	public void getMode(){
		assertEquals(0,agent.get_mode());
	}
	
	@Test
	public void testWaitForFinish(){
		doNothing().when(_dataHelper).waitForReplayFinish();
		agent.waitForFinish();
	}
	
	
	@Test
	public void testProcessDataZeroMode(){
		doNothing().when(_dataHelper).putDataToQueue(any(),anyString(),anyString(),anyObject());
		agent.processData("testName", "testSymbol", new Object());
	}
	
	@Test
	public void testProcessDataOneMode(){
		agent.set_mode(1);
		doNothing().when(_dataHelper).putDataToQueue(any(),anyString(),anyString(),anyObject());
		agent.processData("testName", "testSymbol", new Object());
	}
	
}
