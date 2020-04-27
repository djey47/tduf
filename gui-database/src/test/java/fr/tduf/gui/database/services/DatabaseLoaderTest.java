package fr.tduf.gui.database.services;

import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseLoaderTest extends ApplicationTest {

    private DatabaseLoader loaderService;

    @Override
    public void start(Stage stage) {}

    @BeforeEach
    void setUp() {
        // No mockito mocks here because of internal classes...
        loaderService = new DatabaseLoader();
    }

    @Test
    void callLoaderTask_whenNoJsonFiles_shouldThrowExceptionWithMessages() {
        // given
        loaderService.databaseLocationProperty().setValue(".");
        DatabaseLoader.LoaderTask loaderTask = (DatabaseLoader.LoaderTask) loaderService.createTask();

        // when-then
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class, loaderTask::call);
        assertThat(actualException)
                .hasMessage("Invalid database location: .");
        assertThat(loaderTask.getMessageHistory()).contains(
                "Loading data, please wait...",
                "Database was not loaded due to errors: .");
    }
}
