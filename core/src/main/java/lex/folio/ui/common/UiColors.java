package lex.folio.ui.common;

/** Packed ABGR colors in the format ImGui's draw lists expect. */
public final class UiColors {
    /** Highlights selected and active things. */
    public static final int ACCENT = pack(0.35f, 0.65f, 1f, 1f);

    private UiColors() {
    }

    public static int pack(float red, float green, float blue, float alpha) {
        return toByte(alpha) << 24 | toByte(blue) << 16 | toByte(green) << 8 | toByte(red);
    }

    public static int withAlpha(int color, float alpha) {
        return toByte(alpha) << 24 | (color & 0x00FFFFFF);
    }

    private static int toByte(float component) {
        return (int) (Math.clamp(component, 0f, 1f) * 255f + 0.5f);
    }
}
