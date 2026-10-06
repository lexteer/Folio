package lex.folio.app;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiKey;
import lex.folio.command.CommandStack;
import lex.folio.scene.tool.Tool;
import lex.folio.scene.tool.ToolController;
import lex.folio.scene.tool.ToolState;

/** Keyboard shortcuts that work anywhere in the editor. */
final class EditorShortcuts {
    private final CommandStack commandStack;
    private final ToolController tools;
    private final ToolState toolState;
    private final Runnable save;

    EditorShortcuts(CommandStack commandStack, ToolController tools, ToolState toolState, Runnable save) {
        this.commandStack = commandStack;
        this.tools = tools;
        this.toolState = toolState;
        this.save = save;
    }

    void handle() {
        ImGuiIO io = ImGui.getIO();
        if (io.getKeyCtrl() && !io.getKeyShift() && ImGui.isKeyPressed(ImGuiKey.S)) {
            save.run();
        }
        if (io.getWantTextInput()) return;

        handleUndoRedo(io);
        handleEscape();
    }

    private void handleUndoRedo(ImGuiIO io) {
        if (!io.getKeyCtrl()) return;

        boolean redo = isRedoPressed(io);
        boolean undo = !redo && isUndoPressed(io);
        if (!redo && !undo) return;

        // Undoing while dragging means "put it back"; that is not an undo step yet.
        if (tools.cancel()) return;

        if (redo) {
            commandStack.redo();
        } else {
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
