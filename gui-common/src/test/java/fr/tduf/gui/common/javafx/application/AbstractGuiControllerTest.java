package fr.tduf.gui.common.javafx.application;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbstractGuiControllerTest extends ApplicationTest {

    private final TestingGuiController controller = new TestingGuiController();

    static class TestingGuiController extends AbstractGuiController {

        private boolean initCalled;
        private boolean initErrorEnabled;

        @Override
        protected void init() throws IOException {
            if (initErrorEnabled) {
                throw new IOException("Init exception");
            }
            initCalled = true;
        }

        public boolean isInitCalled() {
            return initCalled;
        }

        public void enableInitError() {
            initErrorEnabled = true;
        }
    }

    @Test
    void initialize_shouldInvokeInit() {
        // given-when
        controller.initialize(null, null);

        // then
        assertThat(controller.isInitCalled()).isTrue();
    }

    @Test
    void initialize_whenInitError_shouldCThrowException() {
        // given
        controller.enableInitError();

        // when-then
        RuntimeException actualException = assertThrows(RuntimeException.class,
                () -> controller.initialize(null, null));
        assertThat(actualException).hasMessage("Window initializing failed.");
    }

    @Test
    void getWindow_whenNoRoot_shouldReturnNull() {
        // given-when-then
        assertThat(controller.getWindow()).isNull();
    }

    @Test
    void getStage_whenNoWindow_shouldThrowException() {
        // given-when-then
        NullPointerException actualException = assertThrows(NullPointerException.class,
                controller::getStage);
        assertThat(actualException).hasMessage("Window has not been initialized yet.");

    }

    @Test
    void rootCursorPropertywhenNoRoot_shouldThrowException() {
        // given-when-then
        NullPointerException actualException = assertThrows(NullPointerException.class,
                controller::rootCursorProperty);
        assertThat(actualException).hasMessage("Root node is not available yet.");
    }
}