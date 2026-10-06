package lex.folio.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import imgui.ImGui;
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
    private static final String WELCOME_POPUP = "Welcome to Folio";

    private final NativeFileDialog fileDialog = new NativeFileDialog();
    private final NewProjectDialog newProjectDialog = new NewProjectDialog(fileDialog, this::createProject);
    private final LastProject lastProject = new LastProject();
    private ProjectSession session;
    private String error;

    Editor() {
        reopenLastProject();
    }

    /** Picks up where the last run left off. If the project is gone, the welcome popup shows as usual. */
    private void reopenLastProject() {
        Path folder = lastProject.find();
        if (folder == null) return;

        try {
            show(ProjectStorage.open(folder));
        } catch (IOException e) {
            lastProject.clear();
        }
    }

    void draw() {
        drawMainMenu();
        if (session != null) {
            session.draw();
        }
        newProjectDialog.draw();
        drawErrorPopup();
        if (session == null && error == null && !newProjectDialog.isOpen()) {
            drawWelcomePopup();
        }
    }

    private void drawMainMenu() {
        if (!ImGui.beginMainMenuBar()) return;

        if (ImGui.beginMenu("File")) {
            boolean enabled = !fileDialog.isOpen();
            if (ImGui.menuItem("New Project...", "", false, enabled)) {
                leaveProject(newProjectDialog::open);
            }
            if (ImGui.menuItem("Open Project...", "", false, enabled)) {
                leaveProject(this::chooseProjectToOpen);
            }
            ImGui.separator();
            if (ImGui.menuItem("Save", "Ctrl+S", false, session != null && session.canSaveActiveRoom())) {
                session.saveActiveRoom();
            }
            ImGui.endMenu();
        }
        ImGui.endMainMenuBar();
    }

    /** Runs {@code action}, which replaces the open project, after asking about unsaved rooms. */
    private void leaveProject(Runnable action) {
        if (session == null) {
            action.run();
        } else {
            session.runWhenNothingIsUnsaved(action);
        }
    }

    /** Shown instead of a blank editor while no project is open. */
    private void drawWelcomePopup() {
        if (!ImGui.isPopupOpen(WELCOME_POPUP)) {
            ImGui.openPopup(WELCOME_POPUP);
        }
        if (!ImGui.beginPopupModal(WELCOME_POPUP, ImGuiWindowFlags.AlwaysAutoResize)) return;

        ImGui.text("Create a new project or open an existing one.");
        ImGui.spacing();

        ImGui.beginDisabled(fileDialog.isOpen());
        if (ImGui.button("New Project...")) {
            ImGui.closeCurrentPopup();
            newProjectDialog.open();
        }
        ImGui.sameLine();
        if (ImGui.button("Open Project...")) {
            chooseProjectToOpen();
        }
        ImGui.endDisabled();
        ImGui.endPopup();
    }

    private void chooseProjectToOpen() {
        fileDialog.chooseFolder("Open a Folio project folder", this::openProject);
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

    /** Returns whether the window may close now. If rooms are unsaved it asks about them first and closes later. */
    boolean requestClose() {
        if (session == null || !session.hasUnsavedRooms()) return true;

        session.runWhenNothingIsUnsaved(Gdx.app::exit);
        return false;
    }

    private void createProject(Path parentFolder, String name, float pixelsPerMeter) throws IOException {
        show(ProjectStorage.create(parentFolder, name, pixelsPerMeter));
    }

    private void openProject(Path folder) {
        try {
            show(ProjectStorage.open(folder));
        } catch (IOException e) {
            error = e.getMessage();
        }
    }

    private void show(Project project) throws IOException {
        ProjectSession opened = new ProjectSession(project, message -> error = message);
        if (session != null) session.dispose();
        session = opened;
        lastProject.save(project.getRootFolder());
    }

    void filesDropped(List<Path> files) {
        if (session != null) session.filesDropped(files);
    }

    @Override
    public void dispose() {
        if (session != null) session.dispose();
    }
}
