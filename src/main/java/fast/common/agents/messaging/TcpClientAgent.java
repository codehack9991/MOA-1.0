package fast.common.agents.messaging;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import fast.common.agents.Agent;
import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

public class TcpClientAgent extends Agent implements IMessagingAgent {

	private FastLogger logger;
	private String server;
	private int port;

	private Socket client;
	private InputStream in;
	private OutputStream out;

	private ArrayList<String> receivedMessages;

	private boolean cancelationToken;

	public TcpClientAgent(String name, Map<String, Object> agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		logger = FastLogger.getLogger(String.format("%s:TcpAgent", _name));
		this.server = Configurator.getStringOr(agentParams, "server", null);
		this.port = Configurator.getInt(agentParams, "port");
		receivedMessages = new ArrayList<>();
	}

	public TcpClientAgent(String server, int port, String name, Map<String, Object> agentParams, Configurator configurator,
			FastLogger logger) {
		super(name, agentParams, configurator);
		this.logger = logger;
		this.server = server;
		this.port = port;
		receivedMessages = new ArrayList<>();
	}

	@Override
	public void send(Object message) throws MessagingException {
		try {
			byte[] msgBytes = null;
			if (message instanceof String) {
				msgBytes = ((String) message).getBytes();
			} else {
				msgBytes = (byte[]) message;
			}

			if (!this.isConnected() || out == null) {
				throw new MessagingException("Agent does not start or connection is aborted.");
			}

			out.write(msgBytes, 0, msgBytes.length);
		} catch (Exception ex) {
			throw new MessagingException(ex);
		}
	}

	@Override
	public MessagingStepResult receive() throws MessagingException {
		if (!this.isConnected() || in == null) {
			throw new MessagingException("agent is not configured for RECEIVE-based action");
		}

		ArrayList<Object> result;
		synchronized (receivedMessages) {
			result = new ArrayList<>(receivedMessages);
			receivedMessages.clear();
		}

		return new MessagingStepResult(result);
	}

	private void listen() {
		Thread listenTh = new Thread(createRunnableListener());
		listenTh.start();
	}

	private Runnable createRunnableListener()
	{
		return new Runnable() {
			@Override
			public void run() {
				int readLen = 0;
				byte[] readBytes = new byte[1024];
				StringBuilder readText = new StringBuilder();
				cancelationToken = false;
				try {
					while (in != null && !cancelationToken) {
						while (in.available() > 0) {
							readLen = in.read(readBytes);
							readText.append(new String(readBytes, 0, readLen));
						}
						if (readText.length() > 0) {
							synchronized (receivedMessages) {
								receivedMessages.add(readText.toString());
							}
							readText = new StringBuilder();
						}
						sleep(0);
					}
				} catch (Exception ex) {
					logger.error("Agent failed to receive message.Error:" + ex.getMessage());
				}
			}

			private void sleep(int milliseconds) {
				try {
					Thread.sleep(milliseconds);
				} catch (Exception e) {
					Thread.currentThread().interrupt();
				}
			}
		};
	}

	public boolean isConnected() {
		return (client != null && client.isConnected());
	}

	@Override
	public void start() throws MessagingException {
		synchronized (this) {
			try {
				if (client != null && client.isConnected()) {
					return;
				}
				if (this.server == null || this.server.isEmpty() || this.port <= 0) {
					throw new MessagingException(
							"Agent is not configured correctly for neither server or port. Please check the config file.");
				}

				client = new Socket(server, port);
				in = client.getInputStream();
				out = client.getOutputStream();
				this.listen();

			} catch (Exception ex) {
				logger.error(String.format("connect failure: %s", ex.toString()));
				try {
					this.close();
				} catch (Exception exception) {
					logger.error(String.format(ex.getMessage()));
				}

				throw new MessagingException(ex);
			}
		}
	}

	@Override
	public void close() throws Exception {
		if (!cancelationToken) {
			cancelationToken = true;
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				Thread.currentThread().interrupt();
			}
		}

		if (in != null) {
			in.close();
			in = null;
		}
		if (out != null) {
			out.close();
			out = null;
		}
		if (client != null) {
			client.close();
			client = null;
		}
	}

	@Override
	public boolean isStarted() {
		return this.isConnected();
	}

}
