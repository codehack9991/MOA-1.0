package fast.common.phantom.agents;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.citi.cet.automation.framework.messaging.tcpfix.TcpFixInitiator;

import fast.common.agents.Agent;
import fast.common.core.Configurator;
import quickfix.Message;

public class TcpFixAgent extends Agent {

	private TcpFixInitiator initiator;
	
	public TcpFixAgent(String name, Map agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		// TODO Auto-generated constructor stub
		initiator = new TcpFixInitiator(agentParams.get("sessionfile").toString());
		initiator.start();
	}

	public void sendMessage(String message) {
		initiator.SendToTarget(message);
	}

	public void sendMessage(Message message) {
		initiator.SendToTarget(message);
	}

	public String findfirstResponse(long timeOutInSec, String... filters) {
		List<String> messages = initiator.findMessages(timeOutInSec, filters);
		if (messages.size() > 0) {
			return messages.get(0);
		}
		return null;
	}

	public String findfirstResponse(long timeOutInSec, String[] excludeFilter, String... includeFilters) {
		List<String> messages = initiator.findMessages(timeOutInSec, excludeFilter, includeFilters);
		if (messages.size() > 0) {
			return messages.get(0);
		}
		return null;
	}

	public List<String> findAllResponses(String... filters) {
		return initiator.findMessages(filters);
	}

	public boolean isConnected() {
		return initiator.isLoggedOn();
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		initiator.stop();
	}
}
