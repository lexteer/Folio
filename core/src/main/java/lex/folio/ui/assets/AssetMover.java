package lex.folio.ui.assets;

import lex.folio.assets.AssetFolders;
import lex.folio.command.Command;
import lex.folio.command.CommandGroup;
import lex.folio.command.CommandStack;
import lex.folio.command.SetValueCommand;
import lex.folio.model.ImageAsset;

import java.util.ArrayList;
import java.util.List;

/** Moves assets between folders as one undoable step. */
class AssetMover {
    private final AssetFolders folders;
    private final CommandStack commandStack;

    AssetMover(AssetFolders folders, CommandStack commandStack) {
        this.folders = folders;
        this.commandStack = commandStack;
    }

    /** Moves the assets among the items into the folder. Returns whether any of them moved. */
    boolean moveAssets(List<BrowserItem> items, String folder) {
        List<Command> moves = new ArrayList<>();
        for (BrowserItem item : items) {
            if (item instanceof BrowserItem.Asset(ImageAsset asset) && !asset.getFolder().equals(folder)) {
                moves.add(createMove(asset, folder));
            }
        }
        if (moves.isEmpty()) return false;

        commandStack.execute(new CommandGroup(moves));
        return true;
    }

    private Command createMove(ImageAsset asset, String folder) {
        return new SetValueCommand<>(target -> folders.moveAsset(asset, target), asset.getFolder(), folder);
    }
}
