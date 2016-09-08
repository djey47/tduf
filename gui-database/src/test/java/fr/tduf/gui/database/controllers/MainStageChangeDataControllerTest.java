package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import javafx.beans.property.SimpleObjectProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MainStageChangeDataControllerTest {

    private static final List<DbDto> databaseObjects = DatabaseHelper.createDatabaseForReadOnly();

    @Mock
    private BulkDatabaseMiner minerMock;

    @Mock
    private MainStageController mainStageController;

    @InjectMocks
    private MainStageChangeDataController controller;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws URISyntaxException {
        Log.set(Log.LEVEL_INFO);

        objectMapper = new ObjectMapper();
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
        assertThat(patchObject.getChanges()).hasSize(132); //7 BRANDS + 5 CAR_COLORS + 105 CAR_PHYSICS_DATA + 1 CAR_RIMS + 3 INTERIOR + 11 RIMS
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
        assertThat(result).isTrue();
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

    @Test
    public void exportCurrentEntryAsLine_should_generate_line_with_trailing_semicolon() {
        // GIVEN
        ContentEntryDto entry = ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("25").build())
                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue("36").build())
                .build();
        when(mainStageController.getCurrentEntryIndex()).thenReturn(1L);
        when(mainStageController.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(CAR_PHYSICS_DATA));
        when(mainStageController.getMiner()).thenReturn(minerMock);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(1L, CAR_PHYSICS_DATA)).thenReturn(Optional.of(entry));

        // WHEN
        final String actualEntry = controller.exportCurrentEntryAsLine();

        // THEN
        assertThat(actualEntry).isEqualTo("25;36;");
    }

    private static String createTempDirectory() throws IOException {
        return FilesHelper.createTempDirectoryForDatabaseEditor();
    }
}
