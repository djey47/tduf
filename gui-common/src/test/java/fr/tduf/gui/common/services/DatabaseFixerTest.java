package fr.tduf.gui.common.services;

import fr.tduf.libtesting.common.helper.javafx.ApplicationTestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseFixerTest {
    private DatabaseFixer fixerService;

    @BeforeAll
    static void globalSetUp() {
        ApplicationTestHelper.initJavaFX();
    }

    @BeforeEach
    void setUp() {
        // No mockito mocks here because of internal classes...
        fixerService = new DatabaseFixer();

        fixerService.databaseLocation.setValue("/path/to/database");
        fixerService.jsonDatabaseLocation.setValue("/path/to/json");
    }

    @Test
    void callFixerTask_whenError_shouldThrowException() {
        // given
        DatabaseFixer.FixerTask fixerTask = (DatabaseFixer.FixerTask) fixerService.createTask();

        // when-then
        assertThrows(NullPointerException.class,
                fixerTask::call);
        assertThat(fixerTask.getMessageHistory()).contains(
                "Could not fix database.");
    }

    @Test
    void callFixerTask_whenNoIntegrityError() throws Exception {
        // given
        DatabaseFixer.FixerTask fixerTask = (DatabaseFixer.FixerTask) fixerService.createTask();
        fixerService.loadedDatabaseObjects.setValue(new ArrayList<>());
        fixerService.integrityErrors.setValue(new HashSet<>());

        // when
        fixerTask.call();

        // then
        assertThat(fixerService.integrityErrors.getValue()).isEmpty();
        assertThat(fixerTask.getMessageHistory()).contains(
                "Performing database fix 1/3, please wait...",
                "Performing database fix 2/3, please wait...",
                "Performing database fix 3/3, please wait...",
                "Done fixing database, 0 error(s) remaining.");
    }
}
