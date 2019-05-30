package fast.common.agents;

import fast.common.context.ScenarioContext;
import fast.common.logging.FastLogger;
import fast.common.core.Configurator;
import fast.common.fix.FixHelper;
import fast.common.context.FixStepResult;
import fast.common.core.client.Client;
import fast.common.core.client.FileClient;
import fast.common.core.client.SshClient;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import quickfix.InvalidMessage;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

/**
 * Created by ao94803 on 4/20/2017.
 */
public class GmaLogReader extends Agent {
    private Client client;

    String _receiveExtraTags = null;

    static FastLogger logger;
    FixHelper fixHelper;

    public GmaLogReader(String name, Map agentParams, Configurator configurator) throws Exception {
        super(name, agentParams, configurator);
        logger = FastLogger.getLogger(String.format("%s:GmaLogReader", _name));

        fixHelper = new FixHelper(agentParams, configurator);

        start();
    }

    private void start() throws Exception    {
        String clientType = _agentParams.get("clientType").toString();
        if(clientType.equalsIgnoreCase("ssh"))
            client = new SshClient(_agentParams);
        else if(clientType.equalsIgnoreCase("file"))
            client = new FileClient(_agentParams);
        else
            throw new NotImplementedException(clientType + " client is not supported");
        client.connect();
    }

    public FixStepResult receiveMessage(ScenarioContext scenarioContext, String msgName, String userstr) throws IOException, XPathExpressionException, InvalidMessage {
        logger.debug("msgtype=" + msgName + ", userstr=" + userstr);
        quickfix.Message msg = fixHelper.convertUserstrToMessage(scenarioContext, msgName, userstr, _receiveExtraTags);
        String[] searchFilters = StringUtils.split(msg.toString(), '\u0001');
        logger.debug(String.format("%s: checking received userstr=[%s]",
                _agentParams.get("uri"), msg.toString()));
        client.fillOutput(Arrays.asList(searchFilters));
        List<String> foundMessages = parseLogFile();
        if (foundMessages.size() > 0) {
            return new FixStepResult(msg, fixHelper);
        } else
            throw new IOException("Failed to find message in " + _agentParams.get("uri"));
        //return new FixStepResult("", fixHelper);
    }

    private List<String> parseLogFile() throws IOException {
        List<String> messages = new ArrayList<>();
        String response;
        response = client.getWholeResponse();
        logger.debug("Got file content of length " + response.length());
        String[] logContent = response.split("\r\n\r\n");
        for (String line : logContent) {
            String message = Arrays.asList(line.split("\u001F")).stream().filter(x -> x.startsWith("8=FIX")).findFirst().orElse(null);
            if (message != null)
                messages.add(message);
        }

        return messages;
    }

    @Override
    public void close() throws Exception {
        client.disconnect();
    }
}
