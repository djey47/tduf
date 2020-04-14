package fr.tduf.gui.common.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.stages.DatabaseCheckStageDesigner;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.util.HashSet;

/**
 * To display stage without running whole application.
 */
@Disabled("Interactive testing - can't be asserted automatically")
class DatabaseCheckStageControllerTest extends ApplicationTest {
    @Override
    public void start(Stage stage) {}

    @BeforeEach
    void setUp() {
        Log.set(Log.LEVEL_TRACE);
    }

    @Test
    void display_whenManyErrors() {
        // GIVEN
        final HashSet<IntegrityError> integrityErrors = new HashSet<>();
        integrityErrors.add(IntegrityError.builder().ofType(IntegrityError.ErrorTypeEnum.CONTENTS_FIELDS_COUNT_MISMATCH).build());
        integrityErrors.add(IntegrityError.builder().ofType(IntegrityError.ErrorTypeEnum.CONTENTS_NOT_FOUND).build());
        integrityErrors.add(IntegrityError.builder().ofType(IntegrityError.ErrorTypeEnum.RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES).build());
        integrityErrors.add(IntegrityError.builder().ofType(IntegrityError.ErrorTypeEnum.RESOURCE_REFERENCE_NOT_FOUND).build());
        integrityErrors.add(IntegrityError.builder().ofType(IntegrityError.ErrorTypeEnum.CONTENT_ITEMS_COUNT_MISMATCH).build());
        integrityErrors.add(IntegrityError.builder().ofType(IntegrityError.ErrorTypeEnum.CONTENTS_ENCRYPTION_NOT_SUPPORTED)
                .addInformation(IntegrityError.ErrorInfoEnum.FILE, "File1")
                .addInformation(IntegrityError.ErrorInfoEnum.LOCALE, Locale.FRANCE)
                .build());
        integrityErrors.add(IntegrityError.builder().ofType(IntegrityError.ErrorTypeEnum.CONTENTS_ENCRYPTION_NOT_SUPPORTED)
                .addInformation(IntegrityError.ErrorInfoEnum.FILE, "File2")
                .addInformation(IntegrityError.ErrorInfoEnum.LOCALE, Locale.UNITED_STATES)
                .build());

        // WHEN
        interact(() -> initDatabaseCheckStageController().initAndShowModalDialog(integrityErrors));
    }

    private static DatabaseCheckStageController initDatabaseCheckStageController() throws IOException {
        Stage stage = new Stage();
        stage.initOwner(null);

        return DatabaseCheckStageDesigner.init(stage, "Testing DatabaseCheckStageController...");
    }
}
