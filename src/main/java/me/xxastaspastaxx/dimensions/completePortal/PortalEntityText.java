package me.xxastaspastaxx.dimensions.completePortal;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.xxastaspastaxx.dimensions.utils;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * The PortalEntity that sends players packets of spawning block displays with textures of blocks
 */
public class PortalEntityText extends PortalEntity {

  private int portalEntityId;

  private WrapperPlayServerSpawnEntity spawnPacket;
  private WrapperPlayServerEntityMetadata metaPacket;
  private WrapperPlayServerDestroyEntities destroyPacket;

  /**
   * Construct the PortalEntity and create all the packets to summon, retexture, and destroy the
   * block display
   *
   * @param location the location to summon the entity
   * @param insideSprite the text component representing the texture of the block display (using a
   *     sprite)
   */
  public PortalEntityText(Location location, Component insideSprite) {
    super(location);
    portalEntityId = (int) (Math.random() * Integer.MAX_VALUE);

    spawnPacket =
        new WrapperPlayServerSpawnEntity(
            portalEntityId,
            Optional.of(UUID.randomUUID()),
            EntityTypes.TEXT_DISPLAY,
            new Vector3d(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
            0f, // pitch
            0f, // yaw
            0f, // headYaw
            0, // data
            Optional.empty() // velocity
            );

    List<EntityData<?>> metadataList = new ArrayList<>();
    // index 23 is the index of the "Text" entity data of the text display entity
    metadataList.add(new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, insideSprite));
    // index 25 is the index of the "Background Color" entity data of the text display entity, set
    // it to 0 to make the background invisible
    metadataList.add(new EntityData<>(25, EntityDataTypes.INT, 0));
    // index 16 is the index of the "Brightness" entity data of the display entity, set it to
    // 15728880 to make it fullbright on both sky and block light
    // blockLight << 4 | skyLight << 20
    metadataList.add(new EntityData<>(16, EntityDataTypes.INT, utils.packBrightness(15, 15)));
    // index 12 is the index of the "Scale" entity data of the display entity, set it to 5 to make
    // it the size of a block
    metadataList.add(
        new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f(5.0f, 5.0f, 5.0f)));
    // index 11 is the index of the "Translation" entity data of the display entity, text displays
    // need a bit of translation to be centered on the block
    metadataList.add(
        new EntityData<>(11, EntityDataTypes.VECTOR3F, new Vector3f(-0.125f, 0.0f, 0.75f)));

    metaPacket = new WrapperPlayServerEntityMetadata(portalEntityId, metadataList);

    destroyPacket = new WrapperPlayServerDestroyEntities(portalEntityId);
  }

  // Check if the player's version is unsupported (older than 1.21.9) and the entity is a fallback
  private boolean playerDoesntSupportSprites(Player p) {
    return p.getProtocolVersion() < 773;
  }

  /** Send the spawn packets to the player */
  public void summon(Player p) {
    if (playerDoesntSupportSprites(p)) return;
    PacketEvents.getAPI().getPlayerManager().sendPacket(p, spawnPacket);
    PacketEvents.getAPI().getPlayerManager().sendPacket(p, metaPacket);
  }

  /** Send the destroy packets to the player */
  public void destroy(Player p) {
    if (playerDoesntSupportSprites(p)) return;
    PacketEvents.getAPI().getPlayerManager().sendPacket(p, destroyPacket);

    p.sendBlockChange(getLocation(), getLocation().getBlock().getBlockData());
  }

  /** Send the destroy packets to all players */
  public void destroyBroadcast() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (playerDoesntSupportSprites(p)) return;
      PacketEvents.getAPI().getPlayerManager().sendPacket(p, destroyPacket);
      p.sendBlockChange(getLocation(), getLocation().getBlock().getBlockData());
    }
  }
}
