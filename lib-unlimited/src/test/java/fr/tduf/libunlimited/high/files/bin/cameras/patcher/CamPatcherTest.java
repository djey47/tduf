package fr.tduf.libunlimited.high.files.bin.cameras.patcher;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CamPatcherTest {

    @Test
    void new_whenNullCamerasInfo_shouldThrowException() {
        // given-when-then
        assertThrows(NullPointerException.class,
                () -> new CamPatcher(null));
    }

    @Test
    void apply_whenNullPatchObject_shouldThrowException() {
        // given-when
        CamPatcher camPatcher = new CamPatcher(new ArrayList<>(0));

        // then
        assertThrows(NullPointerException.class,
                () -> camPatcher.apply(null));
    }
}
