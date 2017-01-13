package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.controllers.MainStageViewDataController;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.plugins.common.EditorContext;
import fr.tduf.gui.database.plugins.common.PluginHandler;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.INTEGER;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class DynamicFieldControlsHelperTest {
    @Mock
    private MainStageController controller;

    @Mock
    private MainStageViewDataController viewDataMock;

    @Mock
    private PluginHandler pluginHandlerMock;

    @InjectMocks
    private DynamicFieldControlsHelper helper;

    @BeforeEach
    void setUp() {
        initMocks(this);

        when(controller.getPluginHandler()).thenReturn(pluginHandlerMock);
    }

    @Test
    void addAllFieldsControls_whenNoFieldSettings_shouldDoNothing() throws Exception {
        // GIVEN
        String profileName = "Profile 1";
        EditorLayoutDto layout = new EditorLayoutDto();
        EditorLayoutDto.EditorProfileDto profile = new EditorLayoutDto.EditorProfileDto(profileName);
        layout.getProfiles().add(profile);

        DbDto currentTopicObject = DbDto.builder()
                .withStructure(DbStructureDto.builder().addItem(
                        createField())
                        .build())
                .build();

        when(controller.getCurrentTopicObject()).thenReturn(currentTopicObject);
        when(controller.getLayoutObject()).thenReturn(layout);
        when(controller.getViewData()).thenReturn(viewDataMock);
        when(viewDataMock.currentProfile()).thenReturn(new SimpleObjectProperty<>(profile));


        // WHEN-THEN
        helper.addAllFieldsControls(layout, profileName, CAR_PHYSICS_DATA);
    }

    @Test
    void addCustomControls_whenPluginNamePresent_shouldInvokeHandler() {
        // given
        FieldSettingsDto fieldSettings = new FieldSettingsDto();
        fieldSettings.setPluginName("PLUGIN");
        HBox fieldBox = new HBox();

        EditorContext editorContext = new EditorContext();
        when(pluginHandlerMock.getContext()).thenReturn(editorContext);


        // when
        helper.addCustomControls(fieldBox, createField(), fieldSettings, CAR_PHYSICS_DATA, new SimpleStringProperty("RAW_VALUE"));


        // then
        verify(pluginHandlerMock).renderPluginByName("PLUGIN", fieldBox);
    }

    @Test
    void addCustomControls_withoutPluginName_shouldNotInvokeHandler() {
        // given-when
        helper.addCustomControls(new HBox(), createField(), new FieldSettingsDto(), CAR_PHYSICS_DATA, new SimpleStringProperty("RAW_VALUE"));

        // then
        verifyZeroInteractions(pluginHandlerMock);
    }

    private static DbStructureDto.Field createField() {
        return DbStructureDto.Field.builder()
                .ofRank(1)
                .fromType(INTEGER)
                .build();
    }
}
