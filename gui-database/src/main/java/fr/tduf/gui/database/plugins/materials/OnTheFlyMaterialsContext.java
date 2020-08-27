package fr.tduf.gui.database.plugins.materials;

import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Enhanced dynamic context for materials plugin
 */
public class OnTheFlyMaterialsContext extends OnTheFlyContext {
    private final Property<Material> currentMaterialProperty = new SimpleObjectProperty<>();

    public Property<Material> getCurrentMaterialProperty() {
        return currentMaterialProperty;
    }

    public void setCurrentMaterial(Material currentMaterial) {
        this.currentMaterialProperty.setValue(currentMaterial);
    }
}
