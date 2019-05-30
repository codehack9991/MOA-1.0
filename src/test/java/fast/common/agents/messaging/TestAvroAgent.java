package fast.common.agents;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.avro.generic.GenericData;
import org.apache.bcel.generic.NEW;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fast.common.agents.messaging.KafkaAgent;
import fast.common.core.Configurator;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fast.common.agents.messaging.KafkaAgent;
import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AgentsManager.class })
@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
public class TestAvroAgent {

	@SuppressWarnings("unchecked")
	@Test
	public void test() throws Exception {

		HashMap<String, String> agentParams = new HashMap<String, String>();
		agentParams.put("class_name", "fast.common.agents.AvroAgent");
		agentParams.put("messaging_agent", "KafkaAgent");
		agentParams.put("schemaPath", "./testdata/avro/record.avsc");
		agentParams.put("dataPath", "./testdata/avro/data.txt");
		Configurator configurator = mock(Configurator.class);
		KafkaAgent kafkaAgent = mock(KafkaAgent.class);
		PowerMockito.mockStatic(AgentsManager.class);
		PowerMockito.when(AgentsManager.getAgent("KafkaAgent")).thenReturn(kafkaAgent);
		
		AvroAgent avroAgent = new AvroAgent("avroAgent", agentParams, configurator);
		Mockito.doNothing().when(kafkaAgent).send(any(Object.class));
		avroAgent.start();
		
		avroAgent.send();

		ArrayList<Object> responseMessages = new ArrayList<Object>();
		byte[] value = avroAgent.convertToByteArray(avroAgent.getRecords().get(0));
		responseMessages.add(new ConsumerRecord("test1208", 0, 0, 0, value));
		MessagingStepResult messagingStepResult = new MessagingStepResult(responseMessages);

		when(kafkaAgent.receive()).thenReturn(messagingStepResult);
		avroAgent.receive();

	}

}
