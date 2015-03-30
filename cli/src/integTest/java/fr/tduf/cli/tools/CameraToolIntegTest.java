package fr.tduf.cli.tools;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class CameraToolIntegTest {
    private final String outputDirectory = "integ-tests/cameras/out";
    private final String inputCameraFile = "integ-tests/cameras/Cameras.bin";
    private final String outputCameraFile = outputDirectory + "/Cameras.bin.extended";

    @Before
    public void setUp() throws IOException {
        Files.createDirectories(Paths.get(outputDirectory));

        Files.deleteIfExists(Paths.get(outputCameraFile));
    }

    @Test
    public void copyAllSets_shouldProduceCorrectFile() throws IOException {
        String referenceCameraFile = "integ-tests/cameras/Cameras.allSetsCopied.bin";

        // WHEN: copy-all-sets
        System.out.println("-> Copy All Sets!");
        CameraTool.main(new String[]{"copy-all-sets", "-n", "-i", inputCameraFile, "-o", outputCameraFile, "-t", "10000"});

        // THEN
        assertThat(new File(outputCameraFile)).hasContentEqualTo(new File(referenceCameraFile));
    }

    @Test
    public void copySet_shouldProduceCorrectFile() throws IOException {
        String referenceCameraFile = "integ-tests/cameras/Cameras.set108CopiedTo109.bin";

        // WHEN: copy-set
        System.out.println("-> Copy Set!");
        CameraTool.main(new String[]{"copy-set", "-n", "-i", inputCameraFile, "-o", outputCameraFile, "-s", "108", "-t", "109"});

        // THEN
        assertThat(new File(outputCameraFile)).hasContentEqualTo(new File(referenceCameraFile));
    }
}
