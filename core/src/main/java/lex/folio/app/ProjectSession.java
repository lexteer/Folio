package lex.folio.app;

import com.badlogic.gdx.utils.Disposable;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import lex.folio.assets.AssetLibrary;
import lex.folio.command.CommandStack;
import lex.folio.model.Project;
import lex.folio.model.Room;
import lex.folio.model.SpriteLayer;
import lex.folio.project.ProjectStorage;
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
    private final Project project;
    private final RoomStorage roomStorage;
    private final AssetLibrary assetLibrary;
    private final SceneRenderer sceneRenderer;
    private final ScenePanel scenePanel;
    private final InspectorPanel inspectorPanel;
    private final AssetsPanel assetsPanel;
    private final EditorShortcuts shortcuts;
    private final Selection selection;
    private final Consumer<String> errorSink;
    private List<String> lastRememberedRooms;

    ProjectSession(Project project, Consumer<String> errorSink) throws IOException {
        roomStorage = new RoomStorage(project);
        List<Room> allSavedRooms = roomStorage.loadAll();
        List<String> rememberedRooms = ProjectStorage.readOpenRooms(project.getRootFolder());
        List<Room> savedRooms = rememberedRooms == null ? allSavedRooms
            : allSavedRooms.stream().filter(room -> isRemembered(rememberedRooms, room)).toList();
        this.project = project;
        this.lastRememberedRooms = savedRooms.stream().map(Room::getName).toList();

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
        if (allSavedRooms.isEmpty()) scenePanel.openNewRoom(createRoom("main"));
        commandStack.setChangeListener(scenePanel::roomsChanged);
        this.selection = selection;
        this.errorSink = errorSink;
        inspectorPanel = new InspectorPanel(selection, new SpriteInspector(commandStack));
        assetsPanel = AssetsPanel.create(project, assetLibrary, commandStack, toolState, errorSink,
            this::assetRenamed);
        shortcuts = new EditorShortcuts(commandStack, tools, toolState, scenePanel::saveActiveRoom);
    }

    private static boolean isRemembered(List<String> names, Room room) {
        return names.stream().anyMatch(name -> name.equalsIgnoreCase(room.getName()));
    }

    /** The next session opens the rooms that are open now, so this keeps the project file up to date. */
    private void rememberOpenRooms() {
        List<String> names = scenePanel.getOpenSavedRoomNames();
        if (names.equals(lastRememberedRooms)) return;

        lastRememberedRooms = names;
        try {
            ProjectStorage.writeOpenRooms(project.getRootFolder(), names);
        } catch (IOException e) {
            errorSink.accept("Could not remember which rooms are open: " + e.getMessage());
        }
    }

    /** Rooms refer to assets by id, so renaming an asset updates the open rooms and the saved ones. */
    private void assetRenamed(String oldId, String newId) {
        scenePanel.assetRenamed(oldId, newId);
        try {
            roomStorage.replaceAssetId(oldId, newId);
        } catch (IOException e) {
            errorSink.accept("Could not update the saved rooms to the new asset name: " + e.getMessage());
        }
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
        rememberOpenRooms();
    }

    /** The inspector edits the selection, so it has to keep it. The scene handles its own clicks. */
    private void deselectWhenClickedOutsideSceneAndInspector() {
        boolean clicked = ImGui.isMouseClicked(ImGuiMouseButton.Left) || ImGui.isMouseClicked(ImGuiMouseButton.Right);
        if (clicked && !scenePanel.isHovered() && !inspectorPanel.isHovered()) {
            selection.clear();
        }
    }

    Path getRoomsFolder() {
        return roomStorage.getFolder();
    }

    /** Opens a room file chosen by the user, which has to be one of this project's rooms. */
    void openRoomFile(Path file) {
        try {
            if (!roomStorage.contains(file)) {
                errorSink.accept(notThisProjectsRoomMessage(file));
                return;
            }
            scenePanel.openSavedRoom(roomStorage.load(file));
        } catch (IOException e) {
            errorSink.accept(e.getMessage());
        }
    }

    private static String notThisProjectsRoomMessage(Path file) {
        Path owner = RoomStorage.findProjectOf(file);
        if (owner == null) return "That file is not a room of this project.";

        return "That room belongs to another project (" + owner.getFileName()
            + "). Open that project first if you want to open that room.";
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
