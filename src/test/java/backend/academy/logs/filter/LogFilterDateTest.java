package backend.academy.logs.filter;

import backend.academy.logs.parser.LogRecord;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

public class LogFilterDateTest {
    @Test
    public void allDates() {
        LocalDateTime date1 = LocalDateTime.of(2024, 7, 3, 12, 30, 30);
        LocalDateTime date2 = LocalDateTime.of(2024, 7, 3, 13, 30, 30);
        LocalDateTime date3 = LocalDateTime.of(2024, 7, 4, 10, 30, 30);
        LogRecord log1 = Instancio.of(LogRecord.class).set(field("timeLocal"), date1).create();
        LogRecord log2 = Instancio.of(LogRecord.class).set(field("timeLocal"), date2).create();
        LogRecord log3 = Instancio.of(LogRecord.class).set(field("timeLocal"), date3).create();

        LogFilter filter = LogFilterDate.create(null, null);
        List<LogRecord> filtered = filter.filter(Stream.of(log1, log2, log3)).toList();

        assertThat(filtered).containsExactly(log1, log2, log3);
    }

    @Test
    public void fromDate() {
        LocalDateTime date1 = LocalDateTime.of(2024, 7, 3, 12, 30, 30);
        LocalDateTime date2 = LocalDateTime.of(2024, 7, 3, 13, 30, 30);
        LocalDateTime date3 = LocalDateTime.of(2024, 7, 4, 10, 30, 30);
        LogRecord log1 = Instancio.of(LogRecord.class).set(field("timeLocal"), date1).create();
        LogRecord log2 = Instancio.of(LogRecord.class).set(field("timeLocal"), date2).create();
        LogRecord log3 = Instancio.of(LogRecord.class).set(field("timeLocal"), date3).create();

        LogFilter filter = LogFilterDate.create(date2.atOffset(ZoneOffset.UTC), null);
        List<LogRecord> filtered = filter.filter(Stream.of(log1, log2, log3)).toList();

        assertThat(filtered).containsExactly(log3);
    }

    @Test
    public void toDate() {
        LocalDateTime date1 = LocalDateTime.of(2024, 7, 3, 12, 30, 30);
        LocalDateTime date2 = LocalDateTime.of(2024, 7, 3, 13, 30, 30);
        LocalDateTime date3 = LocalDateTime.of(2024, 7, 4, 10, 30, 30);
        LogRecord log1 = Instancio.of(LogRecord.class).set(field("timeLocal"), date1).create();
        LogRecord log2 = Instancio.of(LogRecord.class).set(field("timeLocal"), date2).create();
        LogRecord log3 = Instancio.of(LogRecord.class).set(field("timeLocal"), date3).create();

        LogFilter filter = LogFilterDate.create(null, date2.atOffset(ZoneOffset.UTC));
        List<LogRecord> filtered = filter.filter(Stream.of(log1, log2, log3)).toList();

        assertThat(filtered).containsExactly(log1);
    }

    @Test
    public void fromToDate() {
        LocalDateTime date1 = LocalDateTime.of(2024, 7, 3, 12, 30, 30);
        LocalDateTime date2 = LocalDateTime.of(2024, 7, 3, 13, 30, 30);
        LocalDateTime date3 = LocalDateTime.of(2024, 7, 4, 10, 30, 30);
        LogRecord log1 = Instancio.of(LogRecord.class).set(field("timeLocal"), date1).create();
        LogRecord log2 = Instancio.of(LogRecord.class).set(field("timeLocal"), date2).create();
        LogRecord log3 = Instancio.of(LogRecord.class).set(field("timeLocal"), date3).create();

        LogFilter filter = LogFilterDate.create(date1.atOffset(ZoneOffset.UTC), date3.atOffset(ZoneOffset.UTC));
        List<LogRecord> filtered = filter.filter(Stream.of(log1, log2, log3)).toList();

        assertThat(filtered).containsExactly(log2);
    }

    @Test
    public void fromExceedToDate() {
        LocalDateTime date1 = LocalDateTime.of(2024, 7, 3, 12, 30, 30);
        LocalDateTime date2 = LocalDateTime.of(2024, 7, 3, 13, 30, 30);
        LocalDateTime date3 = LocalDateTime.of(2024, 7, 4, 10, 30, 30);
        LogRecord log1 = Instancio.of(LogRecord.class).set(field("timeLocal"), date1).create();
        LogRecord log2 = Instancio.of(LogRecord.class).set(field("timeLocal"), date2).create();
        LogRecord log3 = Instancio.of(LogRecord.class).set(field("timeLocal"), date3).create();

        LogFilter filter = LogFilterDate.create(date3.atOffset(ZoneOffset.UTC), date1.atOffset(ZoneOffset.UTC));
        List<LogRecord> filtered = filter.filter(Stream.of(log1, log2, log3)).toList();

        assertThat(filtered).isEmpty();
    }
}
