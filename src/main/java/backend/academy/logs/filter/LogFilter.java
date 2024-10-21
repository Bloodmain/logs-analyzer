package backend.academy.logs.filter;

import backend.academy.logs.parser.LogRecord;
import java.util.stream.Stream;

public interface LogFilter {
    Stream<LogRecord> filter(Stream<LogRecord> logs);
}
