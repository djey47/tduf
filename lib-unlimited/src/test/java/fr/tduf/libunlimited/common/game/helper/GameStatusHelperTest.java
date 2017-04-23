package fr.tduf.libunlimited.common.game.helper;

import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.game.domain.GameVersion;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.tduf.libunlimited.common.game.domain.GameVersion.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GameStatusHelperTest {
    @Test
    void resolveGameVersion_whenNullFile_shouldReturnNoGame() {
        // given - when - then
        assertThat(GameStatusHelper.resolveGameVersion(null)).isEqualTo(GameVersion.NO_GAME_BINARY);
    }

    @Test
    void resolveGameVersion_whenFileDoesNotExist_shouldReturnNoGame() {
        // given
        String binaryPath = "/foo.exe";

        // when-then
        assertThat(GameStatusHelper.resolveGameVersion(binaryPath)).isEqualTo(GameVersion.NO_GAME_BINARY);
    }

    @Test
    void resolveGameVersion_whenEmptyFile_shouldReturnUnknown() throws IOException {
        // given
        Path path = Paths.get(FilesHelper.createTempDirectoryForLibrary(), "foo.exe");
        Files.createFile(path);
        String binaryPath = path.toString();

        // when-then
        assertThat(GameStatusHelper.resolveGameVersion(binaryPath)).isEqualTo(UNKNOWN);
    }
}