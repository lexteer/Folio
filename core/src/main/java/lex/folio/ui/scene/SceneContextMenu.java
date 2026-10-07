package lex.folio.ui.scene;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import lex.folio.command.ItemOrder;
import lex.folio.model.Room;
import lex.folio.model.RoomObject;
import lex.folio.scene.ObjectPicker;
import lex.folio.scene.Selection;

import java.util.function.Consumer;

/** The right click menu of the scene, for changing the draw order of the selected objects. */
class SceneContextMenu {
    private static final String POPUP = "##SceneMenu";

    private final SceneViewport viewport;
    private final ObjectPicker picker;
    private final Selection selection;
    private final Consumer<ItemOrder> reorder;

    SceneContextMenu(SceneViewport viewport, ObjectPicker picker, Selection selection, Consumer<ItemOrder> reorder) {
        this.viewport = viewport;
        this.picker = picker;
        this.selection = selection;
        this.reorder = reorder;
    }

    void handle(Room room, boolean hovered) {
        if (hovered && ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
            RoomObject hit = picker.findAt(room, viewport.getMouseWorld().x, viewport.getMouseWorld().y);
            if (hit != null && !selection.contains(hit)) selection.selectOnly(hit);
            if (!selection.isEmpty()) ImGui.openPopup(POPUP);
        }
        if (!ImGui.beginPopup(POPUP)) return;

        for (ItemOrder order : ItemOrder.values()) {
            if (ImGui.menuItem(order.getLabel(), shortcutOf(order))) reorder.accept(order);
        }
        ImGui.endPopup();
    }

    static String shortcutOf(ItemOrder order) {
        return switch (order) {
            case BRING_FORWARD -> "]";
            case SEND_BACKWARD -> "[";
            case BRING_TO_FRONT -> "Ctrl+]";
            case SEND_TO_BACK -> "Ctrl+[";
        };
    }
}
