package fr.tduf.libunlimited.high.files.db.interop.tdupe;

import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TdupePerformancePackConverterTest {

    private static Class<TdupePerformancePackConverterTest> thisClass = TdupePerformancePackConverterTest.class;

    @Test
    void tdupkToJson_whenNullLine_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> TdupePerformancePackConverter.tdupkToJson(null, Optional.empty(), DbDto.builder().build()));
    }

    @Test
    void tdupkToJson_whenNullTopicObject_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> TdupePerformancePackConverter.tdupkToJson("", Optional.empty(), null));
    }

    @Test
    void tdupkToJson_withoutReference_andEntryNotFound_shouldMakePatchObject() throws IOException, URISyntaxException {
        // GIVEN
        String carPhysicsDataLine = readLineFromPack("/db/patch/tdupe/F150-newRef.tdupk");
        DbDto carPhysicsTopicObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);

        // WHEN
        DbPatchDto actualPatchObject = TdupePerformancePackConverter.tdupkToJson(carPhysicsDataLine, Optional.empty(), carPhysicsTopicObject);

        // THEN
        DbPatchDto expectedPatchObject = readPatchObjectFromResource("/db/patch/updateContents-f150PerformancePack-newRef.mini.json");
        assertThat(actualPatchObject).isEqualTo(expectedPatchObject);
    }

    @Test
    void tdupkToJson_withoutReference_andEntryFound_shouldMakePatchObject() throws IOException, URISyntaxException {
        // GIVEN
        String carPhysicsDataLine = readLineFromPack("/db/patch/tdupe/F150.tdupk");
        DbDto carPhysicsTopicObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);

        // WHEN
        DbPatchDto actualPatchObject = TdupePerformancePackConverter.tdupkToJson(carPhysicsDataLine, Optional.empty(), carPhysicsTopicObject);

        // THEN
        DbPatchDto expectedPatchObject = readPatchObjectFromResource("/db/patch/updateContents-f150PerformancePack-existingRef.mini.json");
        assertThat(actualPatchObject).isEqualTo(expectedPatchObject);
    }

    @Test
    void tdupkToJson_withReference_andEntryNotFound_shouldMakePatchObject() throws IOException, URISyntaxException {
        // GIVEN
        String carPhysicsDataLine = readLineFromPack("/db/patch/tdupe/F150.tdupk");
        DbDto carPhysicsTopicObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);

        // WHEN
        DbPatchDto actualPatchObject = TdupePerformancePackConverter.tdupkToJson(carPhysicsDataLine, Optional.of("601945475"), carPhysicsTopicObject);

        // THEN
        DbPatchDto expectedPatchObject = readPatchObjectFromResource("/db/patch/updateContents-f150PerformancePack-targetNewRef.mini.json");
        assertThat(actualPatchObject).isEqualTo(expectedPatchObject);
    }

    @Test
    void tdupkToJson_withReference_andEntryFound_shouldMakePatchObject() throws IOException, URISyntaxException {
        // GIVEN
        String carPhysicsDataLine = readLineFromPack("/db/patch/tdupe/F150.tdupk");
        DbDto carPhysicsTopicObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);

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

    private static DbPatchDto readPatchObjectFromResource(String resource) throws URISyntaxException, IOException {
        return FilesHelper.readObjectFromJsonResourceFile(DbPatchDto.class, resource);
    }
}
