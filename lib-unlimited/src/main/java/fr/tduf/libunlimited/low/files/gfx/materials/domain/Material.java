package fr.tduf.libunlimited.low.files.gfx.materials.domain;

/**
 * Represents a material with its settings and layer parameters
 */
public class Material {
    protected String name;
    protected MaterialSettings properties;
    protected LayerGroup layerGroup;

    private Material() {}

    public static MaterialBuilder builder() {
        return new MaterialBuilder();
    }

    public String getName() {
        return name;
    }

    public MaterialSettings getProperties() {
        return properties;
    }

    public LayerGroup getLayerGroup() {
        return layerGroup;
    }

    public static class MaterialBuilder extends Material {
        public MaterialBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public MaterialBuilder withGlobalSettings(MaterialSettings materialSettings) {
            this.properties = materialSettings;
            return this;
        }

        public MaterialBuilder withLayerGroup(LayerGroup layerGroup) {
            this.layerGroup = layerGroup;
            return this;
        }

        public Material build() {
            Material material = new Material();
            material.name = name;
            material.properties = properties;
            material.layerGroup = layerGroup;
            return material;
        }
    }
}
