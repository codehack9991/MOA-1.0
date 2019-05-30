package fast.common.agents;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import fast.common.context.SshStepResult;
import fast.common.context.StepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;
/**
 * The {@code SshAgent} class defines several actions for automating tests of the interaction with Unix server.
 * 
 * <p>The basic actions includes: send command, transfer file ...</p>
 * <p>Details information for using a SshAgent can see: 
 * <p><a href="https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/Unix+Automation+Example">Examples</a></p>
 * 
 * @author QA Framework Team
 * @since 1.5
 */
public class SshAgent extends Agent {
	public static final String CONFIG_HOST_NAME = "hostName";
	public static final String CONFIG_USER_NAME = "username";
	public static final String CONFIG_PORT = "port";
	public static final String CONFIG_PASSCODE = "password";
	public static final String CONFIG_RUN_COMMAND_TIMEOUT = "runCommandTimeout";
	public static final String WAITTIME_KEYWORD="sleep";
	public String ACCOUNT_PATTERN=".*$.*";
	public FastLogger logger;
	public String hostName;
	public String username;
	public String port;
	public String password;
	public int runCommandTimeout = 5; //5 seconds by default
	/**
     * Constructs a new <tt>SshAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
     *
     * @param   name a string for naming the creating SshAgent 
     * @param   agentParams a map to get the required parameters for creating a SshAgent 
     * @param   configurator a Configurator instance to provide configuration info for the actions of the SshAgent
     * 
     * @since 1.5
     */
	public SshAgent(String name, Map<?, ?> agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		logger = FastLogger.getLogger(String.format("%s:SshAgent", _name));
		_agentParams = agentParams;

		hostName = _agentParams.get(CONFIG_HOST_NAME).toString();
		username = _agentParams.get(CONFIG_USER_NAME).toString();
		port = _agentParams.get(CONFIG_PORT).toString();
		password = _agentParams.get(CONFIG_PASSCODE).toString();
		ACCOUNT_PATTERN = ".*@"+hostName.split("\\.")[0] + ACCOUNT_PATTERN;
		if (agentParams.containsKey(CONFIG_RUN_COMMAND_TIMEOUT)) {
			runCommandTimeout = Integer.parseInt(agentParams.get(CONFIG_RUN_COMMAND_TIMEOUT).toString());
		}

	}

	private Session connect() throws JSchException, IOException {
		JSch jSch = new JSch();
		jSch.removeAllIdentity();
		Session session = null;
		try {
			session = jSch.getSession(username, hostName,
					Integer.parseInt(port));
			session.setPassword(password);
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();

		} catch (Exception e) {
			logger.error(e.toString());
			throw e;
		}
		return session;
	}
	
	public String readOutput(InputStream commandOutput) throws IOException {
		StringBuilder responseMessageBuilder= new StringBuilder();
		byte[] tmp = new byte[1024];
		while(commandOutput.available() > 0){
			int i = commandOutput.read(tmp, 0, 1024);
			if (i < 0)
				break;
			responseMessageBuilder.append(new String(tmp, 0, i));
		}
		
		return responseMessageBuilder.toString();
	}
	
	public void waitForResponse(InputStream commandOutput) throws IOException, TimeoutException {
		int waitTimes = runCommandTimeout * 100;
		while(commandOutput.available() <= 0 && waitTimes > 0){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				Thread.currentThread().interrupt();
			}
			waitTimes--;
		}
		
