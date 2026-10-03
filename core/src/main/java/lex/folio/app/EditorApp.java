package lex.folio.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import imgui.ImGui;
import lex.folio.assets.AssetFolderScanner;
import lex.folio.assets.AssetLibrary;
import lex.folio.model.Project;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;
import lex.folio.scene.*;
import lex.folio.ui.ScenePanel;
import lex.folio.ui.SceneOverlay;

import java.nio.file.Path;

public class EditorApp extends ApplicationAdapter {
    private ImGuiBackend imGui;
    private SceneCamera sceneCamera;
    private SceneRenderer sceneRenderer;
    private ScenePanel scenePanel;
    private AssetLibrary assetLibrary;

    private static final Path TEST_PROJECT_FOLDER = Path.of(System.getProperty("user.home"), "FolioTestProject");
    private Project project;

    @Override
    public void create() {
        imGui = new ImGuiBackend();
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
            new Selection(), createTestRoom());
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.08f, 0.08f, 0.09f, 1f);
        imGui.beginFrame();
        ImGui.dockSpaceOverViewport(0, ImGui.getMainViewport());
        scenePanel.draw();
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
