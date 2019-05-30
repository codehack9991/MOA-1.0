package fast.common.reporting.email;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import fast.common.core.Configurator;
import fast.common.logging.FastLogger;
import fast.common.utilities.ZipUtils;

public class Email {
	private static final String TD_STYLE = "<td style='border-color:#5c87b2; border-style:solid; border-width:thin; padding: 5px;'><b>";
	private static final String B_TD = "</b></td>";
	public static final String CONFIG_HOST_NAME = "hostName";
	public static final String CONFIG_PROTOCOL = "protocol";
	public static final String CONFIG_PORT = "port";
	public static final String CONFIG_AUTH = "auth";
	public static final String CONFIG_SENDER_ADDRESS = "senderAddress";
	public static final String CONFIG_RECEIVER_ADDRESS = "receiverAddress";
	public static final String CONFIG_COPY_ADDRESS = "copyAddress";
	public static final String CONFIG_ATTACHED_FILENAME = "attachedFileName";

	public static final String CONFIG_ATTACHE_REPORT = "attacheHtmlReport";
	public static final String CONFIG_X_PRIORITY = "X_Priority";
	public static final String CONFIG_X_MSMAIL_PRIORITY = "X_MSMail_Priority";
	public static final String CONFIG_X_MAILER = "X_Mailer";

	public static final String CONFIG_X_MIMEOLE = "X_MimeOLE";
	public static final String CONFIG_RETURNRECEIPT = "ReturnReceipt";

	private static FastLogger logger = FastLogger.getLogger("Email"); 
	protected String protocol;
	protected String hostName;
	protected String port;
	protected String auth;
	protected String senderAddress;
	protected String[] receiverAddress;
	protected String[] copyAddress;
	protected String subject;
	protected String attachedFileName;
	protected String attacheHtmlReport;
	protected String x_Priority;
	protected String x_MSMail_Priority;
	protected String x_Mailer;
	protected String x_MimeOLE;
	protected String returnReceipt;

	public Email(Map<String, String> properties) {
		protocol = properties.get(CONFIG_PROTOCOL);
		hostName = properties.get(CONFIG_HOST_NAME);
		port = properties.get(CONFIG_PORT);
		auth = properties.get(CONFIG_AUTH);
		senderAddress = properties.get(CONFIG_SENDER_ADDRESS);
		receiverAddress = properties.get(CONFIG_RECEIVER_ADDRESS).split(";");
		copyAddress = properties.get(CONFIG_COPY_ADDRESS).split(";");
		attachedFileName = properties.get(CONFIG_ATTACHED_FILENAME) != null ? properties.get(CONFIG_ATTACHED_FILENAME)
				: null;
		attacheHtmlReport = properties.get(CONFIG_ATTACHE_REPORT) != null ? properties.get(CONFIG_ATTACHE_REPORT)
				: null;
		x_Priority = properties.get(CONFIG_X_PRIORITY);
		x_MSMail_Priority = properties.get(CONFIG_X_MSMAIL_PRIORITY);
		x_Mailer = properties.get(CONFIG_X_MAILER);
		x_MimeOLE = properties.get(CONFIG_X_MIMEOLE);
		returnReceipt = properties.get(CONFIG_RETURNRECEIPT);

	}

