package fr.tduf.libunlimited.low.files.gfx.materials.domain;

import java.util.List;
import java.util.StringJoiner;

/**
 * Represents a basic color structure, hosting RGB and opacity percents
 */
public class Color {
    protected float redCompound;
    protected float greenCompound;
    protected float blueCompound;
    protected float opacity;

    private Color() {}

    public float getRedCompound() {
        return redCompound;
    }

    public float getGreenCompound() {
        return greenCompound;
    }

    public float getBlueCompound() {
        return blueCompound;
    }

    public float getOpacity() {
        return opacity;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("R=" + redCompound)
                .add("G=" + greenCompound)
                .add("B=" + blueCompound)
                .add("Opacity=" + opacity)
                .toString();
    }

    public static ColorBuilder builder() {
        return new ColorBuilder();
    }

    public final static class ColorBuilder extends Color {
        public ColorBuilder fromRGB(float r, float g, float b) {
            redCompound = r;
            greenCompound = g;
            blueCompound = b;
            return this;
        }

        public ColorBuilder fromRGBAndOpacity(List<Float> values) {
            redCompound = values.get(0);
            greenCompound = values.get(1);
            blueCompound = values.get(2);
            opacity = values.get(3);
            return this;
        }

        public  ColorBuilder withOpacity(float o) {
            opacity = o;
            return  this;
        }

        public Color build() {
            Color color = new Color();
            color.redCompound = redCompound;
            color.greenCompound = greenCompound;
            color.blueCompound = blueCompound;
            color.opacity = opacity;
            return color;
        }
    }
}
