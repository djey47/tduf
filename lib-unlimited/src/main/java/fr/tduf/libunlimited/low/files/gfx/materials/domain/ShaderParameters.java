package fr.tduf.libunlimited.low.files.gfx.materials.domain;

import java.util.List;

/**
 * Represents all parameters for a shader
 */
public class ShaderParameters {
    private MaterialPiece configuration;
    private List<MaterialSubSetting> subSettings;
    private LayerGroup layerGroup;

    public static ShaderParametersBuilder builder() {
        return new ShaderParametersBuilder();
    }

    public MaterialPiece getConfiguration() {
        return configuration;
    }

    public List<MaterialSubSetting> getSubSettings() {
        return subSettings;
    }

    public LayerGroup getLayerGroup() {
        return layerGroup;
    }

    public static class ShaderParametersBuilder {
        private MaterialPiece configuration;
        private List<MaterialSubSetting> subSettings;
        private LayerGroup layerGroup;

        public ShaderParametersBuilder withConfiguration(MaterialPiece configuration) {
            this.configuration = configuration;
            return this;
        }

        public ShaderParametersBuilder withSubSettings(List<MaterialSubSetting> subSettings) {
            this.subSettings = subSettings;
            return this;
        }

        public  ShaderParametersBuilder withLayerGroup(LayerGroup layerGroup) {
            this.layerGroup = layerGroup;
            return this;
        }

        public ShaderParameters build() {
            ShaderParameters shaderParameters = new ShaderParameters();
            shaderParameters.configuration = this.configuration;
            shaderParameters.subSettings = this.subSettings;
            shaderParameters.layerGroup = this.layerGroup;
            return shaderParameters;
        }
    }
}
