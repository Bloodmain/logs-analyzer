package backend.academy.logs.provider;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.function.Uncheck;

public class FileLogProvider implements LogProvider {
    private final Path dir;
    private final PathMatcher pathMatcher;

    private FileLogProvider(Path dir, PathMatcher pathMatcher) {
        this.dir = dir;
        this.pathMatcher = pathMatcher;
    }

    public static FileLogProvider create(String dir, String glob) {
        return new FileLogProvider(Paths.get(dir), FileSystems.getDefault().getPathMatcher("glob:" + glob));
    }

    @Override
    @SuppressFBWarnings(
        value = "PATH_TRAVERSAL_IN",
        justification = "Logs can be located anywhere, so we can't filter the path anyhow"
    )
    public List<FileView> getLogs() throws IOException {
        List<FileView> files = new ArrayList<>();
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (pathMatcher.matches(path)) {
                    files.add(new FileView(
                        path.toString(),
                        () -> Uncheck.get(() -> Files.newInputStream(path))
                    ));
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return files;
    }
}
