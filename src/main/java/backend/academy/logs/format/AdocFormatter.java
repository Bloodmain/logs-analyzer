package backend.academy.logs.format;

import java.util.List;
import java.util.stream.Collectors;

public class AdocFormatter extends StatisticFormatter {
    @Override
    protected String formatTable(List<List<String>> rows) {
        String table = rows.stream()
            .map(row -> row.stream().collect(Collectors.joining(" |", "|", "")))
            .collect(Collectors.joining(String.format("%n%n")));
        return String.format("|===%n%s%n|===", table);
    }

    @Override
    protected String formatHeader(String name) {
        return String.format("=== %s", name);
    }

    @Override
    protected String highlight(String word) {
        return String.format("+%s+", word);
    }
}
