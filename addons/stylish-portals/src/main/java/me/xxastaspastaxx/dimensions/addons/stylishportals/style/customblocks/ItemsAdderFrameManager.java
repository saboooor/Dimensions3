package me.xxastaspastaxx.dimensions.addons.stylishportals.style.customblocks;

import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.block.Block;

public class ItemsAdderFrameManager extends FrameManager {

  private CustomBlock block;
  private String namespaceId;

  public ItemsAdderFrameManager(String string) {
    this.namespaceId = string;
    this.block = CustomBlock.getInstance(namespaceId);
  }

  public CustomBlock getBlock() {
    return block;
  }

  @Override
  public boolean isAccepted(Block block) {

    if (this.block == null) this.block = CustomBlock.getInstance(namespaceId);

    CustomBlock cBlock = CustomBlock.byAlreadyPlaced(block);
    return cBlock != null && cBlock.getNamespacedID().equals(namespaceId);
  }

  @Override
  public void placeBlock(Block block) {

    if (this.block == null) this.block = CustomBlock.getInstance(namespaceId);

    this.block.place(block.getLocation());
  }
}
