package fr.tduf.libunlimited.low.files.banks.mapping.helper;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class MapHelperTest {

    private static Class<MapHelperTest> thisClass = MapHelperTest.class;

    @Test
    public void parseBanks_whenEmptyFiles_shouldReturnFileNameList() throws URISyntaxException, IOException {
        // GIVEN
        Path path = Paths.get(thisClass.getResource("/banks/Bnk1.map").toURI());
        String bnkFolderName = path.getParent().toString();
        List<String> expectedFileList = createExpectedFileList();

        // WHEN
        List<String> actualFileList = MapHelper.parseBanks(bnkFolderName);

        // THEN
        assertThat(actualFileList).isNotNull();
        assertThat(actualFileList).hasSize(5);
        assertThat(actualFileList).contains(expectedFileList.toArray(new String[expectedFileList.size()]));
    }

    @Test
    public void computeChecksums_whenEmptyFiles_shouldReturnAllChecksums() {
        // GIVEN
        List<String> files = createExpectedFileList();

        // WHEN
        Map<Long, String> checksums = MapHelper.computeChecksums(files);

        // THEN
        assertThat(checksums).isNotNull();
        assertThat(checksums).hasSameSizeAs(files);
        assertThat(checksums.get(0xc48bdcaaL)).isEqualTo("avatar/barb.bnk");
        assertThat(checksums.get(0xfe168a1cL)).isEqualTo("bnk1.map");
        assertThat(checksums.get(0x0b6b3ea2L)).isEqualTo("frontend/hires/gauges/hud01.bnk");
    }

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

    @Test
    public void findNewChecksums_forGivenValues_shouldReturnDifferences() {
        // GIVEN
        BankMap bankMap = new BankMap();
        bankMap.addMagicEntry(0xc48bdcaaL);
        bankMap.addMagicEntry(0x0b6b3ea2L);

        Map<Long, String> existingChecksums = new HashMap<>();
        existingChecksums.put(0xc48bdcaaL, "avatar/barb.bnk");
        existingChecksums.put(0xfe168a1cL, "bnk1.map");
        existingChecksums.put(0x0b6b3ea2L, "frontend/hires/gauges/hud01.bnk");


        // WHEN
        Map<Long, String> newChecksums = MapHelper.findNewChecksums(bankMap, existingChecksums);


        // THEN
        assertThat(newChecksums).isNotNull();
        assertThat(newChecksums).hasSize(1);
        assertThat(newChecksums.containsKey(0xfe168a1cL)).isTrue();
        assertThat(newChecksums.containsValue("bnk1.map")).isTrue();
    }

    private static List<String> createExpectedFileList() {
        return asList(
                "Bnk1.map",
                String.format("Avatar%1$sBARB.BNK", File.separator),
                String.format("FrontEnd%1$sHires%1$sGauges%1$shud01.bnk", File.separator),
                String.format("Vehicules%1$sA3_V6.bnk", File.separator));
    }
}