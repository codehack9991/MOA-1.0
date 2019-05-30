package fast.common.gmdReplay.pserver;

import com.citi.gmd.client.messages.component.*;
import com.citi.gmd.client.messages.constants.GMDCitiBinaryMsgType;
import com.citi.gmd.client.messages.constants.GMDImbalanceCrossType;
import com.citi.gmd.client.messages.constants.GMDLogOnErrRespCodeType;
import com.citi.gmd.client.messages.constants.GMDSubRespCodeType;
import com.citi.gmd.client.messages.processor.GMDCitiBinaryMsgFormatter;
import com.citi.gmd.client.utils.GMDByteArrayUtils;
import com.citi.gmd.client.utils.GMDCitiBinaryUtil;

import fast.common.gmdReplay.GMDInternalCallback;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.ByteOrder;

public class GMDCertificationClient extends GMDInternalCallback implements GMDCertificationClientMBean, Runnable {
//public class GMDCertificationClient implements GMDCertificationClientMBean, Runnable {
    private static final Logger log = LogManager.getLogger(GMDCertificationClient.class);
    private GMDDummyServer server;
    private GMDCitiBinaryMsgFormatter formatter;
    private String recordedMsgsFileName;
    private long replayInterval;
    private int bufferSize=0;
    private int feedId=0;
    private int timeIndex=0;
    
    public GMDCertificationClient(GMDDummyServer server) {
        this.server = server;
        this.formatter = new GMDCitiBinaryMsgFormatter();
        this.recordedMsgsFileName = null;
        this.replayInterval=0;
        this.bufferSize=0;
    }
    
    public GMDCertificationClient(GMDDummyServer server, String dumpFile, int bufferSize, long delay) {
        this.server = server;
        this.formatter = new GMDCitiBinaryMsgFormatter();
        this.recordedMsgsFileName = dumpFile;
        this.replayInterval=delay;
        this.bufferSize=bufferSize; 
    }
    
    
    @Override
    public void handleMsg(GMDAbstractMsg msg){
        log.info("Received: "+ msg);
        if (msg.getType() == GMDCitiBinaryMsgType.GMD_MSGTYPE_LOGONREQ) {
            GMDLogOnReqMsg req = (GMDLogOnReqMsg) msg;
            sendLogOnResponse(req);
        }else if (msg.getType() == GMDCitiBinaryMsgType.GMD_MSGTYPE_HEARTBEATREQ) {
            GMDHeartBeatReqMsg hbReq = (GMDHeartBeatReqMsg) msg;
            sendHbResponse(hbReq);
        }else if (msg.getType() == GMDCitiBinaryMsgType.GMD_MSGTYPE_SUBREQ) {
            GMDSubReqMsg req = (GMDSubReqMsg) msg;
            sendSubRespMsg(req);
        }
        
    }
        
    
   

    @Override
    public void sendLogonResponse(String clientId) {
        GMDLogOnRespMsg resp = new GMDLogOnRespMsg();
        resp.setClientId(clientId);
        resp.setSymRangeServed("A-Z");
        resp.setPartitionNum((short) 1);
        resp.setServerVersion("Ver-3");
        resp.setPartitionCount((short) 10);
        resp.setServerMode("CitiBinary");
        send(resp);
    }
    
    
    @Override
    public void sendLogonReject(int reasonCode, String reasonStr) {
        GMDLogOnRejMsg msg = new GMDLogOnRejMsg();
        msg.setReasonCode(reasonCode);
        msg.setReasonStr(reasonStr);
        send(msg);
    }
    
    @Override
    public void sendHeartBeatResponse() {
        GMDHeartBeatRespMsg resp = new GMDHeartBeatRespMsg();
        resp.setClientData(12456L);
        send(resp);
        
    }
    
    private void sendHbResponse(GMDHeartBeatReqMsg req) {
        GMDHeartBeatRespMsg resp = new GMDHeartBeatRespMsg();
        resp.setClientData(req.getClientData());
        send(resp);
    }
    
    private void sendLogOnResponse(GMDLogOnReqMsg req) {
        GMDLogOnRespMsg resp = new GMDLogOnRespMsg();
        resp.setClientId(req.getClientId());
        resp.setSymRangeServed("A-Z");
        resp.setPartitionNum((short) 1);
        resp.setServerVersion("Ver-3");
        resp.setPartitionCount((short) 10);
        resp.setServerMode("CitiBinary");
        send(resp);
    }
    
