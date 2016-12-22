package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Cockpit;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Cockpit_Back;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Unknown;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class CameraInfoTest {

    @Test
    void buildCameraInfo_whenUsedViews_shouldMergeThem() {
        // GIVEN
        CameraInfo.CameraView view1 = CameraInfo.CameraView.from(Cockpit, 0, ViewKind.Unknown);
        CameraInfo.CameraView view2 = CameraInfo.CameraView.from(Cockpit_Back, 0, ViewKind.Unknown);
        CameraInfo.CameraView view3 = CameraInfo.CameraView.from(Cockpit, 101L, Cockpit);;
        CameraInfo.CameraView view4 = CameraInfo.CameraView.from(Cockpit_Back, 0, ViewKind.Unknown);;
        List<CameraInfo.CameraView> allViews = asList(view1, view2);
        List<CameraInfo.CameraView> usedViews = asList(view3, view4);

        // WHEN
        CameraInfo actual = CameraInfo.builder()
                .forIdentifier(1000L)
                .withUsedViews(allViews, usedViews)
                .build();

        // THEN
        // TODO test settings
        assertThat(actual.getCameraIdentifier()).isEqualTo(1000L);
        assertThat(actual.getViewsByKind().get(Cockpit).getType()).isEqualTo(Cockpit);
        assertThat(actual.getViewsByKind().get(Cockpit).getSourceCameraIdentifier()).isEqualTo(101L);
        assertThat(actual.getViewsByKind().get(Cockpit).getSourceType()).isEqualTo(Cockpit);
        assertThat(actual.getViewsByKind().get(Cockpit_Back).getType()).isEqualTo(Cockpit_Back);
        assertThat(actual.getViewsByKind().get(Cockpit_Back).getSourceCameraIdentifier()).isEqualTo(0L);
        assertThat(actual.getViewsByKind().get(Cockpit_Back).getSourceType()).isEqualTo(Unknown);
    }
}