package me.xxastaspastaxx.dimensions.addons.portalbleportals;

import java.util.HashMap;
import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.completePortal.PortalGeometry;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalDestroyCause;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalIgniteCause;
import me.xxastaspastaxx.dimensions.events.CustomPortalBreakEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DimensionsPortablePortals extends DimensionsAddon implements Listener {

  // private Plugin pl;

  // portal portals
  // charges
  // timed
  // uses
  // specific lcoation
  // specific world

  public DimensionsPortablePortals() {
    super(
        "DimensionsPortablePortalsAddon",
        "3.0.1",
        "Take portals in your hands",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    // this.pl = pl;

    Dimensions.getCommandManager()
        .registerCommand(
            new SpawnPortalCommand(
                "spawnPortal",
                "-portal <portal> [args...]",
                new String[] {"build"},
                "Build a temporary portal",
                "",
                true,
                this));

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void onPortalUse(CustomPortalUseEvent e) {
    if (!(e.getEntity() instanceof Player)) return;
    CompletePortal complete = e.getCompletePortal();
    Object obj = complete.getTag("soloUse");
    if (obj != null) {
      Player p = (Player) e.getEntity();
      if (!p.getUniqueId().toString().equals((String) obj)) e.setCancelled(true);
    }

    Object forceLoc = complete.getTag("portableForceLocation");
    Object forceWorld = complete.getTag("portableForceWorld");
    if (forceLoc != null || forceWorld != null) {
      e.setDestinationPortal(
          complete.getDestinationPortal(
              true,
              forceLoc == null ? null : (Location) forceLoc,
              forceWorld == null ? null : (World) forceWorld));
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postPortalUse(CustomPortalUseEvent e) {
    if (!(e.getEntity() instanceof Player)) return;
    CompletePortal complete = e.getCompletePortal();
    Object obj = complete.getTag("soloUse");
    if (obj != null) {
      Dimensions.getCompletePortalManager()
          .removePortal(complete, CustomPortalDestroyCause.PLUGIN, null);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postPortalDestroy(CustomPortalBreakEvent e) {
    CompletePortal complete = e.getCompletePortal();
    Object obj = complete.getTag("restoreBlocks");
    if (obj != null) {
      @SuppressWarnings("unchecked")
      HashMap<Block, BlockData> data = (HashMap<Block, BlockData>) obj;
      for (Block bd : data.keySet()) {
        bd.setBlockData(data.get(bd), false);
      }
    }
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {}

  public void buildPortal(
      CustomPortal portal,
      Player target,
      Location loc,
      boolean zAxis,
      int width,
      int height,
      boolean isPublic,
      int time,
      List<String> timeEffects,
      int maxUses,
      Location forceLocation,
      String forceWorld) {

    Location min = loc.clone().add(zAxis ? 0 : -width, 0, zAxis ? -width : 0);
    Location max = loc.clone().add(zAxis ? 0 : width - 0.5, height, zAxis ? width - 0.5 : 0);

    World world = loc.getWorld();

    PortalGeometry geom =
        PortalGeometry.getPortalGeometry(portal).createGeometry(min.toVector(), max.toVector());
    geom.buildPortal(min, world, portal);

    CompletePortal tempPortal = new CompletePortal(portal, world, geom);

    boolean keepRestore = false;

    if (!isPublic) {
      tempPortal.setTag("soloUse", target.getUniqueId().toString());
      keepRestore = true;
    }

    if (time != 0) {
      DimensionsAddon.setOption(tempPortal, "timedPortalsDestroyAfter", time);
      DimensionsAddon.setOption(tempPortal, "timedPortalsAction", "Close");

      // 5000->timer1,6000->timer1,7000->timer1,8000->timer1,9000->timer1,10000->timer2,15000->timer3
      DimensionsAddon.setOption(tempPortal, "timedPortalsEffects", timeEffects);
      keepRestore = true;
    }

    if (maxUses != 0) {
      DimensionsAddon.setOption(tempPortal, "limitedUses", maxUses);
      DimensionsAddon.setOption(tempPortal, "limitedUsesAction", "Close");
      keepRestore = true;
    }

    if (keepRestore) {
      double maxY = (min.getY() + height);
      double maxSide = ((zAxis ? min.getZ() : min.getX()) + width + 1);

      HashMap<Block, BlockData> data = new HashMap<Block, BlockData>();
      for (double y = min.getY(); y <= maxY; y++) {
        for (double side = (zAxis ? min.getZ() : min.getX()); side <= maxSide; side++) {
          Block block =
              new Location(world, zAxis ? min.getX() : side, y, !zAxis ? min.getZ() : side)
                  .getBlock();
          data.put(block, block.getBlockData());
        }
      }

      tempPortal.setTag("restoreBlocks", data);
    }

    if (forceLocation != null) {
      tempPortal.setTag("portableForceLocation", forceLocation);
    }
    if (forceWorld != null) tempPortal.setTag("portableForceWorld", forceWorld);

    // create portal
    Dimensions.getCompletePortalManager()
        .createNew(tempPortal, target, CustomPortalIgniteCause.PLUGIN, null);
  }
}
