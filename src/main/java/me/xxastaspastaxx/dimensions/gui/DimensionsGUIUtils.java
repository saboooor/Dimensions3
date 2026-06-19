package me.xxastaspastaxx.dimensions.gui;

import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DimensionsGUIUtils {

  // Constants
  public static ItemStack BLACK_GLASS = createBlackGlass();

  private static ItemStack createBlackGlass() {
    if (BLACK_GLASS != null) return BLACK_GLASS;
    return createItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(" "));
  }

  private static Enchantment DECOR_ENCHANT = Enchantment.SMITE;

  // Create ItemStack
  public static ItemStack createItem(Material material, Component title) {
    return createItem(material, 1, title);
  }

  public static ItemStack createItem(Material material, int amount, Component title) {
    return createItem(material, amount, title, java.util.List.of());
  }

  public static ItemStack createItem(
      Material material, Component title, java.util.List<Component> lore) {
    return createItem(material, 1, title, lore);
  }

  public static ItemStack createItem(
      Material material, int amount, Component title, java.util.List<Component> lore) {
    ItemStack item = new ItemStack(material, amount);
    ItemMeta meta = item.getItemMeta();
    if (title != null) meta.displayName(title);
    if (lore != null && !lore.isEmpty()) {
      meta.lore(lore);
    }
    item.setItemMeta(meta);

    return item;
  }

  // Update ItemStack
  public static void updateItem(
      Inventory inventory,
      int index,
      Component title,
      java.util.List<Component> lore,
      int toggleEnchant) {
    ItemStack item = inventory.getItem(index);
    updateItem(item, title, lore, toggleEnchant);
  }

  public static void updateItem(
      ItemStack item, Component title, java.util.List<Component> lore, int toggleEnchant) {
    if (toggleEnchant == 1) {
      item.addUnsafeEnchantment(DECOR_ENCHANT, 1);
    } else if (toggleEnchant == -1) {
      item.removeEnchantment(DECOR_ENCHANT);
    }

    ItemMeta meta = item.getItemMeta();
    if (title != null) meta.displayName(title);
    if (lore != null && !lore.isEmpty()) {
      meta.lore(lore);
    }
    if (toggleEnchant == 1) {
      meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }
    item.setItemMeta(meta);
  }

  public static ItemStack createPortalItem(CustomPortal customPortal) {
    return createItem(
        customPortal.getOutsideMaterial(), Component.text(customPortal.getDisplayName()));
  }
}
