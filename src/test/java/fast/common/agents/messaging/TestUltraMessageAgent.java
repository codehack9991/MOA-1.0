package fast.common.agents.messaging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.New;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.kafka.common.network.Send;
import org.apache.xalan.templates.ElemWhen;
import org.elasticsearch.index.shard.IndexShardNotStartedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import quickfix.ConfigError;

//@PrepareForTest({ UltraMessageAgent.class })
//@SuppressStaticInitializationFor({"org.powermock.reflect.Whitebox"})
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
public class TestUltraMessageAgent {
	
	private Map<String,Object> agentParams;
	
	@InjectMocks
	private UltraMessageAgent ultraMessageAgent = new UltraMessageAgent();
	
	@Mock
	private TcpClientAgent client;
	
	@Mock
	private UltraMessageTopic sendTopic;
	
	@Mock
	private List<UltraMessageTopic> receiveTopics;
	
	@Before
	public void setup() throws Exception{
	
	}
	
	@Test
	public void consructor_fieldsSetProperply() throws Exception{
		Map<String,Object> agentParams=new HashMap<>();
		agentParams.put(UltraMessageAgent.CONFIG_SERVER, "server");
		agentParams.put(UltraMessageAgent.CONFIG_PORT, 0);
		agentParams.put(UltraMessageAgent.CONFIG_SENDTOPIC, "topic");
		agentParams.put(UltraMessageAgent.CONFIG_RECEIVETOPICS, "topic");
		agentParams.put(UltraMessageAgent.CONFIG_CONFIG, null);
		UltraMessageAgent agent=new UltraMessageAgent("UltraMessageAgent", agentParams, Configurator.getInstance());
		
		assertEquals("server", Whitebox.getInternalState(agent, "server"));
		assertEquals("62005", Whitebox.getInternalState(agent, "port").toString());
		assertEquals("topic",Whitebox.getInternalState(agent, "sendTopicName"));
		assertEquals("UltraMessageAgent", agent.getName());
		assertEquals(agentParams, agent.getAgentParams());
		assertNotNull(agent.getConfigurator());
		assertNotNull(agent.getLogger());
		assertEquals(System.getProperty("line.separator"), UltraMessageAgent.getMessageSeparator());
	}
	
	@Test
	public void isConnected_passed() throws MessagingException{
		
		client=mock(TcpClientAgent.class);
		assertTrue(ultraMessageAgent.isConnected());
	}
	
	@Test
	public void isConnected_exceptionThrown() {
		UltraMessageAgent agent=new UltraMessageAgent();		
		assertFalse(agent.isConnected());
	}
	
	@Test
	public void send_topicIsNull() throws MessagingException{
		UltraMessageAgent agent=new UltraMessageAgent();
		agent.send("");
	}
	
	@Test
	public void  send_passed() throws MessagingException{
		ultraMessageAgent.send("");
	}
//	
//	@Test
//	public void received_passed() throws MessagingException{
//		ultraMessageAgent.receive();
//	}

	@Test
	public void receive_topicsNull(){		
		try(UltraMessageAgent agent=new UltraMessageAgent()) {
			agent.receive();
		} catch (Exception e) {
			assertEquals(MessagingException.class.getName(), e.getClass().getName());
		}
	}
	
	@Test
	public void start_startFailed_notConnedted(){
		try {
			ultraMessageAgent.start();
		} catch (Exception e) {
			assertEquals(MessagingException.class.getName(), e.getClass().getName());
		}
	}
	
	@Test
	public void start_startFailed_fieldsNull(){
		try {
			UltraMessageAgent agent=mock(UltraMessageAgent.class);
			when(agent.isConnected()).thenReturn(true);
			agent.start();
		} catch (Exception e) {
			assertEquals(MessagingException.class.getName(), e.getClass().getName());
		}
	}
	
	@Test
	public void close_passed(){
		ultraMessageAgent.close();
	}
	
	@Test
	public void isStarted_passed(){
		client=mock(TcpClientAgent.class);
		assertTrue(ultraMessageAgent.isStarted());
	}
	
	@Test
	public void isStartted_exceptionThrown() {
		UltraMessageAgent agent=new UltraMessageAgent();		
		assertFalse(agent.isStarted());
	}
	
