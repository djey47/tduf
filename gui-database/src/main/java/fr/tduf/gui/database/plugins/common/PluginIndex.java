package fr.tduf.gui.database.plugins.common;

import fr.tduf.gui.database.plugins.nope.NopePlugin;

import java.util.stream.Stream;

/**
 * Defines all database editor plugins.
 */
public enum PluginIndex {
    NOPE("Default plugin, doing nothing", new NopePlugin());

    private final String description;
    private final DatabasePlugin pluginInstance;

    PluginIndex(String description, DatabasePlugin pluginInstance) {
        this.description = description;
        this.pluginInstance = pluginInstance;
    }

    public static Stream<PluginIndex> allAsStream() {
        return Stream.of(values());
    }

    public String getDescription() {
        return description;
    }

    public DatabasePlugin getPluginInstance() {
        return pluginInstance;
    }
}
