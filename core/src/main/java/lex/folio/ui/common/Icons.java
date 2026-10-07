package lex.folio.ui.common;

/**
 * Icons from the Font Awesome Free solid font, which is merged into the editor font so they can be used in any text.
 * Find more at https://fontawesome.com/search?o=r&m=free&s=solid (the code point is the "unicode" shown for each icon)
 * and add them here.
 */
public final class Icons {
    public static final String ADD = "\uF067";
    public static final String CLOSE = "\uF00D";
    public static final String SELECT = "\uF245";
    public static final String PAINT = "\uF1FC";
    public static final String FOLDER = "\uF07B";
    public static final String RECT = "\uF0C8";
    public static final String CIRCLE = "\uF111";
    public static final String POLYGON = "\uF5EE";
    public static final String EDGE_CHAIN = "\uF201";
    public static final String SPRITE_LAYER = "\uF03E";
    public static final String COLLISION_LAYER = "\uF5CB";
    public static final String VISIBLE = "\uF06E";
    public static final String HIDDEN = "\uF070";
    public static final String LOCKED = "\uF023";
    public static final String UNLOCKED = "\uF09C";
    public static final String MORE = "\uF142";
    public static final String REMOVE = "\uF068";

    /** The font file, relative to the assets folder. */
    public static final String FONT_FILE = "ui/FontAwesome-Solid.ttf";
    /** The size the icons are drawn at, in pixels. */
    public static final float FONT_SIZE = 14f;
    /** The code points the font is loaded for: its private use block of icons, then a terminating 0. */
    public static final short[] GLYPH_RANGES = {(short) 0xF000, (short) 0xF8FF, 0};

    private Icons() {
    }
}
