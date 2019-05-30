package fast.common.jira.entities;

public class PVItem {

	private String value;
	private String label;
	private String hasAccessToSoftware;
	private String type;
	
	public String getValue(){
		return this.value;
	}
	
	public String getLabel(){
		return this.label;
	}
	
	public String getHasAccessToSoftware(){
		return this.hasAccessToSoftware;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setHasAccessToSoftware(String hasAccessToSoftware) {
		this.hasAccessToSoftware = hasAccessToSoftware;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
