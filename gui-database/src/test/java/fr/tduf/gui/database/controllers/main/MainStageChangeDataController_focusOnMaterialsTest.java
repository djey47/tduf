package fr.tduf.gui.database.controllers.main;

import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialPiece;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialSettings;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Shader;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class MainStageChangeDataController_focusOnMaterialsTest {
    private BooleanProperty modifiedProperty;

    @Mock
    private MainStageController mainStageController;

    @InjectMocks
    private MainStageChangeDataController controller;

    @BeforeEach
    void setUp() {
        initMocks(this);

        modifiedProperty = new SimpleBooleanProperty(false);
        when(mainStageController.modifiedProperty()).thenReturn(modifiedProperty);
    }

    @Test
    void updateShaderConfiguration_shouldUpdateMaterialPiece_andMarkChangesMade() {
        // given
        Material material = Material.builder()
                .withGlobalSettings(MaterialSettings.builder()
                        .withShaderParameters(Shader.builder()
                                .withConfiguration(MaterialPiece.BLOC).build()).build()).build();

        // when
        controller.updateShaderConfiguration(material, MaterialPiece.SHADER_ASPHALT);

        // then
        assertThat(material.getProperties().getShader().getConfiguration()).isSameAs(MaterialPiece.SHADER_ASPHALT);
        assertThat(controller.modifiedProperty().getValue()).isTrue();
    }
}
