package lex.folio.app;

import imgui.ImGui;

/** The full-window dockspace the panels dock into, arranged by default unless the user saved a layout. */
final class Dockspace {
    private static final String NAME = "MainDockSpace";

    private boolean needsDefaultLayout;

    Dockspace(boolean needsDefaultLayout) {
        this.needsDefaultLayout = needsDefaultLayout;
    }

    void show() {
        int id = ImGui.getID(NAME);
        if (needsDefaultLayout) {
            DefaultDockLayout.build(id);
            needsDefaultLayout = false;
        }
        ImGui.dockSpaceOverViewport(id, ImGui.getMainViewport());
    }
}
