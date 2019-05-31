package fast.common.mailexchanger.test;

import static org.junit.Assert.*;

import java.net.URISyntaxException;

import org.junit.Test;

import fast.common.mailexchanger.mailFactory;
import fast.common.mailexchanger.mailItem;
import fast.common.mailexchanger.mailer;
import fast.common.mailexchanger.serverConfig;

public class SMTPSender {

	//@Test
	public void test() throws Exception {

		serverConfig config = new serverConfig();
		config.setHostName("mailhub-vip.ny.ssmb.com");
		config.setServerEnable(true);
		config.setServerAuth(false);
		config.setServerPort("2525");

		mailItem mi = new mailItem();
		mi.setSender("abc@xyz.com");
		mi.setReceiver("shweta.yadav@citi.com");
		mi.setMailSubject("Ping");
		mi.setMessageBody("Test mail !! Pls ignore !!");

		mailer m = mailFactory.getServerType("SMTP");
		m.configServer(config);
		m.sendMail(mi);
	}

}
