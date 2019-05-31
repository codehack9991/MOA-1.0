package fast.common.replay;

import static org.junit.Assert.assertNotNull;

import java.util.HashSet;

import org.junit.Test;

public class TestTagMissing_ReplayException {

	@Test
	public void testTagMissing_ReplayException() {
		TagMissing_ReplayException exception = new TagMissing_ReplayException(0, "");
		assertNotNull(exception);
	}

	@Test
	public void testTagMissing_ReplayExceptionGetReportTags() {
		TagMissing_ReplayException exception = new TagMissing_ReplayException(0, "");
		HashSet<String> reportTags = exception.getReportTags();
		assertNotNull(reportTags);
	}

	
	@Test
	public void testTagMissing_ReplayExceptionGetMessage() {
		TagMissing_ReplayException exception = new TagMissing_ReplayException(0, "");
		String message = exception.getMessage();
		assertNotNull(message);
	}

}
