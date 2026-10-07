package lex.folio.ui.common;

/**
 * Icons from the Material Icons font, which is merged into the editor font so they can be used in any text.
 * Find more at https://fonts.google.com/icons (the code point is shown for each icon) and add them here.
 */
public final class Icons {
    public static final String ADD = "";
    public static final String SELECT = "\uE569";
    public static final String PAINT = "\uE3AE";
    public static final String CLOSE = "";

    /** The font file, relative to the assets folder. */
    public static final String FONT_FILE = "ui/MaterialIcons-Regular.ttf";
    /** The size the icons are drawn at, in pixels. */
    public static final float FONT_SIZE = 14f;
    /** The code points the font is loaded for: its whole private use block of icons, then a terminating 0. */
    public static final short[] GLYPH_RANGES = {(short) 0xE000, (short) 0xEB4C, 0};

    private Icons() {
    }
}
