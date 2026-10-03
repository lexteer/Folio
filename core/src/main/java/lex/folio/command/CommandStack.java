package lex.folio.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class CommandStack {
    private static final int MAX_UNDO_STEPS = 200;
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command command) {
        Objects.requireNonNull(command, "command");
        command.execute();
        undoStack.push(command);
        redoStack.clear();
        dropOldestBeyondLimit();
    }

    private void dropOldestBeyondLimit() {
        while (undoStack.size() > MAX_UNDO_STEPS) {
            undoStack.removeLast();
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (!canUndo()) return;

        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    public void redo() {
        if (!canRedo()) return;

        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }
}
