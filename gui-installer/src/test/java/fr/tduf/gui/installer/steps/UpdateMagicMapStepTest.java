package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.TestHelper;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
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
        tempDirectory = TestHelper.createTempDirectory();

        TestHelper.prepareTduDirectoryLayout(tempDirectory);
    }

    @Test
    public void updateMagicMapStep_whenMapFilesExists_andNewFiles_shouldUpdateMap() throws IOException, ReflectiveOperationException {
        // GIVEN
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .build();

        // WHEN
        GenericStep previousStep = GenericStep.starterStep(configuration, null);
        GenericStep.loadStep(UPDATE_MAGIC_MAP, previousStep).start();

        // THEN
        File actualMagicMapFile = Paths.get(tempDirectory, "Euro", "Bnk", "Bnk1.map").toFile();
        File expectedMagicMapFile = new File(thisClass.getResource("/banks/Bnk1-enhanced.map").getFile());
        assertThat(actualMagicMapFile).hasSameContentAs(expectedMagicMapFile);
    }
}
