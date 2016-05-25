package fr.tduf.gui.installer.services;

import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.BankHelper;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityChecker;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * Background service to load and check TDU database from banks directory.
 */
public class DatabaseChecker extends Service<Set<IntegrityError>> {
    private StringProperty databaseLocation = new SimpleStringProperty();
    private ObjectProperty<BankSupport> bankSupport = new SimpleObjectProperty<>();

    @Override
    protected Task<Set<IntegrityError>> createTask() {
        return new Task<Set<IntegrityError>>() {
            @Override
            protected Set<IntegrityError> call() throws Exception {

                updateMessage(String.format(DisplayConstants.STATUS_FMT_CHECK_IN_PROGRESS, "1/3"));
                String jsonDirectory = resolveJsonDatabaseLocationAndUnpack(databaseLocation.get(), bankSupport.get());

                updateMessage(String.format(DisplayConstants.STATUS_FMT_CHECK_IN_PROGRESS, "2/3"));
                final List<DbDto> databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);

                updateMessage(String.format(DisplayConstants.STATUS_FMT_CHECK_IN_PROGRESS, "3/3"));
                final DatabaseIntegrityChecker checkerComponent = AbstractDatabaseHolder.prepare(DatabaseIntegrityChecker.class, databaseObjects);
                final Set<IntegrityError> integrityErrorsFromExtensiveCheck = checkerComponent.checkAllContentsObjects();

                updateMessage(String.format(DisplayConstants.STATUS_FMT_CHECK_DONE, integrityErrorsFromExtensiveCheck.size()));

                return integrityErrorsFromExtensiveCheck;
            }
        };
    }

    private static String resolveJsonDatabaseLocationAndUnpack(String realDatabaseLocation, BankSupport bankSupport) throws
            IOException {
        final Path realDatabasePath = Paths.get(realDatabaseLocation);
        return BankHelper.isPackedDatabase(realDatabasePath) ?
                DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(realDatabasePath, bankSupport) :
                realDatabaseLocation;
    }

    public StringProperty databaseLocationProperty() {
        return databaseLocation;
    }

    public ObjectProperty<BankSupport> bankSupportProperty() {
        return bankSupport;
    }
}
