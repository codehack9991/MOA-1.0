package fast.common.agents;


import com.citi.gmd.client.GMDContext;
import com.citi.gmd.client.book.GMDAbstractBook;
import com.citi.gmd.client.callbacks.GMDComponentCallback;
import com.citi.gmd.client.callbacks.GMDEventListener;
import com.citi.gmd.client.config.GMDServerConfig;
import com.citi.gmd.client.mbeans.GMDMBeanRegistrar;
import com.citi.gmd.client.messages.admin.GMDAdminRespMsg;
import com.citi.gmd.client.messages.component.*;
import com.citi.gmd.client.messages.constants.*;
import fast.common.core.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import com.citi.gmd.client.messages.component.GMDSubscription;
import com.citi.gmd.client.messages.constants.GMDAPIResponseCode;
import com.citi.gmd.client.messages.constants.GMDSubscriptionType;


public class GmdClientAgent extends Agent implements GMDEventListener {
    public static final String mode_property = "GMD_RT_mode";
    private static final Logger _logger = LoggerFactory.getLogger(GmdClientAgent.class);
    private GMDContext context = GMDContext.getInstance();
    private final Integer mode; //0 - without
    private static final int RECORD = 1;
    private static final int COMPARE = 2;
    private static final String _speedLabel = "MDreplaySpeed";
    private IDataProcessAgent _agent = null;

    public GmdClientAgent(String name, Map agentParams, Configurator configurator) {
        super(name, agentParams, configurator);
        // TODO: load config and add all remaining code

        String configFullFilename = configurator.getFilename(agentParams, "configFile");
        mode = Integer.parseInt(System.getProperty(GmdClientAgent.mode_property));
        //mode = Integer.parseInt(agentParams.get("mode").toString());
        GMDAPIResponseCode status = context.init(configFullFilename);
        if(status != GMDAPIResponseCode.SUCCESS){
            _logger.error("Initialization failed: "+ status);
            System.exit(2);
        }
        int speed = Integer.parseInt(agentParams.get(_speedLabel).toString());
        if(mode == RECORD) {
            try {
                _agent = (DataRecorderAgent) Configurator.getInstance().createAgent("ClientMDRecorder");
            } catch (Exception e) {
                _logger.error("Failed to initiate Data Recorder, stack trace:\n");
                _logger.error(e.getMessage());
                System.exit(3);
            }
        }
        else if(mode == COMPARE){
            try {
                _agent = (DataComparatorAgent) Configurator.getInstance().createAgent("ClientMDComparator");
            } catch (Exception e) {
                _logger.error("Failed to initiate Data Recorder, stack trace:\n");
                _logger.error(e.getMessage());
                System.exit(4);
            }
        }
        else {
            _agent = null;
        }
        if(_agent != null) _agent.setMDspeed(speed);
    }

    public void register(GMDEventListener lstnr) {
        registerCommonCallback(_agent);
        registerEventListener(lstnr);
        GMDMBeanRegistrar mbeanRegistrar  = new GMDMBeanRegistrar();
        mbeanRegistrar.registerMBeans(context);
    }

    public void run() {
        context.connectAllSessions();
    }

    public void wait_for_finish() {
        if(mode == COMPARE) {
            ((DataComparatorAgent)_agent).waitForFinish();
            _logger.info("Replay (Receive & Compare) is finished, no more messages in the Golden Copy.");
            System.exit(0);
        }

    }

    @Override
    public void close() throws Exception {

    }

    public void subscribe(String symbol, short feedId, byte mktCtrId, byte keepSubActiveIfSymNotFound, GMDSubscriptionType subType){
        GMDSubscription sub = new GMDSubscription();
        sub.setSymbol(symbol);
        sub.setFeedId(feedId);
        sub.setMktCtrId(mktCtrId);
        sub.setKeepSubActiveIfSymNotFound(keepSubActiveIfSymNotFound);
        sub.setSubType(subType);
        context.subscribe(sub);
    }

    public void registerEventListener(GMDEventListener lstnr) {
        context.registerEventListener(lstnr != null ? lstnr : this);
    }

