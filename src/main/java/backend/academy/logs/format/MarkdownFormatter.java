package backend.academy.logs.format;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkdownFormatter extends StatisticFormatter {

    @Override
    protected String formatTable(List<List<String>> rows) {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Rows cannot be empty");
        }

        List<String> headers = rows.getFirst();
        List<String> headerDelimiters = new ArrayList<>(headers.size());
        for (String header : headers) {
            headerDelimiters.add("-".repeat(header.length()));
        }
        Stream<List<String>> rowsStream = Stream.concat(
            Stream.of(headers, headerDelimiters),
            rows.subList(1, rows.size()).stream()
        );
        return rowsStream
            .map(row -> row.stream().collect(Collectors.joining(" | ", "| ", " |")))
            .collect(Collectors.joining(String.format("%n")));
    }

    @Override
    protected String formatHeader(String name) {
        return String.format("### %s", name);
    }

    @Override
    protected String highlight(String word) {
        return String.format("`%s`", word);
    }
}
