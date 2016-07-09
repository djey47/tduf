package fr.tduf.gui.installer.services;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.services.tasks.InstallTask;
import fr.tduf.gui.installer.services.tasks.UninstallTask;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Background service to orchestrate all operations on vehicle mod.
 */
public class StepsCoordinator extends Service<Void> {
    private ObjectProperty<InstallerConfiguration> configuration = new SimpleObjectProperty<>();
    private ObjectProperty<DatabaseContext> context = new SimpleObjectProperty<>();
    // TODO replace by an op enum
    private BooleanProperty uninstall = new SimpleBooleanProperty(false);

    @Override
    protected Task<Void> createTask() {
        return uninstall.get() ?
                new UninstallTask(configuration, context) : new InstallTask(configuration, context);
    }

    public ObjectProperty<InstallerConfiguration> configurationProperty() {
        return configuration;
    }

    public ObjectProperty<DatabaseContext> contextProperty() {
        return context;
    }

    public BooleanProperty uninstallProperty() { return uninstall; }
}
