package fr.tduf.gui.database.controllers.main;

import fr.tduf.libunlimited.low.files.gfx.materials.domain.*;
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
    @Mock
    private MainStageController mainStageController;

    @InjectMocks
    private MainStageChangeDataController controller;

    @BeforeEach
    void setUp() {
        initMocks(this);

        BooleanProperty modifiedProperty = new SimpleBooleanProperty(false);
        when(mainStageController.modifiedProperty()).thenReturn(modifiedProperty);
    }

    @Test
    void updateShaderConfiguration_whenValueChanged_shouldUpdateMaterialPiece_andMarkChangesMade() {
        // given
        Material material = createMaterial();

        // when
        controller.updateShaderConfiguration(material, MaterialPiece.SHADER_ASPHALT);

        // then
        assertThat(material.getProperties().getShader().getConfiguration()).isSameAs(MaterialPiece.SHADER_ASPHALT);
        assertThat(controller.modifiedProperty().getValue()).isTrue();
    }

    @Test
    void updateShaderConfiguration_whenValueDidNotChange_shouldNotMarkChangesMade() {
        // given
        Material material = createMaterial();

        // when
        controller.updateShaderConfiguration(material, MaterialPiece.BLOC);

        // then
        assertThat(controller.modifiedProperty().getValue()).isFalse();
    }

    @Test
    void updateMaterialColor_whenAColorChanged() {
        // given
        Material material = createMaterial();
        Color newColor = Color.builder()
                .ofKind(Color.ColorKind.AMBIENT)
                .fromRGB(0.0f, 0.0f, 0.0f)
                .withOpacity(0.0f).build();

        // when
        controller.updateMaterialColor(material, newColor);

        // then
        assertThat(material.getProperties().getAmbientColor()).isEqualTo(newColor);
        assertThat(controller.modifiedProperty().getValue()).isTrue();
    }

    @Test
    void updateMaterialColor_whenColorDidNotChange() {
        // given
        Material material = createMaterial();
        Color newColor = Color.builder()
                .ofKind(Color.ColorKind.AMBIENT)
                .fromRGB(1.0f, 1.0f, 1.0f)
                .withOpacity(1.0f).build();

        // when
        controller.updateMaterialColor(material, newColor);

        // then
        assertThat(controller.modifiedProperty().getValue()).isFalse();
    }

    private static Material createMaterial() {
        return Material.builder()
                .withGlobalSettings(MaterialSettings.builder()
                        .withAmbientColor(Color.builder()
                                .ofKind(Color.ColorKind.AMBIENT)
                                .fromRGB(1.0f, 1.0f, 1.0f)
                                .withOpacity(1.0f).build())
                        .withShaderParameters(Shader.builder()
                                .withConfiguration(MaterialPiece.BLOC).build()).build()).build();
    }
}
