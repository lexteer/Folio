package lex.folio.ui.scene;

import imgui.ImGui;
import imgui.flag.ImGuiHoveredFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import lex.folio.model.Room;
import lex.folio.scene.render.SceneRenderer;

/** The window showing the room: renders it, draws the overlay, and passes on the input. */
public class ScenePanel {
    public static final String TITLE = "Scene";
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;

    private final Room room;
    private final SceneRenderer renderer;
    private final SceneViewport viewport;
    private final SceneOverlay overlay;
    private final SceneInput input;
    private boolean hovered;

    public ScenePanel(Room room, SceneRenderer renderer, SceneViewport viewport, SceneOverlay overlay,
                      SceneInput input) {
        this.room = room;
        this.renderer = renderer;
        this.viewport = viewport;
        this.overlay = overlay;
        this.input = input;
    }

    public void draw() {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        boolean visible = ImGui.begin(TITLE, WINDOW_FLAGS);
        ImGui.popStyleVar();
        hovered = ImGui.isWindowHovered(ImGuiHoveredFlags.RootAndChildWindows);

        if (visible) {
            drawContent();
        }
        ImGui.end();
    }

    /** Whether the mouse was over the window, including its overlay, when it was last drawn. */
    public boolean isHovered() {
        return hovered;
    }

    private void drawContent() {
        int width = (int) ImGui.getContentRegionAvailX();
        int height = (int) ImGui.getContentRegionAvailY();
        if (width <= 0 || height <= 0) return;

        renderer.render(room, width, height);
        ImGui.image(renderer.getTextureHandle(), width, height, 0, 1, 1, 0);
        viewport.updateFromLastItem();
        boolean imageHovered = ImGui.isItemHovered();
        input.acceptDrops(room);

        overlay.draw(room);
        input.handle(room, imageHovered && !overlay.isHovered());
    }
}
