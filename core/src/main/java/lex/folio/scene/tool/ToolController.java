package lex.folio.scene.tool;

import lex.folio.model.Room;

import java.util.Map;
import java.util.Objects;

/** Sends scene input to the active tool, and keeps drag and release with the tool that got the press. */
public class ToolController {
    private final ToolState toolState;
    private final Map<Tool, SceneTool> tools;
    private SceneTool pressedTool;

    public ToolController(ToolState toolState, Map<Tool, SceneTool> tools) {
        this.toolState = toolState;
        this.tools = Map.copyOf(tools);
        for (Tool tool : Tool.values()) {
            Objects.requireNonNull(this.tools.get(tool), "No implementation registered for tool " + tool);
        }
    }

    public void press(Room room, float worldX, float worldY) {
        pressedTool = tools.get(toolState.getTool());
        pressedTool.press(room, worldX, worldY);
    }

    public boolean isDragging() {
        return pressedTool != null && pressedTool.isDragging();
    }

    public void drag(float worldX, float worldY) {
        if (pressedTool != null) pressedTool.drag(worldX, worldY);
    }

    public void release() {
        if (pressedTool == null) return;

        pressedTool.release();
        pressedTool = null;
    }

    /** Abandons the interaction in progress, if any. Returns whether there was one. */
    public boolean cancel() {
        if (!isDragging()) return false;

        pressedTool.cancel();
        pressedTool = null;
        return true;
    }
}
