
package fast.common.mailexchanger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

public class Exchange implements mailer {

	ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010);

	public void configServer(serverConfig config) throws URISyntaxException {

		ExchangeCredentials credentials = new WebCredentials(config.getServerUsername(), config.getServerPassword(),
				config.getDomain());
		service.setCredentials(credentials);
		service.setUrl(new URI(config.getURL()));

	}

	public ExchangeService getService() {
		return service;
	}

	public void sendMail(mailItem mailItem) throws Exception {

		EmailMessage msg = new EmailMessage(service);

		msg.setSubject(mailItem.getMailSubject());
		msg.setBody(MessageBody.getMessageBodyFromText(mailItem.getMessageBody()));
		msg.getToRecipients().add(mailItem.getReceiver());

		msg.send();

	}

	public List<mailItem> readMail(String searchby, String furthersearchby) throws Exception {

		Logger theLogger = Logger.getLogger(Exchange.class.getName());

		PropertySet itempropertyset = new PropertySet(BasePropertySet.FirstClassProperties);
		itempropertyset.setRequestedBodyType(BodyType.Text);

		ItemView view = new ItemView(100);
		view.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Ascending);
		view.setPropertySet(new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived));

		FindItemsResults<Item> findResults = new FindItemsResults<Item>();

		if (searchby.equalsIgnoreCase("subject")) {
			findResults = service.findItems(WellKnownFolderName.Inbox,
					new SearchFilter.ContainsSubstring(ItemSchema.Subject, furthersearchby), view);
		}

		else if (searchby.equalsIgnoreCase("content")) {
			findResults = service.findItems(WellKnownFolderName.Inbox,
					new SearchFilter.ContainsSubstring(ItemSchema.Body, furthersearchby), view);
		} else if (searchby.equalsIgnoreCase("categories")) {
			findResults = service.findItems(WellKnownFolderName.Inbox,
					new SearchFilter.ContainsSubstring(ItemSchema.Categories, furthersearchby), view);
		}

		else {
			System.out.println("Give a valid search type !!");
		}

		getService().loadPropertiesForItems(findResults, itempropertyset);
		theLogger.info("Total number of items found: " + findResults.getTotalCount());

		List<mailItem> li = new ArrayList<mailItem>();

		for (Item item : findResults) {

			mailItem mi = new mailItem();
			mi.setSender(((EmailMessage) item).getSender().getAddress());
			mi.setReceiver(item.getDisplayTo());
			mi.setMailSubject(item.getSubject());
			mi.setMessageBody(item.getBody().toString());
			li.add(mi);
		}

		return li;

	}

}
