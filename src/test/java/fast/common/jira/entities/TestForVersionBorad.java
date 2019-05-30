package fast.common.jira.entities;

import static org.mockito.Matchers.any;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class TestForVersionBoard {

	private VersionBoard versionBoard;

	@Before
	public void Before() {
		versionBoard = new VersionBoard();
	}
	
	@Test
	public void testForSetGetUnreleasedVersions() {
		ArrayList<Version> versionList = new ArrayList<Version>();
		Version version = new Version();
		version.setValue("2018.12.07");
		versionList.add(version);
		
		versionBoard.setUnreleasedVersions(versionList);
		
		assertEquals("2018.12.07", versionBoard.getUnreleasedVersions().get(0).getValue());
	}
	
	@Test
	public void testForSetGetReleasedVersions() {
		ArrayList<Version> versionList = new ArrayList<Version>();
		Version version = new Version();
		version.setValue("2018.11.07");
		versionList.add(version);
		
		versionBoard.setReleasedVersions(versionList);
		
		assertEquals("2018.11.07", versionBoard.getReleasedVersions().get(0).getValue());
	}
	
}
