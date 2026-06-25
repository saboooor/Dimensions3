package me.xxastaspastaxx.dimensions.completePortal;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsScheduler;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/** Class containing the info of a built portal */
public class CompletePortal {

  CustomPortal customPortal;

  PortalGeometry portalGeometry;
  World world;
  private int chunkX;
  private int chunkZ;

  private ScheduledTask particlesTask;
  private ScheduledTask entitiesTask;
  private ScheduledTask updateTask;
  private ScheduledTask ambientSoundTask;

  // We store the last linked world in case the plugin needs to return the player to the world he
  // came from but for some reason the portal is broken
  CompletePortal linkedPortal;
  World lastLinkedWorld;

  // The fake falling block entities are stored here in order to spawn.despawn them
  private ArrayList<PortalEntity> spawnedEntities = new ArrayList<PortalEntity>();

  // We keep a list of players that have been teleported to the portal in order to not spam teleport
  // them around
  ArrayList<Entity> hold = new ArrayList<Entity>();
  HashMap<Entity, ScheduledTask> queue = new HashMap<Entity, ScheduledTask>();

  HashMap<String, Object> tags = new HashMap<String, Object>();

  boolean brokenPortal = false;

  /**
   * Construct the CompletePortal If <b>portalGeometry</b> is not null, create <a
   * href="../PortalEntity.html">PortalEntities</a>
   *
   * @param customPortal the customPortal that is built
   * @param world the world that is built
   * @param portalGeometry PortalGeometry of the portal
   */
  public CompletePortal(CustomPortal customPortal, World world, PortalGeometry portalGeometry) {
    this.customPortal = customPortal;
    this.world = world;
    this.portalGeometry = portalGeometry;

    if (portalGeometry == null) return;

    Location center = getCenter();
    chunkX = center.getBlockX() >> 4;
    chunkZ = center.getBlockZ() >> 4;

    Vector min = portalGeometry.getInsideMin();
    Vector max = portalGeometry.getInsideMax();
    boolean zAxis = portalGeometry.iszAxis();

    for (double y = min.getY(); y <= max.getY(); y++) {
      for (double side = zAxis ? min.getZ() : min.getX();
          side <= (zAxis ? max.getZ() : max.getX());
          side++) {
        double xLocation = zAxis ? min.getX() : side;
        double zLocation = zAxis ? side : min.getZ();

        if (customPortal.getInsideSprite() != null) {
          BlockFace facing = zAxis ? BlockFace.EAST : BlockFace.SOUTH;

          PortalEntity frontEntity =
              new PortalEntityText(
                  new Location(world, xLocation, y, zLocation), customPortal, facing);
          spawnedEntities.add(frontEntity);
          PortalEntity backEntity =
              new PortalEntityText(
                  new Location(world, xLocation, y, zLocation),
                  customPortal,
                  facing.getOppositeFace());
          spawnedEntities.add(backEntity);
        }

        if (customPortal.getInsideMaterial() != null) {
          PortalEntity entity;
          if (customPortal.getInsideMaterial().isSolid()
              || customPortal.getInsideMaterial() == Material.NETHER_PORTAL) {
            entity =
                new PortalEntitySand(
                    new Location(world, xLocation, y, zLocation),
                    customPortal.getCombinedID(zAxis),
                    customPortal.getInsideSprite() != null);
          } else {
            entity =
                new PortalEntitySolid(
                    new Location(world, xLocation, y, zLocation),
                    customPortal.getInsideBlockData(zAxis),
                    customPortal.getInsideSprite() != null);
          }
          spawnedEntities.add(entity);
        }
      }
    }
  }

  /**
   * Construct the CompletePortal If <b>portalGeometry</b> is not null, create <a
   * href="../PortalEntity.html">PortalEntities</a> If <b>linked</b> is not null, links the portals
   *
   * @param customPortal the customPortal that is built
   * @param world the world that is built
   * @param portalGeometry PortalGeometry of the portal
   * @param linked The portal to be linked with
   */
  public CompletePortal(
      CustomPortal customPortal,
      World world,
      PortalGeometry portalGeometry,
      CompletePortal linked) {
    this(customPortal, world, portalGeometry);

    if (linked == null) return;
    setLinkedPortal(linked);
    linked.setLinkedPortal(this);
  }