    protected void sendLogOnReject(GMDLogOnReqMsg req) {
        GMDLogOnRejMsg rej = new GMDLogOnRejMsg();
        rej.setReasonCode(GMDLogOnErrRespCodeType.GMD_INVALIDUSERNAME_PASSWORD.ordinal());
        rej.setReasonStr(GMDLogOnErrRespCodeType.GMD_INVALIDUSERNAME_PASSWORD.toString());
        send(rej);
    }
    
    private void sendSubRespMsg(GMDSubReqMsg reqMsg) {
        for (GMDSubscription sub : reqMsg.getSubscriptionAry()) {
            GMDSubRespMsg msg = new GMDSubRespMsg();
            msg.setFeedId(sub.getFeedId());
            msg.setMktCtrId(sub.getMktCtrId());
            msg.setSymbol(sub.getSymbol());
            msg.setAdditionalSubBitMask(sub.getAdditionalSubBitMask());
            msg.setSubType(sub.getSubType());
            msg.setConflationFactor(sub.getConflationFactor());
            msg.setErrCode(GMDSubRespCodeType.GMD_SUBERROR_SYMNOTFOUNDSUBACTIVE);
            msg.setKeepSubActiveIfSymNotFound(sub.getKeepSubActiveIfSymNotFound());
            msg.setIsRange(sub.getIsRange());
            msg.setNumLevels(sub.getNumLevels());
            msg.setTurnKey(sub.getTurnKey());
            send(msg);
        }
    }
    
    private void send(GMDAbstractMsg msg) {
        byte[] buf = formatter.encodeMsg(msg);
        GMDByteArrayUtils.putShort(buf, GMDPktHdr.getLength()+4, (short) feedId, ByteOrder.LITTLE_ENDIAN);
        GMDByteArrayUtils.putShort(buf, GMDPktHdr.getLength()+22, (short) timeIndex, ByteOrder.LITTLE_ENDIAN);
        server.sendMsgToAllClient(buf);
    }
    
    private void send(GMDAbstractMsg msg, boolean isSnapshot) {
        byte[] buf = formatter.encodeMsg(msg);
        GMDByteArrayUtils.putShort(buf, GMDPktHdr.getLength()+4, (short) feedId, ByteOrder.LITTLE_ENDIAN);
        buf[GMDPktHdr.getLength()+7]=1;
        server.sendMsgToAllClient(buf);
    }
    
    int c =0;
    @Override
    public void sendQuoteMessage(String symbol, long price, char side, int size, int feedId, int mktCtrId, short idx,  boolean isSnapshot, String mktMkrId) {
        
        this.feedId=feedId;
        GMDQuoteMsg msg = new GMDQuoteMsg();
        GMDPriceLevel pl = msg.getQuoteAtIdx((byte) 0);
        pl.setFeedId((short) feedId);
        pl.setMktCtrId((byte) mktCtrId);
        msg.setSymbol(symbol);
        pl.setPrice(price);
        pl.setSize(size);
        pl.setSide(side);
        pl.setIdx(idx);
        pl.setMktMkrId(mktMkrId);
        if (isSnapshot) {
            send(msg, true);
        } else {
            send(msg);
        }
        log.info(msg);
    }
    
    @Override
    public void sendNBBO(String symbol, long price, int size, int feedId) {
        
        this.feedId=feedId;
        GMDQuoteMsg msg = new GMDQuoteMsg((byte) 2);
        msg.setSymbol(symbol);
        
        byte idx =1;

        GMDPriceLevel pl = msg.getQuoteAtIdx((byte) 0);
        pl.setFeedId((short) feedId);
        pl.setMktCtrId((byte) 4);
        pl.setPrice(price+1);
        pl.setSize(size);
        pl.setSide('B');
        pl.setIdx(idx);
        pl.setMktMkrId("CITI");
        
        GMDPriceLevel pl1 = msg.getQuoteAtIdx((byte) 1);
        pl1.setFeedId((short) feedId);
        pl1.setMktCtrId((byte) 0);
        pl1.setPrice(price+2);
        pl1.setSize(size);
        pl1.setSide('S');
        pl1.setIdx(idx);
        pl1.setMktMkrId("GMD");
        
        log.info(msg);
        send(msg);
    }
    
