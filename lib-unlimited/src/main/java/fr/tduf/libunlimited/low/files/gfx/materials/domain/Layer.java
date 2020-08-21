package fr.tduf.libunlimited.low.files.gfx.materials.domain;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * Represents a layer within a group for a material
 */
public class Layer {
    protected String name;
    protected String textureFile;
    protected byte[] flags;
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

    public byte[] getFlags() {
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

        public LayerBuilder withFlags(List<Byte> flagValues) {
            this.flags = ArrayUtils.toPrimitive(flagValues.toArray(new Byte[0]));
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
