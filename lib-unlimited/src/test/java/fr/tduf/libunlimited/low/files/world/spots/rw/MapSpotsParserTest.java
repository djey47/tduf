package fr.tduf.libunlimited.low.files.world.spots.rw;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.world.spots.domain.MapSpotsInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class MapSpotsParserTest {
    private static final String THIS_CLASS_NAME = MapSpotsParserTest.class.getSimpleName();

    @BeforeAll
    static void globalSetUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @Test
    void parse_whenRealContents() throws IOException {
        // given
        byte[] bytes = FilesHelper.readBytesFromResourceFile("/spots/Hawai.spt");
        XByteArrayInputStream byteArrayInputStream = new XByteArrayInputStream(bytes);
        MapSpotsParser parser = MapSpotsParser.load(byteArrayInputStream);

        // when
        MapSpotsInfo actualInfo = parser.parse();

        // then
        Log.debug(THIS_CLASS_NAME, parser.dump());
        assertThat(actualInfo).isNotNull();
        assertThat(actualInfo.getMapSpots()).hasSize(480);
    }
}
