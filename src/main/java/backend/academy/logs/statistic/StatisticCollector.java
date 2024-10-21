package backend.academy.logs.statistic;

import backend.academy.logs.parser.LogRecord;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

public class StatisticCollector {
    @Getter
    private long totalResponseSize = 0;
    @Getter
    private long maxRequestsSize = 0;

    private final List<Integer> bytesSent = new ArrayList<>();

    @Getter
    private final Map<LocalDate, Integer> requestsPerDay = new HashMap<>();
    @Getter
    private final Map<String, Integer> resourcesCount = new HashMap<>();
    @Getter
    private final Map<Integer, Integer> statusCount = new HashMap<>();
    @Getter
    private final Map<String, Integer> methodsCount = new HashMap<>();

    public void addLog(LogRecord log) {
        totalResponseSize += log.bytesSent();
        bytesSent.add(log.bytesSent());
        maxRequestsSize = Math.max(maxRequestsSize, log.bytesSent());

        addToCount(resourcesCount, log.resource());
        addToCount(statusCount, log.status());
        addToCount(requestsPerDay, log.timeLocal().toLocalDate());
        addToCount(methodsCount, log.method());
    }

    private static <T> void addToCount(Map<T, Integer> countMap, T key) {
        countMap.compute(key, (k, v) -> v == null ? 1 : v + 1);
    }

    public long requestsCount() {
        return bytesSent.size();
    }

    @SuppressWarnings(value = "MagicNumber") // 100 is not a magic number, it is a maximum percentage
    public Optional<Integer> percentile(int a) {
        if (a < 0 || a > 100) {
            throw new IllegalArgumentException("percentile must be between 0 and 100");
        }
        if (bytesSent.isEmpty()) {
            return Optional.empty();
        }

        Collections.sort(bytesSent);
        int k = Math.max(0, Math.ceilDiv(bytesSent.size() * a, 100) - 1);
        return Optional.of(bytesSent.get(k));
    }

    public double average() {
        return totalResponseSize / (double) requestsCount();
    }
}
