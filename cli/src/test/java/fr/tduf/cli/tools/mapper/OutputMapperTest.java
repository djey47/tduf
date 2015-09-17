package fr.tduf.cli.tools.mapper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.cli.tools.dto.DatabaseIntegrityErrorDto;
import fr.tduf.cli.tools.dto.ErrorOutputDto;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class OutputMapperTest {

    private static final Class<OutputMapperTest> thisClass = OutputMapperTest.class;

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
    private final ObjectReader objectReader = new ObjectMapper().reader();

    @Before
    public void setUp() throws IOException {
        Log.set(Log.LEVEL_INFO);
    }

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
        Log.debug(thisClass.getSimpleName(), "JSON output:\n" + actualJson);

        // THEN
        JsonNode actualRootNode = objectReader.readTree(actualJson);
        assertThat(actualRootNode.get("errorMessage").getTextValue()).isEqualTo("An exception occurred");
        assertThat(actualRootNode.get("stackTrace").getTextValue()).containsSequence("Exception", "IllegalArgumentException");
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
                .addInformation(IntegrityError.ErrorInfoEnum.LOCALE, DbResourceDto.Locale.FRANCE)
                .build();

        // WHEN
        DatabaseIntegrityErrorDto actualIntegrityErrorObject = DatabaseIntegrityErrorDto.fromIntegrityError(integrityError);

        // THEN
        assertThat(actualIntegrityErrorObject.getErrorType()).isEqualTo("CONTENT_ITEMS_COUNT_MISMATCH");
        assertThat(actualIntegrityErrorObject.getInformation()).containsKey("locale");
        assertThat(actualIntegrityErrorObject.getInformation().get("locale")).isEqualTo(DbResourceDto.Locale.FRANCE);
    }

    private static Exception createNestedExceptions() {
        IllegalArgumentException parentException = new IllegalArgumentException("An illegal argument exception occurred");
        return new Exception("An exception occurred", parentException);
    }
}