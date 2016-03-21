package fr.tduf.gui.installer.controllers;

import fr.tduf.gui.common.rule.JavaFXThreadingRule;
import fr.tduf.gui.installer.stages.DatabaseCheckStageDesigner;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;

public class DatabaseCheckStageControllerTest {

    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    @Test
    @Ignore
    public void display() throws IOException {
        // GIVEN
        final HashSet<IntegrityError> integrityErrors = new HashSet<>();
        integrityErrors.add(IntegrityError.builder().ofType(IntegrityError.ErrorTypeEnum.RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES).build());
        integrityErrors.add(IntegrityError.builder().ofType(IntegrityError.ErrorTypeEnum.RESOURCE_REFERENCE_NOT_FOUND).build());
        integrityErrors.add(IntegrityError.builder().ofType(IntegrityError.ErrorTypeEnum.CONTENT_ITEMS_COUNT_MISMATCH).build());
        integrityErrors.add(IntegrityError.builder().ofType(IntegrityError.ErrorTypeEnum.CONTENTS_ENCRYPTION_NOT_SUPPORTED).build());

        // WHEN
        initDatabaseCheckStageController(null).initAndShowModalDialog(integrityErrors);
    }

    private static DatabaseCheckStageController initDatabaseCheckStageController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return DatabaseCheckStageDesigner.init(stage);
    }
}
