package fr.tduf.libunlimited.high.files.common.interop;


import com.esotericsoftware.minlog.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class GenuineGatewayTest {

    private static final Path TOOL_PATH = Paths.get("tools", "tdumt-cli");

    @BeforeAll
    static void setUp() {
        Log.set(Log.LEVEL_DEBUG);
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
}
