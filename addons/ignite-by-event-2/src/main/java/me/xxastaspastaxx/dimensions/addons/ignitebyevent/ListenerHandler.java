package me.xxastaspastaxx.dimensions.addons.ignitebyevent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

public class ListenerHandler implements Listener {

  private Dimensions pl;
  private DimensionsIgniteByEvent main;

  public ListenerHandler(Dimensions pl, DimensionsIgniteByEvent main) {
    this.main = main;
    this.pl = pl;

    Bukkit.getScheduler()
        .scheduleSyncRepeatingTask(
            pl,
            () -> {
              ArrayList<Entity> toRemove = new ArrayList<Entity>();
              trackedItems.forEach(
                  (item, location) -> {
                    if (item.getLocation().equals(location)) toRemove.add(item);
                  });

              // instead of location maybe use item.getLocation
              trackedItems.forEach(
                  (item, location) -> {
                    if (main.getHandler(DropItem.class).stream()
                        .anyMatch(i -> i.ignite(item.getLocation()))) {
                      if (DimensionsSettings.consumeItems) item.remove();
                      toRemove.add(item);
                    }
                  });

              toRemove.forEach(item -> trackedItems.remove(item));

              Iterator<Entity> iter = trackedItems.keySet().iterator();
              while (iter.hasNext()) {
                Entity en = iter.next();
                trackedItems.put(en, en.getLocation());
              }
            },
            DimensionsSettings.updateEveryTick,
            DimensionsSettings.updateEveryTick);

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  private HashMap<Entity, Location> trackedItems = new HashMap<Entity, Location>();

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void event(ItemSpawnEvent e) {

    if (main.getHandler(DropItem.class).stream()
        .anyMatch(i -> i.isAccepted(e.getEntity().getItemStack()))) {
      trackedItems.put(e.getEntity(), e.getLocation());
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void event(EntityDeathEvent e) {

    EntityDeath onEntityDeath =
        (EntityDeath)
            main.getHandler(EntityDeath.class).stream()
                .filter(i -> i.isAccepted(e.getEntity()))
                .findAny()
                .orElse(null);

    if (onEntityDeath != null) {

      Location loc = e.getEntity().getLocation();
      Bukkit.getScheduler()
          .runTaskLater(
              pl,
              () -> {
                onEntityDeath.ignite(loc);
              },
              1);
    }
  }

  /*@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void event(BlockBreakEvent e) {
  	onBlockBreak(e.getBlock());
  }

  public void onBlockBreak(Block block) {
  	if (main.onBlockBreak.isAccepted(block)) {
  		main.onBlockBreak.ignite(block.getLocation());
  	}
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void event(BlockPlaceEvent e) {
  	onBlockPlace(e.getBlock());
  }

  public void onBlockPlace(Block block) {
  	if (main.onBlockPlace.isAccepted(block)) {
  		main.onBlockPlace.ignite(block.getLocation());
  	}
  }*/
}
