package fr.tduf.libunlimited.low.files.gfx.materials.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of layers for a material
 */
public class LayerGroup {
    protected int layerCount;
    protected final List<Layer> layers = new ArrayList<>();
    protected byte[] magic;

    public static LayerGroupBuilder builder() {
        return new LayerGroupBuilder();
    }

    public int getLayerCount() {
        return layerCount;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public byte[] getMagic() {
        return magic;
    }

    public static class LayerGroupBuilder extends LayerGroup {
        public LayerGroupBuilder withLayerCount(int count) {
            this.layerCount = count;
            return this;
        }

        public LayerGroupBuilder withLayers(List<Layer> layers) {
            this.layers.clear();
            this.layers.addAll(layers);
            return this;
        }

        public LayerGroupBuilder withMagic(byte[] magic) {
            this.magic = magic;
            return this;
        }

        public LayerGroup build() {
            LayerGroup layerGroup = new LayerGroup();
            layerGroup.layerCount = layerCount;
            layerGroup.layers.addAll(layers);
            layerGroup.magic = magic;
            return layerGroup;
        }
    }
}
