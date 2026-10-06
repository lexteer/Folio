package lex.folio.app;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImFloat;
import imgui.type.ImString;
import lex.folio.project.ProjectStorage;
import lex.folio.ui.common.NativeFileDialog;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/** The form for creating a project: name, pixels per meter, and the folder to create it in. */
final class NewProjectDialog {
    private static final String TITLE = "New Project";
    private static final String FORBIDDEN_NAME_CHARACTERS = "/\\:*?\"<>|";

    /** Creates the project and makes it the open one. */
    interface Creator {
        void create(Path parentFolder, String name, float pixelsPerMeter) throws IOException;
    }

    private final NativeFileDialog fileDialog;
    private final Creator creator;

    private final ImString name = new ImString(128);
    private final ImFloat pixelsPerMeter = new ImFloat(ProjectStorage.DEFAULT_PIXELS_PER_METER);
    private final ImString location = new ImString(512);

    private boolean open;
    private boolean popupRequested;
    private String error;

    NewProjectDialog(NativeFileDialog fileDialog, Creator creator) {
        this.fileDialog = fileDialog;
        this.creator = creator;
    }

    void open() {
        name.set("");
        pixelsPerMeter.set(ProjectStorage.DEFAULT_PIXELS_PER_METER);
        if (location.get().isEmpty()) {
            location.set(System.getProperty("user.home"));
        }
        error = null;
        open = true;
        popupRequested = true;
    }

    boolean isOpen() {
        return open;
    }

    void draw() {
        if (!open) return;

        if (popupRequested) {
            ImGui.openPopup(TITLE);
            popupRequested = false;
        }
        if (!ImGui.beginPopupModal(TITLE, ImGuiWindowFlags.AlwaysAutoResize)) return;

        ImGui.inputText("Name", name);
        ImGui.inputFloat("Pixels per meter", pixelsPerMeter, 1f, 10f, "%.2f");
        ImGui.inputText("Location", location);
        ImGui.sameLine();
        if (ImGui.button("Browse...") && !fileDialog.isOpen()) {
            fileDialog.chooseFolder("Choose where to create the project", folder -> location.set(folder.toString()));
        }
        ImGui.textDisabled("A new folder named after the project is created in this location.");

        if (error != null) {
            ImGui.pushStyleColor(ImGuiCol.Text, 1f, 0.4f, 0.4f, 1f);
            ImGui.textWrapped(error);
            ImGui.popStyleColor();
        }

        if (ImGui.button("Create")) {
            create();
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel")) {
            close();
        }
        ImGui.endPopup();
    }

    private void create() {
        String projectName = name.get().trim();
        error = validate(projectName);
        if (error != null) return;

        try {
            creator.create(Path.of(location.get().trim()), projectName, pixelsPerMeter.get());
            close();
        } catch (IOException | InvalidPathException e) {
            error = e.getMessage();
        }
    }

    private String validate(String projectName) {
        if (projectName.isEmpty()) return "Enter a project name.";
        if (projectName.equals(".") || projectName.equals("..")
            || projectName.chars().anyMatch(c -> FORBIDDEN_NAME_CHARACTERS.indexOf(c) >= 0)) {
            return "The name cannot contain any of: " + FORBIDDEN_NAME_CHARACTERS;
        }
        float ppm = pixelsPerMeter.get();
        if (ppm <= 0 || Float.isNaN(ppm) || Float.isInfinite(ppm)) return "Pixels per meter must be above 0.";
        if (location.get().isBlank()) return "Choose a location.";
        return null;
    }

    private void close() {
        open = false;
        ImGui.closeCurrentPopup();
    }
}
