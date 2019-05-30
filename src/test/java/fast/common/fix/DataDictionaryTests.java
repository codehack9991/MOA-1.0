package fast.common.fix;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;

import quickfix.ConfigError;

public class DataDictionaryTests {
	
	@Test
	public void verifyGroupTagsForMessage42() throws ConfigError {
		DataDictionary dictionary = new DataDictionary("config/quickfix_spec/FIX42New.xml");
		Assert.assertTrue("Tag 448 is a repeating group", dictionary.IsRepeatingTag(null, 448));
		Assert.assertFalse("Tag 564 is a not repeating group", dictionary.IsRepeatingTag(null, 564));
		
		Assert.assertTrue("Tag 336 is a repeating group in D", dictionary.IsRepeatingTag("D", 336));
		Assert.assertTrue("Tag 564 is a repeating group in AB", dictionary.IsRepeatingTag("AB", 564));
	}
	
	@Test
	public void verifyGroupTagsForMessage44() throws ConfigError {
		DataDictionary dictionary = new DataDictionary("config/quickfix_spec/FIX44New.xml");
		Assert.assertTrue("Tag 448 is a repeating group", dictionary.IsRepeatingTag(null, 448));
		Assert.assertFalse("Tag 564 is a not repeating group", dictionary.IsRepeatingTag(null, 564));
		
		Assert.assertTrue("Tag 448 is a repeating group", dictionary.IsRepeatingTag("UZDO", 448));
		Assert.assertFalse("Tag 949 is a not repeating group", dictionary.IsRepeatingTag("AB", 949));
		Assert.assertFalse("Tag 953 is a not repeating group", dictionary.IsRepeatingTag("AB", 953));
	}
	
	@Test
	public void verifyTagInComponentGroup() throws ConfigError, XPathExpressionException {
		DataDictionary dictionary = new DataDictionary("config/quickfix_spec/FIX44New.xml");
		Assert.assertTrue("Tag 448 is a repeating group", dictionary.IsRepeatingTag("D", 448));
		Assert.assertFalse("Tag 11 is not a repeating group", dictionary.IsRepeatingTag("NewOrderSingle", 11));
	}
	
	@Test
	public void verifyTagInMsgName() throws ConfigError, XPathExpressionException {
		DataDictionary dictionary = new DataDictionary("config/quickfix_spec/FIX44New.xml");
		Assert.assertTrue("Tag 448 is a repeating group", dictionary.IsRepeatingTag("NewOrderSingle", 448));
		Assert.assertFalse("Tag 523 is a repeating group", dictionary.IsRepeatingTag("D", 523));
	}
	
	@Test
	public void verifyTagValueLookupValue() throws ConfigError, XPathExpressionException {
		DataDictionary dictionary = new DataDictionary("config/quickfix_spec/FIX44New.xml");
		Assert.assertTrue("Tag 54 value BUY -> 1", dictionary.getReverseValueName(54, "BUY").equalsIgnoreCase("1"));
		Assert.assertTrue("Tag 54 value 1 -> 1", dictionary.getReverseValueName(54, "1").equalsIgnoreCase("1"));
	}

}
