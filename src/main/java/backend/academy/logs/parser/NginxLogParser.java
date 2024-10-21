package backend.academy.logs.parser;

import backend.academy.logs.exceptions.ParseException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.stream.Stream;

/**
 * Parses NGINX logs with date format "dd/MMM/yyyy:HH:mm:ss Z" converting dates to the UTC offset.
 */
public class NginxLogParser implements LogParser {
    @Override
    public Stream<LogRecord> parse(Stream<String> logLines) {
        return logLines.map(NginxLogParser::parseNginx);
    }

    private static LogRecord parseNginx(String line) {
        Matcher matcher = NginxLogFormatConfig.NGINX_LOG_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new ParseException("Invalid Nginx log line: " + line);
        }

        try {
            String remoteAddr = matcher.group(NginxLogFormatConfig.REMOTE_ADDR_GROUP);
            String remoteUser = matcher.group(NginxLogFormatConfig.REMOTE_USER_GROUP);
            LocalDateTime timeLocal =
                NginxLogFormatConfig.NGINX_TIME_FORMAT.parse(
                        matcher.group(NginxLogFormatConfig.DATE_GROUP), OffsetDateTime::from
                    )
                    .atZoneSameInstant(NginxLogFormatConfig.NGINX_DEFAULT_ZONE)
                    .toLocalDateTime();

            String method = matcher.group(NginxLogFormatConfig.METHOD_GROUP);
            String resource = matcher.group(NginxLogFormatConfig.RESOURCE_GROUP);
            int status = Integer.parseInt(matcher.group(NginxLogFormatConfig.STATUS_GROUP));
            int bytesSent = Integer.parseInt(matcher.group(NginxLogFormatConfig.BYTE_SENT_GROUP));
            String referer = matcher.group(NginxLogFormatConfig.REFERER_GROUP);
            String userAgent = matcher.group(NginxLogFormatConfig.USER_AGENT_GROUP);

            return new LogRecord(
                remoteAddr, remoteUser, timeLocal, method, resource, status, bytesSent, referer, userAgent
            );
        } catch (DateTimeParseException e) {
            throw new ParseException("Invalid date in Nginx log: " + matcher.group(NginxLogFormatConfig.DATE_GROUP), e);
        }
    }
}
