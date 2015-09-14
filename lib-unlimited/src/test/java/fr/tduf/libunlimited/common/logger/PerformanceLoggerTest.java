package fr.tduf.libunlimited.common.logger;

import com.esotericsoftware.minlog.Log;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PerformanceLoggerTest {

    private Path perfLogPath;

    @Before
    public void setUp() throws IOException {
        perfLogPath = Files
                .createTempDirectory("libUnlimited-tests")
                .resolve(Paths.get("tduf-perfs.log"));

        Log.setLogger(new PerformanceLogger(perfLogPath.getParent()));
    }

    @Test(expected = NullPointerException.class)
    public void newPerformanceLogger_whenParentPathIsNull_shouldThrowException() {
        // GIVEN-WHEN
        new PerformanceLogger(null);

        // THEN: NPE
    }

    @Test
    public void info_whenInfoLevel_shouldWriteMessageInFile() throws IOException {
        // GIVEN-WHEN
        Log.set(Log.LEVEL_INFO);
        Log.info("PerformanceLoggerTest", "here is a logged line!");


        // THEN
        System.out.println("Perf log file:" + perfLogPath);

        List<String> loggedMessages = getLoggedMessages();
        assertThat(loggedMessages).hasSize(1);
        assertThat(loggedMessages.get(0))
                .hasSize(66)
                .contains("INFO: [PerformanceLoggerTest] here is a logged line!");
    }

    @Test
    public void info_withoutCategory_shouldWriteMessageInFile() throws IOException {
        // GIVEN-WHEN
        Log.set(Log.LEVEL_INFO);
        Log.info("here is a logged line without category!");


        // THEN
        System.out.println("Perf log file:" + perfLogPath);

        List<String> loggedMessages = getLoggedMessages();
        assertThat(loggedMessages).hasSize(1);
        assertThat(loggedMessages.get(0))
                .hasSize(59)
                .contains("INFO: here is a logged line without category!");
    }

    @Test
    public void error_withException_shouldWriteMessageInFile() throws IOException {
        // GIVEN-WHEN
        Log.set(Log.LEVEL_INFO);
        Log.error("PerformanceLoggerTest", "here is a logged exception!", new IllegalArgumentException("iae!"));


        // THEN
        System.out.println("Perf log file:" + perfLogPath);

        List<String> loggedMessages = getLoggedMessages();
        assertThat(loggedMessages.size()).isGreaterThan(1);
        assertThat(loggedMessages.get(0))
                .hasSize(71)
                .contains("ERROR: [PerformanceLoggerTest] here is a logged exception!");
        assertThat(loggedMessages.get(1)).isEqualTo("java.lang.IllegalArgumentException: iae!");
    }

    private List<String> getLoggedMessages() throws IOException {
        try {
            // Wait for asynchronous ops to end
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(perfLogPath.toFile()).exists();

        return Files.readAllLines(perfLogPath);
    }
}