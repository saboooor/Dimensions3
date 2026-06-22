package me.xxastaspastaxx.dimensions.completePortal;

import io.github.retrooper.packetevents.util.viaversion.ViaVersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

/**
 * PortalEntity class for blocks that are not solid and dont have to be spawned as falling blocks
 */
public class PortalEntitySolid extends PortalEntity {

  private BlockData blockdata;
  private boolean isFallback;

  /**
   * Construct PortalEntity with the blockData to place
   *
   * @param location the location of the block
   * @param blockData the block data to place
   * @param isFallback whether the entity is a fallback for older clients
   */
  public PortalEntitySolid(Location location, BlockData blockData, boolean isFallback) {
    super(location);
    this.blockdata = blockData;
    this.isFallback = isFallback;
  }

  // Check if the player's version is unsupported (older than 1.21.9) and the entity is a fallback
  private boolean playerSupportsSprites(Player p) {
    return isFallback && ViaVersionUtil.getProtocolVersion(p) >= 773;
  }

  /** Send block change (block data) to the player */
  public void summon(Player p) {
    if (playerSupportsSprites(p)) return;
    p.sendBlockChange(getLocation(), blockdata);
  }

  /** Send block change (air) to the player */
  public void destroy(Player p) {
    if (playerSupportsSprites(p)) return;
    p.sendBlockChange(getLocation(), Material.AIR.createBlockData());
  }

  /** Send block change (air) to all players */
  public void destroyBroadcast() {
    getLocation().getBlock().setType(Material.AIR);
    Bukkit.getOnlinePlayers()
        .forEach(
            p -> {
              if (playerSupportsSprites(p)) return;
              p.sendBlockChange(getLocation(), Material.AIR.createBlockData());
            });
  }
}
