package fr.tduf.gui.database.plugins.cameras.converter;


import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CameraViewToItemConverterTest {
    private final CameraViewToItemConverter cameraViewToItemConverter = new CameraViewToItemConverter();

    @Test
    void testToString_whenNullCameraView_shouldReturnEmptyString() {
        // given-when-then
        assertThat(cameraViewToItemConverter.toString(null)).isEmpty();
    }

    @Test
    void testToString_shouldReturnCameraViewDescription() {
        // given
        CameraView cameraView = CameraView.builder()
                .ofKind(ViewKind.Bumper)
                .build();

        // when-then
        assertThat(cameraViewToItemConverter.toString(cameraView)).isEqualTo("(22) Bumper");
    }
}
