package fr.tduf.gui.database.controllers.main;

import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.ItemViewModel;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.libtesting.common.helper.javafx.ApplicationTestHelper;
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
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.*;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javafx.collections.FXCollections.observableArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class MainStageViewDataControllerTest {
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
    private static final EditorLayoutDto.EditorProfileDto TEST_PROFILE = new EditorLayoutDto.EditorProfileDto(TEST_PROFILE_NAME);
    private static final EditorLayoutDto.EditorProfileDto TEST_REMOTE_PROFILE = new EditorLayoutDto.EditorProfileDto(TEST_REMOTE_PROFILE_NAME);
    private static final EditorLayoutDto.EditorProfileDto TEST_REMOTE_ASSO_PROFILE = new EditorLayoutDto.EditorProfileDto(TEST_REMOTE_ASSO_PROFILE_NAME);
    private static final EditorLayoutDto.EditorProfileDto TEST_UNEXISTING_PROFILE = new EditorLayoutDto.EditorProfileDto("My profile");

    @BeforeAll
    static void globalSetUp() {
        ApplicationTestHelper.initJavaFX();
    }

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

    private ChoiceBox<EditorLayoutDto.EditorProfileDto> profilesChoiceBox;
    private ChoiceBox<Locale> localesChoiceBox;
    private TitledPane settingsPane;
    private TextField entryFilterTextField;

    @BeforeEach
    void setUp() {
        // Inits application singleton
        new DatabaseEditor();

        initMocks(this);

        TEST_PROFILE.setTopic(TOPIC2);
        TEST_UNEXISTING_PROFILE.setTopic(TOPIC2);

        DatabaseEditor.getInstance().getCommandLineParameters().clear();

        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);

        when(mainStageControllerMock.getMiner()).thenReturn(minerMock);
        when(mainStageControllerMock.getLayoutObject()).thenReturn(layoutObject);
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(topicObject);
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(new SimpleObjectProperty<>());

        when(mainStageControllerMock.currentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        when(mainStageControllerMock.currentEntryLabelProperty()).thenReturn(currentEntryLabelProperty);

        profilesChoiceBox = new ChoiceBox<>(observableArrayList(TEST_PROFILE, TEST_REMOTE_PROFILE, TEST_REMOTE_ASSO_PROFILE));
        profilesChoiceBox.valueProperty().setValue(TEST_PROFILE);
        profilesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (TEST_PROFILE.equals(newValue)) {
                        controller.currentProfileProperty().setValue(getSecondLayoutProfile());
                    } else if (TEST_REMOTE_PROFILE.equals(newValue)) {
                        controller.currentProfileProperty().setValue(getThirdLayoutProfile());
                    } else {
                        throw new IllegalArgumentException("Unknown profile name!");
                    }
                });
        when(mainStageControllerMock.getProfilesChoiceBox()).thenReturn(profilesChoiceBox);

        when(mainStageControllerMock.getTabPane()).thenReturn(new TabPane());

        settingsPane = new TitledPane();
        when(mainStageControllerMock.getSettingsPane()).thenReturn(settingsPane);

        localesChoiceBox = new ChoiceBox<>();
        when(mainStageControllerMock.getLocalesChoiceBox()).thenReturn(localesChoiceBox);

        when(mainStageControllerMock.getDatabaseLocationTextField()).thenReturn(new TextField("location"));

        entryFilterTextField = new TextField();
        when(mainStageControllerMock.getEntryFilterTextField()).thenReturn(entryFilterTextField);

        Button entryFilterButton = new Button();
        when(mainStageControllerMock.getEntryFilterButton()).thenReturn(entryFilterButton);

        Button emptyEntryFilterButton = new Button();
        when(mainStageControllerMock.getEntryEmptyFilterButton()).thenReturn(emptyEntryFilterButton);

        when(mainStageControllerMock.getMainSplashImage()).thenReturn(new ImageView());
        when(mainStageControllerMock.getMainSplashBox()).thenReturn(new HBox());
        when(mainStageControllerMock.getMainVBox()).thenReturn(new VBox());

        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(topicObject));

        when(applicationConfigurationMock.getEditorProfile()).thenReturn(empty());

        controller.currentLocaleProperty.setValue(LOCALE);
    }

    @Test
    void initSettingsPane_whenNoLocaleInProperties_shouldSetDefaultLocale() throws IOException {
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
    void initSettingsPane_whenLocaleInProperties_shouldSetLocaleAccordingly() throws IOException {
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
    void initSettingsPane_shouldSetProfileProperties() throws IOException {
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
    void initGUIComponentsGraphics_shouldSetButtonGraphics_andSplashImage() {
        // given-when
        controller.initGUIComponentsGraphics();

        // then
        assertThat(controller.getEntryFilterButton().getGraphic()).isNotNull();
        assertThat(controller.getEntryEmptyFilterButton().getGraphic()).isNotNull();
        assertThat(controller.getMainSplashImage().getImage()).isNotNull();
    }

    @Test
    void resolveInitialDatabaseDirectory_whenNoCommandLineParameter_andNoConfiguration_shouldReturnEmpty() {
        // GIVEN
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(empty());

        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();

        // THEN
        assertThat(actualDirectory).isEmpty();
    }

    @Test
    void resolveInitialDatabaseDirectory_whenWrongCommandLineParameter_andNoConfiguration_shouldReturnEmpty() {
        // GIVEN
        DatabaseEditor.getInstance().getCommandLineParameters().add("-p");
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(empty());

        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();

        // THEN
        assertThat(actualDirectory).isEmpty();
    }

    @Test
    void resolveInitialDatabaseDirectory_whenRightCommandLineParameter_shouldReturnLocation() {
        // GIVEN
        DatabaseEditor.getInstance().getCommandLineParameters().add("/tdu/euro/bnk/database");

        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();

        // THEN
        assertThat(actualDirectory).contains("/tdu/euro/bnk/database");

        verifyNoInteractions(mainStageControllerMock, applicationConfigurationMock);
    }

    @Test
    void resolveInitialDatabaseDirectory_whenNoCommandLineParameter_andConfiguration_shouldReturnSavedLocation() {
        // GIVEN
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(of(Paths.get("/tdu/euro/bnk/database")));

        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();

        // THEN
        assertThat(actualDirectory).contains("/tdu/euro/bnk/database");
    }

    @Test
    void applyProfile_whenProfileDoesNotExist_shouldThrowException() {
        // GIVEN
        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(topicObject));
        when(mainStageControllerMock.getViewData()).thenReturn(controller);

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> controller.applyProfile(TEST_UNEXISTING_PROFILE));
    }

    @Test
    void applyProfile_whenProfileExists_shouldSwitchProperties_andUpdateConfiguration() throws IOException {
        // GIVEN
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectWithoutStructureFields());
        when(mainStageControllerMock.currentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>());
        final EditorLayoutDto.EditorProfileDto profileObject = getSecondLayoutProfile();
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC2)).thenReturn(empty());


        // WHEN
        controller.applyProfile(TEST_PROFILE);


        // THEN
        assertThat(controller.currentProfileProperty().getValue().getName()).isEqualTo(profileObject.getName());
        assertThat(controller.currentTopicProperty().getValue()).isEqualTo(TOPIC2);
        verify(mainStageControllerMock).setCurrentTopicObject(topicObject);

        verify(applicationConfigurationMock).setEditorProfile(TEST_PROFILE_NAME);
        verify(applicationConfigurationMock).store();
    }

    @Test
    void applySelectedLocale_shouldUpdateConfiguration() throws IOException {
        // GIVEN
        controller.currentProfileProperty().setValue(getSecondLayoutProfile());
        when(mainStageControllerMock.currentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC1));
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(anyInt(), any(DbDto.Topic.class))).thenReturn(empty());

        // WHEN
        controller.applySelectedLocale();

        // THEN
        verify(applicationConfigurationMock).setEditorLocale(LOCALE);
        verify(applicationConfigurationMock).store();
    }

    @Test
    void updateDisplayWithLoadedObjects_whenNoProfileInProperties_shouldUseFirstProfile_andUpdateConfiguration() throws IOException {
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
        assertThat(profilesChoiceBox.getSelectionModel().getSelectedItem().getName()).isEqualTo(profileName);
        assertThat(controller.currentProfileProperty().getValue()).isEqualTo(getSecondLayoutProfile());

        verify(applicationConfigurationMock).setDatabasePath("location");
        verify(applicationConfigurationMock).setEditorProfile(profileName);
        verify(applicationConfigurationMock).store();
    }

    @Test
    void updateDisplayWithLoadedObjects_whenProfileInProperties_shouldUseRightProfile() throws IOException {
        // GIVEN
        final String profileName = getThirdLayoutProfile().getName();
        when(applicationConfigurationMock.getEditorProfile()).thenReturn(of(profileName));
        when(mainStageControllerMock.getDatabaseObjects()).thenReturn(singletonList(createTopicObject(TOPIC2)));
        when(mainStageControllerMock.getNavigationHistory()).thenReturn(new ArrayDeque<>());


        // WHEN
        controller.updateDisplayWithLoadedObjects();


        // THEN
        assertThat(profilesChoiceBox.getSelectionModel().getSelectedItem().getName()).isEqualTo(profileName);
        assertThat(controller.currentProfileProperty().getValue()).isEqualTo(getThirdLayoutProfile());

        verify(applicationConfigurationMock).setEditorProfile(profileName);
        verify(applicationConfigurationMock).store();
    }

    @Test
    void updateDisplayWithLoadedObjects_whenUnknownProfileInProperties_shouldSetPropertyWithInitialProfile() throws IOException {
        // GIVEN
        when(applicationConfigurationMock.getEditorProfile()).thenReturn(of(TEST_UNK_PROFILE_NAME));
        when(mainStageControllerMock.getDatabaseObjects()).thenReturn(singletonList(createTopicObject(TOPIC2)));
        when(mainStageControllerMock.getNavigationHistory()).thenReturn(new ArrayDeque<>());

        // WHEN
        controller.updateDisplayWithLoadedObjects();

        // THEN
        verify(applicationConfigurationMock).setEditorProfile(eq("Test profile"));
        verify(applicationConfigurationMock).store();
    }

    @Test
    void updateEntriesAndSwitchTo_whenNoEntry_shouldSetEmptyList() {
        // GIVEN
        controller.currentProfileProperty().setValue(getSecondLayoutProfile());
        controller.getBrowsableEntries().add(new ContentEntryDataItem());

        // WHEN
        controller.updateEntriesAndSwitchTo(0);

        // THEN
        assertThat(controller.getBrowsableEntries()).isEmpty();
    }

    @Test
    void updateEntriesAndSwitchTo_whenEntries_shouldPopulateEntryList() {
        // GIVEN
        ObservableList<ContentEntryDataItem> browsableEntries = controller.getBrowsableEntries();
        browsableEntries.add(new ContentEntryDataItem());
        controller.currentProfileProperty().setValue(getSecondLayoutProfile());

        when(mainStageControllerMock.currentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC2));

        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(createTopicObjectWithDataEntryAndRef()));
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC2)).thenReturn(empty());


        // WHEN
        controller.updateEntriesAndSwitchTo(0);


        // THEN
        assertThat(browsableEntries).hasSize(1);
        final ContentEntryDataItem actualEntry = browsableEntries.get(0);
        assertThat(actualEntry.referenceProperty().get()).isEqualTo("0");
        assertThat(actualEntry.internalEntryIdProperty().get()).isEqualTo(0);
        assertThat(actualEntry.valueProperty().get()).isEqualTo("<?>");
    }

    @Test
    void updateEntriesAndSwitchTo_whenNegativeIndex_shouldSelectFirstItem() {
        // GIVEN
        final DbDto topicObjectWithDataEntry = createTopicObjectWithDataEntry();

        controller.currentProfileProperty().setValue(getSecondLayoutProfile());
        controller.getBrowsableEntries().add(new ContentEntryDataItem());

        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(topicObjectWithDataEntry);
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC2));

        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(createTopicObjectWithDataEntryAndRef()));
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC2)).thenReturn(empty());
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC2)).thenReturn(of(topicObjectWithDataEntry.getData().getEntries().get(0)));


        // WHEN
        controller.updateEntriesAndSwitchTo(-1);


        // THEN
        assertThat(controller.currentEntryIndexProperty().getValue()).isEqualTo(0);
    }

    @Test
    void refreshAll_shouldResetProperties() {
        // GIVEN
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectWithoutStructureFields());
        final Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>();
        when(mainStageControllerMock.currentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        final EditorLayoutDto.EditorProfileDto profileObject = getFourthLayoutProfile();
        controller.currentProfileProperty().setValue(profileObject);
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC2);
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC2)).thenReturn(empty());

        // WHEN
        controller.refreshAll();

        // THEN
        assertThat(currentEntryIndexProperty.getValue()).isEqualTo(0);
        assertThat(controller.getItemPropsByFieldRank().isEmpty()).isTrue();
        assertThat(controller.getResourcesByTopicLink()).isEmpty();
    }

    @Test
    void updateAllPropertiesWithItemValues_whenEntryNotFound_shouldNotCrash() {
        // GIVEN
        final EditorLayoutDto.EditorProfileDto profileObject = getSecondLayoutProfile();
        controller.currentProfileProperty().setValue(profileObject);
        final Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>(0);
        when(mainStageControllerMock.currentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC1)).thenReturn(empty());

        // WHEN-THEN
        controller.updateAllPropertiesWithItemValues();
    }

    @Test
    void updateAllPropertiesWithItemValues_whenClassicFieldType_andNoLink_shouldUpdateCurrentEntryLabel () {
        // GIVEN
        ContentItemDto item = createContentItem();
        ContentEntryDto contentEntry = ContentEntryDto.builder()
                .addItem(item)
                .build();

        controller.currentProfileProperty().setValue(getSecondLayoutProfile());
        controller.getItemPropsByFieldRank()
                .rawValuePropertyAtFieldRank(1)
                .set("VAL1");

        when(mainStageControllerMock.currentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC1));

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC1)).thenReturn(of(contentEntry));


        // WHEN
        controller.updateAllPropertiesWithItemValues();

        // THEN
        assertThat(currentEntryLabelProperty.get()).isEqualTo("<?>");
    }

    @Test
    void updateViewForContentItem() {
        // given
        controller.currentProfileProperty().setValue(getSecondLayoutProfile());
        ContentItemDto updatedItem = createContentItem();

        // when-then
        controller.updateViewForContentItem(0, updatedItem);

    }

    @Test
    void updateCurrentEntryLabelProperty_whenNoFieldRank_shouldReturnDefaultLabel() {
        // GIVEN
        final EditorLayoutDto.EditorProfileDto profileObject = getSecondLayoutProfile();
        controller.currentProfileProperty().setValue(profileObject);
        final Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>(0);
        when(mainStageControllerMock.currentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(currentTopicProperty);

        // WHEN
        controller.updateCurrentEntryLabelProperty();

        // THEN
        assertThat(currentEntryLabelProperty.getValue()).isEqualTo("<?>");
    }

    @Test
    void updateCurrentEntryLabelProperty_whenSingleFieldRank_shouldRetrieveLabel() {
        // GIVEN
        final EditorLayoutDto.EditorProfileDto profileObject = getSecondLayoutProfile();
        profileObject.addDefaultEntryLabelFieldRank();
        controller.currentProfileProperty().setValue(profileObject);
        final Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>(0);
        when(mainStageControllerMock.currentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC2);
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getLocalizedResourceValueFromContentEntry(0, 1, TOPIC2, LOCALE)).thenReturn(of("label"));

        // WHEN
        controller.updateCurrentEntryLabelProperty();

        // THEN
        assertThat(currentEntryLabelProperty.getValue()).isEqualTo("label");
    }

    @Test
    void updateItemProperties_withoutRawValueSet() {
        // GIVEN
        ContentItemDto itemObject = createContentItem();

        // WHEN-THEN
        controller.updateItemProperties(itemObject);
    }

    @Test
    void updateItemProperties_withRawValueSet_andNoResolvedValueInIndex_shouldOnlyUpdateProperty() {
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
    void updateItemProperties_withRawValueSet_andResolvedValueInIndex_forReferenceField_withUnknownRemoteReference_shouldUpdatePropertyWithErrorLabel() {
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
    void updateItemProperties_withRawValueSet_andResolvedValueInIndex_forReferenceField_withExistingRemoteReference_shouldUpdateProperty() {
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
    void updateItemProperties_withRawValueSet_andResolvedValueInIndex_forLocalResourceField_shouldUpdateProperty() {
        // GIVEN
        ItemViewModel itemViewModel = controller.getItemPropsByFieldRank();
        itemViewModel
                .rawValuePropertyAtFieldRank(1)
                .set("old local resource rawValue");
        StringProperty resolvedValueProperty = itemViewModel.resolvedValuePropertyAtFieldRank(1);
        resolvedValueProperty.set("old local resource value");
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(createTopicObjectForLocalResource());
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC1));
        ContentItemDto itemObject = createContentItem();

        when(minerMock.getLocalizedResourceValueFromTopicAndReference("rawValue", TOPIC1, LOCALE)).thenReturn(of("resolved value"));


        // WHEN
        controller.updateItemProperties(itemObject);


        // THEN
        assertThat(resolvedValueProperty.get()).isEqualTo("resolved value");
    }

    @Test
    void updateItemProperties_withRawValueSet_andResolvedValueInIndex_forRemoteResourceField_shouldUpdateProperty() {
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

    @Test
    void updateAllLinkProperties_whenReferenceNotAvailable_shouldThrowException() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObject();
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.currentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC1)).thenReturn(empty());

        // WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> controller.updateAllLinkProperties(topicLinkObject));
    }

    @Test
    void updateAllLinkProperties_whenLinkedTopicNotFound_shouldThrowException() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObject();
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.currentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, TOPIC1)).thenReturn(of("entryRef"));
        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(empty());

        // WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> controller.updateAllLinkProperties(topicLinkObject));
    }

    @Test
    void updateAllLinkProperties_whenEntryFoundInLinkedTopic_shouldUpdateProperties() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObject();
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.currentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC1);
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(currentTopicProperty);
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
    void updateAllLinkProperties_whenEntryFoundInLinkedTopic_andLinkedTopicAsAssociation_shouldUpdateProperties() {
        // GIVEN
        TopicLinkDto topicLinkObject = createTopicLinkObjectForAssociation();
        ContentEntryDataItem item = new ContentEntryDataItem();
        ObservableList<ContentEntryDataItem> resources = observableArrayList(item);
        controller.getResourcesByTopicLink().put(topicLinkObject, resources);
        when(mainStageControllerMock.currentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC1));
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
    void updateLinkProperties_whenNoMoreLinkedItem_shouldResetErrorProps() {
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
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC1));


        // WHEN
        controller.updateLinkProperties(remoteEntry);


        // THEN
        assertThat(itemProps.isEmpty()).isTrue();
    }

    @Test
    void selectFieldsFromTopic_whenTopicWithoutREFSupport_shouldReturnEmptyList() {
        // GIVEN
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(new SimpleObjectProperty<>(ACHIEVEMENTS));

        // WHEN
        final List<String> actualFields = controller.selectFieldsFromTopic();

        // THEN
        assertThat(actualFields).isEmpty();
    }

    @Test
    void selectEntriesFromTopic_whenTopicWithoutREFSupport_shouldReturnEmptyList() {
        // GIVEN
        when(mainStageControllerMock.currentTopicProperty()).thenReturn(new SimpleObjectProperty<>(ACHIEVEMENTS));

        // WHEN
        final List<String> actualEntries = controller.selectEntriesFromTopic();

        // THEN
        assertThat(actualEntries).isEmpty();
    }

    @Test
    void fetchRemoteContentsWithEntryRef_whenRemoteEntryDoesNotExist_shouldUpdateErrorProps_andReturnDefaultValue() {
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
    void fetchRemoteContentsWithEntryRef_whenRemoteEntryDoesExist_shouldNotUpdateErrorProps_andReturnResolveddValue() {
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

    @Test
    void switchToFirstFilteredEntry_whenNotEmptyList_shouldSetCurrentIndexProperty() {
        // given
        ObservableList<ContentEntryDataItem> browsableEntries = controller.getBrowsableEntries();
        ContentEntryDataItem item1 = new ContentEntryDataItem();
        item1.setInternalEntryId(1);
        ContentEntryDataItem item2 = new ContentEntryDataItem();
        item2.setInternalEntryId(10);
        browsableEntries.addAll(item1, item2);
        controller.currentProfileProperty.setValue(getFirstLayoutProfile());

        // when
        controller.switchToFirstFilteredEntry();


        // then
        assertThat(controller.currentEntryIndexProperty().getValue()).isEqualTo(1);
    }

    @Test
    void switchToFirstFilteredEntry_whenEmptyList_shouldNotSetCurrentIndexProperty() {
        // given
        controller.currentEntryIndexProperty().setValue(10);

        // when
        controller.switchToFirstFilteredEntry();

        // then
        assertThat(controller.currentEntryIndexProperty().getValue()).isEqualTo(10);
    }

    @Test
    void applyEntryFilter_whenCriteriaGiven_shouldSetPredicateFilter() {
        // given
        Predicate<? super ContentEntryDataItem> defaultPredicate = controller.getFilteredEntries().getPredicate();
        entryFilterTextField.textProperty().setValue("foo");

        // when
        controller.applyEntryFilter();

        // then
        assertThat(controller.getFilteredEntries().getPredicate()).isNotSameAs(defaultPredicate);
    }

    @Test
    void applyEntryFilter_whenNoCriteriaGiven_shouldSetPredicateFilterToDefault() {
        // given
        Predicate<? super ContentEntryDataItem> defaultPredicate = controller.getFilteredEntries().getPredicate();
        entryFilterTextField.textProperty().setValue("");

        // when
        controller.applyEntryFilter();

        // then
        assertThat(controller.getFilteredEntries().getPredicate()).isSameAs(defaultPredicate);
    }

    @Test
    void resetEntryFilter_shouldResetProperty_andSetPredicateFilterToDefault() {
        // given
        Predicate<? super ContentEntryDataItem> defaultPredicate = controller.getFilteredEntries().getPredicate();
        entryFilterTextField.textProperty().setValue("foo");
        controller.applyEntryFilter();

        // when
        controller.resetEntryFilter();

        // then
        assertThat(controller.getFilteredEntries().getPredicate()).isSameAs(defaultPredicate);
        assertThat(entryFilterTextField.textProperty().getValue()).isEmpty();
    }

    @Test
    void toggleSplashImage_whenTrue_shouldHideMainVbox_andDisplayMainSplashHbox() {
        // given-when
        controller.toggleSplashImage(true);

        // then
        assertThat(controller.getMainSplashHBox().isVisible()).isTrue();
        assertThat(controller.getMainVBox().isVisible()).isFalse();
    }

    @Test
    void toggleSplashImage_whenFalse_shouldDisplayMainVbox_andHideMainSplashHbox() {
        // given-when
        controller.toggleSplashImage(true);

        // then
        assertThat(controller.getMainSplashHBox().isVisible()).isTrue();
        assertThat(controller.getMainVBox().isVisible()).isFalse();
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

    private TopicLinkDto createTopicLinkObject() {
        TopicLinkDto topicLinkObject = new TopicLinkDto();
        topicLinkObject.setTopic(TOPIC2);
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

    private EditorLayoutDto.EditorProfileDto getFirstLayoutProfile() {
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
