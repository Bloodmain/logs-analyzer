package backend.academy.logs.parser;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NginxLogFormatConfig {
    public static final Pattern NGINX_LOG_PATTERN = Pattern.compile(
        "(\\S*) - (\\S*) \\[(.*)] \"(\\w*) (\\S*) (\\S*)\" (\\d*) (\\d*) \"(\\S*)\" \"(.*)\""
    );
    public static final DateTimeFormatter NGINX_TIME_FORMAT = DateTimeFormatter.ofPattern(
        "dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH
    );
    public static final ZoneOffset NGINX_DEFAULT_ZONE = ZoneOffset.UTC;

    public static final int REMOTE_ADDR_GROUP = 1;
    public static final int REMOTE_USER_GROUP = 2;
    public static final int DATE_GROUP = 3;
    public static final int METHOD_GROUP = 4;
    public static final int RESOURCE_GROUP = 5;
    public static final int STATUS_GROUP = 7;
    public static final int BYTE_SENT_GROUP = 8;
    public static final int REFERER_GROUP = 9;
    public static final int USER_AGENT_GROUP = 10;

    public static final Map<String, Function<LogRecord, String>> SELECTORS = Map.of(
        "address", LogRecord::remoteAddr,
        "user", LogRecord::remoteUser,
        "method", LogRecord::method,
        "resource", LogRecord::resource,
        "status", l -> String.valueOf(l.status()),
        "bytesSent", l -> String.valueOf(l.bytesSent()),
        "referer", LogRecord::httpReferer,
        "agent", LogRecord::httpUserAgent
    );

}
