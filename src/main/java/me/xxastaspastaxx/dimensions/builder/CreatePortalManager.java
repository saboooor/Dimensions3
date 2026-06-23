package me.xxastaspastaxx.dimensions.builder;

import java.util.HashMap;
import me.xxastaspastaxx.dimensions.Dimensions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class CreatePortalManager {

  public static HashMap<Player, CreatePortalInstance> map =
      new HashMap<Player, CreatePortalInstance>();

  public CreatePortalManager(Dimensions pl) {

    Bukkit.getScheduler()
        .scheduleSyncRepeatingTask(
            pl,
            () -> {
              map.values().forEach(inst -> inst.spawnParticles());
            },
            15,
            15);

    Bukkit.getPluginManager().registerEvents(new CreatePortalListener(this), pl);
  }

  public void handle(Player p) {
    if (map.containsKey(p)) {
      map.get(p).open();
    } else {
      map.put(p, new CreatePortalInstance(p, true));
    }
  }

  public boolean hasInstance(Player p) {
    return map.containsKey(p);
  }

  public boolean click(
      Player p, Inventory inv, int rawSlot, boolean rightClick, boolean shiftClick) {
    return map.get(p).click(inv, rawSlot, rightClick, shiftClick);
  }

  public CreatePortalInstance getInstance(Player p) {
    return map.get(p);
  }

  public void clear(Player p) {
    p.closeInventory();
    map.remove(p);
  }
}
