package me.xxastaspastaxx.dimensions.commands;

import java.util.Arrays;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AddonCommand extends DimensionsCommand implements Listener {

  private Inventory mainGUI;
  private ItemStack installedAddonsItemStack;
  private ItemStack installAddonsItemStack;

  private Inventory installedAddonsGUI;

  private Inventory manageAddonGUI;
  private ItemStack updateAddonItemStack;
  private ItemStack unloadAddonItemStack;
  private ItemStack addonInfoItemStack;

  public AddonCommand(
      String command,
      String args,
      String[] aliases,
      String description,
      String permission,
      boolean adminCommand) {
    super(command, args, aliases, description, permission, adminCommand);

    mainGUI =
        Bukkit.createInventory(
            null, 9, Component.text("Dimensions addons manager", NamedTextColor.GREEN));

    installedAddonsItemStack = new ItemStack(Material.COMMAND_BLOCK, 1);
    ItemMeta installedAddonsItemStackMeta = installedAddonsItemStack.getItemMeta();
    installedAddonsItemStackMeta.displayName(
        Component.text("Installed addons", NamedTextColor.GREEN));
    installedAddonsItemStackMeta.lore(
        Arrays.asList(
            new Component[] {
              Component.text("There are currently", NamedTextColor.GRAY),
              Component.text(Dimensions.getAddonManager().getAddons().size(), NamedTextColor.GREEN)
                  .append(Component.text(" addons installed", NamedTextColor.GRAY))
            }));
    installedAddonsItemStack.setItemMeta(installedAddonsItemStackMeta);
    mainGUI.addItem(installedAddonsItemStack);

    installAddonsItemStack = new ItemStack(Material.COMMAND_BLOCK_MINECART, 1);
    ItemMeta installAddonsItemStackkMeta = installAddonsItemStack.getItemMeta();
    installAddonsItemStackkMeta.displayName(Component.text("Browse addons", NamedTextColor.GREEN));
    installAddonsItemStackkMeta.lore(
        Arrays.asList(
            new Component[] {Component.text("Currently unavailable", NamedTextColor.GRAY)}));
    installAddonsItemStack.setItemMeta(installAddonsItemStackkMeta);
    mainGUI.addItem(installAddonsItemStack);

    installedAddonsGUI =
        Bukkit.createInventory(
            null,
            (int) Math.ceil(Dimensions.getAddonManager().getAddons().size() / 9f) * 9,
            Component.text("Dimensions addons manager", NamedTextColor.GREEN));

    for (DimensionsAddon addon : Dimensions.getAddonManager().getAddons()) {
      ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS, 1);
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.displayName(Component.text(addon.getName(), NamedTextColor.GREEN));
      itemMeta.lore(
          Arrays.asList(
              new Component[] {
                Component.text(addon.getDescription(), NamedTextColor.GRAY),
                Component.text("v" + addon.getVersion(), NamedTextColor.GRAY),
                Component.text("Click for more options", NamedTextColor.GRAY)
              }));
      item.setItemMeta(itemMeta);

      installedAddonsGUI.addItem(item);
    }

    manageAddonGUI =
        Bukkit.createInventory(
            null, 9, Component.text("Dimensions addons manager", NamedTextColor.RED));

    addonInfoItemStack = new ItemStack(Material.COMMAND_BLOCK, 1);
    ItemMeta addonInfoItemStackMeta = addonInfoItemStack.getItemMeta();
    addonInfoItemStackMeta.displayName(
        Component.text("Something went wrong", NamedTextColor.DARK_RED));
    addonInfoItemStackMeta.lore(
        Arrays.asList(
            new Component[] {Component.text("Something went wrong", NamedTextColor.GRAY)}));
    addonInfoItemStack.setItemMeta(addonInfoItemStackMeta);
    manageAddonGUI.addItem(addonInfoItemStack);

    updateAddonItemStack = new ItemStack(Material.GREEN_BANNER, 1);
    ItemMeta updateAddonItemStackMeta = updateAddonItemStack.getItemMeta();
    updateAddonItemStackMeta.displayName(Component.text("Update addon", NamedTextColor.GREEN));
    updateAddonItemStackMeta.lore(
        Arrays.asList(
            new Component[] {Component.text("Click to update the addon", NamedTextColor.GRAY)}));
    updateAddonItemStack.setItemMeta(updateAddonItemStackMeta);
    manageAddonGUI.addItem(updateAddonItemStack);

    unloadAddonItemStack = new ItemStack(Material.RED_BANNER, 1);
    ItemMeta unloadAddonItemStackMeta = unloadAddonItemStack.getItemMeta();
    unloadAddonItemStackMeta.displayName(Component.text("Unload addon", NamedTextColor.RED));
    unloadAddonItemStackMeta.lore(
        Arrays.asList(
            new Component[] {Component.text("Click to unload the addon", NamedTextColor.GRAY)}));
    unloadAddonItemStack.setItemMeta(unloadAddonItemStackMeta);
    manageAddonGUI.addItem(unloadAddonItemStack);

    Bukkit.getServer().getPluginManager().registerEvents(this, Dimensions.getInstance());
  }

  @Override
  public void execute(CommandSender sender, String[] args) {

    if (sender instanceof Player) {
      ((Player) sender).openInventory(mainGUI);
    } else {
      sender.sendMessage(
          Component.text("You must be a player to use this command.", NamedTextColor.RED));
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onItemClick(InventoryClickEvent e) {
    if (e.getInventory() == null
        || e.getCurrentItem() == null
        || e.getClickedInventory() == null
        || e.getWhoClicked() == null
        || !(e.getWhoClicked() instanceof Player)) return;

    try {
      if (e.getCurrentItem().isSimilar(installedAddonsItemStack)) {
        e.getWhoClicked().openInventory(installedAddonsGUI);
        e.setCancelled(true);
      } else if (e.getCurrentItem().isSimilar(installAddonsItemStack)) {
        e.getWhoClicked()
            .sendMessage(
                Component.text(
                    "This feature is not ready yet. It will be added in the future.",
                    NamedTextColor.GRAY));
        e.setCancelled(true);
      } else if (e.getClickedInventory().equals(installedAddonsGUI)) {
        DimensionsAddon addon =
            Dimensions.getAddonManager()
                .getAddonByName(
                    PlainTextComponentSerializer.plainText()
                        .serialize(e.getCurrentItem().getItemMeta().displayName()));
        if (addon == null) {
          e.getWhoClicked()
              .sendMessage(
                  Component.text(
                      "There was a problem while trying to access the addon", NamedTextColor.RED));
        } else {
          Inventory guiClone =
              Bukkit.createInventory(e.getWhoClicked(), 9, Component.text(addon.getName()));
          guiClone.setContents(manageAddonGUI.getContents());

          ItemMeta addonInfoItemStackMeta = addonInfoItemStack.getItemMeta();
          addonInfoItemStackMeta.displayName(Component.text(addon.getName(), NamedTextColor.RED));
          addonInfoItemStackMeta.lore(
              Arrays.asList(
                  new Component[] {
                    Component.text(addon.getDescription(), NamedTextColor.GRAY),
                    Component.text("v" + addon.getVersion(), NamedTextColor.GRAY)
                  }));
          guiClone.getItem(0).setItemMeta(addonInfoItemStackMeta);
          e.getWhoClicked().openInventory(guiClone);
        }
        e.setCancelled(true);
      } else if (e.getView().title() != null) {
        String titleStr = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        DimensionsAddon addon = Dimensions.getAddonManager().getAddonByName(titleStr);
        if (addon != null) {
          if (e.getCurrentItem().isSimilar(updateAddonItemStack)) {
            e.getWhoClicked()
                .sendMessage(
                    Component.text(
                        addon.getName()
                            + " v"
                            + addon.getVersion()
                            + " will be updated after a restart",
                        NamedTextColor.GREEN));
          } else if (e.getCurrentItem().isSimilar(unloadAddonItemStack)) {
            Dimensions.getAddonManager().unload(addon);
            e.getWhoClicked()
                .sendMessage(
                    Component.text(
                        addon.getName() + " v" + addon.getVersion() + " has been unloaded",
                        NamedTextColor.GREEN));
            e.getWhoClicked().openInventory(installedAddonsGUI);
          } else if (e.getSlot() == 0) e.getWhoClicked().openInventory(installedAddonsGUI);

          e.setCancelled(true);
        }
      }
    } catch (Exception e1) {
      e1.printStackTrace();
      e.setCancelled(true);
    }
  }
}
