package fr.tduf.libunlimited.high.files.db.interop.tdupe;

import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TdupePerformancePackConverterTest {

    private static Class<TdupePerformancePackConverterTest> thisClass = TdupePerformancePackConverterTest.class;

    @Test(expected = NullPointerException.class)
    public void tdupkToJson_whenNullLine_shouldThrowException() {
        // GIVEN-WHEN
        TdupePerformancePackConverter.tdupkToJson(null, Optional.<String>empty(), DbDto.builder().build());

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void tdupkToJson_whenNullTopicObject_shouldThrowException() {
        // GIVEN-WHEN
        TdupePerformancePackConverter.tdupkToJson("", Optional.<String>empty(), null);

        // THEN: NPE
    }

    @Test
    public void tdupkToJson_withoutReference_andEntryNotFound_shouldMakePatchObject() throws IOException, URISyntaxException {
        // GIVEN
        String carPhysicsDataLine = readLineFromPack("/db/patch/tdupe/F150-newRef.tdupk");
        DbDto carPhysicsTopicObject = loadCarPhysicsTopicFromResources();

        // WHEN
        DbPatchDto actualPatchObject = TdupePerformancePackConverter.tdupkToJson(carPhysicsDataLine, Optional.<String>empty(), carPhysicsTopicObject);

        // THEN
        DbPatchDto expectedPatchObject = readPatchObjectFromResource("/db/patch/updateContents-f150PerformancePack-newRef.mini.json");
        assertThat(actualPatchObject).isEqualTo(expectedPatchObject);
    }

    @Test
    public void tdupkToJson_withoutReference_andEntryFound_shouldMakePatchObject() throws IOException, URISyntaxException {
        // GIVEN
        String carPhysicsDataLine = readLineFromPack("/db/patch/tdupe/F150.tdupk");
        DbDto carPhysicsTopicObject = loadCarPhysicsTopicFromResources();

        // WHEN
        DbPatchDto actualPatchObject = TdupePerformancePackConverter.tdupkToJson(carPhysicsDataLine, Optional.<String>empty(), carPhysicsTopicObject);

        // THEN
        DbPatchDto expectedPatchObject = readPatchObjectFromResource("/db/patch/updateContents-f150PerformancePack-existingRef.mini.json");
        assertThat(actualPatchObject).isEqualTo(expectedPatchObject);
    }

    @Test
    public void tdupkToJson_withReference_andEntryNotFound_shouldMakePatchObject() throws IOException, URISyntaxException {
        // GIVEN
        String carPhysicsDataLine = readLineFromPack("/db/patch/tdupe/F150.tdupk");
        DbDto carPhysicsTopicObject = loadCarPhysicsTopicFromResources();

        // WHEN
        DbPatchDto actualPatchObject = TdupePerformancePackConverter.tdupkToJson(carPhysicsDataLine, Optional.of("601945475"), carPhysicsTopicObject);

        // THEN
        DbPatchDto expectedPatchObject = readPatchObjectFromResource("/db/patch/updateContents-f150PerformancePack-targetNewRef.mini.json");
        assertThat(actualPatchObject).isEqualTo(expectedPatchObject);
    }

    @Test
    public void tdupkToJson_withReference_andEntryFound_shouldMakePatchObject() throws IOException, URISyntaxException {
        // GIVEN
        String carPhysicsDataLine = readLineFromPack("/db/patch/tdupe/F150.tdupk");
        DbDto carPhysicsTopicObject = loadCarPhysicsTopicFromResources();

        // WHEN
        DbPatchDto actualPatchObject = TdupePerformancePackConverter.tdupkToJson(carPhysicsDataLine, Optional.of("601945474"), carPhysicsTopicObject);

        // THEN
        DbPatchDto expectedPatchObject = readPatchObjectFromResource("/db/patch/updateContents-f150PerformancePack-targetExistingRef.mini.json");
        assertThat(actualPatchObject).isEqualTo(expectedPatchObject);
    }

    private static String readLineFromPack(String packFile) throws IOException, URISyntaxException {
        URI packFileURI = thisClass.getResource(packFile).toURI();
        List<String> lines = Files.readAllLines(Paths.get(packFileURI));
        return lines.get(0);
    }

    private static DbDto loadCarPhysicsTopicFromResources() throws URISyntaxException, IOException {
        URI dataFileURI = thisClass.getResource("/db/json/TDU_CarPhysicsData.data.json").toURI();
        URI resourceFileURI = thisClass.getResource("/db/json/TDU_CarPhysicsData.resources.json").toURI();
        URI structureFileURI = thisClass.getResource("/db/json/TDU_CarPhysicsData.structure.json").toURI();
        return DbDto.builder()
                .withData(new ObjectMapper().readValue(new File(dataFileURI), DbDataDto.class))
                .withStructure(new ObjectMapper().readValue(new File(structureFileURI), DbStructureDto.class))
                .withResource(new ObjectMapper().readValue(new File(resourceFileURI), DbResourceDto.class))
                .build();
    }

    private static DbPatchDto readPatchObjectFromResource(String resource) throws URISyntaxException, IOException {
        URI resourceURI = thisClass.getResource(resource).toURI();
        return new ObjectMapper().readValue(new File(resourceURI), DbPatchDto.class);
    }
}