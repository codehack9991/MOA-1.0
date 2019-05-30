package fast.common.phantom.agents;

import java.util.Map;

import com.cet.citi.automation.framework.communication.unix.Shell;
import com.citi.cet.automation.framework.core.ObjectFactory;

import fast.common.agents.Agent;
import fast.common.core.Configurator;

public class SSHAgent extends Agent {

	Shell shell;

	public SSHAgent(String name, Map agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		shell = ObjectFactory.getInstance(Shell.class, agentParams);
		shell.connect();
	}

	public Shell getShell() {
		return shell;
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub

	}

}
