package fr.tduf.gui.database.controllers;

import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.assertj.core.api.StrictAssertions;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MainStageChangeDataControllerTest {

    private static final Class<MainStageChangeDataControllerTest> thisClass = MainStageChangeDataControllerTest.class;

    @Mock
    MainStageController mainStageController;

    @InjectMocks
    MainStageChangeDataController controller;

    private ObjectMapper objectMapper;

    private List<DbDto> databaseObjects;

    @Before
    public void setUp() throws URISyntaxException {
//        Log.set(Log.LEVEL_TRACE);
        objectMapper = new ObjectMapper();

        final String jsonFilePath = thisClass.getResource("/database/TDU_CarPhysicsData.json").toURI().getPath();
        databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(Paths.get(jsonFilePath).getParent().toString());
    }

    @Test
    public void exportEntriesToPatchFile_whenIllegalOutputFile_shouldReturnFalse() throws Exception {

        // GIVEN
        String patchFile = "/unkdir/patch.mini.json";

        when(mainStageController.getDatabaseObjects()).thenReturn(databaseObjects);


        // WHEN
        final boolean result = controller.exportEntriesToPatchFile(CAR_PHYSICS_DATA, asList("734237852", "1202238231", "1289993715"), new ArrayList<>(), patchFile);


        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void exportEntriesToPatchFile_whenReferenceList_shouldExportSelectedRefs() throws Exception {

        // GIVEN
        final Path patchPath = Paths.get(createTempDirectory(), "export-3.mini.json");
        String patchFile = patchPath.toString();

        when(mainStageController.getDatabaseObjects()).thenReturn(databaseObjects);


        // WHEN
        final boolean result = controller.exportEntriesToPatchFile(CAR_PHYSICS_DATA, asList("734237852", "1202238231", "1289993715"), new ArrayList<>(), patchFile);


        // THEN
        assertThat(result).isTrue();
        final DbPatchDto patchObject = objectMapper.readValue(patchPath.toFile(), DbPatchDto.class);
        assertThat(patchObject.getChanges()).hasSize(128);
    }

    @Test
    public void exportEntriesToPatchFile_whenEmptyLists_shouldExportAllRefs_andAllFields() throws Exception {

        // GIVEN
        final Path patchPath = Paths.get(createTempDirectory(), "export-all.mini.json");
        String patchFile = patchPath.toString();

        when(mainStageController.getDatabaseObjects()).thenReturn(databaseObjects);


        // WHEN
        final boolean result = controller.exportEntriesToPatchFile(BRANDS, new ArrayList<>(), new ArrayList<>(), patchFile);


        // THEN
        StrictAssertions.assertThat(result).isTrue();
        final DbPatchDto patchObject = objectMapper.readValue(patchPath.toFile(), DbPatchDto.class);
        assertThat(patchObject.getChanges()).hasSize(172);

        patchObject.getChanges().stream()

                .filter((changeObject) -> changeObject.getType() == DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE)

                .forEach((updateObject) -> assertThat(updateObject.isPartialChange()).isFalse());
    }

    @Test
    public void exportEntriesToPatchFile_whenFieldList_shouldExportSelectedFields() throws Exception {

        // GIVEN
        final Path patchPath = Paths.get(createTempDirectory(), "export-partial-3fields.mini.json");
        String patchFile = patchPath.toString();

        when(mainStageController.getDatabaseObjects()).thenReturn(databaseObjects);


        // WHEN
        final boolean result = controller.exportEntriesToPatchFile(BRANDS, new ArrayList<>(), asList("1","2","3"), patchFile);


        // THEN
        assertThat(result).isTrue();
        final DbPatchDto patchObject = objectMapper.readValue(patchPath.toFile(), DbPatchDto.class);

        patchObject.getChanges().stream()

                .filter((changeObject) -> changeObject.getType() == DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE)

                .forEach((updateObject) -> assertThat(updateObject.getPartialValues()).hasSize(3));
    }

    private static String createTempDirectory() throws IOException {
        return Files.createTempDirectory("libUnlimited-patchGeneratorGui").toString();
    }
}
