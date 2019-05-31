package fast.common.phantom.test;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.hssf.record.chart.LinkedDataRecord;
import org.junit.Test;

import com.cet.citi.automation.framework.database.connections.DbConnection;
import com.citi.cet.automation.framework.core.DateTime;
import com.citi.cet.automation.framework.core.Phantom;
import com.citi.cet.automation.framework.messaging.FixMessage;
import com.graphbuilder.struc.LinkedList;

import fast.common.agents.AgentsManager;
import fast.common.phantom.agents.DNAAgent;
import fast.common.phantom.agents.DatabaseAgent;
import fast.common.phantom.agents.JMSAgent;
import fast.common.phantom.agents.SSHAgent;
import fast.common.phantom.agents.TcpFixAgent;
import quickfix.Message;

public class PhantomTest {

	//@Test
	public void DatabseTest() throws Exception {
		
		DatabaseAgent agent = (DatabaseAgent)AgentsManager.getAgent("database");
		List<LinkedHashMap<String, Object>>  output =  agent.getConnection().query("SELECT top 5 TradeStatus FROM CPS_Core..Colt_IncomingTrade");
		System.out.println(output);
		
	}
	
	//@Test KT: it is not working when we build the whole project where fast.common is inside it (submodule). fast.common should not run tests when we only compile it
	public void ExtensionObjectTest() throws Exception {
		DbConnection agent = (DbConnection)AgentsManager.getAgent("extensionTestAgent").getExtensionObject();
		agent.Open();
		List<LinkedHashMap<String, Object>>  output =  agent.query("SELECT top 5 TradeStatus FROM CPS_Core..Colt_IncomingTrade");
		System.out.println(output);
		
	}
	
	//@Test
	public void jmsTest() throws Exception {
		//System.out.println(DateTime.now("HH:mm:ss:SSS"));
		System.out.println("topic");
		JMSAgent agent = (JMSAgent)AgentsManager.getInstance().getOrCreateAgent("jmsTopic");
		
		//System.out.println(DateTime.now("HH:mm:ss:SSS"));
		agent.publishMessage("test message 123 to topic");
		String output =agent.findfirstMessage(10, "");
		System.out.println(output);
		//System.out.println(DateTime.now("HH:mm:ss:SSS"));
		
		System.out.println("queues");
		JMSAgent agent1 = (JMSAgent)AgentsManager.getInstance().getOrCreateAgent("jmsQueue");
		//System.out.println(DateTime.now("HH:mm:ss:SSS"));
		agent1.publishMessage("test message ABZ to queues");
		String output1 =agent1.findfirstMessage(10, "");
		System.out.println(output1);
		//System.out.println(DateTime.now("HH:mm:ss:SSS"));
	}
	
	//@Test
	public void dnaTest() throws Exception {
		DNAAgent agent = (DNAAgent)AgentsManager.getInstance().getOrCreateAgent("c4dna");
//		String tag11 = "";
//		String sql = "select Msg from FixMessage where ClOrdId like \"" + tag11
//				+ "*\", MsgType = `$\"D\" , FixMessageDir = `$\"I\", IsAck = 0, IsExecution = 0, IsProcessed = 0, FixMessageSrcClass = `$\"1\"";
	
		String sql = "select Msg from FixMessage where MsgType = `$\"D\" , FixMessageDir = `$\"I\", IsAck = 0, IsExecution = 0, IsProcessed = 0, FixMessageSrcClass = `$\"1\"";
		System.out.println(sql);
		List<LinkedHashMap<String, Object>> rows = agent.queryDNA(sql);
		String messageInDna = rows.get(0).get("Msg").toString();
		System.out.println(messageInDna);
	}
	
	//@Test
	public void sshTest() throws Exception {
		SSHAgent agent = (SSHAgent)AgentsManager.getInstance().getOrCreateAgent("unix");
		String output = agent.getShell().RunCommand("ls -ltr");
		System.out.println(output);
	}
	
	//@Test
	public void tcpfixTest() throws Exception {
		Phantom.sim.start("config/sim/simplesim.properties");
		String msg = "8=FIX.4.4|9=927|35=D|49=C4|56=CGMI|50=UKQA_C1U_2|57=EUCB2|143=14|52=20170426-06:45:46.043|122=20170426-06:45:46.043|-6=1493189146108|34=2|627=1|628=C4C_UKQA_C1U_2|60=20170426-06:45:46.092|54=1|1=DMNE|11=17116G18te0#0|15=GBX|38=10.0|11022=20170426-06:45:46.104|40=1|55=VOD.L|59=0|63=0|75=20170426|100=EUCB2|168=20170426-07:45:46.000|167=CS|421=GBR|10006=ContinuousOrder;;TBS;|455=59198795|456=101|528=A|581=1|10031=0A|10033=4|10037=QFF|10038=LNPG|10039=LNPG|10040=S|10049=N|10050=BG_AE1|10053=DMA|10075=0|377=N|561=1.0|21=1|100169=N|660=103|11210=17116G18te0|8013=R|10202=EMEADMA|10184=17116G18te0|10292=128|8003=EUCB2|10515=75|10518=75|10895=1000554592|-8003=EUCB2|148=BG_AE1::CFAK42S_A1|8044=newCurrencyProfile|100053=C|47=A|-20301=Y|-11210=17116G18te0|20030=FF:17116G18te0|-10515=75|-88=FIX.4.2|100150=DMNE|-487=N|-491=JUK|-492=GBP|-489=VOD LN|-490=ORD|-10892=EMEA|-10893=NON_PASSTHRU|-493=1.0|10893=NON_PASSTHRU|10892=EMEA|-497=?|-100=LNDN|10=000|";
		Message fixmsg = FixMessage.parse(msg,true);
		TcpFixAgent agent = (TcpFixAgent)AgentsManager.getInstance().getOrCreateAgent("tcpfix");
		agent.sendMessage(fixmsg);
		
	}
	

}
