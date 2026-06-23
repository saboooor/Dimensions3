package me.xxastaspastaxx.dimensions.addons.limiteduses;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.completePortal.PortalGeometry;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalDestroyCause;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class DimensionsLimitedUses extends DimensionsAddon implements Listener {

  private Plugin pl;

  public DimensionsLimitedUses() {
    super(
        "DimensionsLimitedUsesAddon",
        "3.0.1",
        "Portals can now only be used specific amount of times",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    this.pl = pl;

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postPortalUse(CustomPortalUseEvent e) {

    CompletePortal complete = e.getCompletePortal();
    Object maxUsesOBJ = getOption(complete, "limitedUses");
    if (maxUsesOBJ == null) return;

    Object hubWorldName = getOption(complete, "hubWorld");
    if (hubWorldName != null) {
      if (complete.getWorld().getName().contentEquals((String) hubWorldName)) return;
    }
    int maxUses = (int) maxUsesOBJ;
    String action = (String) getOption(complete, "limitedUsesAction");

    Object usesOBJ = complete.getTag("portalUses");
    int uses = 0;
    if (usesOBJ == null) complete.setTag("portalUses", 0);
    else uses = (int) usesOBJ;
    complete.setTag("portalUses", ++uses);
    if (uses >= maxUses) {
      if (action.startsWith("Close")) {
        Dimensions.getCompletePortalManager()
            .removePortal(complete, CustomPortalDestroyCause.PLUGIN, e.getEntity());
      } else if (action.startsWith("Destroy")) {

        Dimensions.getCompletePortalManager()
            .removePortal(complete, CustomPortalDestroyCause.PLUGIN, e.getEntity());

        PortalGeometry geom = complete.getPortalGeometry();
        Vector min = geom.getMin();
        Vector max = geom.getMax();
        boolean zAxis = geom.iszAxis();

        for (double y = min.getY(); y <= max.getY(); y++) {
          for (double side = zAxis ? min.getZ() : min.getX();
              side <= (zAxis ? max.getZ() : max.getX());
              side++) {
            (new Location(
                    complete.getWorld(), zAxis ? min.getX() : side, y, !zAxis ? min.getZ() : side))
                .getBlock()
                .setType(Material.AIR);
          }
        }
      }

      if (action.contains("{explode%")
          && Math.random() <= Integer.parseInt(action.split("%")[1].replace("}", "")) / 100) {
        Bukkit.getScheduler()
            .scheduleSyncDelayedTask(
                pl,
                new Runnable() {

                  @Override
                  public void run() {
                    complete.getWorld().createExplosion(complete.getCenter(), 5);
                  }
                },
                1);
      }
    }
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    int uses = portalConfig.getInt("Addon.LimitedUses.MaxUses", 0);
    if (uses == 0) return;

    setOption(portal, "limitedUses", uses);
    setOption(
        portal,
        "limitedUsesAction",
        portalConfig.getString("Addon.LimitedUses.Action", "Destroy{explode%100}"));

    return;
  }
}
