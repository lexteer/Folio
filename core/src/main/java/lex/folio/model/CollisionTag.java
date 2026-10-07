package lex.folio.model;

import java.util.Objects;

/** What a collision shape means, such as solid ground or a zone that can be passed through. */
public record CollisionTag(String name, int rgb) {
    public CollisionTag {
        Objects.requireNonNull(name, "name");
    }

    public float red() {
        return (rgb >> 16 & 0xFF) / 255f;
    }

    public float green() {
        return (rgb >> 8 & 0xFF) / 255f;
    }

    public float blue() {
        return (rgb & 0xFF) / 255f;
    }

    public static int toRgb(float red, float green, float blue) {
        return Math.round(Math.clamp(red, 0f, 1f) * 255f) << 16 | Math.round(Math.clamp(green, 0f, 1f) * 255f) << 8
            | Math.round(Math.clamp(blue, 0f, 1f) * 255f);
    }
}
