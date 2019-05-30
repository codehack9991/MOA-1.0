package fast.common.jira;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import fast.common.context.StepResult;
import fast.common.jira.JiraUploader.JiraExecutionStatus;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class TestJiraUploader {

	@InjectMocks
	private JiraUploader jiraUploader=new JiraUploader();
	
	@Mock 
	private ZapiRestService service;
	
	@Before
	public void setUp() {

	}

	@Test
	public void testForJiraUploderInitialization() throws Exception {
		JiraUploader.getInstance();
	}

	@Test
	public void testForJiraUploaderIssueKeyFetcher() throws Exception {
		String originStr = "C167813-326 SLKHGFLKWHGSLKGHLKSW";
		assertEquals("C167813-326", JiraUploader.getIssueKeyFromScenarioName(originStr));
		originStr = " C167813-326 SLKHG FLKWHGSL KGHLKSW ";
		assertEquals("C167813-326", JiraUploader.getIssueKeyFromScenarioName(originStr));
		originStr = "C167813- SLKHGFLKWHGSLKGHLKSW";
		assertNull(JiraUploader.getIssueKeyFromScenarioName(originStr));
		originStr = "C1678 SLKHGFLKWHGSLKGHLKSW";
		assertNull(JiraUploader.getIssueKeyFromScenarioName(originStr));
		originStr = "167813-326 SLKHGFLKWHGSLKGHLKSW";
		assertNull(JiraUploader.getIssueKeyFromScenarioName(originStr));
		originStr = "slkkgjwlksghshlgkjskjg";
		assertNull(JiraUploader.getIssueKeyFromScenarioName(originStr));
		originStr = null;
		assertNull(JiraUploader.getIssueKeyFromScenarioName(originStr));
		originStr = "C167813-326SLKHGFLKWHGSLKGHLKSW";
		assertNull(JiraUploader.getIssueKeyFromScenarioName(originStr));
		originStr = "C167813-326SLKH GFLKWHGSLKGHLKSW";
		assertNull(JiraUploader.getIssueKeyFromScenarioName(originStr));
	}

	@Test
	public void testForConvertStatus() {

		String status = StepResult.STEP_RESULT_PASS;
		assertEquals(1, JiraExecutionStatus.convertToJiraExecutionStatus(status).getValue());
		status = StepResult.STEP_RESULT_FAIL;
		assertEquals(2, JiraExecutionStatus.convertToJiraExecutionStatus(status).getValue());
		status = StepResult.STEP_RESULT_SKIP;
		assertEquals(-1, JiraExecutionStatus.convertToJiraExecutionStatus(status).getValue());
		status = "";
		assertEquals(-1, JiraExecutionStatus.convertToJiraExecutionStatus(status).getValue());
	}

	@Test
	public void testPropertiesSetterGetter() throws Exception {
		JiraUploader.getInstance().setUrl("url");
		String url = JiraUploader.getInstance().getUrl();
		assertEquals("url", url);
		boolean enabled = JiraUploader.getInstance().getEnabled();
		assertFalse(enabled);
	}

	@Test
	public void testInit() {
		JiraUploader.getInstance().init("url", "userName", "password", "prjName", "verName", "cycName");
		String url = JiraUploader.getInstance().getUrl();
		assertEquals("url", url);
	}
	
	
	@Test
	public void constructor_fieldNotSet(){
		JiraUploader uploader=new JiraUploader();
		assertNull(uploader.getUrl());
		assertNull(uploader.getUser());
		assertEquals(false, uploader.getEnabled());
	}
	
	@Test 
	public void setUrl_setProperly(){
		JiraUploader uploader=new JiraUploader();
		uploader.setUrl("");
		assertEquals("", uploader.getUrl());
	}
	
	@Test
	public void uploadNewExecutionInfo_disable() throws RequestNotSucceedException{
		JiraUploader uploader=new JiraUploader();
		uploader.uploadNewExecutionInfo("", 0, "", new ArrayList<>());
		assertEquals(false, uploader.getEnabled());
	}
	
	@Test
	public void getIssueKeyFromScenarioName_nullOrEmpty(){
		assertNull(JiraUploader.getIssueKeyFromScenarioName(""));
		assertNull(JiraUploader.getIssueKeyFromScenarioName(null));
	}
	
	@Test
	public void getIssueKeyFromScenarioName_notContainKey(){
		assertNull(JiraUploader.getIssueKeyFromScenarioName("scenario"));
	}
	
	@Test
	public void getIssueKeyFromScenarioName_rightJiraKey(){
		assertEquals(JiraUploader.getIssueKeyFromScenarioName("C167813-326 scenario"),"C167813-326");
	}
	
	@Test
	public void getIssueKeyFromScenarioName_wrongJiraKey(){
		assertNull(JiraUploader.getIssueKeyFromScenarioName("X167813-326 scenario"));
	}
	
//	@Test
//	public void init_passed_withFiveParams() throws Exception{
//		PowerMockito.mockStatic(ZapiRestService.class);
//		List<PVItem> items=new ArrayList<>();
//		PVItem item=new PVItem();
//		item.setLabel("FAST");
//		item.setValue("1");
//		items.add(item);
////		PowerMockito.whenNew(ZapiRestService.class).withArguments(any(String.class), any(String.class),any(String.class)).thenReturn(service);
////		JiraUploader uploader=Mockito.mock(JiraUploader.class);
//		when(service.getAllProjects()).thenReturn(items);		
//		when(service.getVersionsByPrjId(any(String.class))).thenReturn(items);
//		List<Cycle> cycles=new ArrayList<>();
//		Cycle cycle=new Cycle();
//		cycle.setId("1");	
//		cycle.setName("FAST");
//		cycles.add(cycle);
//		when(service.getCyclesByVerId(any(String.class))).thenReturn(cycles);		
//		when(ZapiRestService.generateService("","","")).thenReturn(service);
//		jiraUploader.init("", "", "FAST", "FAST", "FAST");
//		assertEquals("1", Whitebox.getInternalState(jiraUploader,"prjId"));
//		assertEquals("1", Whitebox.getInternalState(jiraUploader,"verId"));
//		assertEquals("1", Whitebox.getInternalState(jiraUploader,"cycId"));
//	}
}
