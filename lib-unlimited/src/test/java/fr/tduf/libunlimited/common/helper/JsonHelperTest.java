package fr.tduf.libunlimited.common.helper;

import org.codehaus.jackson.JsonParseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JsonHelperTest {
    @Test
    void prettify_whenNullSON_shouldThrowException() throws IOException {
        // given-when-then
        assertThrows(NullPointerException.class,
                () -> JsonHelper.prettify(null));
    }

    @Test
    void prettify_whenInvalidJSON_shouldThrowException() throws IOException {
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
}
