package me.xxastaspastaxx.dimensions.addons.timedportals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.addons.particles.ParticlePack;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.completePortal.PortalGeometry;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalDestroyCause;
import me.xxastaspastaxx.dimensions.events.CustomPortalBreakEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class DimensionsTimedPortals extends DimensionsAddon implements Listener {

  private Plugin pl;

  private HashMap<CompletePortal, ArrayList<Integer>> threads =
      new HashMap<CompletePortal, ArrayList<Integer>>();

  public DimensionsTimedPortals() {
    super(
        "DimensionsTimedPortalsAddon",
        "3.0.2",
        "Portals unlit after some time",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    this.pl = pl;

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postPortalIgnite(CustomPortalIgniteEvent e) {

    CompletePortal complete = e.getCompletePortal();

    Object destroyAfter = getOption(complete, "timedPortalsDestroyAfter");
    if (destroyAfter == null) return;

    if (threads.containsKey(complete)) {
      for (int i : threads.remove(complete)) {
        Bukkit.getScheduler().cancelTask(i);
      }
    }

    ArrayList<Integer> ids = new ArrayList<Integer>();

    @SuppressWarnings("unchecked")
    List<String> effects = (List<String>) getOption(complete, "timedPortalsEffects");
    if (!effects.isEmpty()) {
      for (String effect : effects) {
        String[] spl = effect.split("->");
        ids.add(
            Bukkit.getScheduler()
                .scheduleSyncDelayedTask(
                    pl,
                    new Runnable() {

                      @Override
                      public void run() {
                        ParticlePack.get(spl[1]).begin(complete);
                      }
                    },
                    (Integer.parseInt(spl[0])) / 50));
      }
    }

    ids.add(
        Bukkit.getScheduler()
            .scheduleSyncDelayedTask(
                pl,
                new Runnable() {

                  @Override
                  public void run() {

                    destroy(complete, (String) getOption(complete, "timedPortalsAction"));
                  }
                },
                ((int) destroyAfter) / 50));

    threads.put(complete, ids);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postPortalDestroy(CustomPortalBreakEvent e) {

    CompletePortal complete = e.getCompletePortal();

    if (threads.containsKey(complete)) {
      for (int i : threads.remove(complete)) {
        Bukkit.getScheduler().cancelTask(i);
      }
    }
  }

  public void destroy(CompletePortal completePortal, String action) {
    threads.remove(completePortal);

    if (action.startsWith("Close")) {
      Dimensions.getCompletePortalManager()
          .removePortal(completePortal, CustomPortalDestroyCause.PLUGIN, null);
    } else if (action.startsWith("Destroy")) {
      Dimensions.getCompletePortalManager()
          .removePortal(completePortal, CustomPortalDestroyCause.PLUGIN, null);

      PortalGeometry geom = completePortal.getPortalGeometry();
      Vector min = geom.getMin();
      Vector max = geom.getMax();
      boolean zAxis = geom.iszAxis();

      for (double y = min.getY(); y <= max.getY(); y++) {
        for (double side = zAxis ? min.getZ() : min.getX();
            side <= (zAxis ? max.getZ() : max.getX());
            side++) {
          (new Location(
                  completePortal.getWorld(),
                  zAxis ? min.getX() : side,
                  y,
                  !zAxis ? min.getZ() : side))
              .getBlock()
              .setType(Material.AIR);
        }
      }
    }

    if (action.contains("{explode%")
        && Math.random() <= Integer.parseInt(action.split("%")[1].replace("}", "")) / 100) {
      completePortal.getWorld().createExplosion(completePortal.getCenter(), 5);
    }
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    int uses = portalConfig.getInt("Addon.TimedPortals.DestroyAfterMillis", 0);
    if (uses == 0) return;

    setOption(portal, "timedPortalsDestroyAfter", uses);
    setOption(
        portal, "timedPortalsAction", portalConfig.getString("Addon.TimedPortals.Action", "Close"));
    setOption(
        portal, "timedPortalsEffects", portalConfig.getStringList("Addon.TimedPortals.Effects"));

    return;
  }
}
