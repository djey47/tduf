package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TabPane;
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
import java.util.Optional;
import java.util.OptionalLong;

import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.INTEGER;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.REFERENCE;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class MainStageViewDataControllerTest {
    private static final DbDto.Topic TOPIC1 = CAR_PHYSICS_DATA;
    private static final DbDto.Topic TOPIC2 = BRANDS;
    private static final Locale LOCALE = UNITED_STATES;
    private static final String TOPIC_REFERENCE = "TOPICREF";
    private static final String TEST_PROFILE_NAME = "Test profile";
    private static final String TEST_REMOTE_PROFILE_NAME = "Test remote profile";

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

    @Before
    public void setUp() {
        DatabaseEditor.getCommandLineParameters().clear();

        when(mainStageControllerMock.getMiner()).thenReturn(minerMock);
        when(mainStageControllerMock.getLayoutObject()).thenReturn(layoutObject);
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(topicObject);

        when(mainStageControllerMock.getCurrentLocaleProperty()).thenReturn(new SimpleObjectProperty<>(LOCALE));
        when(mainStageControllerMock.getCurrentEntryLabelProperty()).thenReturn(currentEntryLabelProperty);

        final ChoiceBox<String> profilesChoiceBox = new ChoiceBox<>();
        profilesChoiceBox.valueProperty().setValue(TEST_PROFILE_NAME);
        when(mainStageControllerMock.getProfilesChoiceBox()).thenReturn(profilesChoiceBox);
        when(mainStageControllerMock.getTabPane()).thenReturn(new TabPane());

        when(minerMock.getDatabaseTopic(TOPIC2)).thenReturn(of(topicObject));
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenNoCommandLineParameter_andNoConfiguration_shouldReturnEmpty() throws Exception {
        // GIVEN
        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
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

        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
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
        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
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
        DbDto remoteTopic = createTopicObject();
        when(minerMock.getDatabaseTopicFromReference(TOPIC_REFERENCE)).thenReturn(remoteTopic);
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
        DbDto remoteTopic = createTopicObject();
        when(minerMock.getDatabaseTopicFromReference(TOPIC_REFERENCE)).thenReturn(remoteTopic);
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

    private ContentItemDto createContentItem() {
        return ContentItemDto.builder()
                .ofFieldRank(1)
                .withRawValue("rawValue")
                .build();
    }
}
