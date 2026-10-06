package lex.folio.app;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiKey;
import lex.folio.command.CommandStack;
import lex.folio.scene.SpriteDrag;
import lex.folio.scene.Tool;
import lex.folio.scene.ToolState;

final class EditorShortcuts {
    private final CommandStack commandStack;
    private final SpriteDrag spriteDrag;
    private final ToolState toolState;

    EditorShortcuts(CommandStack commandStack, SpriteDrag spriteDrag, ToolState toolState) {
        this.commandStack = commandStack;
        this.spriteDrag = spriteDrag;
        this.toolState = toolState;
    }

    void handle() {
        if (ImGui.getIO().getWantTextInput()) return;

        handleUndoRedo(ImGui.getIO());
        handleEscape();
    }

    private void handleUndoRedo(ImGuiIO io) {
        if (!io.getKeyCtrl()) return;
        if (io.getWantTextInput() || !io.getKeyCtrl()) return;

        if (isRedoPressed(io)) {
            if (spriteDrag.isActive()) {
                spriteDrag.cancel();
                return;
            }
            commandStack.redo();
        } else if (isUndoPressed(io)) {
            if (spriteDrag.isActive()) {
                spriteDrag.cancel();
                return;
            }
            commandStack.undo();
        }
    }

    private void handleEscape() {
        if (ImGui.isKeyPressed(ImGuiKey.Escape)) {
            toolState.setTool(Tool.SELECT);
        }
    }

    private boolean isUndoPressed(ImGuiIO io) {
        return !io.getKeyShift() && ImGui.isKeyPressed(ImGuiKey.Z);
    }

    private boolean isRedoPressed(ImGuiIO io) {
        return ImGui.isKeyPressed(ImGuiKey.Y) || (io.getKeyShift() && ImGui.isKeyPressed(ImGuiKey.Z));
    }
}
