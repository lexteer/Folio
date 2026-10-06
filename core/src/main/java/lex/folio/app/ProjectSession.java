package lex.folio.app;

import com.badlogic.gdx.utils.Disposable;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import lex.folio.assets.AssetLibrary;
import lex.folio.command.CommandStack;
import lex.folio.model.Project;
import lex.folio.model.Room;
import lex.folio.model.SpriteLayer;
import lex.folio.project.RoomStorage;
import lex.folio.scene.BoxSelect;
import lex.folio.scene.Selection;
import lex.folio.scene.render.SceneRenderer;
import lex.folio.scene.sprite.SpriteDrag;
import lex.folio.scene.sprite.SpriteGeometry;
import lex.folio.scene.sprite.SpritePicker;
import lex.folio.scene.sprite.SpritePlacer;
import lex.folio.scene.tool.PaintTool;
import lex.folio.scene.tool.SceneTool;
import lex.folio.scene.tool.SelectTool;
import lex.folio.scene.tool.Tool;
import lex.folio.scene.tool.ToolController;
import lex.folio.scene.tool.ToolState;
import lex.folio.ui.assets.AssetsPanel;
import lex.folio.ui.inspector.InspectorPanel;
import lex.folio.ui.inspector.SpriteInspector;
import lex.folio.ui.scene.SceneInput;
import lex.folio.ui.scene.SceneOverlay;
import lex.folio.ui.scene.ScenePanel;
import lex.folio.ui.scene.SceneViewport;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Everything that exists while one project is open: creates the parts, wires them together and draws them. */
final class ProjectSession implements Disposable {
    private final AssetLibrary assetLibrary;
    private final SceneRenderer sceneRenderer;
    private final ScenePanel scenePanel;
    private final InspectorPanel inspectorPanel;
    private final AssetsPanel assetsPanel;
    private final EditorShortcuts shortcuts;
    private final Selection selection;

    ProjectSession(Project project, Consumer<String> errorSink) throws IOException {
        RoomStorage roomStorage = new RoomStorage(project);
        List<Room> savedRooms = roomStorage.loadAll();

        assetLibrary = new AssetLibrary(project);
        assetLibrary.loadAll();

        CommandStack commandStack = new CommandStack();
        Selection selection = new Selection();
        BoxSelect boxSelect = new BoxSelect();
        ToolState toolState = new ToolState();

        SpriteGeometry spriteGeometry = new SpriteGeometry(assetLibrary, project.getPixelsPerMeter());
        SpritePlacer spritePlacer = new SpritePlacer(commandStack);
        ToolController tools = createTools(commandStack, selection, boxSelect, toolState, spriteGeometry, spritePlacer);

        SceneViewport viewport = new SceneViewport();
        sceneRenderer = new SceneRenderer(assetLibrary, spriteGeometry);

        scenePanel = new ScenePanel(savedRooms, project.getPixelsPerMeter(), ProjectSession::createRoom,
            roomStorage, errorSink, sceneRenderer, viewport,
            new SceneOverlay(viewport, spriteGeometry, selection, boxSelect, toolState),
            new SceneInput(viewport, tools, spritePlacer), selection);
        if (savedRooms.isEmpty()) scenePanel.openNewRoom(createRoom("main"));
        commandStack.setChangeListener(scenePanel::roomsChanged);
        this.selection = selection;
        inspectorPanel = new InspectorPanel(selection, new SpriteInspector(commandStack));
        assetsPanel = AssetsPanel.create(project, assetLibrary, commandStack, toolState);
        shortcuts = new EditorShortcuts(commandStack, tools, toolState, scenePanel::saveActiveRoom);
    }

    /** A new room starts with one sprite layer. */
    private static Room createRoom(String name) {
        Room room = new Room(name);
        room.addLayer(new SpriteLayer(room.createId(), "Art"));
        return room;
    }

    /** Add a tool here, and to the {@link Tool} enum, to make it available. */
    private static ToolController createTools(CommandStack commandStack, Selection selection, BoxSelect boxSelect,
                                              ToolState toolState, SpriteGeometry spriteGeometry,
                                              SpritePlacer spritePlacer) {
        Map<Tool, SceneTool> tools = new EnumMap<>(Tool.class);
        tools.put(Tool.SELECT, new SelectTool(new SpritePicker(spriteGeometry), selection,
            new SpriteDrag(commandStack), boxSelect));
        tools.put(Tool.PAINT, new PaintTool(toolState, spritePlacer));
        return new ToolController(toolState, tools);
    }

    void draw() {
        scenePanel.draw();
        inspectorPanel.draw();
        assetsPanel.draw();
        deselectWhenClickedOutsideSceneAndInspector();
        shortcuts.handle();
    }

    /** The inspector edits the selection, so it has to keep it. The scene handles its own clicks. */
    private void deselectWhenClickedOutsideSceneAndInspector() {
        boolean clicked = ImGui.isMouseClicked(ImGuiMouseButton.Left) || ImGui.isMouseClicked(ImGuiMouseButton.Right);
        if (clicked && !scenePanel.isHovered() && !inspectorPanel.isHovered()) {
            selection.clear();
        }
    }

    boolean canSaveActiveRoom() {
        return scenePanel.canSaveActiveRoom();
    }

    void saveActiveRoom() {
        scenePanel.saveActiveRoom();
    }

    boolean hasUnsavedRooms() {
        return scenePanel.hasUnsavedRooms();
    }

    /** Asks about each unsaved room, then runs {@code action}, unless the user cancels. */
    void runWhenNothingIsUnsaved(Runnable action) {
        scenePanel.runWhenNothingIsUnsaved(action);
    }

    void filesDropped(List<Path> files) {
        assetsPanel.filesDropped(files);
    }

    @Override
    public void dispose() {
        scenePanel.dispose();
        sceneRenderer.dispose();
        assetLibrary.dispose();
    }
}
