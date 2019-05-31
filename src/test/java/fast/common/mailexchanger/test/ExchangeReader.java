package fast.common.mailexchanger.test;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;

import fast.common.mailexchanger.mailFactory;
import fast.common.mailexchanger.mailItem;
import fast.common.mailexchanger.mailer;
import fast.common.mailexchanger.serverConfig;

public class ExchangeReader {

	//@Test
	public void test() throws Exception {

		serverConfig config = new serverConfig();
		config.setServerUsername("ab1234");// give yourusername to login the server 
		config.setServerPassword("xxxx");// give in the password to login the server
		config.setDomain("NAM");//give domain name
		config.setURL("https://citiwebmail.nam.nsroot.net/EWS/Exchange.asmx");//give the url to the server

		mailer m = mailFactory.getServerType("Exchange");
		m.configServer(config);
		List<mailItem> li = m.readMail("subject", "FAST/fast.common - Pull request #40: Development");//give criteria to search(subject/categories/content) 
		for (mailItem ML : li) {
			System.out.println("MailRead : " + ML.getSender());
			System.out.println("MailRead : " + ML.getMessageBody());
		}
	}

}
