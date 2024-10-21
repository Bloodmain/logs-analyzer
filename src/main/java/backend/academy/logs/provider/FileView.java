package backend.academy.logs.provider;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.io.function.Uncheck;

public record FileView(String name, Supplier<InputStream> inputSupplier) {
    /**
     * Get the stream of lines from the given input.
     * Important: the returned stream must be closed after its usage in order to prevent a file descriptor leak.
     *
     * @return stream of lines
     */
    @SuppressFBWarnings(
        value = "OS_OPEN_STREAM",
        justification = "stream should be used for laziness and we can't close the stream before its usage,"
            + " so it's a responsibility of other classes "
    )
    public Stream<String> getContent() {
        InputStream inputStream = inputSupplier.get();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        return bufferedReader.lines().onClose(
            () -> Uncheck.run(() -> {
                bufferedReader.close();
                inputStream.close();
            })
        );
    }
}
