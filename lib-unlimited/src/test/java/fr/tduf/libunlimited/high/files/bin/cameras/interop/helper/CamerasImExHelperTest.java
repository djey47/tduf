package fr.tduf.libunlimited.high.files.bin.cameras.interop.helper;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tduf.libtesting.common.helper.TestingFilesHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CamerasDatabase;
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
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    @Mock
    private CamerasDatabase camerasDatabaseMock;

    private final CamerasImExHelper imExHelper = new CamerasImExHelper();

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void importPatch_withEmptyPatch_shouldReturnEmpty() throws IOException {
        // given
        File patchFile = new File(thisClass.getResource("/cameras/patch/tduf-empty.cam.json").getFile());

        // when
        Optional<String> actualPropertyPath = imExHelper.importPatch(patchFile, camerasDatabaseMock, null);

        // then
        assertThat(actualPropertyPath).isEmpty();
    }

    @Test
    void importPatch_withEmptyParser_shouldThrowException() {
        // given
        File patchFile = new File(thisClass.getResource("/cameras/patch/tduf-simple.cam.json").getFile());

        // when-then
        assertThrows(IllegalStateException.class,
                () -> imExHelper.importPatch(patchFile, camerasDatabaseMock, null));
    }

    @Test
    void exportToPatch_withEmptyParser_shouldReturnEmptyPatch() throws IOException {
        // given
        File patchFile = new File(TestingFilesHelper.createTempDirectoryForLibrary(), "tduf-export.cam.json");

        // when
        imExHelper.exportToPatch(patchFile, camerasDatabaseMock, 1L, null);

        // then
        Log.info(THIS_CLASS_NAME, "Written patch file: " + patchFile.getPath());
        assertThat(patchFile).exists();
        CamPatchDto actualPatchObject = new ObjectMapper().readValue(patchFile, CamPatchDto.class);
        assertThat(actualPatchObject.getComment()).startsWith("Camera patch built");
        assertThat(actualPatchObject.getChanges()).isEmpty();
    }
}
