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
    private Exception initError;
    private Exception saveError;
    private String name;

    @Override
    public void onInit(String pluginName, EditorContext editorContext) throws IOException {
        this.editorContext = editorContext;
        name = pluginName;
    }

    @Override
    public void setInitError(Exception initException) {
        initError = initException;
    }

    @Override
    public void setSaveError(Exception saveException) {
        saveError = saveException;
    }

    @Override
    public Optional<Exception> getInitError() {
        return ofNullable(initError);
    }

    @Override
    public Optional<Exception> getSaveError() {
        return ofNullable(saveError);
    }

    @Override
    public EditorContext getEditorContext() {
        return editorContext;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Visible for testing
     */
    protected void setEditorContext(EditorContext editorContext) {
        this.editorContext = editorContext;
    }
}
