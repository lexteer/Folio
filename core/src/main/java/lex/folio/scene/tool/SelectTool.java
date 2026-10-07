package lex.folio.scene.tool;

import lex.folio.model.Layer;
import lex.folio.model.Room;
import lex.folio.model.RoomObject;
import lex.folio.scene.ActiveLayers;
import lex.folio.scene.BoxSelect;
import lex.folio.scene.ObjectDrag;
import lex.folio.scene.ObjectPicker;
import lex.folio.scene.Selection;

import java.util.ArrayList;
import java.util.List;

/** Click to select, shift or ctrl click to add to the selection, drag empty space to box select, drag objects to move. */
public class SelectTool implements SceneTool {
    private final ObjectPicker picker;
    private final Selection selection;
    private final ActiveLayers activeLayers;
    private final ObjectDrag drag;
    private final BoxSelect boxSelect;

    private Room room;
    /** What the selection was when the box started, which the box adds to. */
    private List<RoomObject> selectionBeforeBox = List.of();
    /** Set when a plain click lands on one of several selected objects: if it was no drag, only it stays selected. */
    private RoomObject clickedInGroup;

    public SelectTool(ObjectPicker picker, Selection selection, ActiveLayers activeLayers, ObjectDrag drag,
                      BoxSelect boxSelect) {
        this.picker = picker;
        this.selection = selection;
        this.activeLayers = activeLayers;
        this.drag = drag;
        this.boxSelect = boxSelect;
    }

    @Override
    public void press(Room room, float worldX, float worldY, boolean additive) {
        this.room = room;
        clickedInGroup = null;

        RoomObject object = picker.findAt(room, worldX, worldY);
        if (object == null) {
            startBox(worldX, worldY, additive);
            return;
        }

        if (additive) {
            selection.toggle(object);
            if (!selection.contains(object)) return;
        } else if (!selection.contains(object)) {
            selection.selectOnly(object);
        } else if (selection.getObjects().size() > 1) {
            clickedInGroup = object;
        }
        // New objects go to the layer of what was last picked.
        Layer<?> layer = room.findLayerOf(object);
        if (layer != null) activeLayers.set(room, layer);
        drag.start(selection.getObjects(), worldX, worldY);
    }

    private void startBox(float worldX, float worldY, boolean additive) {
        if (!additive) selection.clear();

        selectionBeforeBox = new ArrayList<>(selection.getObjects());
        boxSelect.start(worldX, worldY);
    }

    @Override
    public boolean isDragging() {
        return drag.isActive() || boxSelect.isActive();
    }

    @Override
    public void drag(float worldX, float worldY) {
        if (drag.isActive()) {
            drag.moveTo(worldX, worldY);
        } else if (boxSelect.isActive()) {
            boxSelect.moveTo(worldX, worldY);
            selectBoxContents();
        }
    }

    private void selectBoxContents() {
        List<RoomObject> result = new ArrayList<>(selectionBeforeBox);
        for (RoomObject object : picker.findIn(room,
            boxSelect.getMinX(), boxSelect.getMinY(), boxSelect.getMaxX(), boxSelect.getMaxY())) {
            if (!result.contains(object)) result.add(object);
        }
        selection.replaceWith(result);
    }

    @Override
    public void release() {
        boolean moved = drag.finish();
        if (clickedInGroup != null && !moved) {
            selection.selectOnly(clickedInGroup);
        }
        clickedInGroup = null;
        boxSelect.finish();
    }

    @Override
    public void cancel() {
        drag.cancel();
        clickedInGroup = null;
        if (boxSelect.isActive()) {
            selection.replaceWith(selectionBeforeBox);
            boxSelect.finish();
        }
    }
}
