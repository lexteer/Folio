package lex.folio.scene.tool;

import lex.folio.model.ImageAsset;

import java.util.Objects;

public class ToolState {
    private Tool tool = Tool.SELECT;
    private ImageAsset armedAsset;

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = Objects.requireNonNull(tool, "tool");
        if (tool == Tool.SELECT) {
            armedAsset = null;
        }
    }

    public ImageAsset getArmedAsset() {
        return armedAsset;
    }

    public boolean isArmed(ImageAsset asset) {
        return asset == armedAsset;
    }

    public void arm(ImageAsset asset) {
        if (tool != Tool.PAINT) throw new IllegalStateException("Assets can only be armed in paint mode");
        armedAsset = Objects.requireNonNull(asset, "asset");
    }
}
