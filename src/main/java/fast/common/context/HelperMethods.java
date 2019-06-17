package fast.common.context;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.apache.commons.lang.math.NumberUtils;

import com.citi.cet.automation.framework.core.Phantom;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fast.common.agents.AgentsManager;
import fast.common.agents.WebBrowserAgent;
import fast.common.fix.FixHelper;
import fast.common.logging.FastLogger;
import fast.common.utilities.BusinessDaysInfo;

/**
 * 
 */
public class HelperMethods {
	static FastLogger logger = FastLogger.getLogger("HelperMethods");

	public static void UpdatePriceForSymbolIfNeeded(ScenarioContext scenarioContext) throws Exception {
		try {

			boolean needToUpdate = false;
			try {
				String str = scenarioContext.processString("$UpdatePriceForSymbol");
				needToUpdate = Boolean.parseBoolean(str);
			} catch (Exception e) {
				needToUpdate = false;
			}

			if (needToUpdate) {
				String symbol = scenarioContext.processString("$Symbol");
				Number price = getPriceForSymbolFromReuters(symbol);
				String priceStr = FormatNumber(price, "#.##");
				scenarioContext.setThreadParam("Price", priceStr);
			}

		} catch (Exception e) {
			logger.error(String.format("UpdatePriceForSymbolIfNeeded failed: %s", e.toString()));
			throw e;
		}
	}

	static HashMap<String, Number> cachedSymbolPrices = new HashMap<>();

	public static Number getPriceForSymbolFromReuters(String symbol) {
		Number priceNum;
		String url = "http://www.reuters.com/finance/stocks/overview?symbol=" + symbol;
		String priceStr = String.format("Error: can't get price for '%s' using '%s'", symbol, url);
		if (cachedSymbolPrices.containsKey(symbol)) {
			return cachedSymbolPrices.get(symbol);
		}

		try {
			WebBrowserAgent webBrowser = AgentsManager.getInstance().getOrCreateAgent("WebBrowserForReuters");
			webBrowser.openUrl(url);
			priceStr = webBrowser.getText("SymbolPage.SymbolPrice");

			priceNum = NumberUtils.createNumber(priceStr);
			/// priceStr = FormatNumber(priceNum, "#.##");
			cachedSymbolPrices.put(symbol, priceNum);
			logger.info(String.format("Successfully extracted price from reuters.com: $Symbol=%s has $Price=%s", symbol,
					priceStr));
		} catch (Exception e) {
			String error = String.format("Can't extract price from reuters.com: $Symbol=%s", symbol);
			logger.error(error);
			throw new RuntimeException(error + " - " + e.toString());
		}
		return priceNum;
	}

	public static int getRandom(int from, int to) {
		int numOfNumbers = to - from + 1;
		return (int) (Math.random() * numOfNumbers) + from;
	}

	public static Number Mod(Number x, Number y) {
		return x.doubleValue() % y.doubleValue();
	}
	public static Number Round(Number value, Integer precision) {
		double scale = Math.pow(10, precision);
		return (double) Math.round(value.doubleValue() * scale) / scale;
	}

	public static String FormatInt(int value, String format) {
		DecimalFormat df = new DecimalFormat(format);
		String result = df.format(value);
		return result;
	}
	
	public static String FormatNumber(Number value, String format) {
		DecimalFormat df = new DecimalFormat(format);
		df.setRoundingMode(RoundingMode.CEILING);
		String result = df.format(value);
		return result;
	}

	public static String getQuotes(String s) {
		return Phantom.marketData.getQuotes(s).getPrice() + "";
	}

	static HashMap<String, String> cachedCalculations = new HashMap<>();

	public static String getOrCalculate(String key, String expression) throws ScriptException {
		if (cachedCalculations.containsKey(key)) {
			return cachedCalculations.get(key);
		}

		String result = EcmaScriptInterpreter.getInstance().interpret(expression);
		cachedCalculations.put(key, result);
		return result;
	}

	public static String addBusinessDaysFromToday(int days) {
		Date date = new Date();
		BusinessDaysInfo bDaysInfo = new BusinessDaysInfo();

		return bDaysInfo.addBusinessDays(date, days);
	}
		
	public static String createTsID(){
		String id="";
		Date now=new Date();
		
		@SuppressWarnings("deprecation")
		Date startOfThisYear=new Date(now.getYear(),0,1);
		
		int days=(int) ((now.getTime()-startOfThisYear.getTime())/(1000*3600*24));
		id+=String.valueOf(days+1);
		
		SimpleDateFormat sdf=new SimpleDateFormat("HHmmssSS");
		String timeStamp= sdf.format(now);
		
		id+=timeStamp;
		
		return id;
	}
	/**
	 * 	Use to fix user tag value contains FIX_SEP
	 */
	public static String EscapseReservedCharacters(String userStr){
		return userStr.contains(MapMessageTemplateHelper.MESSAGE_FIELD_SEP)?userStr.replaceAll("\\" + MapMessageTemplateHelper.MESSAGE_FIELD_SEP, FixHelper.FIX_SEP_ESCAPE+""):userStr;
	}
	
	public static String UnEscapseReservedCharacters(String userStr) {
		return userStr.contains(FixHelper.FIX_SEP_ESCAPE+"")?userStr.replaceAll(FixHelper.FIX_SEP_ESCAPE+"", "\\" + MapMessageTemplateHelper.MESSAGE_FIELD_SEP):userStr;
	}
	
	/**
	 * <p>Process json string to Map<String,Object> Object.
	 * 
	 * @param str the json string
	 * @return a Map<String,Object> object
	 * @since 1.7
	 */
	public static Map<String,Object> processStringToMap(String str){
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		Map<String, Object> mapObject = null;
		try {
			mapObject = objectMapper.readValue(str, Map.class);
		} catch (JsonParseException e) {
			logger.warn("JsonParseException: " + e.getMessage());
		} catch (JsonMappingException e) {
			logger.warn("JsonMappingException: " + e.getMessage());
		} catch (IOException e) {
			logger.warn("IOException: " + e.getMessage());
		}
		return mapObject;
	}
}
