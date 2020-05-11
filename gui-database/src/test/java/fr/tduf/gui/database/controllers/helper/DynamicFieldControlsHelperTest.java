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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.INTEGER;
import static org.assertj.core.api.Assertions.assertThat;
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

    @Captor
    private ArgumentCaptor<OnTheFlyContext> onTheFlyContextCaptor;

    @InjectMocks
    private DynamicFieldControlsHelper helper;

    @BeforeEach
    void setUp() {
        initMocks(this);

        when(controllerMock.getPluginHandler()).thenReturn(pluginHandlerMock);
        when(controllerMock.getViewData()).thenReturn(viewDataMock);
        when(controllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
        when(viewDataMock.getItemPropsByFieldRank()).thenReturn(new ItemViewModel());
        when(pluginHandlerMock.getEditorContext()).thenReturn(new EditorContext());
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
    void addCustomControls_whenPluginNamePresent_andPluginsEnabled_shouldInvokeHandler_withProvisionedContext() {
        // given
        HBox fieldBox = new HBox();
        when(applicationConfigurationMock.isEditorPluginsEnabled()).thenReturn(true);
        when(controllerMock.getCurrentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(1));

        // when
        helper.addCustomControls(fieldBox, createField(), createFieldSettingsForPlugin(), CAR_PHYSICS_DATA, new SimpleStringProperty("RAW_VALUE"));

        // then
        verify(pluginHandlerMock).renderPluginByName(eq("PLUGIN"), onTheFlyContextCaptor.capture());
        OnTheFlyContext actualContext = onTheFlyContextCaptor.getValue();
        assertThat(actualContext.getContentEntryIndexProperty().getValue()).isEqualTo(1);
        assertThat(actualContext.getCurrentTopic()).isEqualTo(CAR_PHYSICS_DATA);
        assertThat(actualContext.getRemoteTopic()).isEqualTo(CAR_PHYSICS_DATA);
        assertThat(actualContext.getFieldRank()).isEqualTo(1);
        assertThat(actualContext.isFieldReadOnly()).isFalse();
        assertThat(actualContext.getRawValueProperty().getValue()).isEqualTo("RAW_VALUE");
        assertThat(actualContext.getParentPane()).isSameAs(fieldBox);
        assertThat(actualContext.getErrorProperty().get()).isFalse();
        assertThat(actualContext.getErrorMessageProperty()).isNotNull();
    }

    @Test
    void addCustomControls_whenPluginNamePresent_andPluginsDisabled_shouldNotInvokeHandler() {
        // given
        HBox fieldBox = new HBox();
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
        fieldSettings.setRank(1);
        return fieldSettings;
    }
}
