package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CamerasPluginTest {
    private final CamerasPlugin camerasPlugin = new CamerasPlugin();
    private final EditorContext editorContext = new EditorContext();

    @BeforeEach
    void setUp() {
        camerasPlugin.setEditorContext(editorContext);
    }

    @Test
    void onInit_whenNoCameras_shouldThrowException_andNotAttemptLoading() {
        // given
        editorContext.setDatabaseLocation(".");

        // when-then
        assertThrows(IOException.class, () -> camerasPlugin.onInit("CAMERAS", editorContext));
        assertThat(camerasPlugin.getCamerasContext().isPluginLoaded()).isFalse();
    }

    @Test
    void onSave_whenNoCamerasLoaded_shouldNotAttemptSaving() throws IOException {
        // given-when-then
        camerasPlugin.onSave();
    }

    @Test
    void renderControls_whenNoCamerasLoaded_shouldReturnEmptyComponent() {
        // given
        camerasPlugin.getCamerasContext().setPluginLoaded(false);
        OnTheFlyContext onTheFlyContext = new OnTheFlyContext();

        // when
        Node actualNode = camerasPlugin.renderControls(onTheFlyContext);

        // then
        assertThat(actualNode).isInstanceOf(HBox.class);
        assertThat(((HBox) actualNode).getChildren()).isEmpty();
    }

    @Test
    void determineInitialCameraView_whenPreviousViewSelected_shouldReturnViewWithSameKind() {
        // given
        final CameraView previousView = CameraView.builder().ofKind(ViewKind.Hood).build();
        final CameraView availableView1 = CameraView.builder().ofKind(ViewKind.Bumper).build();
        final CameraView availableView2 = CameraView.builder().ofKind(ViewKind.Hood).build();
        List<CameraView> sortedViews = asList(availableView1, availableView2);

        // when
        Optional<CameraView> actual = CamerasPlugin.determineInitialCameraView(sortedViews, previousView);

        // then
        assertThat(actual).isPresent();
        final CameraView actualView = actual.get();
        assertThat(actualView).isSameAs(availableView2);
        assertThat(actualView).isNotSameAs(previousView);
    }

    @Test
    void determineInitialCameraView_whenNoPreviousViewSelected_shouldReturnFirstAvailableView() {
        // given
        final CameraView availableView1 = CameraView.builder().ofKind(ViewKind.Bumper).build();
        final CameraView availableView2 = CameraView.builder().ofKind(ViewKind.Hood).build();
        List<CameraView> sortedViews = asList(availableView1, availableView2);

        // when
        Optional<CameraView> actual = CamerasPlugin.determineInitialCameraView(sortedViews, null);

        // then
        assertThat(actual).isPresent();
        final CameraView actualView = actual.get();
        assertThat(actualView).isSameAs(availableView1);
    }

    @Test
    void determineInitialCameraView_whenPreviousViewSelected_andNoMatchingView_shouldReturnFirstAvailableView() {
        // given
        final CameraView previousView = CameraView.builder().ofKind(ViewKind.Hood).build();
        final CameraView availableView1 = CameraView.builder().ofKind(ViewKind.Bumper).build();
        final CameraView availableView2 = CameraView.builder().ofKind(ViewKind.Cockpit).build();
        List<CameraView> sortedViews = asList(availableView1, availableView2);

        // when
        Optional<CameraView> actual = CamerasPlugin.determineInitialCameraView(sortedViews, previousView);

        // then
        assertThat(actual).isPresent();
        final CameraView actualView = actual.get();
        assertThat(actualView).isSameAs(availableView1);
        assertThat(actualView).isNotSameAs(previousView);
    }

    @Test
    void determineInitialCameraView_whenNoPreviousViewSelected_andNoAvailableViews_shouldReturnEmpty() {
        // given-when
        Optional<CameraView> actual = CamerasPlugin.determineInitialCameraView(emptyList(), null);

        // then
        assertThat(actual).isEmpty();
    }
}
