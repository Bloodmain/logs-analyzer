package backend.academy.logs.provider;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class UrlLogsProviderTest {
    @Test
    public void getFewLinesFromUrl() {
        LogProvider provider = assertDoesNotThrow(() -> UrlLogProvider.create(
            "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs"
        ));
        List<FileView> files = assertDoesNotThrow(provider::getLogs);
        assertThat(files).hasSize(1);

        try (Stream<String> f = files.getFirst().getContent()) {
            assertThat(f.limit(3).toList()).isEqualTo(List.of(
                "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
                "93.180.71.3 - - [17/May/2015:08:05:23 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
                "80.91.33.133 - - [17/May/2015:08:05:24 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.17)\""
            ));
        }
    }

    @Test
    public void getAllLinesFromUrl() {
        LogProvider provider = assertDoesNotThrow(() -> UrlLogProvider.create(
            "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs"
        ));
        List<FileView> files = assertDoesNotThrow(provider::getLogs);
        assertThat(files).hasSize(1);

        try (Stream<String> f = files.getFirst().getContent()) {
            assertThat(f.count()).isEqualTo(51462);
        }
    }
}
