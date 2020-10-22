package fr.tduf.libunlimited.low.files.gfx.materials.domain;

import fr.tduf.libunlimited.framework.lang.UByte;

import java.util.List;
import java.util.Objects;

import static fr.tduf.libunlimited.low.files.gfx.materials.domain.Color.ColorKind.*;
import static java.util.Objects.requireNonNull;

/**
 * Represents global settings for a given material
 */
public class MaterialSettings {

    protected UByte alpha;
    protected UByte[] alphaBlending;
    protected Color ambientColor;
    protected Color diffuseColor;
    protected Color specularColor;
    protected Color otherColor;
    protected byte[] unknown;
    protected Shader shader;

    private MaterialSettings() {}

    public static MaterialSettingsBuilder builder() {
        return new MaterialSettingsBuilder();
    }

    public UByte getAlpha() {
        return alpha;
    }

    public UByte[] getAlphaBlending() {
        return alphaBlending;
    }

    public Color getAmbientColor() {
        return ambientColor;
    }

    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public Color getSpecularColor() {
        return specularColor;
    }

    public Color getOtherColor() {
        return otherColor;
    }

    public Shader getShaderParameters() {
        return shader;
    }

    public byte[] getUnknown() {
        return unknown;
    }

    public Shader getShader() {
        return shader;
    }

    /**
     * Updates a color given its kind
     * @param color : color to be updated
     * @return true if color needed to be changed, false otherwise
     */
    public boolean updateColor(Color color) {
        switch (requireNonNull(color, "A color is required for material update").kind) {
            case AMBIENT:
                if (Objects.equals(ambientColor, color)) {
                    return false;
                }
                ambientColor = color;
                break;
            case DIFFUSE:
                if (Objects.equals(diffuseColor, color)) {
                    return false;
                }
                diffuseColor = color;
                break;
            case OTHER:
                if (Objects.equals(otherColor, color)) {
                    return false;
                }
                otherColor = color;
                break;
            case SPECULAR:
                if (Objects.equals(specularColor, color)) {
                    return false;
                }
                specularColor = color;
                break;
        }
        return true;
    }

    public static class MaterialSettingsBuilder extends MaterialSettings {
        public MaterialSettingsBuilder withAlpha(UByte alpha) {
            this.alpha = alpha;
            return this;
        }

        public MaterialSettingsBuilder withAmbientColor(Color ambientColor) {
            this.ambientColor = ambientColor;
            this.ambientColor.kind = AMBIENT;
            return this;
        }

        public MaterialSettingsBuilder withDiffuseColor(Color diffuseColor) {
            this.diffuseColor = diffuseColor;
            this.diffuseColor.kind = DIFFUSE;
            return this;
        }

        public MaterialSettingsBuilder withSpecularColor(Color specularColor) {
            this.specularColor = specularColor;
            this.specularColor.kind = SPECULAR;
            return this;
        }

        public MaterialSettingsBuilder withOtherColor(Color otherColor) {
            this.otherColor = otherColor;
            this.otherColor.kind = OTHER;
            return this;
        }

        public MaterialSettingsBuilder withAlphaBlending(List<UByte> alphaBlendValues) {
            this.alphaBlending = alphaBlendValues.toArray(new UByte[0]);
            return this;
        }

        public MaterialSettingsBuilder withShaderParameters(Shader shader) {
            this.shader = shader;
            return this;
        }

        public  MaterialSettingsBuilder withUnknownSettings(byte[] data) {
            this.unknown = data;
            return this;
        }

        public MaterialSettings build() {
            MaterialSettings materialSettings = new MaterialSettings();
            materialSettings.alpha = alpha;
            materialSettings.ambientColor = ambientColor;
            materialSettings.diffuseColor = diffuseColor;
            materialSettings.otherColor = otherColor;
            materialSettings.alphaBlending = alphaBlending;
            materialSettings.specularColor = specularColor;
            materialSettings.shader = shader;
            materialSettings.unknown = unknown;
            return materialSettings;
        }
    }
}
