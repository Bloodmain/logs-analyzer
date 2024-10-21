package backend.academy.logs.filter;

import backend.academy.logs.parser.LogRecord;
import java.util.List;
import java.util.stream.Stream;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

public class LogFilterFieldTest {
    @Test
    public void filterPostMethods() {
        LogRecord log1 = Instancio.of(LogRecord.class).set(field("method"), "GET").create();
        LogRecord log2 = Instancio.of(LogRecord.class).set(field("method"), "POST").create();
        LogRecord log3 = Instancio.of(LogRecord.class).set(field("method"), "GET").create();

        LogFilter filter = LogFilterField.create("method", "POST");
        List<LogRecord> filtered = filter.filter(Stream.of(log1, log2, log3)).toList();

        assertThat(filtered).containsExactly(log2);
    }

    @Test
    public void filterResourcesPattern() {
        LogRecord log1 = Instancio.of(LogRecord.class).set(field("resource"), "rate").create();
        LogRecord log2 = Instancio.of(LogRecord.class).set(field("resource"), "rate/limit/100").create();
        LogRecord log3 = Instancio.of(LogRecord.class).set(field("resource"), "").create();
        LogRecord log4 = Instancio.of(LogRecord.class).set(field("resource"), "Killer2004Find").create();
        LogRecord log5 = Instancio.of(LogRecord.class).set(field("resource"), "2Them").create();

        LogFilter filter = LogFilterField.create("resource", ".+\\d");
        List<LogRecord> filtered = filter.filter(Stream.of(log1, log2, log3, log4, log5)).toList();

        assertThat(filtered).containsExactly(log2);
    }

    @Test
    public void twoFieldFiltering() {
        LogRecord log1 = Instancio.of(LogRecord.class).set(field("httpUserAgent"), "MozillaDry")
            .set(field("bytesSent"), 32)
            .create();
        LogRecord log2 = Instancio.of(LogRecord.class).set(field("httpUserAgent"), "MoziDry").create();
        LogRecord log3 = Instancio.of(LogRecord.class).set(field("httpUserAgent"), "Mozilla")
            .set(field("bytesSent"), 3438762)
            .create();
        LogRecord log4 = Instancio.of(LogRecord.class).set(field("httpUserAgent"), "").create();
        LogRecord log5 = Instancio.of(LogRecord.class).set(field("httpUserAgent"), "Mozilla 2")
            .set(field("bytesSent"), 508342)
            .create();

        LogFilter filter1 = LogFilterField.create("bytesSent", "3\\d+2");
        LogFilter filter2 = LogFilterField.create("agent", "Mozilla.*");
        List<LogRecord> filtered = filter2.filter(filter1.filter(Stream.of(log1, log2, log3, log4, log5))).toList();

        assertThat(filtered).containsExactly(log3);
    }
}
