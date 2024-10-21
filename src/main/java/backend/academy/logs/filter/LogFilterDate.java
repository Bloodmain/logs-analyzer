package backend.academy.logs.filter;

import backend.academy.logs.parser.LogRecord;
import backend.academy.logs.parser.NginxLogFormatConfig;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Stream;

public class LogFilterDate implements LogFilter {
    private final Optional<LocalDateTime> from;
    private final Optional<LocalDateTime> to;

    private LogFilterDate(Optional<LocalDateTime> from, Optional<LocalDateTime> to) {
        this.from = from;
        this.to = to;
    }

    public static LogFilterDate create(OffsetDateTime from, OffsetDateTime to) {
        return new LogFilterDate(
            Optional.ofNullable(from).map(LogFilterDate::toDefaultZone),
            Optional.ofNullable(to).map(LogFilterDate::toDefaultZone)
        );
    }

    private static LocalDateTime toDefaultZone(OffsetDateTime t) {
        return t.atZoneSameInstant(NginxLogFormatConfig.NGINX_DEFAULT_ZONE).toLocalDateTime();
    }

    private boolean filterRecord(LogRecord log) {
        return (from.isEmpty() || log.timeLocal().isAfter(from.orElseThrow()))
            && (to.isEmpty() || log.timeLocal().isBefore(to.orElseThrow()));
    }

    @Override
    public Stream<LogRecord> filter(Stream<LogRecord> logs) {
        return logs.filter(this::filterRecord);
    }

    @Override
    public String toString() {
        return String.format("dateFilter{from=%s, to=%s}", getTime(from), getTime(to));
    }

    private static String getTime(Optional<LocalDateTime> time) {
        return time.map(LocalDateTime::toString).orElse("-");
    }
}