    public void registerCommonCallback(IDataProcessAgent agent) {
        if(agent != null) {
            context.registerCallback(new GmdClientComponentCallback(agent));
        }
        else {
            registerCommonCallback();
        }
    }
    public void registerCommonCallback() {
        context.registerCallback(new GmdClientComponentCallback());
    }

    public String getSessionName(String symbol) {
        return context.getSessionNameForSymbol(symbol);
    }

    @Override
    public void listenGMDEvent(GMDConnectionEvent connectionEvent, GMDServerConfig serverConfig) {
        if(serverConfig != null){
            _logger.info(connectionEvent + " on " + serverConfig);
        }else{
            _logger.info(connectionEvent.toString());
        }
    }
}



class GmdClientComponentCallback extends GMDComponentCallback {
    private static Logger _logger = LoggerFactory.getLogger(GmdClientComponentCallback.class);
    private final IDataProcessAgent _processor;

    GmdClientComponentCallback(IDataProcessAgent agent) {
        _processor = agent;
  }
    GmdClientComponentCallback() {
        _processor = null;
    }

    @Override
    public void handleQuoteMsg(GMDQuoteMsg msg) {
        processData(msg.getSymbolString(), msg);
        _logger.info(msg.toString());
    }

    @Override
    public void handleTradeMsg(GMDTradeMsg msg) {
        processData(msg.getSymbolString(), msg);
        _logger.info(msg.toString());
    }

    @Override
    public void handleImbalanceMsg(GMDImbalanceMsg imbalanceMsg) {
        processData(imbalanceMsg.getSymbolString(), imbalanceMsg);
        _logger.info(imbalanceMsg.toString());
    }

    @Override
    public void handleCacheClearMsg(GMDCacheClearMsg cacheClearMsg) {
        processData(cacheClearMsg.getSymbolString(), cacheClearMsg);
        _logger.info(cacheClearMsg.toString());
    }

    @Override
    public void handleStaticInfoMsg(GMDStaticInfoMsg staticInfoMsg) {
        processData(staticInfoMsg.getSymbolString(), staticInfoMsg);
        _logger.info(staticInfoMsg.toString());
    }

    @Override
    public void handleMarketStatusMsg(GMDMktStatusMsg mktStatusMsg) {
        processData(mktStatusMsg.getSymbolString(), mktStatusMsg);
        _logger.info(mktStatusMsg.toString());
    }

    @Override
    public void handleInstrumentStatusMsg(GMDInstrumentStatusMsg instrumentStatusMsg) {
        processData(instrumentStatusMsg.getSymbolString(), instrumentStatusMsg);
        _logger.info(instrumentStatusMsg.toString());
    }

    @Override
    public void handleDetailedTradeMsg(GMDTradeMsgDetailed tradeMsgDetailed) {
        processData(tradeMsgDetailed.getSymbolString(), tradeMsgDetailed);
        _logger.info(tradeMsgDetailed.toString());
    }

    @Override
    public void handlePeggedOrderMsg(GMDPeggedOrderMsg peggedOrderMsg) {
        processData(peggedOrderMsg.getSymbolString(), peggedOrderMsg);
        _logger.info(peggedOrderMsg.toString());
    }

    @Override
    public void handleLogonRej(GMDLogOnRejMsg logOnRejMsg) {
        processData(logOnRejMsg.getSymbolString(), logOnRejMsg);
        _logger.info(logOnRejMsg.toString());
    }

    @Override
    public void handleSubRespMsg(GMDSubRespMsg subRespMsg) {
        processData(subRespMsg.getSymbolString(), subRespMsg);
        _logger.info(subRespMsg.toString());
    }

    @Override
    public void handleUnSubRespMsg(GMDUnSubRespMsg unSubRespMsg) {
        processData(unSubRespMsg.getSymbolString(), unSubRespMsg);
        _logger.info(unSubRespMsg.toString());
    }

    @Override
    public void handleLogOnRespMsg(GMDLogOnRespMsg logOnRespMsg) {
        processData(logOnRespMsg.getSymbolString(), logOnRespMsg);
        _logger.info(logOnRespMsg.toString());
    }

    @Override
    public void handleLogOffRespMsg(GMDLogOffRespMsg logOffRespMsg) {
        processData(logOffRespMsg.getSymbolString(), logOffRespMsg);
        _logger.info(logOffRespMsg.toString());
    }

