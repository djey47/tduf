package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.gui.database.dto.EditorLayoutDto;
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

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.INTEGER;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class MainStageViewDataControllerTest {
    private static final DbDto.Topic TOPIC = CAR_PHYSICS_DATA;

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

        when(mainStageControllerMock.getCurrentLocaleProperty()).thenReturn(new SimpleObjectProperty<>(Locale.UNITED_STATES));
        when(mainStageControllerMock.getCurrentEntryLabelProperty()).thenReturn(currentEntryLabelProperty);

        when(mainStageControllerMock.getProfilesChoiceBox()).thenReturn(new ChoiceBox<>());
        when(mainStageControllerMock.getTabPane()).thenReturn(new TabPane());

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));
    }

    @Test
    public void updateAllPropertiesWithItemValues_whenClassicFieldType_andNoLink_shouldUpdateCurrentEntryLabel () {
        // GIVEN
        ContentItemDto item = ContentItemDto.builder()
                .ofFieldRank(1)
                .build();
        ContentEntryDto contentEntry = ContentEntryDto.builder()
                .addItem(item)
                .build();

        controller.getRawValuesByFieldRank().put(1, new SimpleStringProperty("VAL1"));

        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(layoutObject.getProfiles().get(0));
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0L));
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(topicObject);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(TOPIC));

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0L, TOPIC)).thenReturn(of(contentEntry));


        // WHEN
        controller.updateAllPropertiesWithItemValues();

        // THEN
        assertThat(currentEntryLabelProperty.get()).isEqualTo("<?>");
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
        Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>();
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>());
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(topicObject);
        final EditorLayoutDto.EditorProfileDto profileObject = layoutObject.getProfiles().get(0);
        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(profileObject);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0L, TOPIC)).thenReturn(empty());

        // WHEN
        controller.applyProfile("Test profile");

        // THEN
        assertThat(currentTopicProperty.getValue()).isEqualTo(TOPIC);
        verify(mainStageControllerMock).setCurrentProfileObject(profileObject);
        verify(mainStageControllerMock).setCurrentTopicObject(topicObject);
    }

    @Test
    public void refreshAll_shouldResetProperties() {
        // GIVEN
        final Property<Long> currentEntryIndexProperty = new SimpleObjectProperty<>();
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(currentEntryIndexProperty);
        final EditorLayoutDto.EditorProfileDto profileObject = layoutObject.getProfiles().get(0);
        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(profileObject);
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn(topicObject);
        final Property<DbDto.Topic> currentTopicProperty = new SimpleObjectProperty<>(TOPIC);
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(currentTopicProperty);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0L, TOPIC)).thenReturn(empty());

        // WHEN
        controller.refreshAll();

        // THEN
        assertThat(currentEntryIndexProperty.getValue()).isEqualTo(0L);
        assertThat(controller.getResolvedValuesByFieldRank()).isEmpty();
        assertThat(controller.getResourcesByTopicLink()).isEmpty();
    }

    private EditorLayoutDto createLayoutObject() {
        EditorLayoutDto.EditorProfileDto profileObject = new EditorLayoutDto.EditorProfileDto("Test profile");
        profileObject.setTopic(TOPIC);
        EditorLayoutDto layoutObject = new EditorLayoutDto();
        layoutObject.getProfiles().add(profileObject);
        return layoutObject;
    }

    private DbDto createTopicObject() {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(TOPIC)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(INTEGER)
                                .build())
                        .build())
                .withData(DbDataDto.builder().build())
                .build();
    }
}
