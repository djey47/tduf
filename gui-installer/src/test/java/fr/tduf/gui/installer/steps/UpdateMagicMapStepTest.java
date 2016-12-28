package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libtesting.common.helper.FilesHelper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.UPDATE_MAGIC_MAP;
import static org.assertj.core.api.Assertions.assertThat;

class UpdateMagicMapStepTest {

    private static final Class<UpdateMagicMapStepTest> thisClass = UpdateMagicMapStepTest.class;

    private String tempDirectory;

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = InstallerTestsHelper.createTempDirectory();

        FilesHelper.prepareTduDirectoryLayout(tempDirectory);
    }

    @Test
    void updateMagicMapStep_whenMapFilesExists_andNewFiles_shouldUpdateMap() throws Exception {
        // GIVEN
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .build();

        // WHEN
        GenericStep.starterStep(configuration, null)
                .nextStep(UPDATE_MAGIC_MAP).start();

        // THEN
        File actualMagicMapFile = Paths.get(tempDirectory, "Euro", "Bnk", "Bnk1.map").toFile();
        byte[] expectedBytes = IOUtils.toByteArray(thisClass.getResource("/banks/Bnk1-enhanced.map"));
        assertThat(actualMagicMapFile).hasBinaryContent(expectedBytes);
    }
}
