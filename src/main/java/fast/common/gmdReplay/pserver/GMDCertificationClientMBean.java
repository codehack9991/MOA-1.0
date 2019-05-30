package fast.common.gmdReplay.pserver;

import com.citi.gmd.client.mbeans.Description;
import com.citi.gmd.client.mbeans.PName;

/**
 * Created by ab56783 on 08/10/2017.
 */
public interface GMDCertificationClientMBean {

    @Description("Send log on response")
    void sendLogonResponse(@PName(value = "Client Id :") String clientId);

    @Description("Send log on reject")
    void sendLogonReject(@PName(value = "Client Id :") int reasonCode, @PName(value = "ReasonString :") String reasonStr);

    @Description("Send heart beat response")
    void sendHeartBeatResponse();

    @Description("Send Quote Message")
    void sendQuoteMessage(@PName(value = "Symbol :") String symbol, @PName(value = "Price :") long price, @PName(value = "Side :") char side, @PName(value = "Size :") int size, @PName(value = "FeedId :") int feedId, @PName(value = "MktCtrId :") int mktCtrId, @PName(value = "Index :") short idx, @PName(value = "isSnapsot :") boolean isSnapshot, @PName(value = "MktMkrId :") String mktMkrId);

    @Description("Send NBBO")
    void sendNBBO(@PName(value = "Symbol :") String symbol, @PName(value = "Price :") long price, @PName(value = "Size :") int size, @PName(value = "FeedId :") int feedId);

    @Description("Send sendNBBOBothSideOKLevelBased")
    void sendNBBOBothSideOKLevelBased(@PName(value = "Symbol :") String symbol, @PName(value = "Price :") long price, @PName(value = "Size :") int size);

    @Description("Send sendNBBOBLevelBasedBuySideValid")
    void sendNBBOBLevelBasedBuySideValid(@PName(value = "Symbol :") String symbol, @PName(value = "Price :") long price, @PName(value = "Size :") int size);

    @Description("Send sendNBBOBLevelBasedSellSideValid")
    void sendNBBOBLevelBasedSellSideValid(@PName(value = "Symbol :") String symbol, @PName(value = "Price :") long price, @PName(value = "Size :") int size);

    @Description("Send Trade Message")
    void sendTradeMessage(@PName(value = "TradeOrderId :") long tradeOrderId, @PName(value = "TradeMatchId :") long tradeMatchId, @PName(value = "Symbol :") String symbol, @PName(value = "TradeCond :") String tradeCond, @PName(value = "RegionalTradeFlags :") byte regionalTradeFlags, @PName(value = "ConsolTradeFlags :") short consolTradeFlags, @PName(value = "Price :") long price, @PName(value = "Size :") int size, @PName(value = "FeedId :") int feedId, @PName(value = "MktCtrId :") int mktCtrId, @PName(value = "isSnapsot :") boolean isSnapshot, @PName(value = "SubscriptionId :") short SubscriptionId);

    @Description("Send Order Message")
    void sendOrderMessage(@PName(value = "Symbol :") String symbol, @PName(value = "Price :") long price, @PName(value = "Size :") int size, @PName(value = "Side :") char side, @PName(value = "FeedId :") int feedId, @PName(value = "MktCtrId :") int mktCtrId, @PName(value = "MsgType :") int msgType, @PName(value = "OrderId :") long orderId, @PName(value = "isSnapsot :") boolean isSnapshot);

    @Description("Send Imbalance Message")
    void sendImbalanceMessage(@PName(value = "Symbol :") String symbol, @PName(value = "Source :") short source, @PName(value = "Cross Type :") char crossType, @PName(value = "Imbalance Side :") char imbSide, @PName(value = "Imbalance Volume:") long imbVolume, @PName(value = "FeedId :") int feedId, @PName(value = "MktCtrId :") int mktCtrId, @PName(value = "RegImbInd :") char regImbInd, @PName(value = "PriceVarInd :") char priceVarInd, @PName(value = "MatchedVolume :") long matchedVolume, @PName(value = "MocOrMarketImbVolume :") long mocOrMarketImbVolume, @PName(value = "ClosingOnlyClearingPrice :") long closingOnlyClearingPrice, @PName(value = "AuctionClearingPrice :") long auctionClearingPrice, @PName(value = "CurRefPrice :") long curRefPrice, @PName(value = "AuctionTime :") long auctionTime);

