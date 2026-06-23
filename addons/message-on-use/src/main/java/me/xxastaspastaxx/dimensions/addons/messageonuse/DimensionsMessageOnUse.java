package me.xxastaspastaxx.dimensions.addons.messageonuse;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DimensionsMessageOnUse extends DimensionsAddon implements Listener {

  // private Plugin pl;

  public DimensionsMessageOnUse() {
    super(
        "DimensionsMessageOnUseAddon",
        "3.0.2",
        "Send a message when players use portals",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    // this.pl = pl;

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postPortalUse(CustomPortalUseEvent e) {
    if (!(e.getEntity() instanceof Player)) return;

    Object msg = getOption(e.getCompletePortal(), "messageOnUse");
    if (msg == null) return;

    e.getEntity().sendMessage((String) msg);
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    String str = portalConfig.getString("Addon.MessageOnUse", "none");

    if (str.equalsIgnoreCase("none")) return;

    setOption(portal, "messageOnUse", str.replace("&", "§"));

    return;
  }
}
