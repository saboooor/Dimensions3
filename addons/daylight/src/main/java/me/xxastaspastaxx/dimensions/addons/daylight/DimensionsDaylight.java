package me.xxastaspastaxx.dimensions.addons.daylight;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalIgniteCause;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DimensionsDaylight extends DimensionsAddon implements Listener {

  // private Plugin pl;

  public DimensionsDaylight() {
    super(
        "DimensionsDayLightAddon",
        "3.0.2",
        "Allow portals to be ignite only for a specific period of time",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    // this.pl = pl;

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void onPortalIgnite(CustomPortalIgniteEvent e) {

    CompletePortal complete = e.getCompletePortal();
    Object min = getOption(complete, "dayLightMin");
    if (min == null) return;

    if (e.getCause() != CustomPortalIgniteCause.EXIT_PORTAL
        && !isOk(complete, complete.getWorld().getTime())) {
      Entity entity = e.getEntity();
      if (entity instanceof Player)
        entity.sendMessage((String) getOption(complete, "dayLightDenyMessage"));
      e.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void onPortalUse(CustomPortalUseEvent e) {

    CompletePortal complete = e.getCompletePortal();
    Object min = getOption(complete, "dayLightMin");
    if (min == null) return;

    if (!isOk(complete, complete.getWorld().getTime())) {
      Entity entity = e.getEntity();
      if (entity instanceof Player)
        entity.sendMessage((String) getOption(complete, "dayLightDenyMessage"));
      e.setCancelled(true);
    }
  }

  public boolean isOk(CompletePortal complete, long time) {
    int min = (int) getOption(complete, "dayLightMin");
    int max = (int) getOption(complete, "dayLightMax");

    if (min < max) {
      if ((!(time >= min && time <= max))) {
        return false;
      }
    } else {
      // 18000
      // 20000-6000
      if (time < min && time > max) {
        return false;
      }
    }

    return true;
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    int min = portalConfig.getInt("Addon.DayLightSensor.StartAllow", 0);
    int max = portalConfig.getInt("Addon.DayLightSensor.StopAllow", 0);
    if (min == max) return;

    setOption(portal, "dayLightMin", min);
    setOption(portal, "dayLightMax", max);
    setOption(
        portal,
        "dayLightDenyMessage",
        portalConfig
            .getString(
                "Addon.DayLightSensor.DenyMessage",
                "The portal cannot be activate at this time of the day")
            .replace("&", "§"));
  }
}
