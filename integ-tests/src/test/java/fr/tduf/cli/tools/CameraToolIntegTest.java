package fr.tduf.cli.tools;

import fr.tduf.libtesting.common.helper.AssertionsHelper;
import fr.tduf.libtesting.common.helper.ConsoleHelper;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.system.domain.ProcessResult;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.tduf.tests.IntegTestsConstants.RESOURCES_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class CameraToolIntegTest {

    private final Path camerasIntegTestPath = RESOURCES_PATH.resolve("cameras");
    private final Path outputPath = camerasIntegTestPath.resolve("out");
    private final Path jsonPath = camerasIntegTestPath.resolve("json");
    private final String outputDirectory = outputPath.toString();
    private final String inputCameraFile = camerasIntegTestPath.resolve("Cameras.bin").toString();
    private final String outputCameraFile = outputPath.resolve("Cameras.bin.modified").toString();
    private final String batchFile = camerasIntegTestPath.resolve("instructions.csv").toString();
    private final String setConfigurationFile = jsonPath.resolve("customize-set.in.json").toString();
    private final String useViewsConfigurationFile = jsonPath.resolve("use-views.in.json").toString();

    @BeforeEach
    void setUp() throws IOException {
        FileUtils.deleteDirectory(new File(outputDirectory));
        FilesHelper.createDirectoryIfNotExists(outputDirectory);
    }

    @AfterEach
    void tearDown() {
        ConsoleHelper.restoreOutput();
    }

    @Test
    void list_shouldReturnAllCameraIdentifiers() throws IOException, JSONException {
        // GIVEN
        byte[] jsonContents = Files.readAllBytes(jsonPath.resolve("list.out.json"));
        String expectedJson = new String(jsonContents, FilesHelper.CHARSET_DEFAULT);

        // WHEN: list
        System.out.println("-> List!");
        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
        CameraTool.main(new String[]{"list", "-n", "-i", inputCameraFile});

        // THEN
        AssertionsHelper.assertOutputStreamContainsJsonExactly(outputStream, expectedJson);
    }

    @Test
    void copySet_shouldProduceCorrectFile() throws IOException {
        String referenceCameraFile = camerasIntegTestPath.resolve("Cameras.set108CopiedTo109.bin").toString();

        // WHEN: copy-set
        System.out.println("-> Copy Set!");
        CameraTool.main(new String[]{"copy-set", "-n", "-i", inputCameraFile, "-o", outputCameraFile, "-s", "108", "-t", "109"});

        // THEN
        assertThat(new File(outputCameraFile)).hasSameContentAs(new File(referenceCameraFile));
    }

    @Test
    void copySets_shouldProduceCorrectFile() throws IOException {
        String referenceCameraFile = camerasIntegTestPath.resolve("Cameras.set108CopiedTo109.bin").toString();

        // WHEN: copy-sets
        System.out.println("-> Copy Sets!");
        CameraTool.main(new String[]{"copy-sets", "-n", "-i", inputCameraFile, "-o", outputCameraFile, "-b", batchFile});

        // THEN
        assertThat(new File(outputCameraFile)).hasSameContentAs(new File(referenceCameraFile));
    }

    @Test
    void viewSet_shouldReturnAllViewProperties() throws IOException, JSONException {
        // GIVEN
        byte[] jsonContents = Files.readAllBytes(jsonPath.resolve("view-set.out.json"));
        String expectedJson = new String(jsonContents, FilesHelper.CHARSET_DEFAULT);

        // WHEN: viewSet
        System.out.println("-> View-set!");
        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
        CameraTool.main(new String[]{"view-set", "-n", "-i", inputCameraFile, "-s", "1000"});

        // THEN
        AssertionsHelper.assertOutputStreamContainsJsonExactly(outputStream, expectedJson);
    }

    @Test
    void customizeSet_shouldSetProperties_andReturnAllViewProperties() throws IOException, JSONException {
        // GIVEN
        byte[] jsonContents = Files.readAllBytes(jsonPath.resolve("customize-set.out.json"));
        String expectedJson = new String(jsonContents, FilesHelper.CHARSET_DEFAULT);

        // WHEN: customizeSet
        System.out.println("-> Customize-set!");
        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
        CameraTool.main(new String[]{"customize-set", "-n", "-i", inputCameraFile,  "-o", outputCameraFile, "-c", setConfigurationFile});

        // THEN
        AssertionsHelper.assertOutputStreamContainsJsonExactly(outputStream, expectedJson);
    }

    @Test
    void useViews_shouldUseProperties_andReturnAllViewProperties() throws IOException, JSONException {
        // GIVEN
        byte[] jsonContents = Files.readAllBytes(jsonPath.resolve("use-views.mock.out.json"));
        String expectedJson = new String(jsonContents, FilesHelper.CHARSET_DEFAULT);

        String tempDirectory = fr.tduf.libtesting.common.helper.FilesHelper.createTempDirectoryForLibrary();
        Path cameraPath = Paths.get(tempDirectory, "cameras.bin");
        Files.copy(Paths.get(inputCameraFile), cameraPath);

        CamerasHelper.setCameraSupport(getGatewayWithMockedCLI());


        // WHEN: useViews
        System.out.println("-> Use-views!");
        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
        CameraTool.main(new String[]{"use-views", "-n", "-i", cameraPath.toString(), "-c", useViewsConfigurationFile});


        // THEN
        assertThat(cameraPath).exists();
        AssertionsHelper.assertOutputStreamContainsJsonExactly(outputStream, expectedJson);
    }

    private GenuineCamGateway getGatewayWithMockedCLI() throws IOException {
        byte[] genuineJsonContents = Files.readAllBytes(jsonPath.resolve("tdumt.cam-l.out.json"));
        String genuineJson = new String(genuineJsonContents, FilesHelper.CHARSET_DEFAULT);

        CommandLineHelper commandLineHelperMock = Mockito.mock(CommandLineHelper.class);
        GenuineCamGateway camGateway = new GenuineCamGateway(commandLineHelperMock);

        when(commandLineHelperMock.runCliCommand(anyString(), any())).thenReturn(
            new ProcessResult("mono", 0, "{}", ""),    // CAM-C
            new ProcessResult("mono", 0, genuineJson, "")); // CAM-L

        return camGateway;
    }
}
