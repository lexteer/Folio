package lex.folio.scene.tool;

import lex.folio.model.Room;
import lex.folio.model.RoomObject;
import lex.folio.model.Sprite;
import lex.folio.scene.BoxSelect;
import lex.folio.scene.Selection;
import lex.folio.scene.sprite.SpriteDrag;
import lex.folio.scene.sprite.SpritePicker;

import java.util.ArrayList;
import java.util.List;

/** Click to select, shift or ctrl click to add to the selection, drag empty space to box select, drag sprites to move. */
public class SelectTool implements SceneTool {
    private final SpritePicker spritePicker;
    private final Selection selection;
    private final SpriteDrag spriteDrag;
    private final BoxSelect boxSelect;

    private Room room;
    /** What the selection was when the box started, which the box adds to. */
    private List<RoomObject> selectionBeforeBox = List.of();
    /** Set when a plain click lands on one of several selected sprites: if it was no drag, only it stays selected. */
    private Sprite clickedInGroup;

    public SelectTool(SpritePicker spritePicker, Selection selection, SpriteDrag spriteDrag, BoxSelect boxSelect) {
        this.spritePicker = spritePicker;
        this.selection = selection;
        this.spriteDrag = spriteDrag;
        this.boxSelect = boxSelect;
    }

    @Override
    public void press(Room room, float worldX, float worldY, boolean additive) {
        this.room = room;
        clickedInGroup = null;

        Sprite sprite = spritePicker.findSpriteAt(room, worldX, worldY);
        if (sprite == null) {
            startBox(worldX, worldY, additive);
            return;
        }

        if (additive) {
            selection.toggle(sprite);
            if (!selection.contains(sprite)) return;
        } else if (!selection.contains(sprite)) {
            selection.selectOnly(sprite);
        } else if (selection.getObjects().size() > 1) {
            clickedInGroup = sprite;
        }
        spriteDrag.start(getSelectedSprites(), worldX, worldY);
    }

    private void startBox(float worldX, float worldY, boolean additive) {
        if (!additive) selection.clear();

        selectionBeforeBox = new ArrayList<>(selection.getObjects());
        boxSelect.start(worldX, worldY);
    }

    private List<Sprite> getSelectedSprites() {
        List<Sprite> sprites = new ArrayList<>();
        for (RoomObject object : selection.getObjects()) {
            if (object instanceof Sprite sprite) sprites.add(sprite);
        }
        return sprites;
    }

    @Override
    public boolean isDragging() {
        return spriteDrag.isActive() || boxSelect.isActive();
    }

    @Override
    public void drag(float worldX, float worldY) {
        if (spriteDrag.isActive()) {
            spriteDrag.moveTo(worldX, worldY);
        } else if (boxSelect.isActive()) {
            boxSelect.moveTo(worldX, worldY);
            selectBoxContents();
        }
    }

    private void selectBoxContents() {
        List<RoomObject> result = new ArrayList<>(selectionBeforeBox);
        for (Sprite sprite : spritePicker.findSpritesIn(room,
            boxSelect.getMinX(), boxSelect.getMinY(), boxSelect.getMaxX(), boxSelect.getMaxY())) {
            if (!result.contains(sprite)) result.add(sprite);
        }
        selection.replaceWith(result);
    }

    @Override
    public void release() {
        boolean moved = spriteDrag.finish();
        if (clickedInGroup != null && !moved) {
            selection.selectOnly(clickedInGroup);
        }
        clickedInGroup = null;
        boxSelect.finish();
    }

    @Override
    public void cancel() {
        spriteDrag.cancel();
        clickedInGroup = null;
        if (boxSelect.isActive()) {
            selection.replaceWith(selectionBeforeBox);
            boxSelect.finish();
        }
    }
}