    @Description("Send Instrument statusMessage")
    void sendInstrumentStatusMessage(@PName(value = "Symbol :") String symbol, @PName(value = "Regulatory Trading Status :") char regulatoryTradingStatus, @PName(value = "Status Code :") short statusCode, @PName(value = "FeedId :") int feedId, @PName(value = "MktCtrId :") byte mktCtrId, @PName(value = "RegShoInd :") char regShoInd, @PName(value = "MktCtrTradingStatus :") String mktCtrTradingStatus);

    @Description("Send Cache Clear Message")
    void sendCacheClearMessage(@PName(value = "Symbol :") String symbol, @PName(value = "Clear BitMask:") short clearBitMask, @PName(value = "MktCtrId :") short mktCtrId);

    @Description("Send Feed Status Message")
    void sendFeedStatusMessage(@PName(value = "FeedID :") short feedId, @PName(value = "Feed Status :") byte feedStatus, @PName(value = "SymbolRangeAffected :") String symbolRangeAffected, @PName(value = "IpAddressAffected :") String ipAddressAffected, @PName(value = "FeedSubId :") byte feedSubId, @PName(value = "DiscReason :") char discReason);

    @Description("Send MarketStatus Message")
    void sendMarketStatusMessage(@PName(value = "MarketCenterId :") byte mktCtrId, @PName(value = "Market State:") char mktState, @PName(value = "Status Code :") short statusCode);

    @Description("Send PassThrough Message")
    void sendPassThruMessage(@PName(value = "Symbol :") String symbol, @PName(value = "PassThruType :") byte passThruType, @PName(value = "PassThruDataLength :") short passThruDataLength, @PName(value = "MktCtrId :") byte mktCtrId);

    @Description("Send PeggedOrder Message")
    void sendPeggedOrderMessage(@PName(value = "Symbol :") String symbol, @PName(value = "Price :") long price, @PName(value = "Size :") int size, @PName(value = "Side :") char side, @PName(value = "IncMsgType :") byte incMsgType, @PName(value = "MktCtrId :") byte mktCtrId, @PName(value = "OrderId :") long orderId, @PName(value = "Source :") String source, @PName(value = "OrderCat :") byte orderCat, @PName(value = "Operation :") byte operation, @PName(value = "MarketId :") byte marketId, @PName(value = "OrderStatus :") char orderStatus, @PName(value = "ReferencePriceCode :") char referencePriceCode, @PName(value = "MinExecQuantity :") int minExecQuantity, @PName(value = "Attributes :") int attributes, @PName(value = "PegOffset :") long pegOffset);

