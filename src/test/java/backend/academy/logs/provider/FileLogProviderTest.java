package backend.academy.logs.provider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class FileLogProviderTest {
    private static List<String> getString(FileView view) {
        try (Stream<String> f = view.getContent()) {
            return f.toList();
        }
    }

    @Test
    public void oneFile() throws IOException {
        Path dir = Files.createTempDirectory(".");
        Path file = Files.createTempFile(dir, "crocodileTEST", null);
        Files.writeString(file, String.format("A simple noop usecase %n NextCase is"));

        LogProvider provider = FileLogProvider.create(dir.toString(), "**crocodileTEST*");

        List<FileView> files = assertDoesNotThrow(provider::getLogs);
        assertThat(files).hasSize(1);

        assertThat(getString(files.getFirst())).isEqualTo(List.of(
            "A simple noop usecase ",
            " NextCase is"
        ));
    }

    @Test
    public void manyFile() throws IOException {
        Path dir = Files.createTempDirectory(".");
        Path file1 = Files.createTempFile(dir, "crocodileTEST1", null);
        Path file2 = Files.createTempFile(dir, "crocodileTEST2", null);
        Files.writeString(file1, String.format("A simple noop usecase %n NextCase is"));
        Files.writeString(file2, "And here is not");

        LogProvider provider = FileLogProvider.create(dir.toString(), "**crocodileTEST*");

        List<FileView> files = assertDoesNotThrow(provider::getLogs);
        assertThat(files).hasSize(2);

        Set<List<String>> actual = Set.of(
            getString(files.get(0)),
            getString(files.get(1))
        );

        assertThat(actual).contains(List.of("A simple noop usecase ", " NextCase is"), List.of("And here is not"));
    }

    @Test
    public void subDirectories() throws IOException {
        Path dir = Files.createTempDirectory(".");
        Path subDir1 = Files.createTempDirectory(dir, "");
        Path subDir2 = Files.createTempDirectory(subDir1, "");

        Path file1 = Files.createTempFile(dir, null, ".namaste");
        Path file2 = Files.createTempFile(subDir1, null, ".namaste");
        Path file3 = Files.createTempFile(subDir2, null, ".namaste");

        Files.writeString(file1, "main dir");
        Files.writeString(file2, "First sub");
        Files.writeString(file3, "second sub");

        LogProvider provider = FileLogProvider.create(dir.toString(), "**.namaste");

        List<FileView> files = assertDoesNotThrow(provider::getLogs);
        assertThat(files).hasSize(3);

        Set<List<String>> actual = Set.of(
            getString(files.get(0)),
            getString(files.get(1)),
            getString(files.get(2))
        );

        assertThat(actual).contains(List.of("main dir"), List.of("First sub"), List.of("second sub"));
    }
}