    @Override
    public void sendTradeMessage(long tradeOrderId, long tradeMatchId, String symbol, String tradeCond, byte regionalTradeFlags, short consolTradeFlags,  long price, int size, int feedId, int mktCtrId, boolean isSnapshot, short timeIndex) {
        this.feedId=feedId;
        GMDTradeMsg msg = new GMDTradeMsg();
        msg.setTradeOrderId(tradeOrderId);
        msg.setTradeMatchId(tradeMatchId);
        msg.setSymbol(symbol);
        msg.setTradePrice(price);
        msg.setTradeSize(size);
        msg.setMktCtrId((byte) mktCtrId);
        msg.setTradeCond(tradeCond);
        msg.setRegionalTradeFlags(regionalTradeFlags);
        msg.setConsolTradeFlags(consolTradeFlags);
        msg.setRegionalTradeFlags(regionalTradeFlags);
        if (isSnapshot) {
            send(msg, true);
        } else {
            send(msg);
        }
    }
    
    @Override
    public void sendOrderMessage(String symbol, long price, int size, char side, int feedId, int mktCtrId, int msgType, long orderId, boolean isSnapshot ) {
        this.feedId=feedId;
        GMDOrderMsg msg = new GMDOrderMsg();
        msg.setSymbol(symbol);
        msg.setPrice(price);
        msg.setSize(size);
        msg.setSide(side);
        msg.setMktCtrId((byte) mktCtrId);
        msg.setIncMsgType((byte) msgType);
        msg.setOrderId(orderId);
        if (isSnapshot) {
            send(msg, true);
        } else {
            send(msg);
        }
   }
    
    @Override
    public void sendImbalanceMessage(String symbol, short source, char crossType, char imbSide, long imbVolume,
            int feedId, int mktCtrId, char regImbInd, char priceVarInd, long matchedVolume, long mocOrMarketImbVolume, long closingOnlyClearingPrice, long auctionClearingPrice, long curRefPrice, long auctionTime ) {
        this.feedId=feedId;
        GMDImbalanceMsg msg = new GMDImbalanceMsg();
        msg.setSymbol(symbol);
        msg.setSource(source);
        msg.setCrossType(GMDImbalanceCrossType.FIRM);
        msg.setImbSide(imbSide);
        msg.setImbVolume(imbVolume);
        msg.setMktCtrId((short) mktCtrId);
        
        msg.setRegImbInd(regImbInd);
        msg.setPriceVarInd(priceVarInd);
        msg.setMatchedVolume(matchedVolume);
        msg.setMocOrMarketImbVolume(mocOrMarketImbVolume);
        msg.setClosingOnlyClearingPrice(closingOnlyClearingPrice);
        msg.setAuctionClearingPrice(auctionClearingPrice);
        msg.setAuctionTime(auctionTime);
        msg.setCurRefPrice(curRefPrice);
        send(msg);
    }
    
    @Override
    public void sendInstrumentStatusMessage(String symbol, char regulatoryTradingStatus, short statusCode, int feedId, byte mktCtrId, char regShoInd, String mktCtrTradingStatus) {
        this.feedId=feedId;
        GMDInstrumentStatusMsg msg = new GMDInstrumentStatusMsg();
        msg.setSymbol(symbol);
        msg.setRegulatoryTradingStatus(regulatoryTradingStatus);
        msg.setStatusCode(statusCode);
        msg.setMktCtrId(mktCtrId);
        msg.setRegShoInd(regShoInd);
        msg.setMktCtrTradingStatus(mktCtrId, (byte) mktCtrTradingStatus.toCharArray()[0]);
        send(msg);
    }
    
    @Override
    public void sendCacheClearMessage(String symbol, short clearBitMask, short mktCtrId) {
        GMDCacheClearMsg msg = new GMDCacheClearMsg();
        msg.setSymbol(symbol);
        msg.setClearBitMask(clearBitMask);
        msg.setMktCtrId(mktCtrId);
        send(msg);
    }
    
