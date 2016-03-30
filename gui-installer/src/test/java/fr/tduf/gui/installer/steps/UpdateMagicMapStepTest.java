package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libtesting.common.helper.FilesHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.UPDATE_MAGIC_MAP;
import static org.assertj.core.api.StrictAssertions.assertThat;

public class UpdateMagicMapStepTest {

    private static final Class<UpdateMagicMapStepTest> thisClass = UpdateMagicMapStepTest.class;

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = InstallerTestsHelper.createTempDirectory();

        FilesHelper.prepareTduDirectoryLayout(tempDirectory);
    }

    @Test
    public void updateMagicMapStep_whenMapFilesExists_andNewFiles_shouldUpdateMap() throws Exception {
        // GIVEN
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .build();

        // WHEN
        GenericStep.starterStep(configuration, null)
                .nextStep(UPDATE_MAGIC_MAP).start();

        // THEN
        File actualMagicMapFile = Paths.get(tempDirectory, "Euro", "Bnk", "Bnk1.map").toFile();
        File expectedMagicMapFile = new File(thisClass.getResource("/banks/Bnk1-enhanced.map").getFile());
        assertThat(actualMagicMapFile).hasSameContentAs(expectedMagicMapFile);
    }
}
