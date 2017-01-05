package fr.tduf.gui.database.plugins.common;

import fr.tduf.gui.database.plugins.bitfield.BitfieldPlugin;
import fr.tduf.gui.database.plugins.nope.NopePlugin;

import java.util.stream.Stream;

/**
 * Defines all database editor plugins.
 */
enum PluginIndex {
    NOPE("Default plugin, doing nothing", new NopePlugin()),
    BITFIELD("Allows to display known bitfield labels and checkboxes for easier changes", new BitfieldPlugin());

    private final String description;
    private final DatabasePlugin pluginInstance;

    PluginIndex(String description, DatabasePlugin pluginInstance) {
        this.description = description;
        this.pluginInstance = pluginInstance;
    }

    static Stream<PluginIndex> allAsStream() {
        return Stream.of(values());
    }

    String getDescription() {
        return description;
    }

    DatabasePlugin getPluginInstance() {
        return pluginInstance;
    }
}