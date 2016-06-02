package fr.tduf.gui.common.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.stages.DatabaseCheckStageDesigner;
import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;

/**
 * To display stage without running whole application.
 */
@Ignore
public class DatabaseCheckStageControllerTest {

    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    @Before
    public void setUp() {
        Log.set(Log.LEVEL_TRACE);
    }

    @Test
    public void display_whenManyErrors() throws IOException {
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
        initDatabaseCheckStageController(null).initAndShowModalDialog(integrityErrors);
    }

    private static DatabaseCheckStageController initDatabaseCheckStageController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return DatabaseCheckStageDesigner.init(stage, "Testing DatabaseCheckStageController...");
    }
}
