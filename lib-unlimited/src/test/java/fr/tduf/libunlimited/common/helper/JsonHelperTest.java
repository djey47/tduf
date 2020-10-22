package fr.tduf.libunlimited.common.helper;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonHelperTest {
    @Test
    void prettify_whenNullSON_shouldThrowException() {
        // given-when-then
        assertThrows(NullPointerException.class,
                () -> JsonHelper.prettify(null));
    }

    @Test
    void prettify_whenInvalidJSON_shouldThrowException() {
        // given
        String source = "{\"resourceType\": \"ValueSet\",\"id\": \"example-inline\",\"meta\": {\"profile\": [\"http://hl7.org/fhir/StructureDefinition/valueset-shareable-definition\"]";

        // when-then
        assertThrows(JsonParseException.class,
                () -> JsonHelper.prettify(source));
    }

    @Test
    void prettify_whenValidJSON_shouldReturnFormattedOne() throws IOException {
        // given
        String source = "{\"resourceType\": \"ValueSet\",\"id\": \"example-inline\",\"meta\": {\"profile\": [\"http://hl7.org/fhir/StructureDefinition/valueset-shareable-definition\"]}}";

        // when
        String actual = JsonHelper.prettify(source);

        // then
        String expected = "{\n" +
                "  \"resourceType\" : \"ValueSet\",\n" +
                "  \"id\" : \"example-inline\",\n" +
                "  \"meta\" : {\n" +
                "    \"profile\" : [ \"http://hl7.org/fhir/StructureDefinition/valueset-shareable-definition\" ]\n" +
                "  }\n" +
                "}";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void isValidJSON_whenValid_shouldReturnTrue() {
        // given
        String trueJson = "{ \"array\": [], \"object\": {}, \"string\": \"a\", \"number\": 0 }";

        // when-then
        assertThat(JsonHelper.isValid(trueJson)).isTrue();
    }

    @Test
    void isValidJSON_whenMixedContents_shouldReturnFalse() {
        // given
        String trueJson = "WARNING!\n{ \"array\": [], \"object\": {}, \"string\": \"a\", \"number\": 0 }";

        // when-then
        assertThat(JsonHelper.isValid(trueJson)).isFalse();
    }

    @Test
    void isValidJSON_whenDuplicateKey_shouldReturnFalse() {
        // given
        String trueJson = "{ \"array\": [], \"object\": {}, \"string\": \"a\", \"number\": 0, \"number\": 1 }";

        // when-then
        assertThat(JsonHelper.isValid(trueJson)).isFalse();
    }
}
