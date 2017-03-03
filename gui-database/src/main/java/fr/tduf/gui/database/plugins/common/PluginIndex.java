package fr.tduf.gui.database.plugins.common;

import fr.tduf.gui.database.plugins.bitfield.BitfieldPlugin;
import fr.tduf.gui.database.plugins.cameras.CamerasPlugin;
import fr.tduf.gui.database.plugins.contentRef.ContentRefPlugin;
import fr.tduf.gui.database.plugins.iks.IKsPlugin;
import fr.tduf.gui.database.plugins.mapping.MappingPlugin;
import fr.tduf.gui.database.plugins.nope.NopePlugin;
import fr.tduf.gui.database.plugins.percent.PercentPlugin;

import java.util.stream.Stream;

/**
 * Defines all database editor plugins.
 */
enum PluginIndex {
    NOPE("Default plugin, doing nothing", new NopePlugin()),
    BITFIELD("Allows to display known bitfield labels and checkboxes for easier changes", new BitfieldPlugin()),
    CAMERAS("Allows to select over available cameras and view/modify properties", new CamerasPlugin()),
    IKS("Allows to select over available Inverse Kinematics sets", new IKsPlugin()),
    PERCENT("Allows to make percent value changes easier", new PercentPlugin()),
    CONTENT_REF("Displays warning image and label", new ContentRefPlugin()),
    MAPPING("Shows files required by content entries and registration status in Bnk1.map", new MappingPlugin());

    private final String description;
    private final DatabasePlugin pluginInstance;

    PluginIndex(String description, DatabasePlugin pluginInstance) {
        this.description = description;
        this.pluginInstance = pluginInstance;
    }

    static Stream<PluginIndex> allAsStream() {
        return Stream.of(values());
    }

    DatabasePlugin getPluginInstance() {
        return pluginInstance;
    }

    public String getDescription() {
        return description;
    }
}
