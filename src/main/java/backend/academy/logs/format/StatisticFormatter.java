package backend.academy.logs.format;

import backend.academy.logs.statistic.StatisticCollector;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class StatisticFormatter {
    private static final Map<Integer, String> HTTP_STATUS_NAMES = Map.ofEntries(
        Map.entry(200, "OK"),
        Map.entry(201, "Created"),
        Map.entry(202, "Accepted"),
        Map.entry(204, "No Content"),
        Map.entry(301, "Moved Permanently"),
        Map.entry(302, "Found"),
        Map.entry(303, "See Other"),
        Map.entry(304, "Not Modified"),
        Map.entry(307, "Temporary Redirect"),
        Map.entry(308, "Permanent Redirect"),
        Map.entry(400, "Bad Request"),
        Map.entry(401, "Unauthorized"),
        Map.entry(403, "Forbidden"),
        Map.entry(404, "Not Found"),
        Map.entry(405, "Method Not Allowed"),
        Map.entry(408, "Request Timeout"),
        Map.entry(409, "Conflict"),
        Map.entry(410, "Gone"),
        Map.entry(415, "Unsupported Media Type"),
        Map.entry(429, "Too Many Requests"),
        Map.entry(500, "Internal Server Error"),
        Map.entry(501, "Not Implemented"),
        Map.entry(502, "Bad Gateway"),
        Map.entry(503, "Service Unavailable"),
        Map.entry(504, "Gateway Timeout")
    );

    private static final Comparator<Map.Entry<?, Integer>> ENTRY_VALUES_COMPARATOR =
        Comparator.<Map.Entry<?, Integer>>comparingInt(Map.Entry::getValue).reversed();

    @SuppressWarnings(value = {"MagicNumber", "MultipleStringLiterals"})
    public String format(StatisticCollector collector, FormaterAdditionalData additionalData) {
        String filtersApplied = additionalData.filtersApplied().isEmpty() ? "-"
            : additionalData.filtersApplied().stream()
            .map(f -> highlight(f.toString()))
            .collect(Collectors.joining(", "));

        List<List<String>> generalMetrics = List.of(
            List.of(
                "Metric",
                "Value"
            ),
            List.of(
                "File(-s)",
                additionalData.fileNames().stream().map(this::highlight).collect(Collectors.joining(", "))
            ),
            List.of(
                "Applied filters",
                filtersApplied
            ),
            List.of(
                "Requests count",
                String.valueOf(collector.requestsCount())
            ),
            List.of(
                "Average response size",
                String.valueOf(collector.average())
            ),
            List.of(
                "95p response size",
                collector.percentile(95).map(String::valueOf).orElse("-")
            ),
            List.of(
                "Max response size",
                String.valueOf(collector.maxRequestsSize())
            ),
            List.of(
                "Total response size",
                String.valueOf(collector.totalResponseSize())
            )
        );

        List<List<String>> accessedResources = generateTable(
            List.of("Resource", "Count"),
            collector.resourcesCount(),
            entry -> List.of(entry.getKey(), String.valueOf(entry.getValue()))
        );

        List<List<String>> statusCodes = generateTable(
            List.of("Code", "Name", "Count"),
            collector.statusCount(),
            entry -> List.of(
                String.valueOf(entry.getKey()), getStatusName(entry.getKey()), String.valueOf(entry.getValue())
            )
        );
        List<List<String>> methods = generateTable(
            List.of("Method", "Count"),
            collector.methodsCount(),
            entry -> List.of(entry.getKey(), String.valueOf(entry.getValue()))
        );

        List<List<String>> requestsPerDay = generateTable(
            List.of("Day", "Count"),
            collector.requestsPerDay(),
            entry -> List.of(entry.getKey().toString(), String.valueOf(entry.getValue()))
        );

        List<String> lines = List.of(
            formatHeader("General information"),
            formatTable(generalMetrics),
            formatHeader("Accessed resources"),
            formatTable(accessedResources),
            formatHeader("Status codes"),
            formatTable(statusCodes),
            formatHeader("Used methods"),
            formatTable(methods),
            formatHeader("Requests per day"),
            formatTable(requestsPerDay)
        );

        return lines.stream().collect(Collectors.joining(String.format("%n%n")));

    }

    private static <T> List<List<String>> generateTable(
        List<String> headers,
        Map<T, Integer> data,
        Function<Map.Entry<T, Integer>, List<String>> mapper
    ) {
        List<List<String>> table = new ArrayList<>(List.of(headers));

        table.addAll(
            data.entrySet().stream()
                .sorted(ENTRY_VALUES_COMPARATOR)
                .map(mapper)
                .toList()
        );

        return table;
    }

    private static String getStatusName(int status) {
        return HTTP_STATUS_NAMES.getOrDefault(status, "-");
    }

    protected abstract String formatTable(List<List<String>> rows);

    protected abstract String formatHeader(String name);

    protected abstract String highlight(String word);
}
