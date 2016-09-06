package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.OptionalLong;

import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.*;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class MainStageViewDataControllerTest {
    private static final DbDto.Topic TOPIC1 = CAR_PHYSICS_DATA;
    private static final DbDto.Topic TOPIC2 = BRANDS;
    private static final DbDto.Topic TOPIC3 = CAR_RIMS;
    private static final DbDto.Topic TOPIC4 = RIMS;
    private static final Locale LOCALE = UNITED_STATES;
    private static final String TOPIC_REFERENCE = "TOPICREF";
    private static final String TOPIC_REMOTE_REFERENCE = "TOPICREMOTEREF";
    private static final String TEST_PROFILE_NAME = "Test profile";
    private static final String TEST_REMOTE_PROFILE_NAME = "Test remote profile";
    private static final String TEST_REMOTE_ASSO_PROFILE_NAME = "Test association remote profile";

    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    @Mock
    private MainStageController mainStageControllerMock;

    @Mock
    private ApplicationConfiguration applicationConfigurationMock;

    @Mock
    private BulkDatabaseMiner minerMock;

    @InjectMocks
    private MainStageViewDataController controller;

    @Captor
    private ArgumentCaptor<EditorLayoutDto> editorLayoutCaptor;

    private final EditorLayoutDto layoutObject = createLayoutObject();

    private final DbDto topicObject = createTopicObject();

    private final StringProperty currentEntryLabelProperty = new SimpleStringProperty("");

    private ChoiceBox<String> profilesChoiceBox;

    @Before
    public void setUp() {
        DatabaseEditor.getCommandLineParameters().clear();

        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);

        when(mainStageControllerMock.getMiner()).thenReturn(minerMock);
        when(mainStageControllerMock.getLayoutObject()).thenReturn(layoutObject);
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(topicObject);

        when(mainStageControllerMock.getCurrentLocaleProperty()).thenReturn(new SimpleObjectProperty<>(LOCALE));
        when(mainStageControllerMock.getCurrentEntryLabelProperty()).thenReturn(currentEntryLabelProperty);

        profilesChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(TEST_PROFILE_NAME, TEST_REMOTE_PROFILE_NAME, TEST_REMOTE_ASSO_PROFILE_NAME));
        profilesChoiceBox.valueProperty().setValue(TEST_PROFILE_NAME);
        when(mainStageControllerMock.getProfilesChoiceBox()).thenReturn(profilesChoiceBox);
        when(mainStageControllerMock.getTabPane()).thenReturn(new TabPane());
        when(mainStageControllerMock.getDatabaseLocationTextField()).thenReturn(new TextField("location"));


        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(topicObject));
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenNoCommandLineParameter_andNoConfiguration_shouldReturnEmpty() throws Exception {
        // GIVEN
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(Optional.empty());

        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();

        // THEN
        assertThat(actualDirectory).isEmpty();
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenWrongCommandLineParameter_andNoConfiguration_shouldReturnEmpty() throws Exception {
        // GIVEN
        DatabaseEditor.getCommandLineParameters().add("-p");
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(Optional.empty());


        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();


        // THEN
        assertThat(actualDirectory).isEmpty();
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenRightCommandLineParameter_shouldReturnLocation() throws Exception {
        // GIVEN
        DatabaseEditor.getCommandLineParameters().add("/tdu/euro/bnk/database");


        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();


        // THEN
        assertThat(actualDirectory).contains("/tdu/euro/bnk/database");

        verifyZeroInteractions(mainStageControllerMock, applicationConfigurationMock);
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenNoCommandLineParameter_andConfiguration_shouldReturnSavedLocation() throws Exception {
        // GIVEN
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(of(Paths.get("/tdu/euro/bnk/database")));

        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();

        // THEN
        assertThat(actualDirectory).contains("/tdu/euro/bnk/database");
    }

    @Test
    public void loadAndFillProfiles_shouldSetInMainController() throws IOException {
        // GIVEN-WHEN
        controller.loadAndFillProfiles();

        // THEN
        verify(mainStageControllerMock).setLayoutObject(editorLayoutCaptor.capture());
        final EditorLayoutDto actualLayout = editorLayoutCaptor.getValue();
        assertThat(actualLayout.getProfiles()).isNotEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyProfile_whenProfileDoesNotExist_shouldThrowException() {
        // GIVEN-WHEN
        controller.applyProfile("myProfile");

        // THEN: IAE
    }

    @Test
    public void applyProfile_whenProfileExists_shouldSwitchProperties() {
        // GIVEN
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectWithoutStructureFields());
        Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>();
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>());
        final EditorLayoutDto.EditorProfileDto profileObject = layoutObject.getProfiles().get(0);
        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(profileObject);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0L, TOPIC2)).thenReturn(empty());

        // WHEN
        controller.applyProfile(TEST_PROFILE_NAME);

        // THEN
        assertThat(currentTopicProperty.getValue()).isEqualTo(TOPIC2);
        verify(mainStageControllerMock).setCurrentProfileObject(profileObject);
        verify(mainStageControllerMock).setCurrentTopicObject(topicObject);
    }

    @Test
    public void applySelectedLocale_shouldUpdateConfiguration() throws IOException {
        // GIVEN
        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(layoutObject.getProfiles().get(0));
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0L));
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC1));
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(anyLong(), any(DbDto.Topic.class))).thenReturn(empty());

        // WHEN
        controller.applySelectedLocale();

        // THEN
        verify(applicationConfigurationMock).setEditorLocale(LOCALE);
        verify(applicationConfigurationMock).store();
    }

    @Test
    public void updateDisplayWithLoadedObjects_shouldUpdateConfiguration() throws IOException {
        // GIVEN
        when(mainStageControllerMock.getDatabaseObjects()).thenReturn(singletonList(createTopicObject()));
        Deque<EditorLocation> navigationHistory = new ArrayDeque<>();
        navigationHistory.add(new EditorLocation(1, "profile", 0));
        when(mainStageControllerMock.getNavigationHistory()).thenReturn(navigationHistory);


        // WHEN
        controller.updateDisplayWithLoadedObjects();


        // THEN
        assertThat(navigationHistory).isEmpty();
        assertThat(profilesChoiceBox.getSelectionModel().isEmpty()).isFalse();

        verify(applicationConfigurationMock).setDatabasePath("location");
        verify(applicationConfigurationMock).store();
    }

    @Test
    public void updateEntriesAndSwitchTo_whenNoEntry_shouldSetEmptyList() {
        // GIVEN
        final EditorLayoutDto.EditorProfileDto profileObject = layoutObject.getProfiles().get(0);
        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(profileObject);

        controller.getBrowsableEntries().add(new ContentEntryDataItem());


        // WHEN
        controller.updateEntriesAndSwitchTo(0);


        // THEN
        assertThat(controller.getBrowsableEntries()).isEmpty();
    }

    @Test
    public void updateEntriesAndSwitchTo_whenEntries_shouldPopulateEntryList() {
        // GIVEN
        final EditorLayoutDto.EditorProfileDto profileObject = layoutObject.getProfiles().get(0);
        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(profileObject);

        controller.getBrowsableEntries().add(new ContentEntryDataItem());

        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(createTopicObjectWithDataEntryAndRef("entryRef")));
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC2)).thenReturn(empty());


        // WHEN
        controller.updateEntriesAndSwitchTo(0);


        // THEN
        assertThat(controller.getBrowsableEntries()).hasSize(1);
        final ContentEntryDataItem actualEntry = controller.getBrowsableEntries().get(0);
        assertThat(actualEntry.referenceProperty().get()).isEqualTo("0");
        assertThat(actualEntry.internalEntryIdProperty().get()).isEqualTo(0L);
        assertThat(actualEntry.valueProperty().get()).isEqualTo("<?>");
    }

    @Test
    public void refreshAll_shouldResetProperties() {
        // GIVEN
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectWithoutStructureFields());
        final Property<Long> currentEntryIndexProperty = new SimpleObjectProperty<>();
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        final EditorLayoutDto.EditorProfileDto profileObject = layoutObject.getProfiles().get(2);
        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(profileObject);
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC2);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0L, TOPIC2)).thenReturn(empty());

        // WHEN
        controller.refreshAll();

        // THEN
        assertThat(currentEntryIndexProperty.getValue()).isEqualTo(0L);
        assertThat(controller.getResolvedValuesByFieldRank()).isEmpty();
        assertThat(controller.getResourcesByTopicLink()).isEmpty();
    }

    @Test
    public void updateAllPropertiesWithItemValues_whenEntryNotFound_shouldNotCrash() {
        // GIVEN
        final EditorLayoutDto.EditorProfileDto profileObject = layoutObject.getProfiles().get(0);
        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(profileObject);
        final Property<Long> currentEntryIndexProperty = new SimpleObjectProperty<>(0L);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0L, TOPIC1)).thenReturn(empty());

        // WHEN-THEN
        controller.updateAllPropertiesWithItemValues();
    }

    @Test
    public void updateAllPropertiesWithItemValues_whenClassicFieldType_andNoLink_shouldUpdateCurrentEntryLabel () {
        // GIVEN
        ContentItemDto item = createContentItem();
        ContentEntryDto contentEntry = ContentEntryDto.builder()
                .addItem(item)
                .build();

        controller.getRawValuesByFieldRank().put(1, new SimpleStringProperty("VAL1"));

        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(layoutObject.getProfiles().get(0));
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0L));
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC1));

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0L, TOPIC1)).thenReturn(of(contentEntry));


        // WHEN
        controller.updateAllPropertiesWithItemValues();

        // THEN
        assertThat(currentEntryLabelProperty.get()).isEqualTo("<?>");
    }

    @Test
    public void updateCurrentEntryLabelProperty_whenNoFieldRank_shouldReturnDefaultLabel() {
        // GIVEN
        final EditorLayoutDto.EditorProfileDto profileObject = layoutObject.getProfiles().get(0);
        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(profileObject);
        final Property<Long> currentEntryIndexProperty = new SimpleObjectProperty<>(0L);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getLocalizedResourceValueFromContentEntry(0, 1, TOPIC1, LOCALE)).thenReturn(of("label"));

        // WHEN
        controller.updateCurrentEntryLabelProperty();

        // THEN
        assertThat(currentEntryLabelProperty.getValue()).isEqualTo("<?>");
    }

    @Test
    public void updateCurrentEntryLabelProperty_whenSingleFieldRank_shouldRetrieveLabel() {
        // GIVEN
        final EditorLayoutDto.EditorProfileDto profileObject = layoutObject.getProfiles().get(0);
        profileObject.addEntryLabelFieldRank(1);
        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(profileObject);
        final Property<Long> currentEntryIndexProperty = new SimpleObjectProperty<>(0L);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC2);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getLocalizedResourceValueFromContentEntry(0, 1, TOPIC2, LOCALE)).thenReturn(of("label"));

        // WHEN
        controller.updateCurrentEntryLabelProperty();

        // THEN
        assertThat(currentEntryLabelProperty.getValue()).isEqualTo("label");
    }

    @Test
    public void updateItemProperties_withoutRawValueSet() {
        // GIVEN
        ContentItemDto itemObject = createContentItem();

        // WHEN-THEN
        controller.updateItemProperties(itemObject);
    }

    @Test
    public void updateItemProperties_withRawValueSet_andNoResolvedValueInIndex_shouldOnlyUpdateProperty() {
        // GIVEN
        ContentItemDto itemObject = createContentItem();
        SimpleStringProperty rawValueProperty = new SimpleStringProperty("old rawValue");
        controller.getRawValuesByFieldRank().put(1, rawValueProperty);

        // WHEN
        controller.updateItemProperties(itemObject);

        // THEN
        assertThat(rawValueProperty.get()).isEqualTo("rawValue");
    }

    @Test
    public void updateItemProperties_withRawValueSet_andResolvedValueInIndex_forReferenceField_withUnknownRemoteReference_shouldUpdatePropertyWithErrorLabel() {
        // GIVEN
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectForReference());
        when(minerMock.getDatabaseTopicFromReference(TOPIC_REFERENCE)).thenReturn(createTopicObject());
        when(minerMock.getContentEntryInternalIdentifierWithReference("rawValue", TOPIC2)).thenReturn(OptionalLong.empty());
        ContentItemDto itemObject = createContentItem();
        controller.getRawValuesByFieldRank().put(1, new SimpleStringProperty("old reference rawValue"));
        SimpleStringProperty resolvedValueProperty = new SimpleStringProperty("resolved value");
        controller.getResolvedValuesByFieldRank().put(1, resolvedValueProperty);

        // WHEN
        controller.updateItemProperties(itemObject);

        // THEN
        assertThat(resolvedValueProperty.get()).isEqualTo("<ERROR: entry not found!>");
    }

    @Test
    public void updateItemProperties_withRawValueSet_andResolvedValueInIndex_forReferenceField_withExistingRemoteReference_shouldUpdateProperty() {
        // GIVEN
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectForReference());
        when(minerMock.getDatabaseTopicFromReference(TOPIC_REFERENCE)).thenReturn(createTopicObject());
        when(minerMock.getContentEntryInternalIdentifierWithReference("rawValue", TOPIC2)).thenReturn(OptionalLong.of(0L));
        when(minerMock.getLocalizedResourceValueFromContentEntry(0, 1, TOPIC2, LOCALE)).thenReturn(of("resource value"));
        ContentItemDto itemObject = createContentItem();
        controller.getRawValuesByFieldRank().put(1, new SimpleStringProperty("old reference rawValue"));
        SimpleStringProperty resolvedValueProperty = new SimpleStringProperty("resolved value");
        controller.getResolvedValuesByFieldRank().put(1, resolvedValueProperty);

        // WHEN
        controller.updateItemProperties(itemObject);

        // THEN
        assertThat(resolvedValueProperty.get()).isEqualTo("resource value");
    }

    @Test
    public void updateItemProperties_withRawValueSet_andResolvedValueInIndex_forLocalResourceField_shouldUpdateProperty() {
        // GIVEN
        controller.getRawValuesByFieldRank().put(1, new SimpleStringProperty("old local resource rawValue"));
        final SimpleStringProperty resolvedValueProperty = new SimpleStringProperty("old local resource value");
        controller.getResolvedValuesByFieldRank().put(1, resolvedValueProperty);
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectForLocalResource());
        ContentItemDto itemObject = createContentItem();

        when(minerMock.getLocalizedResourceValueFromTopicAndReference("rawValue", TOPIC1, LOCALE)).thenReturn(of("resolved value"));


        // WHEN
        controller.updateItemProperties(itemObject);


        // THEN
        assertThat(resolvedValueProperty.get()).isEqualTo("resolved value");
    }

    @Test
    public void updateItemProperties_withRawValueSet_andResolvedValueInIndex_forRemoteResourceField_shouldUpdateProperty() {
        // GIVEN
        controller.getRawValuesByFieldRank().put(1, new SimpleStringProperty("old remote resource rawValue"));
        final SimpleStringProperty resolvedValueProperty = new SimpleStringProperty("old local resource value");
        controller.getResolvedValuesByFieldRank().put(1, resolvedValueProperty);
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectForRemoteResource());
        ContentItemDto itemObject = createContentItem();

        when(minerMock.getDatabaseTopicFromReference(TOPIC_REMOTE_REFERENCE)).thenReturn(createTopicObject());
        when(minerMock.getLocalizedResourceValueFromTopicAndReference("rawValue", TOPIC2, LOCALE)).thenReturn(of("resolved remote value"));

        // WHEN
        controller.updateItemProperties(itemObject);

        // THEN
        assertThat(resolvedValueProperty.get()).isEqualTo("resolved remote value");
    }

    @Test(expected=IllegalStateException.class)
    public void updateLinkProperties_whenReferenceNotAvailable_shouldThrowException() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObject();
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = FXCollections.observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0L));
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC1)).thenReturn(empty());

        // WHEN
        controller.updateLinkProperties(topicLinkObject);

        // THEN:ISE
    }

    @Test(expected=IllegalStateException.class)
    public void updateLinkProperties_whenLinkedTopicNotFound_shouldThrowException() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObject();
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = FXCollections.observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0L));
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC1)).thenReturn(of("entryRef"));
        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(empty());

        // WHEN
        controller.updateLinkProperties(topicLinkObject);

        // THEN:ISE
    }

    @Test(expected=IllegalStateException.class)
    public void updateLinkProperties_whenEntryNotFoundInLinkedTopic_shouldThrowException() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObject();
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = FXCollections.observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0L));
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC1)).thenReturn(of("entryRef"));
        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(createTopicObjectWithDataEntry()));

        // WHEN
        controller.updateLinkProperties(topicLinkObject);

        // THEN:ISE
    }

    @Test
    public void updateLinkProperties_whenEntryFoundInLinkedTopic_shouldUpdateProperties() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObject();
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = FXCollections.observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0L));
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC1)).thenReturn(of("entryRef"));
        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(createTopicObjectWithDataEntryAndRef("entryRef")));
        when(minerMock.getLocalizedResourceValueFromContentEntry(0, 1, TOPIC2, LOCALE)).thenReturn(of("remote value"));

        // WHEN
        controller.updateLinkProperties(topicLinkObject);

        // THEN
        assertThat(resources).hasSize(1);
        final ContentEntryDataItem actualDataItem = resources.get(0);
        assertThat(actualDataItem.internalEntryIdProperty().get()).isEqualTo(0);
        assertThat(actualDataItem.referenceProperty().get()).isNull();
        assertThat(actualDataItem.valueProperty().get()).isEqualTo("remote value");
    }

    @Test
    public void updateLinkProperties_whenEntryFoundInLinkedTopic_andLinkedTopicAsAssociation_shouldUpdateProperties() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObjectForAssociation();
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = FXCollections.observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0L));
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC1));
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC1)).thenReturn(of("entryRef1"));
        when(minerMock.getDatabaseTopic(TOPIC3)).thenReturn(of(createAssociationTopicObjectWithDataEntriesAndRefs("entryRef1", "entryRef2")));
        when(minerMock.getDatabaseTopicFromReference(TOPIC_REFERENCE)).thenReturn(createSourceTopicObject());
        final DbDto remoteTopicObject = createRemoteTopicObject();
        when(minerMock.getDatabaseTopicFromReference(TOPIC_REMOTE_REFERENCE)).thenReturn(remoteTopicObject);
        final ContentEntryDto contentEntryDto = ContentEntryDto.builder().build();
        when(minerMock.getRemoteContentEntryWithInternalIdentifier(TOPIC1, 1, 0, TOPIC3)).thenReturn(of(contentEntryDto));
        when(minerMock.getContentEntryInternalIdentifierWithReference("entryRef2", TOPIC4)).thenReturn(OptionalLong.of(0L));
        when(minerMock.getDatabaseTopic(TOPIC4)).thenReturn(of(remoteTopicObject));
        when(minerMock.getLocalizedResourceValueFromContentEntry(0, 1, TOPIC4, LOCALE)).thenReturn(of("remote value"));

        // WHEN
        controller.updateLinkProperties(topicLinkObject);

        // THEN
        assertThat(resources).hasSize(1);
        final ContentEntryDataItem actualDataItem = resources.get(0);
        assertThat(actualDataItem.internalEntryIdProperty().get()).isEqualTo(0);
        assertThat(actualDataItem.referenceProperty().get()).isEqualTo("entryRef2");
        assertThat(actualDataItem.valueProperty().get()).isEqualTo("remote value");
    }

    private EditorLayoutDto createLayoutObject() {
        EditorLayoutDto.EditorProfileDto profileObject = new EditorLayoutDto.EditorProfileDto(TEST_PROFILE_NAME);
        profileObject.setTopic(TOPIC2);
        FieldSettingsDto fieldSettings = new FieldSettingsDto();
        fieldSettings.setRank(1);
        fieldSettings.setRemoteReferenceProfile(TEST_REMOTE_PROFILE_NAME);
        profileObject.getFieldSettings().add(fieldSettings);
        EditorLayoutDto layoutObject = new EditorLayoutDto();
        layoutObject.getProfiles().add(profileObject);

        EditorLayoutDto.EditorProfileDto remoteProfileObject = new EditorLayoutDto.EditorProfileDto(TEST_REMOTE_PROFILE_NAME);
        remoteProfileObject.setTopic(TOPIC1);
        remoteProfileObject.addEntryLabelFieldRank(1);
        layoutObject.getProfiles().add(remoteProfileObject);

        EditorLayoutDto.EditorProfileDto simpleProfileObject = new EditorLayoutDto.EditorProfileDto(TEST_PROFILE_NAME);
        simpleProfileObject.setTopic(TOPIC2);
        layoutObject.getProfiles().add(simpleProfileObject);

        EditorLayoutDto.EditorProfileDto remoteAssociationProfileObject = new EditorLayoutDto.EditorProfileDto(TEST_REMOTE_ASSO_PROFILE_NAME);
        remoteAssociationProfileObject.setTopic(TOPIC3);
        remoteAssociationProfileObject.addEntryLabelFieldRank(1);
        layoutObject.getProfiles().add(remoteAssociationProfileObject);

        return layoutObject;
    }

    private DbDto createTopicObject() {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(TOPIC2)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(INTEGER)
                                .build())
                        .build())
                .withData(DbDataDto.builder().build())
                .build();
    }

    private DbDto createSourceTopicObject() {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(TOPIC1)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(UID)
                                .build())
                        .build())
                .withData(DbDataDto.builder().build())
                .build();
    }

    private DbDto createRemoteTopicObject() {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(TOPIC4)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(UID)
                                .build())
                        .build())
                .withData(DbDataDto.builder().build())
                .build();
    }

    private DbDto createTopicObjectWithDataEntry() {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(TOPIC2)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(INTEGER)
                                .build())
                        .build())
                .withData(DbDataDto.builder()
                        .addEntry(ContentEntryDto.builder().build())
                        .build())
                .build();
    }

    private DbDto createTopicObjectWithDataEntryAndRef(String ref) {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(TOPIC2)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(INTEGER)
                                .build())
                        .build())
                .withData(DbDataDto.builder()
                        .addEntry(ContentEntryDto.builder()
                                .forId(0)
                                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(ref).build())
                                .build())
                        .build())
                .build();
    }

    private DbDto createAssociationTopicObjectWithDataEntriesAndRefs(String sourceRef, String targetRef) {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(TOPIC3)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(REFERENCE)
                                .toTargetReference(TOPIC_REFERENCE)
                                .build())
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(2)
                                .fromType(REFERENCE)
                                .toTargetReference(TOPIC_REMOTE_REFERENCE)
                                .build())
                        .build())
                .withData(DbDataDto.builder()
                        .addEntry(ContentEntryDto.builder()
                                .forId(0)
                                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(sourceRef).build())
                                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue(targetRef).build())
                                .build())
                        .build())
                .build();
    }

    private TopicLinkDto createTopicLinkObject() {
        TopicLinkDto topicLinkObject = new TopicLinkDto();
        topicLinkObject.setTopic(TOPIC2);
        topicLinkObject.setRemoteReferenceProfile(TEST_REMOTE_PROFILE_NAME);
        return topicLinkObject;
    }

    private TopicLinkDto createTopicLinkObjectForAssociation() {
        TopicLinkDto topicLinkObject = new TopicLinkDto();
        topicLinkObject.setTopic(TOPIC3);
        topicLinkObject.setRemoteReferenceProfile(TEST_REMOTE_ASSO_PROFILE_NAME);
        return topicLinkObject;
    }

    private DbDto createTopicObjectWithoutStructureFields() {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(TOPIC2)
                        .build())
                .withData(DbDataDto.builder().build())
                .build();
    }

    private DbDto createTopicObjectForReference() {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(TOPIC1)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(REFERENCE)
                                .toTargetReference(TOPIC_REFERENCE)
                                .build())
                        .build())
                .withData(DbDataDto.builder().build())
                .build();
    }

    private DbDto createTopicObjectForLocalResource() {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(TOPIC1)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(RESOURCE_CURRENT_LOCALIZED)
                                .build())
                        .build())
                .withData(DbDataDto.builder().build())
                .build();
    }

    private DbDto createTopicObjectForRemoteResource() {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(TOPIC1)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(RESOURCE_REMOTE)
                                .toTargetReference(TOPIC_REMOTE_REFERENCE)
                                .build())
                        .build())
                .withData(DbDataDto.builder().build())
                .build();
    }

    private ContentItemDto createContentItem() {
        return ContentItemDto.builder()
                .ofFieldRank(1)
                .withRawValue("rawValue")
                .build();
    }
}
