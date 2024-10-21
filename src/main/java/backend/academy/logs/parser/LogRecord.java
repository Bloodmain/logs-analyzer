package backend.academy.logs.parser;

import java.time.LocalDateTime;

@SuppressWarnings("RecordComponentNumber")
public record LogRecord(
    String remoteAddr, String remoteUser, LocalDateTime timeLocal, String method, String resource,
    int status, int bytesSent, String httpReferer, String httpUserAgent
) {
}
