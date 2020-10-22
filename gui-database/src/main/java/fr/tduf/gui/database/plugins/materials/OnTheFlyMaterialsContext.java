package fr.tduf.gui.database.plugins.materials;

import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Color;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialPiece;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.gfx.materials.domain.Color.ColorKind.*;

/**
 * Enhanced dynamic context for materials plugin
 */
public class OnTheFlyMaterialsContext extends OnTheFlyContext {
    private final Property<Material> currentMaterialProperty = new SimpleObjectProperty<>();
    private final Property<MaterialPiece> currentShaderProperty = new SimpleObjectProperty<>();
    private final Map<Color.ColorKind, Property<Color>> colorProperties = new HashMap<>();

    public OnTheFlyMaterialsContext() {
        this.colorProperties.put(AMBIENT, new SimpleObjectProperty<>());
        this.colorProperties.put(DIFFUSE, new SimpleObjectProperty<>());
        this.colorProperties.put(OTHER, new SimpleObjectProperty<>());
        this.colorProperties.put(SPECULAR, new SimpleObjectProperty<>());
    }

    public Property<Material> getCurrentMaterialProperty() {
        return currentMaterialProperty;
    }

    public Property<MaterialPiece> getCurrentShaderProperty() {
        return currentShaderProperty;
    }

    /**
     * @param colorKind : color kind to be retrieved
     * @return current color property of provided kind
     */
    public Property<Color> getCurrentColorPropertyOfKind(Color.ColorKind colorKind) {
        return colorProperties.get(colorKind);
    }

    /**
     * Sets all color properties to null
     */
    public void resetAllCurrentColorProperties() {
        colorProperties.values().forEach(property -> property.setValue(null));
    }

    public void setCurrentMaterial(Material currentMaterial) {
        this.currentMaterialProperty.setValue(currentMaterial);
    }

    public void setCurrentShader(MaterialPiece currentShader) {
        this.currentShaderProperty.setValue(currentShader);
    }

    /**
     * Set current color of provided kind
     * @param color : color to apply
     */
    public void setCurrentColor(Color color) {
        Color.ColorKind colorKind = color.getKind();
        if (!colorProperties.containsKey(colorKind)) {
            throw new IllegalArgumentException("Unsupported color kind:" + colorKind);
        }
        colorProperties.get(colorKind).setValue(color);
    }
}
