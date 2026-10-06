package lex.folio.project;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.SerializationException;
import lex.folio.model.Layer;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;

import java.io.IOException;
import java.io.StringWriter;

/** Turns a room into JSON text and back. */
final class RoomJson {
    private RoomJson() {
    }

    static String write(Room room) {
        StringWriter text = new StringWriter();
        try (JsonWriter json = new JsonWriter(text)) {
            json.object();
            json.set("name", room.getName());
            json.set("anchorX", room.getAnchorX());
            json.set("anchorY", room.getAnchorY());
            json.set("nextId", room.getNextId());
            json.array("layers");
            for (Layer<?> layer : room.getLayers()) {
                writeLayer(json, layer);
            }
            json.pop();
            json.pop();
        } catch (IOException e) {
            throw new IllegalStateException(e); // A StringWriter does not fail.
        }
        return new Json().prettyPrint(text.toString());
    }

    private static void writeLayer(JsonWriter json, Layer<?> layer) throws IOException {
        switch (layer) {
            case SpriteLayer spriteLayer -> writeSpriteLayer(json, spriteLayer);
        }
    }

    private static void writeSpriteLayer(JsonWriter json, SpriteLayer layer) throws IOException {
        json.object();
        json.set("type", "sprite");
        json.set("id", layer.getId());
        json.set("name", layer.getName());
        json.set("visible", layer.isVisible());
        json.set("locked", layer.isLocked());
        json.set("parallaxX", layer.getParallaxX());
        json.set("parallaxY", layer.getParallaxY());
        json.set("opacity", layer.getOpacity());
        json.array("items");
        for (Sprite sprite : layer.getItems()) {
            writeSprite(json, sprite);
        }
        json.pop();
        json.pop();
    }

    private static void writeSprite(JsonWriter json, Sprite sprite) throws IOException {
        json.object();
        json.set("id", sprite.getId());
        json.set("asset", sprite.getAssetId());
        json.set("x", sprite.getX());
        json.set("y", sprite.getY());
        json.set("rotation", sprite.getRotationDegrees());
        json.set("scaleX", sprite.getScaleX());
        json.set("scaleY", sprite.getScaleY());
        json.set("flipX", sprite.isFlipX());
        json.set("flipY", sprite.isFlipY());
        json.set("tint", sprite.getTint());
        json.pop();
    }

    static Room read(String text) throws IOException {
        try {
            JsonValue root = new JsonReader().parse(text);
            Room room = new Room(root.getString("name"));
            room.setAnchor(root.getFloat("anchorX", 0f), root.getFloat("anchorY", 0f));
            room.setNextId(root.getInt("nextId", 1));
            for (JsonValue layer : root.get("layers")) {
                room.addLayer(readLayer(layer));
            }
            return room;
        } catch (SerializationException | IllegalArgumentException | NullPointerException e) {
            throw new IOException("Invalid room data: " + e.getMessage(), e);
        }
    }

    private static Layer<?> readLayer(JsonValue json) {
        String type = json.getString("type");
        if (!type.equals("sprite")) throw new IllegalArgumentException("Unknown layer type: " + type);

        SpriteLayer layer = new SpriteLayer(json.getInt("id"), json.getString("name"));
        layer.setVisible(json.getBoolean("visible", true));
        layer.setLocked(json.getBoolean("locked", false));
        layer.setParallax(json.getFloat("parallaxX", 1f), json.getFloat("parallaxY", 1f));
        layer.setOpacity(json.getFloat("opacity", 1f));
        for (JsonValue item : json.get("items")) {
            layer.add(readSprite(item));
        }
        return layer;
    }

    private static Sprite readSprite(JsonValue json) {
        Sprite sprite = new Sprite(json.getInt("id"), json.getString("asset"), json.getFloat("x"), json.getFloat("y"));
        sprite.setRotationDegrees(json.getFloat("rotation", 0f));
        sprite.setScale(json.getFloat("scaleX", 1f), json.getFloat("scaleY", 1f));
        sprite.setFlipX(json.getBoolean("flipX", false));
        sprite.setFlipY(json.getBoolean("flipY", false));
        sprite.setTint(json.getInt("tint", Sprite.NO_TINT));
        return sprite;
    }
}
