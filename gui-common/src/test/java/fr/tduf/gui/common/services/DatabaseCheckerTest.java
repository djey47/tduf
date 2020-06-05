package fr.tduf.gui.common.services;

import fr.tduf.libtesting.common.helper.javafx.ApplicationTestHelper;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseCheckerTest {
    private DatabaseChecker checkerService;

    @BeforeAll
    static void globalSetUp() {
        ApplicationTestHelper.initJavaFX();
    }

    @BeforeEach
    void setUp() {
        // No mockito mocks here because of internal classes...
        checkerService = new DatabaseChecker();

        checkerService.databaseLocation.setValue("/path/to/database");
    }

    @Test
    void callCheckerTask_whenError_shouldThrowException() {
        // given
        checkerService.databaseLocation.setValue(null);
        DatabaseChecker.CheckerTask checkerTask = (DatabaseChecker.CheckerTask) checkerService.createTask();

        // when-then
        assertThrows(NullPointerException.class,
                checkerTask::call);
        assertThat(checkerTask.getMessageHistory()).contains(
                "Performing database check 1/3, please wait...",
                "Could not check database.");
    }

    @Test
    void callCheckerTask_whenNoDatabaseInDirectory_shouldReturnIntegrityError() throws Exception {
        // given
        DatabaseChecker.CheckerTask checkerTask = (DatabaseChecker.CheckerTask) checkerService.createTask();

        // when
        checkerTask.call();

        // then
        assertThat(checkerService.integrityErrors.getValue()).hasSize(1);
        IntegrityError integrityError = pickSingleIntegrityError();
        assertThat(integrityError.getError()).isEqualTo("INCOMPLETE_DATABASE");
        assertThat(checkerService.jsonDatabaseLocation.getValue()).isEqualTo("/path/to/database");
        assertThat(checkerTask.getMessageHistory()).contains(
                "Performing database check 1/3, please wait...",
                "Performing database check 2/3, please wait...",
                "Performing database check 3/3, please wait...",
                "Done checking database, 1 error(s).");
    }

    @Test
    void callCheckerTask_whenLoadedIncompleteDatabase_shouldReturnIntegrityError() throws Exception {
        // given
        checkerService.loadedDatabaseObjects.setValue(new ArrayList<>());
        DatabaseChecker.CheckerTask checkerTask = (DatabaseChecker.CheckerTask) checkerService.createTask();

        // when
        checkerTask.call();

        // then
        assertThat(checkerService.integrityErrors.getValue()).hasSize(1);
        IntegrityError integrityError = pickSingleIntegrityError();
        assertThat(integrityError.getError()).isEqualTo("INCOMPLETE_DATABASE");
        assertThat(checkerService.jsonDatabaseLocation.getValue()).isEqualTo("/path/to/database");
        assertThat(checkerTask.getMessageHistory()).contains(
                "Performing database check 3/3, please wait...",
                "Done checking database, 1 error(s).");
    }

    private IntegrityError pickSingleIntegrityError() {
        return checkerService.integrityErrors.getValue().stream().findAny().orElseThrow(IllegalStateException::new);
    }
}
