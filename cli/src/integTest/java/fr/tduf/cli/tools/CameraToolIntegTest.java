package fr.tduf.cli.tools;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class CameraToolIntegTest {
    private final String outputDirectory = "integ-tests/cameras/out";
    private final String inputCameraFile = Paths.get("integ-tests/cameras", "Cameras.bin").toString();
    private final String outputCameraFile = Paths.get(outputDirectory, "Cameras.bin.extended").toString();

    @Before
    public void setUp() throws IOException {
        FileUtils.deleteDirectory(new File(outputDirectory));
        FilesHelper.createDirectoryIfNotExists(outputDirectory);
    }

    @Test
    public void copyAllSets_shouldProduceCorrectFile() throws IOException {
        String referenceCameraFile = "integ-tests/cameras/Cameras.allSetsCopied.bin";

        // WHEN: copy-all-sets
        System.out.println("-> Copy All Sets!");
        CameraTool.main(new String[]{"copy-all-sets", "-n", "-i", inputCameraFile, "-o", outputCameraFile, "-t", "10000"});

        // THEN
        assertThat(new File(outputCameraFile)).hasSameContentAs(new File(referenceCameraFile));
    }

    @Test
    public void copySet_shouldProduceCorrectFile() throws IOException {
        String referenceCameraFile = "integ-tests/cameras/Cameras.set108CopiedTo109.bin";

        // WHEN: copy-set
        System.out.println("-> Copy Set!");
        CameraTool.main(new String[]{"copy-set", "-n", "-i", inputCameraFile, "-o", outputCameraFile, "-s", "108", "-t", "109"});

        // THEN
        assertThat(new File(outputCameraFile)).hasSameContentAs(new File(referenceCameraFile));
    }
}