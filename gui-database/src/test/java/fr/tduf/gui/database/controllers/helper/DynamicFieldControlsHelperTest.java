package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.controllers.MainStageViewDataController;
import fr.tduf.gui.database.domain.ItemViewModel;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.PluginHandler;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.libtesting.common.helper.javafx.ApplicationTestHelper;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.INTEGER;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class DynamicFieldControlsHelperTest {
    @BeforeAll
    static void globalSetUp() {
        ApplicationTestHelper.initJavaFX();
    }

    @Mock
    private MainStageController controllerMock;

    @Mock
    private MainStageViewDataController viewDataMock;

    @Mock
    private PluginHandler pluginHandlerMock;

    @Mock
    private ApplicationConfiguration applicationConfigurationMock;

    @InjectMocks
    private DynamicFieldControlsHelper helper;

    @BeforeEach
    void setUp() {
        initMocks(this);

        when(controllerMock.getPluginHandler()).thenReturn(pluginHandlerMock);
        when(controllerMock.getViewData()).thenReturn(viewDataMock);
        when(controllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
        when(viewDataMock.getItemPropsByFieldRank()).thenReturn(new ItemViewModel());
    }

    @Test
    void addAllFieldsControls_whenNoFieldSettings_shouldDoNothing() {
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

        when(controllerMock.getCurrentTopicObject()).thenReturn(currentTopicObject);
        when(controllerMock.getLayoutObject()).thenReturn(layout);
        when(controllerMock.getViewData()).thenReturn(viewDataMock);
        when(viewDataMock.currentProfile()).thenReturn(new SimpleObjectProperty<>(profile));


        // WHEN-THEN
        helper.addAllFieldsControls(layout, profileName, CAR_PHYSICS_DATA);
    }

    @Test
    void addCustomControls_whenPluginNamePresent_andPluginsEnabled_shouldInvokeHandler() {
        // given
        HBox fieldBox = new HBox();

        EditorContext editorContext = new EditorContext();
        when(pluginHandlerMock.getEditorContext()).thenReturn(editorContext);
        when(applicationConfigurationMock.isEditorPluginsEnabled()).thenReturn(true);


        // when
        helper.addCustomControls(fieldBox, createField(), createFieldSettingsForPlugin(), CAR_PHYSICS_DATA, new SimpleStringProperty("RAW_VALUE"));


        // then
        verify(pluginHandlerMock).renderPluginByName(eq("PLUGIN"), any(OnTheFlyContext.class));
    }

    @Test
    void addCustomControls_whenPluginNamePresent_andPluginsDisabled_shouldNotInvokeHandler() {
        // given
        HBox fieldBox = new HBox();

        EditorContext editorContext = new EditorContext();
        when(pluginHandlerMock.getEditorContext()).thenReturn(editorContext);
        when(applicationConfigurationMock.isEditorPluginsEnabled()).thenReturn(false);


        // when
        helper.addCustomControls(fieldBox, createField(), createFieldSettingsForPlugin(), CAR_PHYSICS_DATA, new SimpleStringProperty("RAW_VALUE"));


        // then
        verifyNoInteractions(pluginHandlerMock);
    }

    @Test
    void addCustomControls_withoutPluginName_andPluginsEnabled_shouldNotInvokeHandler() {
        // given
        when(applicationConfigurationMock.isEditorPluginsEnabled()).thenReturn(false);

        // when
        helper.addCustomControls(new HBox(), createField(), new FieldSettingsDto(), CAR_PHYSICS_DATA, new SimpleStringProperty("RAW_VALUE"));

        // then
        verifyNoInteractions(pluginHandlerMock);
    }

    private static DbStructureDto.Field createField() {
        return DbStructureDto.Field.builder()
                .ofRank(1)
                .fromType(INTEGER)
                .build();
    }

    private FieldSettingsDto createFieldSettingsForPlugin() {
        FieldSettingsDto fieldSettings = new FieldSettingsDto();
        fieldSettings.setPluginName("PLUGIN");
        return fieldSettings;
    }
}
