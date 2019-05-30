package fast.common.agents;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hamcrest.core.IsNull;
import org.jsoup.select.Evaluator.IsEmpty;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import fast.common.agents.messaging.KafkaAgent;
import fast.common.context.ElkStepResult;
import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

import java.util.Set;

//@RunWith(PowerMockRunner.class)
//@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
public class TestRedisAgent {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		
		HashMap<String, String> agentParams = new HashMap<String, String>();
		agentParams.put("class_name", "fast.common.agents.RedisAgent");
		agentParams.put("messaging_agent", "RedisAgent");
		agentParams.put("redisClusterMaster", "6379");
		agentParams.put("redisClusterSlave", "6380");
		agentParams.put("redisClusterServerCount", "3");
		agentParams.put("redisClusterPassword", "");
		agentParams.put("redisClusterServer", "sd-c01a-03ef.nam.nsroot.net,sd-ed79-b54e.nam.nsroot.net,sd-3ec8-e0d8.nam.nsroot.net");
		agentParams.put("redisPort", "6379");
		//agentParams.put("redisServer", "sd-89fb-8e1f.eur.nsroot.net");
		agentParams.put("redisServer", "sd-f918-85d7");
		
		Configurator configurator = mock(Configurator.class);
		//RedisAgent redisAgent = mock(RedisAgent.class);
		
		//PowerMockito.mockStatic(AgentsManager.class);
		//PowerMockito.when(AgentsManager.getAgent("KafkaAgent")).thenReturn(redisAgent);
		
		RedisAgent redisAgent = new RedisAgent("redisAgent", agentParams, configurator);
		//String result = redisAgent.getGET("tca:orderVersion:TE#18218jp000052|1");
		String result = redisAgent.getGET("{orderVersion}:17249XHegdl_17249XHegdl_1Atest_691:order:0");

	}

}
