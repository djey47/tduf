package fr.tduf.libunlimited.low.files.gfx.materials.domain;

import java.util.List;
import java.util.StringJoiner;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

/**
 * Represents a basic color structure, hosting RGB and opacity percents
 */
public class Color {
    private static final String FORMAT_COLOR_DESCRIPTION = "#%02x%02x%02x - (%d,%d,%d,%d)";

    protected float redCompound;
    protected float greenCompound;
    protected float blueCompound;
    protected float opacity;
    protected ColorKind kind = ColorKind.OTHER;

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

    public ColorKind getKind() {
        return kind;
    }

    /**
     * @return a normalized description (hex and argb specs)
     */
    public String getDescription() {
        int r = toByteCompound(redCompound);
        int g = toByteCompound(greenCompound);
        int b = toByteCompound(blueCompound);
        int o = toByteCompound(opacity);
        return String.format(FORMAT_COLOR_DESCRIPTION, r, g, b, r, g, b, o);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("Kind=" + kind)
                .add("R=" + redCompound)
                .add("G=" + greenCompound)
                .add("B=" + blueCompound)
                .add("Opacity=" + opacity)
                .toString();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    private static int toByteCompound(float compound) {
        return new Float(compound * 255).intValue();
    }

    public static ColorBuilder builder() {
        return new ColorBuilder();
    }

    public final static class ColorBuilder extends Color {
        public ColorBuilder ofKind(ColorKind colorKind) {
            kind = colorKind;
            return this;
        }

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
            color.kind = kind;
            return color;
        }
    }



    /**
     * All available color kinds
     */
    public enum ColorKind {
        AMBIENT, DIFFUSE, SPECULAR, OTHER
    }
}
