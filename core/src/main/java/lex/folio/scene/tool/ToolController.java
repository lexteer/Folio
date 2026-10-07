package lex.folio.scene.tool;

import lex.folio.model.Room;

import java.util.Map;
import java.util.Objects;

/** Sends scene input to the active tool, and keeps drag and release with the tool that got the press. */
public class ToolController {
    private final ToolState toolState;
    private final Map<Tool, SceneTool> tools;
    private SceneTool pressedTool;
    private Tool lastTool;
    private Room lastRoom;

    public ToolController(ToolState toolState, Map<Tool, SceneTool> tools) {
        this.toolState = toolState;
        this.tools = Map.copyOf(tools);
        for (Tool tool : Tool.values()) {
            Objects.requireNonNull(this.tools.get(tool), "No implementation registered for tool " + tool);
        }
        lastTool = toolState.getTool();
    }

    /** Call every frame: what a tool was building is dropped when another tool or another room is shown. */
    public void update(Room room) {
        if (toolState.getTool() != lastTool || room != lastRoom) {
            tools.get(lastTool).cancel();
            pressedTool = null;
            lastTool = toolState.getTool();
            lastRoom = room;
        }
    }

    public void hover(float worldX, float worldY) {
        tools.get(toolState.getTool()).hover(worldX, worldY);
    }

    public void press(Room room, float worldX, float worldY, boolean additive) {
        pressedTool = tools.get(toolState.getTool());
        pressedTool.press(room, worldX, worldY, additive);
    }

    public void doubleClick(Room room, float worldX, float worldY) {
        tools.get(toolState.getTool()).doubleClick(room, worldX, worldY);
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

    /** Completes the shape being built, if any. */
    public void finish() {
        tools.get(toolState.getTool()).finish();
    }

    /** Abandons the interaction in progress, if any. Returns whether there was one. */
    public boolean cancel() {
        SceneTool tool = tools.get(toolState.getTool());
        boolean busy = isDragging() || tool.isBuilding();
        if (!busy) return false;

        (isDragging() ? pressedTool : tool).cancel();
        pressedTool = null;
        return true;
    }
}
