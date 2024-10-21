package backend.academy.logs.parser;

import backend.academy.logs.exceptions.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NginxLogParserTest {
    @Test
    public void validLogs() {
        LogParser parser = new NginxLogParser();
        Stream<String> rawLogs = Stream.of(
            "54.207.57.55 - - [18/May/2015:06:05:26 +0000] \"GET /downloads/product_2 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\"",
            "62.75.198.179 - example [17/May/2015:08:05:06 +0000] \"POST /downloads/product_2 HTTP/1.1\" 200 490 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\"",
            "79.136.114.202 - - [04/Jun/2015:07:06:47 +0000] \"TRACE /downloads/product_1 HTTP/1.1\" 404 340 \"comonad\" \"Winmac\""
        );

        List<LogRecord> logs = parser.parse(rawLogs).toList();

        LogRecord log1 = new LogRecord(
            "54.207.57.55", "-",
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse("2015-05-18T06:05:26", LocalDateTime::from),
            "GET", "/downloads/product_2", 304, 0, "-", "Debian APT-HTTP/1.3 (0.9.7.9)"
        );
        LogRecord log2 = new LogRecord(
            "62.75.198.179", "example",
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse("2015-05-17T08:05:06", LocalDateTime::from),
            "POST", "/downloads/product_2", 200, 490, "-", "Debian APT-HTTP/1.3 (0.9.7.9)"
        );
        LogRecord log3 = new LogRecord(
            "79.136.114.202", "-",
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse("2015-06-04T07:06:47", LocalDateTime::from),
            "TRACE", "/downloads/product_1", 404, 340, "comonad", "Winmac"
        );
        assertThat(logs).isEqualTo(List.of(log1, log2, log3));
    }

    @Test
    public void validTimeZone() {
        LogParser parser = new NginxLogParser();
        Stream<String> rawLogs = Stream.of(
            "54.207.57.55 - - [18/May/2015:06:05:26 +0800] \"GET /downloads/product_2 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\"",
            "54.207.57.55 - - [18/May/2015:16:05:26 -1753] \"GET /downloads/product_2 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\""
        );

        List<LogRecord> logs = parser.parse(rawLogs).toList();

        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).timeLocal()).isEqualTo(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse("2015-05-17T22:05:26", LocalDateTime::from));
        assertThat(logs.get(1).timeLocal()).isEqualTo(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse("2015-05-19T09:58:26", LocalDateTime::from));
    }

    @Test
    public void invalidStatusCode() {
        LogParser parser = new NginxLogParser();
        Stream<String> rawLogs = Stream.of(
            "54.207.57.55 - - [18/May/2015:06:05:26 +0800] \"GET /downloads/product_2 HTTP/1.1\" noOp 0 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\""
        );

        Stream<LogRecord> logs = parser.parse(rawLogs);

        assertThatThrownBy(logs::toList).isInstanceOf(ParseException.class);
    }

    @Test
    public void invalidBytesSent() {
        LogParser parser = new NginxLogParser();
        Stream<String> rawLogs = Stream.of(
            "54.207.57.55 - - [18/May/2015:06:05:26 +0800] \"GET /downloads/product_2 HTTP/1.1\" 200 -3 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\""
        );

        Stream<LogRecord> logs = parser.parse(rawLogs);

        assertThatThrownBy(logs::toList).isInstanceOf(ParseException.class);
    }
}
