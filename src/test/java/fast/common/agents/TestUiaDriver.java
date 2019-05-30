package fast.common.agents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import fast.common.context.StepResult;
import fast.common.context.UiaStepResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ UiaDriver.class, RandomAccessFile.class })
@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
@SuppressStaticInitializationFor({"java.io.RandomAccessFile", "org.powermock.reflect.Whitebox"})
public class TestUiaDriver{	
	private RandomAccessFile pipe;	
	private UiaDriver driver = new UiaDriver("path", "repo");
	
	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);
		
		//initialize the mock pipe
		pipe = PowerMockito.mock(RandomAccessFile.class);
		doNothing().when(pipe).write(any());
		PowerMockito.when(pipe.length()).thenReturn(0L);
		Whitebox.setInternalState(driver, "pipe", pipe);	
	}
	
	@Test
	public void constructor_fieldsSetProperly() throws IllegalArgumentException, IllegalAccessException {
		UiaDriver myDriver = new UiaDriver("path", "repo", "true");		
		assertEquals("path", Whitebox.getInternalState(myDriver, "driverPath"));
		assertEquals("repo", Whitebox.getInternalState(myDriver, "uiRepo"));
		assertNull(Whitebox.getInternalState(myDriver, "pipe"));
		assertNull(Whitebox.getInternalState(myDriver, "driverProcess"));
		assertFalse(Whitebox.getInternalState(myDriver, "isAlive"));
		assertTrue(Whitebox.getInternalState(myDriver, "quietMode"));
	}

	@Test
	public void run_passed() throws Throwable{		
		when(pipe.readLine()).thenReturn("SUCCESS:OK");
		
		StepResult result = driver.run("action");
		assertEquals(StepResult.Status.Passed, result.getStatus());
		assertEquals("OK", result.getFieldValue(UiaStepResult.DefaultField));
	}
	
	@Test
	public void run_failed() throws IOException {		
		when(pipe.readLine()).thenReturn("ERROR:Cannot find the control");
		
		try {
			driver.run("action");
			fail("Expected an Exception to be thrown");
		} catch (Exception ex){
			assertEquals("Cannot find the control", ex.getMessage());
		}
	}	
	
	@Test
	public void start_failed(){
		assertFalse(driver.start());		
	}	
	
}
