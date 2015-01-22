package fr.tduf.libunlimited.low.files.research.common;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StructureHelperTest {

    @Test
    public void retrieveStructureFromLocation_whenClasspathResource_shouldReturnResource() throws Exception {
        // GIVEN
        String resourcePath = "/files/structures/TEST-map.json";

        // WHEN
        FileStructureDto actualStructure = StructureHelper.retrieveStructureFromLocation(resourcePath);

        // THEN
        assertThat(actualStructure).isNotNull();
    }

    @Test
    public void retrieveStructureFromLocation_whenExternalResource_shouldReturnResource() throws Exception {
        // GIVEN
        String resourcePath = "./src/test/resources/files/structures/TEST-map.json";

        // WHEN
        FileStructureDto actualStructure = StructureHelper.retrieveStructureFromLocation(resourcePath);

        // THEN
        assertThat(actualStructure).isNotNull();
    }
}