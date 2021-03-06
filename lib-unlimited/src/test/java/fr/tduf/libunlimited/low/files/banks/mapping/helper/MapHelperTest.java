package fr.tduf.libunlimited.low.files.banks.mapping.helper;

import fr.tduf.libtesting.common.helper.TestingFilesHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import org.junit.jupiter.api.Test;

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

class MapHelperTest {
    private static final Class<MapHelperTest> thisClass = MapHelperTest.class;

    @Test
    void parseBanks_whenEmptyFiles_shouldReturnFileNameList() throws URISyntaxException, IOException {
        // GIVEN
        Path path = Paths.get(thisClass.getResource("/banks/Bnk1.no.magic.map").toURI());
        String bnkFolderName = path.getParent().toString();
        List<String> expectedFileList = createExpectedFileList();

        // WHEN
        List<String> actualFileList = MapHelper.parseBanks(bnkFolderName);

        // THEN
        assertThat(actualFileList)
                .isNotNull()
                .hasSameSizeAs(expectedFileList)
                .contains(expectedFileList.toArray(new String[0]));
    }

    @Test
    void computeChecksums_whenEmptyFiles_shouldReturnAllChecksums() {
        // GIVEN
        List<String> files = createExpectedFileList();

        // WHEN
        Map<Long, String> checksums = MapHelper.computeChecksums(files);

        // THEN
        assertThat(checksums)
                .isNotNull()
                .hasSameSizeAs(files);
        assertThat(checksums.get(0xc48bdcaaL)).isEqualTo("Avatar/BARB.BNK");
        assertThat(checksums.get(0xe5b1ee5fL)).isEqualTo("Bnk1.no.magic.map");
        assertThat(checksums.get(0x0b6b3ea2L)).isEqualTo("FrontEnd/Hires/Gauges/hud01.bnk");
    }

    @Test
    void computeChecksum_forGivenFileNames_shouldReturnMapHash() {
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
    void computeChecksum_forGivenFileName_andExtendedCharacter_shouldReturnMapHash() {
        // GIVEN
        String fileName = "bnk1ï.map";

        // WHEN
        Long checksum = MapHelper.computeChecksum(fileName);

        // THEN
        assertThat(checksum).isEqualTo(0x68a63bffL);
    }

    @Test
    void findNewChecksums_forGivenValues_shouldReturnDifferences() {
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
        assertThat(newChecksums)
                .isNotNull()
                .hasSize(1);
        assertThat(newChecksums.containsKey(0xfe168a1cL)).isTrue();
        assertThat(newChecksums.containsValue("bnk1.map")).isTrue();
    }

    @Test
    void hasEntryForPath_whenEntryExists() {
        // given
        BankMap bankMap = new BankMap();
        bankMap.addMagicEntry(0xc48bdcaaL);

        // when-then
        assertThat(MapHelper.hasEntryForPath(bankMap, "avatar/barb.bnk")).isTrue();
    }

    @Test
    void hasEntryForPath_whenEntryDoesNotExist() {
        // given-when-then
        assertThat(MapHelper.hasEntryForPath(new BankMap(), "avatar/barb.bnk")).isFalse();
    }
    
    @Test
    void registerPath() {
        // given
        BankMap bankMap = new BankMap();
        
        // when
        MapHelper.registerPath(bankMap, "avatar/barb.bnk");
        
        // then
        assertThat(MapHelper.hasEntryForPath(bankMap, "avatar/barb.bnk")).isTrue();
        assertThat(MapHelper.hasEntryForPath(bankMap, "avatar/barb1.bnk")).isFalse();
    }
    
    @Test
    void saveBankMap() throws IOException {
        // given
        String outputFile = Paths.get(TestingFilesHelper.createTempDirectoryForLibrary(), "bnk1.map").toString();

        // when
        MapHelper.saveBankMap(new BankMap(), outputFile);
        
        // then
        assertThat(new File(outputFile)).exists();
    }

    private static List<String> createExpectedFileList() {
        return asList(
                "Bnk1.no.magic.map",
                "Bnk1-enhanced1.map",
                "Bnk1-enhanced2.map",
                String.format("Avatar%1$sBARB.BNK", File.separator),
                String.format("FrontEnd%1$sHires%1$sGauges%1$shud01.bnk", File.separator),
                String.format("Vehicules%1$sA3_V6.bnk", File.separator));
    }
}
