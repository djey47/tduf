package fr.tduf.cli.tools.mapper;

import fr.tduf.cli.tools.dto.ErrorOutputDto;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;

import static net.sf.json.test.JSONAssert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

public class OutputMapperTest {

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
    private final ObjectReader objectReader = new ObjectMapper().reader();

    @Test(expected = NullPointerException.class)
    public void errorOutputFromException_whenExceptionIsNull_shouldThrowNullPointerException() throws Exception {
        // GIVEN-WHEN
        ErrorOutputDto.fromException(null);

        // THEN: exception
    }

    @Test
    public void errorOutputFromException_shouldReturnMessageAndCompleteStackTrace() throws Exception {
        // GIVEN
        Exception exception = createNestedExceptions();

        // WHEN
        ErrorOutputDto actualErrorOutputDto = ErrorOutputDto.fromException(exception);

        // THEN
        assertThat(actualErrorOutputDto).isNotNull();
        assertThat(actualErrorOutputDto.getErrorMessage()).isEqualTo("An exception occurred");
        assertThat(actualErrorOutputDto.getStackTrace()).isNotEmpty();
    }

    @Test
    public void errorOutputToJson_shouldReturnCorrectJson() throws IOException, URISyntaxException {
        // GIVEN
        Exception exception = createNestedExceptions();
        Serializable errorOutputObject = ErrorOutputDto.fromException(exception);

        // WHEN
        String actualJson = objectWriter.writeValueAsString(errorOutputObject);
        System.out.println("JSON output:\n" + actualJson);

        // THEN
        JsonNode actualRootNode = objectReader.readTree(actualJson);
        assertEquals("An exception occurred", actualRootNode.get("errorMessage").getTextValue());
        assertThat(actualRootNode.get("stackTrace").getTextValue()).containsSequence("Exception", "IllegalArgumentException");
    }

    private static Exception createNestedExceptions() {
        IllegalArgumentException parentException = new IllegalArgumentException("An illegal argument exception occurred");
        return new Exception("An exception occurred", parentException);
    }
}