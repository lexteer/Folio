package lex.folio.ui.assets;

import lex.folio.model.ImageAsset;

sealed interface BrowserItem {
    record Folder(String path) implements BrowserItem {
    }

    record Asset(ImageAsset asset) implements BrowserItem {
    }
}
