package fr.tduf.libunlimited.low.files.world.spots.rw;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.world.spots.domain.MapSpotsInfo;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class MapSpotsParserTest {
    private static final String THIS_CLASS_NAME = MapSpotsParserTest.class.getSimpleName();

    @Test
    void parse_whenRealContents() throws IOException {
        // given
        byte[] bytes = FilesHelper.readBytesFromResourceFile("/spots/Hawai.spt");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        MapSpotsParser parser = MapSpotsParser.load(byteArrayInputStream);

        // when
        MapSpotsInfo actualInfo = parser.parse();

        // then
        Log.info(THIS_CLASS_NAME, parser.dump());
        assertThat(actualInfo).isNotNull();
        assertThat(actualInfo.getMapSpots()).hasSize(480);
    }
}
