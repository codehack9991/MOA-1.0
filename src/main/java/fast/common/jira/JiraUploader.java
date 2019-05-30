package fast.common.jira;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.xmlbeans.impl.regex.RegularExpression;

import fast.common.context.EvalScope;
import fast.common.core.Configurator;
import fast.common.jira.entities.Cycle;
import fast.common.jira.entities.PVItem;
import fast.common.logging.FastLogger;

public class JiraUploader {

	private static FastLogger logger = FastLogger.getLogger(JiraUploader.class.getName());

	private static final String CONGIF_NAME = "JiraUploader";
	private static final String DEFAULT_JIRA_URL = "https://cedt-icg-jira.nam.nsroot.net";

	private static final String PARAMETER_ENABLED_NAME = "Enabled";
	private static final String PARAMETER_URL_NAME = "Url";
	private static final String PARAMETER_USERNAME_NAME = "User";
	private static final String PARAMETER_PASS_WORD_NAME = "Password";
	private static final String PARAMETER_PROJECT_NAME = "Project";
	private static final String PARAMETER_VERSION_NAME = "Version";
	private static final String PARAMETER_CYCLE_NAME = "Cycle";
	private static final String PARAMETER_ALLOWTOCREATECYCLE_NAME = "AllowToCreateCycle";

	private static JiraUploader instance;
	private static Lock lock = new ReentrantLock();

	private EvalScope evalScope=new EvalScope();
	
	public static JiraUploader getInstance() {
		lock.lock();
		if (instance == null) {
			instance = new JiraUploader();
			instance.init();
		}
		lock.unlock();
		return instance;
	}

	private ZapiRestService service;
	private String url;

	private String prjId;
	private String verId;
	private String cycId;

	private boolean enabled;
	private boolean allowToCreateCycle;
	private String prjName;
	private String verName;
	private String cycName;
	private String user;
	private String password;

	public String getUrl() {
		return url;
	}

	public void setUrl(String value) {
		this.url = value;
	}

	public boolean getEnabled() {
		return this.enabled;
	}

	public String getUser() {
		return this.user;
	}

	public void init() {
		this.loadConfig();
		if (!enabled) {
			return;
		}
		this.init(user, password, prjName, verName, cycName);
	}

	public void init(String userName, String password, String prjName, String verName, String cycName) {
		enabled = true;

		if (url == null || "".equals(url)) {
			url = DEFAULT_JIRA_URL;
		}
		try {
			service=ZapiRestService.generateService(url, userName, password);

			this.getProjectId(prjName);
			this.getVersionId(verName);
			this.getCycleId(cycName);
		} catch (Exception exception) {
			logger.info("JiraUploader init failed. Error" + exception.toString());

			enabled = false;
		}
	}

	public void init(String url, String userName, String password, String prjName, String verName, String cycName) {
		this.url = url;
		this.init(userName, password, prjName, verName, cycName);
	}

	public void uploadNewExecutionInfoAsync(String issueKey, int status, String assingee, List<String> reports) {
		Thread thread = new Thread(new UploaderExecutor(this, issueKey, status, assingee, reports));
		thread.start();
	}

	public void uploadNewExecutionInfo(String issueKey, int status, String assingee, List<String> reports)
			throws RequestNotSucceedException {
		if (!enabled) {
			return;
		}
		logger.info("JiraUploader enabled. Upload execution info for " + issueKey + ". Processing......");

		if (prjId == null || "".equals(prjId) || verId == null || "".equals(verId) || cycId == null
				|| "".equals(cycId)) {
			logger.info("Jira uploader did not initialize successfully.");
			enabled = false;
			return;
		}

		if (issueKey == null || "".equals(issueKey)) {
			logger.info("Invalid issueKey:" + issueKey);

			return;
		}

		String issueId = this.getIssueId(issueKey);
		if (issueId == null || "".equals(issueId)) {
			logger.info("Failed to get issue id related to issue key: " + issueKey);
			return;
		}
		String executionId = null;
		try {
			executionId = service.createNewExecution(prjId, verId, cycId, issueId, assingee);
		} catch (Exception ex) {
			logger.info("Failed to create new execution. Error:" + ex.getMessage());

			return;
		}

		try {
			service.updateExecutionInfo(executionId, Integer.toString(status));
		} catch (Exception ex) {
			logger.error("Failed to update info for execution belonged to " + issueKey + " which id is " + executionId
					+ ". Error:" + ex.getMessage());

			return;
		}

		if (reports == null || reports.isEmpty()) {
			return;
		}
		try {
			service.uploadAttachementToExecution(executionId, reports);
		} catch (Exception ex) {
			logger.error("Failed to attach files to execution belonged to " + issueKey + " which id is " + executionId
					+ ". Error:" + ex.getMessage());

			return;
		}
		logger.info("JiraUploader executed completed.");
	}

	private String getIssueId(String issueKey) throws RequestNotSucceedException {
		String issueId = null;

		issueId = service.getIssueIdByIssueKey(issueKey);

		return issueId;
	}

	private void getProjectId(String prjName) throws JiraTransactionException, RequestNotSucceedException {
		String prjid = null;
		if (prjName == null || "".equals(prjName)) {
			throw new JiraTransactionException("Project name is null or empty.");
		}

		List<PVItem> projects = service.getAllProjects();
		for (int i = 0; i < projects.size(); i++) {
			if (prjName.equals(projects.get(i).getLabel())) {
				prjid = projects.get(i).getValue();
				break;
			}
		}
		if (prjid == null || "".equals(prjid)) {
			throw new JiraTransactionException("Failed to fetch project id. There is no project related to " + prjName);
		}
		this.prjId = prjid;
	}

