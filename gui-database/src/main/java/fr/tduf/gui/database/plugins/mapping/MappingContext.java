package fr.tduf.gui.database.plugins.mapping;

import fr.tduf.gui.database.plugins.common.PluginContext;

public class MappingContext implements PluginContext {
    private boolean pluginLoaded = false;
    
    @Override
    public void reset() {
        pluginLoaded = false;
    }

    void setPluginLoaded(boolean pluginLoaded) {
        this.pluginLoaded = pluginLoaded;
    }

    boolean isPluginLoaded() {
        return pluginLoaded;
    }    
}
