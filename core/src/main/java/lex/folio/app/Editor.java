package lex.folio.app;

import com.badlogic.gdx.utils.Disposable;
import lex.folio.assets.AssetLibrary;
import lex.folio.command.CommandStack;
import lex.folio.model.Project;
import lex.folio.scene.Selection;
import lex.folio.scene.camera.SceneCamera;
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

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** The editor itself: creates the parts, wires them together and draws them every frame. */
final class Editor implements Disposable {
    private final AssetLibrary assetLibrary;
    private final SceneRenderer sceneRenderer;
    private final ScenePanel scenePanel;
    private final InspectorPanel inspectorPanel;
    private final AssetsPanel assetsPanel;
    private final EditorShortcuts shortcuts;

    Editor() {
        Project project = TestProject.load();
        assetLibrary = new AssetLibrary(project);
        assetLibrary.loadAll();

        CommandStack commandStack = new CommandStack();
        Selection selection = new Selection();
        ToolState toolState = new ToolState();

        SpriteGeometry spriteGeometry = new SpriteGeometry(assetLibrary, project.getPixelsPerMeter());
        SpritePlacer spritePlacer = new SpritePlacer(commandStack);
        ToolController tools = createTools(commandStack, selection, toolState, spriteGeometry, spritePlacer);

        SceneCamera camera = new SceneCamera(project.getPixelsPerMeter());
        SceneViewport viewport = new SceneViewport(camera);
        sceneRenderer = new SceneRenderer(camera, assetLibrary, spriteGeometry);

        scenePanel = new ScenePanel(TestProject.createRoom(), sceneRenderer, viewport,
            new SceneOverlay(viewport, spriteGeometry, selection, toolState),
            new SceneInput(viewport, tools, spritePlacer));
        inspectorPanel = new InspectorPanel(selection, new SpriteInspector(commandStack));
        assetsPanel = AssetsPanel.create(project, assetLibrary, commandStack, toolState);
        shortcuts = new EditorShortcuts(commandStack, tools, toolState);
    }

    /** Add a tool here, and to the {@link Tool} enum, to make it available. */
    private static ToolController createTools(CommandStack commandStack, Selection selection, ToolState toolState,
                                              SpriteGeometry spriteGeometry, SpritePlacer spritePlacer) {
        Map<Tool, SceneTool> tools = new EnumMap<>(Tool.class);
        tools.put(Tool.SELECT, new SelectTool(new SpritePicker(spriteGeometry), selection, new SpriteDrag(commandStack)));
        tools.put(Tool.PAINT, new PaintTool(toolState, spritePlacer));
        return new ToolController(toolState, tools);
    }

    void draw() {
        scenePanel.draw();
        inspectorPanel.draw();
        assetsPanel.draw();
        shortcuts.handle();
    }

    void filesDropped(List<Path> files) {
        assetsPanel.filesDropped(files);
    }

    @Override
    public void dispose() {
        sceneRenderer.dispose();
        assetLibrary.dispose();
    }
}