	private void getVersionId(String verName) throws JiraTransactionException, RequestNotSucceedException {
		String verid = null;
		if (verName == null || "".equals(verName)) {
			throw new JiraTransactionException("Version name is null or empty.");
		}

		List<PVItem> versions = service.getVersionsByPrjId(prjId);
		for (int i = 0; i < versions.size(); i++) {
			if (verName.equals(versions.get(i).getLabel())) {
				verid = versions.get(i).getValue();
				break;
			}
		}
		if (verid == null || "".equals(verid)) {
			throw new JiraTransactionException("Failed to fetch version id. There is no version related to " + verName);
		}
		this.verId = verid;
	}

	private void getCycleId(String cycName) throws RequestNotSucceedException, JiraTransactionException {
		String cycid = null;
		if (cycName == null || "".equals(cycName)) {
			throw new JiraTransactionException("Version name is null or empty.");
		}

		List<Cycle> cycles = null;

		if (this.verId.equals("-1")) {
			cycles = service.getCyclesByPrjIdAndVerId(prjId, verId);
		} else {
			cycles = service.getCyclesByVerId(verId);
		}

		for (int i = 0; i < cycles.size(); i++) {
			if (cycName.equals(cycles.get(i).getName())) {
				cycid = cycles.get(i).getId();
				break;
			}
		}
		boolean isCycleIdEmpty = (cycid == null || "".equals(cycid));
		if (isCycleIdEmpty && !allowToCreateCycle) {
			throw new JiraTransactionException("Failed to fetch cycle id. There is no cycle related to " + cycName);
		}
		if (isCycleIdEmpty && allowToCreateCycle) {
			cycid = createNewCycle();
		}
		this.cycId = cycid;
	}

	private String createNewCycle() throws RequestNotSucceedException, JiraTransactionException {
		return this.service.createNewCycle(prjId, verId, cycName);
	}

	@SuppressWarnings("unchecked")
	private void loadConfig() {
		Map<String, String> jConfig = null;
		try {
			jConfig = Configurator.getMap(Configurator.getInstance().getSettingsMap(), CONGIF_NAME);
		} catch (Exception exception) {
			enabled = false;
			logger.info("The config for JIRA uploader is not given");
			return;
		}

		enabled = Configurator.getBooleanOr(jConfig, PARAMETER_ENABLED_NAME, false);
		if (!enabled) {
			logger.info("The JiraUploader is disabled.");
			return;
		}

		url = Configurator.getStringOr(jConfig, PARAMETER_URL_NAME, null);
		if (url == null || "".equals(url)) {
			url = DEFAULT_JIRA_URL;
		}

		user = Configurator.getStringOr(jConfig, PARAMETER_USERNAME_NAME, null);
		if (user == null || "".equals(user)) {
			enabled = false;
			logger.info("No valid user for JiraUploader");
			return;
		}

		password = Configurator.getStringOr(jConfig, PARAMETER_PASS_WORD_NAME, null);
		if (password == null || "".equals(password)) {
			enabled = false;
			logger.info("No valid password for JiraUploader");
			return;
		}

		this.prjName = Configurator.getStringOr(jConfig, PARAMETER_PROJECT_NAME, null);
		if (this.prjName == null || "".equals(this.prjName)) {
			enabled = false;
			logger.info("No valid project for JiraUploader");
			return;
		}

		this.verName = Configurator.getStringOr(jConfig, PARAMETER_VERSION_NAME, null);
		if (this.verName == null || "".equals(this.verName)) {
			enabled = false;
			logger.info("No valid version for JiraUploader");
			return;
		}

		this.cycName = Configurator.getStringOr(jConfig, PARAMETER_CYCLE_NAME, null);
		if (this.cycName == null || "".equals(this.cycName)) {
			enabled = false;
			logger.info("No valid cycle for JiraUploader");
			return;
		}
		this.cycName=evalScope.processString(this.cycName);
		allowToCreateCycle = Configurator.getBooleanOr(jConfig, PARAMETER_ALLOWTOCREATECYCLE_NAME, false);
	}

	public static String getIssueKeyFromScenarioName(String scenarioName) {

		if (scenarioName == null || "".equals(scenarioName)) {
			return null;
		}

		String[] spliteArray = scenarioName.trim().split(" ");
		if (spliteArray.length < 2) {
			return null;
		}
		String result = spliteArray[0].trim();

		RegularExpression regularExpression = new RegularExpression("^C\\d+.*-\\d+$");

		if (regularExpression.matches(result)) {
			return result;
		}
		return null;
	}

	public enum JiraExecutionStatus {
		UNEXECUTED(-1), BLOCKED(4), WIP(3), FAIL(2), PASS(1);

		private int value;

		public int getValue() {
			return value;
		}

		JiraExecutionStatus(int status) {
			this.value = status;
		}

		public static JiraExecutionStatus convertToJiraExecutionStatus(String status) {
			switch (status.toUpperCase()) {
			case "PASSED":
				return JiraExecutionStatus.PASS;
			case "FAILED":
				return JiraExecutionStatus.FAIL;
			case "SKIPPED":
				return JiraExecutionStatus.UNEXECUTED;
			default:
				return JiraExecutionStatus.UNEXECUTED;
			}
		}
	}

	class UploaderExecutor implements Runnable {
		private JiraUploader uploader;
		private String issueKey;
		private int status;
		private String assingee;
		private List<String> reports;

		public UploaderExecutor(JiraUploader uploader, String issueKey, int status, String assingee,
				List<String> reports) {
			this.issueKey = issueKey;
			this.status = status;
			this.assingee = assingee;
			this.reports = reports;
			this.uploader = uploader;
		}

		@Override
		public void run() {

			try {
				this.uploader.uploadNewExecutionInfo(issueKey, status, assingee, reports);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}

	}

}
