package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.exceptions.StepException;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericStepTest {

    @Test(expected = StepException.class)
    public void start_whenExceptionInProcessMethod_shouldThrowStepException() throws StepException {
        // GIVEN
        GenericStep step = new GenericStep() {
            @Override
            protected void perform() throws IOException, ReflectiveOperationException {
                throw new IllegalAccessException("Illegal access!");
            }
        };

        // WHEN
        try {
            step.start();
        } catch (StepException se) {

            // THEN
            assertThat(se)
                    .hasCauseExactlyInstanceOf(IllegalAccessException.class)
                    .hasMessage("Current step could not be performed.");
            assertThat(se.getStepName()).isEqualTo("UNDEFINED");

            throw se;
        }
    }
}
