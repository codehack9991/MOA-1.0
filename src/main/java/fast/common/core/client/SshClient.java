package fast.common.core.client;

import fast.common.logging.FastLogger;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import net.sf.expectit.ExpectBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

// TODO: refactor to be Agent - not a Client
/**
 * Created by ao94803 on 4/20/2017.
 */
public class SshClient extends Client {
    JSch jSch;
    Session session;
    Channel channel;

    static FastLogger logger = FastLogger.getLogger("SshClient");

    public SshClient(Map params) {
        super(params);
    }

    public void connect() throws Exception {
        try {
            jSch = new JSch();
            session = jSch.getSession(
                    params.get("user").toString(),
                    params.get("host").toString(),
                    Integer.parseInt(params.get("port").toString()));
            session.setPassword(params.get("password").toString());
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channel = session.openChannel("shell");
            channel.connect();

            expect = new ExpectBuilder()
                    .withOutput(channel.getOutputStream())
                    .withInputs(channel.getInputStream(), channel.getExtInputStream())
                    .withTimeout(10, TimeUnit.SECONDS)
                    .withEchoOutput(System.out)
                    .withEchoInput(System.err)
                    //.withInputFilters(removeColors(), removeNonPrintable())
                    //.withExceptionOnFailure()
                    .build();
            getResponse(); //consume server greeting
            getResponse(); //consume server greeting

        }
        catch (Exception e) {
            disconnect();
            logger.error(e.toString());
            throw e;
        }

    }

    public void disconnect() throws IOException {
        if(expect != null ) {
                expect.close();
        }
        if(channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if(session != null && session.isConnected()) {
            session.disconnect();
        }
        expect = null;
        channel = null;
        session = null;
    }

    public void send(String command) throws IOException {
        expect.sendLine(command);
    }

    @Override
    public void fillOutput(List<String> filters) throws IOException {
        String prepareOutputCommand = params.get("prepareOutputCommand").toString();
        if(filters != null) {
            for (String filter : filters) {
                prepareOutputCommand += " | grep -a " + filter;
            }
        }
        expect.sendLine(prepareOutputCommand);
    }
}
