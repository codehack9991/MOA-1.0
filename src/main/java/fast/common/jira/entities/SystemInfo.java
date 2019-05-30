package fast.common.jira.entities;

public class SystemInfo {
	private String jira_db_build;
	private String jira_app_server;
	private String jira_db_type;
	private String licenseDescription;
	private String jira_version;
	private String customerId;
	private String zfj_build;
	private String SEN;
	private String zfj_version;
	
	public String getJiraDbBuild(){
		return jira_db_build;
	}
	
	public void setJiraDbBuild(String jiraDbBuild){
		jira_db_build = jiraDbBuild;
	}
	
	public String getJiraAppServer(){
		return jira_app_server;
	}
	
	public void setJiraAppServer(String jiraAppServer){
		jira_app_server = jiraAppServer;
	}
	
	public String getJiraDbType(){
		return jira_db_type;
	}
	
	public void setJiraDbType(String jiraDbType){
		jira_db_type = jiraDbType;
	}
	
	
}
