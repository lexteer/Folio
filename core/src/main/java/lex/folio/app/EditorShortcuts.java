package lex.folio.app;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiKey;
import lex.folio.command.CommandStack;
import lex.folio.command.ItemOrder;
import lex.folio.scene.tool.Tool;
import lex.folio.scene.tool.ToolController;
import lex.folio.scene.tool.ToolState;

import java.util.function.Consumer;

/** Keyboard shortcuts that work anywhere in the editor. */
final class EditorShortcuts {
    private final CommandStack commandStack;
    private final ToolController tools;
    private final ToolState toolState;
    private final Runnable save;
    private final Runnable deleteSelection;
    private final Consumer<ItemOrder> reorderSelection;

    EditorShortcuts(CommandStack commandStack, ToolController tools, ToolState toolState, Runnable save,
                    Runnable deleteSelection, Consumer<ItemOrder> reorderSelection) {
        this.deleteSelection = deleteSelection;
        this.reorderSelection = reorderSelection;
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
        handleFinish();
        handleOrder(io);
        if (ImGui.isKeyPressed(ImGuiKey.Delete)) {
            deleteSelection.run();
        }
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

    /** Escape first abandons what is being drawn, and only then goes back to the select tool. */
    private void handleEscape() {
        if (ImGui.isKeyPressed(ImGuiKey.Escape) && !tools.cancel()) {
            toolState.setTool(Tool.SELECT);
        }
    }

    private void handleFinish() {
        if (ImGui.isKeyPressed(ImGuiKey.Enter) || ImGui.isKeyPressed(ImGuiKey.KeypadEnter)) {
            tools.finish();
        }
    }

    private void handleOrder(ImGuiIO io) {
        boolean toEnd = io.getKeyCtrl();
        if (ImGui.isKeyPressed(ImGuiKey.RightBracket)) {
            reorderSelection.accept(toEnd ? ItemOrder.BRING_TO_FRONT : ItemOrder.BRING_FORWARD);
        }
        if (ImGui.isKeyPressed(ImGuiKey.LeftBracket)) {
            reorderSelection.accept(toEnd ? ItemOrder.SEND_TO_BACK : ItemOrder.SEND_BACKWARD);
        }
    }

    private boolean isUndoPressed(ImGuiIO io) {
        return !io.getKeyShift() && ImGui.isKeyPressed(ImGuiKey.Z);
    }

    private boolean isRedoPressed(ImGuiIO io) {
        return ImGui.isKeyPressed(ImGuiKey.Y) || (io.getKeyShift() && ImGui.isKeyPressed(ImGuiKey.Z));
    }
}
