package fr.tduf.gui.database.plugins.common;

import fr.tduf.gui.database.plugins.common.contexts.EditorContext;

import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Hosts all common behaviour in plugins
 */
public abstract class AbstractDatabasePlugin implements DatabasePlugin {
    private EditorContext editorContext;
    private Exception initError = null;

    @Override
    public void onInit(EditorContext editorContext) throws IOException {
        this.editorContext = editorContext;
    }

    @Override
    public void setInitError(Exception initException) {
        initError = initException;
    }

    @Override
    public Optional<Exception> getInitError() {
        return ofNullable(initError);
    }

    @Override
    public EditorContext getEditorContext() {
        return editorContext;
    }

    /**
     * Visible for testing
     */
    protected void setEditorContext(EditorContext editorContext) {
        this.editorContext = editorContext;
    }
}
