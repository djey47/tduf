package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.UPDATE_DATABASE;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDatabaseStepTest {

    private static final Class<UpdateDatabaseStepTest> thisClass = UpdateDatabaseStepTest.class;

    @Mock
    private BankSupport bankSupportMock;

    private DatabaseContext databaseContext;

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        Log.set(Log.LEVEL_DEBUG);

        databaseContext = InstallerTestsHelper.createJsonDatabase();
        databaseContext.setPatch(DbPatchDto.builder().build(), new PatchProperties());

        tempDirectory = InstallerTestsHelper.createTempDirectory();
    }

    @Test
    public void perform_whenForcedVehicleSlot_shouldNotCrash() throws URISyntaxException, IOException, ReflectiveOperationException {
        // GIVEN
        String assetsDirectory = new File(thisClass.getResource("/assets-patch-only").toURI()).getAbsolutePath();
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();

        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(configuration, databaseContext)
                        .nextStep(UPDATE_DATABASE));
        updateDatabaseStep.perform();

        // THEN
    }
}
