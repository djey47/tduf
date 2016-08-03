package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.INTEGER;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class MainStageViewDataControllerTest {
    @Mock
    private MainStageController mainStageControllerMock;

    @Mock
    private ApplicationConfiguration applicationConfigurationMock;

    @Mock
    private BulkDatabaseMiner minerMock;

    @InjectMocks
    private MainStageViewDataController controller;


    @Before
    public void setUp() {
        DatabaseEditor.getCommandLineParameters().clear();

        when (mainStageControllerMock.getMiner()).thenReturn(minerMock);
    }

    @Test
    public void updateAllPropertiesWithItemValues_whenClassicFieldType_andNoLink_shouldUpdateCurrentEntryLabel () {
        // GIVEN
        final SimpleStringProperty currentEntryLabelProperty = new SimpleStringProperty("");
        DbDto currentTopicObject = DbDto.builder()
                .withStructure(DbStructureDto.builder().addItem(
                        DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(INTEGER)
                                .build())
                        .build())
                .build();
        ContentItemDto item = ContentItemDto.builder()
                .ofFieldRank(1)
                .build();
        ContentEntryDto contentEntry = ContentEntryDto.builder()
                .addItem(item)
                .build();
        EditorLayoutDto.EditorProfileDto currentProfileObject = new EditorLayoutDto.EditorProfileDto("Test profile");
        EditorLayoutDto currentLayoutObject = new EditorLayoutDto();
        currentLayoutObject.getProfiles().add(currentProfileObject);

        final HashMap<Integer, SimpleStringProperty> rawValueByFieldRank = new HashMap<>();
        rawValueByFieldRank.put(1, new SimpleStringProperty("VAL1"));

        Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> resourceListByTopicLink = new HashMap<>();

        when(mainStageControllerMock.getCurrentProfileObject()).thenReturn(currentProfileObject);
        when(mainStageControllerMock.getLayoutObject()).thenReturn(currentLayoutObject);
        when(mainStageControllerMock.getRawValuePropertyByFieldRank()).thenReturn(rawValueByFieldRank);
        when(mainStageControllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0L));
        when(mainStageControllerMock.getCurrentTopicObject()).thenReturn((currentTopicObject));
        when(mainStageControllerMock.getCurrentTopicProperty()).thenReturn(new SimpleObjectProperty<>(DbDto.Topic.CAR_PHYSICS_DATA));
        when(mainStageControllerMock.getCurrentLocaleProperty()).thenReturn(new SimpleObjectProperty<>(Locale.UNITED_STATES));
        when(mainStageControllerMock.getCurrentEntryLabelProperty()).thenReturn(currentEntryLabelProperty);
        when(mainStageControllerMock.getResourceListByTopicLink()).thenReturn(resourceListByTopicLink);

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0L, DbDto.Topic.CAR_PHYSICS_DATA)).thenReturn(of(contentEntry));


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
}
