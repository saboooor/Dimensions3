package me.xxastaspastaxx.dimensions.addons.testaddon;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsDebbuger;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalIgniteCause;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DimensionsTestAddon extends DimensionsAddon implements Listener {

  // private Plugin pl;

  public DimensionsTestAddon() {
    super(
        "DimensionsTestAddon",
        "3.0.0",
        "That addon should not be here??!",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    // this.pl = pl;

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void onPortalIgnite(CustomPortalIgniteEvent e) {

    if (e.getCause() == CustomPortalIgniteCause.EXIT_PORTAL) {
      DimensionsDebbuger.DEBUG.print(e.getCause(), e.isCancelled());
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postPortalIgnite(CustomPortalIgniteEvent e) {

    System.out.println("TEST3");
  }
}
