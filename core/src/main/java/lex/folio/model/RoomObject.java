package lex.folio.model;

public abstract sealed class RoomObject permits Sprite, CollisionShape {
    private final int id;
    private float x;
    private float y;

    protected RoomObject(int id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
