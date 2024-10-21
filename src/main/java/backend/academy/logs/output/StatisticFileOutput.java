package backend.academy.logs.output;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StatisticFileOutput implements StatisticOutput {
    private final Path file;

    private StatisticFileOutput(Path file) {
        this.file = file;
    }

    @SuppressFBWarnings(
        value = "PATH_TRAVERSAL_IN",
        justification = "We can't filter destination anyhow"
    )
    public static StatisticFileOutput create(String filename) {
        return new StatisticFileOutput(Paths.get(filename));
    }

    @Override
    public void write(String result) throws IOException {
        Files.writeString(file, result);
    }
}