    @Override
    public void handleAdminRespMsg(GMDAdminRespMsg adminRespMsg) {
        processData(adminRespMsg.getSymbolString(), adminRespMsg);
        _logger.info(adminRespMsg.toString());
    }

    @Override
    public void handleFeedStatusMsg(GMDFeedStatusMsg feedStatusMsg) {
        processData(feedStatusMsg.getSymbolString(), feedStatusMsg);
        _logger.info(feedStatusMsg.toString());
    }

    @Override
    public void handleFeedCfgInfoMsg(GMDFeedCfgInfoMsg fedCfgInfoMsg) {
        processData(fedCfgInfoMsg.getSymbolString(), fedCfgInfoMsg);
        _logger.info(fedCfgInfoMsg.toString());

    }

    @Override
    public void handleBasketQuoteMsg(GMDBasketQuoteMsg basketQuoteMsg) {
        processData(basketQuoteMsg.getSymbolString(), basketQuoteMsg);
        _logger.info(basketQuoteMsg.toString());
    }

    @Override
    public void handlePassThruMsg(GMDPassThruMsg passThruMsg) {
        processData(passThruMsg.getSymbolString(), passThruMsg);
        _logger.info(passThruMsg.toString());
    }

    @Override
    public void handleSnapshotCompleteMsg(GMDSnpshotCompleteNotification snpshotComplete) {
        processData(snpshotComplete.getSymbolString(), snpshotComplete);
        _logger.info(snpshotComplete.toString());
    }

    @Override
    public void handleEnhancedQuoteMsg(GMDEnhancedQuoteMsg enhancedQuoteMsg) {
        processData(enhancedQuoteMsg.getSymbolString(), enhancedQuoteMsg);
        _logger.info(enhancedQuoteMsg.getMsgHdr().toString());
        _logger.info(enhancedQuoteMsg.toString());
    }

    @Override
    public void handleClosingTradeSummaryMsg(GMDClosingTradeSummaryMsg closingTradeSummaryMsg) {
        processData(closingTradeSummaryMsg.getSymbolString(), closingTradeSummaryMsg);
        _logger.info(closingTradeSummaryMsg.toString());
    }

    @Override
    public void handleLULDBandMsg(GMDLULDBandMsg luldBandMsg) {
        processData(luldBandMsg.getSymbolString(), luldBandMsg);
        _logger.info(luldBandMsg.toString());
    }

    @Override
    public void handleTheoreticalFillRespMsg(GMDTheoreticalFillRespMsg theoreticalFillRespMsg) {
        processData(theoreticalFillRespMsg.getSymbolString(), theoreticalFillRespMsg);
        _logger.info(theoreticalFillRespMsg.toString());
    }

    @Override
    public void handleFXSnapshotMsg(GMDFXSnapshotMsg fxSnapShotMsg) {
        processData(fxSnapShotMsg.getSymbolString(), fxSnapShotMsg);
        _logger.info(fxSnapShotMsg.toString());
    }

    @Override
    public void handleBookUpdate(GMDAbstractBook book) {

        processData(book.getSymbol().toString(), book);

        GMDAbstractMsg changedMsg=book.getChangedMsg();
        _logger.info("Book Type: "+book.getClass().getSimpleName());
        if(changedMsg.getType()== GMDCitiBinaryMsgType.GMD_MSGTYPE_TRADE || changedMsg.getType()==GMDCitiBinaryMsgType.GMD_MSGTYPE_TRADEDETAILED){
            _logger.info("\nNew Trade: "+book.getTradeDetailedMsg().toString());
        }
        if(changedMsg.getType()==GMDCitiBinaryMsgType.GMD_MSGTYPE_QUOTE){
            _logger.info("\nQuote Book Update:"+book.printBook(0));
        }
        if(changedMsg.getType()==GMDCitiBinaryMsgType.GMD_MSGTYPE_CACHECLEAR){
            _logger.info("\nQuote Book Update:"+book.printBook(0));
        }

        book.resetBookFlags();

    }

    private void processData(String symbol, Object msg) {
        if(_processor != null) {
            _processor.processData(msg.getClass().getName(), symbol, msg);
        }
    }
}
