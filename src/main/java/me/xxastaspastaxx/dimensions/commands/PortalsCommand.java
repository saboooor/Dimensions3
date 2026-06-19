package me.xxastaspastaxx.dimensions.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
      String msg = DimensionsSettings.getPrefix() + "Portals list:";
      for (CustomPortal portal : Dimensions.getCustomPortalManager().getCustomPortals()) {
        msg +=
            "\n["
                + (portal.isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")
                + ChatColor.GRAY
                + "] "
                + portal.getPortalId();
      }

      sender.sendMessage(msg);
    }
  }

  private void setupMenu() {

    mainInventory = Bukkit.createInventory(null, 9, ChatColor.RED + "Dimensions");

    ItemStack decor = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta decorMeta = decor.getItemMeta();
    decorMeta.setDisplayName(ChatColor.GOLD.toString());
    decor.setItemMeta(decorMeta);

    for (int i = 0; i < 9; i++) mainInventory.setItem(i, decor);

    ItemStack myPortals = new ItemStack(Material.PAPER);
    ItemMeta myPortalsMeta = myPortals.getItemMeta();
    myPortalsMeta.setDisplayName(ChatColor.GOLD + "My Portals");
    myPortalsMeta.setLore(
        Arrays.asList(new String[] {ChatColor.GRAY + "Click to view your portals"}));
    myPortals.setItemMeta(myPortalsMeta);

    mainInventory.setItem(3, myPortals);

    ItemStack browsePortals = new ItemStack(Material.CHEST);
    ItemMeta browsePortalsMeta = browsePortals.getItemMeta();
    browsePortalsMeta.setDisplayName(ChatColor.AQUA + "Browse portals online");
    browsePortalsMeta.setLore(
        Arrays.asList(
            new String[] {
              ChatColor.GRAY + "Click to browse portals online",
              ChatColor.GRAY
                  + "Use "
                  + ChatColor.UNDERLINE
                  + "Shift+Click"
                  + ChatColor.GRAY
                  + " to forcfully load portals"
            }));
    browsePortals.setItemMeta(browsePortalsMeta);

    mainInventory.setItem(5, browsePortals);
  }

  private void updatePortalsMenu(Player p, int page) {
    ArrayList<CustomPortal> portals = Dimensions.getCustomPortalManager().getCustomPortals();

    Inventory inv =
        Bukkit.createInventory(
            null,
            54,
            "My portals | Page "
                + ChatColor.DARK_RED
                + (page + 1)
                + ChatColor.RED
                + "/"
                + ((int) Math.ceil(portals.size() / 45f)));

    for (int i = page * 45; i < (page + 1) * 45; i++) {
      if (i >= portals.size()) break;
      CustomPortal portal = portals.get(i);
      ItemStack portalItem = new ItemStack(portal.getOutsideMaterial());
      ItemMeta itemMeta = portalItem.getItemMeta();
      itemMeta.setDisplayName(ChatColor.GOLD + portal.getPortalId());
      itemMeta.setLore(
          Arrays.asList(
              new String[] {
                ChatColor.GOLD + ChatColor.BOLD.toString() + portal.getDisplayName(),
                ChatColor.GRAY + "Click for more details"
              }));
      portalItem.setItemMeta(itemMeta);
      inv.addItem(portalItem);
    }

    ItemStack decor = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta decorMeta = decor.getItemMeta();
    decorMeta.setDisplayName(ChatColor.GOLD.toString());
    decor.setItemMeta(decorMeta);

    for (int i = 46; i <= 52; i++) inv.setItem(i, decor);

    ItemStack goBack = new ItemStack(page == 0 ? Material.BARRIER : Material.ARROW);
    ItemMeta goBackMeta = goBack.getItemMeta();
    goBackMeta.setDisplayName(ChatColor.GOLD + "Previous page");
    goBackMeta.setLore(
        Arrays.asList(new String[] {ChatColor.GRAY + "Go to page " + ((int) Math.max(1, page))}));
    goBack.setItemMeta(goBackMeta);
    inv.setItem(45, goBack);

    ItemStack goMprosta =
        new ItemStack((page + 1) * 45 >= portals.size() ? Material.BARRIER : Material.ARROW);
    ItemMeta goMprostaMeta = goBack.getItemMeta();
    goMprostaMeta.setDisplayName(ChatColor.GOLD + "Next page");
    goMprostaMeta.setLore(
        Arrays.asList(
            new String[] {
              ChatColor.GRAY
                  + "Go to page "
                  + ((int) Math.min(Math.ceil(portals.size() / 45f), page + 2))
            }));
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
            "Browse portals | Page "
                + ChatColor.DARK_RED
                + (page + 1)
                + ChatColor.RED
                + "/"
                + ((int) Math.ceil(cachedPortals.size() / 45f)));

    if (forceUpdate || System.currentTimeMillis() - lastUpdate >= 108000000) {
      p.closeInventory();
      lastUpdate = System.currentTimeMillis();
      cachedPortals.clear();
      p.sendMessage("Fetching portals...");

      HashMap<String, HashMap<String, Object>> portals = null;

      try {
        portals =
            gson.fromJson(
                readStringFromURL(
                    "https://astaspasta.alwaysdata.net/api/portalData.php?all="
                        + p.getUniqueId().toString().replace("-", "")),
                new TypeToken<HashMap<String, HashMap<String, Object>>>() {}.getType());
      } catch (JsonSyntaxException | IOException e) {
        p.sendMessage("There was an issue while trying to fetch portals");
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
    decorMeta.setDisplayName(ChatColor.GOLD.toString());
    decor.setItemMeta(decorMeta);

    for (int i = 46; i <= 52; i++) inv.setItem(i, decor);

    ItemStack goBack = new ItemStack(page == 0 ? Material.BARRIER : Material.ARROW);
    ItemMeta goBackMeta = goBack.getItemMeta();
    goBackMeta.setDisplayName(ChatColor.GOLD + "Previous page");
    goBackMeta.setLore(
        Arrays.asList(new String[] {ChatColor.GRAY + "Go to page " + ((int) Math.max(1, page))}));
    goBack.setItemMeta(goBackMeta);
    inv.setItem(45, goBack);

    ItemStack goMprosta =
        new ItemStack((page + 1) * 45 >= cachedPortals.size() ? Material.BARRIER : Material.ARROW);
    ItemMeta goMprostaMeta = goBack.getItemMeta();
    goMprostaMeta.setDisplayName(ChatColor.GOLD + "Next page");
    goMprostaMeta.setLore(
        Arrays.asList(
            new String[] {
              ChatColor.GRAY
                  + "Go to page "
                  + ((int) Math.min(Math.ceil(cachedPortals.size() / 45f), page + 2))
            }));
    goMprosta.setItemMeta(goMprostaMeta);
    inv.setItem(53, goMprosta);

    p.openInventory(inv);
    browseInventory.put(p, inv);
  }

  public static String readStringFromURL(String requestURL) throws IOException {
    try (Scanner scanner =
        new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString())) {
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
                  + ChatColor.RED
                  + "You do not have the "
                  + ChatColor.UNDERLINE
                  + "dimensions.forceupdatebrowser"
                  + " permission to do perform that action");
        }
      ;
    } else if (portalsInventory.containsKey(p)
        && e.getInventory().equals(portalsInventory.get(p))) {
      e.setCancelled(true);
      if (e.getClickedInventory() != e.getInventory()) return;
      String name = item.getItemMeta().getDisplayName();
      if (name.equalsIgnoreCase(ChatColor.GOLD.toString())) {
        return;
      } else if (name.contentEquals(ChatColor.GOLD + "Previous page")
          || name.contentEquals(ChatColor.GOLD + "Next page")) {
        updatePortalsMenu(
            p, Integer.parseInt(item.getItemMeta().getLore().get(0).substring(13)) - 1);
        return;
      } else {
        CustomPortal portal =
            Dimensions.getCustomPortalManager()
                .getCustomPortal(item.getItemMeta().getDisplayName().substring(2));
        if (portal != null) {
          p.sendMessage(
              DimensionsSettings.getPrefix()
                  + portal.getDisplayName()
                  + ":"
                  + ChatColor.GRAY
                  + " Is built from "
                  + ChatColor.RED
                  + portal.getOutsideMaterial()
                  + ChatColor.GRAY
                  + ", is ignited using "
                  + ChatColor.RED
                  + portal.getLighterMaterial()
                  + ChatColor.GRAY
                  + " and goes to "
                  + ChatColor.RED
                  + portal.getWorld().getName()
                  + ChatColor.GRAY
                  + ".");
        } else {
          p.sendMessage(
              DimensionsSettings.getPrefix()
                  + "There was a problem, please try reloading the plugin.");
        }
      }
    } else if (browseInventory.containsKey(p) && e.getInventory().equals(browseInventory.get(p))) {
      e.setCancelled(true);
      if (e.getClickedInventory() != e.getInventory()) return;
      String name = item.getItemMeta().getDisplayName();
      if (name.equalsIgnoreCase(ChatColor.GOLD.toString())) {
        return;
      } else if (name.contentEquals(ChatColor.GOLD + "Previous page")
          || name.contentEquals(ChatColor.GOLD + "Next page")) {
        updateBrowseInventory(
            p, Integer.parseInt(item.getItemMeta().getLore().get(0).substring(13)) - 1, false);
        return;
      } else {
        CachedPortal cached =
            cachedPortals.stream()
                .filter(portal -> name.equals(ChatColor.GOLD + portal.getFile()))
                .findAny()
                .orElseGet(null);
        if (cached != null)
          if (e.isShiftClick()) {
            try {
              if (cached.download(p)) {
                p.sendMessage(
                    DimensionsSettings.getPrefix()
                        + ChatColor.GREEN
                        + "The portal has been succesfully downloaded, please"
                        + " use "
                        + ChatColor.UNDERLINE
                        + "/dim reload"
                        + ChatColor.GREEN
                        + " to apply changes.");
              }
            } catch (IOException e1) {
              p.sendMessage(
                  DimensionsSettings.getPrefix()
                      + ChatColor.RED
                      + "There was an issue while trying to download the file.");
              e1.printStackTrace();
            }
          } else {
            p.sendMessage(
                DimensionsSettings.getPrefix()
                    + ChatColor.GREEN
                    + "Link to portal: "
                    + cached.getLink());
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
    meta.setDisplayName(ChatColor.GOLD + file);
    meta.setLore(
        Arrays.asList(
            new String[] {
              ChatColor.GOLD + "by " + ChatColor.UNDERLINE + creator,
              ChatColor.GOLD + ChatColor.UNDERLINE.toString() + likes + ChatColor.GOLD + " likes",
              "",
              ChatColor.GOLD + "Click to get link to portal",
              ChatColor.GOLD + "Shift+Click to download portal",
              "",
              ChatColor.GOLD + ChatColor.ITALIC.toString() + "portal id: " + id
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
