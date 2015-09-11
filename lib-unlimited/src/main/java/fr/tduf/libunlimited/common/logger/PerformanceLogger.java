package fr.tduf.libunlimited.common.logger;

import com.esotericsoftware.minlog.Log;

import java.io.*;
import java.nio.file.Path;
import java.util.Date;

import static com.esotericsoftware.minlog.Log.*;
import static java.util.Objects.requireNonNull;

/**
 * Minlog logger implementation for performance tracing into a file.
 */
public class PerformanceLogger extends Log.Logger {

    private static final String PERF_LOG_FILE_NAME = "tduf-perfs.log";

    private final long firstLogTime = new Date().getTime();

    private final Path logFilePath;

    public PerformanceLogger(Path parentPath) {
        requireNonNull(parentPath);

        logFilePath = parentPath.resolve(PERF_LOG_FILE_NAME);
    }

    private PerformanceLogger() {
        logFilePath = null;
    }

    @Override
    public void log(int level, String category, String message, Throwable ex) {
        StringBuilder builder = new StringBuilder(256);

        long time = new Date().getTime() - firstLogTime;
        long hours = time / (1000 * 60 * 60);
        long minutes = time / (1000 * 60);
        long seconds = time / (1000) % 60;
        long millis = time % 1000;
        if (hours <= 9) builder.append('0');
        builder.append(hours);
        builder.append(':');
        if (minutes <= 9) builder.append('0');
        builder.append(minutes);
        builder.append(':');
        if (seconds <= 9) builder.append('0');
        builder.append(seconds);
        builder.append(':');
        if (millis <= 99) builder.append('0');
        if (millis <= 9) builder.append('0');
        builder.append(millis);

        switch (level) {
            case LEVEL_ERROR:
                builder.append(" ERROR: ");
                break;
            case LEVEL_WARN:
                builder.append("  WARN: ");
                break;
            case LEVEL_INFO:
                builder.append("  INFO: ");
                break;
            case LEVEL_DEBUG:
                builder.append(" DEBUG: ");
                break;
            case LEVEL_TRACE:
                builder.append(" TRACE: ");
                break;
        }

        if (category != null) {
            builder.append('[');
            builder.append(category);
            builder.append("] ");
        }

        builder.append(message);

        if (ex != null) {
            StringWriter writer = new StringWriter(256);
            ex.printStackTrace(new PrintWriter(writer));
            builder.append('\n');
            builder.append(writer.toString().trim());
        }

        print(builder.toString());
    }

    @Override
    protected void print(String message) {
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath.toFile(), true)))) {
            out.println(message);
        } catch (IOException e) {
            super.print(message);
            e.printStackTrace();
        }
    }
}