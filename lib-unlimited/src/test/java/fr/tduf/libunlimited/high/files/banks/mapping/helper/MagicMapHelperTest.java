package fr.tduf.libunlimited.high.files.banks.mapping.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libtesting.common.helper.FilesHelper;
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
        Log.set(Log.LEVEL_INFO);

        tempDirectory = FilesHelper.createTempDirectoryForLibrary();
    }

    @Test
    public void fixMagicMap_shouldUpdateWithNewFiles_andReturnNewFileList() throws Exception {
        // GIVEN
        Path originalMagicMapPath = getOriginalMagicMapPath();
        Path magicMapPath = getTemporaryMapPath(originalMagicMapPath);
        Path banksPath = originalMagicMapPath.getParent();


        // WHEN
        List<String> actualFiles = MagicMapHelper.fixMagicMap(magicMapPath.toString(), banksPath.toString());


        // THEN
        Log.info(thisClass.getSimpleName(), "Temp dir: " + tempDirectory);

        assertThat(actualFiles).containsOnly(
                "avatar/barb.bnk",
                "bnk1.no.magic.map",
                "bnk1-enhanced.map",
                "frontend/hires/gauges/hud01.bnk",
                "bnk1.map",
                "vehicules/a3_v6.bnk");

        Path expectedMagicMapPath = Paths.get(thisClass.getResource("/banks/Bnk1-enhanced.map").toURI());
        assertThat(magicMapPath.toFile()).hasSameContentAs(expectedMagicMapPath.toFile());
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

    @Test
    public void toMagicMap_whenNormalMap_shouldSetAllEntrySizesTo0() throws URISyntaxException, IOException {
        // GIVEN
        Path originalMapPath = getOriginalMapPath();
        Path mapPath = getTemporaryMapPath(originalMapPath);

        // WHEN
        MagicMapHelper.toMagicMap(mapPath.toString());

        // THEN
        Path originalMagicMapPath = getOriginalMagicMapPath();
        assertThat(mapPath.toFile()).hasSameContentAs(originalMagicMapPath.toFile());
    }

    @Test(expected = NullPointerException.class)
    public void toMagicMap_whenNullMapFile_shouldThrowException() throws IOException {
        // GIVEN-WHEN
        MagicMapHelper.toMagicMap(null);

        // THEN: NPE
    }

    private static Path getOriginalMagicMapPath() throws URISyntaxException {
        return Paths.get(thisClass.getResource("/banks/Bnk1.map").toURI());
    }

    private static Path getOriginalMapPath() throws URISyntaxException {
        return Paths.get(thisClass.getResource("/banks/Bnk1.no.magic.map").toURI());
    }

    private Path getTemporaryMapPath(Path originalMapPath) throws IOException, URISyntaxException {
        Path magicMapPath = Paths.get(tempDirectory).resolve(originalMapPath.getFileName());

        Files.copy(originalMapPath, magicMapPath);

        return magicMapPath;
    }
}
