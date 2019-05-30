package fast.common.mailexchanger;

import java.net.URISyntaxException;
import java.util.List;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;

public interface mailer {
	public void configServer(serverConfig config) throws URISyntaxException;

	public void sendMail(mailItem mailItem) throws Exception;

	public List<mailItem> readMail(String searchby, String furthersearchby) throws ServiceLocalException, Exception;

}
