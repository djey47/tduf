package fr.tduf.libunlimited.low.files.gfx.materials.domain;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * Represents global settings for a given material
 */
public class MaterialSettings {

    protected byte alpha;
    protected byte[] saturation;
    protected float[] ambient;
    protected float[] diffuse;
    protected float[] specular;
    protected float[] other;
    protected byte[] unknown;
    protected ShaderParameters shaderParameters;

    private MaterialSettings() {}

    public static MaterialSettingsBuilder builder() {
        return new MaterialSettingsBuilder();
    }

    public byte getAlpha() {
        return alpha;
    }

    public byte[] getSaturation() {
        return saturation;
    }

    public float[] getAmbient() {
        return ambient;
    }

    public float[] getDiffuse() {
        return diffuse;
    }

    public float[] getSpecular() {
        return specular;
    }

    public float[] getOther() {
        return other;
    }

    public ShaderParameters getShaderParameters() {
        return shaderParameters;
    }

    public byte[] getUnknown() {
        return unknown;
    }

    public static class MaterialSettingsBuilder extends MaterialSettings {
        public MaterialSettingsBuilder withAlpha(byte alpha) {
            this.alpha = alpha;
            return this;
        }

        public MaterialSettingsBuilder withAmbient(List<Float> ambientValues) {
            this.ambient = ArrayUtils.toPrimitive(ambientValues.toArray(new Float[0]));
            return this;
        }

        public MaterialSettingsBuilder withDiffuse(List<Float> ambientValues) {
            this.diffuse = ArrayUtils.toPrimitive(ambientValues.toArray(new Float[0]));
            return this;
        }

        public MaterialSettingsBuilder withSpecular(List<Float> specularValues) {
            this.specular = ArrayUtils.toPrimitive(specularValues.toArray(new Float[0]));
            return this;
        }

        public MaterialSettingsBuilder withSaturation(List<Byte> saturationValues) {
            this.saturation = ArrayUtils.toPrimitive(saturationValues.toArray(new Byte[0]));
            return this;
        }

        public MaterialSettingsBuilder withOtherSettings(List<Float> otherValues) {
            this.other = ArrayUtils.toPrimitive(otherValues.toArray(new Float[0]));
            return this;
        }

        public MaterialSettingsBuilder withShaderParameters(ShaderParameters shaderParameters) {
            this.shaderParameters = shaderParameters;
            return this;
        }

        public  MaterialSettingsBuilder withUnknownSettings(byte[] data) {
            this.unknown = data;
            return this;
        }

        public MaterialSettings build() {
            MaterialSettings materialSettings = new MaterialSettings();
            materialSettings.alpha = alpha;
            materialSettings.ambient = ambient;
            materialSettings.diffuse = diffuse;
            materialSettings.other = other;
            materialSettings.saturation = saturation;
            materialSettings.specular = specular;
            materialSettings.shaderParameters = shaderParameters;
            materialSettings.unknown = unknown;

            return materialSettings;
        }
    }
}
