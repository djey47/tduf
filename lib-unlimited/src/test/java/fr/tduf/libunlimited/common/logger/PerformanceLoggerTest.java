package fr.tduf.libunlimited.common.logger;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libtesting.common.helper.FilesHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PerformanceLoggerTest {

    private static final  Class<PerformanceLoggerTest> thisClass = PerformanceLoggerTest.class;

    private Path perfLogPath;

    @BeforeEach
    void setUp() throws IOException {
        final String tempDirectoryForLibrary = FilesHelper.createTempDirectoryForLibrary();
        perfLogPath = Paths.get(tempDirectoryForLibrary).resolve("tduf-perfs.log");

        Log.setLogger(new PerformanceLogger(perfLogPath.getParent()));
    }

    @Test
    void newPerformanceLogger_whenParentPathIsNull_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> new PerformanceLogger(null));
    }

    @Test
    void info_whenInfoLevel_shouldWriteMessageInFile() throws IOException, InterruptedException {
        // GIVEN-WHEN
        Log.set(Log.LEVEL_INFO);
        Log.info(thisClass.getSimpleName(), "here is a logged line!");


        // THEN
        System.out.println("Perf log file:" + perfLogPath);

        List<String> loggedMessages = getLoggedMessages();
        assertThat(loggedMessages).hasSize(1);
        assertThat(loggedMessages.get(0))
                .hasSize(66)
                .endsWith("  INFO: [PerformanceLoggerTest] here is a logged line!");
    }

    @Test
    void info_whenInfoLevel_andMaxMessages_shouldWriteAllMessagesToFile() throws IOException, InterruptedException {
        // GIVEN-WHEN
        Log.set(Log.LEVEL_INFO);

        for (int i = 1 ; i <= PerformanceLogger.MAX_QUEUED_MESSAGE_COUNT ; i++) {
            Log.info(thisClass.getSimpleName(), "here is a logged line! Iteration " + i);
        }

        // THEN
        System.out.println("Perf log file:" + perfLogPath);

        List<String> loggedMessages = getLoggedMessages();
        assertThat(loggedMessages).hasSize(PerformanceLogger.MAX_QUEUED_MESSAGE_COUNT);
        assertThat(loggedMessages.get(0))
                .endsWith("  INFO: [PerformanceLoggerTest] here is a logged line! Iteration 1");
        assertThat(loggedMessages.get(1023))
                .endsWith("  INFO: [PerformanceLoggerTest] here is a logged line! Iteration 1024");
    }

    @Test
    void info_withoutCategory_shouldWriteMessageInFile() throws IOException, InterruptedException {
        // GIVEN-WHEN
        Log.set(Log.LEVEL_INFO);
        Log.info("here is a logged line without category!");


        // THEN
        System.out.println("Perf log file:" + perfLogPath);

        List<String> loggedMessages = getLoggedMessages();
        assertThat(loggedMessages).hasSize(1);
        assertThat(loggedMessages.get(0))
                .hasSize(59)
                .endsWith("  INFO: here is a logged line without category!");
    }

    @Test
    void error_withException_shouldWriteMessageInFile() throws IOException, InterruptedException {
        // GIVEN-WHEN
        Log.set(Log.LEVEL_INFO);
        Log.error("PerformanceLoggerTest", "here is a logged exception!", new IllegalArgumentException("iae!"));


        // THEN
        System.out.println("Perf log file:" + perfLogPath);

        List<String> loggedMessages = getLoggedMessages();
        assertThat(loggedMessages.size()).isGreaterThan(1);
        assertThat(loggedMessages.get(0))
                .hasSize(71)
                .endsWith(" ERROR: [PerformanceLoggerTest] here is a logged exception!");
        assertThat(loggedMessages.get(1)).isEqualTo("java.lang.IllegalArgumentException: iae!");
    }

    private List<String> getLoggedMessages() throws IOException, InterruptedException {
        // Wait for asynchronous ops to end
        Thread.sleep(250);

        assertThat(perfLogPath.toFile()).exists();

        return Files.readAllLines(perfLogPath);
    }
}