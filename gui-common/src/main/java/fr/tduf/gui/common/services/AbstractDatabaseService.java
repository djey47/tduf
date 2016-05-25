package fr.tduf.gui.common.services;

import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;

import java.util.List;
import java.util.Set;

/**
 * Root service for database checking and fixing
 */
public abstract class AbstractDatabaseService extends Service<Void> {
    protected StringProperty jsonDatabaseLocation = new SimpleStringProperty();
    protected StringProperty databaseLocation = new SimpleStringProperty();
    protected ObjectProperty<BankSupport> bankSupport = new SimpleObjectProperty<>();
    protected ObjectProperty<Set<IntegrityError>> integrityErrors = new SimpleObjectProperty<>();
    protected ObjectProperty<List<DbDto>> loadedDatabaseObjects = new SimpleObjectProperty<>();

    public void fromService(AbstractDatabaseService service) {
        jsonDatabaseLocation.setValue(service.jsonDatabaseLocation.get());
        databaseLocation.setValue(service.databaseLocation.getValue());
        bankSupport.setValue(service.bankSupport.get());
        integrityErrors.setValue(service.integrityErrors.get());
        loadedDatabaseObjects.setValue(service.loadedDatabaseObjects.get());
    }

    public StringProperty databaseLocationProperty() {
        return databaseLocation;
    }

    public ObjectProperty<BankSupport> bankSupportProperty() {
        return bankSupport;
    }

    public ObjectProperty<Set<IntegrityError>> integrityErrorsProperty() {
        return integrityErrors;
    }
}
