package fr.tduf.gui.database.plugins.common;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.List;

/**
 * Ensures support for database editor plugins.
 */
public class PluginHandler {
    private static final String THIS_CLASS_NAME = PluginHandler.class.getSimpleName();

    /**
     * Calls all init methods from all plugins in index
     * @param databaseObjects   : loaded database topic objects
     */
    public void initializeAllPlugins(List<DbDto> databaseObjects) {
       PluginIndex.allAsStream().forEach(this::initializePlugin);
    }

    private void initializePlugin(PluginIndex pluginIndex) {
        Log.debug(THIS_CLASS_NAME, "Now initializing plugin: " + pluginIndex);

        pluginIndex.getPluginInstance().onInit();
    }
}
