package fr.tduf.libunlimited.low.files.banks.mapping;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class MapHelperTest {

    //TODO map testing

    @Test
    public void computeChecksum_forGivenFileNames_shouldReturnMapHash() {
        // GIVEN
        String fileName1 = "avatar/barb.bnk";
        String fileName2 = "bnk1.map";
        String fileName3 = "frontend/hires/gauges/hud01.bnk";

        // WHEN
        Long checksum1 = MapHelper.computeChecksum(fileName1);
        Long checksum2 = MapHelper.computeChecksum(fileName2);
        Long checksum3 = MapHelper.computeChecksum(fileName3);

        // THEN
        assertThat(checksum1).isEqualTo(0xc48bdcaaL);
        assertThat(checksum2).isEqualTo(0xfe168a1cL);
        assertThat(checksum3).isEqualTo(0x0b6b3ea2L);
    }

}