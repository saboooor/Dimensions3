package me.xxastaspastaxx.dimensions.addons.customlighter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsDebbuger;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.addons.customlighter.commands.CustomFrameCommand;
import me.xxastaspastaxx.dimensions.addons.customlighter.commands.CustomInsideCommand;
import me.xxastaspastaxx.dimensions.addons.customlighter.commands.CustomLighterCommand;
import me.xxastaspastaxx.dimensions.addons.customlighter.framemanager.CustomItemsFrameManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.framemanager.FrameManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.framemanager.ItemsAdderFrameManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.framemanager.OraxenFrameManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.framemanager.VanillaFrameManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.insidemanager.CustomItemsInsideManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.insidemanager.InsideManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.insidemanager.ItemsAdderInsideManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.insidemanager.OraxenInsideManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.insidemanager.VanillaInsideManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.itemmanager.CustomItemsItemManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.itemmanager.ItemManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.itemmanager.ItemsAdderItemManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.itemmanager.OraxenItemManager;
import me.xxastaspastaxx.dimensions.addons.customlighter.itemmanager.VanillaItemManager;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.completePortal.PortalGeometry;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalIgniteCause;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DimensionsCustomLighter extends DimensionsAddon implements Listener {

  // private Plugin pl;

  public static DimensionsCustomLighter instance;

  private HashMap<CustomPortal, YamlConfiguration> configs =
      new HashMap<CustomPortal, YamlConfiguration>();

  public DimensionsCustomLighter() {
    super(
        "DimensionsCustomLighterAddon",
        "3.0.4",
        "Custom lighters and blocks",
        DimensionsAddonPriority.NORMAL);
    DimensionsCustomLighter.instance = this;
  }

  @Override
  public void onEnable(Dimensions pl) {
    // this.pl = pl;

    Dimensions.getCommandManager()
        .registerCommand(
            new CustomLighterCommand(
                "setLighter",
                "<portal>",
                new String[0],
                "Set the vanilla lighter for the portal",
                "",
                true,
                this));
    Dimensions.getCommandManager()
        .registerCommand(
            new CustomFrameCommand(
                "setFrameBlock",
                "<portal>",
                new String[0],
                "Set the vanilla frame blockdata for the portal",
                "",
                true,
                this));
    Dimensions.getCommandManager()
        .registerCommand(
            new CustomInsideCommand(
                "setInsideBlock",
                "<portal>",
                new String[0],
                "Set the vanilla inside blockdata for the portal",
                "",
                true,
                this));

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onPortalIgnite(CustomPortalIgniteEvent e) {

    DimensionsDebbuger.DEBUG.print("testtt");

    CompletePortal complete = e.getCompletePortal();
    Object manager = getOption(complete, "customItem");
    if (manager == null) return;

    if (e.getLighter() == null) {
      if (e.getCause() == CustomPortalIgniteCause.PLAYER) e.setCancelled(true);
      return;
    }

    @SuppressWarnings("unchecked")
    List<ItemManager> itemManagers = (List<ItemManager>) manager;

    for (ItemManager itemManager : itemManagers) {
      if (itemManager.isAccepted(e.getLighter())) {
        return;
      }
    }
    e.setCancelled(true);
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    List<ItemManager> itemManagers = new ArrayList<ItemManager>();
    for (String item : portalConfig.getStringList("Addon.CustomLighter.Item")) {
      ItemManager itemManager = null;
      String itemManagerString = item.substring(0, item.indexOf(':')).toUpperCase();
      item = item.substring(item.indexOf(':') + 1, item.length());
      if (itemManagerString.equals("MINECRAFT")) itemManager = new VanillaItemManager(item);
      if (itemManagerString.equals("ITEMSADDER")) itemManager = new ItemsAdderItemManager(item);
      if (itemManagerString.equals("ORAXEN")) itemManager = new OraxenItemManager(item);
      if (itemManagerString.equals("CUSTOMITEMS")) itemManager = new CustomItemsItemManager(item);
      itemManagers.add(itemManager);
    }
    if (!itemManagers.isEmpty()) setOption(portal, "customItem", itemManagers);

    String frame = portalConfig.getString("Addon.CustomLighter.FrameBlock");
    if (frame != null) {
      FrameManager frameManager = null;
      String frameManagerString = frame.substring(0, frame.indexOf(':')).toUpperCase();
      frame = frame.substring(frame.indexOf(':') + 1, frame.length());
      if (frameManagerString.equals("MINECRAFT")) frameManager = new VanillaFrameManager(frame);
      if (frameManagerString.equals("ITEMSADDER")) frameManager = new ItemsAdderFrameManager(frame);
      if (frameManagerString.equals("ORAXEN"))
        frameManager = new OraxenFrameManager(Integer.parseInt(frame));
      if (frameManagerString.equals("CUSTOMITEMS"))
        frameManager = new CustomItemsFrameManager(frame);
      setOption(portal, "customFrame", frameManager);
      PortalGeometry.setCustomGeometry(portal, new CustomPortalGeometry(null, null, frameManager));
    }

    String inside = portalConfig.getString("Addon.CustomLighter.InsideBlock");
    if (inside != null) {
      InsideManager insideManager = null;
      String insideManagerString = inside.substring(0, inside.indexOf(':')).toUpperCase();
      inside = inside.substring(inside.indexOf(':') + 1, inside.length());
      if (insideManagerString.equals("MINECRAFT")) insideManager = new VanillaInsideManager(inside);
      if (insideManagerString.equals("ITEMSADDER"))
        insideManager = new ItemsAdderInsideManager(inside);
      if (insideManagerString.equals("ORAXEN"))
        insideManager = new OraxenInsideManager(Integer.parseInt(inside));
      if (insideManagerString.equals("CUSTOMITEMS"))
        insideManager = new CustomItemsInsideManager(inside);

      portal.setInsideBlockData(insideManager.getBlockData());
    }

    configs.put(portal, portalConfig);
  }

  public YamlConfiguration getPortalConfig(CustomPortal portal) {
    return configs.get(portal);
  }
}
