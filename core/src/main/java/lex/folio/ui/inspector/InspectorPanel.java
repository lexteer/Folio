package lex.folio.ui.inspector;

import imgui.ImGui;
import lex.folio.model.RoomObject;
import lex.folio.model.Sprite;
import lex.folio.scene.Selection;

import java.util.Set;

public class InspectorPanel {
    public static final String TITLE = "Inspector";

    private final Selection selection;
    private final SpriteInspector spriteInspector;

    public InspectorPanel(Selection selection, SpriteInspector spriteInspector) {
        this.selection = selection;
        this.spriteInspector = spriteInspector;
    }

    public void draw() {
        boolean visible = ImGui.begin(TITLE);
        if (visible) {
            drawSelected();
        }
        ImGui.end();
    }

    private void drawSelected() {
        Set<RoomObject> objects = selection.getObjects();
        if (objects.size() != 1) return;

        RoomObject object = objects.iterator().next();
        if (object instanceof Sprite sprite) {
            spriteInspector.draw(sprite);
        }
    }
}
