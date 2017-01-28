package fr.tduf.libunlimited.high.files.bin.cameras.patcher;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.SetChangeDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.ViewChangeDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;

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
            () -> camPatchGenerator.makePatch(null));
    }

    @Test
    void makePatch_whenEmptyCamerasInfo_shouldReturnEmptyPatch() {
        // given
        CamPatchGenerator camPatchGenerator = new CamPatchGenerator(new ArrayList<>(0));

        // when
        CamPatchDto actualPatchObject = camPatchGenerator.makePatch(ItemRange.ALL);

        // then
        assertThat(actualPatchObject.getComment()).isNotEmpty();
        assertThat(actualPatchObject.getChanges()).isEmpty();
    }

    @Test
    void makePatch_whenSetIdentifierNotInRange_shouldReturnEmptyPatch() {
        // given
        CameraInfo cameraInfo = CameraInfo.builder()
                .forIdentifier(1L)
                .build();
        CamPatchGenerator camPatchGenerator = new CamPatchGenerator(singletonList(cameraInfo));

        // when
        CamPatchDto actualPatchObject = camPatchGenerator.makePatch(ItemRange.fromSingleValue("10"));

        // then
        assertThat(actualPatchObject.getChanges()).isEmpty();
    }

    @Test
    void makePatch_whenSetIdentifierInRange_andSingleView_shouldReturnPatchWithSingleChange() {
        // given
        EnumMap<ViewProps, Object> viewProps = new EnumMap<>(ViewProps.class);
        viewProps.put(ViewProps.TYPE, ViewKind.Cockpit_Back);
        viewProps.put(ViewProps.BINOCULARS, 50L);
        CameraInfo cameraInfo = CameraInfo.builder()
                .forIdentifier(1L)
                .addView(CameraInfo.CameraView.fromProps(viewProps))
                .build();
        CamPatchGenerator camPatchGenerator = new CamPatchGenerator(singletonList(cameraInfo));

        // when
        CamPatchDto actualPatchObject = camPatchGenerator.makePatch(ItemRange.fromSingleValue("1"));

        // then
        assertThat(actualPatchObject.getChanges()).hasSize(1);
        SetChangeDto singleChange = actualPatchObject.getChanges().get(0);
        assertThat(singleChange.getChanges()).hasSize(1);
        ViewChangeDto viewChange = singleChange.getChanges().get(0);
        assertThat(viewChange.getCameraViewKind()).isEqualTo(ViewKind.Cockpit_Back);
        assertThat(viewChange.getViewProps()).containsKeys(ViewProps.TYPE, ViewProps.BINOCULARS);
        assertThat(viewChange.getViewProps()).containsValues("Cockpit_Back", "50");
    }
}
