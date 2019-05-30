package fast.common.mailexchanger;

public class serverConfig {
	private boolean serverAuth;
	private boolean serverEnable;
	private String hostname;
	private String serverport;
	private String serverusername;
	private String serverpassword;
	private String domain;
	private String url;

	public String getHostName() {
		return hostname;
	}

	public void setHostName(String hostname) {
		this.hostname = hostname;
	}

	public boolean getServerAuth() {
		return serverAuth;
	}

	public void setServerAuth(boolean serverAuth) {
		this.serverAuth = serverAuth;
	}

	public boolean getServerEnable() {
		return serverEnable;
	}

	public void setServerEnable(boolean serverEnable) {
		this.serverEnable = serverEnable;
	}

	public String getServerUsername() {
		return serverusername;
	}

	public void setServerUsername(String serverusername) {
		this.serverusername = serverusername;
	}

	public String getServerPort() {
		return serverport;
	}

	public void setServerPort(String serverport) {
		this.serverport = serverport;
	}

	public String getServerPassword() {
		return serverpassword;
	}

	public void setServerPassword(String serverpassword) {
		this.serverpassword = serverpassword;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}

}
