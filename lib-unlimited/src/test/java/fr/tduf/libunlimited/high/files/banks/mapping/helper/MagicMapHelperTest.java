package fr.tduf.libunlimited.high.files.banks.mapping.helper;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class MagicMapHelperTest {

    private static final Class<MagicMapHelperTest> thisClass = MagicMapHelperTest.class;

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("libUnlimited-tests").toString();
    }

    @Test
    public void fixMagicMap_shouldUpdateWithNewFiles_andReturnNewFileList() throws Exception {
        // GIVEN
        Path originalMagicMapPath = getOriginalMagicMapPath();
        Path banksPath = originalMagicMapPath.getParent();

        Path magicMapPath = Paths.get(tempDirectory).resolve(originalMagicMapPath.getFileName());
        Files.copy(originalMagicMapPath, magicMapPath);


        // WHEN
        List<String> actualFiles = MagicMapHelper.fixMagicMap(magicMapPath.toString(), banksPath.toString());


        // THEN
        System.out.println("Temp dir: " + tempDirectory);

        assertThat(actualFiles).containsOnly("avatar/barb.bnk", "bnk1-enhanced.map", "frontend/hires/gauges/hud01.bnk", "bnk1.map", "vehicules/a3_v6.bnk");

        Path expectedMagicMapPath = Paths.get(thisClass.getResource("/banks/Bnk1-enhanced.map").toURI());
        assertThat(magicMapPath.toFile()).hasContentEqualTo(expectedMagicMapPath.toFile());
    }

    @Test(expected = NullPointerException.class)
    public void fixMagicMap_whenMagicMapFileNull_shouldThrowException() throws Exception {
        // GIVEN
        Path originalMagicMapPath = getOriginalMagicMapPath();
        Path banksPath = originalMagicMapPath.getParent();

        // WHEN
        MagicMapHelper.fixMagicMap(null, banksPath.toString());

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void fixMagicMap_whenBankDirectoryNull_shouldThrowException() throws Exception {
        // GIVEN
        Path originalMagicMapPath = getOriginalMagicMapPath();
        Path magicMapPath = Paths.get(tempDirectory).resolve(originalMagicMapPath.getFileName());

        // WHEN
        MagicMapHelper.fixMagicMap(magicMapPath.toString(), null);

        // THEN: NPE
    }

    private static Path getOriginalMagicMapPath() throws URISyntaxException {
        return Paths.get(thisClass.getResource("/banks/Bnk1.map").toURI());
    }
}