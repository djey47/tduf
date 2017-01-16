package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.gui.database.plugins.common.PluginContext;

/**
 * Piece of information required by Cameras plugin.
 */
public class CamerasContext implements PluginContext {
    private String binaryFileLocation = null;
    private boolean pluginLoaded = false;

    @Override
    public void reset() {
        binaryFileLocation = null;
        pluginLoaded = false;
    }

    void setBinaryFileLocation(String binaryFileLocation) {
        this.binaryFileLocation = binaryFileLocation;
    }

    String getBinaryFileLocation() {
        return binaryFileLocation;
    }

    void setPluginLoaded(boolean pluginLoaded) {
        this.pluginLoaded = pluginLoaded;
    }

    boolean isPluginLoaded() {
        return pluginLoaded;
    }
}
