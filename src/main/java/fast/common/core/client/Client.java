package fast.common.core.client;

import net.sf.expectit.Expect;
import net.sf.expectit.matcher.Matchers;

import java.io.IOException;
import java.util.List;
import java.util.Map;



// TODO: refactor to be Agent - not a Client
/**
 */
public abstract class Client {
    Expect expect;
    Map params;
    public Client(Map params) {
        this.params = params;
    }
    public abstract void connect() throws Exception;
    public abstract void disconnect() throws IOException;
    public abstract void send(String command) throws IOException;
    public abstract void fillOutput(List<String> filters) throws IOException;

    public boolean expect(String response) throws IOException {
        return expect.expect(Matchers.contains(response)).isSuccessful();
    }

    public boolean expectNonEmpty() throws IOException {
        return expect.expect(Matchers.anyString()).isSuccessful();
    }

    public String getResponse() throws IOException {
        return expect.expect(Matchers.anyString()).getInput();
    }

    public String getResponse(String endMarker) throws IOException {
        return expect.expect(Matchers.contains(endMarker)).getInput();
    }

    public String getWholeResponse() throws IOException {
        return expect.expect(Matchers.eof()).getInput();
    }


}
