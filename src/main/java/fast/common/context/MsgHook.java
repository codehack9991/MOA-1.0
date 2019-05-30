package fast.common.context;

import quickfix.Message;
import quickfix.SessionID;

//Interface for messaging hooks
public interface MsgHook {
	void handleMessage(Message message, SessionID sessionId);
}
