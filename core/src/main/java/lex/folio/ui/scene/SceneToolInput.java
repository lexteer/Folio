package lex.folio.ui.scene;

import com.badlogic.gdx.math.Vector2;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import lex.folio.model.Room;
import lex.folio.scene.tool.ToolController;

/** Turns left mouse input over the scene into tool presses, drags and releases. */
class SceneToolInput {
    private final SceneViewport viewport;
    private final ToolController tools;

    SceneToolInput(SceneViewport viewport, ToolController tools) {
        this.viewport = viewport;
        this.tools = tools;
    }

    void handle(Room room, boolean hovered) {
        Vector2 world = viewport.getMouseWorld();
        if (hovered && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            tools.press(room, world.x, world.y);
        }
        if (!tools.isDragging()) return;

        if (ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            tools.drag(world.x, world.y);
        } else {
            tools.release();
        }
    }
}
