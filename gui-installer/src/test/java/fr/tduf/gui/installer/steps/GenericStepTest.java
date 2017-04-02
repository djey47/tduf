package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.exceptions.StepException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenericStepTest {

    @Test
    void start_whenExceptionInProcessMethod_shouldThrowStepException() throws StepException {
        // GIVEN
        GenericStep step = new GenericStep() {
            @Override
            protected void perform() throws IOException, ReflectiveOperationException {
                throw new IllegalAccessException("Illegal access!");
            }
        };

        // WHEN-THEN
        StepException actualException = assertThrows(StepException.class,
                step::start);
        assertThat(actualException)
                .hasCauseExactlyInstanceOf(IllegalAccessException.class)
                .hasMessage("Current step could not be performed");
        assertThat(actualException.getStepName()).isEqualTo("UNDEFINED");
    }
}