    @Description("Send TradeDetailed Message")
    void sendTradeMessageDetailed(@PName(value = "Symbol :") String symbol, @PName(value = "feedId :") int feedId, @PName(value = "TradePrice :") long tradePrice, @PName(value = "TradeSize :") int tradeSize, @PName(value = "CumVol :") int cumVol, @PName(value = "MktCtrId :") byte mktCtrId, @PName(value = "TradeCond :") String tradeCond, @PName(value = "NumOfPrevTrades :") byte numOfPrevTrades, @PName(value = "SubMktCtrId :") byte subMktCtrId, @PName(value = "bookKeepBitMask :") byte bookKeepBitMask, @PName(value = "vwapPrice :") long vwapPrice, @PName(value = "totDollarVolTraded :") long totDollarVolTraded, @PName(value = "highPrice :") long highPrice, @PName(value = "lowPrice :") long lowPrice, @PName(value = "netChange :") long netChange, @PName(value = "openPrice :") long openPrice, @PName(value = "primaryOpenPrice :") long primaryOpenPrice, @PName(value = "closingPriceCur :") long closingPriceCur, @PName(value = "primaryClosingPriceCur :") long primaryClosingPriceCur, @PName(value = "closingPricePrev :") long closingPricePrev, @PName(value = "primaryClosingPricePrev :") long primaryClosingPricePrev, @PName(value = "cumVolHidden :") int cumVolHidden, @PName(value = "cumVolDisplayed :") int cumVolDisplayed, @PName(value = "tradeMatchId :") long tradeMatchId, @PName(value = "cumOffExchVol :") long cumOffExchVol, @PName(value = "cumOnExchCount :") int cumOnExchCount, @PName(value = "cumOffExchCount :") int cumOffExchCount, @PName(value = "tradeOrderId :") long tradeOrderId, @PName(value = "cumVolEx :") long cumVolEx, @PName(value = "primaryOnExchLastTradePrice :") long primaryOnExchLastTradePrice);

    @Description("Send Static Info Message")
    void sendStaticInfoMessage(@PName(value = "Symbol :") String symbol, @PName(value = "FullName :") String fullName, @PName(value = "SecurityType :") short secType, @PName(value = "SecuritySubType:") byte secSubType, @PName(value = "FeedId :") short feedId, @PName(value = "MktCtrId :") byte mktCtrId, @PName(value = "UpperLimit :") long upperLimit, @PName(value = "LowerLimit :") long lowerLimit, @PName(value = "OpenPrice :") long openPrice, @PName(value = "OpenPricePrimary :") long openPricePrimary, @PName(value = "ClosePriceCur :") long closePriceCur, @PName(value = "ClosePricePrev :") long closePricePrev);

    @Description("Send recorded Msgs")
    void startReplaying(@PName(value = "RecordedMessages File Name :") String fileName, @PName(value = "buffer size") int bufSize, @PName(value = "Delay between two msgs:") int delay);

    @Description("Send QuoteMsg")
    void startSendingRandomQuote(@PName(value = "Nano sec Delay between two msgs:") int delay);

    @Description("testFastInsideAtomicUpdateOnceSideDelete")
    public void testFastInsideAtomicUpdateOnceSideDelete(@PName(value = "Symbol :") String symbol, @PName(value = "Price :") long price, @PName(value = "Size :") int size, @PName(value = "Side :") char side);
    @Description("testFastInsideAtomicUpdateOnceSide")
    public void testFastInsideAtomicUpdateOnceSide(@PName(value = "Symbol :") String symbol, @PName(value = "Price :") long price, @PName(value = "Size :") int size, @PName(value = "Side :") char side);
    @Description("testFastInsideAtomicUpdate")
    public void testFastInsideAtomicUpdate(@PName(value = "Symbol :") String symbol, @PName(value = "Price :") long price, @PName(value = "Size :") int size);

    @Description("Send Cacel Replace")
    public void sendCancelReplace(@PName(value = "Symbol :") String symbol, @PName(value = "CancelPrice :") long cancelPrice, @PName(value = "Price :") long price, @PName(value = "Side :") char side, @PName(value = "CancelSize :") int cancelSize, @PName(value = "Size :") int size, @PName(value = "FeedId :") int feedId, @PName(value = "MktCtrId :") int mktCtrId, @PName(value = "isSnapsot :") boolean isSnapshot);

    @Description("sendLULDMsg")
    public void sendLULDMsg(@PName(value = "Symbol :") String symbol, @PName(value = "lowerLimitPrice :") long lowerLimitPrice, @PName(value = "uperLimitPrice :") long uperLimitPrice, @PName(value = "priceBandIndicator :") char priceBandIndicator, @PName(value = "priceBandEffectiveTime :") long priceBandEffectiveTime, @PName(value = "feedId :") int feedId);

}
