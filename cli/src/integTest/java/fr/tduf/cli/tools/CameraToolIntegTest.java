package fr.tduf.cli.tools;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class CameraToolIntegTest {

    private final Path camerasIntegTestPath = Paths.get("integ-tests").resolve("cameras");
    private final Path outputPath = camerasIntegTestPath.resolve("out");
    private final String outputDirectory = outputPath.toString();
    private final String inputCameraFile = camerasIntegTestPath.resolve("Cameras.bin").toString();
    private final String outputCameraFile = outputPath.resolve("Cameras.bin.extended").toString();

    @Before
    public void setUp() throws IOException {
        FileUtils.deleteDirectory(new File(outputDirectory));
        FilesHelper.createDirectoryIfNotExists(outputDirectory);
    }

    @Test
    public void copySet_shouldProduceCorrectFile() throws IOException {
        String referenceCameraFile = camerasIntegTestPath.resolve("Cameras.set108CopiedTo109.bin").toString();

        // WHEN: copy-set
        System.out.println("-> Copy Set!");
        CameraTool.main(new String[]{"copy-set", "-n", "-i", inputCameraFile, "-o", outputCameraFile, "-s", "108", "-t", "109"});

        // THEN
        assertThat(new File(outputCameraFile)).hasSameContentAs(new File(referenceCameraFile));
    }
}
