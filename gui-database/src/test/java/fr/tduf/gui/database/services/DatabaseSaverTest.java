package fr.tduf.gui.database.services;

import fr.tduf.libtesting.common.helper.javafx.ApplicationTestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class DatabaseSaverTest {

    private DatabaseSaver saverService;

    @BeforeAll
    static void globalSetUp() {
        ApplicationTestHelper.initJavaFX();
    }

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
