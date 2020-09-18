package fr.tduf.libunlimited.low.files.gfx.materials.domain;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Represents all parameters for a shader
 */
public class Shader {
    protected MaterialPiece configuration;
    protected List<MaterialSubSetting> subSettings;
    protected float[] reflectionLayerScale;

    private Shader() {}

    public static ShaderBuilder builder() {
        return new ShaderBuilder();
    }

    public MaterialPiece getConfiguration() {
        return configuration;
    }

    public List<MaterialSubSetting> getSubSettings() {
        return subSettings;
    }

    /**
     * Changes shader configuration
     * @param materialPiece : new configuration to apply
     * @return true if configuration needed to be changed, false otherwise
     */
    public boolean updateConfiguration(MaterialPiece materialPiece) {
        if (requireNonNull(materialPiece, "A material piece is required for update") == configuration) {
            return false;
        }

        configuration = materialPiece;
        return true;
    }

    public static class ShaderBuilder extends Shader {
        public ShaderBuilder withConfiguration(MaterialPiece configuration) {
            this.configuration = configuration;
            return this;
        }

        public ShaderBuilder withSubSettings(List<MaterialSubSetting> subSettings) {
            this.subSettings = subSettings;
            return this;
        }

        public ShaderBuilder withReflectionLayerScale(List<Float> reflectionLayerScaleValues) {
            this.reflectionLayerScale = reflectionLayerScaleValues == null ? null :  ArrayUtils.toPrimitive(reflectionLayerScaleValues.toArray(new Float[0]));
            return this;
        }

        public Shader build() {
            Shader shader = new Shader();
            shader.configuration = configuration;
            shader.subSettings = subSettings;
            shader.reflectionLayerScale = reflectionLayerScale;
            return shader;
        }
    }
}
