package fast.common.mailexchanger;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;

public class SMTP implements mailer {

	mailItem mailItem = new mailItem() ;
   
	Session  session ;
	  Logger theLogger = Logger.getLogger(SMTP.class.getName()) ;
	
	public void configServer(serverConfig config) throws URISyntaxException {
		
		 Properties properties = new Properties();  
	      properties.put("mail.smtp.host", config.getHostName());
	      properties.put("mail.smtp.starttls.enable", config.getServerEnable());
	      properties.put("mail.smtp.auth", config.getServerAuth());
	      properties.put("mail.smtp.port",config.getServerPort());
	    
	      if (config.getServerAuth()==false)
	      {
	       session = Session.getInstance(properties);  
	      
	      }
	      else {
	    	 
		 session = (Session.getDefaultInstance(properties, new Authenticator() {

			      
			      protected PasswordAuthentication getPasswordAuthentication() {
			        return new PasswordAuthentication(config.getServerUsername(),config.getServerPassword());
			      
			      }
		
	}));
	      }
	      }

	
	public Session  SessionObject () {
		return session ;
	}
	
	
	public void sendMail(mailItem mailItem) throws Exception {
		
		 MimeMessage message = new MimeMessage(session);  
		
         message.setFrom(new InternetAddress(mailItem.getSender()));  
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailItem.getReceiver()));  
         message.setSubject( mailItem.getMailSubject());  
         message.setText(mailItem.getMessageBody());  
       
  
        
         Transport.send(message);  
         theLogger.info("message sent successfully...."); 
	}

	
	public List<mailItem> readMail(String searchby, String furthersearchby) throws RuntimeException {
		
		throw new RuntimeException("Sorry we do not support !!");
	}

}
