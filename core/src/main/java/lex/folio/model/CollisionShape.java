package lex.folio.model;

import java.util.Objects;

/** An area that tells the game something about the room, such as where the floor is. Its tag says what. */
public abstract sealed class CollisionShape extends RoomObject permits RectShape, CircleShape, PointShape {
    private String tag;

    protected CollisionShape(int id, float x, float y, String tag) {
        super(id, x, y);
        setTag(tag);
    }

    /** The name of one of the project's {@link CollisionTag}s. It can name a tag that has been removed since. */
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = Objects.requireNonNull(tag, "tag");
    }
}
