package fr.tduf.gui.common.helper.javafx;

import fr.tduf.gui.common.rule.JavaFXThreadingRule;
import javafx.scene.control.Button;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ControlHelperTest {
    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    @Test(expected=NullPointerException.class)
    public void setTooltipText_whenNullControl_shouldThrowException() {
        // GIVEN-WHEN
        ControlHelper.setTooltipText(null, "");

        // THEN: NPE
    }

    @Test
    public void setTooltipText_shouldDefineTooltip() {
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