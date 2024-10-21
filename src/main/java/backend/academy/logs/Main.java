package backend.academy.logs;

import backend.academy.logs.exceptions.BadFieldException;
import backend.academy.logs.exceptions.ParseException;
import backend.academy.logs.filter.LogFilter;
import backend.academy.logs.filter.LogFilterDate;
import backend.academy.logs.filter.LogFilterField;
import backend.academy.logs.format.AdocFormatter;
import backend.academy.logs.format.FormaterAdditionalData;
import backend.academy.logs.format.MarkdownFormatter;
import backend.academy.logs.format.StatisticFormatter;
import backend.academy.logs.output.StatisticFileOutput;
import backend.academy.logs.parser.LogParser;
import backend.academy.logs.parser.LogRecord;
import backend.academy.logs.parser.NginxLogParser;
import backend.academy.logs.provider.FileLogProvider;
import backend.academy.logs.provider.FileView;
import backend.academy.logs.provider.LogProvider;
import backend.academy.logs.provider.UrlLogProvider;
import backend.academy.logs.statistic.StatisticCollector;
import com.beust.jcommander.IParametersValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Main {
    @SuppressWarnings(value = "ReturnCount") // otherwise more nested scopes that is harder to read
    public static void main(String[] args) {
        LogAnalyzerArgs jArgs = new LogAnalyzerArgs();
        JCommander cmd = JCommander.newBuilder()
            .addObject(jArgs)
            .build();

        try {
            cmd.parse(args);
        } catch (ParameterException e) {
            errorAndUsage(cmd, "Bad analyzer arguments: ", e);
            return;
        }

        if (jArgs.help) {
            cmd.usage();
            return;
        }

        try {
            LogProvider provider = getProvider(jArgs.src);

            try {
                List<LogFilter> filters = getFilters(jArgs);

                try {
                    List<FileView> files = provider.getLogs();
                    LogParser parser = new NginxLogParser();
                    StatisticCollector collector = new StatisticCollector();
                    List<String> filenames = new ArrayList<>();

                    for (FileView file : files) {
                        collectLogsFromFile(file, parser, filters, filenames, collector);
                    }

                    FormaterAdditionalData additionalData = new FormaterAdditionalData(filenames, filters);
                    StatisticFormatter formatter = getFormatter(jArgs.outputFormat);
                    String result = formatter.format(collector, additionalData);
                    printResult(jArgs.dst, System.out, result, cmd);

                } catch (IOException | UncheckedIOException e) {
                    errorAndUsage(cmd, "Can't read provided source: ", e);
                }
            } catch (BadFieldException | PatternSyntaxException e) {
                errorAndUsage(cmd, "Bad filters provided: ", e);
            }
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            errorAndUsage(cmd, "Can't get source: ", e);
        }
    }

    private static void collectLogsFromFile(
        FileView file,
        LogParser parser,
        List<LogFilter> filters,
        List<String> filenames,
        StatisticCollector collector
    ) {
        try (Stream<String> lines = file.getContent()) {
            Stream<LogRecord> logs = parser.parse(lines);
            for (LogFilter filter : filters) {
                logs = filter.filter(logs);
            }
            filenames.add(file.name());
            logs.forEach(collector::addLog);
        } catch (ParseException e) {
            System.err.printf("Bad logs in file %s: %s%n", file.name(), e.getMessage());
        }
    }

    private static List<LogFilter> getFilters(LogAnalyzerArgs jArgs) {
        List<LogFilter> filters = new ArrayList<>();
        if (Objects.nonNull(jArgs.from) || Objects.nonNull(jArgs.to)) {
            filters.add(LogFilterDate.create(jArgs.from, jArgs.to));
        }
        if (Objects.nonNull(jArgs.filtersFields)) {
            for (int i = 0; i < jArgs.filtersFields.size(); i++) {
                filters.add(LogFilterField.create(jArgs.filtersFields.get(i), jArgs.filtersPatterns.get(i)));
            }
        }
        return filters;
    }

    private static StatisticFormatter getFormatter(OutputFormats format) {
        return switch (format) {
            case markdown -> new MarkdownFormatter();
            case adoc -> new AdocFormatter();
        };
    }

    private static LogProvider getProvider(String src) {
        try {
            return UrlLogProvider.create(src);
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            return FileLogProvider.create(".", src);
        }
    }

    private static void printResult(String dst, PrintStream defaultStream, String result, JCommander cmd) {
        try {
            if (Objects.isNull(dst)) {
                defaultStream.println(result);
            } else {
                StatisticFileOutput.create(dst).write(result);
            }
        } catch (IOException e) {
            errorAndUsage(cmd, "Can't write to output file", e);
        } catch (InvalidPathException e) {
            errorAndUsage(cmd, "Bad path for output file", e);
        }
    }

    private static void errorAndUsage(JCommander cmd, String msg, Throwable e) {
        System.err.println(msg + e.getMessage());
        cmd.usage();
    }

    public static class TimeConverter implements IStringConverter<OffsetDateTime> {
        @Override
        public OffsetDateTime convert(String value) {
            try {
                return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value, OffsetDateTime::from);
            } catch (DateTimeParseException e) {
                throw new ParameterException("Invalid time: " + e.getMessage(), e);
            }
        }
    }

    public static class FieldsAndPatternToFilterHaveTheSameLength implements IParametersValidator {
        @Override
        public void validate(Map<String, Object> parameters) throws ParameterException {
            List<String> ff = (List<String>) parameters.get("--filter-field");
            List<String> fo = (List<String>) parameters.get("--filter-pattern");
            if (Objects.isNull(ff) != Objects.isNull(fo)
                || Objects.nonNull(ff) && ff.size() != fo.size()) {
                throw new ParameterException("fields and patterns to filter should have the same length");
            }
        }
    }

    public enum OutputFormats {
        markdown, adoc
    }

    @Parameters(parametersValidators = FieldsAndPatternToFilterHaveTheSameLength.class)
    public static class LogAnalyzerArgs {
        @Parameter(
            names = {"--source", "--src", "-s"},
            description = "glob/url for log file(-s) to analyze",
            required = true
        )
        public String src;

        @Parameter(
            names = {"--from", "-f"},
            description = "time in ISO8601 format, logs to be analyzed after which",
            converter = TimeConverter.class
        )
        public OffsetDateTime from;

        @Parameter(
            names = {"--to", "-t"},
            description = "time in ISO8601 format,logs to be analyzed before which",
            converter = TimeConverter.class
        )
        public OffsetDateTime to;

        @Parameter(
            names = {"--format", "-o"},
            description = "output format"
        )
        public OutputFormats outputFormat = OutputFormats.markdown;

        @Parameter(
            names = {"--filter-field", "-ff"},
            description = "fields to filter",
            variableArity = true
        )
        public List<String> filtersFields = new ArrayList<>();

        @Parameter(
            names = {"--filter-pattern", "-fp"},
            description = "patterns to filter",
            variableArity = true
        )
        public List<String> filtersPatterns = new ArrayList<>();

        @Parameter(
            names = {"--destination", "--dst", "-d"},
            description = "filename to save the result"
        )
        public String dst;

        @Parameter(
            names = {"--help", "-h"},
            description = "show help",
            help = true
        )
        public boolean help = false;
    }
}
