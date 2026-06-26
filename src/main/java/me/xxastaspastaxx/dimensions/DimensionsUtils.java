package me.xxastaspastaxx.dimensions;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Light;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import io.github.retrooper.packetevents.util.GeyserUtil;
import io.github.retrooper.packetevents.util.viaversion.ViaVersionUtil;

/** Contains methods that are commonly used */
public class DimensionsUtils {

  /**
   * Check if the block is air
   *
   * @param block block that is being checked
   * @return true if the type of the block is AIR, CAVE_AIR, or LIGHT (invisible light source block
   *     placed by portals)
   */
  public static boolean isAir(Block block) {
    return block.getType() == Material.AIR
        || block.getType() == Material.CAVE_AIR
        || block.getType() == Material.LIGHT;
  }

  /**
   * Return a random integer in the given range
   *
   * @param min inclusive
   * @param max inclusive
   * @return a random integer
   */
  public static int getRandom(int min, int max) {
    return (int) (Math.random() * ((max - min) + 1)) + min;
  }

  private static final Orientable netherPortalEffect =
      (Orientable) Material.NETHER_PORTAL.createBlockData();

  /**
   * Get the nether portal BlockData for the axis
   *
   * @param zAxis if the BlockData must have zAxis
   * @return NETHER_PORTAL BlockData facing the set Axis
   */
  public static BlockData getNetherPortalEffect(boolean zAxis) {
    netherPortalEffect.setAxis(zAxis ? Axis.Z : Axis.X);
    return netherPortalEffect;
  }

  /**
   * Parse a location from a string
   *
   * @param str the string containing the location
   * @param delim delimiter seperating the data
   * @return the parsed location
   */
  public static Location parseLocationFromString(String str, String delim) {
    String[] spl = str.split(delim);
    return new Location(
        Bukkit.getWorld(spl[0]),
        Double.parseDouble(spl[1]),
        Double.parseDouble(spl[2]),
        Double.parseDouble(spl[3]));
  }

  /**
   * Convert a location to string
   *
   * @param loc the location to be stringified
   * @param delim the delimeter to seperate the data
   * @return the stringified location
   */
  public static String locationToString(Location loc, String delim) {
    return loc.getWorld().getName() + delim + loc.getX() + delim + loc.getY() + delim + loc.getZ();
  }

  /**
   * Check if string can be parsed as integer
   *
   * @param string string to check
   * @return true if string is integer
   */
  public static boolean isInt(String string) {
    try {
      Integer.parseInt(string);
      return true;
    } catch (Exception e) {

    }
    return false;
  }

  private static BlockFace[] radial = {
    BlockFace.NORTH,
    BlockFace.NORTH_EAST,
    BlockFace.EAST,
    BlockFace.SOUTH_EAST,
    BlockFace.SOUTH,
    BlockFace.SOUTH_WEST,
    BlockFace.WEST,
    BlockFace.NORTH_WEST
  };

  /**
   * Get blockface from location yaw
   *
   * @param yaw the yaw of the location
   * @return BlockFace from yaw
   */
  public static BlockFace yawToFace(float yaw) {
    return radial[Math.round(yaw / 45f) & 0x7];
  }

  /**
   * Check if the given BlockFace is placed along the Z Axis
   *
   * @param face BlockFace to check
   * @return true if the BlockFace is WEST or EAST
   */
  public static boolean isBlockFacezAxis(BlockFace face) {
    return face == BlockFace.WEST || face == BlockFace.EAST;
  }

