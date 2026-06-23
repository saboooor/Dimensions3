package me.xxastaspastaxx.dimensions.gui.player;

import java.util.ArrayList;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPlayerGUIAction;
import me.xxastaspastaxx.dimensions.builder.CreatePortalInstance;
import me.xxastaspastaxx.dimensions.gui.CreatePortalGUI;
import me.xxastaspastaxx.dimensions.gui.DimensionsGUIType;
import me.xxastaspastaxx.dimensions.gui.DimensionsGUIUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DimensionsPlayerPortalGUI extends CreatePortalGUI {

  private final float MAX_ITEMS_PER_PAGE = 20f;

  int page = 0;
  int maxPage = 0;

  public DimensionsPlayerPortalGUI(CreatePortalInstance instance) {
    super(instance, DimensionsGUIType.PLAYER_PORTAL);
  }

  @Override
  public Inventory createInventory() {
    Inventory inv =
        Bukkit.createInventory(
            p, 54, Component.text(instance.selectedPortal.getDisplayName(), NamedTextColor.GOLD));

    for (int i = 0; i < 6; i++) {
      inv.setItem(4 + (9 * i), DimensionsGUIUtils.BLACK_GLASS);
    }
    inv.setItem(50, DimensionsGUIUtils.BLACK_GLASS);
    inv.setItem(53, DimensionsGUIUtils.BLACK_GLASS);

    // Navigation
    inv.setItem(
        51,
        DimensionsGUIUtils.createItem(
            Material.BIRCH_BOAT, Component.text("Previous page", NamedTextColor.GRAY)));
    inv.setItem(
        52,
        DimensionsGUIUtils.createItem(
            Material.BIRCH_BOAT, Component.text("Next page", NamedTextColor.GRAY)));

    ItemStack closeItemStack =
        DimensionsGUIUtils.createItem(
            Material.GREEN_STAINED_GLASS_PANE, Component.text("Close", NamedTextColor.GREEN));
    inv.setItem(45, closeItemStack);
    inv.setItem(46, closeItemStack);
    inv.setItem(47, closeItemStack);
    inv.setItem(48, closeItemStack);

    return inv;
  }

  @Override
  public void open() {
    ArrayList<DimensionsAddonPlayerGUIAction> actionList =
        instance.selectedPortal.getAddonPlayerGUIActions();

    maxPage = (int) Math.floor(actionList.size() / MAX_ITEMS_PER_PAGE);
    ItemStack[] contents = inventory.getContents();
    inventory =
        Bukkit.createInventory(
            p,
            inventory.getSize(),
            Component.text(instance.selectedPortal.getDisplayName(), NamedTextColor.GOLD));
    inventory.setContents(contents);

    for (int i = 0; i < MAX_ITEMS_PER_PAGE; i++) {
      int itemIndex = (int) (MAX_ITEMS_PER_PAGE * page + i);
      int posIndex = (int) (5 + ((i / 4) * 4) + Math.floor(i / 4) * 5);
      if (actionList.size() <= itemIndex) {
        inventory.clear(posIndex);
      } else {
        inventory.setItem(posIndex, actionList.get(itemIndex).getItemStack());
      }
    }

    ItemStack portalBlock =
        DimensionsGUIUtils.createItem(
            instance.selectedPortal.getOutsideMaterial(),
            Component.text(
                instance.selectedPortal.getOutsideMaterial().name(), NamedTextColor.WHITE),
            java.util.List.of(Component.text("Build using this block.", NamedTextColor.GRAY)));
    for (int i = 0; i < 4; i++) {
      inventory.setItem(i, portalBlock);
      inventory.setItem(36 + i, portalBlock);
    }
    for (int i = 9; i <= 27; i += 9) {
      inventory.setItem(i, portalBlock);
      inventory.setItem(i + 3, portalBlock);
    }

    if (instance.selectedPortal.getLighterMaterial() == null) {
      inventory.setItem(
          29,
          DimensionsGUIUtils.createItem(
              Material.BARRIER,
              Component.text("{{customItem}}", NamedTextColor.WHITE),
              java.util.List.of(
                  Component.text(
                      "This should have been replaced by the responsible addon",
                      NamedTextColor.GRAY),
                  Component.text(
                      "Please make sure everything is setup correctly", NamedTextColor.GRAY))));
    } else {
      inventory.setItem(
          29,
          DimensionsGUIUtils.createItem(
              instance.selectedPortal.getLighterMaterial(),
              Component.text(
                  instance.selectedPortal.getLighterMaterial().name(), NamedTextColor.WHITE),
              java.util.List.of(Component.text("Ignite using this item.", NamedTextColor.GRAY))));
    }

    ItemStack prevItem = inventory.getItem(51);
    if (prevItem != null) {
      inventory.setItem(
          51, prevItem.withType(page == 0 ? Material.MINECART : Material.CHEST_MINECART));
    }
    ItemStack nextItem = inventory.getItem(52);
    if (nextItem != null) {
      inventory.setItem(
          52, nextItem.withType(page == maxPage ? Material.MINECART : Material.CHEST_MINECART));
    }

    super.open();
  }

  @Override
  public void handleClick(int index, boolean rightClick, boolean shiftClick) {

    ArrayList<DimensionsAddonPlayerGUIAction> actionList =
        instance.selectedPortal.getAddonPlayerGUIActions();

    if (index >= 5 && (index - 5) % 9 < 4 && index <= 44) {
      int itemIndex = (index - 18) + 27 * page;
      if (actionList.size() <= itemIndex) return;

      if (actionList.get(itemIndex).execute(instance.getPlayer(), instance.selectedPortal)) {
        p.closeInventory();
        Dimensions.getCreatePortalManager().clear(p);
      }

      return;
    }

    switch (index) {
      // Navigation
      case 51:
        page = Math.max(0, page - 1);
        open();
        break;
      case 52:
        page = (int) Math.min(maxPage, page + 1);
        open();
        break;

      case 45:
      case 46:
      case 47:
      case 48:
        instance.guiMap.get(DimensionsGUIType.PLAYER_MAIN).open();
        break;
    }
  }

  @Override
  public boolean handleChat(String string) {
    return true;
  }
}
