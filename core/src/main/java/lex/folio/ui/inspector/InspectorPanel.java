package lex.folio.ui.inspector;

import imgui.ImGui;
import imgui.flag.ImGuiHoveredFlags;
import lex.folio.model.CollisionShape;
import lex.folio.model.RoomObject;
import lex.folio.model.Sprite;
import lex.folio.scene.Selection;

import java.util.ArrayList;
import java.util.List;

public class InspectorPanel {
    public static final String TITLE = "Inspector";

    private final Selection selection;
    private final SpriteInspector spriteInspector;
    private final CollisionShapeInspector shapeInspector;
    private boolean hovered;

    public InspectorPanel(Selection selection, SpriteInspector spriteInspector,
                          CollisionShapeInspector shapeInspector) {
        this.selection = selection;
        this.spriteInspector = spriteInspector;
        this.shapeInspector = shapeInspector;
    }

    public void draw() {
        boolean visible = ImGui.begin(TITLE);
        hovered = ImGui.isWindowHovered(ImGuiHoveredFlags.RootAndChildWindows);
        if (visible) {
            drawSelected();
        }
        ImGui.end();
    }

    /** Whether the mouse was over the window when it was last drawn. */
    public boolean isHovered() {
        return hovered;
    }

    private void drawSelected() {
        List<Sprite> sprites = new ArrayList<>();
        List<CollisionShape> shapes = new ArrayList<>();
        for (RoomObject object : selection.getObjects()) {
            // Exhaustive over RoomObject: a new kind of object won't compile until it has an inspector.
            switch (object) {
                case Sprite sprite -> sprites.add(sprite);
                case CollisionShape shape -> shapes.add(shape);
            }
        }

        // Each kind of object gets one inspector that edits all selected objects of that kind at once.
        if (!sprites.isEmpty()) {
            ImGui.pushID("sprites");
            spriteInspector.draw(sprites);
            ImGui.popID();
        }
        if (!sprites.isEmpty() && !shapes.isEmpty()) {
            ImGui.separator();
        }
        if (!shapes.isEmpty()) {
            ImGui.pushID("shapes");
            shapeInspector.draw(shapes);
            ImGui.popID();
        }
    }
}
