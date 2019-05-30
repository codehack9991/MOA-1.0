package fast.common.mailexchanger;

import java.util.logging.Logger;

public class mailItem {

	private String sender;
	private String receiver;
	private String mailsubject;
	private String messagebody;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getMailSubject() {
		return mailsubject;
	}

	public void setMailSubject(String mailsubject) {
		this.mailsubject = mailsubject;
	}

	public String getMessageBody() {
		return messagebody;
	}

	public void setMessageBody(String messagebody) {
		this.messagebody = messagebody;
	}

	public mailItem() {

		String sender = getSender();
		String receiver = getReceiver();
		String mailsubject = getMailSubject();
		String messagebody = getMessageBody();
	}

}
