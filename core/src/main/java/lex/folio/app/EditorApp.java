package lex.folio.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import imgui.ImGui;
import lex.folio.assets.AssetFolderScanner;
import lex.folio.assets.AssetLibrary;
import lex.folio.command.CommandStack;
import lex.folio.model.Project;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;
import lex.folio.scene.*;
import lex.folio.ui.InspectorPanel;
import lex.folio.ui.ScenePanel;
import lex.folio.ui.SceneOverlay;
import lex.folio.ui.SpriteInspector;

import java.nio.file.Path;

public class EditorApp extends ApplicationAdapter {
    private ImGuiBackend imGui;
    private SceneCamera sceneCamera;
    private SceneRenderer sceneRenderer;
    private ScenePanel scenePanel;
    private InspectorPanel inspectorPanel;
    private AssetLibrary assetLibrary;

    private static final Path TEST_PROJECT_FOLDER = Path.of(System.getProperty("user.home"), "FolioTestProject");
    private Project project;
    private CommandStack commandStack = new CommandStack();
    private Selection selection = new Selection();

    private static final String DOCKSPACE_NAME = "MainDockSpace";
    private boolean needsDefaultLayout;

    @Override
    public void create() {
        imGui = new ImGuiBackend();
        needsDefaultLayout = !imGui.hadSavedLayout();

        project = new Project(TEST_PROJECT_FOLDER, 128f);
        AssetFolderScanner.addAssetsTo(project);
        assetLibrary = new AssetLibrary(project);
        assetLibrary.loadAll();

        SpriteGeometry spriteGeometry = new SpriteGeometry(assetLibrary, project.getPixelsPerMeter());
        sceneCamera = new SceneCamera(project.getPixelsPerMeter());
        sceneRenderer = new SceneRenderer(sceneCamera, assetLibrary, spriteGeometry);
        scenePanel = new ScenePanel(sceneRenderer, sceneCamera,
            new SpritePicker(spriteGeometry),
            new SceneOverlay(sceneCamera, spriteGeometry, assetLibrary),
            selection, createTestRoom());
        inspectorPanel = new InspectorPanel(selection, new SpriteInspector(commandStack));
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
}
