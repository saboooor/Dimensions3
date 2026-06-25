package me.xxastaspastaxx.dimensions.completePortal;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
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
   * @param customPortal the custom portal instance
   * @param facing the direction the portal is facing
   */
  public PortalEntityText(Location location, CustomPortal customPortal, BlockFace facing) {
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

    int backgroundColor = 0; // Default transparent
    byte textOpacity = -1; // Default fully opaque (255 as signed byte is -1)
    byte flags = 0;
    boolean hasFlags = false;
    boolean hasLineWidth = false;
    int lineWidth = 200;

    if (customPortal != null) {
      // 1. Background color & background opacity
      Object bgColObj =
          me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
              customPortal, "text_display_background_color");
      Object bgOpObj =
          me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
              customPortal, "text_display_background_opacity");

      int parsedColor = 0;
      if (bgColObj instanceof Integer) {
        parsedColor = (Integer) bgColObj;
      }

      int parsedOpacity = -1;
      if (bgOpObj instanceof Integer) {
        parsedOpacity = (Integer) bgOpObj;
      } else if (bgOpObj instanceof Double) {
        double opDouble = (Double) bgOpObj;
        if (opDouble <= 1.0) {
          parsedOpacity = (int) (opDouble * 255.0);
        } else {
          parsedOpacity = (int) opDouble;
        }
      } else if (bgOpObj instanceof Float) {
        float opFloat = (Float) bgOpObj;
        if (opFloat <= 1.0f) {
          parsedOpacity = (int) (opFloat * 255.0f);
        } else {
          parsedOpacity = (int) opFloat;
        }
      }

      if (bgColObj != null) {
        if (parsedOpacity != -1) {
          backgroundColor = (parsedColor & 0x00FFFFFF) | ((parsedOpacity & 0xFF) << 24);
        } else {
          if ((parsedColor & 0xFF000000) == 0 && parsedColor != 0) {
            backgroundColor = parsedColor | 0xFF000000;
          } else {
            backgroundColor = parsedColor;
          }
        }
      } else if (bgOpObj != null) {
        backgroundColor = ((parsedOpacity & 0xFF) << 24);
      }

      // 2. Sprite/Text Opacity
      Object textOpObj =
          me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
              customPortal, "text_display_sprite_opacity");
      if (textOpObj == null) {
        textOpObj =
            me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
                customPortal, "text_display_text_opacity");
      }
      if (textOpObj instanceof Integer) {
        int val = (Integer) textOpObj;
        textOpacity = (byte) (val <= 1 ? val * 255 : val);
      } else if (textOpObj instanceof Double) {
        double val = (Double) textOpObj;
        textOpacity = (byte) (val <= 1.0 ? (val * 255.0) : val);
      } else if (textOpObj instanceof Float) {
        float val = (Float) textOpObj;
        textOpacity = (byte) (val <= 1.0f ? (val * 255.0f) : val);
      }

      // 3. Flags
      Object shadowObj =
          me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
              customPortal, "text_display_shadow");
      Object seeThroughObj =
          me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
              customPortal, "text_display_see_through");
      Object defaultBgObj =
          me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
              customPortal, "text_display_default_background");
      Object alignmentObj =
          me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
              customPortal, "text_display_alignment");

      if (shadowObj != null
          || seeThroughObj != null
          || defaultBgObj != null
          || alignmentObj != null) {
        hasFlags = true;
        if (shadowObj instanceof Boolean && (Boolean) shadowObj) {
          flags |= 0x01;
        }
        if (seeThroughObj instanceof Boolean && (Boolean) seeThroughObj) {
          flags |= 0x02;
        }
        if (defaultBgObj instanceof Boolean && (Boolean) defaultBgObj) {
          flags |= 0x04;
        }
        if (alignmentObj instanceof String) {
          String align = ((String) alignmentObj).toUpperCase();
          if (align.equals("LEFT")) {
            flags |= 0x08;
          } else if (align.equals("RIGHT")) {
            flags |= 0x10;
          }
        }
      }

      // 4. Line width
      Object lwObj =
          me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
              customPortal, "text_display_line_width");
      if (lwObj instanceof Integer) {
        lineWidth = (Integer) lwObj;
        hasLineWidth = true;
      }
    }

    List<EntityData<?>> metadataList = new ArrayList<>();
    // index 23 is the index of "Text"
    metadataList.add(
        new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, customPortal.getInsideSprite()));
    // index 25 is the index of "Background Color"
    metadataList.add(new EntityData<>(25, EntityDataTypes.INT, backgroundColor));
    // index 16 is the index of "Brightness", set it to 15 to make the block display fully bright
    // todo: custom lightlevel
    metadataList.add(new EntityData<>(16, EntityDataTypes.INT, DimensionsUtils.packBrightness(10)));
    // index 12 is the index of "Scale", set it to 5 to make it the size of a block
    metadataList.add(
        new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f(5.0f, 5.0f, 5.0f)));

    // index 26 is the index of "Text Opacity"
    metadataList.add(new EntityData<>(26, EntityDataTypes.BYTE, textOpacity));

    // index 27 is the index of "Flags"
    if (hasFlags) {
      metadataList.add(new EntityData<>(27, EntityDataTypes.BYTE, flags));
    }

    // index 24 is the index of "Line Width"
    if (hasLineWidth) {
      metadataList.add(new EntityData<>(24, EntityDataTypes.INT, lineWidth));
    }

    // 1. Determine Translation and Rotation based on the facing direction
    Vector3f translation;
    Quaternion4f leftRotation;
    switch (facing) {
      case NORTH:
        translation = new Vector3f(0.625f, -0.25f, 0.34f);
        leftRotation = new Quaternion4f(0f, 1f, 0f, 0f); // 180 deg around Y
        break;
      case SOUTH:
        translation = new Vector3f(0.375f, -0.25f, 0.66f);
        leftRotation = new Quaternion4f(0f, 0f, 0f, 1f); // Identity (0 deg)
        break;
      case EAST:
        translation = new Vector3f(0.34f, -0.25f, 0.375f);
        leftRotation = new Quaternion4f(0f, -0.7071f, 0f, 0.7071f); // 270 deg around Y
        break;
      case WEST:
        translation = new Vector3f(0.66f, -0.25f, 0.625f);
        leftRotation = new Quaternion4f(0f, 0.7071f, 0f, 0.7071f); // 90 deg around Y
        break;
      case UP:
        translation = new Vector3f(0.375f, 0.66f, 1.25f);
        leftRotation = new Quaternion4f(-0.7071f, 0f, 0f, 0.7071f); // -90 deg around X
        break;
      case DOWN:
        translation = new Vector3f(0.375f, 0.34f, -0.25f);
        leftRotation = new Quaternion4f(0.7071f, 0f, 0f, 0.7071f); // 90 deg around X
        break;
      default: // Fallback/Default case
        translation = new Vector3f(0.375f, -0.25f, 0.66f);
        leftRotation = new Quaternion4f(0f, 0f, 0f, 1f); // Identity (0 deg)
        break;
    }

    // index 11 is the index of "Translation", text displays need a bit of translation to be
    // centered on the block
    metadataList.add(new EntityData<>(11, EntityDataTypes.VECTOR3F, translation));
    // index 13 is the index of "Rotation left", set it to 0, 0, 0 to make the block display face
    // the player
    metadataList.add(new EntityData<>(13, EntityDataTypes.QUATERNION, leftRotation));

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
