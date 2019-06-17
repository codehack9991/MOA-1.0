package fast.common.fix;

import quickfix.Group;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.MessageUtils;
import quickfix.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**

 */
public class FixMessageFactory implements quickfix.MessageFactory {
    private final Map<String, MessageFactory> messageFactories = new ConcurrentHashMap();

    public FixMessageFactory() {
        this.addFactory("FIX.4.0");
        this.addFactory("FIX.4.1");
        this.addFactory("FIX.4.2");
        this.addFactory("FIX.4.3");
        this.addFactory("FIX.4.4");
        this.addFactory("FIXT.1.1");
        this.addFactory("FIX.5.0");
        this.addFactory("FIX.5.0SP1");
        this.addFactory("FIX.5.0SP2");
    }
    private void addFactory(String beginString) {
        String packageVersion = beginString.replace(".", "").toLowerCase();

        try {
            this.addFactory(beginString, "quickfix." + packageVersion + ".MessageFactory");
        } catch (ClassNotFoundException var4) {
            ;
        }

    }

    public void addFactory(String beginString, String factoryClassName) throws ClassNotFoundException {
        Class factoryClass = null;

        try {
            factoryClass = Class.forName(factoryClassName);
        } catch (ClassNotFoundException var5) {
            Thread.currentThread().getContextClassLoader().loadClass(factoryClassName);
        }

        if(factoryClass != null) {
            this.addFactory(beginString, factoryClass);
        }

    }

    public void addFactory(String beginString, Class<? extends MessageFactory> factoryClass) {
        try {
            MessageFactory e = (MessageFactory)factoryClass.newInstance();
            this.messageFactories.put(beginString, e);
        } catch (Exception var4) {
            throw new RuntimeException("can\'t instantiate " + factoryClass.getName(), var4);
        }
    }

    public Message create(String beginString, String msgType) {
        MessageFactory messageFactory = (MessageFactory)this.messageFactories.get(beginString);
        if(beginString.equals("FIXT.1.1") && !MessageUtils.isAdminMessage(msgType)) {
            messageFactory = (MessageFactory)this.messageFactories.get("FIX.5.0");
        }

        if(messageFactory != null) {
            return messageFactory.create(beginString, msgType);
        } else {
            Message message = new FixMessage();
            message.getHeader().setString(35, msgType);
            return message;
        }
    }

    public Group create(String beginString, String msgType, int correspondingFieldID) {
        MessageFactory messageFactory = (MessageFactory)this.messageFactories.get(beginString);
        if(messageFactory != null) {
            return messageFactory.create(beginString, msgType, correspondingFieldID);
        } else {
            throw new IllegalArgumentException("Unsupported FIX version: " + beginString);
        }
    }
}
