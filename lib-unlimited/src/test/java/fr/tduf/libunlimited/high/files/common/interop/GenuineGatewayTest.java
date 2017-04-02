package fr.tduf.libunlimited.high.files.common.interop;


import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GenuineGatewayTest {
    @Test
    void getExecutableDirectory() throws Exception {
        // GIVEN-WHEN
        final Path actualDirectory = GenuineGateway.getExecutableDirectory();

        // THEN
        assertThat(actualDirectory.resolve("tools").resolve("tdumt-cli")).exists();
    }
}
