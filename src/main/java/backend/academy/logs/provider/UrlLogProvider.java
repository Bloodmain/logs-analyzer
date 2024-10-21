package backend.academy.logs.provider;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import org.apache.commons.io.function.Uncheck;

public class UrlLogProvider implements LogProvider {
    private final URL url;

    private UrlLogProvider(URL url) {
        this.url = url;
    }

    public static UrlLogProvider create(String url) throws MalformedURLException, URISyntaxException {
        return new UrlLogProvider(new URI(url).toURL());
    }

    @Override
    public List<FileView> getLogs() {
        return List.of(new FileView(url.toString(), () -> Uncheck.get(url::openStream)));
    }
}
