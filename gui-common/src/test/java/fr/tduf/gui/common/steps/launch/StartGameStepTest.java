package fr.tduf.gui.common.steps.launch;

import fr.tduf.gui.common.domain.exceptions.StepException;
import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class StartGameStepTest {
    @Mock
    private ApplicationConfiguration applicationConfigurationMock;
    
    @InjectMocks
    private StartGameStep step;
    
    @BeforeEach
    void setUp() {
        initMocks(this);
    }
    
    @Test
    void resolveAndCheckGamePath_whenNoSetting_shouldThrowException() throws StepException, IOException {
        // given
        when(applicationConfigurationMock.getGamePath()).thenReturn(empty());
        
        // when-then
        assertThrows(IllegalStateException.class,
                () -> step.resolveAndCheckGamePath());
    }    
    
    @Test
    void resolveAndCheckGamePath_whenNonExistingPath_shouldThrowException() throws StepException, IOException {
        // given
        Path gamePath = Paths.get("/games/tdu");
        when(applicationConfigurationMock.getGamePath()).thenReturn(of(gamePath));
        
        // when-then
        assertThrows(IllegalArgumentException.class,
                () -> step.resolveAndCheckGamePath());
    }    
    
    @Test
    void resolveAndCheckGamePath_whenExistingPath_shouldReturnIt() throws StepException, IOException {
        // given
        Path gamePath = Paths.get(FilesHelper.createTempDirectoryForLauncher());
        when(applicationConfigurationMock.getGamePath()).thenReturn(of(gamePath));
        
        // when
        File actualGamePath = step.resolveAndCheckGamePath();
        
        // then
        assertThat(actualGamePath)
                .exists()
                .isDirectory();        
        assertThat(actualGamePath.getName()).isEqualTo(gamePath.getFileName().toString());
    }

    @Test
    void buildFullCommand_whenNonExistingBinary_shouldThrowException() {
        // given
        File gamePath = Paths.get("/games/tdu").toFile();

        // when-then
        assertThrows(IllegalArgumentException.class,
                () -> step.buildFullCommand(gamePath));
    }

    @Test
    void buildFullCommand_whenExistingBinary_shouldReturnFullCommand() throws StepException, IOException {
        // given
        Path gamePath = Paths.get(FilesHelper.createTempDirectoryForLauncher());
        Path gameBinaryPath = gamePath.resolve("TestDriveUnlimited.exe");
        Files.createFile(gameBinaryPath);

        // when
        String actualCommand = step.buildFullCommand(gamePath.toFile());

        // then
        assertThat(actualCommand).isEqualTo(gameBinaryPath.toString());
    }
}
