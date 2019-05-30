package fast.common.gmdReplay;

import com.citi.gmd.client.messages.component.GMDAbstractMsg;

public abstract class GMDInternalCallback {
    public abstract void handleMsg(GMDAbstractMsg msg);
}
