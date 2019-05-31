package fast.common.replay;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Test;

import quickfix.InvalidMessage;

public class TestMessageIncorrect_ReplayException {
	private String fixMsg = "8=FIX.4.29=20035=D34=249=CL_TGT1152=20180212-15:49:08.10900056=CL_SDR111=CITI000000000111=1/20180212-15:49:08.84115=GBX21=138=10040=244=19147=A48=BARC.L54=155=BARC.L59=060=20180212-15:49:08.83400010=245";
	private String fixMsg2 = "8=FIX.4.29=20035=D34=237=249=CL_TGT1152=20180212-15:49:08.10900056=CL_SDR111=CITI000000000111=1/20180212-15:49:08.84115=GBX21=138=10040=244=19147=A48=BARC.L54=155=BARC.L59=060=20180212-15:49:08.83400010=207";
	@Test
	public void testMessageIncorrect_ReplayException() throws InvalidMessage {
		quickfix.Message actualMessage = new quickfix.Message(fixMsg);
		ArrayList<TagError_ReplayException> tagErrors = new ArrayList<>();		
		MessageIncorrect_ReplayException exception = new MessageIncorrect_ReplayException(actualMessage, tagErrors);
		assertNotNull(exception);
	}

	@Test
	public void testMessageIncorrect_ReplayExceptionGetMessage() throws InvalidMessage {
		quickfix.Message actualMessage = new quickfix.Message(fixMsg);
		ArrayList<TagError_ReplayException> tagErrors = new ArrayList<>();		
		MessageIncorrect_ReplayException exception = new MessageIncorrect_ReplayException(actualMessage, tagErrors);
		String message = exception.getMessage();
		assertNotNull(message);
		ArrayList<TagError_ReplayException> actualTagErrors = exception.getTagErrors();
		assertNotNull(actualTagErrors);
		assertNotNull(exception);
	}
	
	@Test
	public void testMessageIncorrect_ReplayExceptionGetReportTags() throws InvalidMessage {
		quickfix.Message actualMessage = new quickfix.Message(fixMsg2);
		ArrayList<TagError_ReplayException> tagErrors = new ArrayList<>();
		tagErrors.add(new TagError_ReplayException(34) {
			@Override
			protected HashSet<String> getReportTags() {
				HashSet<String> hashSet = new HashSet<String>();
				hashSet.add("");
				return hashSet;
			}
		});

		MessageIncorrect_ReplayException exception = new MessageIncorrect_ReplayException(actualMessage, tagErrors);
		HashSet<String> reportTags = exception.getReportTags();
		assertNotNull(reportTags);
		String message = exception.getMessage();
		assertNotNull(message);
	}

}
