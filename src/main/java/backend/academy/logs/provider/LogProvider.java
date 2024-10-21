package backend.academy.logs.provider;

import java.io.IOException;
import java.util.List;

public interface LogProvider {
    List<FileView> getLogs() throws IOException;
}
