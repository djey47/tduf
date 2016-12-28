package fr.tduf.libunlimited.high.files.banks.mapping.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libtesting.common.helper.AssertionsHelper;
import fr.tduf.libtesting.common.helper.FilesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class MagicMapHelperTest {

    private static final Class<MagicMapHelperTest> thisClass = MagicMapHelperTest.class;

    private String tempDirectory;

    @BeforeAll
    static void globalSetUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = FilesHelper.createTempDirectoryForLibrary();
    }

    @Test
    void fixMagicMap_shouldUpdateWithNewFiles_andReturnNewFileList() throws Exception {
        // GIVEN
        Path magicMapPath = createTemporaryMapPath();
        Path banksPath = getOriginalMapPath().getParent();


        // WHEN
        List<String> actualFiles = MagicMapHelper.fixMagicMap(magicMapPath.toString(), banksPath.toString());


        // THEN
        Log.info(thisClass.getSimpleName(), "Temp dir: " + tempDirectory);

        assertThat(actualFiles).containsOnly(
                "avatar/barb.bnk",
                "bnk1.no.magic.map",
                "bnk1-enhanced1.map",
                "bnk1-enhanced2.map",
                "frontend/hires/gauges/hud01.bnk",
                "vehicules/a3_v6.bnk");

        Path expectedMagicMapPath = Paths.get(thisClass.getResource("/banks/Bnk1-enhanced1.map").toURI());
        AssertionsHelper.assertFileMatchesReference(magicMapPath.toFile(), expectedMagicMapPath.toFile());
    }

    @Test
    void fixMagicMap_whenMagicMapFileNull_shouldThrowException() throws Exception {
        // GIVEN
        Path originalMagicMapPath = getOriginalMapPath();
        Path banksPath = originalMagicMapPath.getParent();

        // WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> MagicMapHelper.fixMagicMap(null, banksPath.toString()));
    }

    @Test
    void fixMagicMap_whenBankDirectoryNull_shouldThrowException() throws Exception {
        // GIVEN
        Path magicMapPath = Paths.get(tempDirectory, "Bnk1.map");

        // WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> MagicMapHelper.fixMagicMap(magicMapPath.toString(), null));
    }

    @Test
    void toMagicMap_whenNormalMap_shouldSetAllEntrySizesTo0() throws URISyntaxException, IOException {
        // GIVEN
        Path mapPath = createTemporaryMapPath();

        // WHEN
        MagicMapHelper.toMagicMap(mapPath.toString());

        // THEN
        byte[] expected = fr.tduf.libunlimited.common.helper.FilesHelper.readBytesFromResourceFile("/banks/Bnk1-enhanced2.map");
        assertThat(mapPath.toFile()).hasBinaryContent(expected);
    }

    @Test
    void toMagicMap_whenNullMapFile_shouldThrowException() throws IOException {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> MagicMapHelper.toMagicMap(null));
    }

    private static Path getOriginalMapPath() throws URISyntaxException {
        return Paths.get(thisClass.getResource("/banks/Bnk1.no.magic.map").toURI());
    }

    private Path createTemporaryMapPath() throws IOException, URISyntaxException {
        Path magicMapPath = Paths.get(tempDirectory, "Bnk1.map");

        byte[] bytes = fr.tduf.libunlimited.common.helper.FilesHelper.readBytesFromResourceFile("/banks/Bnk1.map");
        Files.write(magicMapPath, bytes);

        return magicMapPath;
    }
}
