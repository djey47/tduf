package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.gui.database.plugins.common.PluginContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CamerasPluginTest {
    private CamerasPlugin camerasPlugin = new CamerasPlugin();

    @Test
    void onInit_whenNoCameras_shouldNotAttemptLoading() throws IOException {
        // given
        PluginContext context = new PluginContext();
        context.setDatabaseLocation(".");

        // when
        camerasPlugin.onInit(context);

        // then
        assertThat(context.getCamerasContext().isPluginLoaded()).isFalse();
    }
}
