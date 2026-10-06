package lex.folio.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import imgui.ImGui;
import lex.folio.assets.AssetFolderScanner;
import lex.folio.assets.AssetImporter;
import lex.folio.assets.AssetLibrary;
import lex.folio.command.CommandStack;
import lex.folio.model.Project;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;
import lex.folio.scene.*;
import lex.folio.ui.*;
import lex.folio.ui.assets.AssetsPanel;

import java.nio.file.Path;
import java.util.Arrays;

public class EditorApp extends ApplicationAdapter {
    private ImGuiBackend imGui;
    private SceneCamera sceneCamera;
    private SceneRenderer sceneRenderer;
    private AssetLibrary assetLibrary;
    private AssetImporter assetImporter;

    private ScenePanel scenePanel;
    private InspectorPanel inspectorPanel;
    private AssetsPanel assetsPanel;

    private static final Path TEST_PROJECT_FOLDER = Path.of(System.getProperty("user.home"), "FolioTestProject");
    private Project project;
    private CommandStack commandStack = new CommandStack();
    SpriteDrag spriteDrag = new SpriteDrag(commandStack);
    private Selection selection = new Selection();
    private final NativeFileDialog fileDialog = new NativeFileDialog();

    private static final String DOCKSPACE_NAME = "MainDockSpace";
    private boolean needsDefaultLayout;
    private EditorShortcuts editorShortcuts;
    private final ToolState toolState = new ToolState();

    @Override
    public void create() {
        imGui = new ImGuiBackend();
        needsDefaultLayout = !imGui.hadSavedLayout();

        editorShortcuts = new EditorShortcuts(commandStack, spriteDrag, toolState);
        project = new Project(TEST_PROJECT_FOLDER, 100f);
        AssetFolderScanner.addAssetsTo(project);
        assetLibrary = new AssetLibrary(project);
        assetLibrary.loadAll();

        SpriteGeometry spriteGeometry = new SpriteGeometry(assetLibrary, project.getPixelsPerMeter());
        sceneCamera = new SceneCamera(project.getPixelsPerMeter());
        sceneRenderer = new SceneRenderer(sceneCamera, assetLibrary, spriteGeometry);
        assetImporter = new AssetImporter(project, assetLibrary);

        SpritePlacer spritePlacer = new SpritePlacer(commandStack);
        SceneTools sceneTools = new SceneTools(toolState,
            new SelectTool(new SpritePicker(spriteGeometry), selection, spriteDrag),
            new PaintTool(toolState, spritePlacer),
            spritePlacer);
        scenePanel = new ScenePanel(sceneRenderer, sceneCamera,
            new SceneOverlay(sceneCamera, spriteGeometry, assetLibrary, selection),
            createTestRoom(), sceneTools);
        inspectorPanel = new InspectorPanel(selection, new SpriteInspector(commandStack));
        assetsPanel = new AssetsPanel(project, assetLibrary, assetImporter, commandStack, toolState);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.08f, 0.08f, 0.09f, 1f);

        imGui.beginFrame();
        int dockspaceId = ImGui.getID(DOCKSPACE_NAME);
        if (needsDefaultLayout) {
            DefaultDockLayout.build(dockspaceId);
            needsDefaultLayout = false;
        }
        ImGui.dockSpaceOverViewport(dockspaceId, ImGui.getMainViewport());

        scenePanel.draw();
        inspectorPanel.draw();
        assetsPanel.draw();
        editorShortcuts.handle();
        imGui.endFrame();
    }

    @Override
    public void dispose() {
        sceneRenderer.dispose();
        assetLibrary.dispose();
        imGui.dispose();
    }

    private static Room createTestRoom() {
        Room room = new Room("test");
        SpriteLayer layer = new SpriteLayer(room.createId(), "Art");

        layer.add(new Sprite(room.createId(), "tile008", 2, 1));

        Sprite rotated = new Sprite(room.createId(), "tile16", 4, 1);
        layer.add(rotated);

        Sprite scaled = new Sprite(room.createId(), "tile021", 6, 1);
        layer.add(scaled);

        Sprite flippedX = new Sprite(room.createId(), "tile000", 8, 1);
        layer.add(flippedX);

        Sprite flippedY = new Sprite(room.createId(), "flipY", 10, 1);
        layer.add(flippedY);

        rotated.setRotationDegrees(30);
        scaled.setScale(2f, 0.5f);
        flippedX.setFlipX(true);
        flippedY.setFlipY(true);

        room.addLayer(layer);
        return room;
    }

    public void filesDropped(String[] files) {
        assetsPanel.filesDropped(Arrays.stream(files).map(Path::of).toList());
    }
}
