
package fr.tduf.libunlimited.high.files.common.interop;


import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.system.domain.ProcessResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class GenuineGatewayTest {

    private static class GenuineGatewayForTesting extends GenuineGateway {
        protected GenuineGatewayForTesting(CommandLineHelper commandLineHelper) {
            super(commandLineHelper);
        }
    }

    private static final Path TOOL_PATH = Paths.get("tools", "tdumt-cli");

    @Mock
    private CommandLineHelper commandLineHelperMock;

    @InjectMocks
    private GenuineGatewayForTesting genuineGateway;

    @BeforeAll
    static void globalSetUp() {
//        Log.set(Log.LEVEL_DEBUG);
    }

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void getRootDirectory_whenInTestMode_shouldRetrieveToolsDirectoryAtRoot() throws Exception {
        // GIVEN-WHEN
        final Path actualDirectory = GenuineGateway.getRootDirectory();

        // THEN
        assertThat(actualDirectory.resolve(TOOL_PATH)).exists();
    }

    @Test
    void getRootDirectory_whenProvidedProdSourcePath_asProdBuild_shouldRetrieveRootDirectory() {
        // GIVEN
        final Path sourcePath = Paths.get("/", "home", "user", "apps", "tduf", "tools", "lib", "tduf.jar");

        // WHEN
        final Path actualDirectory = GenuineGateway.getRootDirectory(sourcePath);

        // THEN
        final Path expectedPath = Paths.get("/", "home", "user", "apps", "tduf");
        assertThat(actualDirectory).isEqualTo(expectedPath);
    }

    @Test
    void getRootDirectory_whenProvidedProdSourcePath_asDevBuild_shouldRetrieveRootDirectory() {
        // GIVEN
        final Path sourcePath = Paths.get("/", "home", "user", "dev", "tduf", "lib-unlimited", "build", "libs", "lib-unlimited-1.13.0-SNAPSHOT.jar");

        // WHEN
        final Path actualDirectory = GenuineGateway.getRootDirectory(sourcePath);

        // THEN
        final Path expectedPath = Paths.get("/", "home", "user", "dev", "tduf");
        assertThat(actualDirectory).isEqualTo(expectedPath);
    }

    @Test
    void callCommandLineInterface_whenCommandSuccess_shouldReturnOutput() throws IOException {
        // given
        ProcessResult successProcessResult = new ProcessResult("TEST", 0, "{}", "");
        when(commandLineHelperMock.runCliCommand(anyString(), any())).thenReturn(successProcessResult);

        // when
        String actualOutput = genuineGateway.callCommandLineInterface(GenuineGateway.CommandLineOperation.BANK_INFO);

        // then
        assertThat(actualOutput).isEqualTo("{}");
    }

    @Test
    void callCommandLineInterface_whenCommandSuccess_butInvalidJson_shouldThrowException() throws IOException {
        // given
        ProcessResult successProcessResult = new ProcessResult("TEST", 0, "NOJSON", "");
        when(commandLineHelperMock.runCliCommand(anyString(), any())).thenReturn(successProcessResult);

        // when-then
        IOException actualException = assertThrows(IOException.class,
                () -> genuineGateway.callCommandLineInterface(GenuineGateway.CommandLineOperation.BANK_INFO));
        assertThat(actualException).hasMessage("CLI command output is not valid JSON: NOJSON");
    }

    @Test
    void callCommandLineInterface_whenCommandFailure_shouldThrowExceptionWithMessage() throws IOException {
        // given
        ProcessResult succesProcessResult = new ProcessResult("TEST", 1, "OUTPUT", "ERROR OUTPUT");
        when(commandLineHelperMock.runCliCommand(anyString(), any())).thenReturn(succesProcessResult);

        // when-then
        IOException actualException = assertThrows(IOException.class,
                () -> genuineGateway.callCommandLineInterface(GenuineGateway.CommandLineOperation.BANK_INFO));
        assertThat(actualException).hasMessage("Unable to execute genuine CLI command: 'TEST' > (1) ERROR OUTPUT");
    }
}