  @SuppressWarnings("removal")
  public static void cloneEntity(Entity en, Entity newEn) {
    newEn.customName(en.name());
    newEn.setCustomNameVisible(en.isCustomNameVisible());
    newEn.setFallDistance(en.getFallDistance());
    newEn.setFireTicks(en.getFireTicks());
    newEn.setFreezeTicks(en.getFreezeTicks());
    newEn.setGlowing(en.isGlowing());
    newEn.setGravity(en.hasGravity());
    newEn.setInvulnerable(en.isInvulnerable());
    newEn.setLastDamageCause(en.getLastDamageCause());
    newEn.setOp(en.isOp());
    newEn.setPersistent(en.isPersistent());
    newEn.setPortalCooldown(en.getPortalCooldown());
    newEn.setSilent(en.isSilent());
    newEn.setTicksLived(en.getTicksLived());
    newEn.setVelocity(en.getVelocity());
    newEn.setVisualFire(en.isVisualFire());
    en.getPassengers().forEach(passenger -> newEn.addPassenger(passenger));
    en.getScoreboardTags().forEach(tag -> newEn.addScoreboardTag(tag));
    if (en.getVehicle() != null) en.getVehicle().addPassenger(newEn);
    en.getPersistentDataContainer().copyTo(newEn.getPersistentDataContainer(), true);

    if (en instanceof LivingEntity && newEn instanceof LivingEntity) {
      LivingEntity newEn2 = (LivingEntity) newEn;
      LivingEntity en2 = (LivingEntity) en;

      en2.getActivePotionEffects().forEach(ef -> newEn2.addPotionEffect(ef));
      newEn2.setAbsorptionAmount(en2.getAbsorptionAmount());
      newEn2.setAI(en2.hasAI());
      newEn2.setArrowCooldown(en2.getArrowCooldown());
      newEn2.setArrowsInBody(en2.getArrowsInBody());
      newEn2.setCanPickupItems(en2.getCanPickupItems());
      newEn2.setCollidable(en2.isCollidable());
      newEn2.setGliding(en2.isGliding());
      Attribute maxHealthAttribute =
          Attribute.valueOf("MAX_HEALTH") == null
              ? Attribute.valueOf("GENERIC_MAX_HEALTH")
              : Attribute.valueOf("MAX_HEALTH");
      newEn2.setHealth(
          newEn2.getAttribute(maxHealthAttribute).getValue()
              / (en2.getAttribute(maxHealthAttribute).getValue() / en2.getHealth()));

      for (Attribute at : Attribute.values()) {
        AttributeInstance enAt = en2.getAttribute(at);
        AttributeInstance newEnAt = newEn2.getAttribute(at);
        if (enAt == null || newEnAt == null) continue;

        newEnAt.setBaseValue(enAt.getBaseValue());
        enAt.getModifiers().forEach(m -> newEnAt.addModifier(m));
      }

      if (en2.getEquipment() != null && newEn2.getEquipment() != null) {
        newEn2.getEquipment().setArmorContents(en2.getEquipment().getArmorContents());
        newEn2.getEquipment().setItemInMainHand(en2.getEquipment().getItemInMainHand());
        newEn2.getEquipment().setItemInOffHand(en2.getEquipment().getItemInOffHand());
        newEn2.getEquipment().setHelmetDropChance(en2.getEquipment().getHelmetDropChance());
        newEn2.getEquipment().setChestplateDropChance(en2.getEquipment().getChestplateDropChance());
        newEn2.getEquipment().setLeggingsDropChance(en2.getEquipment().getLeggingsDropChance());
        newEn2.getEquipment().setBootsDropChance(en2.getEquipment().getBootsDropChance());
        newEn2
            .getEquipment()
            .setItemInMainHandDropChance(en2.getEquipment().getItemInMainHandDropChance());
        newEn2
            .getEquipment()
            .setItemInOffHandDropChance(en2.getEquipment().getItemInOffHandDropChance());
      }
    }
  }

  /**
   * Set or remove a light block at the given location with the specified level.
   *
   * @param loc the location to place/remove light
   * @param level the light level (0-15). If 0 or less, the light block is removed.
   */
  public static void setLight(Location loc, int level) {
    Block block = loc.getBlock();
    if (level <= 0) {
      if (block.getType() == Material.LIGHT) {
        block.setType(Material.AIR);
      }
    } else {
      if (isAir(block)) {
        Light lightData = (Light) Material.LIGHT.createBlockData();
        lightData.setLevel(level);
        block.setBlockData(lightData);
      }
    }
  }

  /**
   * Get the plain text representation of a sign line.
   *
   * @param sign the sign
   * @param side the sign side (FRONT or BACK)
   * @param index the line index (0-3)
   * @return the plain text string of the line
   */
  public static String getSignLine(Sign sign, Side side, int index) {
    return PlainTextComponentSerializer.plainText().serialize(sign.getSide(side).line(index));
  }

