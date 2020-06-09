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
import javafx.beans.property.*;
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

import static fr.tduf.libunlimited.common.game.domain.Locale.CHINA;
import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
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

    @Mock
    private MainStageViewDataController viewDataControllerMock;

    @InjectMocks
    private MainStageChangeDataController controller;

    private ObjectMapper objectMapper;

    private BooleanProperty modifiedProperty;

    @BeforeEach
    void setUp() {
        initMocks(this);
        
        Log.set(Log.LEVEL_INFO);

        objectMapper = new ObjectMapper();

        controller.setGenHelper(genHelperMock);
        when(genHelperMock.getChangeHelper()).thenReturn(changeHelperMock);

        Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>(0);
        Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(CAR_PHYSICS_DATA);
        modifiedProperty = new SimpleBooleanProperty(false);
        when(mainStageController.modifiedProperty()).thenReturn(modifiedProperty);
        when(mainStageController.currentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        when(mainStageController.currentTopicProperty()).thenReturn(currentTopicProperty);
        when(mainStageController.getViewData()).thenReturn(viewDataControllerMock);
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
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(1, CAR_PHYSICS_DATA)).thenReturn(of(entry));

        // WHEN
        final String actualEntry = controller.exportCurrentEntryAsLine();

        // THEN
        assertThat(actualEntry).isEqualTo("25;36;");
    }

    @Test
    void updateResourceWithReferenceForLocale_shouldCallUpdateComponent_andUpdateModifiedFlag() {
        // GIVEN-WHEN
        controller.updateResourceWithReferenceForLocale(CAR_PHYSICS_DATA, FRANCE, "0", "1");

        // THEN
        verify(changeHelperMock).updateResourceItemWithReference(CAR_PHYSICS_DATA, FRANCE, "0", "1");
        assertThat(modifiedProperty.getValue()).isTrue();
    }

    @Test
    void updateResourceWithReferenceForAllLocales_withReferenceChange_shouldCallUpdateComponent_andUpdateModifiedFlag() {
        // GIVEN-WHEN
        controller.updateResourceWithReferenceForAllLocales(CAR_PHYSICS_DATA, "0", "1", "V");

        // THEN
        verify(changeHelperMock).updateResourceEntryWithReference(CAR_PHYSICS_DATA, "0", "1", "V");
        assertThat(modifiedProperty.getValue()).isTrue();
    }

    @Test
    void updateResourceWithReferenceForAllLocales_shouldCallUpdateComponent_andUpdateModifiedFlag() {
        // GIVEN-WHEN
        controller.updateResourceWithReferenceForAllLocales(CAR_PHYSICS_DATA, "0", "V");

        // THEN
        verify(changeHelperMock, times(8)).updateResourceItemWithReference(eq(CAR_PHYSICS_DATA), any(Locale.class), eq("0"), eq("V"));
        assertThat(modifiedProperty.getValue()).isTrue();
    }

    @Test
    void importPatch_whenPatchWithoutProperties_shouldApplyIt_andUpdateModifiedFlag() throws IOException, ReflectiveOperationException, URISyntaxException {
        // GIVEN
        File patchFile = new File(fr.tduf.libunlimited.common.helper.FilesHelper.getFileNameFromResourcePath("/patches/tduf.mini.json"));

        when(mainStageController.getDatabaseObjects()).thenReturn(databaseObjects);


        // WHEN
        final Optional<String> actualPropertyFile = controller.importPatch(patchFile);


        // THEN
        assertThat(actualPropertyFile).isEmpty();
        assertThat(verifyMiner.getContentEntryFromTopicWithReference("606298799", CAR_PHYSICS_DATA)).isEmpty();
        assertThat(controller.modifiedProperty().getValue()).isTrue();
    }

    @Test
    void importPerformancePack_shouldApplyIt_andUpdateModifiedFlag() throws URISyntaxException, ReflectiveOperationException {
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
        assertThat(controller.modifiedProperty().getValue()).isTrue();
    }

    @Test
    void importLegacyPatch_shouldApplyIt_andUpdateModifiedFlag() throws URISyntaxException, ReflectiveOperationException, IOException {
        // GIVEN
        String patchFile = fr.tduf.libunlimited.common.helper.FilesHelper.getFileNameFromResourcePath("/patches/legacy.pch");

        when(mainStageController.getDatabaseObjects()).thenReturn(databaseObjects);


        // WHEN
        controller.importLegacyPatch(patchFile);


        // THEN
        assertThat(verifyMiner.getContentEntryFromTopicWithReference("606298799", CAR_PHYSICS_DATA)).isEmpty();
        assertThat(controller.modifiedProperty().getValue()).isTrue();
    }

    @Test
    void removeResourceWithReference_shouldRemoveEntry_andUpdateModifiedFlag() {
        // GIVEN
        String resourceReference = "3005487";

        // WHEN
        controller.removeResourceWithReference(CAR_PHYSICS_DATA, resourceReference);

        // THEN
        verify(changeHelperMock).removeResourceEntryWithReference(CAR_PHYSICS_DATA, resourceReference);
        assertThat(modifiedProperty.getValue()).isTrue();
    }

    @Test
    void updateContentItem_shouldInvokeViewDataController_andUpdateModifiedFlag() {
        // given
        ContentItemDto contentItemDto = ContentItemDto.builder().ofFieldRank(1).build();
        when(changeHelperMock.updateItemRawValueAtIndexAndFieldRank(any(), anyInt(), anyInt(), anyString()))
                .thenReturn(of(contentItemDto));

        // when
        controller.updateContentItem(CAR_PHYSICS_DATA, 1, "RAW");

        // then
        verify(viewDataControllerMock).updateItemProperties(eq(contentItemDto));
        verify(viewDataControllerMock).updateBrowsableEntryLabel(0);
        verify(viewDataControllerMock).updateCurrentEntryLabelProperty();
        assertThat(controller.modifiedProperty().getValue()).isTrue();
    }

    @Test
    void updateContentItem_whenNonExistingItem_shouldNotInvokeViewDataController_norUpdateModifiedFlag() {
        // given
        when(changeHelperMock.updateItemRawValueAtIndexAndFieldRank(any(), anyInt(), anyInt(), anyString()))
                .thenReturn(empty());

        // when
        controller.updateContentItem(CAR_PHYSICS_DATA, 1, "RAW");

        // then
        verifyNoInteractions(viewDataControllerMock);
        assertThat(controller.modifiedProperty().getValue()).isFalse();
    }

    @Test
    void removeEntryWithIdentifier_shouldInvokeChangeHelper_andUpdateModifiedFlag() {
        // given
        controller.removeEntryWithIdentifier(0, CAR_PHYSICS_DATA);

        // then
        verify(changeHelperMock).removeEntryWithIdentifier(0, CAR_PHYSICS_DATA);
        assertThat(controller.modifiedProperty().getValue()).isTrue();

    }

    @Test
    void addEntryForCurrentTopic_shouldInvokeChangeHelper_andUpdateModifiedFlag() {
        // given
        when(changeHelperMock.addContentsEntryWithDefaultItems(CAR_PHYSICS_DATA))
                .thenReturn(ContentEntryDto.builder().build());

        // when
        int actualId = controller.addEntryForCurrentTopic();

        // then
        assertThat(actualId).isEqualTo(-1);
        assertThat(controller.modifiedProperty().getValue()).isTrue();
    }

    @Test
    void duplicateCurrentEntry_shouldInvokeChangeHelper_andUpdateModifiedFlag() {
        // given
        when(changeHelperMock.duplicateEntryWithIdentifier(0, CAR_PHYSICS_DATA))
                .thenReturn(ContentEntryDto.builder().build());

        // when
        int actualId = controller.duplicateCurrentEntry();

        // then
        assertThat(actualId).isEqualTo(-1);
        assertThat(controller.modifiedProperty().getValue()).isTrue();
    }

    @Test
    void addResourceWithReference_shouldInvokeChangeHelper_andUpdateModifiedFlag() {
        // given-when
        controller.addResourceWithReference(CAR_PHYSICS_DATA, CHINA, "REF", "VAL");

        // then
        verify(changeHelperMock).addResourceValueWithReference(CAR_PHYSICS_DATA, CHINA, "REF", "VAL" );
        assertThat(controller.modifiedProperty().getValue()).isTrue();
    }

    @Test
    void moveEntryWithIdentifier_shouldInvokeChangeHelper_andUpdateModifiedFlag() {
        // given-when
        controller.moveEntryWithIdentifier(1, 0, CAR_PHYSICS_DATA);

        // then
        verify(changeHelperMock).moveEntryWithIdentifier(1, 0, CAR_PHYSICS_DATA);
        assertThat(controller.modifiedProperty().getValue()).isTrue();
    }

    @Test
    void addLinkedEntry_shouldInvokeChangeHelper_andUpdateModifiedFlag() {
        // given
        ContentEntryDto contentEntryDto = ContentEntryDto.builder().build();
        when(changeHelperMock.addContentsEntryWithDefaultItems(BRANDS)).thenReturn(contentEntryDto);

        // when
        controller.addLinkedEntry("SOURCE_REF", "TARGET_REF", BRANDS);

        // then
        verify(changeHelperMock).updateAssociationEntryWithSourceAndTargetReferences(eq(contentEntryDto), eq("SOURCE_REF"), eq("TARGET_REF"));
        assertThat(controller.modifiedProperty().getValue()).isTrue();
    }

    private static String createTempDirectory() throws IOException {
        return TestingFilesHelper.createTempDirectoryForDatabaseEditor();
    }
}