		if(waitTimes == 0){
			throw new TimeoutException("Timed out when reading command output");
		}		
	}
	/**
	 * Sends commands to remote server and stores response message into CommonStepResult.
	 * 
	 * @param command unix commands with ";" as a delimiter, example:"ls -lrt;su dm18232;bf d2 ca 99 c9 8a 17 aa 97 79 10 fe a2 00 29 f6 ;cd tmp;ls" 
	 * @return a stepResult object with all response when executing command in remote server. 
	 * @throws Exception
	 * 
	 * @see fast.common.glue.CommonStepDefs#runCommand(String, String,String)
	 */
	public StepResult sendCommand(String command) throws Exception{
		SshStepResult stepResult = null;		
		StringBuilder logBuilder = new StringBuilder();
		Session session = null;
		Channel channel = null;
		OutputStream commandInput = null;
		InputStream commandOutput = null;
		try {
			logger.info("Create session...");
			session = connect();
			channel = session.openChannel("shell");
			channel.connect();
			logger.info("Session is created!");
			((ChannelShell) channel).setPty(true);
			channel.setInputStream(System.in);
			channel.setOutputStream(System.out);
			commandInput = channel.getOutputStream();
			commandOutput = channel.getInputStream();
			waitForResponse(commandOutput);
			logBuilder.append(readOutput(commandOutput)); // read response message after login and before running command
			
			stepResult = new SshStepResult();
			logger.info("Running command...");
			StringBuilder responseMessageBuilder = new StringBuilder();		
			String [] commandArray = command.split(";");
			for (int i = 0; i < commandArray.length; i++) { 
		        String currentCommand = commandArray[i];
		        String nextCommand="";
		        if(i+1 < commandArray.length)nextCommand=commandArray[i+1];
		        if(currentCommand.trim().isEmpty()){
					continue;
				}else if (currentCommand.startsWith(WAITTIME_KEYWORD)) {
					String[] sleepTime = currentCommand.split(WAITTIME_KEYWORD);
					if (sleepTime.length==2) {
						logger.info("Wait Time:=" + sleepTime[1]);
					}
	            	continue;
				}else if(currentCommand.trim().equalsIgnoreCase("exit")){
					break;
				}
				commandInput.write((currentCommand + "\n").getBytes());
				commandInput.flush();				
				if(nextCommand.startsWith(WAITTIME_KEYWORD)){
					String[] sleepTime = nextCommand.split(WAITTIME_KEYWORD);
					if (sleepTime.length==2) {
						long timeWait = (long) Integer.parseInt(sleepTime[1])*1000;
		                Thread.sleep(timeWait); 
					}
				}
				String responseMessage = "";
				do{
					//keep reading response till reaching the end
					waitForResponse(commandOutput);
					responseMessageBuilder.append(readOutput(commandOutput));	
					String[] splits= responseMessageBuilder.toString().trim().split(System.getProperty("line.separator"));
					responseMessage = splits[splits.length-1];
				} while(!responseMessage.endsWith("$") && !responseMessage.endsWith(":") && !responseMessage.matches(ACCOUNT_PATTERN));
				
		        logger.info(responseMessageBuilder.toString());
				logBuilder.append(responseMessageBuilder.toString());
				responseMessageBuilder.delete(0, responseMessageBuilder.length());
			}
		} catch (IOException | JSchException | TimeoutException e ) {
			logger.error(e.getMessage());
			throw new Exception(e);
		} finally {
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
			try {
				commandInput.close();
				commandOutput.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}

			channel = null;
			session = null;

			logger.info("Channel is disconnect!");
			logger.info("Session is disconnect!");

		}
		stepResult.setLog(logBuilder.toString());
		return stepResult;
	}

	/**
	 * Transfer file from source to destination.
	 * 
	 * @param transferType a type to transfer file: upload, download, move
	 * @param source source file location
	 * @param destination destination file location
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.CommonStepDefs#uploadFile(String, String,String)
	 * @see fast.common.glue.CommonStepDefs#downloadFile(String, String,String)
	 * @see fast.common.glue.CommonStepDefs#moveFile(String, String,String)
	 */
	public void transferFile(String transferType, String source,
			String destination) throws Exception{
		Session session = null;
		Channel channel = null;
		try {
			logger.info("Create session...");
			session = connect();
			channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			logger.info("Session is created!");
			File file = new File(source);
			switch (transferType.toLowerCase()) {
			case "upload":
				try (FileInputStream fis = new FileInputStream(file)){
					sftpChannel.cd(destination);
					sftpChannel.put(fis, file.getName());
				}
				break;
			case "download":
				sftpChannel.get(source, destination + file.getName());
				break;
			case "move":
				sftpChannel.rename(source, destination + file.getName());
				break;
			default:
				logger.info("File is not transfered!");
				return;
			}
			logger.info("File is transfered successfully!");
		} catch (IOException | JSchException | SftpException e) {
			logger.error(e.getMessage());
			throw new Exception(e);
		} finally {
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
			if (session != null && session.isConnected()) {
				session.disconnect();
			}

			channel = null;
			session = null;

			logger.info("Channel is disconnect!");
			logger.info("Session is disconnect!");

		}
	}

	@Override
	public void close() throws IOException {		
		logger.info("SSH Agent closes");
	}

}
