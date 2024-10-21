package backend.academy.logs.filter;

import backend.academy.logs.exceptions.BadFieldException;
import backend.academy.logs.parser.LogRecord;
import backend.academy.logs.parser.NginxLogFormatConfig;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LogFilterField implements LogFilter {
    private final Predicate<LogRecord> predicate;
    private final String patternString;
    private final String selectorString;

    private LogFilterField(
        Pattern pattern,
        Function<LogRecord, String> selector,
        String patternString,
        String selectorString
    ) {
        predicate = l -> pattern.matcher(selector.apply(l)).matches();
        this.patternString = patternString;
        this.selectorString = selectorString;
    }

    public static LogFilterField create(String field, String pattern) {
        Function<LogRecord, String> selector = NginxLogFormatConfig.SELECTORS.get(field);
        if (selector == null) {
            throw new BadFieldException("Required filtering of a non-existent field: " + field);
        }
        Pattern p = Pattern.compile(pattern);
        return new LogFilterField(p, selector, pattern, field);
    }

    @Override
    public Stream<LogRecord> filter(Stream<LogRecord> logs) {
        return logs.filter(predicate);
    }

    @Override
    public String toString() {
        return String.format("%sFilter{pattern=%s}", selectorString, patternString);
    }
}
