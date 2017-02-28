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

class CameraSetInfoTest {

    @Test
    void buildCameraInfo_whenUsedViews_shouldMergeThem() {
        // GIVEN
        EnumMap<ViewProps, Object> view1Props = new EnumMap<>(ViewProps.class);
        view1Props.put(BINOCULARS, 20L);
        CameraView view1 = CameraView.fromProps(view1Props, Cockpit, 1000);
        EnumMap<ViewProps, Object> view2Props = new EnumMap<>(ViewProps.class);
        view2Props.put(BINOCULARS, 30L);
        CameraView view2 = CameraView.fromProps(view2Props, Cockpit_Back, 1000);
        CameraView view3 = CameraView.from(Cockpit, 101, Cockpit);
        CameraView view4 = CameraView.from(Cockpit_Back, 0, Unknown);
        List<CameraView> allViews = asList(view1, view2);
        List<CameraView> usedViews = asList(view3, view4);


        // WHEN
        CameraSetInfo actual = CameraSetInfo.builder()
                .forIdentifier(1000)
                .withUsedViews(allViews, usedViews)
                .build();


        // THEN
        assertThat(actual.getCameraIdentifier()).isEqualTo(1000);

        CameraView cockpitViewInfo = actual.getViewsByKind().get(Cockpit);
        assertThat(cockpitViewInfo.getKind()).isEqualTo(Cockpit);
        assertThat(cockpitViewInfo.getUsedCameraSetId()).isEqualTo(101);
        assertThat(cockpitViewInfo.getUsedKind()).isEqualTo(Cockpit);
        assertThat(cockpitViewInfo.getSettings().get(BINOCULARS)).isEqualTo(20L);

        CameraView cockpitBackViewInfo = actual.getViewsByKind().get(Cockpit_Back);
        assertThat(cockpitBackViewInfo.getKind()).isEqualTo(Cockpit_Back);
        assertThat(cockpitBackViewInfo.getUsedCameraSetId()).isNull();
        assertThat(cockpitBackViewInfo.getUsedKind()).isNull();
        assertThat(cockpitBackViewInfo.getSettings().get(BINOCULARS)).isEqualTo(30L);
    }
}
