package fast.common.fix;

import java.util.Map;

import com.citigroup.get.quantum.intf.Message;
import com.citigroup.get.quantum.intf.MessageFactory;
import com.citigroup.get.quantum.intf.ParseException;

import fast.common.core.Configurator;
import fast.common.logging.FastLogger;
import quickfix.ConfigError;

public class RIOFixHelper extends FixHelper{

	static FastLogger logger = FastLogger.getLogger("RIOFixHelper");
	public static final int ZEXECID = 10018;
	public static final String RIOMSGTYPE = "35=RIO";
	public static final String DEFAULT_DEL = "\\|";
	public static final String DEFAULT_FIX_DEL = "\001";


	public RIOFixHelper(Map agentParams, Configurator configurator) throws ConfigError {
		super(agentParams, configurator);
	}

	public static String convertFixStringToRioMessage(String processFixString) {
		Message message = null;
		try {
			if (processFixString.contains(RIOMSGTYPE)) {
				MessageFactory rioValidator = com.citi.get.rio.intf.MessageFactoryImpl.getInstance();
				message = rioValidator.getMessage(processFixString.replaceAll(DEFAULT_DEL, DEFAULT_FIX_DEL));
				String tag = message.getTag(ZEXECID);
				message.setTag(ZEXECID, tag);
			} else {
				logger.warn("This is not rio fix!");
				return processFixString;
			}
		} catch (ParseException e) {
			String errorStr = String.format("Error conver fix string to fix message '%s'", e.toString());
			logger.error(errorStr.toString());
			throw e;
		}
		return message.toFixString();

	}
}
