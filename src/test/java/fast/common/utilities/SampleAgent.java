package fast.common.utilities;


public class SampleAgent {
	public enum AgentCategory{
		GUI,
		NonGUI
	}
	
	private String name;
	private int timeout;
	private double ratio;
	private AgentCategory category;

	public SampleAgent(String name, int timeout, double ratio, AgentCategory category) {
		this.name = name;
		this.timeout = timeout;
		this.ratio = ratio;
		this.category = category;
	}
	
	public String getName(){
		return name;
	}
	
	public int getTimeout(){
		return timeout;
	}
	
	public double getRatio(){
		return ratio;
	}
	
	public AgentCategory getCategory(){
		return category;
	}
}
