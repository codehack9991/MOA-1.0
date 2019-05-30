package fast.common.agents;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import fast.common.agents.Agent;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

/**
 * Sftp client/Agent to connect to remote server and fetch remote file content or file.
 * this agent also provides the method to check if specified file exist at remote location or not.
 * while using this agent user can set isSessionMaintained parameter, which is false by default.
 * isSessionMaintained parameter to be set true if we want to maintain one session
 * for multiple operations and set as false if we want to disconnect after each operation.
 * in case of disconnecting after the operation use need to reconnect to server using connect()
 * method.
 * @author ss80230 - Sameer Shrivastava
 *
 */

public class SftpAgent extends Agent {



	private JSch jSch = new JSch();
	private Session session;
	private Channel channel;
	private ChannelSftp sftpChannel;
	private boolean isSessionMaintained = false;
	private boolean isConnected = false;
	private FastLogger _logger;


	public SftpAgent(String name, Map agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		_logger = FastLogger.getLogger(String.format("%s:SftpAgent", _name));

	}

	/**
	 * private method, to be referred to create the sftp connection with remote server.
	 * @param user : user name for connection
	 * @param host : host url
	 * @param port : connection port
	 * @param password : password for connection
	 * @throws Exception
	 */
	private void initConnection(String user, String host, int port, String password)  throws Exception {
		try {
			session = jSch.getSession(user,host,port);
			session.setPassword(password);
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			sftpChannel = (ChannelSftp) channel;
			_logger.info(String.format("sftp connection to '%s' on port '%s' for user '%s' is successfull", host, port, user));
			isConnected=true;

		} catch (JSchException e) {
			_logger.error(e.toString());
			throw e;
		}
	}


	/**
	 * Connect to remote host and create a session and sftp channel.
	 * yml file containing the parameters for sftpAgent will be used to get the credentials
	 * for the connection.\n Following is an Example of Agent configuration and the require parameters
	 *  <pre><i> 
	 *  SftpAgent:
	 *     class_name: 'fast.common.agents.SftpAgent'
	 * 	   username: ss80230
	 * 	   password: yourPassword
	 * 	   host: examplehost.eur.nsroot.net
	 * 	   port: 22
	 * </i></pre>
	 * @throws Exception
	 */
	public void connectUsingAgentParams() throws Exception {

		String user = _agentParams.get("username").toString();
		String host = _agentParams.get("host").toString();
		int port = Integer.parseInt(_agentParams.get("port").toString());
		String password = _agentParams.get("password").toString();
		initConnection(user,host,port,password);
	}


	/**
	 * Connect to remote host and create a session and sftp channel.
	 * @param user : user name for connection
	 * @param host : host url
	 * @param port : connection port
	 * @param password : password for connection
	 * @throws JSchException
	 */
	public void connect(String user, String host, int port, String password) throws Exception {

		initConnection(user,host,port,password);
	}

	/**
	 * Get the content of the remote file specified.
	 * create the connection and channel using method connect() before using this method.
	 * @param remoteFilePath : file path including the file name relative to the remote server root dir or default dir after sftp connection
	 * @return : content of the file in string format.
	 */
	public String getFileContentFromRemote(String remoteFilePath) {

		String contentString="";

		try {

			InputStream inStream;
			inStream = sftpChannel.get(remoteFilePath);

			InputStreamReader isr = new InputStreamReader(inStream);
			BufferedReader br = new BufferedReader(isr);
			String line;
			StringBuilder content = new StringBuilder();
			while((line = br.readLine()) != null) {
				content.append(line);
			}
			_logger.info(String.format("Content received from remote file '%s is: \n '%s'", remoteFilePath, content.toString())); // convert to logger
			contentString = content.toString();
		} catch (SftpException e) {
			_logger.error(e.toString());
			contentString = "Error in reading file content";
		} catch (IOException e) {
			_logger.error(e.toString());
			contentString = "Error in reading file content";
		} finally {
			if(!isSessionMaintained) disconnect();
		}

		return contentString;

	}

