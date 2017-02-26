package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Cockpit;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Cockpit_Back;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.BINOCULARS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class CameraInfoTest {

    @Test
    void buildCameraInfo_whenUsedViews_shouldMergeThem() {
        // GIVEN
        EnumMap<ViewProps, Object> view1Props = new EnumMap<>(ViewProps.class);
        view1Props.put(BINOCULARS, 20L);
        CameraViewEnhanced view1 = CameraViewEnhanced.fromProps(view1Props, Cockpit);
        EnumMap<ViewProps, Object> view2Props = new EnumMap<>(ViewProps.class);
        view2Props.put(BINOCULARS, 30L);
        CameraViewEnhanced view2 = CameraViewEnhanced.fromProps(view2Props, Cockpit_Back);
        CameraViewEnhanced view3 = CameraViewEnhanced.from(Cockpit, 101, Cockpit);
        CameraViewEnhanced view4 = CameraViewEnhanced.from(Cockpit_Back, 0, ViewKind.Unknown);
        List<CameraViewEnhanced> allViews = asList(view1, view2);
        List<CameraViewEnhanced> usedViews = asList(view3, view4);


        // WHEN
        CameraInfo actual = CameraInfo.builder()
                .forIdentifier(1000L)
                .withUsedViews(allViews, usedViews)
                .build();


        // THEN
        assertThat(actual.getCameraIdentifier()).isEqualTo(1000L);

        CameraViewEnhanced cockpitViewInfo = actual.getViewsByKind().get(Cockpit);
        assertThat(cockpitViewInfo.getKind()).isEqualTo(Cockpit);
        assertThat(cockpitViewInfo.getUsedCameraSetId()).isEqualTo(101L);
        assertThat(cockpitViewInfo.getUsedKind()).isEqualTo(Cockpit);
        assertThat(cockpitViewInfo.getSettings().get(BINOCULARS)).isEqualTo(20L);

        CameraViewEnhanced cockpitBackViewInfo = actual.getViewsByKind().get(Cockpit_Back);
        assertThat(cockpitBackViewInfo.getKind()).isEqualTo(Cockpit_Back);
        assertThat(cockpitBackViewInfo.getUsedCameraSetId()).isEqualTo(0L);
        assertThat(cockpitBackViewInfo.getUsedKind()).isNull();
        assertThat(cockpitBackViewInfo.getSettings().get(BINOCULARS)).isEqualTo(30L);
    }
}
