package fr.tduf.libunlimited.low.files.gfx.materials.domain;

import fr.tduf.libunlimited.framework.lang.UByte;

import java.util.List;

/**
 * Represents a layer within a group for a material
 */
public class Layer {
    protected String name;
    protected String textureFile;
    protected UByte[] flags;
    protected AnimationSettings animationSettings;

    public static LayerBuilder builder() {
        return new LayerBuilder();
    }

    public String getName() {
        return name;
    }

    public String getTextureFile() {
        return textureFile;
    }

    public UByte[] getFlags() {
        return flags;
    }

    public static class LayerBuilder extends Layer {
        public LayerBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public LayerBuilder withTextureFile(String textureFile) {
            this.textureFile = textureFile;
            return this;
        }

        public LayerBuilder withFlags(List<UByte> flagValues) {
            this.flags = flagValues.toArray(new UByte[0]);
            return this;
        }

        public LayerBuilder withAnimationSettings(AnimationSettings animationSettings) {
            this.animationSettings = animationSettings;
            return this;
        }

        public Layer build() {
            Layer layer = new Layer();
            layer.name = name;
            layer.textureFile = textureFile;
            layer.flags = flags;
            layer.animationSettings = animationSettings;
            return layer;
        }
    }
}
