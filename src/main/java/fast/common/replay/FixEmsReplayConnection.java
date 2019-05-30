package fast.common.replay;

import com.tibco.tibjms.TibjmsConnectionFactory;
import quickfix.DataDictionary;

import javax.jms.*;
import java.nio.file.Paths;
import java.util.Map;

// TODO: rename - remove Replay word
// TODO: think if we can merge with FixEmsAgent
// can either send or receive messages - not both!
public class FixEmsReplayConnection extends ReplayConnection implements javax.jms.MessageListener {
    Boolean _send; // if true we are sending only, of false we are receiving only

    //String _mappedConnectionName; // mapped topic - we not use super.connection as real mapped connection topic
    Connection _emsConnection;
    Session _session;
    Topic _topic;
    MessageProducer _producer;
    MessageConsumer _consumer;

    public FixEmsReplayConnection(ReplayManager replayManager, String factoryName, Map params, String configFolder, String connection, Boolean send) throws Exception { // connection is already mapped topic name
        super(replayManager, factoryName, params, configFolder, connection);
        _send = send;
        fixDictionary = new DataDictionary(Paths.get(_configFolder).resolve(_params.get("data_dictionary").toString()).toString());
    }

    @Override
    public void send(quickfix.Message msg) throws Exception {
        TextMessage textMsg = _session.createTextMessage();
        textMsg.setText(msg.toString());
        _producer.send(textMsg);
        _logger.debug(String.format("Send message [%s]", msg.toString()));
    }


    @Override
    public void sendraw(String msg) throws Exception {
        TextMessage textMsg = _session.createTextMessage();
        textMsg.setText(msg);
        _producer.send(textMsg);
        _logger.debug(String.format("Send message [%s]", msg.toString()));
    }

    @Override
    public void connect() throws Exception {
        _logger.info("Connecting");

        if(_session != null) {
            String errorMsg = String.format("EMS can't connect to topic '%s' because already connected", _connection);
            //_logger.error(errorMsg);
            throw new Exception(errorMsg);
        }
        String host = _params.get("host").toString();
        String user = _params.get("user").toString();
        String password = _params.get("password").toString();

        TibjmsConnectionFactory connectionFactory = new TibjmsConnectionFactory(host);
        _emsConnection = connectionFactory.createConnection(user, password);
        _session = _emsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);


        _topic = _session.createTopic(_connection);

        try {
            if(_send){
                _producer = _session.createProducer(_topic);
            }
            else { // check receive
                _consumer = _session.createConsumer(_topic);
                _consumer.setMessageListener(this);
            }

            _emsConnection.start();

        } catch(Exception ex) {
            _emsConnection = null;
            //_logger.error("Failed to connect");
            throw ex;
        }


        _logger.info("Connected");
    }

    @Override
    public void disconnect() throws JMSException {
        if(_emsConnection != null) {
            _logger.debug("Disconnecting");
            _emsConnection.close();
            _emsConnection = null;

            _session = null;

            _logger.info("Disconnected");
        }
    }

    @Override
    public void force_disconnect() throws JMSException {
        disconnect();
    }

    @Override
    public void checkDisconnect() throws Exception {
        throw new CheckDisconnectedFailed_ReplayException("Failed to check disconnected. Action is not supported");
    }

    @Override
    public void onMessage(Message message) {
        try {
            String data = ((TextMessage)message).getText();
            quickfix.Message msg = new quickfix.Message(data, fixDictionary, false);
            AddToReceivedMessages(msg);
            _logger.info(String.format("Incoming message [%s]", data));
        } catch (Exception e) {
            _logger.error(String.format("Exception in onMessage(): %s", e.toString()));
        }
    }

}
