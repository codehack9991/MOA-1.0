package fast.common.phantom.agents;

import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import com.citi.cet.automation.framework.core.ObjectFactory;
import com.citi.cet.automation.framework.messaging.common.MessageListenerApp;
import com.citi.cet.automation.framework.messaging.common.MessagePublisher;
import com.citi.cet.automation.framework.messaging.queue.QueueListener;
import com.citi.cet.automation.framework.messaging.queue.QueuePublisher;
import com.citi.cet.automation.framework.messaging.topic.TopicListener;
import com.citi.cet.automation.framework.messaging.topic.TopicPublisher;

import fast.common.agents.Agent;
import fast.common.core.Configurator;

public class JMSAgent extends Agent {

	private MessageListenerApp listener = null;
	private MessagePublisher publisher = null;

	public JMSAgent(String name, Map agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		String incoming = "", outgoing = "";

		if (agentParams.containsKey("incoming_name")) {
			incoming = agentParams.get("incoming_name").toString();
		}
		if (agentParams.containsKey("outgoing_name")) {
			outgoing = agentParams.get("outgoing_name").toString();
		}

		if (agentParams.get("connection_type").toString().toLowerCase().equals("topic")) {
			if (!incoming.equals("")) {
				agentParams.put("topicName", agentParams.get("incoming_name"));
				listener = ObjectFactory.getInstance(TopicListener.class, agentParams);
			}
			if (!outgoing.equals("")) {
				agentParams.put("topicName", agentParams.get("outgoing_name"));
				publisher = ObjectFactory.getInstance(TopicPublisher.class, agentParams);
			}
		} else if (agentParams.get("connection_type").toString().toLowerCase().equals("queue")) {
			if (!incoming.equals("")) {
				agentParams.put("queueName", agentParams.get("incoming_name"));
				listener = ObjectFactory.getInstance(QueueListener.class, agentParams);
			}
			if (!outgoing.equals("")) {
				agentParams.put("queueName", agentParams.get("outgoing_name"));
				publisher = ObjectFactory.getInstance(QueuePublisher.class, agentParams);
			}
		}

		if (listener != null) {
			listener.start();
		}
		if (publisher != null) {
			publisher.start();
		}
	}

	public void publishMessage(String message) {
		publisher.publish(message);
	}

	public void publishMessage(Message message) {
		publisher.publish(message);
	}

	public String findfirstMessage(long timeOutInSec, String... filters) {
		List<String> messages = this.listener.findMessages(timeOutInSec, filters);
		if (messages.size() > 0) {
			return messages.get(0);
		}
		return null;
	}

	public String findfirstMessage(long timeOutInSec, String[] exclude, String... filters) {
		List<String> messages = listener.findMessages(timeOutInSec, exclude, filters);
		if (messages.size() > 0) {
			return messages.get(0);
		}
		return null;
	}

	public List<String> findAllMessages(long timeOutInSec, String... filters) {
		return listener.findMessages(timeOutInSec, filters);
	}

	public MapMessage createMapMessage(String alias) {
		try {
			return publisher.getSession().createMapMessage();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public TextMessage createTextMessage(String alias) {
		try {
			return publisher.getSession().createTextMessage();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public ObjectMessage createObjectMessage(String alias) {
		try {
			return publisher.getSession().createObjectMessage();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public BytesMessage createBytesMessage(String alias) {
		try {
			return publisher.getSession().createBytesMessage();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		if (listener != null) {
			listener.stop();
		}

		if (publisher != null) {
			publisher.stop();
		}
	}

}