    @Override
    public void sendFeedStatusMessage(short feedId, byte feedStatus, String symbolRangeAffected,
            String ipAddressAffected, byte feedSubId, char discReason) {
        this.feedId=feedId;
        GMDFeedStatusMsg msg = new GMDFeedStatusMsg();
        msg.setFeedId(feedId);
        msg.setFeedStatus(feedStatus);
        msg.setSymbolRangeAffected(symbolRangeAffected);
        msg.setIpAddressAffected(ipAddressAffected);
        msg.setFeedSubId(feedSubId);
        msg.setDiscReason(discReason);
        send(msg);
    }
    
    @Override
    public void sendMarketStatusMessage(byte mktCtrId, char mktState, short statusCode) {
        GMDMktStatusMsg msg = new GMDMktStatusMsg();
        msg.setMktCtrId(mktCtrId);
        msg.setMktState(mktState);
        msg.setStatusCode(statusCode);
        send(msg);
    }
    
    @Override
    public void sendPassThruMessage(String symbol, byte passThruType, short passThruDataLength, byte mktCtrId) {
        GMDPassThruMsg msg = new GMDPassThruMsg();
        msg.setSymbol(symbol);
        msg.setPassThruType(passThruType);
        msg.setPassThruDataLength(passThruDataLength);
        msg.setMktCtrId(mktCtrId);
        send(msg);
    }
    
    @Override
    public void sendPeggedOrderMessage(String symbol, long price, int size, char side, byte incMsgType, byte mktCtrId, long orderId, String source, byte orderCat, byte operation, byte marketId, char orderStatus, char referencePriceCode, int minExecQuantity, int attributes, long pegOffset ) {
        GMDPeggedOrderMsg msg = new GMDPeggedOrderMsg();
        msg.setSymbol(symbol);
        msg.setPrice(price);
        msg.setSize(size);
        msg.setSide(side);
        msg.setIncMsgType(incMsgType);
        msg.setMktCtrId(mktCtrId);
        msg.setOrderId(orderId);
        msg.setSource(source);
        msg.setOrderCat(orderCat);
        msg.setOperation(operation);
        msg.setMarketId(marketId);
        msg.setOrderStatus(orderStatus);
        msg.setReferencePriceCode(referencePriceCode);
        msg.setMinExecQuantity(minExecQuantity);
        msg.setAttributes(attributes);
        msg.setPegOffset(pegOffset);
     
        send(msg);
    }
    
    @Override
    public void sendTradeMessageDetailed(String symbol, int feedId, long tradePrice, int tradeSize, int cumVol, byte mktCtrId,
            String tradeCond, byte numOfPrevTrades, byte subMktCtrId, byte bookKeepBitMask, long vwapPrice,
            long totDollarVolTraded, long highPrice, long lowPrice, long netChange, long openPrice,
            long primaryOpenPrice, long closingPriceCur, long primaryClosingPriceCur, long closingPricePrev,
            long primaryClosingPricePrev, int cumVolHidden, int cumVolDisplayed, long tradeMatchId, long cumOffExchVol,
            int cumOnExchCount, int cumOffExchCount, long tradeOrderId, long cumVolEx, long primaryOnExchLastTradePrice) {
        
        GMDTradeMsgDetailed msg = new GMDTradeMsgDetailed();
        msg.setSymbol(symbol);
        msg.setTradePrice(tradePrice);
        msg.setTradeSize(tradeSize);
        msg.setCumVol(cumVol);
        msg.setMktCtrId(mktCtrId);
        msg.setTradeCond(tradeCond);
        //msg.setNumOfPrevTrades(numOfPrevTrades);
        msg.setSubMktCtrId(subMktCtrId);
        msg.setBookKeepBitMask(bookKeepBitMask);
        msg.setVwapPrice(vwapPrice);
        msg.setTotDollarVolTraded(totDollarVolTraded);
        msg.setHighPrice(highPrice);
        msg.setLowPrice(lowPrice);
        msg.setNetChange(netChange);
        msg.setOpenPrice(openPrice);
        msg.setPrimaryOpenPrice(primaryOpenPrice);
        msg.setClosingPriceCur(closingPriceCur);
        msg.setPrimaryClosingPriceCur(primaryClosingPriceCur);
        msg.setPrimaryClosingPricePrev(primaryClosingPricePrev);
        msg.setCumVolHidden(cumVolHidden);
        msg.setCumVolDisplayed(cumVolDisplayed);
        msg.setTradeMatchId(tradeMatchId);
        msg.setCumOffExchVol(cumOffExchVol);
        msg.setCumOffExchCount(cumOffExchCount);
        msg.setCumVolDisplayed(cumVolDisplayed);
        msg.setTradeOrderId(tradeOrderId);
        msg.setCumVolEx(cumVolEx);
        msg.setPrimaryOnExchLastTradePrice(primaryOnExchLastTradePrice);
        this.feedId=feedId;
        send(msg);
        
    }
    
