package fr.tduf.libunlimited.high.files.common.interop;

import org.junit.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class GenuineGatewayTest {
    @Test
    public void getExecutableDirectory() throws Exception {
        // GIVEN-WHEN
        final Path actualDirectory = GenuineGateway.getExecutableDirectory();

        // THEN
        assertThat(actualDirectory.resolve("tools").resolve("tdumt-cli")).exists();
    }
}
