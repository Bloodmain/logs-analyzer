package backend.academy.logs.parser;

import java.util.stream.Stream;

public interface LogParser {
    Stream<LogRecord> parse(Stream<String> logLines);
}
