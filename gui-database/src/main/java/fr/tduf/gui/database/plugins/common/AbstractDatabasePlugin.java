package fr.tduf.gui.database.plugins.common;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Hosts all common behaviour in plugins
 */
public abstract class AbstractDatabasePlugin implements DatabasePlugin {
    private Exception initError = null;

    //TODO embed editor context to be filled at init

    @Override
    public void setInitError(Exception initException) {
        initError = initException;
    }

    @Override
    public Optional<Exception> getInitError() {
        return ofNullable(initError);
    }
}
