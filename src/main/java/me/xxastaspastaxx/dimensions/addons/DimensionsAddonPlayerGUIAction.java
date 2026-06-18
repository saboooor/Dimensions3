package me.xxastaspastaxx.dimensions.addons;

import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class DimensionsAddonPlayerGUIAction {

  public abstract ItemStack getItemStack();

  public abstract boolean execute(Player player, CustomPortal selectedPortal);
}
