package lex.folio.ui.layers;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDragDropFlags;
import imgui.flag.ImGuiHoveredFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import lex.folio.command.AddLayerCommand;
import lex.folio.command.CommandStack;
import lex.folio.command.MoveLayerCommand;
import lex.folio.command.RemoveLayerCommand;
import lex.folio.command.SetValueCommand;
import lex.folio.model.CollisionLayer;
import lex.folio.model.Layer;
import lex.folio.model.Room;
import lex.folio.model.SpriteLayer;
import lex.folio.scene.ActiveLayers;
import lex.folio.ui.common.Icons;
import lex.folio.ui.common.UiColors;

import java.util.List;
import java.util.function.Supplier;

/**
 * The layers of the shown room, the topmost layer first. Layers can be renamed with a double click, dragged to change
 * their order, hidden and locked. The three dots open the rest of a layer's settings.
 */
public class LayersPanel {
    public static final String TITLE = "Layers";
    private static final String DRAG_PAYLOAD = "LAYER";
    private static final String ADD_MENU = "##AddLayerMenu";
    private static final String LAYER_MENU = "##LayerMenu";
    private static final String DELETE_POPUP = "Delete layer";
    private static final float ROW_PADDING_Y = 5f;
    private static final float ROW_PADDING_X = 8f;
    /** The room the type icon has before the name starts, which makes the names line up. */
    private static final float ICON_SLOT_IN_FONT_SIZES = 1.8f;
    private static final float DROP_LINE_THICKNESS = 2f;
    private static final int NAME_MAX_LENGTH = 64;
    private static final float SETTINGS_WIDTH_IN_FONT_SIZES = 14f;
    private static final float SETTINGS_LABEL_WIDTH_IN_FONT_SIZES = 5f;

    private final Supplier<Room> activeRoom;
    private final ActiveLayers activeLayers;
    private final CommandStack commandStack;
    private boolean hovered;

    private Layer<?> renaming;
    private boolean renameActive;
    private final ImString renameText = new ImString(NAME_MAX_LENGTH);

    private Layer<?> layerToDelete;
    private boolean openDeletePopup;

    /** A layer dropped on another one while drawing the rows, which is moved once all rows are drawn. */
    private Layer<?> droppedLayer;
    private Layer<?> dropTarget;
    private boolean dropAbove;

    private SpriteLayerSettings settingsBeforeEdit;

    /** The settings of a sprite layer that are edited together, so that one edit is one undo step. */
    private record SpriteLayerSettings(float parallaxX, float parallaxY, float opacity) {
        static SpriteLayerSettings of(SpriteLayer layer) {
            return new SpriteLayerSettings(layer.getParallaxX(), layer.getParallaxY(), layer.getOpacity());
        }

        void applyTo(SpriteLayer layer) {
            layer.setParallax(parallaxX, parallaxY);
            layer.setOpacity(opacity);
        }
    }

    public LayersPanel(Supplier<Room> activeRoom, ActiveLayers activeLayers, CommandStack commandStack) {
        this.activeRoom = activeRoom;
        this.activeLayers = activeLayers;
        this.commandStack = commandStack;
    }

    public void draw() {
        boolean visible = ImGui.begin(TITLE);
        hovered = ImGui.isWindowHovered(ImGuiHoveredFlags.RootAndChildWindows);
        if (visible) {
            Room room = activeRoom.get();
            if (room == null) {
                ImGui.textDisabled("No room is open.");
            } else {
                drawLayers(room);
            }
        }
        ImGui.end();
    }

    /** Whether the mouse was over the window when it was last drawn. */
    public boolean isHovered() {
        return hovered;
    }

    private void drawLayers(Room room) {
        drawAddButton(room);
        ImGui.separator();

        droppedLayer = null;
        List<Layer<?>> layers = room.getLayers();
        for (int index = layers.size() - 1; index >= 0; index--) {
            drawRow(room, layers.get(index));
        }
        if (renaming != null && !layers.contains(renaming)) renaming = null;
        applyDrop(room);
        drawDeletePopup(room);
    }

    private void drawAddButton(Room room) {
        if (ImGui.button(Icons.ADD + " Add Layer")) ImGui.openPopup(ADD_MENU);
        if (!ImGui.beginPopup(ADD_MENU)) return;

        if (ImGui.menuItem(Icons.SPRITE_LAYER + "  Sprite Layer")) {
            addLayer(room, new SpriteLayer(room.createId(), uniqueName(room, "Sprite Layer")));
        }
        if (ImGui.menuItem(Icons.COLLISION_LAYER + "  Collision Layer")) {
            addLayer(room, new CollisionLayer(room.createId(), uniqueName(room, "Collision Layer")));
        }
        ImGui.endPopup();
    }

