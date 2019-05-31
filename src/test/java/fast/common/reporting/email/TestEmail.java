package fast.common.reporting.email;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.Multipart;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlException;


public class TestEmail {
	
	Map<String, String> properties;

	@Before
	public void setUp() throws Exception {
		properties = new HashMap<String, String>();
		properties.put(Email.CONFIG_PROTOCOL, "");
		properties.put(Email.CONFIG_HOST_NAME, "");
		properties.put(Email.CONFIG_PORT, "");
		properties.put(Email.CONFIG_AUTH, "");
		properties.put(Email.CONFIG_SENDER_ADDRESS, "FastFramework@automation.com");
		properties.put(Email.CONFIG_RECEIVER_ADDRESS, "XXXXXX@imcnam.ssmb.com");
		properties.put(Email.CONFIG_COPY_ADDRESS, "");
		properties.put(Email.CONFIG_ATTACHED_FILENAME, "");
		properties.put(Email.CONFIG_ATTACHE_REPORT, "");
		properties.put(Email.CONFIG_X_PRIORITY, "");
		properties.put(Email.CONFIG_X_MSMAIL_PRIORITY, "");
		properties.put(Email.CONFIG_X_MAILER, "");
		properties.put(Email.CONFIG_X_MIMEOLE, "");
		properties.put(Email.CONFIG_RETURNRECEIPT, "");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConstructorWithProperties1() {
		Email email = new Email(properties);
		assertNotNull(email);
	}
	
	@Test
	public void testConstructorWithProperties2() {
		properties.put(Email.CONFIG_ATTACHED_FILENAME, null);
		Email email = new Email(properties);
		assertNotNull(email);
	}
	@Test
	public void testConstructorWithProperties3() {
		properties.put(Email.CONFIG_ATTACHE_REPORT, null);
		Email email = new Email(properties);
		assertNotNull(email);
	}
	
	@Test
	public void testConstructorWithConfigFile() {
		String configFile = "config/reportingConfig.yml";
		Email email = new Email(configFile);
		assertNotNull(email);
	}
	
	@Test
	public void testSubjectSetterGetter() {		
		Email email = new Email(properties);
		String subject = "Hello";
		email.setSubject(subject );
		String actual = email.getSubject();
		assertEquals(subject, actual);
	}
	
	@Test
	public void testGenerateEmail() {
		List<EmailData> emailResult = new ArrayList<>();
		emailResult.add(new EmailData("featureName"));
		Email email = new Email(properties);
		String emailContent = email.generateEmail(emailResult);
		assertNotNull(emailContent);
	}
	
	@Test
	public void testGenerateEmail2() {
		List<EmailData> emailResult = new ArrayList<>();
		EmailData e = new EmailData("featureName");
		e.faildScenario = 1;
		emailResult.add(e);
		Email email = new Email(properties);
		String emailContent = email.generateEmail(emailResult);
		assertNotNull(emailContent);
	}
	
	@Test
	public void testAddSuiteName() {
		Email email = new Email(properties);
		String suiteName = "Test Suite";
		String subject = "Execution Result for [] Test";
		email.setSubject(subject );
		email.addSuiteName(suiteName);
		subject = email.getSubject();
		boolean actual = subject.contains(suiteName);
		assertTrue(actual);
	}
	
	@Test
	public void testAttachFile() throws UnsupportedEncodingException, MessagingException {
		Email email = new Email(properties);
		Multipart multipart = new Multipart() {
			
			@Override
			public void writeTo(OutputStream os) throws IOException, MessagingException {
			}
		};
		email.attacheFile(new File(""), multipart);
		assertNotNull(email);
	}
	
	@Test
	public void testReadEmailConfig() throws YamlException, FileNotFoundException {
		Email email = new Email(properties);
		email.readEmailConfig("config/reportingConfig.yml");
		assertNotNull(email);
	}
	
	@Test
	public void testReadEmailConfig2() throws YamlException, FileNotFoundException {
		Email email = new Email("config/reportingConfig2.yml");
		assertNotNull(email);
	}
	
	@Test
	public void testReadEmailConfig3() throws YamlException, FileNotFoundException {
		Email email = new Email("config/reportingConfig3.yml");
		assertNotNull(email);
	}
	
	@Test
	public void testSendEmail() throws YamlException, FileNotFoundException {
		Email email = new Email(properties);
		email.readEmailConfig("config/reportingConfig.yml");
		String subject = "Execution Result for [] Test";
		email.setSubject(subject );
		try {
			email.sendEmail("result", "suiteName", "");
		} catch (Exception e) {
		}
		assertNotNull(email);
	}
}
