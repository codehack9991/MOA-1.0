package fast.common.core.client;

import net.sf.expectit.ExpectBuilder;
import org.apache.commons.lang3.NotImplementedException;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// TODO: refactor to be Agent - not a Client
/**
 */
public class FileClient extends Client {
    InputStream stream;

    public FileClient(Map params) {
        super(params);
    }

    @Override
    public void connect() throws Exception {
        stream = new FileInputStream(new File(params.get("uri").toString()));

        expect = new ExpectBuilder()
                .withInputs(stream)
                .withTimeout(30, TimeUnit.SECONDS)
                .withBufferSize(1024*1024)
                .build();
    }

    @Override
    public void disconnect() throws IOException {
        if(expect != null ) {
            expect.close();
        }
        if(stream != null) {
            stream.close();
        }
        expect = null;
        stream = null;
    }

    @Override
    public void send(String command) throws IOException {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public void fillOutput(List<String> filters) {
        //ignore
    }

}
