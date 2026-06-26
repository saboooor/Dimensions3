package me.xxastaspastaxx.dimensions.completePortal;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.viaversion.ViaVersionUtil;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import io.github.retrooper.packetevents.util.GeyserUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/** The PortalEntity that sends players packets of spawning falling sand with textures of blocks */
public class PortalEntitySand extends PortalEntity {

  private int fallingBlockId;
  private boolean isFallback;

  private WrapperPlayServerSpawnEntity spawnPacket;
  private WrapperPlayServerEntityTeleport teleportPacket;
  private WrapperPlayServerEntityMetadata metaPacket;
  private WrapperPlayServerDestroyEntities destroyPacket;

  /**
   * Construct the PortalEntity and create all the packets to summon, retexture, teleport and
   * destroy the falling block
   *
   * @param location the location to summon the entity
   * @param combinedID the combinedID of the texture
   * @param isFallback required for players with older versions that don't support display entities
   */
  public PortalEntitySand(Location location, int combinedID, boolean isFallback) {
    super(location);
    fallingBlockId = (int) (Math.random() * Integer.MAX_VALUE);
    this.isFallback = isFallback;

    spawnPacket =
        new WrapperPlayServerSpawnEntity(
            fallingBlockId,
            Optional.of(UUID.randomUUID()),
            EntityTypes.FALLING_BLOCK,
            new Vector3d(location.getX(), location.getY(), location.getZ()),
            0f, // pitch
            0f, // yaw
            0f, // headYaw
            combinedID, // data (combined block ID)
            Optional.empty() // velocity
            );

    List<EntityData<?>> metadataList = new ArrayList<>();
    metadataList.add(new EntityData<>(5, EntityDataTypes.BOOLEAN, true));
    metadataList.add(new EntityData<>(1, EntityDataTypes.INT, Integer.MAX_VALUE));

    metaPacket = new WrapperPlayServerEntityMetadata(fallingBlockId, metadataList);

    teleportPacket =
        new WrapperPlayServerEntityTeleport(
            fallingBlockId,
            new Vector3d(location.getX() + 0.5f, location.getY(), location.getZ() + 0.5f),
            0f, // yaw
            0f, // pitch
            false // onGround
            );

    destroyPacket = new WrapperPlayServerDestroyEntities(fallingBlockId);
  }

  /** Send the spawn packets to the player */
  public void summon(Player p) {
    if (isFallback && DimensionsUtils.playerSupportsSprites(p)) return;
    PacketEvents.getAPI().getPlayerManager().sendPacket(p, spawnPacket);
    PacketEvents.getAPI().getPlayerManager().sendPacket(p, teleportPacket);
    PacketEvents.getAPI().getPlayerManager().sendPacket(p, metaPacket);
  }

  /** Send the destroy packets to the player */
  public void destroy(Player p) {
    if (isFallback && DimensionsUtils.playerSupportsSprites(p)) return;
    PacketEvents.getAPI().getPlayerManager().sendPacket(p, destroyPacket);

    p.sendBlockChange(getLocation(), getLocation().getBlock().getBlockData());
  }

  /** Send the destroy packets to all players */
  public void destroyBroadcast() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (isFallback && DimensionsUtils.playerSupportsSprites(p)) return;
      PacketEvents.getAPI().getPlayerManager().sendPacket(p, destroyPacket);
      p.sendBlockChange(getLocation(), getLocation().getBlock().getBlockData());
    }
  }
}
