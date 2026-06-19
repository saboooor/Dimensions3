package me.xxastaspastaxx.dimensions.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalLoader;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PortalsCommand extends DimensionsCommand implements Listener {

  private Inventory mainInventory;
  private HashMap<Player, Inventory> portalsInventory = new HashMap<Player, Inventory>();
  private HashMap<Player, Inventory> browseInventory = new HashMap<Player, Inventory>();

  private ArrayList<CachedPortal> cachedPortals = new ArrayList<CachedPortal>();
  private long lastUpdate = 0;

  private Gson gson;

  public PortalsCommand(
      String command,
      String args,
      String[] aliases,
      String description,
      String permission,
      boolean adminCommand) {
    super(command, args, aliases, description, permission, adminCommand);

    setupMenu();

    gson = new GsonBuilder().create();

    Bukkit.getPluginManager().registerEvents(this, Dimensions.getInstance());
  }

  @Override
  public void execute(CommandSender sender, String[] args) {

    if (sender instanceof Player) {
      ((Player) sender).openInventory(mainInventory);
    } else {
      net.kyori.adventure.text.TextComponent.Builder builder =
          Component.text()
              .append(DimensionsSettings.getPrefix())
              .append(Component.text("Portals list:"));
      for (CustomPortal portal : Dimensions.getCustomPortalManager().getCustomPortals()) {
        builder
            .append(Component.newline())
            .append(Component.text("["))
            .append(
                portal.isEnabled()
                    ? Component.text("Enabled", NamedTextColor.GREEN)
                    : Component.text("Disabled", NamedTextColor.RED))
            .append(Component.text("] ", NamedTextColor.GRAY))
            .append(Component.text(portal.getPortalId()));
      }

      sender.sendMessage(builder.build());
    }
  }

  private void setupMenu() {

    mainInventory =
        Bukkit.createInventory(null, 9, Component.text("Dimensions", NamedTextColor.RED));

    ItemStack decor = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta decorMeta = decor.getItemMeta();
    decorMeta.displayName(Component.text(" "));
    decor.setItemMeta(decorMeta);

    for (int i = 0; i < 9; i++) mainInventory.setItem(i, decor);

    ItemStack myPortals = new ItemStack(Material.PAPER);
    ItemMeta myPortalsMeta = myPortals.getItemMeta();
    myPortalsMeta.displayName(Component.text("My Portals", NamedTextColor.GOLD));
    myPortalsMeta.lore(
        Arrays.asList(Component.text("Click to view your portals", NamedTextColor.GRAY)));
    myPortals.setItemMeta(myPortalsMeta);

    mainInventory.setItem(3, myPortals);

    ItemStack browsePortals = new ItemStack(Material.CHEST);
    ItemMeta browsePortalsMeta = browsePortals.getItemMeta();
    browsePortalsMeta.displayName(Component.text("Browse Portals", NamedTextColor.BLUE));
    browsePortalsMeta.lore(
        Arrays.asList(
            Component.text("Click to browse portals online", NamedTextColor.GRAY),
            Component.text("Use Shift+Click to forcefully load portals", NamedTextColor.GRAY)));
    browsePortals.setItemMeta(browsePortalsMeta);

    mainInventory.setItem(5, browsePortals);
  }

  private void updatePortalsMenu(Player p, int page) {
    ArrayList<CustomPortal> portals = Dimensions.getCustomPortalManager().getCustomPortals();

    Inventory inv =
        Bukkit.createInventory(
            null,
            54,
            Component.text(
                "My portals | Page " + (page + 1) + "/" + ((int) Math.ceil(portals.size() / 45f))));

    for (int i = page * 45; i < (page + 1) * 45; i++) {
      if (i >= portals.size()) break;
      CustomPortal portal = portals.get(i);
      ItemStack portalItem = new ItemStack(portal.getOutsideMaterial());
      ItemMeta itemMeta = portalItem.getItemMeta();
      itemMeta.displayName(Component.text(portal.getPortalId(), NamedTextColor.GOLD));
      itemMeta.lore(
          Arrays.asList(
              Component.text(portal.getDisplayName(), NamedTextColor.YELLOW)
                  .decoration(TextDecoration.BOLD, true),
              Component.text("Click for more details", NamedTextColor.GRAY)));
      portalItem.setItemMeta(itemMeta);
      inv.addItem(portalItem);
    }

    ItemStack decor = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta decorMeta = decor.getItemMeta();
    decorMeta.displayName(Component.text(" "));
    decor.setItemMeta(decorMeta);

    for (int i = 46; i <= 52; i++) inv.setItem(i, decor);

    ItemStack goBack = new ItemStack(page == 0 ? Material.BARRIER : Material.ARROW);
    ItemMeta goBackMeta = goBack.getItemMeta();
    goBackMeta.displayName(Component.text("Previous page", NamedTextColor.GOLD));
    goBackMeta.lore(
        Arrays.asList(
            Component.text("Go to page " + ((int) Math.max(1, page)), NamedTextColor.GRAY)));
    goBack.setItemMeta(goBackMeta);
    inv.setItem(45, goBack);

    ItemStack goMprosta =
        new ItemStack((page + 1) * 45 >= portals.size() ? Material.BARRIER : Material.ARROW);
    ItemMeta goMprostaMeta = goBack.getItemMeta();
    goMprostaMeta.displayName(Component.text("Next page", NamedTextColor.GOLD));
    goMprostaMeta.lore(
        Arrays.asList(
            Component.text(
                "Go to page " + ((int) Math.min(Math.ceil(portals.size() / 45f), page + 2)),
                NamedTextColor.GRAY)));
    goMprosta.setItemMeta(goMprostaMeta);
    inv.setItem(53, goMprosta);

    portalsInventory.put(p, inv);
    p.openInventory(inv);
  }

  private void updateBrowseInventory(Player p, int page, boolean forceUpdate) {

    Inventory inv =
        Bukkit.createInventory(
            null,
            54,
            Component.text(
                "Browse portals | Page "
                    + (page + 1)
                    + "/"
                    + ((int) Math.ceil(cachedPortals.size() / 45f))));

    if (forceUpdate || System.currentTimeMillis() - lastUpdate >= 108000000) {
      p.closeInventory();
      lastUpdate = System.currentTimeMillis();
      cachedPortals.clear();
      p.sendMessage(Component.text("Fetching portals..."));

      HashMap<String, HashMap<String, Object>> portals = null;

      try {
        portals =
            gson.fromJson(
                readStringFromURL(
                    "https://astaspasta.alwaysdata.net/api/portalData.php?all="
                        + p.getUniqueId().toString().replace("-", "")),
                new TypeToken<HashMap<String, HashMap<String, Object>>>() {}.getType());
      } catch (JsonSyntaxException | IOException e) {
        p.sendMessage(Component.text("There was an issue while trying to fetch portals"));
        e.printStackTrace();
        return;
      }

      portals.forEach((id, map) -> cachedPortals.add(CachedPortal.create(id, map)));
    }

    for (int i = page * 45; i < (page + 1) * 45; i++) {
      if (i >= cachedPortals.size()) break;
      CachedPortal portal = cachedPortals.get(i);
      inv.addItem(portal.getItemStack());
    }

    ItemStack decor = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta decorMeta = decor.getItemMeta();
    decorMeta.displayName(Component.text(" "));
    decor.setItemMeta(decorMeta);

    for (int i = 46; i <= 52; i++) inv.setItem(i, decor);

    ItemStack goBack = new ItemStack(page == 0 ? Material.BARRIER : Material.ARROW);
    ItemMeta goBackMeta = goBack.getItemMeta();
    goBackMeta.displayName(Component.text("Previous page", NamedTextColor.GOLD));
    goBackMeta.lore(
        Arrays.asList(
            Component.text("Go to page " + ((int) Math.max(1, page)), NamedTextColor.GRAY)));
    goBack.setItemMeta(goBackMeta);
    inv.setItem(45, goBack);

    ItemStack goMprosta =
        new ItemStack((page + 1) * 45 >= cachedPortals.size() ? Material.BARRIER : Material.ARROW);
    ItemMeta goMprostaMeta = goBack.getItemMeta();
    goMprostaMeta.displayName(Component.text("Next page", NamedTextColor.GOLD));
    goMprostaMeta.lore(
        Arrays.asList(
            Component.text(
                "Go to page "
                    + ((int) Math.min(Math.ceil(cachedPortals.size() / 45f), page + 2)))));
    goMprosta.setItemMeta(goMprostaMeta);
    inv.setItem(53, goMprosta);

    p.openInventory(inv);
    browseInventory.put(p, inv);
  }

  public static String readStringFromURL(String requestURL) throws IOException {
    try (Scanner scanner =
        new Scanner(
            URI.create(requestURL).toURL().openStream(), StandardCharsets.UTF_8.toString())) {
      scanner.useDelimiter("\\A");
      return scanner.hasNext() ? scanner.next() : "";
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onClick(InventoryClickEvent e) {
    if (e.getCurrentItem() == null
        || e.getInventory() == null
        || !(e.getWhoClicked() instanceof Player)) return;
    ItemStack item = e.getCurrentItem();
    Player p = (Player) e.getWhoClicked();
    if (e.getInventory().equals(mainInventory)) {
      e.setCancelled(true);
      if (e.getClickedInventory() != e.getInventory()) return;
      if (item.getType() == Material.PAPER) updatePortalsMenu(p, 0);
      if (item.getType() == Material.CHEST)
        if (p.hasPermission("dimensions.forceupdatebrowser")) {
          updateBrowseInventory(p, 0, e.isShiftClick());
        } else {
          p.sendMessage(
              DimensionsSettings.getPrefix()
                  .append(Component.text("You do not have the ", NamedTextColor.RED))
                  .append(
                      Component.text("dimensions.forceupdatebrowser", NamedTextColor.RED)
                          .decorate(TextDecoration.UNDERLINED))
                  .append(
                      Component.text(" permission to do perform that action", NamedTextColor.RED)));
        }
      ;
    } else if (portalsInventory.containsKey(p)
        && e.getInventory().equals(portalsInventory.get(p))) {
      e.setCancelled(true);
      if (e.getClickedInventory() != e.getInventory()) return;
      String name =
          PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
      if (name.equalsIgnoreCase(" ") || name.isEmpty()) {
        return;
      } else if (name.equals("Previous page") || name.equals("Next page")) {
        String lore =
            PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().lore().get(0));
        updatePortalsMenu(p, Integer.parseInt(lore.substring(11)) - 1);
        return;
      } else {
        CustomPortal portal = Dimensions.getCustomPortalManager().getCustomPortal(name);
        if (portal != null) {
          p.sendMessage(
              DimensionsSettings.getPrefix()
                  .append(Component.text(portal.getDisplayName()))
                  .append(Component.text(":"))
                  .append(Component.text(" Is built from ", NamedTextColor.GRAY))
                  .append(
                      Component.text(portal.getOutsideMaterial().toString(), NamedTextColor.RED))
                  .append(Component.text(", is ignited using ", NamedTextColor.GRAY))
                  .append(
                      Component.text(portal.getLighterMaterial().toString(), NamedTextColor.RED))
                  .append(Component.text(" and goes to ", NamedTextColor.GRAY))
                  .append(Component.text(portal.getWorld().getName(), NamedTextColor.RED))
                  .append(Component.text(".", NamedTextColor.GRAY)));
        } else {
          p.sendMessage(
              DimensionsSettings.getPrefix()
                  .append(Component.text("There was a problem, please try reloading the plugin.")));
        }
      }
    } else if (browseInventory.containsKey(p) && e.getInventory().equals(browseInventory.get(p))) {
      e.setCancelled(true);
      if (e.getClickedInventory() != e.getInventory()) return;
      String name =
          PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
      if (name.equalsIgnoreCase(" ") || name.isEmpty()) {
        return;
      } else if (name.equals("Previous page") || name.equals("Next page")) {
        String lore =
            PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().lore().get(0));
        updateBrowseInventory(p, Integer.parseInt(lore.substring(11)) - 1, false);
        return;
      } else {
        CachedPortal cached =
            cachedPortals.stream()
                .filter(portal -> name.equals(portal.getFile()))
                .findAny()
                .orElse(null);
        if (cached != null)
          if (e.isShiftClick()) {
            try {
              if (cached.download(p)) {
                p.sendMessage(
                    DimensionsSettings.getPrefix()
                        .append(
                            Component.text(
                                "The portal has been succesfully downloaded, please use ",
                                NamedTextColor.GREEN))
                        .append(
                            Component.text("/dim reload", NamedTextColor.GREEN)
                                .decorate(TextDecoration.UNDERLINED))
                        .append(Component.text(" to apply changes.", NamedTextColor.GREEN)));
              }
            } catch (IOException e1) {
              p.sendMessage(
                  DimensionsSettings.getPrefix()
                      .append(
                          Component.text(
                              "There was an issue while trying to download the file.",
                              NamedTextColor.RED)));
              e1.printStackTrace();
            }
          } else {
            p.sendMessage(
                DimensionsSettings.getPrefix()
                    .append(
                        Component.text(
                            "Link to portal: " + cached.getLink(), NamedTextColor.GREEN)));
          }
      }
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onClose(InventoryCloseEvent e) {
    if (e.getInventory().equals(portalsInventory.get(e.getPlayer()))) {
      portalsInventory.remove(e.getPlayer());
    }

    if (e.getInventory().equals(browseInventory.get(e.getPlayer()))) {
      browseInventory.remove(e.getPlayer());
    }
  }
}

final class CachedPortal {

  private String id;
  private String file;
  private String creator;
  private int likes;
  private Material block;
  private String yml;

  private CachedPortal(
      String id, String file, String creator, int likes, Material block, String yml) {
    this.id = id;
    this.file = file;
    this.creator = creator;
    this.likes = likes;
    this.block = block;
    this.yml = yml;
  }

  public String getFile() {
    return file;
  }

  public ItemStack getItemStack() {
    ItemStack item = new ItemStack(block);
    ItemMeta meta = item.getItemMeta();
    meta.displayName(Component.text(file, NamedTextColor.GOLD));
    meta.lore(
        Arrays.asList(
            new Component[] {
              Component.text("by " + creator, NamedTextColor.GOLD),
              Component.text(likes + " likes", NamedTextColor.GOLD),
              Component.empty(),
              Component.text("Click to get link to portal", NamedTextColor.GOLD),
              Component.text("Shift+Click to download portal", NamedTextColor.GOLD),
              Component.empty(),
              Component.text("portal id: " + id, NamedTextColor.GOLD)
            }));
    item.setItemMeta(meta);
    return item;
  }

  public String getLink() {
    return "https://astaspasta.alwaysdata.net/editor/portal/?portal=" + id;
  }

  public boolean download(Player p) throws IOException {

    File f = new File(CustomPortalLoader.DIRECTORY_PATH + "/" + file + ".yml");
    if (f.exists()) {
      p.sendMessage(DimensionsSettings.getPrefix() + "A portal with the same name already exists.");
      return false;
    }

    Path file = f.toPath();
    Files.write(file, Arrays.asList(new String[] {yml}), Charset.forName("UTF-8"));

    return true;
  }

  public static CachedPortal create(String id, HashMap<String, Object> map) {
    return new CachedPortal(
        id,
        (String) map.get("file"),
        (String) map.get("creator"),
        (int) ((double) map.get("likes")),
        Material.valueOf(((String) map.get("block")).toUpperCase()),
        (String) map.get("yml"));
  }
}
