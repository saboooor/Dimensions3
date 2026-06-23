package me.xxastaspastaxx.dimensions.addons.weather;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DimensionsWeather extends DimensionsAddon implements Listener {

  // private Plugin pl;

  public DimensionsWeather() {
    super(
        "DimensionsWeatherAddon",
        "3.0.2",
        "Weather controls portals",
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

    Object obj = getOption(complete, "weatherDisabled");
    if (obj == null) return;

    String disabled = (String) obj;
    Entity entity = e.getEntity();

    World world = complete.getWorld();
    boolean thunder = world.isThundering();
    boolean rain = world.hasStorm() && !thunder;
    boolean clear = !thunder && !rain;
    if ((disabled.contains("RAIN") && rain)
        || (disabled.contains("THUNDER") && thunder)
        || (disabled.contains("CLEAR") && clear)) {
      if (entity instanceof Player)
        entity.sendMessage((String) getOption(complete, "weatherDenyMessage"));
      e.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void onPortalUse(CustomPortalUseEvent e) {

    CompletePortal complete = e.getCompletePortal();

    Object obj = getOption(complete, "weatherDisabled");
    if (obj == null) return;

    String disabled = (String) obj;
    Entity entity = e.getEntity();

    World world = complete.getWorld();
    boolean thunder = world.isThundering();
    boolean rain = world.hasStorm() && !thunder;
    boolean clear = !thunder && !rain;
    if ((disabled.contains("RAIN") && rain)
        || (disabled.contains("THUNDER") && thunder)
        || (disabled.contains("CLEAR") && clear)) {
      if (entity instanceof Player)
        entity.sendMessage((String) getOption(complete, "weatherDenyMessage"));
      e.setCancelled(true);
    }
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    String disabled = portalConfig.getString("Addon.Weather.Disabled", "none");
    if (disabled.contentEquals("none")) return;

    setOption(portal, "weatherDisabled", disabled);
    setOption(
        portal,
        "weatherDenyMessage",
        portalConfig
            .getString(
                "Addon.Weather.DenyMessage",
                "The portal cannot be activate at this weather condition")
            .replace("&", "§"));

    return;
  }
}