    @Override
    public void sendStaticInfoMessage(String symbol, String fullName, short secType, byte secSubType, short feedId, byte mktCtrId, long upperLimit, long lowerLimit, long openPrice, long OpenPricePrimary, long closePriceCur, long closePricePrev) {
        GMDStaticInfoMsg msg = new GMDStaticInfoMsg();
        msg.setSymbol(symbol);
        msg.setFullName(fullName);
        msg.setSecType(secType);
        msg.setSecSubType(secSubType);
        this.feedId=feedId;
        msg.setPrimaryMktCtrId(mktCtrId);
        msg.setUpperLimit(upperLimit);
        msg.setLowerLimit(lowerLimit);
        msg.setOpenPrice(openPrice);
        msg.setOpenPricePrimary(OpenPricePrimary);
        msg.setClosingPriceCur(closePriceCur);
        msg.setClosingPricePrev(closePricePrev);
        
        send(msg);
    }
    

    @Override
    public void startReplaying(String fileName, int bufSize, int delay) {
        this.recordedMsgsFileName = fileName;
        this.replayInterval = delay;
        this.bufferSize=bufSize;
        Thread replayThread = new Thread(this, "GMDSimulatorReplayer");
        replayThread.start();
        
    }
    
    @Override
    public void startSendingRandomQuote(int delay) {
        Thread replayThread = new Thread(this, "GMDSimulatorReplayer");
        replayInterval = delay;
        replayThread.start();
    }
    
    public void testFastInsideAtomicUpdate(String symbol, long price, int size){
        GMDQuoteMsg qm = new GMDQuoteMsg((byte) 2);
        qm.setSymbol(symbol);  
        byte idx =0;
        qm.getQuoteAtIdx(idx).setPrice(price);
        qm.getQuoteAtIdx(idx).setSize(size);
        qm.getQuoteAtIdx(idx).setIdx((short) 0);
        qm.getQuoteAtIdx(idx).setSide('B');
        qm.getQuoteAtIdx(idx).setIdx((short) 1);
        
        idx++;
        qm.getQuoteAtIdx(idx).setPrice(price+1000);
        qm.getQuoteAtIdx(idx).setSize(size+100);
        qm.getQuoteAtIdx(idx).setIdx((short) 0);
        qm.getQuoteAtIdx(idx).setSide('S');
        qm.getQuoteAtIdx(idx).setIdx((short) 1);
        
        log.info(qm);
        send(qm);
    }
    
    public void sendNBBOBLevelBasedSellSideValid(String symbol, long price, int size){
        GMDQuoteMsg qm = new GMDQuoteMsg((byte) 2);
        qm.setSymbol(symbol);  
        byte idx =0;
        qm.getQuoteAtIdx(idx).setPrice(0);
        qm.getQuoteAtIdx(idx).setSize(0);
        qm.getQuoteAtIdx(idx).setSide(' ');
        qm.getQuoteAtIdx(idx).setIdx((short) 0);
        
        idx++;
        qm.getQuoteAtIdx(idx).setPrice(price);
        qm.getQuoteAtIdx(idx).setSize(size);
        qm.getQuoteAtIdx(idx).setSide('S');
        qm.getQuoteAtIdx(idx).setIdx((short) 1);
        
        log.info(qm);
        send(qm);
    }
    
    
    public void sendNBBOBothSideOKLevelBased(String symbol, long price, int size){
        GMDQuoteMsg qm = new GMDQuoteMsg((byte) 2);
        qm.setSymbol(symbol);  
        byte idx =0;
        qm.getQuoteAtIdx(idx).setPrice(price);
        qm.getQuoteAtIdx(idx).setSize(size);
        qm.getQuoteAtIdx(idx).setSide('B');
        qm.getQuoteAtIdx(idx).setIdx((short) 1);
        
        idx++;
        qm.getQuoteAtIdx(idx).setPrice(price);
        qm.getQuoteAtIdx(idx).setSize(size);
        qm.getQuoteAtIdx(idx).setSide('S');
        qm.getQuoteAtIdx(idx).setIdx((short) 1);
        
        log.info(qm);
        send(qm);
    }
    
