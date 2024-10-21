package backend.academy.logs.format;

import backend.academy.logs.filter.LogFilterDate;
import backend.academy.logs.filter.LogFilterField;
import backend.academy.logs.statistic.StatisticCollector;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.instancio.Instancio;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

public class FormattersTest {
    private static Stream<StatisticFormatter> getFormatters() {
        return Stream.of(
            new AdocFormatter(),
            new MarkdownFormatter()
        );
    }

    @ParameterizedTest
    @MethodSource("getFormatters")
    public void allMetricsAppears(StatisticFormatter formatter) {
        StatisticCollector collector = Instancio.of(StatisticCollector.class)
            .set(field("totalResponseSize"), 13)
            .set(field("maxRequestsSize"), 17)
            .set(field("bytesSent"), new ArrayList<>(List.of(3, 1, 2, 4, 5, 6, 11, 10, 8, 7, 9)))
            .set(field("resourcesCount"), Map.of(
                "github.io", 27,
                "niuitmo.ru", 26
            ))
            .set(field("statusCount"), Map.of(
                5000, 0
            ))
            .set(field("methodsCount"), Map.of(
                "get", 51,
                "put", 50,
                "head", 52
            ))
            .set(field("requestsPerDay"), Map.of(
                LocalDate.of(2024, 12, 11), 21,
                LocalDate.of(2024, 12, 10), 23
            ))
            .create();
        FormaterAdditionalData additionalData = new FormaterAdditionalData(
            List.of("f1", "f2"), List.of(
            LogFilterField.create("agent", "perry"),
            LogFilterField.create("method", "larry"),
            LogFilterDate.create(
                LocalDate.of(2024, 7, 2).atTime(0, 0).atOffset(ZoneOffset.UTC),
                LocalDate.of(2021, 7, 2).atTime(0, 0).atOffset(ZoneOffset.UTC)
            ))
        );

        String res = formatter.format(collector, additionalData);

        assertThat(res).contains(
            "f1", "f2",
            "perry", "larry", "2024", "7", "2", "2021", "7", "2",
            "11",
            "1.18",
            "3",
            "17",
            "13",
            "27", "26",
            "0",
            "51", "50", "52",
            "21", "23"
        );
    }

    @ParameterizedTest
    @MethodSource("getFormatters")
    public void metricsSorted(StatisticFormatter formatter) {
        StatisticCollector collector = Instancio.of(StatisticCollector.class)
            .set(field("resourcesCount"), Map.of(
                "github.io", 27,
                "niuitmo.ru", 26
            ))
            .set(field("statusCount"), Map.of(
                5000, 0
            ))
            .set(field("methodsCount"), Map.of(
                "get", 51,
                "put", 50,
                "head", 52
            ))
            .set(field("requestsPerDay"), Map.of(
                LocalDate.of(2024, 12, 11), 21,
                LocalDate.of(2024, 12, 10), 23
            ))
            .create();
        FormaterAdditionalData additionalData = new FormaterAdditionalData(List.of(), List.of());

        String res = formatter.format(collector, additionalData);

        assertThat(res).containsSubsequence(
            "27", "26",
            "0",
            "52", "51", "50",
            "23", "21"
        );
    }

    @ParameterizedTest
    @MethodSource("getFormatters")
    public void defaultMetrics(StatisticFormatter formatter) {
        StatisticCollector collector = Instancio.of(StatisticCollector.class)
            .set(field("bytesSent"), new ArrayList<>())
            .create();
        FormaterAdditionalData additionalData = new FormaterAdditionalData(List.of(), List.of());

        String res = formatter.format(collector, additionalData);

        assertThat(res).containsSubsequence(
            "Applied filters", "-", "Requests count",
            "95p response size", "-", "Max response size"
        );
    }
}
