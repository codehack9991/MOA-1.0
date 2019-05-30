package fast.common.agents;

import static org.junit.Assert.*;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import fast.common.agents.messaging.MessagingException;
import fast.common.agents.messaging.MessagingQueueAgent;
import fast.common.agents.messaging.TibcoEmsAgent;
import fast.common.context.FixStepResult;
import fast.common.context.ScenarioContext;
import fast.common.core.Configurator;
import fast.common.glue.BaseCommonStepDefs;
import quickfix.FieldNotFound;
import quickfix.InvalidMessage;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import com.citi.cet.automation.framework.reporter.json.StepResult;









import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({ AgentsManager.class})
public class TestFixEmsAgent {
	
	

			  
	@Test
	public void sendMultiMessagesInTimeWindow_NotImplementedException() throws Throwable{
	
		Map<String, String> agentParams =new HashMap();
		agentParams.put("class_name", "fast.common.agents.FixEmsAgen");
		agentParams.put("data_dictionary", "quickfix_spec/FIX44.xml");
		agentParams.put("send_extra_tags", "BeginString=FIX.4.2|52=%generateTsWithMicroseconds()%");
		agentParams.put("host", "tcp://eqtmsqax.nam.nsroot.net:1234");	    	
		agentParams.put("user", "test");
		agentParams.put("password", "test");
		agentParams.put("send_topic", "X.X.X.X.1");
		Configurator c =Configurator.getInstance();
		c.getSettingsMap().put("FixEmsAgent", agentParams);
		
		FixEmsAgent fixEmsAgent = new FixEmsAgent("FixEmsAgent",agentParams,c);
		ScenarioContext scenarioContext= new ScenarioContext("fixSendMultipleMessages");
		String userstr = "11=123|38=1000|49=SENDER|56=RECEIVER|54=2|";
		try{
			fixEmsAgent.sendMultiMessagesInTimeWindow(scenarioContext, 100, 1, "ClientNew", userstr);
		}catch(NotImplementedException e){
			assertTrue(e.getMessage().contains("FixEmsAgent is not supported to send multiple message in time window now"));
		}
	}
}
