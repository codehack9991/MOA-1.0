package fast.common.context;

import static org.junit.Assert.*;

import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.citi.cet.automation.framework.marketdata.MarketData;
import com.citi.cet.automation.framework.marketdata.Quote;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest(MarketData.class)
public class TestHelperMethods {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void UpdatePriceForSymbolIfNeeded_passUpdatePrice() throws Exception{
		EvalScope evalScope = new EvalScope();
		Map<String, ArrayList<String>> _threadParams = new HashMap<>();
		ArrayList<String> symbolList = new ArrayList<>();
		symbolList.add("IBM");
		_threadParams.put("Symbol", symbolList);
		ArrayList<String> updatePriceForSymbol = new ArrayList<>();
		updatePriceForSymbol.add("true");
		_threadParams.put("UpdatePriceForSymbol", updatePriceForSymbol);
		evalScope.setThreadParams(_threadParams, 1);
		ScenarioContext scenarioContext = new ScenarioContext("test");
		Whitebox.setInternalState(scenarioContext, "_evalScope", evalScope);
		HelperMethods.cachedSymbolPrices=new HashMap();
		HelperMethods.cachedSymbolPrices.put("IBM", 12);
		HelperMethods.UpdatePriceForSymbolIfNeeded(scenarioContext);
		assertEquals(scenarioContext.processString("$Price"),"12");

	}
	@Test
	public void getPriceForSymbolFromReuters_nullDriverException() throws Exception{
		HelperMethods.cachedSymbolPrices=new HashMap();
		try{
		   HelperMethods.getPriceForSymbolFromReuters("IBM");
		}catch(RuntimeException e){
			assertTrue(e.getMessage().contains("Can't extract price from reuters.com: $Symbol=IBM"));
		}
	}
	
	@Test
	public void getRandom_passRandomNumber(){
		int random = HelperMethods.getRandom(10, 1000);
		assertTrue(10 <= random && random <= 1000);
	}	
	@Test
	public void Mod_positiveWithOneDecimal() {
		assertEquals(HelperMethods.Mod(Double.parseDouble("13"), Double.parseDouble("6")), Double.parseDouble("1.0"));
	}

	@Test
	public void Mod_negativeWithOneDecimal() {
		assertEquals(HelperMethods.Mod(Double.parseDouble("-13"), Double.parseDouble("6")), Double.parseDouble("-1.0"));
	}
	@Test
	public void Mod_decimaleWithOneDecimal() {
		assertEquals(HelperMethods.Mod(Double.parseDouble("0.5"), Double.parseDouble("0.3")),
				Double.parseDouble("0.2"));
	}
	@Test
	public void Round_roundUpWithTwoDecimal() {
		assertEquals(HelperMethods.Round(Double.parseDouble("1.11987"), 2), Double.parseDouble("1.12"));
	}
	@Test
	public void Round_roundDownWithTwoDecimal() {
		assertEquals(HelperMethods.Round(Double.parseDouble("1.11487"), 2), Double.parseDouble("1.11"));
	}
	@Test
	public void FormatInt_FormatWithTwoDecimal(){
		String priceStr = HelperMethods.FormatInt(12, "0.00");
		assertEquals("12.00",priceStr);
	}
	@Test
	public void FormatNumber_FormatWithTwoDecimal(){
		String priceStr = HelperMethods.FormatNumber(12.3330, "0.00");
		assertEquals("12.34",priceStr);
	}
	@Test
	public void getQuotes_getNumberWithDecimal(){
		PowerMockito.mockStatic(MarketData.class);
		MarketData marketData = mock(MarketData.class);
		Quote quote = mock(Quote.class);
		when(marketData.getQuotes("testSymbol")).thenReturn(quote);
		when(quote.getPrice()).thenReturn(10.0);
		String strQuote = HelperMethods.getQuotes("testSymbol");
		boolean correct = strQuote.matches("[-+]?[0-9]*\\.?[0-9]+");		
		assertTrue(correct);
	}
	@Test
	public void getOrCalculate_getCalcuValueWithExpression() throws ScriptException{
		HelperMethods.cachedCalculations=new HashMap();
		String result = HelperMethods.getOrCalculate("123","123+235");
		assertEquals(result,"358");
	}
	@Test
	public void getOrCalculate_getCalcuValueFromCache() throws ScriptException{
		HelperMethods.cachedCalculations=new HashMap();
		HelperMethods.cachedCalculations.put("12", "666");
		String result = HelperMethods.getOrCalculate("12","123+235");
		assertEquals(result,"666");
	}
	
	@Test
	public void addBusinessDaysFromToday() throws ParseException{
		String result = HelperMethods.addBusinessDaysFromToday(3);
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		Date date = format.parse(result);
		Date checknow = new Date();
		assertTrue(date.after(checknow));
	}
	
	@Test
	public void createTsID_getRandomID(){
		String ID =  HelperMethods.createTsID();
		boolean correct = ID.matches("[-+]?[0-9]*\\.?[0-9]+");		
		assertTrue(correct);
	}
	@Test
	public void EscapseReservedCharacters_containsSpecialCharacters() {
		assertEquals(HelperMethods.EscapseReservedCharacters("SIDE=ASD|HELLO"), "SIDE=ASDÂ¶HELLO");
	}
	
	@Test
	public void UnEscapseReservedCharacters_convertToFixSep() {
		assertEquals(HelperMethods.UnEscapseReservedCharacters("SIDE=ASDÂ¶HELLO"),"SIDE=ASD|HELLO");
	}
	@Test
	public void processStringToMap(){
		String str =
			     "{" 
			   + "  \"geodata\": [" 
			   + "    {" 
			   + "      \"id\": \"1\"," 
			   + "      \"name\": \"Julie Sherman\","                  
			   + "      \"gender\" : \"female\"," 
			   + "      \"latitude\" : \"37.33774833333334\"," 
			   + "      \"longitude\" : \"-121.88670166666667\""
			   + "    }," 
			   + "    {" 
			   + "      \"id\": \"2\"," 
			   + "      \"name\": \"Johnny Depp\","          
			   + "      \"gender\" : \"male\"," 
			   + "      \"latitude\" : \"37.336453\"," 
			   + "      \"longitude\" : \"-121.884985\""
			   + "    }" 
			   + "  ]" 
			   + "}";
		Map<String,Object> result = HelperMethods.processStringToMap(str);
		ArrayList<Map<String,String>> items= (ArrayList<Map<String,String>>)result.get("geodata");
		assertTrue(items.get(0).get("name").equals("Julie Sherman"));
	}
}
