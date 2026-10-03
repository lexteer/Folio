package lex.folio.command;

public interface Command {
    void execute();
    void undo();
}
