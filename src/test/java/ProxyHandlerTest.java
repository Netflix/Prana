import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyHandlerTest {

    @Test
    public void shouldMatchURIStartingWithProxy() {
        Pattern pattern = Pattern.compile("^/proxy.*");
        Matcher matcher = pattern.matcher("/proxy/VIPNAME/path");
        Assert.assertEquals(true, matcher.matches());
    }
}
