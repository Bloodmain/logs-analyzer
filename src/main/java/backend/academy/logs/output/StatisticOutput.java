package backend.academy.logs.output;

import java.io.IOException;

public interface StatisticOutput {
    void write(String result) throws IOException;
}
