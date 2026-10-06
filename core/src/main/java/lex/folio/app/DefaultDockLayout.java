package lex.folio.app;

import imgui.flag.ImGuiDir;
import imgui.internal.ImGui;
import imgui.internal.flag.ImGuiDockNodeFlags;
import imgui.type.ImInt;
import lex.folio.ui.assets.AssetsPanel;
import lex.folio.ui.InspectorPanel;
import lex.folio.ui.ScenePanel;

final class DefaultDockLayout {
    private static final float INSPECTOR_WIDTH_RATIO = 0.20f;
    private static final float ASSETS_HEIGHT_RATIO = 0.3f;

    private DefaultDockLayout() {
    }

    static void build(int dockspaceId) {
        ImGui.dockBuilderRemoveNode(dockspaceId);
        ImGui.dockBuilderAddNode(dockspaceId, ImGuiDockNodeFlags.DockSpace);
        ImGui.dockBuilderSetNodeSize(dockspaceId, ImGui.getMainViewport().getSizeX(), ImGui.getMainViewport().getSizeY());

        ImInt inspectorArea = new ImInt();
        ImInt sceneArea = new ImInt();
        ImGui.dockBuilderSplitNode(dockspaceId, ImGuiDir.Right, INSPECTOR_WIDTH_RATIO, inspectorArea, sceneArea);

        ImInt assetsArea = new ImInt();
        ImGui.dockBuilderSplitNode(sceneArea.get(), ImGuiDir.Down, ASSETS_HEIGHT_RATIO, assetsArea, sceneArea);

        ImGui.dockBuilderDockWindow(AssetsPanel.TITLE, assetsArea.get());
        ImGui.dockBuilderDockWindow(InspectorPanel.TITLE, inspectorArea.get());
        ImGui.dockBuilderDockWindow(ScenePanel.TITLE, sceneArea.get());
        ImGui.dockBuilderFinish(dockspaceId);
    }
}