  /**
   * Safe utility to get a Sound from its string representation. Supports legacy uppercase enum
   * names (via reflection) and modern namespaced keys (via Registry).
   *
   * @param soundName the name or key of the sound
   * @return the resolved Sound, or Sound.BLOCK_GLASS_BREAK as a fallback
   */
  public static Sound getSound(String soundName) {
    if (soundName == null || soundName.trim().isEmpty()) {
      return Sound.BLOCK_GLASS_BREAK;
    }

    // 1. Try reflection for legacy uppercase names (e.g. BLOCK_GLASS_BREAK)
    try {
      java.lang.reflect.Field field = Sound.class.getField(soundName.toUpperCase());
      return (Sound) field.get(null);
    } catch (Exception ignored) {
    }

    // 2. Try Registry lookup for modern namespaced keys (e.g. block.glass.break)
    try {
      NamespacedKey key = NamespacedKey.fromString(soundName.toLowerCase());
      if (key != null) {
        Sound registrySound = Registry.SOUNDS.get(key);
        if (registrySound != null) {
          return registrySound;
        }
      }
    } catch (Exception ignored) {
    }

    // 3. Ultimate fallback to prevent crashes
    return Sound.BLOCK_GLASS_BREAK;
  }

  /**
   * Safe utility to get a Particle from a list of possible names (in order of preference). Prevents
   * IllegalArgumentException by catching it and trying the next fallback name.
   *
   * @param names one or more particle names to try
   * @return the resolved Particle, or a default fallback if none match
   */
  public static Particle getParticle(String... names) {
    for (String name : names) {
      try {
        return Particle.valueOf(name);
      } catch (IllegalArgumentException ignored) {
      }
    }
    // Ultimate fallback
    try {
      return Particle.valueOf("DUST");
    } catch (IllegalArgumentException ignored) {
      try {
        return Particle.valueOf("REDSTONE");
      } catch (IllegalArgumentException ignored2) {
        return null;
      }
    }
  }

  /**
   * Packs the block light and sky light values into a single integer.
   *
   * @param blockLight the block light value (0-15)
   * @param skyLight the sky light value (0-15)
   * @return the packed brightness value
   */
  public static int packBrightness(int lightLevel) {
    if (lightLevel < 0 || lightLevel > 15) {
      throw new IllegalArgumentException("lightLevel must be between 0 and 15");
    }

    return (lightLevel << 4) | (lightLevel << 20);
  }

  /**
   * Check if the player's version is unsupported (older than 1.21.9) and the entity is a fallback
   * Geyser also doesn't support display entities natively (todo: try to find geyser equivalent)
   * 
   * @param p the player to check
   * 
   */
  public static boolean playerSupportsSprites(Player p) {
    return ViaVersionUtil.getProtocolVersion(p) >= 773 && !GeyserUtil.isGeyserPlayer(p.getUniqueId());
  }

  /**
   * Play a portal sound at the given location, supporting custom sound options (which can be a
   * Bukkit Sound enum or a String).
   *
   * @param location the location to play the sound at
   * @param soundOption the custom sound option (e.g. from an addon)
   * @param fallbackSound the default sound to play if no option is configured
   * @param volume the volume of the sound
   * @param pitch the pitch of the sound
   */
  public static void playPortalSound(
      Location location, Object soundOption, Sound fallbackSound, float volume, float pitch) {
    if (location == null) return;
    org.bukkit.World world = location.getWorld();
    if (world == null) return;

    Object soundToPlay = soundOption != null ? soundOption : fallbackSound;
    if (soundToPlay == null) return;

    if (soundToPlay instanceof String) {
      String soundStr = (String) soundToPlay;
      if (soundStr.equalsIgnoreCase("none")) return;
      if (!soundStr.trim().isEmpty()) {
        Sound soundEnum = null;
        try {
          soundEnum = Sound.valueOf(soundStr.toUpperCase());
        } catch (IllegalArgumentException e) {
          // Not a valid enum name
        }
        if (soundEnum != null) {
          world.playSound(location, soundEnum, volume, pitch);
        } else {
          world.playSound(location, soundStr, volume, pitch);
        }
      }
    } else if (soundToPlay instanceof Sound) {
      world.playSound(location, (Sound) soundToPlay, volume, pitch);
    }
  }
}
