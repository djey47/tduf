package fr.tduf.gui.database.converter;

import com.esotericsoftware.minlog.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModifiedFlagToTitleConverterTest {

    private final ModifiedFlagToTitleConverter converter = new ModifiedFlagToTitleConverter();

    @BeforeEach
    void setUp() {
        Log.INFO();
    }

    @Test
    void toString_whenUnmodifiedState_shouldReturnSimpleTitle() {
        // given-when-then
        assertThat(converter.toString(false)).isEqualTo("TDUF Database Editor ");
    }

    @Test
    void toString_whenModifiedState_shouldReturnTitleWithAsterisk() {
        // given-when-then
        assertThat(converter.toString(true)).isEqualTo("TDUF Database Editor *");
    }

    @Test
    void toString_whenDebugLogLevel_shouldContainMode() {
        // given
        Log.DEBUG();

        // when-then
        assertThat(converter.toString(false)).contains("{DEBUG}");
    }

    @Test
    void toString_whenTraceLogLevel_shouldContainMode() {
        // given
        Log.TRACE();

        // when-then
        assertThat(converter.toString(false)).contains("{TRACE}");
    }
}
