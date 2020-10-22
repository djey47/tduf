package fr.tduf.libunlimited.common.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
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

    static {
        mapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
    }

    /**
     * @return formatted string with default pretty printer
     */
    public static String prettify(String input) throws IOException {
        Object source = mapper.readValue(requireNonNull(input, "JSON input string is required"), Object.class);
        return writer.writeValueAsString(source);
    }

    /**
     * @return true if provided string is valid JSON
     */
    public static boolean isValid(final String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch(JsonProcessingException jpe){
            return false;
        }
    }
}
