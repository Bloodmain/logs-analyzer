package backend.academy.logs.statistic;

import backend.academy.logs.parser.LogRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

public class StatisticCollectorTest {
    @Test
    public void testCount() {
        StatisticCollector collector = new StatisticCollector();
        List<LogRecord> records = Instancio.ofList(LogRecord.class).size(47).create();

        records.forEach(collector::addLog);

        assertThat(collector.requestsCount()).isEqualTo(47);
    }

    @Test
    public void testTotalSize() {
        StatisticCollector collector = new StatisticCollector();
        List<LogRecord> records = List.of(
            Instancio.of(LogRecord.class).set(field("bytesSent"), 134).create(),
            Instancio.of(LogRecord.class).set(field("bytesSent"), 0).create(),
            Instancio.of(LogRecord.class).set(field("bytesSent"), 3).create()
        );

        records.forEach(collector::addLog);

        assertThat(collector.totalResponseSize()).isEqualTo(137);
    }

    @Test
    public void testResourcesCount() {
        StatisticCollector collector = new StatisticCollector();
        List<LogRecord> records = List.of(
            Instancio.of(LogRecord.class).set(field("resource"), "nop").create(),
            Instancio.of(LogRecord.class).set(field("resource"), "noop").create(),
            Instancio.of(LogRecord.class).set(field("resource"), "nop").create()
        );

        records.forEach(collector::addLog);

        assertThat(collector.resourcesCount()).isEqualTo(
            Map.of(
                "nop", 2,
                "noop", 1
            )
        );
    }

    @Test
    public void testStatusCount() {
        StatisticCollector collector = new StatisticCollector();
        List<LogRecord> records = List.of(
            Instancio.of(LogRecord.class).set(field("status"), 321).create(),
            Instancio.of(LogRecord.class).set(field("status"), 321).create(),
            Instancio.of(LogRecord.class).set(field("status"), 404).create()
        );

        records.forEach(collector::addLog);

        assertThat(collector.statusCount()).isEqualTo(
            Map.of(
                404, 1,
                321, 2
            )
        );
    }

    @Test
    public void testRequestsPerDay() {
        StatisticCollector collector = new StatisticCollector();
        List<LogRecord> records = List.of(
            Instancio.of(LogRecord.class).set(field("timeLocal"), LocalDateTime.of(2024, 11, 12, 10, 30, 20)).create(),
            Instancio.of(LogRecord.class).set(field("timeLocal"), LocalDateTime.of(2024, 11, 12, 12, 30, 20)).create(),
            Instancio.of(LogRecord.class).set(field("timeLocal"), LocalDateTime.of(2024, 11, 12, 3, 2, 1)).create(),
            Instancio.of(LogRecord.class).set(field("timeLocal"), LocalDateTime.of(2024, 12, 12, 10, 30, 20)).create(),
            Instancio.of(LogRecord.class).set(field("timeLocal"), LocalDateTime.of(2023, 11, 12, 10, 30, 20)).create()
        );

        records.forEach(collector::addLog);

        assertThat(collector.requestsPerDay()).isEqualTo(
            Map.of(
                LocalDate.of(2024, 11, 12), 3,
                LocalDate.of(2024, 12, 12), 1,
                LocalDate.of(2023, 11, 12), 1
            )
        );
    }

    @Test
    public void testMaxRequestsSize() {
        StatisticCollector collector = new StatisticCollector();
        List<LogRecord> records = List.of(
            Instancio.of(LogRecord.class).set(field("bytesSent"), 134).create(),
            Instancio.of(LogRecord.class).set(field("bytesSent"), 0).create(),
            Instancio.of(LogRecord.class).set(field("bytesSent"), 3).create()
        );

        records.forEach(collector::addLog);

        assertThat(collector.maxRequestsSize()).isEqualTo(134);
    }

    @ParameterizedTest
    @CsvSource({
        "95, 6",
        "95, 134",
        "63, 213",
        "23, 11",
        "100, 100",
        "0, 100",
        "1, 1",
        "100, 1",
    })
    public void testPercentile(int percentile, int length) {
        StatisticCollector collector = new StatisticCollector();
        List<LogRecord> records = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            records.add(Instancio.of(LogRecord.class).set(field("bytesSent"), i).create());
        }
        Collections.shuffle(records);

        records.forEach(collector::addLog);

        int expected = percentile == 0 ? 0 : Math.ceilDiv(length * percentile, 100) - 1;
        assertThat(collector.percentile(percentile)).isEqualTo(Optional.of(expected));
    }

    @Test
    public void testEmptyPercentile() {
        StatisticCollector collector = new StatisticCollector();

        assertThat(collector.percentile(95)).isEmpty()
    }
}