	/**
	 * Get the specified file from remote server.
	 * create the connection and channel using method connect() before using this method.
	 * @param remoteFilePath : file path including the file name relative to the remote server root dir or default dir after sftp connection
	 * @param localFilePath : file path including the file name on the local machine. This should eb the absolute path on the local machine.
	 * @return : boolean, true if file transfer is successful, false 
	 */
	public boolean getFileFromRemote( String remoteFilePath, String localFilePath) {

		try {

			if(!checkFileExist(remoteFilePath)) {
				_logger.error("Content fetch the remote file.");
				return false;
			}

			sftpChannel.get(remoteFilePath, localFilePath);

			File localFile = new File(localFilePath);
			if(!localFile.exists()) { 
				_logger.error("File do not exist at local machine");
				return false;
			}

			_logger.info(String.format("File Successfully fetched from %s to %s",remoteFilePath,localFilePath));
			return true;		

		} catch (SftpException e) {
			_logger.error(e.toString());
			return false;
		} finally {
			if(!isSessionMaintained) disconnect();
		}
	}

	/**
	 * This method check if file at remote path exist or not
	 * @param remoteFilePath including the file name
	 * @return true if file exist, false if file do not exist.
	 */
	public boolean checkFileExist(String remoteFilePath) {

		boolean isFileExist=false;
		try {

			SftpATTRS fileAttributes = sftpChannel.lstat(remoteFilePath);
			_logger.info(String.format("file %s !!EXIST!!", remoteFilePath)); // convert to logger
			_logger.info("File Attributes : \n"+fileAttributes.toString());
			isFileExist = true;
		} catch (SftpException e) {
			_logger.error(e.toString());
		} finally {
			if(!isSessionMaintained) disconnect();
		}

		return isFileExist;
	}

	/**
	 * This method is to get the content of the directory at a remote location
	 * @param remote directory path
	 * @return Vector list of the directory content of item type LsEntry (jsch).
	 */
	@SuppressWarnings("unchecked")
	public Vector<LsEntry> getDirectoryContent(String remoteDirectoryPath) {

		StringBuilder stringBuilder = new StringBuilder();
		Vector<LsEntry> filelist=null;
		try {

			filelist = sftpChannel.ls(remoteDirectoryPath);
			if(filelist.isEmpty()) {
				return filelist;
			}
			for(int i=0; i<filelist.size();i++){
				LsEntry entry = (LsEntry) filelist.get(i);
				stringBuilder.append(entry.getLongname()+"\n");                
			}
			_logger.info(String.format("Directory Content:%n%s", stringBuilder.toString()));
		} catch (SftpException e) {
			_logger.error(e.toString());
		} finally {
			if(!isSessionMaintained) disconnect();
		}

		return filelist;
	}

	/**
	 * Return boolean value of the isSessionMaintained parameter.
	 * @return true/false (boolean)
	 */
	public boolean isSessionMaintained() {
		return isSessionMaintained;
	}
	
	/**
	 * boolean flag to indicate connection status
	 * @return true is agent connected to remote else false.
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 *  Method to set the value of the isSessionMaintained parameter.
	 * isSessionMaintained parameter to be set true if we want to maintain one session
	 * for multiple operations and set as false if we want to disconnect after each operation.
	 * in case of disconnecting after the operation use need to reconnect to server using connect()
	 * method.
	 * @param isSessionMaintained (boolean)
	 */
	public void setSessionMaintained(boolean isSessionMaintained) {
		this.isSessionMaintained = isSessionMaintained;
	}



	/**
	 * Disconnects the connection and closes channels.
	 */
	public void disconnect() {

		if(sftpChannel != null ) {
			sftpChannel.exit();
		}
		if(channel != null && channel.isConnected()) {
			channel.disconnect();
		}
		if(session != null && session.isConnected()) {
			session.disconnect();
		}

		sftpChannel = null;
		channel = null;
		session = null;
		isConnected=false;
		_logger.info("Session Disconnected");

	}


	@Override
	public void close() throws Exception {
		disconnect();
	}

}
