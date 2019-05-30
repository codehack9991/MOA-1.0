package fast.common.jira.entities;

public class Version {
	private String value;
	private boolean archived;
	private String label;
	
	public String getValue(){
		return value;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public boolean getArchived(){
		return archived;
	}
	
	public void setArchived(boolean archived){
		this.archived = archived;
	}
	
	public String getLabel(){
		return label;
	}
	
	public void setLabel(String label){
		this.label = label;
	}
}
