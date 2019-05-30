package fast.common.mailexchanger;

public class mailFactory {

	public static mailer getServerType(String serverType) throws RuntimeException {

		if (serverType.equalsIgnoreCase("Exchange")) {
			return new Exchange();
		}

		if (serverType.equalsIgnoreCase("SMTP")) {
			return new SMTP();
		}

		throw new RuntimeException("Server Type not identified !!");

	}
}
