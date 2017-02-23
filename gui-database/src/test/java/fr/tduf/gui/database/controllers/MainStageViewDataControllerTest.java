package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.ItemViewModel;
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
import javafx.scene.control.TitledPane;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.*;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javafx.collections.FXCollections.observableArrayList;
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
    private static final String TEST_UNK_PROFILE_NAME = "profile?";

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

    private final DbDto topicObject = createTopicObject(TOPIC2);

    private final StringProperty currentEntryLabelProperty = new SimpleStringProperty("");
    private final Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>(-1);

    private ChoiceBox<String> profilesChoiceBox;
    private ChoiceBox<Locale> localesChoiceBox;
    private TitledPane settingsPane;

    @Before
    public void setUp() {
        DatabaseEditor.getCommandLineParameters().clear();

        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);

        when(mainStageControllerMock.getMiner()).thenReturn(minerMock);
        when(mainStageControllerMock.getLayoutObject()).thenReturn(layoutObject);
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(topicObject);

        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        when(mainStageControllerMock.getCurrentEntryLabelProperty()).thenReturn(currentEntryLabelProperty);

        profilesChoiceBox = new ChoiceBox<>(observableArrayList(TEST_PROFILE_NAME, TEST_REMOTE_PROFILE_NAME, TEST_REMOTE_ASSO_PROFILE_NAME));
        profilesChoiceBox.valueProperty().setValue(TEST_PROFILE_NAME);
        profilesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (TEST_PROFILE_NAME.equals(newValue)) {
                        controller.currentProfile().setValue(getSecondLayoutProfile());
                    } else if (TEST_REMOTE_PROFILE_NAME.equals(newValue)) {
                        controller.currentProfile().setValue(getThirdLayoutProfile());
                    } else {
                        throw new IllegalArgumentException("Unknwown profile name!");
                    }
                });
        when(mainStageControllerMock.getProfilesChoiceBox()).thenReturn(profilesChoiceBox);

        when(mainStageControllerMock.getTabPane()).thenReturn(new TabPane());

        settingsPane = new TitledPane();
        when(mainStageControllerMock.getSettingsPane()).thenReturn(settingsPane);

        localesChoiceBox = new ChoiceBox<>();
        when(mainStageControllerMock.getLocalesChoiceBox()).thenReturn(localesChoiceBox);

        when(mainStageControllerMock.getDatabaseLocationTextField()).thenReturn(new TextField("location"));

        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(topicObject));

        when(applicationConfigurationMock.getEditorProfile()).thenReturn(empty());

        controller.currentLocaleProperty.setValue(LOCALE);
    }

    @Test
    public void initSettingsPane_whenNoLocaleInProperties_shouldSetDefaultLocale() throws IOException {
        // GIVEN
        settingsPane.expandedProperty().set(true);

        when(applicationConfigurationMock.getEditorLocale()).thenReturn(empty());


        // WHEN
        controller.initSettingsPane("directory",
                (observable, oldValue, newValue) -> {},
                (observable, oldValue, newValue) -> {});


        // THEN
        assertThat(settingsPane.expandedProperty().get()).isFalse();
        assertThat(localesChoiceBox.getItems()).hasSize(8);
        assertThat(controller.currentLocaleProperty.getValue()).isEqualTo(UNITED_STATES);
        assertThat(controller.getDatabaseLocationTextField().getText()).isEqualTo("directory");
    }

    @Test
    public void initSettingsPane_whenLocaleInProperties_shouldSetLocaleAccordingly() throws IOException {
        // GIVEN
        when(applicationConfigurationMock.getEditorLocale()).thenReturn(of(FRANCE));

        // WHEN
        controller.initSettingsPane("directory",
                (observable, oldValue, newValue) -> {},
                (observable, oldValue, newValue) -> {});

        // THEN
        assertThat(controller.currentLocaleProperty.getValue()).isEqualTo(FRANCE);
    }

    @Test
    public void initSettingsPane_shouldSetProfileProperties() throws IOException {
        // GIVEN
        when(applicationConfigurationMock.getEditorLocale()).thenReturn(empty());

        // WHEN
        controller.initSettingsPane("directory",
                (observable, oldValue, newValue) -> {},
                (observable, oldValue, newValue) -> {});

        // THEN
        verify(mainStageControllerMock).setLayoutObject(editorLayoutCaptor.capture());
        final EditorLayoutDto actualLayout = editorLayoutCaptor.getValue();
        assertThat(actualLayout.getProfiles()).isNotEmpty();
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenNoCommandLineParameter_andNoConfiguration_shouldReturnEmpty() throws Exception {
        // GIVEN
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(empty());

        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();

        // THEN
        assertThat(actualDirectory).isEmpty();
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenWrongCommandLineParameter_andNoConfiguration_shouldReturnEmpty() throws Exception {
        // GIVEN
        DatabaseEditor.getCommandLineParameters().add("-p");
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(empty());


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
    public void applyProfile_whenProfileDoesNotExist_shouldApplyDefaultProfile() {
        // GIVEN
        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(topicObject));
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>());
        when(mainStageControllerMock.getViewData()).thenReturn(controller);

        // WHEN-THEN
        controller.applyProfile("myProfile");
    }

    @Test
    public void applyProfile_whenProfileExists_shouldSwitchProperties_andUpdateConfiguration() throws IOException {
        // GIVEN
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectWithoutStructureFields());
        Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>();
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>());
        final EditorLayoutDto.EditorProfileDto profileObject = getSecondLayoutProfile();
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC2)).thenReturn(empty());


        // WHEN
        controller.applyProfile(TEST_PROFILE_NAME);


        // THEN
        assertThat(controller.currentProfile().getValue()).isEqualTo(profileObject);
        assertThat(currentTopicProperty.getValue()).isEqualTo(TOPIC2);
        verify(mainStageControllerMock).setCurrentTopicObject(topicObject);

        verify(applicationConfigurationMock).setEditorProfile(TEST_PROFILE_NAME);
        verify(applicationConfigurationMock).store();
    }

    @Test
    public void applySelectedLocale_shouldUpdateConfiguration() throws IOException {
        // GIVEN
        controller.currentProfile().setValue(getSecondLayoutProfile());
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC1));
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(anyInt(), any(DbDto.Topic.class))).thenReturn(empty());

        // WHEN
        controller.applySelectedLocale();

        // THEN
        verify(applicationConfigurationMock).setEditorLocale(LOCALE);
        verify(applicationConfigurationMock).store();
    }

    @Test
    public void updateDisplayWithLoadedObjects_whenNoProfileInProperties_shouldUseFirstProfile_andUpdateConfiguration() throws IOException {
        // GIVEN
        when(mainStageControllerMock.getDatabaseObjects()).thenReturn(singletonList(createTopicObject(TOPIC2)));
        Deque<EditorLocation> navigationHistory = new ArrayDeque<>();
        navigationHistory.add(new EditorLocation(1, "profile", 0));
        when(mainStageControllerMock.getNavigationHistory()).thenReturn(navigationHistory);


        // WHEN
        controller.updateDisplayWithLoadedObjects();


        // THEN
        final String profileName = getSecondLayoutProfile().getName();
        assertThat(navigationHistory).isEmpty();
        assertThat(profilesChoiceBox.getSelectionModel().getSelectedItem()).isEqualTo(profileName);
        assertThat(controller.currentProfile().getValue()).isEqualTo(getSecondLayoutProfile());

        verify(applicationConfigurationMock).setDatabasePath("location");
        verify(applicationConfigurationMock).setEditorProfile(profileName);
        verify(applicationConfigurationMock).store();
    }

    @Test
    public void updateDisplayWithLoadedObjects_whenProfileInProperties_shouldUseRightProfile() throws IOException {
        // GIVEN
        final String profileName = getThirdLayoutProfile().getName();
        when(applicationConfigurationMock.getEditorProfile()).thenReturn(of(profileName));
        when(mainStageControllerMock.getDatabaseObjects()).thenReturn(singletonList(createTopicObject(TOPIC2)));
        when(mainStageControllerMock.getNavigationHistory()).thenReturn(new ArrayDeque<>());


        // WHEN
        controller.updateDisplayWithLoadedObjects();


        // THEN
        assertThat(profilesChoiceBox.getSelectionModel().getSelectedItem()).isEqualTo(profileName);
        assertThat(controller.currentProfile().getValue()).isEqualTo(getThirdLayoutProfile());

        verify(applicationConfigurationMock).setEditorProfile(profileName);
        verify(applicationConfigurationMock).store();
    }

    @Test
    public void updateDisplayWithLoadedObjects_whenUnknownProfileInProperties_shouldNotOverwriteProperty() throws IOException {
        // GIVEN
        when(applicationConfigurationMock.getEditorProfile()).thenReturn(of(TEST_UNK_PROFILE_NAME));
        when(mainStageControllerMock.getDatabaseObjects()).thenReturn(singletonList(createTopicObject(TOPIC2)));
        when(mainStageControllerMock.getNavigationHistory()).thenReturn(new ArrayDeque<>());

        // WHEN
        controller.updateDisplayWithLoadedObjects();

        // THEN
        verify(applicationConfigurationMock, never()).setEditorProfile(anyString());
        verify(applicationConfigurationMock).store();
    }

    @Test
    public void updateEntriesAndSwitchTo_whenNoEntry_shouldSetEmptyList() {
        // GIVEN
        controller.currentProfile().setValue(getSecondLayoutProfile());
        controller.getBrowsableEntries().add(new ContentEntryDataItem());

        // WHEN
        controller.updateEntriesAndSwitchTo(0);

        // THEN
        assertThat(controller.getBrowsableEntries()).isEmpty();
    }

    @Test
    public void updateEntriesAndSwitchTo_whenEntries_shouldPopulateEntryList() {
        // GIVEN
        controller.currentProfile().setValue(getSecondLayoutProfile());
        controller.getBrowsableEntries().add(new ContentEntryDataItem());

        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(createTopicObjectWithDataEntryAndRef()));
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC2)).thenReturn(empty());


        // WHEN
        controller.updateEntriesAndSwitchTo(0);


        // THEN
        assertThat(controller.getBrowsableEntries()).hasSize(1);
        final ContentEntryDataItem actualEntry = controller.getBrowsableEntries().get(0);
        assertThat(actualEntry.referenceProperty().get()).isEqualTo("0");
        assertThat(actualEntry.internalEntryIdProperty().get()).isEqualTo(0);
        assertThat(actualEntry.valueProperty().get()).isEqualTo("<?>");
    }

    @Test
    public void updateEntriesAndSwitchTo_whenNegativeIndex_shouldSelectFirstItem() {
        // GIVEN
        final DbDto topicObjectWithDataEntry = createTopicObjectWithDataEntry();

        controller.currentProfile().setValue(getSecondLayoutProfile());
        controller.getBrowsableEntries().add(new ContentEntryDataItem());

        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(topicObjectWithDataEntry);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC2));

        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(createTopicObjectWithDataEntryAndRef()));
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC2)).thenReturn(empty());
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC2)).thenReturn(of(topicObjectWithDataEntry.getData().getEntries().get(0)));


        // WHEN
        controller.updateEntriesAndSwitchTo(-1);


        // THEN
        assertThat(controller.currentEntryIndexProperty().getValue()).isEqualTo(0);
    }

    @Test
    public void refreshAll_shouldResetProperties() {
        // GIVEN
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectWithoutStructureFields());
        final Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>();
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        final EditorLayoutDto.EditorProfileDto profileObject = getFourthLayoutProfile();
        controller.currentProfile().setValue(profileObject);
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC2);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC2)).thenReturn(empty());

        // WHEN
        controller.refreshAll();

        // THEN
        assertThat(currentEntryIndexProperty.getValue()).isEqualTo(0);
        assertThat(controller.getItemPropsByFieldRank().isEmpty()).isTrue();
        assertThat(controller.getResourcesByTopicLink()).isEmpty();
    }

    @Test
    public void updateAllPropertiesWithItemValues_whenEntryNotFound_shouldNotCrash() {
        // GIVEN
        final EditorLayoutDto.EditorProfileDto profileObject = getSecondLayoutProfile();
        controller.currentProfile().setValue(profileObject);
        final Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>(0);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC1)).thenReturn(empty());

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

        controller.currentProfile().setValue(getSecondLayoutProfile());
        controller.getItemPropsByFieldRank()
                .rawValuePropertyAtFieldRank(1)
                .set("VAL1");

        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC1));

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC1)).thenReturn(of(contentEntry));


        // WHEN
        controller.updateAllPropertiesWithItemValues();

        // THEN
        assertThat(currentEntryLabelProperty.get()).isEqualTo("<?>");
    }

    @Test
    public void updateCurrentEntryLabelProperty_whenNoFieldRank_shouldReturnDefaultLabel() {
        // GIVEN
        final EditorLayoutDto.EditorProfileDto profileObject = getSecondLayoutProfile();
        controller.currentProfile().setValue(profileObject);
        final Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>(0);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);

        // WHEN
        controller.updateCurrentEntryLabelProperty();

        // THEN
        assertThat(currentEntryLabelProperty.getValue()).isEqualTo("<?>");
    }

    @Test
    public void updateCurrentEntryLabelProperty_whenSingleFieldRank_shouldRetrieveLabel() {
        // GIVEN
        final EditorLayoutDto.EditorProfileDto profileObject = getSecondLayoutProfile();
        profileObject.addDefaultEntryLabelFieldRank();
        controller.currentProfile().setValue(profileObject);
        final Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>(0);
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
        StringProperty rawValueProperty = controller.getItemPropsByFieldRank()
                .rawValuePropertyAtFieldRank(1);
        rawValueProperty.set("old rawValue");

        // WHEN
        controller.updateItemProperties(itemObject);

        // THEN
        assertThat(rawValueProperty.get()).isEqualTo("rawValue");
    }

    @Test
    public void updateItemProperties_withRawValueSet_andResolvedValueInIndex_forReferenceField_withUnknownRemoteReference_shouldUpdatePropertyWithErrorLabel() {
        // GIVEN
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectForReference());
        when(minerMock.getDatabaseTopicFromReference(TOPIC_REFERENCE)).thenReturn(createTopicObject(TOPIC2));
        when(minerMock.getContentEntryInternalIdentifierWithReference("rawValue", TOPIC2)).thenReturn(OptionalInt.empty());
        ContentItemDto itemObject = createContentItem();
        ItemViewModel itemViewModel = controller.getItemPropsByFieldRank();
        itemViewModel
                .rawValuePropertyAtFieldRank(1)
                .set("old reference rawValue");
        StringProperty resolvedValueProperty = itemViewModel.resolvedValuePropertyAtFieldRank(1);
        resolvedValueProperty.set("resolved value");

        // WHEN
        controller.updateItemProperties(itemObject);

        // THEN
        assertThat(resolvedValueProperty.get()).isEqualTo("");
        assertThat(itemViewModel.errorPropertyAtFieldRank(1).get()).isTrue();
        assertThat(itemViewModel.errorMessagePropertyAtFieldRank(1).get()).isNotEmpty();
    }

    @Test
    public void updateItemProperties_withRawValueSet_andResolvedValueInIndex_forReferenceField_withExistingRemoteReference_shouldUpdateProperty() {
        // GIVEN
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectForReference());
        when(minerMock.getDatabaseTopicFromReference(TOPIC_REFERENCE)).thenReturn(createTopicObject(TOPIC2));
        when(minerMock.getContentEntryInternalIdentifierWithReference("rawValue", TOPIC2)).thenReturn(OptionalInt.of(0));
        when(minerMock.getLocalizedResourceValueFromContentEntry(0, 1, TOPIC2, LOCALE)).thenReturn(of("resource value"));
        ContentItemDto itemObject = createContentItem();
        ItemViewModel itemViewModel = controller.getItemPropsByFieldRank();
        itemViewModel
                .rawValuePropertyAtFieldRank(1)
                .set("old reference rawValue");
        StringProperty resolvedValueProperty = itemViewModel.resolvedValuePropertyAtFieldRank(1);
        resolvedValueProperty.set("resolved value");

        // WHEN
        controller.updateItemProperties(itemObject);

        // THEN
        assertThat(resolvedValueProperty.get()).isEqualTo("resource value");
    }

    @Test
    public void updateItemProperties_withRawValueSet_andResolvedValueInIndex_forLocalResourceField_shouldUpdateProperty() {
        // GIVEN
        ItemViewModel itemViewModel = controller.getItemPropsByFieldRank();
        itemViewModel
                .rawValuePropertyAtFieldRank(1)
                .set("old local resource rawValue");
        StringProperty resolvedValueProperty = itemViewModel.resolvedValuePropertyAtFieldRank(1);
        resolvedValueProperty.set("old local resource value");
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
        ItemViewModel itemViewModel = controller.getItemPropsByFieldRank();
        itemViewModel
                .rawValuePropertyAtFieldRank(1)
                .set("old remote resource rawValue");
        StringProperty resolvedValueProperty = itemViewModel.resolvedValuePropertyAtFieldRank(1);
        resolvedValueProperty.set("old local resource value");
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectForRemoteResource());
        ContentItemDto itemObject = createContentItem();

        when(minerMock.getDatabaseTopicFromReference(TOPIC_REMOTE_REFERENCE)).thenReturn(createTopicObject(TOPIC2));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference("rawValue", TOPIC2, LOCALE)).thenReturn(of("resolved remote value"));

        // WHEN
        controller.updateItemProperties(itemObject);

        // THEN
        assertThat(resolvedValueProperty.get()).isEqualTo("resolved remote value");
    }

    @Test(expected=IllegalStateException.class)
    public void updateAllLinkProperties_whenReferenceNotAvailable_shouldThrowException() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObject(TOPIC2);
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC1)).thenReturn(empty());

        // WHEN
        controller.updateAllLinkProperties(topicLinkObject);

        // THEN:ISE
    }

    @Test(expected=IllegalStateException.class)
    public void updateAllLinkProperties_whenLinkedTopicNotFound_shouldThrowException() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObject(TOPIC2);
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC1)).thenReturn(of("entryRef"));
        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(empty());

        // WHEN
        controller.updateAllLinkProperties(topicLinkObject);

        // THEN:ISE
    }

    @Test
    public void updateAllLinkProperties_whenEntryFoundInLinkedTopic_shouldUpdateProperties() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObject(TOPIC2);
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC1)).thenReturn(of("entryRef"));
        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(createTopicObjectWithDataEntryAndRef()));
        when(minerMock.getLocalizedResourceValueFromContentEntry(0, 1, TOPIC2, LOCALE)).thenReturn(of("remote value"));

        // WHEN
        controller.updateAllLinkProperties(topicLinkObject);

        // THEN
        assertThat(resources).hasSize(1);
        final ContentEntryDataItem actualDataItem = resources.get(0);
        assertThat(actualDataItem.internalEntryIdProperty().get()).isEqualTo(0);
        assertThat(actualDataItem.referenceProperty().get()).isNull();
        assertThat(actualDataItem.valueProperty().get()).isEqualTo("remote value");
    }

    @Test
    public void updateAllLinkProperties_whenEntryFoundInLinkedTopic_andLinkedTopicAsAssociation_shouldUpdateProperties() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObjectForAssociation();
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC1));
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC1)).thenReturn(of("entryRef1"));
        when(minerMock.getDatabaseTopic(TOPIC3)).thenReturn(of(createAssociationTopicObjectWithDataEntriesAndRefs()));
        final DbDto remoteTopicObject = createRemoteTopicObject();
        when(minerMock.getDatabaseTopicFromReference(TOPIC_REMOTE_REFERENCE)).thenReturn(remoteTopicObject);
        when(minerMock.getContentEntryInternalIdentifierWithReference("entryRef2", TOPIC4)).thenReturn(OptionalInt.of(0));
        when(minerMock.getDatabaseTopic(TOPIC4)).thenReturn(of(remoteTopicObject));
        when(minerMock.getLocalizedResourceValueFromContentEntry(0, 1, TOPIC4, LOCALE)).thenReturn(of("remote value"));

        // WHEN
        controller.updateAllLinkProperties(topicLinkObject);

        // THEN
        assertThat(resources).hasSize(1);
        final ContentEntryDataItem actualDataItem = resources.get(0);
        assertThat(actualDataItem.internalEntryIdProperty().get()).isEqualTo(0);
        assertThat(actualDataItem.referenceProperty().get()).isEqualTo("entryRef2");
        assertThat(actualDataItem.valueProperty().get()).isEqualTo("remote value");
    }

    @Test
    public void updateLinkProperties_whenNoMoreLinkedItem_shouldResetErrorProps() {
        // GIVEN
        ItemViewModel itemProps = controller.getItemPropsByFieldRank();
        itemProps.rawValuePropertyAtFieldRank(-1).set("RAW VALUE");
        Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> remoteEntries = new HashMap<>();
        ObservableList<ContentEntryDataItem> entries = FXCollections.observableArrayList();
        remoteEntries.put(createTopicLinkObjectForAssociation(), entries);
        Map.Entry<TopicLinkDto, ObservableList<ContentEntryDataItem>> remoteEntry = remoteEntries.entrySet().stream().findAny()
                .orElseThrow(IllegalStateException::new);

        when(minerMock.getDatabaseTopic(TOPIC3)).thenReturn(of(createTopicObject(TOPIC3)));
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(-1, TOPIC1)).thenReturn(of("000000"));
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC1));


        // WHEN
        controller.updateLinkProperties(remoteEntry);


        // THEN
        assertThat(itemProps.isEmpty()).isTrue();
    }

    @Test
    public void selectFieldsFromTopic_whenTopicWithoutREFSupport_shouldReturnEmptyList() {
        // GIVEN
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(ACHIEVEMENTS));

        // WHEN
        final List<String> actualFields = controller.selectFieldsFromTopic();

        // THEN
        assertThat(actualFields).isEmpty();
    }

    @Test
    public void selectEntriesFromTopic_whenTopicWithoutREFSupport_shouldReturnEmptyList() {
        // GIVEN
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(ACHIEVEMENTS));

        // WHEN
        final List<String> actualEntries = controller.selectEntriesFromTopic();

        // THEN
        assertThat(actualEntries).isEmpty();
    }

    @Test
    public void fetchRemoteContentsWithEntryRef_whenRemoteEntryDoesNotExist_shouldUpdateErrorProps_andReturnDefaultValue() {
        // GIVEN
        when(minerMock.getContentEntryInternalIdentifierWithReference("000000", TOPIC1)).thenReturn(OptionalInt.empty());

        // WHEN
        String actualValue = controller.fetchRemoteContentsWithEntryRef(1, TOPIC1, "000000", singletonList(1));

        // THEN
        ItemViewModel itemProps = controller.getItemPropsByFieldRank();
        assertThat(itemProps.errorMessagePropertyAtFieldRank(1).get()).isEqualTo("No content with provided identifier: either select or create one.");
        assertThat(itemProps.errorPropertyAtFieldRank(1).get()).isTrue();
        assertThat(actualValue).isEmpty();
    }

    @Test
    public void fetchRemoteContentsWithEntryRef_whenRemoteEntryDoesExist_shouldNotUpdateErrorProps_andReturnResolveddValue() {
        // GIVEN
        ItemViewModel itemProps = controller.getItemPropsByFieldRank();
        itemProps.errorPropertyAtFieldRank(1).set(true);
        itemProps.errorMessagePropertyAtFieldRank(1).set("Error!");
        when(minerMock.getContentEntryInternalIdentifierWithReference("000000", TOPIC1)).thenReturn(OptionalInt.of(0));

        // WHEN
        String actualValue = controller.fetchRemoteContentsWithEntryRef(1, TOPIC1, "000000", new ArrayList<>(0));

        // THEN
        assertThat(itemProps.errorMessagePropertyAtFieldRank(1).get()).isEqualTo("Error!");
        assertThat(itemProps.errorPropertyAtFieldRank(1).get()).isTrue();
        assertThat(actualValue).isEqualTo("<?>");
    }

    private EditorLayoutDto createLayoutObject() {
        EditorLayoutDto layoutObject = new EditorLayoutDto();

        EditorLayoutDto.EditorProfileDto defaultProfileObject = new EditorLayoutDto.EditorProfileDto("Vehicle slots");
        defaultProfileObject.setTopic(TOPIC2);
        layoutObject.getProfiles().add(defaultProfileObject);

        EditorLayoutDto.EditorProfileDto profileObject = new EditorLayoutDto.EditorProfileDto(TEST_PROFILE_NAME);
        profileObject.setTopic(TOPIC2);
        FieldSettingsDto fieldSettings = new FieldSettingsDto();
        fieldSettings.setRank(1);
        fieldSettings.setRemoteReferenceProfile(TEST_REMOTE_PROFILE_NAME);
        profileObject.getFieldSettings().add(fieldSettings);
        layoutObject.getProfiles().add(profileObject);

        EditorLayoutDto.EditorProfileDto remoteProfileObject = new EditorLayoutDto.EditorProfileDto(TEST_REMOTE_PROFILE_NAME);
        remoteProfileObject.setTopic(TOPIC1);
        remoteProfileObject.addDefaultEntryLabelFieldRank();
        layoutObject.getProfiles().add(remoteProfileObject);

        EditorLayoutDto.EditorProfileDto simpleProfileObject = new EditorLayoutDto.EditorProfileDto(TEST_PROFILE_NAME);
        simpleProfileObject.setTopic(TOPIC2);
        layoutObject.getProfiles().add(simpleProfileObject);

        EditorLayoutDto.EditorProfileDto remoteAssociationProfileObject = new EditorLayoutDto.EditorProfileDto(TEST_REMOTE_ASSO_PROFILE_NAME);
        remoteAssociationProfileObject.setTopic(TOPIC3);
        remoteAssociationProfileObject.addDefaultEntryLabelFieldRank();
        layoutObject.getProfiles().add(remoteAssociationProfileObject);

        return layoutObject;
    }

    private DbDto createTopicObject(DbDto.Topic topic) {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(topic)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(INTEGER)
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
                        .forTopic(TOPIC2)
                        .addEntry(ContentEntryDto.builder()
                                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("REF").build())
                                .build())
                        .build())
                .build();
    }

    private DbDto createTopicObjectWithDataEntryAndRef() {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(TOPIC2)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(INTEGER)
                                .build())
                        .build())
                .withData(DbDataDto.builder()
                        .forTopic(TOPIC2)
                        .addEntry(ContentEntryDto.builder()
                                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("entryRef").build())
                                .build())
                        .build())
                .build();
    }

    private DbDto createAssociationTopicObjectWithDataEntriesAndRefs() {
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
                        .forTopic(TOPIC3)
                        .addEntry(ContentEntryDto.builder()
                                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("entryRef1").build())
                                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue("entryRef2").build())
                                .build())
                        .build())
                .build();
    }

    private TopicLinkDto createTopicLinkObject(DbDto.Topic topic) {
        TopicLinkDto topicLinkObject = new TopicLinkDto();
        topicLinkObject.setTopic(topic);
        topicLinkObject.setId(-1);
        topicLinkObject.setRemoteReferenceProfile(TEST_REMOTE_PROFILE_NAME);
        return topicLinkObject;
    }

    private TopicLinkDto createTopicLinkObjectForAssociation() {
        TopicLinkDto topicLinkObject = new TopicLinkDto();
        topicLinkObject.setTopic(TOPIC3);
        topicLinkObject.setId(-1);
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

    private EditorLayoutDto.EditorProfileDto getDefaultLayoutProfile() {
        return layoutObject.getProfiles().get(0);
    }

    private EditorLayoutDto.EditorProfileDto getSecondLayoutProfile() {
        return layoutObject.getProfiles().get(1);
    }

    private EditorLayoutDto.EditorProfileDto getThirdLayoutProfile() {
        return layoutObject.getProfiles().get(2);
    }

    private EditorLayoutDto.EditorProfileDto getFourthLayoutProfile() {
        return layoutObject.getProfiles().get(3);
    }
}
