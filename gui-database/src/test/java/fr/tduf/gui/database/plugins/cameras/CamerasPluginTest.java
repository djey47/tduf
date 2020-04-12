package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.gui.database.plugins.common.EditorContext;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class CamerasPluginTest {
    private final CamerasPlugin camerasPlugin = new CamerasPlugin();
    private final EditorContext context = new EditorContext();

    @Test
    void onInit_whenNoCameras_shouldNotAttemptLoading() throws IOException {
        // given
        context.setDatabaseLocation(".");

        // when
        camerasPlugin.onInit(context);

        // then
        assertThat(context.getCamerasContext().isPluginLoaded()).isFalse();
    }

    @Test
    void onSave_whenNoCamerasLoaded_shouldNotAttemptSaving() throws IOException {
        // given-when-then
        camerasPlugin.onSave(context);
    }

    @Test
    void renderControls_whenNoCamerasLoaded_shouldReturnEmptyComponent() throws IOException {
        // given
        context.getCamerasContext().setPluginLoaded(false);

        // when
        Node actualNode = camerasPlugin.renderControls(context);

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
