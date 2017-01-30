package fr.tduf.gui.database.plugins.cameras.helper;

import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;

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
    void importPatch_withEmptyParser_shouldThrowException() throws IOException {
        // given
        File patchFile = new File(thisClass.getResource("/patches/tduf.cam.json").getFile());

        // when-then
        assertThrows(IllegalStateException.class,
                () -> imExHelper.importPatch(patchFile, camerasParser, null));
    }
}