    public void sendNBBOBLevelBasedBuySideValid(String symbol, long price, int size){
        GMDQuoteMsg qm = new GMDQuoteMsg((byte) 2);
        qm.setSymbol(symbol);  
        byte idx =0;
        qm.getQuoteAtIdx(idx).setPrice(price);
        qm.getQuoteAtIdx(idx).setSize(size);
        qm.getQuoteAtIdx(idx).setSide('B');
        qm.getQuoteAtIdx(idx).setIdx((short) 1);
        
        idx++;
        qm.getQuoteAtIdx(idx).setPrice(0);
        qm.getQuoteAtIdx(idx).setSize(0);
        qm.getQuoteAtIdx(idx).setSide(' ');
        qm.getQuoteAtIdx(idx).setIdx((short) 0);
        
        log.info(qm);
        send(qm);
    }
    
    public void testFastInsideAtomicUpdateOnceSide(String symbol, long price, int size, char side){
        GMDQuoteMsg qm = new GMDQuoteMsg((byte) 1);
        qm.setSymbol(symbol);  
        byte idx =0;
        qm.getQuoteAtIdx(idx).setPrice(price);
        qm.getQuoteAtIdx(idx).setSize(size);
        qm.getQuoteAtIdx(idx).setIdx((short) 0);
        qm.getQuoteAtIdx(idx).setSide(side);
        log.info(qm);
        send(qm);
    }
    
    public void testFastInsideAtomicUpdateOnceSideDelete(String symbol, long price, int size, char side){
        GMDQuoteMsg qm = new GMDQuoteMsg((byte) 2);
        qm.setSymbol(symbol);  
        byte idx =0;
        if(side == 'S'){
            qm.getQuoteAtIdx(idx).setPrice(price);
            qm.getQuoteAtIdx(idx).setSize(size);
            qm.getQuoteAtIdx(idx).setIdx((short) 0);
            qm.getQuoteAtIdx(idx).setSide('B');
        }else{
            idx++;
            qm.getQuoteAtIdx(idx).setPrice(price);
            qm.getQuoteAtIdx(idx).setSize(size);
            qm.getQuoteAtIdx(idx).setIdx((short) 0);
            qm.getQuoteAtIdx(idx).setSide('S');
        } 
        log.info(qm);
        send(qm);
    }
    
    
    
    private void send(byte[] buf) {
        server.sendMsgToAllClient(buf);
    }
    
    @Override
    public void run() {
        if (recordedMsgsFileName == null) {
            sendRandomQuoteMsg();
        } else {
            if (bufferSize == 0) {
                startReplay();
            } else {
                replayDataInFixedBufferSize();
            }
        }
        log.info("Started sending reocorded msgs..");
    }
    
    public void sendRandomQuoteMsg() {
        boolean stop = false;
        GMDQuoteMsg msg = new GMDQuoteMsg((byte) 10);
        msg.setSymbol("AAPL");
        for(byte i=0; i<10; i++){
            msg.getQuoteAtIdx(i).setFeedId((short) feedId);
            msg.getQuoteAtIdx(i).setMktCtrId((byte) 0);
            msg.getQuoteAtIdx(i).setPrice(8987+i*100000);
            msg.getQuoteAtIdx(i).setSize(1000+i*100);
            if(i%2 ==0){
                msg.getQuoteAtIdx(i).setSide('B');
            }else{
                msg.getQuoteAtIdx(i).setSide('S');
            }
        }
            
        while (!stop) {
            send(msg);
        }
    }
    
