package me.xxastaspastaxx.dimensions.addons.pastedportals.skyblock;

import java.util.ArrayList;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsScheduler;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import me.xxastaspastaxx.dimensions.addons.pastedportals.DimensionsPastedPortalsAddon;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.completePortal.PortalGeometry;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalDestroyCause;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalIgniteCause;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import world.bentobox.bentobox.api.events.island.IslandCreateEvent;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.events.island.IslandResetEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.database.objects.Island;

public class PastedBentoBox implements Listener {

  DimensionsPastedPortalsAddon main;

  public PastedBentoBox(DimensionsPastedPortalsAddon main) {
    this.main = main;

    Bukkit.getServer().getPluginManager().registerEvents(this, main.getPlugin());
  }

  @EventHandler(ignoreCancelled = true)
  public void onIslandCreated(IslandCreateEvent e) {
    // blueprintBundle.put(e.getIsland(), e.getBlueprintBundle());
    onIslandCreate(e.getIsland());
  }

  @EventHandler(ignoreCancelled = true)
  public void onIslandReseted(IslandResettedEvent e) {
    onIslandDelete(e.getIsland());
    onIslandCreate(e.getIsland());
  }

  // HashMap<Island, BlueprintBundle> blueprintBundle = new HashMap<Island, BlueprintBundle>();
  @EventHandler(ignoreCancelled = true)
  public void onIslandReseted(IslandResetEvent e) {

    // blueprintBundle.put(e.getIsland(), e.getBlueprintBundle());
  }

  public void onIslandCreate(Island island) {

    Location min =
        new Location(island.getWorld(), island.getMinProtectedX(), 0, island.getMinProtectedZ());
    Location max =
        new Location(island.getWorld(), island.getMaxProtectedX(), 0, island.getMaxProtectedZ());

    final org.bukkit.World world = island.getWorld();
    final java.util.HashMap<Long, org.bukkit.ChunkSnapshot> snapshots = new java.util.HashMap<>();
    int minChunkX = Math.min(min.getBlockX(), max.getBlockX()) >> 4;
    int maxChunkX = Math.max(min.getBlockX(), max.getBlockX()) >> 4;
    int minChunkZ = Math.min(min.getBlockZ(), max.getBlockZ()) >> 4;
    int maxChunkZ = Math.max(min.getBlockZ(), max.getBlockZ()) >> 4;

    for (int cx = minChunkX; cx <= maxChunkX; cx++) {
      for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
        if (world.isChunkLoaded(cx, cz)) {
          snapshots.put(
              ((long) cx << 32) | (cz & 0xFFFFFFFFL),
              world.getChunkAt(cx, cz).getChunkSnapshot(true, false, false));
        }
      }
    }

    DimensionsScheduler.runAsync(
        main.getPlugin(),
        new Runnable() {

          @Override
          public void run() {
            for (int x = (int) Math.max(max.getBlockX(), min.getBlockX());
                x >= (int) Math.min(min.getBlockX(), max.getBlockX());
                x--) {
              for (int y = 200; y >= 50; y--) {
                for (int z = (int) Math.max(max.getBlockZ(), min.getBlockZ());
                    z >= (int) Math.min(min.getBlockZ(), max.getBlockZ());
                    z--) {
                  int cx = x >> 4;
                  int cz = z >> 4;
                  long chunkKey = ((long) cx << 32) | (cz & 0xFFFFFFFFL);
                  org.bukkit.ChunkSnapshot snapshot = snapshots.get(chunkKey);
                  if (snapshot == null) continue;
                  if (snapshot.getBlockType(x & 15, y, z & 15) != Material.OAK_WALL_SIGN) continue;

                  final int fx = x;
                  final int fy = y;
                  final int fz = z;

                  DimensionsScheduler.run(
                      main.getPlugin(),
                      new Location(world, fx, fy, fz),
                      new Runnable() {

                        @Override
                        public void run() {
                          Block block = new Location(world, fx, fy, fz).getBlock();
                          if (block.getType() != Material.OAK_WALL_SIGN) return;

                          Sign signData = (Sign) block.getState();

                          if (!DimensionsUtils.getSignLine(signData, Side.FRONT, 0)
                              .contentEquals("[DIMENSIONS]")) return;

                          block.setType(Material.AIR);
                          CustomPortal portal =
                              Dimensions.getCustomPortalManager()
                                  .getCustomPortal(
                                      DimensionsUtils.getSignLine(signData, Side.FRONT, 1));
                          if (portal != null) {
                            PortalGeometry temp =
                                PortalGeometry.getPortalGeometry(portal)
                                    .getPortal(portal, block.getLocation());
                            if (temp != null)
                              Dimensions.getCompletePortalManager()
                                  .createNew(
                                      new CompletePortal(portal, block.getWorld(), temp),
                                      null,
                                      CustomPortalIgniteCause.PLUGIN,
                                      null);
                          }
                        }
                      });
                }
              }
            }
          }
        });
  }

  @EventHandler(ignoreCancelled = true)
  public void onIslandDeleted(IslandDeleteEvent e) {
    onIslandDelete(e.getIsland());
  }

  public void onIslandDelete(Island island) {
    ArrayList<CompletePortal> toRemove =
        new ArrayList<>(
            Dimensions.getCompletePortalManager()
                .getNearestPortals(island.getCenter(), island.getRange()));
    for (CompletePortal complete : toRemove) {
      Dimensions.getCompletePortalManager()
          .removePortal(complete, CustomPortalDestroyCause.PLUGIN, null);
    }
  }
}
