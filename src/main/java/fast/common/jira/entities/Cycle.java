package fast.common.jira.entities;

import java.util.Date;

public class Cycle {
	private String id;
	private int totalExecutions;
	private Date endDate;
	private String description;
	private int totalExecuted;
	private String started;
	private String expand;
	private String projectKey;
	private int versionId;
	private String environment;
	private String build;
	private String ended;
	private String name;
	private String modifiedBy;
	private int projectId;
	private String startDate;
	private ExecutionSummaries executionSummaries;
	
	public String getId(){
		return id;
	}
	public void setId(String value){
		this.id=value;
	}

	public int getTotalExecutions() {
		return totalExecutions;
	}

	public void setTotalExecutions(int totalExecutions) {
		this.totalExecutions = totalExecutions;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getTotalExecuted() {
		return totalExecuted;
	}

	public void setTotalExecuted(int totalExecuted) {
		this.totalExecuted = totalExecuted;
	}

	public String getStarted() {
		return started;
	}

	public void setStarted(String started) {
		this.started = started;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public String getExpand() {
		return expand;
	}

	public void setExpand(String expand) {
		this.expand = expand;
	}

	public int getVersionId() {
		return versionId;
	}

	public void setVersionId(int versionId) {
		this.versionId = versionId;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getBuild() {
		return build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	public String getEnded() {
		return ended;
	}

	public void setEnded(String ended) {
		this.ended = ended;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public ExecutionSummaries getExecutionSummaries() {
		return executionSummaries;
	}

	public void setExecutionSummaries(ExecutionSummaries executionSummaries) {
		this.executionSummaries = executionSummaries;
	}

}
