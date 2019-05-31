package fast.common.mailexchanger.test;

import static org.junit.Assert.*;

import java.net.URISyntaxException;

import org.junit.Test;

import fast.common.mailexchanger.mailFactory;
import fast.common.mailexchanger.mailItem;
import fast.common.mailexchanger.mailer;
import fast.common.mailexchanger.serverConfig;

public class ExchangeSender {

	//@Test
	public void test() throws Exception {

		serverConfig config = new serverConfig();
		config.setServerUsername("ab1234");// give yourusername to login the server 
		config.setServerPassword("xxxx");// give in the password to login the server
		config.setDomain("NAM");//give domain name
		config.setURL("https://citiwebmail.nam.nsroot.net/EWS/Exchange.asmx");//give the url to the server

		mailItem mi = new mailItem();

		mi.setReceiver("shweta.yadav@citi.com");
		mi.setMailSubject("Ping");
		mi.setMessageBody("Test mail !! Pls ignore !!");

		mailer m = mailFactory.getServerType("Exchange");
		m.configServer(config);
		m.sendMail(mi);
	}

	//@Test
	public void SubjectlessBodylesstest() throws Exception {

		serverConfig config = new serverConfig();
		config.setServerUsername("SY63542");
		config.setServerPassword("Humble#19");
		config.setDomain("NAM");
		config.setURL("https://citiwebmail.nam.nsroot.net/EWS/Exchange.asmx");

		mailItem mi = new mailItem();

		mi.setReceiver("shweta.yadav@citi.com");

		mailer m = mailFactory.getServerType("Exchange");
		m.configServer(config);
		m.sendMail(mi);
	}

}