    /** The new layer goes right above the active one, or on top if there is none, and becomes the active one. */
    private void addLayer(Room room, Layer<?> layer) {
        Layer<?> active = activeLayers.get(room);
        int index = active == null ? room.getLayers().size() : room.getLayers().indexOf(active) + 1;
        commandStack.execute(new AddLayerCommand(room, layer, index));
        activeLayers.set(room, layer);
    }

    private static String uniqueName(Room room, String base) {
        String name = base;
        for (int number = 2; isNameUsed(room, name); number++) {
            name = base + " " + number;
        }
        return name;
    }

    private static boolean isNameUsed(Room room, String name) {
        return room.getLayers().stream().anyMatch(layer -> layer.getName().equals(name));
    }

    private static String typeIconOf(Layer<?> layer) {
        return switch (layer) {
            case SpriteLayer sprites -> Icons.SPRITE_LAYER;
            case CollisionLayer shapes -> Icons.COLLISION_LAYER;
        };
    }

    private static float getRowHeight() {
        return ImGui.getTextLineHeight() + 2f * ROW_PADDING_Y;
    }

    private void drawRow(Room room, Layer<?> layer) {
        ImGui.pushID(layer.getId());
        if (layer == renaming) {
            drawRenameRow(layer);
        } else {
            drawNormalRow(room, layer);
        }
        ImGui.popID();
    }

