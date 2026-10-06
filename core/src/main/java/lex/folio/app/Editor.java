package lex.folio.app;

import com.badlogic.gdx.utils.Disposable;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import lex.folio.model.Project;
import lex.folio.project.ProjectStorage;
import lex.folio.ui.common.NativeFileDialog;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** The editor itself: owns the main menu and the open project, if any, and draws them every frame. */
final class Editor implements Disposable {
    private static final String ERROR_POPUP = "Project error";
    private static final String HINT = "Use File > New Project or File > Open Project to get started.";

    private final NativeFileDialog fileDialog = new NativeFileDialog();
    private ProjectSession session;
    private String error;

    void draw() {
        drawMainMenu();
        if (session != null) {
            session.draw();
        } else {
            drawHint();
        }
        drawErrorPopup();
    }

    private void drawMainMenu() {
        if (!ImGui.beginMainMenuBar()) return;

        if (ImGui.beginMenu("File")) {
            boolean enabled = !fileDialog.isOpen();
            if (ImGui.menuItem("New Project...", "", false, enabled)) {
                fileDialog.chooseFolder("Choose a folder for the new project", this::createProject);
            }
            if (ImGui.menuItem("Open Project...", "", false, enabled)) {
                fileDialog.chooseFolder("Open a Folio project folder", this::openProject);
            }
            ImGui.endMenu();
        }
        ImGui.endMainMenuBar();
    }

    private void drawHint() {
        ImVec2 center = ImGui.getMainViewport().getCenter();
        ImVec2 size = new ImVec2();
        ImGui.calcTextSize(size, HINT);
        ImGui.getBackgroundDrawList().addText(center.x - size.x / 2, center.y - size.y / 2,
            ImGui.getColorU32(ImGuiCol.TextDisabled), HINT);
    }

    private void drawErrorPopup() {
        if (error != null) {
            ImGui.openPopup(ERROR_POPUP);
        }
        if (ImGui.beginPopupModal(ERROR_POPUP, ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text(error == null ? "" : error);
            if (ImGui.button("OK")) {
                error = null;
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    private void createProject(Path folder) {
        load(() -> ProjectStorage.create(folder));
    }

    private void openProject(Path folder) {
        load(() -> ProjectStorage.open(folder));
    }

    private void load(ProjectLoader loader) {
        Project project;
        try {
            project = loader.load();
        } catch (IOException e) {
            error = e.getMessage();
            return;
        }

        if (session != null) session.dispose();
        session = new ProjectSession(project);
    }

    void filesDropped(List<Path> files) {
        if (session != null) session.filesDropped(files);
    }

    @Override
    public void dispose() {
        if (session != null) session.dispose();
    }

    private interface ProjectLoader {
        Project load() throws IOException;
    }
}
