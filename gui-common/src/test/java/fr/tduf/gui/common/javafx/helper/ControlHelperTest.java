package fr.tduf.gui.common.javafx.helper;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ControlHelperTest extends ApplicationTest {
    @Override
    public void start(Stage stage) throws Exception {}

    @Test
    void setTooltipText_whenNullControl_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> ControlHelper.setTooltipText(null, ""));
    }

    @Test
    void setTooltipText_shouldDefineTooltip() {
        // GIVEN
        Button button = new Button();
        String text = "tooltip text";

        // WHEN
        ControlHelper.setTooltipText(button, text);

        // THEN
        assertThat(button.getTooltip()).isNotNull();
        assertThat(button.getTooltip().getText()).isEqualTo(text);
    }
}
