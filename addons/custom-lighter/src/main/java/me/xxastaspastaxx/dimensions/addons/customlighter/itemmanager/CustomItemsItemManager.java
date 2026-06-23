package me.xxastaspastaxx.dimensions.addons.customlighter.itemmanager;

import com.jojodmo.customitems.api.CustomItemsAPI;
import org.bukkit.inventory.ItemStack;

public class CustomItemsItemManager extends ItemManager {

  private String id;

  public CustomItemsItemManager(String string) {
    this.id = string;
  }

  @Override
  public boolean isAccepted(ItemStack lighter) {
    return CustomItemsAPI.isCustomItem(lighter, id);
  }
}
