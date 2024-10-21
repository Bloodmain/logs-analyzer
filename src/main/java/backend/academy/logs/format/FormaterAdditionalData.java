package backend.academy.logs.format;

import backend.academy.logs.filter.LogFilter;
import java.util.List;

public record FormaterAdditionalData(List<String> fileNames, List<LogFilter> filtersApplied) {
}
