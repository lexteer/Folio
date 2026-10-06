package lex.folio.assets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Knows which files count as image assets and what id they get. */
final class ImageFiles {
    private static final String PNG_EXTENSION = ".png";

    private ImageFiles() {
    }

    static boolean isPng(Path file) {
        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return Files.isRegularFile(file) && fileName.endsWith(PNG_EXTENSION);
    }

    static String toAssetId(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.substring(0, fileName.length() - PNG_EXTENSION.length());
    }
}
