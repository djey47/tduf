package fr.tduf.libunlimited.common.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Provides utility method for JSON handling
 */
public class JsonHelper {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

    private JsonHelper() {}

    /**
     * @return formatted string with default pretty printer
     */
    public static String prettify(String input) throws IOException {
        Object source = mapper.readValue(requireNonNull(input, "JSON input string is required"), Object.class);
        return writer.writeValueAsString(source);
    }
}
