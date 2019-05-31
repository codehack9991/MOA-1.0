package fast.common.replay;

import static org.junit.Assert.assertNotNull;

import java.util.HashSet;

import org.junit.Test;

public class TestTagIncorrectValue_ReplayException {

	@Test
	public void testTagIncorrectValue_ReplayException() {
		TagIncorrectValue_ReplayException exception = new TagIncorrectValue_ReplayException(0, "", "");
		assertNotNull(exception);
	}

	
	@Test
	public void testTagIncorrectValue_ReplayExceptionGetReportTags() {
		TagIncorrectValue_ReplayException exception = new TagIncorrectValue_ReplayException(0, "", "");
		HashSet<String> reportTags = exception.getReportTags();
		assertNotNull(reportTags);
	}

	
	@Test
	public void testTagIncorrectValue_ReplayExceptionGetMessage() {
		TagIncorrectValue_ReplayException exception = new TagIncorrectValue_ReplayException(0, "", "");
		String message = exception.getMessage();
		assertNotNull(message);
	}

}
