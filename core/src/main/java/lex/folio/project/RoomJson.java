package lex.folio.project;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.SerializationException;
import lex.folio.model.CircleShape;
import lex.folio.model.CollisionLayer;
import lex.folio.model.CollisionShape;
import lex.folio.model.CollisionTags;
import lex.folio.model.EdgeChainShape;
import lex.folio.model.Layer;
import lex.folio.model.PointShape;
import lex.folio.model.PolygonShape;
import lex.folio.model.RectShape;
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
            case CollisionLayer collisionLayer -> writeCollisionLayer(json, collisionLayer);
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

    private static void writeCollisionLayer(JsonWriter json, CollisionLayer layer) throws IOException {
        json.object();
        json.set("type", "collision");
        json.set("id", layer.getId());
        json.set("name", layer.getName());
        json.set("visible", layer.isVisible());
        json.set("locked", layer.isLocked());
        json.array("items");
        for (CollisionShape shape : layer.getItems()) {
            writeShape(json, shape);
        }
        json.pop();
        json.pop();
    }

    private static void writeShape(JsonWriter json, CollisionShape shape) throws IOException {
        json.object();
        json.set("shape", switch (shape) {
            case RectShape rect -> "rect";
            case CircleShape circle -> "circle";
            case PolygonShape polygon -> "polygon";
            case EdgeChainShape chain -> "chain";
        });
        json.set("id", shape.getId());
        json.set("x", shape.getX());
        json.set("y", shape.getY());
        json.set("tag", shape.getTag());
        switch (shape) {
            case RectShape rect -> {
                json.set("width", rect.getWidth());
                json.set("height", rect.getHeight());
            }
            case CircleShape circle -> json.set("radius", circle.getRadius());
            case PointShape points -> {
                json.array("points");
                for (int i = 0; i < points.getPointCount(); i++) {
                    json.value(points.getPointX(i));
                    json.value(points.getPointY(i));
                }
                json.pop();
            }
        }
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
        return switch (type) {
            case "sprite" -> readSpriteLayer(json);
            case "collision" -> readCollisionLayer(json);
            default -> throw new IllegalArgumentException("Unknown layer type: " + type);
        };
    }

    private static SpriteLayer readSpriteLayer(JsonValue json) {
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

    private static CollisionLayer readCollisionLayer(JsonValue json) {
        CollisionLayer layer = new CollisionLayer(json.getInt("id"), json.getString("name"));
        layer.setVisible(json.getBoolean("visible", true));
        layer.setLocked(json.getBoolean("locked", false));
        for (JsonValue item : json.get("items")) {
            layer.add(readShape(item));
        }
        return layer;
    }

    private static CollisionShape readShape(JsonValue json) {
        int id = json.getInt("id");
        float x = json.getFloat("x");
        float y = json.getFloat("y");
        String tag = json.getString("tag", CollisionTags.DEFAULT_NAME);
        String shape = json.getString("shape");
        return switch (shape) {
            case "rect" -> new RectShape(id, x, y, json.getFloat("width"), json.getFloat("height"), tag);
            case "circle" -> new CircleShape(id, x, y, json.getFloat("radius"), tag);
            case "polygon" -> new PolygonShape(id, x, y, json.get("points").asFloatArray(), tag);
            case "chain" -> new EdgeChainShape(id, x, y, json.get("points").asFloatArray(), tag);
            default -> throw new IllegalArgumentException("Unknown shape: " + shape);
        };
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