  /**
   * Get the CustomPortal
   *
   * @return the CustomPortal
   */
  public CustomPortal getCustomPortal() {
    return customPortal;
  }

  /**
   * Get the world of the portal
   *
   * @return the world of the portal
   */
  public World getWorld() {
    return world;
  }

  /**
   * Get the portal geometry of the portal
   *
   * @return the portal geometry of the portal
   */
  public PortalGeometry getPortalGeometry() {
    return portalGeometry;
  }

  /**
   * Get the portal that is linked with
   *
   * @return null if there is no linked portal or the linked portal
   */
  public CompletePortal getLinkedPortal() {
    return linkedPortal;
  }

  /** Unlink the portal */
  public void unlinkPortal() {
    linkedPortal = null;
  }

  /**
   * Link to a portal
   *
   * @param complete the portal to link to
   */
  public void setLinkedPortal(CompletePortal complete) {
    linkedPortal = complete;
    lastLinkedWorld = complete.getWorld();
  }

  /**
   * Get the center of the portal
   *
   * @return the center of the portal
   */
  public Location getCenter() {
    return portalGeometry.getCenter().toLocation(world);
  }

  /**
   * Get the PortalEntity list
   *
   * @return the PortalEntity list
   */
  public ArrayList<PortalEntity> getPortalEntities() {
    return spawnedEntities;
  }

  private ArrayList<Entity> savedEntities = new ArrayList<Entity>();

  /** Check for nearby entities to teleport */
  public void updatePortal() {
    if (!isActive() || brokenPortal) return;
    if (!customPortal.isEnableEntitiesTeleport()) return;

    savedEntities.addAll(
        world.getNearbyEntities(
            portalGeometry.getBoundingBox(),
            new Predicate<Entity>() {
              @Override
              public boolean test(Entity t) {
                if (savedEntities.contains(t) || t instanceof Player || hold.contains(t)) {
                  return false;
                }
                if (t instanceof org.bukkit.entity.Mob && !customPortal.isEnableMobsTeleport()) {
                  return false;
                }
                return true;
              }
            }));

    ArrayList<Entity> toRemove = new ArrayList<Entity>();
    savedEntities.stream()
        .filter(
            en ->
                !isInsidePortal(en.getLocation(), false, false)
                    && (!(en instanceof LivingEntity)
                        || !isInsidePortal(((LivingEntity) en).getEyeLocation(), false, false)))
        .forEach(en -> toRemove.add(en));

    toRemove.forEach(
        en -> {
          removeFromHold(en);
          savedEntities.remove(en);
        });

    savedEntities.forEach(
        en -> {
          if (!hasInHold(en)) {
            handleEntity(en);
          }
        });
  }

  /**
   * Start the teleport countdown for the entity if they are not in hold. The delay for Creative and
   * Spectator players is 0 Call the CustomPortalUseEvent and if its not cancelled the teleport the
   * entity
   *
   * @param en the entity to teleport
   * @see CustomPortalUseEvent
   */
  public void handleEntity(Entity en) {
    CompletePortalTeleporter.handleEntity(this, en);
  }

  /**
   * Check if the location is inside the portal
   *
   * @param loc location to check
   * @param outside true to count the frame of the portal
   * @param corner true to count the corners of the portal
   * @return true if the location is inside the portal
   */
  public boolean isInsidePortal(Location loc, boolean outside, boolean corner) {
    return loc.getWorld().equals(world) && portalGeometry.isInside(loc, outside, corner);
  }

  /**
   * Calculate the teleport destination for the portal Get the destination world if its not being
   * overriden Fix the world ratio using the world sizes Fix the height ratio using the world
   * min/max heights
   *
   * @param overrideLocation override the location with this
   * @param overrideWorld override the world with this
   * @return the destination location to try and build an exit portal to use
   */
  public Location getDestinationLocation(Location overrideLocation, World overrideWorld) {
    return CompletePortalTeleporter.getDestinationLocation(this, overrideLocation, overrideWorld);
  }

  /**
   * Get the destination portal to use
   *
   * @param buildNewPortal true to build an exit portal
   * @param overrideLocation location to override
   * @param overrideWorld world to override
   * @return the portal to use
   */
  public CompletePortal getDestinationPortal(
      boolean buildNewPortal, Location overrideLocation, World overrideWorld) {
    return CompletePortalTeleporter.getDestinationPortal(
        this, buildNewPortal, overrideLocation, overrideWorld);
  }

