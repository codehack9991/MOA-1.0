package fast.common.replay;

import static org.junit.Assert.assertNotNull;

import java.util.HashSet;

import org.junit.Test;

public class TestTagIncorrectFormat_ReplayException {

	@Test
	public void testTagIncorrectFormat_ReplayException() {

		TagIncorrectFormat_ReplayException exception = new TagIncorrectFormat_ReplayException(0, "", "");
		assertNotNull(exception);
	}
	
	@Test
	public void testTagIncorrectFormat_ReplayExceptionGetReportTags() {

		TagIncorrectFormat_ReplayException exception = new TagIncorrectFormat_ReplayException(0, "", "");
		HashSet<String> reportTags = exception.getReportTags();
		assertNotNull(reportTags);
	}
	
	@Test
	public void testTagIncorrectFormat_ReplayExceptionGetMessage() {

		TagIncorrectFormat_ReplayException exception = new TagIncorrectFormat_ReplayException(0, "", "");
		String message = exception.getMessage();
		assertNotNull(message);
	}

}
