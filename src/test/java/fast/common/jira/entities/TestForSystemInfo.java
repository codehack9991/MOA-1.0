package fast.common.jira.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TestForSystemInfo {

	
	private SystemInfo sysInfo;

	@Before
	public void Before() {
		sysInfo = new SystemInfo();
	}
	
	@Test
	public void testForSetGetJiraDbBuild() {		
		sysInfo.setJiraDbBuild("build");
		assertEquals("build", sysInfo.getJiraDbBuild());
	}
	
	@Test
	public void testForSetGetAppServer() {		
		sysInfo.setJiraAppServer("AppServer");		
		assertEquals("AppServer", sysInfo.getJiraAppServer());
	}
	
	@Test
	public void testForSetGetJiraDbType() {		
		sysInfo.setJiraDbType("DbType");		
		assertEquals("DbType", sysInfo.getJiraDbType());
	}
}
