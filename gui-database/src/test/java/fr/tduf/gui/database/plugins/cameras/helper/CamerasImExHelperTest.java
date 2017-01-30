package fr.tduf.gui.database.plugins.cameras.helper;

import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.initMocks;

class CamerasImExHelperTest {
    private static final Class<CamerasImExHelperTest> thisClass = CamerasImExHelperTest.class;

    @Mock
    private CamerasParser camerasParser;

    private CamerasImExHelper imExHelper = new CamerasImExHelper();

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void importPatch_withEmptyParser_shouldReturnEmptyPropertiesPath() throws IOException {
        // given
        File patchFile = new File(thisClass.getResource("/patches/tduf.cam.json").getFile());

        // when
        Optional<String> actualProperties = imExHelper.importPatch(patchFile, camerasParser, null);

        // then
        assertThat(actualProperties).isEmpty();
    }

    @Test
    void exportToPatch_withEmptyParser_shouldReturnEmptyPatch() throws IOException {
        // given
        File patchFile = new File(FilesHelper.createTempDirectoryForDatabaseEditor(), "tduf-export.cam.json");

        // when
        imExHelper.exportToPatch(patchFile, camerasParser, 1L, null);

        // then
        assertThat(patchFile).exists();
        // TODO assert file has empty patch contents
    }
}
