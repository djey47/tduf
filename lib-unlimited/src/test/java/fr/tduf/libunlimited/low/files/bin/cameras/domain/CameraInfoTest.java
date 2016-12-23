package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Cockpit;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Cockpit_Back;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Unknown;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.BINOCULARS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class CameraInfoTest {

    @Test
    void buildCameraInfo_whenUsedViews_shouldMergeThem() {
        // GIVEN
        EnumMap<ViewProps, Object> view1Props = new EnumMap<>(ViewProps.class);
        view1Props.put(ViewProps.TYPE, Cockpit);
        view1Props.put(BINOCULARS, 20L);
        CameraInfo.CameraView view1 = CameraInfo.CameraView.fromProps(view1Props);
        EnumMap<ViewProps, Object> view2Props = new EnumMap<>(ViewProps.class);
        view2Props.put(ViewProps.TYPE, Cockpit_Back);
        view2Props.put(BINOCULARS, 30L);
        CameraInfo.CameraView view2 = CameraInfo.CameraView.fromProps(view2Props);
        CameraInfo.CameraView view3 = CameraInfo.CameraView.from(Cockpit, 101L, Cockpit);
        CameraInfo.CameraView view4 = CameraInfo.CameraView.from(Cockpit_Back, 0, ViewKind.Unknown);
        List<CameraInfo.CameraView> allViews = asList(view1, view2);
        List<CameraInfo.CameraView> usedViews = asList(view3, view4);


        // WHEN
        CameraInfo actual = CameraInfo.builder()
                .forIdentifier(1000L)
                .withUsedViews(allViews, usedViews)
                .build();


        // THEN
        assertThat(actual.getCameraIdentifier()).isEqualTo(1000L);

        CameraInfo.CameraView cockpitViewInfo = actual.getViewsByKind().get(Cockpit);
        assertThat(cockpitViewInfo.getType()).isEqualTo(Cockpit);
        assertThat(cockpitViewInfo.getSourceCameraIdentifier()).isEqualTo(101L);
        assertThat(cockpitViewInfo.getSourceType()).isEqualTo(Cockpit);
        assertThat(cockpitViewInfo.getSettings().get(BINOCULARS)).isEqualTo(20L);

        CameraInfo.CameraView cockpitBackViewInfo = actual.getViewsByKind().get(Cockpit_Back);
        assertThat(cockpitBackViewInfo.getType()).isEqualTo(Cockpit_Back);
        assertThat(cockpitBackViewInfo.getSourceCameraIdentifier()).isEqualTo(0L);
        assertThat(cockpitBackViewInfo.getSourceType()).isNull();
        assertThat(cockpitBackViewInfo.getSettings().get(BINOCULARS)).isEqualTo(30L);
    }
}
