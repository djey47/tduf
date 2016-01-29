package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.TestHelper;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class CopyFilesStepTest {

    private static final Class<CopyFilesStepTest> thisClass = CopyFilesStepTest.class;

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = TestHelper.createTempDirectory();

        TestHelper.prepareTduDirectoryLayout(tempDirectory);
    }

    @Test
    @Ignore
    // TODO
    public void copyFilesStep_withFakeFilesAllPresent_shouldCopyThemToCorrectLocation() throws Exception {
        // GIVEN
        String assetsDirectory = new File(thisClass.getResource("/assets-all").toURI()).getAbsolutePath();
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();
        final GenericStep previousStep = GenericStep.defaultStep(configuration, null);


        // WHEN
        GenericStep.copyFilesStep(previousStep);


        // THEN
        System.out.println("Testing TDU directory: " + tempDirectory);

        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "AC_289.bnk").toFile()).exists();
        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "AC_289.bnk").toFile()).exists();

        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "Rim", "AC", "AC_289_F_01.bnk").toFile()).exists();

        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "FrontEnd", "LowRes", "Gauges", "AC_289.bnk").toFile()).exists();
        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "FrontEnd", "HiRes", "Gauges", "AC_289.bnk").toFile()).exists();

        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Sound", "Vehicules", "AC_289_audio.bnk").toFile()).exists();
    }
}
