package fast.common.agents.messaging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Test;

import fast.common.agents.messaging.KafkaAgent;
import fast.common.core.Configurator;

public class TestKafkaAgent {

	@Test
	public void testProduceAndConsume() throws Exception {
		Configurator configurator = mock(Configurator.class);
		when(configurator.getConfigFolder()).thenReturn(null);
		@SuppressWarnings("unchecked")
		Map<String, Object> agentParams = mock(Map.class);
		when(agentParams.get("server")).thenReturn(new String("lswpffusapn1d.nam.nsroot.net:9092,lswpffusapn2d.nam.nsroot.net:9092,lswpffusapn3d.nam.nsroot.net:9092"));
		when(agentParams.get("group_id")).thenReturn(new String("test1208"));
		when(agentParams.get("topic")).thenReturn(new String("test1208"));
		when(agentParams.get("message_format")).thenReturn(new String("text"));
		KafkaAgent agent = new KafkaAgent("Test", agentParams, configurator);
		agent.send("This is the first message!");
		agent.receive();
		agent.close();
	}

}
