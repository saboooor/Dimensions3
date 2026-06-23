package me.xxastaspastaxx.dimensions.addons.pastedportals.skyblock;

import com.bgsoftware.superiorskyblock.api.events.IslandChunkResetEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import java.util.HashMap;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import me.xxastaspastaxx.dimensions.addons.pastedportals.DimensionsPastedPortalsAddon;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.completePortal.PortalGeometry;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalDestroyCause;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalIgniteCause;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PastedSuperiorSkyblock implements Listener {

  DimensionsPastedPortalsAddon main;

  HashMap<Island, Boolean> creating = new HashMap<Island, Boolean>();

  public PastedSuperiorSkyblock(DimensionsPastedPortalsAddon main) {
    this.main = main;

    Bukkit.getServer().getPluginManager().registerEvents(this, main.getPlugin());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onIslandCreated(IslandCreateEvent e) {
    creating.put(e.getIsland(), true);
  }

  @SuppressWarnings("deprecation")
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onIslandRegen(IslandChunkResetEvent e) {

    Chunk chunk = e.getWorld().getChunkAt(e.getChunkX(), e.getChunkZ());

    if (!e.getIsland().getAllChunks(Environment.NORMAL, true, false).contains(chunk)) return;

    Bukkit.getScheduler()
        .runTaskAsynchronously(
            main.getPlugin(),
            new Runnable() {

              @Override
              public void run() {
                while (!e.getIsland()
                        .getLoadedChunks(Environment.NORMAL, true, false)
                        .contains(chunk)
                    && creating.containsKey(e.getIsland())) {
                  try {
                    Thread.sleep(1000);
                  } catch (InterruptedException e1) {
                    e1.printStackTrace();
                  }
                }

                Bukkit.getScheduler()
                    .runTask(
                        main.getPlugin(),
                        new Runnable() {

                          @Override
                          public void run() {
                            if (!chunk.isLoaded()) return;

                            if (creating.get(e.getIsland())) {
                              final org.bukkit.ChunkSnapshot snapshot =
                                  chunk.getChunkSnapshot(true, false, false);

                              Bukkit.getScheduler()
                                  .runTaskAsynchronously(
                                      main.getPlugin(),
                                      new Runnable() {

                                        @Override
                                        public void run() {
                                          for (int X = 0; X < 16; X++) {
                                            for (int Z = 0; Z < 16; Z++) {
                                              for (int Y = 1; Y < 255; Y++) {
                                                if (snapshot.getBlockType(X, Y, Z)
                                                    != Material.OAK_WALL_SIGN) continue;

                                                final int fx = X;
                                                final int fy = Y;
                                                final int fz = Z;

                                                Bukkit.getScheduler()
                                                    .runTask(
                                                        main.getPlugin(),
                                                        new Runnable() {

                                                          @Override
                                                          public void run() {
                                                            Block block =
                                                                chunk.getBlock(fx, fy, fz);
                                                            if (block.getType()
                                                                != Material.OAK_WALL_SIGN) return;

                                                            Sign signData = (Sign) block.getState();
                                                            if (!DimensionsUtils.getSignLine(
                                                                    signData, Side.FRONT, 0)
                                                                .contentEquals("[DIMENSIONS]"))
                                                              return;

                                                            block.setType(Material.AIR);
                                                            CustomPortal portal =
                                                                Dimensions.getCustomPortalManager()
                                                                    .getCustomPortal(
                                                                        DimensionsUtils.getSignLine(
                                                                            signData,
                                                                            Side.FRONT,
                                                                            1));
                                                            if (portal != null) {
                                                              PortalGeometry temp =
                                                                  PortalGeometry.getPortalGeometry(
                                                                          portal)
                                                                      .getPortal(
                                                                          portal,
                                                                          block.getLocation());
                                                              if (temp != null)
                                                                Dimensions
                                                                    .getCompletePortalManager()
                                                                    .createNew(
                                                                        new CompletePortal(
                                                                            portal,
                                                                            block.getWorld(),
                                                                            temp),
                                                                        null,
                                                                        CustomPortalIgniteCause
                                                                            .PLUGIN,
                                                                        null);
                                                            }
                                                          }
                                                        });
                                              }
                                            }
                                          }
                                          creating.remove(e.getIsland());
                                        }
                                      });
                            } else {
                              // Highly optimized chunk-based portal lookup: no block loop required!
                              java.util.List<CompletePortal> toRemove = new java.util.ArrayList<>();
                              for (CompletePortal portal :
                                  Dimensions.getCompletePortalManager().getCompletePortals()) {
                                if (portal.getWorld().equals(chunk.getWorld())
                                    && (portal.getCenter().getBlockX() >> 4) == chunk.getX()
                                    && (portal.getCenter().getBlockZ() >> 4) == chunk.getZ()) {
                                  toRemove.add(portal);
                                }
                              }
                              for (CompletePortal portal : toRemove) {
                                Dimensions.getCompletePortalManager()
                                    .removePortal(portal, CustomPortalDestroyCause.PLUGIN, null);
                              }
                              creating.remove(e.getIsland());
                            }
                          }
                        });
              }
            });
  }

  @EventHandler(ignoreCancelled = true)
  public void onIslandDeleted(IslandDisbandEvent e) {
    creating.put(e.getIsland(), false);
  }
}
