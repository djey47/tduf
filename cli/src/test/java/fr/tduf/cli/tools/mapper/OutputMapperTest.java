package fr.tduf.cli.tools.mapper;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import fr.tduf.cli.tools.dto.DatabaseIntegrityErrorDto;
import fr.tduf.cli.tools.dto.ErrorOutputDto;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;

public class OutputMapperTest {

    private static final Class<OutputMapperTest> thisClass = OutputMapperTest.class;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectReader objectReader = objectMapper.reader();
    private final ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();

    @Before
    public void setUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @Test(expected = NullPointerException.class)
    public void errorOutputFromException_whenExceptionIsNull_shouldThrowNullPointerException() {
        // GIVEN-WHEN
        ErrorOutputDto.fromException(null);

        // THEN: exception
    }

    @Test
    public void errorOutputFromException_shouldReturnMessageAndCompleteStackTrace() {
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
    public void errorOutputToJson_shouldReturnCorrectJson() throws IOException {
        // GIVEN
        Exception exception = createNestedExceptions();
        Serializable errorOutputObject = ErrorOutputDto.fromException(exception);

        // WHEN
        String actualJson = objectWriter.writeValueAsString(errorOutputObject);
        Log.debug(thisClass.getSimpleName(), "JSON output:\n" + actualJson);

        // THEN
        JsonNode actualRootNode = objectReader.readTree(actualJson);
        assertThat(actualRootNode.get("errorMessage").textValue()).isEqualTo("An exception occurred");
        assertThat(actualRootNode.get("stackTrace").textValue()).containsSequence("Exception", "IllegalArgumentException");
    }

    @Test(expected = NullPointerException.class)
    public void databaseIntegrityErrorFromDomain_whenNullError_shouldThrowNullPointerException() {
        // GIEVN-WHEN
        DatabaseIntegrityErrorDto.fromIntegrityError(null);

        // THEN: exception
    }

    @Test
    public void databaseIntegrityErrorFromDomain_shouldReturnProperObject() {
        // GIVEN
        IntegrityError integrityError = IntegrityError.builder()
                .ofType(IntegrityError.ErrorTypeEnum.CONTENT_ITEMS_COUNT_MISMATCH)
                .addInformation(IntegrityError.ErrorInfoEnum.LOCALE, Locale.FRANCE)
                .build();

        // WHEN
        DatabaseIntegrityErrorDto actualIntegrityErrorObject = DatabaseIntegrityErrorDto.fromIntegrityError(integrityError);

        // THEN
        assertThat(actualIntegrityErrorObject.getErrorType()).isEqualTo("CONTENT_ITEMS_COUNT_MISMATCH");
        assertThat(actualIntegrityErrorObject.getInformation()).containsKey("locale");
        assertThat(actualIntegrityErrorObject.getInformation().get("locale")).isEqualTo(Locale.FRANCE);
    }

    private static Exception createNestedExceptions() {
        IllegalArgumentException parentException = new IllegalArgumentException("An illegal argument exception occurred");
        return new Exception("An exception occurred", parentException);
    }
}