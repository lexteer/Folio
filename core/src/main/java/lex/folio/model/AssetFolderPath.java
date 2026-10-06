package lex.folio.model;

import java.util.ArrayList;
import java.util.List;

/** Helpers for asset folder paths: '/'-separated, relative to the assets folder, "" is the root. */
public final class AssetFolderPath {
    public static final String ROOT = "";
    private static final char SEPARATOR = '/';

    private AssetFolderPath() {
    }

    public static String join(String folder, String name) {
        return folder.equals(ROOT) ? name : folder + SEPARATOR + name;
    }

    /** The folder that contains the given folder or file path. The root contains itself. */
    public static String getParent(String path) {
        int lastSeparator = path.lastIndexOf(SEPARATOR);
        return lastSeparator < 0 ? ROOT : path.substring(0, lastSeparator);
    }

    /** The last segment of a folder or file path. */
    public static String getName(String folder) {
        return folder.substring(folder.lastIndexOf(SEPARATOR) + 1);
    }

    /** The folder and each of its ancestors, outermost first. The root is not included. */
    public static List<String> getPathFromRoot(String folder) {
        List<String> result = new ArrayList<>();
        if (folder.equals(ROOT)) return result;

        int end = folder.indexOf(SEPARATOR);
        while (end >= 0) {
            result.add(folder.substring(0, end));
            end = folder.indexOf(SEPARATOR, end + 1);
        }
        result.add(folder);
        return result;
    }
}