    /**
     * Sends one Pkt at a time;
     */
    public void startReplay() {
    	FileInputStream fis = null;
        BufferedInputStream is1 = null;
        try {
        	fis= new FileInputStream(new File(recordedMsgsFileName));
            is1 = new BufferedInputStream(fis);

            byte[] phBuf = new byte[24];
            while (is1.read(phBuf, 0, 24) != -1) {
                int pktLen = GMDByteArrayUtils.getShort(phBuf, 4, ByteOrder.LITTLE_ENDIAN);
                byte[] msgBuf = new byte[pktLen - 24];
                if (is1.read(msgBuf, 0, pktLen - 24) == -1) {
                    log.error("Inconsistent dump file, exiting the system");
                    System.exit(0);
                }
                byte[] pktBuf = ArrayUtils.addAll(phBuf, msgBuf);
                send(pktBuf);
                if(replayInterval > 0){
                    GMDCitiBinaryUtil.waitForMicroSecond(replayInterval);
                }
            }

        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException", e);
        } catch (IOException e) {
            log.error("FileNotFoundException",e);
        }
        finally{
        	if(fis != null){
        		try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					 log.error("Failed to close the file stream", e);
				}
        	}
        	if(is1 != null){
        		try {
					is1.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("Failed to close the file stream", e);
				}
        	}
        }
    }
    
    
    /**
     * This should be used carefully in non-streaming transportsd
     //* @param recordedMsgsFileName
     */
    public void replayDataInFixedBufferSize(){
    	File file =new File(recordedMsgsFileName);
        FileInputStream fis = null;
        DataInputStream dis = null;
        try {
            fis = new FileInputStream(file);
            dis = new DataInputStream(fis);

            long totalSize = file.length();
            
            int chunkSize = 8388608;
            if(bufferSize != 0){
                chunkSize = bufferSize;
            }
            int chunkCount = (int) (totalSize/chunkSize);
            int remainingSize = (int) (totalSize-(chunkSize*chunkCount));
            
            log.info("Total size :"+totalSize);
            byte[]big = new byte[chunkSize];
            byte[]small = new byte[remainingSize];
            long st = System.nanoTime();
            while(chunkCount > 0){
                dis.readFully(big);
                send(big);
                chunkCount--;
                if(replayInterval > 0 ){
                    GMDCitiBinaryUtil.waitForMicroSecond(replayInterval);
                }
            }
            long et = System.nanoTime();
            log.info("Time to replay: "+ (et-st));
            dis.readFully(small);
            send(small);
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException", e);
        } catch (IOException e) {
            log.error("IOException", e);
        }
        finally{
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("Failed to close the file stream", e);
				}
			}
			if(dis != null){
				try {
					dis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("Failed to close the file stream", e);
				}
			}
		}
    }
    
    
    
    public void sendCancelReplace(String symbol, long cancelPrice, long price, char side, int cancelsize,int size, int feedId, int mktCtrId, boolean isSnapshot) {
        this.feedId=feedId;
        GMDQuoteMsg msg = new GMDQuoteMsg((byte) 2);
        msg.setSymbol(symbol);
        byte idx=0;
        msg.getQuoteAtIdx(idx).setFeedId((short) feedId);
        msg.getQuoteAtIdx(idx).setMktCtrId((byte) mktCtrId);
        msg.getQuoteAtIdx(idx).setPrice(cancelPrice);
        msg.getQuoteAtIdx(idx).setSize(cancelsize);
        msg.getQuoteAtIdx(idx).setSide(side);
        idx++;
        
        msg.getQuoteAtIdx(idx).setFeedId((short) feedId);
        msg.getQuoteAtIdx(idx).setMktCtrId((byte) mktCtrId);
        msg.getQuoteAtIdx(idx).setPrice(price);
        msg.getQuoteAtIdx(idx).setSize(size);
        msg.getQuoteAtIdx(idx).setSide(side);
        
        if (isSnapshot) {
            send(msg, true);
        } else {
            send(msg);
        }
        log.info(msg);
    }  
    
    public void sendLULDMsg(String symbol, long lowerLimitPrice, long upperLimitPrice, char priceBandIndicator, long priceBandEffectiveTime, int feedId ){
        this.feedId = feedId;
        GMDLULDBandMsg msg = new GMDLULDBandMsg();
        msg.setSymbol(symbol);
        msg.setLowerLimitPrice(lowerLimitPrice);
        msg.setUpperLimitPrice(upperLimitPrice);
        msg.setPriceBandIndicator(priceBandIndicator);
        msg.setPriceBandEffectiveTime(priceBandEffectiveTime);
        send(msg);
        log.info("Sent: "+ msg);
    }
}