  /**
   * Calculate the world ratio from the world sizes
   *
   * @param destinationWorld world to get the ratio from
   * @return the world ratio
   */
  public double getWorldRatio(World destinationWorld) {
    return CompletePortalTeleporter.getWorldRatio(this, destinationWorld);
  }

  /**
   * Set the block or summon the falling block entities for all the PortalEntities
   *
   * @param p null to broadcast the packets or the player to send the packets to
   */
  public void fill(Player p) {
    if (p == null) {
      Runnable fillRunnable =
          () -> {
            if (DimensionsSettings.enablePortalLighting && customPortal.getLightLevel() > 0) {
              int level = customPortal.getLightLevel();
              for (PortalEntity en : spawnedEntities) {
                DimensionsUtils.setLight(en.getLocation(), level);
              }
            }

            if (customPortal.canSpawnEntities()) {
              DimensionsScheduler.cancel(entitiesTask);
              Location center = getCenter();
              entitiesTask =
                  DimensionsScheduler.runAtFixedRate(
                      Dimensions.getInstance(),
                      center,
                      () -> {
                        if (!isActive()) return;

                        EntityType type = customPortal.getNextSpawn();
                        if (type == null) return;

                        Location spawnLoc = getCenter().clone();
                        spawnLoc.setY(portalGeometry.getInsideMin().getY());

                        Entity en = world.spawnEntity(spawnLoc, type);
                        pushToHold(en);
                      },
                      customPortal.getSpawnDelay(),
                      customPortal.getSpawnDelay());
            }

            if (DimensionsSettings.enableEntitiesTeleport
                || customPortal.isEnableEntitiesTeleport()) {
              DimensionsScheduler.cancel(updateTask);
              Location center = getCenter();
              updateTask =
                  DimensionsScheduler.runAtFixedRate(
                      Dimensions.getInstance(),
                      center,
                      () -> updatePortal(),
                      1,
                      DimensionsSettings.updateEveryTick);
            }

            DimensionsScheduler.cancel(ambientSoundTask);
            Object ambientSoundOpt =
                me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
                    customPortal, "ambientSound");
            boolean playAmbient = true;
            if (ambientSoundOpt instanceof String
                && ((String) ambientSoundOpt).equalsIgnoreCase("none")) {
              playAmbient = false;
            }
            if (playAmbient) {
              Location center = getCenter();
              ambientSoundTask =
                  DimensionsScheduler.runAtFixedRate(
                      Dimensions.getInstance(),
                      center,
                      () -> {
                        if (!isActive()) return;
                        DimensionsUtils.playPortalSound(
                            center,
                            ambientSoundOpt,
                            org.bukkit.Sound.BLOCK_PORTAL_AMBIENT,
                            1.0f,
                            1.0f);
                      },
                      80,
                      80);
            }

            if (getTag("hidePortalInside") != null) return;

            DimensionsScheduler.cancel(particlesTask);
            if (customPortal.isEnableParticles()) {
              Location center = getCenter();
              particlesTask =
                  DimensionsScheduler.runAtFixedRate(
                      Dimensions.getInstance(),
                      center,
                      () -> {
                        if (!isActive() || getTag("hidePortalParticles") != null) return;
                        for (PortalEntity en : spawnedEntities) {
                          en.emitParticles(customPortal.getParticlesColor());
                        }
                      },
                      20,
                      20);
            }

            for (Entity player :
                world.getNearbyEntities(
                    getCenter(),
                    16 * Bukkit.getViewDistance(),
                    255,
                    16 * Bukkit.getViewDistance(),
                    (player) -> player instanceof Player)) {
              fill((Player) player);
            }
          };

      if (Bukkit.isOwnedByCurrentRegion(getCenter())) {
        fillRunnable.run();
      } else {
        DimensionsScheduler.run(Dimensions.getInstance(), getCenter(), fillRunnable);
      }
      return;
    }

    Runnable fillPlayerRunnable =
        () -> {
          if (getTag("hidePortalInside") != null) return;

          for (PortalEntity en : spawnedEntities) {
            en.destroy(p);
            DimensionsScheduler.runDelayed(
                Dimensions.getInstance(),
                en.getLocation(),
                () -> en.summon(p),
                DimensionsSettings.portalInsideDelay);
          }
        };

