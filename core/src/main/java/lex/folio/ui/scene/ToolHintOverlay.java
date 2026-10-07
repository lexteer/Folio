package lex.folio.ui.scene;

import imgui.ImGui;
import imgui.ImVec2;
import lex.folio.model.Room;
import lex.folio.scene.shape.ShapePlacer;
import lex.folio.scene.sprite.SpritePlacer;
import lex.folio.scene.tool.Tool;
import lex.folio.scene.tool.ToolState;
import lex.folio.ui.common.UiColors;

/** Explains why drawing does nothing, when the active layer is not one that the tool can add to. */
class ToolHintOverlay {
    private static final int TEXT_COLOR = UiColors.pack(1f, 0.85f, 0.5f, 1f);
    private static final int BACKGROUND_COLOR = UiColors.pack(0.1f, 0.1f, 0.12f, 0.9f);
    private static final float PADDING = 6f;

    private final SceneViewport viewport;
    private final ToolState toolState;
    private final SpritePlacer spritePlacer;
    private final ShapePlacer shapePlacer;

    ToolHintOverlay(SceneViewport viewport, ToolState toolState, SpritePlacer spritePlacer, ShapePlacer shapePlacer) {
        this.viewport = viewport;
        this.toolState = toolState;
        this.spritePlacer = spritePlacer;
        this.shapePlacer = shapePlacer;
    }

    /** @param toolbarBottom the screen y under which the hint goes */
    void draw(Room room, float toolbarBottom) {
        String hint = findHint(room);
        if (hint == null) return;

        ImVec2 size = ImGui.calcTextSize(hint);
        float x = viewport.getImageX() + (viewport.getWidth() - size.x) / 2f;
        float y = toolbarBottom + PADDING;
        ImGui.getWindowDrawList().addRectFilled(x - PADDING, y - PADDING / 2f, x + size.x + PADDING,
            y + size.y + PADDING / 2f, BACKGROUND_COLOR, 4f);
        ImGui.getWindowDrawList().addText(x, y, TEXT_COLOR, hint);
    }

    private String findHint(Room room) {
        Tool tool = toolState.getTool();
        if (tool == Tool.PAINT && spritePlacer.findTargetLayer(room) == null) {
            return "Choose an unlocked sprite layer in the Layers panel to paint on";
        }
        boolean drawsShapes = tool == Tool.RECT || tool == Tool.CIRCLE || tool == Tool.POLYGON
            || tool == Tool.EDGE_CHAIN;
        if (drawsShapes && shapePlacer.findTargetLayer(room) == null) {
            return "Choose an unlocked collision layer in the Layers panel to draw on";
        }
        return null;
    }
}
