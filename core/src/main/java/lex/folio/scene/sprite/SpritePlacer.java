package lex.folio.scene.sprite;

import lex.folio.command.AddObjectCommand;
import lex.folio.command.CommandStack;
import lex.folio.model.ImageAsset;
import lex.folio.model.Layer;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;
import lex.folio.scene.ActiveLayers;

/** Adds new sprites on top of the active layer of a room. */
public class SpritePlacer {
    private final CommandStack commandStack;
    private final ActiveLayers activeLayers;

    public SpritePlacer(CommandStack commandStack, ActiveLayers activeLayers) {
        this.commandStack = commandStack;
        this.activeLayers = activeLayers;
    }

    /** The layer a sprite would be placed on now, or null if the active layer is not a sprite layer or is not editable. */
    public SpriteLayer findTargetLayer(Room room) {
        Layer<?> active = activeLayers.get(room);
        return active instanceof SpriteLayer layer && layer.isEditable() ? layer : null;
    }

    public void place(Room room, ImageAsset asset, float worldX, float worldY) {
        SpriteLayer layer = findTargetLayer(room);
        if (layer == null) return;

        Sprite sprite = new Sprite(room.createId(), asset.getId(), worldX, worldY);
        commandStack.execute(new AddObjectCommand<>(layer, sprite));
    }
}