	public Email(String emailConfigPath) {

		try {
			Map emailConfigReader;
			Map reportingMap;
			Map rootMap = Configurator.readYaml(emailConfigPath);	
			
			reportingMap = getReportingMap(rootMap);
			if (reportingMap != null && !rootMap.containsKey("Email")) {
				emailConfigReader = Configurator.getMap(reportingMap, "Email");
			}
			else {
				emailConfigReader = Configurator.getMap(rootMap, "Email");
			}
			
			if (emailConfigReader == null) {
				logger.error("Invalid mailing configuration");
				return;
			}
			
			protocol = emailConfigReader.get(CONFIG_PROTOCOL).toString();
			hostName = emailConfigReader.get(CONFIG_HOST_NAME).toString();
			port = emailConfigReader.get(CONFIG_PORT) != null ? emailConfigReader.get(CONFIG_PORT).toString() : null;
			auth = emailConfigReader.get(CONFIG_AUTH).toString();
			senderAddress = emailConfigReader.get(CONFIG_SENDER_ADDRESS).toString();
			receiverAddress = emailConfigReader.get(CONFIG_RECEIVER_ADDRESS).toString().split(";");
			copyAddress = emailConfigReader.get(CONFIG_COPY_ADDRESS) != null
					? emailConfigReader.get(CONFIG_COPY_ADDRESS).toString().split(";") : null;
	
			attachedFileName = emailConfigReader.get(CONFIG_ATTACHED_FILENAME) != null
					? emailConfigReader.get(CONFIG_ATTACHED_FILENAME).toString() : null;
			attacheHtmlReport = emailConfigReader.get(CONFIG_ATTACHE_REPORT) != null
					? emailConfigReader.get(CONFIG_ATTACHE_REPORT).toString() : null;
			x_Priority = emailConfigReader.get(CONFIG_X_PRIORITY).toString();
			x_MSMail_Priority = emailConfigReader.get(CONFIG_X_MSMAIL_PRIORITY).toString();
			x_Mailer = emailConfigReader.get(CONFIG_X_MAILER).toString();
			x_MimeOLE = emailConfigReader.get(CONFIG_X_MIMEOLE).toString();
			returnReceipt = emailConfigReader.get(CONFIG_RETURNRECEIPT).toString();	
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	private Map getReportingMap(Map rootMap) {
		Map reportingMap = null;
		try {
			reportingMap = Configurator.getMap(rootMap, "Reporting");
		} catch (Exception e) {
			reportingMap = null;
		}
		return reportingMap;
	}

	public Map readEmailConfig(String emailConfigPath) throws YamlException, FileNotFoundException {
		Map result = null;
		if (emailConfigPath != null) {
			YamlReader yamlReader = new YamlReader(new FileReader(emailConfigPath));
			result = (Map) yamlReader.read();
		}
		return result;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void sendEmail(String emailResult, String suiteName, String reportDirectory)
			throws MessagingException, UnsupportedEncodingException {
		Properties prop = new Properties();
		prop.setProperty("mail.transport.protocol", protocol); // protocol
		prop.setProperty("mail.smtp.host", hostName); // hostName
		if (port != null && !port.isEmpty())
			prop.setProperty("mail.smtp.port", port);
		prop.setProperty("mail.smtp.auth", auth); // whether open auth control
		// prop.setProperty("mail.debug", "true"); // return sending code
		Session session = Session.getInstance(prop);
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(senderAddress)); // source email address
		for (String s : receiverAddress) {
			msg.addRecipient(RecipientType.TO, new InternetAddress(s));// use
		}
		// msg.addRecipient(RecipientType.TO, new
		// InternetAddress(receiverAddress));// use
		if (copyAddress != null) {
			for (String s : copyAddress) {
				if (!s.isEmpty())
					msg.addRecipient(RecipientType.CC, new InternetAddress(s));
			}
		}
		addSuiteName(suiteName);
		msg.setSubject(subject);// email title
		Multipart multipart = new MimeMultipart();

		MimeBodyPart tableBodyPart = new MimeBodyPart();

		// Attachment part
		if (attachedFileName != null && !attachedFileName.isEmpty()) {
			File attachment = new File(attachedFileName);
			attacheFile(attachment, multipart);
		}
		ZipUtils utils = new ZipUtils();
		if (attacheHtmlReport != null && attacheHtmlReport.equalsIgnoreCase("TRUE")) {
			logger.info("Attache html report function is enabled !");
			if (reportDirectory != null && !reportDirectory.isEmpty()) {
				File file = new File(reportDirectory);
				utils = new ZipUtils(new File(file.getName() + ".zip"));
				utils.zipFiles(file);
				attacheFile(utils.getTargetFile(), multipart);
			} else {
				logger.warn("The report directory is not set, will not attache the html report !");
			}
		}

		tableBodyPart.setContent(emailResult, "text/html");
		multipart.addBodyPart(tableBodyPart);
		msg.setContent(multipart);
		// below define configuration can void being regared as rubbish email
		msg.addHeader(CONFIG_X_PRIORITY, x_Priority);
		msg.addHeader(CONFIG_X_MSMAIL_PRIORITY, x_MSMail_Priority);
		msg.addHeader(CONFIG_X_MAILER, x_Mailer); // using outlook as host to
													// send
		msg.addHeader(CONFIG_X_MIMEOLE, x_MimeOLE);
		msg.addHeader(CONFIG_RETURNRECEIPT, returnReceipt);
		// connect smtp server to send email
		Transport trans = session.getTransport();
		trans.connect();
		// trans.connect("", ""); //input account and password if auth is true
		trans.sendMessage(msg, msg.getAllRecipients());
		utils.clear();
	}

	public void attacheFile(File file, Multipart multipart) throws MessagingException, UnsupportedEncodingException {
		BodyPart attachmentPart = new MimeBodyPart();
		DataSource source = new FileDataSource(file);
		attachmentPart.setDataHandler(new DataHandler(source));
		attachmentPart.setFileName(MimeUtility.encodeWord(file.getName()));
		multipart.addBodyPart(attachmentPart);
	}

	public String generateEmail(List<EmailData> emailResult) {
		String projectResult = "PASSED";
		StringBuilder sb = new StringBuilder();
		int totalPass = 0;
		int totalFail = 0;

		sb.append("<table style='border-collapse:collapse; text-align:left;'>").append("<tr bgcolor='#4E5066'>")
				.append("<td style='border-color:#5c87b2; border-style:solid; border-width:thin; padding: 5px;' width=150><font color='White'><b>Features</b></font></td>")
				.append("<td style='border-color:#5c87b2; border-style:solid; border-width:thin; padding: 5px;' width=150><font color='White'><b>Total Scenario</b></font></td>")
				.append("<td style='border-color:#5c87b2; border-style:solid; border-width:thin; padding: 5px;' width=150><font color='White'><b>Pass</b></font></td>")
				.append("<td style='border-color:#5c87b2; border-style:solid; border-width:thin; padding: 5px;' width=150><font color='White'><b>Fail</b></font></td>")
				.append("</tr>");

		for (EmailData e : emailResult) {
			sb.append("<tr bgcolor='#EBEBEB'>").append(TD_STYLE + e.getFeatureName() + B_TD)
					.append(TD_STYLE + e.getScenarioNum() + B_TD).append(TD_STYLE + e.getPassedScenario() + B_TD)
					.append(TD_STYLE + e.getFaildScenario() + B_TD).append("</tr>");
			if (e.getFaildScenario() != 0) {
				projectResult = "FAILED";
			}
			totalPass += e.getPassedScenario();
			totalFail += e.getFaildScenario();
		}
		sb.append("</table>");
		sb.append("<h1></h1><p><font size ='4'><a href=\"http://citiurl/fast\">Link To Dashboard</a></font></p>");
		this.subject = projectResult + ": Test Status Report - [] - Passed:" + totalPass + ",Failed:" + totalFail;
		return sb.toString();
	}

	public void addSuiteName(String suiteName) {
		String[] aStrings = this.subject.split("\\[]");

		String result = aStrings[0] + "[" + suiteName + "]" + aStrings[1];
		setSubject(result);
	}

}
