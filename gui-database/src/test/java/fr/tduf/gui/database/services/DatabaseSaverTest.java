package fr.tduf.gui.database.services;

import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class DatabaseSaverTest extends ApplicationTest {

    private DatabaseSaver saverService;

    @Override
    public void start(Stage stage) {}

    @BeforeEach
    void setUp() {
        // No mockito mocks here because of internal classes...
        saverService = new DatabaseSaver();
    }

    @Test
    void callSaverTask_whenSuccess_shouldReturnDatabasePathWithMessages() throws Exception {
        // given
        saverService.databaseLocationProperty().setValue(".");
        saverService.databaseObjectsProperty().setValue(emptyList());
        DatabaseSaver.SaverTask saverTask = (DatabaseSaver.SaverTask) saverService.createTask();

        // when
        String actualPath = saverTask.call();

        // then
        assertThat(actualPath).isEqualTo(".");
        assertThat(saverTask.getMessageHistory()).contains(
                "Saving data, please wait...",
                "Saved database: .");
    }
}