	@Test
	public void testSendMessage() throws MessagingException, ConfigError {
		UltraMessageAgent agent = mock(UltraMessageAgent.class);
		agent.start();
		String message = "8=FIX.4.49=249235=RIO49=COMET50=CASH_COES_INSTANCE10143=ORT52=20180608-16:41:06.234122=20180608-16:41:06.234-11=49420468511240561=BLK_AT54=1372=88004=?10028=953065581=110051=0028691210099529=ADV_Y10018=MOAUTO_Test1232382=1375=EJ711=46nBMvxI8.863Ij810080=20180608-16:41:06.23413=1211043=6000.012=11.0150=F10200=46nBMvxI8.863Ij820180510USCOMET10201=COMET10202=USCASH10203=COMET10206=46nBMvxI8.863Ij820180510USCOMET10207=USCOMET10208=US.COMET-CASH.COES10073=;1F:?;R4:N;421=USA10038=cs1305914=6000.010125=0715610025=011032=115=USD10074=;SSID:je78847_1LxYBPnog863Ij8;SCT:AltIDSrc=TCKR;10039:JEDH;10038:CNS;10037:JEDH;10036:CNS;11027=Y426=182.36-1=181303624e4#0100=CMT39=248=44054123167=CS204=0456=10111045=NONA11048=152595204956811055=11057=M11058=FAP11059=111081=0.017=1LxYBRIl0.863Ij818=110026=;TM:;AUID:503;MT:1525952049568;TPR:Y;TI:je78847;AUCUR:USD;RT:Y;PR:F;AP:78971;TO:Y;TP:0.0;SS:Z;CTRS:Y;10006=;COWORKING:LIMIT;FCCUST;BHNYC;TTLIST;SFU;SY0;TAT1;AA0028691210099;DWGCLG;10017=10093=110021=TRADER10037=je7884729=4-17=je7884710067=182.3610066=20180608-16:41:06.23430=NSDQ31=182.3632=6000.0779=20180608-16:41:06.234151=0.010031=0A40=2528=A10044=Auto Created from Manual Exec37=181303624e438=6000.010008=0-5=181303624e344=182.36-33=;RC:E;389=-999.011015=20180608-16:41:06.23410033=4113=Y10042=A10036=cs13059455=4405412322=10110316=0.0207=NASD120=USD64=2018061263=010045=N10005=7010065=0.010062=6000.055=FB-14=059=010041=T175=2018060810039=je78847-19=11114=N377=N11029=132=0.010030=NASD133=0.010048=0-15=SL:N;10013=;CC:46nBMvxI8.863Ij8;60=20180608-16:41:06.23410079=310077=110078=1010092=110064=;MC:4;EX:NASD;10082=010083=COMET_GUI660=10310071=20180608-08:41:06.393100134=Y10104=Y10243=181303624e312052=210184=181303624e310118=Y6=182.36284=610292=611041=1210352=0.0155=0.0425=6000.0424=6000.010514=EJ710515=7010518=701133=D11040=10111210=1813038x5sw10438=RIO.4.510533=110534=SBSH10535=010537=OTC10621=0.0775=010656=210242=010428=1041727=20,6,64,24,40,503,10,27,509,0,501,596,41,65,999,595,621,25,515,606,613,625,165,28,517,601,850,59,5310000=20180608-16:41:06.23410007=20180608-16:41:06.23410217=Y11302=SEC11304=N10663=210670=3 110673=N10697=110698=0028442210699=10410700=1210701=910708=410723=20180510-11:34:12.20610761=8054=510962=LIST10964=18130363atu10965=7011019=N11346=0.011347=0.010372=100232=50310890=0.09871=181303624e410=000";
		agent.send(message);
		Mockito.verify(agent, Mockito.times(1)).send(message);
	}

	@Test
	public void testReceiveMessage() throws MessagingException, ConfigError {
		UltraMessageAgent agent = mock(UltraMessageAgent.class);
		agent.start();
		ArrayList<Object> responseMessages = new ArrayList<Object>();
		responseMessages.add("Test 1");
		when(agent.receive()).thenReturn(new MessagingStepResult(responseMessages));
		assertEquals(1, agent.receive().getMessages().size());
	}
}
