package fr.tduf.gui.database.controllers.main;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tduf.libtesting.common.helper.TestingFilesHelper;
import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseChangeHelper;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class MainStageChangeDataControllerTest {

    private static final List<DbDto> databaseObjects = DatabaseHelper.createDatabase();
    private static final BulkDatabaseMiner verifyMiner = BulkDatabaseMiner.load(databaseObjects);

    @Mock
    private BulkDatabaseMiner minerMock;

    @Mock
    private MainStageController mainStageController;

    @Mock
    private DatabaseGenHelper genHelperMock;

    @Mock
    private DatabaseChangeHelper changeHelperMock;

    @InjectMocks
    private MainStageChangeDataController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        initMocks(this);
        
        Log.set(Log.LEVEL_INFO);

        objectMapper = new ObjectMapper();

        controller.setGenHelper(genHelperMock);

        when(genHelperMock.getChangeHelper()).thenReturn(changeHelperMock);
    }

    @Test
    void exportEntriesToPatchFile_whenIllegalOutputFile_shouldReturnFalse() {

        // GIVEN
        String patchFile = "/unkdir/patch.mini.json";

        when(mainStageController.getDatabaseObjects()).thenReturn(databaseObjects);


        // WHEN
        final boolean result = controller.exportEntriesToPatchFile(CAR_PHYSICS_DATA, asList("734237852", "1202238231", "1289993715"), new ArrayList<>(), patchFile);


        // THEN
        assertThat(result).isFalse();
    }

    @Test
    void exportEntriesToPatchFile_whenReferenceList_shouldExportSelectedRefs() throws Exception {

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
    void exportEntriesToPatchFile_whenEmptyLists_shouldExportAllRefs_andAllFields() throws Exception {

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
    void exportEntriesToPatchFile_whenFieldList_shouldExportSelectedFields() throws Exception {

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
    void exportCurrentEntryAsLine_should_generate_line_with_trailing_semicolon() {
        // GIVEN
        ContentEntryDto entry = ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("25").build())
                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue("36").build())
                .build();
        when(mainStageController.getCurrentEntryIndex()).thenReturn(1);
        when(mainStageController.currentTopicProperty()).thenReturn(new SimpleObjectProperty<>(CAR_PHYSICS_DATA));
        when(mainStageController.getMiner()).thenReturn(minerMock);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(1, CAR_PHYSICS_DATA)).thenReturn(Optional.of(entry));

        // WHEN
        final String actualEntry = controller.exportCurrentEntryAsLine();

        // THEN
        assertThat(actualEntry).isEqualTo("25;36;");
    }

    @Test
    void updateResourceWithReferenceForLocale_shouldCallUpdateComponent() {
        // GIVEN-WHEN
        controller.updateResourceWithReferenceForLocale(CAR_PHYSICS_DATA, FRANCE, "0", "1");

        // THEN
        verify(changeHelperMock).updateResourceItemWithReference(CAR_PHYSICS_DATA, FRANCE, "0", "1");
    }

    @Test
    void updateResourceWithReferenceForAllLocales_withReferenceChange_shouldCallUpdateComponent() {
        // GIVEN-WHEN
        controller.updateResourceWithReferenceForAllLocales(CAR_PHYSICS_DATA, "0", "1", "V");

        // THEN
        verify(changeHelperMock).updateResourceEntryWithReference(CAR_PHYSICS_DATA, "0", "1", "V");
    }

    @Test
    void updateResourceWithReferenceForAllLocales_shouldCallUpdateComponent() {
        // GIVEN-WHEN
        controller.updateResourceWithReferenceForAllLocales(CAR_PHYSICS_DATA, "0", "V");

        // THEN
        verify(changeHelperMock, times(8)).updateResourceItemWithReference(eq(CAR_PHYSICS_DATA), any(Locale.class), eq("0"), eq("V"));
    }

    @Test
    void importPatch_whenPatchWithoutProperties_shouldApplyIt() throws IOException, ReflectiveOperationException, URISyntaxException {
        // GIVEN
        File patchFile = new File(fr.tduf.libunlimited.common.helper.FilesHelper.getFileNameFromResourcePath("/patches/tduf.mini.json"));

        when(mainStageController.getDatabaseObjects()).thenReturn(databaseObjects);


        // WHEN
        final Optional<String> actualPropertyFile = controller.importPatch(patchFile);


        // THEN
        assertThat(actualPropertyFile).isEmpty();
        assertThat(verifyMiner.getContentEntryFromTopicWithReference("606298799", CAR_PHYSICS_DATA)).isEmpty();
    }

    @Test
    void importPerformancePack_shouldApplyIt() throws URISyntaxException, ReflectiveOperationException {
        // GIVEN
        String packFileName = fr.tduf.libunlimited.common.helper.FilesHelper.getFileNameFromResourcePath("/patches/pp.tdupk");
        String affectedRef = verifyMiner.getContentEntryReferenceWithInternalIdentifier(1, CAR_PHYSICS_DATA)
                .orElse("NO REF");

        when(mainStageController.getDatabaseObjects()).thenReturn(databaseObjects);
        when(mainStageController.currentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(1));


        // WHEN
        controller.importPerformancePack(packFileName);


        // THEN
        assertThat(verifyMiner.getContentEntryFromTopicWithReference(affectedRef, CAR_PHYSICS_DATA)
                .flatMap(entry -> entry.getItemAtRank(17))
                .map(ContentItemDto::getRawValue))
                .contains("6210");
    }

    @Test
    void importLegacyPatch_shouldApplyIt() throws URISyntaxException, ReflectiveOperationException, IOException {
        // GIVEN
        String patchFile = fr.tduf.libunlimited.common.helper.FilesHelper.getFileNameFromResourcePath("/patches/legacy.pch");

        when(mainStageController.getDatabaseObjects()).thenReturn(databaseObjects);


        // WHEN
        controller.importLegacyPatch(patchFile);


        // THEN
        assertThat(verifyMiner.getContentEntryFromTopicWithReference("606298799", CAR_PHYSICS_DATA)).isEmpty();
    }

    @Test
    void removeResourceWithReference_shouldRemoveEntry() {
        // GIVEN
        String resourceReference = "3005487";

        // WHEN
        controller.removeResourceWithReference(CAR_PHYSICS_DATA, resourceReference);

        // THEN
        verify(changeHelperMock).removeResourceEntryWithReference(CAR_PHYSICS_DATA, resourceReference);
    }

    private static String createTempDirectory() throws IOException {
        return TestingFilesHelper.createTempDirectoryForDatabaseEditor();
    }
}
