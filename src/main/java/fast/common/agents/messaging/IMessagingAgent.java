package fast.common.agents.messaging;


import fast.common.agents.IStartable;
import fast.common.context.MessagingStepResult;

public interface IMessagingAgent extends IStartable{
	boolean isAsync = false;
	
	
	void send(Object message) throws MessagingException;
	
	MessagingStepResult receive() throws MessagingException;

	
	void close() throws Exception;
}
