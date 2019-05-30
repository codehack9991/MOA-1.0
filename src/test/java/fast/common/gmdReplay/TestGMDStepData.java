package fast.common.gmdReplay;

import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.citi.gmd.client.utils.structs.CString;
import com.google.gson.JsonElement;

import fast.common.gmdReplay.GMDReplayDataHelper.Outcome;
import fast.common.gmdReplay.client.GMDMessageError;

public class TestGMDStepData {

	private GMDReplayDataHelper dataHelper;

	@Before
	public void setUp() {
		Map rules = new HashMap<>();
		rules.put("LabelsForMessageMatching", new ArrayList<>());
		rules.put("LabelsToIgnoreCompletely", new ArrayList<>());
		rules.put("LabelsToIgnoreValue", new ArrayList<>());
		dataHelper = new GMDReplayDataHelper(rules, 0, 0, 0, false);
	}

	@Test
	public void testGMDStepDataConstructor() {
		GMDStepData stepData = new GMDStepData(1, null, "stepName", "");
		assertNotNull(stepData);
	}

	@Test
	public void testMessageStatus() {
		MessageStatus status = MessageStatus.INORDER;
		assertNotNull(status);
	}

	@Test
	public void testGMDdata() {
		GMDdata data = new GMDdata(null, null, null, null);
		assertNotNull(data);
	}

	@Test
	public void testDataProcessor() {
		dataProcessor processor = new dataProcessor(null, null, null, null, null, null, null);
		assertNotNull(processor);
	}

	@Test
	public void testCitiCStringAdapter() {
		CitiCStringAdapter adapter = new CitiCStringAdapter();
		JsonElement serialize = adapter.serialize(new CString("123"), null, null);
		assertNotNull(serialize);
	}

	@Test
	public void testMessageFlushTimer() {
		MessageFlushTimer timer = new MessageFlushTimer(null);
		assertNotNull(timer);
	}

	@Test
	public void testGMDReplayDataHelper() throws Exception {

		Map rules = new HashMap<>();
		rules.put("LabelsForMessageMatching", new ArrayList<>());
		rules.put("LabelsToIgnoreCompletely", new ArrayList<>());
		rules.put("LabelsToIgnoreValue", new ArrayList<>());
		GMDReplayDataHelper helper = new GMDReplayDataHelper(rules, 0, 0, 0, false);
		assertNotNull(helper);

		Map rules2 = new HashMap<>();
		List<String> list = new ArrayList<>();
		list.add("a");

		rules2.put("LabelsForMessageMatching", list);
		rules2.put("LabelsToIgnoreCompletely", list);
		rules2.put("LabelsToIgnoreValue", list);
		GMDReplayDataHelper helper2 = new GMDReplayDataHelper(rules2, 0, 0, 0, false);
		assertNotNull(helper2);
		String query = helper.createObjectSearchQuery("name", "symbol", "obj");
		assertNotNull(query);
	}

	@Test
	public void testGMDReplayDataHelperInitReporting() {

		Map rules = new HashMap<>();
		rules.put("LabelsForMessageMatching", new ArrayList<>());
		rules.put("LabelsToIgnoreCompletely", new ArrayList<>());
		rules.put("LabelsToIgnoreValue", new ArrayList<>());
		GMDReplayDataHelper helper = new GMDReplayDataHelper(rules, 0, 0, 0, false);
		assertNotNull(helper);
		try {
			helper.initReporting(null, null);
		} catch (Exception e) {
		}
	}

	@Test
	public void testGMDReplayDataHelperCheckMsgMatch() {
		int result = dataHelper.checkMsgMatch("A", "name", "O", "X", "ABC", null);
		assertEquals(result, -1);
		result = dataHelper.checkMsgMatch("A", "name", "name", "X", "ABC", null);
		assertEquals(result, -1);
		result = dataHelper.checkMsgMatch("A", "name", "name", "{}", "ABC", null);
		assertEquals(result, -1);
		GMDMessageError error = new GMDMessageError("A", "Value");
		result = dataHelper.checkMsgMatch("A", "name", "name", "{}", "{}", error);
		assertEquals(result, 0);
	}
	
	
	@Test
	public void testMDspeed() {
		dataHelper.setMDspeed(10);
		assertNotNull(dataHelper);
	}
	
	@Test
	public void testOutcome() {
		Outcome outCome = Outcome.INORDER_PARTIAL;
		assertNotNull(outCome);
	}
}