    private void drawNormalRow(Room room, Layer<?> layer) {
        float rowHeight = getRowHeight();
        float slot = rowHeight;
        float startX = ImGui.getCursorPosX();
        float rightEdge = startX + ImGui.getContentRegionAvailX();

        boolean active = activeLayers.get(room) == layer;
        if (ImGui.selectable("##row", active, ImGuiSelectableFlags.AllowOverlap, 0f, rowHeight)) {
            activeLayers.set(room, layer);
        }
        ImGui.openPopupOnItemClick(LAYER_MENU);
        boolean rowHovered = ImGui.isItemHovered();
        float minX = ImGui.getItemRectMinX();
        float minY = ImGui.getItemRectMinY();
        float maxX = ImGui.getItemRectMaxX();
        float maxY = ImGui.getItemRectMaxY();
        handleDragAndDrop(layer, minX, minY, maxX, maxY);

        // The buttons sit on the row: they are submitted after it, so they get the mouse first.
        float buttonsLeft = rightEdge - 3f * slot;
        ImGui.sameLine(buttonsLeft, 0f);
        ImGui.pushID("lock");
        if (drawIconButton(layer.isLocked() ? Icons.LOCKED : Icons.UNLOCKED, slot, rowHeight, !layer.isLocked(),
            layer.isLocked() ? "Unlock" : "Lock")) {
            setLocked(layer, !layer.isLocked());
        }
        ImGui.popID();
        ImGui.sameLine(0f, 0f);
        ImGui.pushID("eye");
        if (drawIconButton(layer.isVisible() ? Icons.VISIBLE : Icons.HIDDEN, slot, rowHeight, !layer.isVisible(),
            layer.isVisible() ? "Hide" : "Show")) {
            setVisible(layer, !layer.isVisible());
        }
        ImGui.popID();
        ImGui.sameLine(0f, 0f);
        ImGui.pushID("more");
        boolean openMenu = drawIconButton(Icons.MORE, slot, rowHeight, false, "More settings");
        ImGui.popID();
        // The menu is looked up by the ID of the row, so it is opened outside the button's own ID.
        if (openMenu) ImGui.openPopup(LAYER_MENU);

        float nameRightEdge = minX + buttonsLeft - startX;
        drawRowContent(layer, minX, minY, nameRightEdge);
        if (rowHovered && ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left) && ImGui.getMousePosX() < nameRightEdge) {
            startRename(layer);
        }
        drawLayerMenu(room, layer);
    }

    /** The type icon and the name, drawn on the row. */
    private void drawRowContent(Layer<?> layer, float minX, float minY, float nameRightEdge) {
        float textY = minY + ROW_PADDING_Y;
        int color = ImGui.getColorU32(layer.isVisible() ? ImGuiCol.Text : ImGuiCol.TextDisabled);
        String icon = typeIconOf(layer);
        float iconX = minX + ROW_PADDING_X;
        ImGui.getWindowDrawList().addText(ImGui.getFont(), Math.round(Icons.FONT_SIZE), Math.round(iconX), textY,
            color, icon);

        float nameX = iconX + ImGui.getFontSize() * ICON_SLOT_IN_FONT_SIZES;
        ImGui.getWindowDrawList().pushClipRect(nameX, minY, nameRightEdge, minY + getRowHeight(), true);
        ImGui.getWindowDrawList().addText(nameX, textY, color, layer.getName());
        ImGui.getWindowDrawList().popClipRect();
    }

    /** One icon button of a row. Returns whether it was clicked. */
    private static boolean drawIconButton(String icon, float width, float height, boolean dimmed, String tooltip) {
        boolean clicked = ImGui.invisibleButton("##button", width, height);
        float minX = ImGui.getItemRectMinX();
        float minY = ImGui.getItemRectMinY();
        if (ImGui.isItemHovered()) {
            ImGui.getWindowDrawList().addRectFilled(minX + 1f, minY + 1f, minX + width - 1f, minY + height - 1f,
                ImGui.getColorU32(ImGuiCol.ButtonHovered), 4f);
            ImGui.setTooltip(tooltip);
        }
        ImVec2 size = ImGui.calcTextSize(icon);
        int color = ImGui.getColorU32(dimmed ? ImGuiCol.TextDisabled : ImGuiCol.Text);
        ImGui.getWindowDrawList().addText(ImGui.getFont(), Math.round(Icons.FONT_SIZE),
            Math.round(minX + (width - size.x) / 2f), Math.round(minY + (height - size.y) / 2f), color, icon);
        return clicked;
    }

    private void handleDragAndDrop(Layer<?> layer, float minX, float minY, float maxX, float maxY) {
        if (ImGui.beginDragDropSource()) {
            ImGui.setDragDropPayload(DRAG_PAYLOAD, layer);
            ImGui.text(layer.getName());
            ImGui.endDragDropSource();
        }
        if (!ImGui.beginDragDropTarget()) return;

        // The class is not passed on purpose: ImGui checks it the wrong way around, so a Layer class never matches.
        Layer<?> dragged = ImGui.acceptDragDropPayload(DRAG_PAYLOAD,
            ImGuiDragDropFlags.AcceptBeforeDelivery | ImGuiDragDropFlags.AcceptNoDrawDefaultRect);
        if (dragged != null && dragged != layer) {
            boolean above = ImGui.getMousePosY() < (minY + maxY) / 2f;
            float lineY = above ? minY : maxY;
            ImGui.getWindowDrawList().addLine(minX, lineY, maxX, lineY, UiColors.ACCENT, DROP_LINE_THICKNESS);
            if (ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
                droppedLayer = dragged;
                dropTarget = layer;
                dropAbove = above;
            }
        }
        ImGui.endDragDropTarget();
    }

    /** Moves the dropped layer next to the layer it was dropped on. Above means later in the draw order. */
    private void applyDrop(Room room) {
        if (droppedLayer == null) return;

        List<Layer<?>> layers = room.getLayers();
        int from = layers.indexOf(droppedLayer);
        int targetIndex = layers.indexOf(dropTarget);
        droppedLayer = null;
        if (from < 0 || targetIndex < 0) return;

        int indexWithoutDragged = targetIndex - (from < targetIndex ? 1 : 0);
        int to = dropAbove ? indexWithoutDragged + 1 : indexWithoutDragged;
        if (to != from) commandStack.execute(new MoveLayerCommand(room, from, to));
    }

    private void setVisible(Layer<?> layer, boolean visible) {
        commandStack.execute(new SetValueCommand<>(layer::setVisible, layer.isVisible(), visible));
    }

    private void setLocked(Layer<?> layer, boolean locked) {
        commandStack.execute(new SetValueCommand<>(layer::setLocked, layer.isLocked(), locked));
    }

    private void startRename(Layer<?> layer) {
        renaming = layer;
        renameActive = false;
        renameText.set(layer.getName());
    }

    /** The row while its name is edited: a text field applies when it loses focus, and Escape leaves the name as is. */
    private void drawRenameRow(Layer<?> layer) {
        float iconX = ImGui.getCursorScreenPosX() + ROW_PADDING_X;
        float iconY = ImGui.getCursorScreenPosY() + ROW_PADDING_Y;
        ImGui.getWindowDrawList().addText(ImGui.getFont(), Math.round(Icons.FONT_SIZE), Math.round(iconX),
            iconY, ImGui.getColorU32(ImGuiCol.Text), typeIconOf(layer));
        ImGui.setCursorPosX(ImGui.getCursorPosX() + ROW_PADDING_X + ImGui.getFontSize() * ICON_SLOT_IN_FONT_SIZES);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ImGui.getStyle().getFramePaddingX(), ROW_PADDING_Y);
        ImGui.setNextItemWidth(-1f);
        if (!renameActive) ImGui.setKeyboardFocusHere();
        ImGui.inputText("##rename", renameText, ImGuiInputTextFlags.AutoSelectAll | ImGuiInputTextFlags.EnterReturnsTrue);
        ImGui.popStyleVar();

        if (ImGui.isItemActive()) {
            renameActive = true;
        } else if (renameActive) {
            renaming = null;
            String name = renameText.get().trim();
            if (!name.isEmpty() && !name.equals(layer.getName())) {
                commandStack.execute(new SetValueCommand<>(layer::setName, layer.getName(), name));
            }
        }
    }

    private void drawLayerMenu(Room room, Layer<?> layer) {
        if (!ImGui.beginPopup(LAYER_MENU)) return;

        if (ImGui.menuItem("Rename")) startRename(layer);
        if (ImGui.menuItem("Delete")) requestDelete(room, layer);
        if (layer instanceof SpriteLayer spriteLayer) {
            ImGui.separator();
            drawSpriteSettings(spriteLayer);
        }
        ImGui.endPopup();
    }

    /** Asks first if the layer has anything on it. */
    private void requestDelete(Room room, Layer<?> layer) {
        if (layer.getItems().isEmpty()) {
            commandStack.execute(new RemoveLayerCommand(room, layer));
        } else {
            layerToDelete = layer;
            openDeletePopup = true;
        }
    }

    private void drawDeletePopup(Room room) {
        if (openDeletePopup) {
            ImGui.openPopup(DELETE_POPUP);
            openDeletePopup = false;
        }
        if (!ImGui.beginPopupModal(DELETE_POPUP, ImGuiWindowFlags.AlwaysAutoResize)) return;

        if (layerToDelete == null || !room.getLayers().contains(layerToDelete)) {
            layerToDelete = null;
            ImGui.closeCurrentPopup();
            ImGui.endPopup();
            return;
        }
        int count = layerToDelete.getItems().size();
        ImGui.text("Delete \"" + layerToDelete.getName() + "\" and its " + count + (count == 1 ? " object?" : " objects?"));
        ImGui.text("This can be undone.");
        ImGui.spacing();
        if (ImGui.button("Delete")) {
            commandStack.execute(new RemoveLayerCommand(room, layerToDelete));
            layerToDelete = null;
            ImGui.closeCurrentPopup();
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel")) {
            layerToDelete = null;
            ImGui.closeCurrentPopup();
        }
        ImGui.endPopup();
    }

    /** Parallax and opacity change the layer while they are dragged, and are one undo step once let go. */
    private void drawSpriteSettings(SpriteLayer layer) {
        float labelWidth = ImGui.getFontSize() * SETTINGS_LABEL_WIDTH_IN_FONT_SIZES;
        float fieldsWidth = ImGui.getFontSize() * SETTINGS_WIDTH_IN_FONT_SIZES;
        float spacing = ImGui.getStyle().getItemSpacingX();

        ImGui.alignTextToFramePadding();
        ImGui.text("Parallax");
        ImGui.sameLine(labelWidth);
        float[] parallaxX = {layer.getParallaxX()};
        ImGui.setNextItemWidth((fieldsWidth - spacing) / 2f);
        boolean changedX = ImGui.dragFloat("##parallaxX", parallaxX, 0.01f, 0f, 10f, "X %.2f");
        startEditIfActivated(layer);
        if (changedX) layer.setParallax(parallaxX[0], layer.getParallaxY());
        finishEditIfDone(layer);

        ImGui.sameLine();
        float[] parallaxY = {layer.getParallaxY()};
        ImGui.setNextItemWidth((fieldsWidth - spacing) / 2f);
        boolean changedY = ImGui.dragFloat("##parallaxY", parallaxY, 0.01f, 0f, 10f, "Y %.2f");
        startEditIfActivated(layer);
        if (changedY) layer.setParallax(layer.getParallaxX(), parallaxY[0]);
        finishEditIfDone(layer);

        ImGui.alignTextToFramePadding();
        ImGui.text("Opacity");
        ImGui.sameLine(labelWidth);
        float[] opacity = {layer.getOpacity()};
        ImGui.setNextItemWidth(fieldsWidth);
        boolean changedOpacity = ImGui.sliderFloat("##opacity", opacity, 0f, 1f, "%.2f");
        startEditIfActivated(layer);
        if (changedOpacity) layer.setOpacity(opacity[0]);
        finishEditIfDone(layer);
    }

    /** Call right after a settings field, before its value is applied: remembers how the settings were. */
    private void startEditIfActivated(SpriteLayer layer) {
        if (ImGui.isItemActivated()) settingsBeforeEdit = SpriteLayerSettings.of(layer);
    }

    /** Call after a settings field was applied: records the change as one undo step once the field is let go of. */
    private void finishEditIfDone(SpriteLayer layer) {
        if (!ImGui.isItemDeactivatedAfterEdit() || settingsBeforeEdit == null) return;

        SpriteLayerSettings before = settingsBeforeEdit;
        SpriteLayerSettings after = SpriteLayerSettings.of(layer);
        settingsBeforeEdit = null;
        if (!before.equals(after)) {
            commandStack.execute(new SetValueCommand<>(settings -> settings.applyTo(layer), before, after));
        }
    }
}
