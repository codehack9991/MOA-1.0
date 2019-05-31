package fast.common.replay;

import static org.junit.Assert.assertNotNull;

import java.util.HashSet;

import org.junit.Test;

import quickfix.InvalidMessage;
import quickfix.Message;

public class TestMessageMissing_ReplayException {

	private String fixMsg = "8=FIX.4.29=20035=D34=249=CL_TGT1152=20180212-15:49:08.10900056=CL_SDR111=CITI000000000111=1/20180212-15:49:08.84115=GBX21=138=10040=244=19147=A48=BARC.L54=155=BARC.L59=060=20180212-15:49:08.83400010=245";

	@Test
	public void testMessageMissing_ReplayException() {

		Message expectedMessage = null;
		MessageMissing_ReplayException exception = new MessageMissing_ReplayException("tag11Value", "tag37Value",
				expectedMessage);
		assertNotNull(exception);

	}

	@Test
	public void testMessageMissing_ReplayExceptionGetMessage() throws InvalidMessage {

		Message expectedMessage = new Message(fixMsg);
		MessageMissing_ReplayException exception = new MessageMissing_ReplayException("tag11Value", "tag37Value",
				expectedMessage);
		String message = exception.getMessage();
		assertNotNull(message);

	}

	@Test
	public void testMessageMissing_ReplayExceptionGetReportTags() {

		Message expectedMessage = null;
		MessageMissing_ReplayException exception = new MessageMissing_ReplayException("tag11Value", "tag37Value",
				expectedMessage);
		HashSet<String> reportTags = exception.getReportTags();
		assertNotNull(reportTags);

	}

}
