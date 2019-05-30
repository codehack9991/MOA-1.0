package fast.common.glue;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cucumber.api.Scenario;
import fast.common.agents.AgentsManager;
import fast.common.agents.UiaAgent;
import fast.common.context.ScenarioContext;
import fast.common.context.UiaStepResult;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({BaseCommonStepDefs.class,AgentsManager.class})
public class TestGuiCommonStepDefs {

private GuiCommonStepDefs gui;
	
	@Mock
	private BaseCommonStepDefs base;
	
	@Mock
	private ScenarioContext scenarioContext;
	
	@Mock
	private AgentsManager agentsManager;
	
	@Mock UiaAgent uiaAgent;
	
	private Scenario scenario;
	
	@Before
	public void setUp() throws Exception {
		gui = new GuiCommonStepDefs();
		scenario = new Scenario() {
			
			@Override
			public void write(String text) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isFailed() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public String getStatus() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Collection<String> getSourceTagNames() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "invalidName";
			}
			
			@Override
			public String getId() {
				// TODO Auto-generated method stub
				return "invalidId";
			}
			
			@Override
			public void embed(byte[] data, String mimeType) {
				// TODO Auto-generated method stub
				
			}
		};
		PowerMockito.mockStatic(BaseCommonStepDefs.class);
		when(BaseCommonStepDefs.getScenarioContext()).thenReturn(scenarioContext);
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
	}
	
	
	@Test
	public void testSelectItemFromComboBox() throws Throwable{
		
		when(scenarioContext.processString("testStrA")).thenReturn("ComboBox");
		when(scenarioContext.processString("testStrB")).thenReturn("Item");
		
		doNothing().when(uiaAgent).run("selectItemInCombobox","ComboBox","Item");
		when(agentsManager.getOrCreateAgent("Agent")).thenReturn(uiaAgent);
		
		gui.selectItemFromComboBox("Agent", "Item", "ComboBox");
	}
	
	@Test
	public void testForGetColorOnControl() throws Throwable{
		
		when(scenarioContext.processString("testStrA")).thenReturn("ControlName");
		
		when(uiaAgent.runWithResult("getColorOnControlPoint","ControlName","0","0")).thenReturn(new UiaStepResult("SUCCESS:RGB(255,255,255)"));
		when(agentsManager.getOrCreateAgent("Agent")).thenReturn(uiaAgent);
		
		gui.getRgbColorOnControlPoint("Agent", "0", "0", "ControlName", "color");;
	}
	
	@Test
	public void testForGetCheckBoxStatel() throws Throwable{
		
		when(scenarioContext.processString("testStrA")).thenReturn("ControlName");
		
		when(uiaAgent.runWithResult("getCheckBoxState","ControlName")).thenReturn(new UiaStepResult("SUCCESS:False"));
		when(agentsManager.getOrCreateAgent("Agent")).thenReturn(uiaAgent);
		
		gui.getCheckBoxState("Agent", "ControlName", "state");
	}
}
