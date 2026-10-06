package lex.folio.scene;

import lex.folio.command.AddObjectCommand;
import lex.folio.command.CommandStack;
import lex.folio.model.ImageAsset;
import lex.folio.model.Layer;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;

import java.util.List;

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
        List<Layer<?>> layers = room.getLayers();

        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i) instanceof SpriteLayer layer && layer.isVisible() && !layer.isLocked()) {
                return layer;
            }
        }
        return null;
    }
}
