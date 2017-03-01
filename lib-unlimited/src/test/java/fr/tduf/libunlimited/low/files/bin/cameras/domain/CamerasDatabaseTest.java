package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Cockpit;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Cockpit_Back;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class CamerasDatabaseTest {
    private CamerasDatabase camerasDatabase;

    @Test
    void removeSet_whenSetDoesNotExist_shouldDoNothing() {
        // given
        fillDatabase();

        // when
        camerasDatabase.removeSet(1002);

        // then
        assertThat(camerasDatabase.getIndexSize()).isEqualTo(2);
        assertThat(camerasDatabase.cameraSetExistsInIndex(1000)).isTrue();
        assertThat(camerasDatabase.cameraSetExistsInSettings(1000)).isTrue();
        assertThat(camerasDatabase.cameraSetExistsInIndex(1001)).isTrue();
        assertThat(camerasDatabase.cameraSetExistsInSettings(1001)).isTrue();
    }

    @Test
    void removeSet_whenSetExists_shouldRemoveFromIndexAndAllViews() {
        // given
        fillDatabase();

        // when
        camerasDatabase.removeSet(1000);

        // then
        assertThat(camerasDatabase.getIndexSize()).isEqualTo(1);
        assertThat(camerasDatabase.cameraSetExistsInIndex(1000)).isFalse();
        assertThat(camerasDatabase.cameraSetExistsInSettings(1000)).isFalse();
        assertThat(camerasDatabase.cameraSetExistsInIndex(1001)).isTrue();
        assertThat(camerasDatabase.cameraSetExistsInSettings(1001)).isTrue();
    }

    private void fillDatabase() {
        Map<Integer, Short> index = new HashMap<>(2);
        index.put(1000, (short) 2);
        index.put(1001, (short) 2);

        EnumMap<ViewProps, Object> emptySettings = new EnumMap<>(ViewProps.class);
        Map<Integer, List<CameraView>> viewsSettings = new HashMap<>(4);

        CameraView view11 = CameraView.fromProps(emptySettings, Cockpit, 1000);
        CameraView view21 = CameraView.fromProps(emptySettings, Cockpit_Back, 1000);
        List<CameraView> views1 = asList(view11, view21);
        viewsSettings.put(1000, views1);

        CameraView view12 = CameraView.fromProps(emptySettings, Cockpit, 1001);
        CameraView view22 = CameraView.fromProps(emptySettings, Cockpit_Back, 1001);

        List<CameraView> views2 = asList(view12, view22);
        viewsSettings.put(1001, views2);

        camerasDatabase = CamerasDatabase.builder()
                .withIndex(index)
                .withViews(viewsSettings)
                .build();
    }
}
