package fast.common.testng;

import java.util.ArrayList;
import java.util.List;

public class FastTestngStep {
	
	private String stepName;
	private long startTime;
	private List<String> messages = new ArrayList<>();
	
	public String getStepName() {
		return stepName;
	}
	public void setStepName(String stepName) {
		this.stepName = stepName;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public List<String> getMessages() {
		return messages;
	}
	public void setMessages(List<String> messages) {
		if(messages != null){
			this.messages = messages;
		}		
	}
	public FastTestngStep() {
		super();
	}
	public FastTestngStep(String stepName, long startTime, List<String> messages) {
		super();
		this.stepName = stepName;
		this.startTime = startTime;
		if(messages != null){
			this.messages = messages;
		}	
	}
	@Override
	public String toString() {
		return "FastTestngStep [stepName=" + stepName + ", startTime=" + startTime + ", messages=" + messages.toString() + "]";
	}
	
	public void addMessage(String message){
		this.messages.add(message);
	}
}
