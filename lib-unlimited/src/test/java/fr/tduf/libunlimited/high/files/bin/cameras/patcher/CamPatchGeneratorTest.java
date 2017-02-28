package fr.tduf.libunlimited.high.files.bin.cameras.patcher;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.SetChangeDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.ViewChangeDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraSetInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Cockpit_Back;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CamPatchGeneratorTest {
    @Test
    void new_whenNullCamerasInfo_shouldThrowException() {
        // given-when-then
        assertThrows(NullPointerException.class,
            () -> new CamPatchGenerator(null));
    }

    @Test
    void makePatch_whenNullRange_shouldThrowException() {
        // given
        CamPatchGenerator camPatchGenerator = new CamPatchGenerator(new ArrayList<>(0));

        // when-then
        assertThrows(NullPointerException.class,
            () -> camPatchGenerator.makePatch(null, null));
    }

    @Test
    void makePatch_whenEmptyCamerasInfo_shouldReturnEmptyPatch() {
        // given
        CamPatchGenerator camPatchGenerator = new CamPatchGenerator(new ArrayList<>(0));

        // when
        CamPatchDto actualPatchObject = camPatchGenerator.makePatch(ItemRange.ALL, ItemRange.ALL);

        // then
        assertThat(actualPatchObject.getComment()).isNotEmpty();
        assertThat(actualPatchObject.getChanges()).isEmpty();
    }

    @Test
    void makePatch_whenSetIdentifierNotInRange_shouldReturnEmptyPatch() {
        // given
        CameraSetInfo cameraSetInfo = CameraSetInfo.builder()
                .forIdentifier(1)
                .build();
        CamPatchGenerator camPatchGenerator = new CamPatchGenerator(singletonList(cameraSetInfo));

        // when
        CamPatchDto actualPatchObject = camPatchGenerator.makePatch(ItemRange.fromSingleValue("10"), ItemRange.ALL);

        // then
        assertThat(actualPatchObject.getChanges()).isEmpty();
    }

    @Test
    void makePatch_whenSetIdentifierInRange_andSingleView_shouldReturnPatchWithSingleChange() {
        // given
        EnumMap<ViewProps, Object> viewProps = new EnumMap<>(ViewProps.class);
        viewProps.put(ViewProps.BINOCULARS, 50L);
        CameraSetInfo cameraSetInfo = CameraSetInfo.builder()
                .forIdentifier(1)
                .addView(CameraView.fromProps(viewProps, Cockpit_Back))
                .build();
        CamPatchGenerator camPatchGenerator = new CamPatchGenerator(singletonList(cameraSetInfo));

        // when
        CamPatchDto actualPatchObject = camPatchGenerator.makePatch(ItemRange.fromSingleValue("1"), ItemRange.ALL);

        // then
        assertThat(actualPatchObject.getChanges()).hasSize(1);
        SetChangeDto singleChange = actualPatchObject.getChanges().get(0);
        assertThat(singleChange.getChanges()).hasSize(1);
        ViewChangeDto viewChange = singleChange.getChanges().get(0);
        assertThat(viewChange.getCameraViewKind()).isEqualTo(Cockpit_Back);
        assertThat(viewChange.getViewProps()).containsKey(ViewProps.BINOCULARS);
        assertThat(viewChange.getViewProps()).containsValue("50");
    }

    @Test
    void makePatch_whenSetIdentifierInRange_butViewNotInRange_shouldReturnPatchWithoutViewChanges() {
        // given
        EnumMap<ViewProps, Object> viewProps = new EnumMap<>(ViewProps.class);
        viewProps.put(ViewProps.BINOCULARS, 50L);
        CameraSetInfo cameraSetInfo = CameraSetInfo.builder()
                .forIdentifier(1)
                .addView(CameraView.fromProps(viewProps, Cockpit_Back))
                .build();
        CamPatchGenerator camPatchGenerator = new CamPatchGenerator(singletonList(cameraSetInfo));

        // when
        CamPatchDto actualPatchObject = camPatchGenerator.makePatch(ItemRange.fromSingleValue("1"), ItemRange.fromSingleValue(ViewKind.Bumper.name()));

        // then
        assertThat(actualPatchObject.getChanges()).hasSize(1);
        assertThat(actualPatchObject.getChanges().get(0).getChanges()).isEmpty();
    }
}
