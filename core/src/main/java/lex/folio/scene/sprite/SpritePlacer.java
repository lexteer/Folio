package lex.folio.scene.sprite;

import lex.folio.command.AddObjectCommand;
import lex.folio.command.CommandStack;
import lex.folio.model.ImageAsset;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;

/** Adds new sprites to the topmost editable layer of a room. */
public class SpritePlacer {
    private final CommandStack commandStack;

    public SpritePlacer(CommandStack commandStack) {
        this.commandStack = commandStack;
    }

    public void place(Room room, ImageAsset asset, float worldX, float worldY) {
        SpriteLayer layer = findTargetLayer(room);
        if (layer == null) return;

        Sprite sprite = new Sprite(room.createId(), asset.getId(), worldX, worldY);
        commandStack.execute(new AddObjectCommand<>(layer, sprite));
    }

    private static SpriteLayer findTargetLayer(Room room) {
        for (SpriteLayer layer : room.getSpriteLayers().reversed()) {
            if (layer.isEditable()) return layer;
        }
        return null;
    }
}
