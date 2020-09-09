package fr.tduf.gui.database.plugins.materials;

import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialPiece;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Enhanced dynamic context for materials plugin
 */
public class OnTheFlyMaterialsContext extends OnTheFlyContext {
    private final Property<Material> currentMaterialProperty = new SimpleObjectProperty<>();
    private final Property<MaterialPiece> currentShaderProperty = new SimpleObjectProperty<>();

    public Property<Material> getCurrentMaterialProperty() {
        return currentMaterialProperty;
    }

    public Property<MaterialPiece> getCurrentShaderProperty() {
        return currentShaderProperty;
    }

    public void setCurrentMaterial(Material currentMaterial) {
        this.currentMaterialProperty.setValue(currentMaterial);
    }

    public void setCurrentShader(MaterialPiece currentShader) {
        this.currentShaderProperty.setValue(currentShader);
    }
}
