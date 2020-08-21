package fr.tduf.libunlimited.low.files.gfx.materials.domain;

/**
 * Represents a material with its settings and layer parameters
 */
public class Material {
    protected String name;
    protected MaterialSettings settings;

    public static MaterialBuilder builder() {
        return new MaterialBuilder();
    }

    public String getName() {
        return name;
    }

    public MaterialSettings getSettings() {
        return settings;
    }

    public static class MaterialBuilder extends Material {
        public MaterialBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public MaterialBuilder withGlobalSettings(MaterialSettings materialSettings) {
            this.settings = materialSettings;
            return this;
        }

        public Material build() {
            Material material = new Material();
            material.name = this.name;
            material.settings = this.settings;
            return material;
        }
    }
}