    if (Bukkit.isOwnedByCurrentRegion(getCenter())) {
      fillPlayerRunnable.run();
    } else {
      DimensionsScheduler.run(Dimensions.getInstance(), getCenter(), fillPlayerRunnable);
    }
  }

  /**
   * Despawn the entities or change the block to air inside the portal
   *
   * @param p null to stop the running tasks or the player to play the destroy packet
   */
  public void destroy(Player p) {
    Runnable destroyRunnable =
        () -> {
          if (p == null) {
            DimensionsScheduler.cancel(particlesTask);
            DimensionsScheduler.cancel(entitiesTask);
            DimensionsScheduler.cancel(updateTask);
            DimensionsScheduler.cancel(ambientSoundTask);
            particlesTask = null;
            entitiesTask = null;
            updateTask = null;
            ambientSoundTask = null;

            brokenPortal = true;

            if (DimensionsSettings.enablePortalLighting && customPortal.getLightLevel() > 0) {
              for (PortalEntity en : spawnedEntities) {
                DimensionsUtils.setLight(en.getLocation(), 0);
              }
            }
            savedEntities.clear();
          }

          Object soundOpt =
              me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
                  customPortal, "breakSound");
          DimensionsUtils.playPortalSound(
              getCenter(), soundOpt, customPortal.getBreakSound(), 1.0f, 8.0f);

          Particle blockCrackParticle =
              DimensionsUtils.getParticle("BLOCK", "BLOCK_CRUMBLE", "BLOCK_CRACK");
          for (PortalEntity en : spawnedEntities) {
            world.spawnParticle(
                blockCrackParticle,
                en.getLocation(),
                10,
                // fallback to outside material if the inside material is made of text
                customPortal.getInsideBlockData(false) == null
                    ? customPortal.getOutsideMaterial().createBlockData()
                    : customPortal.getInsideBlockData(false));
            if (p == null) en.destroyBroadcast();
            else en.destroy(p);
          }
        };

    if (Bukkit.isOwnedByCurrentRegion(getCenter())) {
      destroyRunnable.run();
    } else {
      DimensionsScheduler.run(Dimensions.getInstance(), getCenter(), destroyRunnable);
    }
  }

  /**
   * Check if the portal is inside the given chunk
   *
   * @param world2 the world of the chunk
   * @param x the X of the chunk
   * @param z the Z of the chunk
   * @return true if all match
   */
  public boolean isInChunk(World world2, int x, int z) {

    return world.equals(world2) && chunkX == x && chunkZ == z;
  }

  /**
   * Add the entity to hold so it won't be teleported
   *
   * @param en the entity to add
   */
  public void pushToHold(Entity en) {
    if (en == null) return;
    hold.add(en);
  }

  /**
   * Check if the entity is on hold or in the queue to teleport
   *
   * @param en the player to check
   * @return true if the player is in hold or queue
   */
  public boolean hasInHold(Entity en) {
    if (en == null) return false;
    return hold.contains(en) || queue.containsKey(en);
  }

  /**
   * Remove the entity from hold and cancel the teleport task
   *
   * @param en the entity to remove
   */
  public void removeFromHold(Entity en) {
    if (en == null) return;
    hold.remove(en);
    try {
      ScheduledTask t = queue.remove(en);
      if (t != null) t.cancel();
    } catch (Exception e) {

    }
  }

  /**
   * Set a portal tag (tags are stored even after restart)
   *
   * @param key the key of the tag
   * @param value the value of the tag
   */
  public void setTag(String key, Object value) {
    if (value == null) tags.remove(key);
    else tags.put(key, value);
  }

  /**
   * Get the value of the tag
   *
   * @param key
   * @return the value
   */
  public Object getTag(String key) {
    return tags.get(key);
  }

  /**
   * Get the portal tags
   *
   * @return the portal tags
   */
  public HashMap<String, Object> getTags() {
    return new HashMap<>(tags);
  }

  /**
   * Override the portal tags
   *
   * @param tags
   */
  public void setTags(HashMap<String, Object> tags) {
    this.tags.clear();
    if (tags != null) {
      this.tags.putAll(tags);
    }
  }

  /**
   * Check if the chunk the portal is in is loaded
   *
   * @return true if chunk is loaded
   */
  public boolean isActive() {
    return world.isChunkLoaded(chunkX, chunkZ);
  }
}
