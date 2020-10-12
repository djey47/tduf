package fr.tduf.libunlimited.framework.io;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XByteArrayInputStreamTest {

    private final XByteArrayInputStream xByteArrayInputStream = new XByteArrayInputStream(new byte[] { 1, 2, 3});

    @Test
    void position_atBeginning_shouldReturnZero() {
        // given-when-then
        assertThat(xByteArrayInputStream.position()).isEqualTo(0);
    }

    @Test
    void seek_shouldChangePosition() {
        // given-when
        xByteArrayInputStream.seek(1);

        // then
        assertThat(xByteArrayInputStream.position()).isEqualTo(1);
    }

    @Test
    void seek_whenOutOfBounds_shouldThrowException() {
        // given-when-then
        assertThatThrownBy(() ->xByteArrayInputStream.seek(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Seeking to invalid position (-1). Valid positions are 0..2");
        assertThatThrownBy(() ->xByteArrayInputStream.seek(3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Seeking to invalid position (3). Valid positions are 0..2");
    }
}