package fr.tduf.libunlimited.low.files.world.spots.rw;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.world.spots.domain.SectorSpotsInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class SectorSpotsParserTest {
    private static final String THIS_CLASS_NAME = SectorSpotsParserTest.class.getSimpleName();

    @BeforeAll
    static void globalSetUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @Test
    void parse_whenRealContents() throws IOException {
        // given
        byte[] file1Bytes = FilesHelper.readBytesFromResourceFile("/spots/Sector-0-2-0-7.spt");
        byte[] file2Bytes = FilesHelper.readBytesFromResourceFile("/spots/Sector-9-7-7-6.spt");
        SectorSpotsParser parser1 = SectorSpotsParser.load(new XByteArrayInputStream(file1Bytes));
        SectorSpotsParser parser2 = SectorSpotsParser.load(new XByteArrayInputStream(file2Bytes));

        // when
        SectorSpotsInfo actualInfo1 = parser1.parse();
        SectorSpotsInfo actualInfo2 = parser2.parse();

        // then
        Log.debug(THIS_CLASS_NAME, parser1.dump());
        Log.debug(THIS_CLASS_NAME, parser2.dump());

        assertThat(actualInfo1).isNotNull();
        assertThat(actualInfo1.getSectorSpots()).hasSize(1);
        assertThat(actualInfo2).isNotNull();
        assertThat(actualInfo2.getSectorSpots()).hasSize(3);
    }
}
