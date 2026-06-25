package me.xxastaspastaxx.dimensions.completePortal;

import java.util.ArrayList;
import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsDebbuger;
import me.xxastaspastaxx.dimensions.DimensionsScheduler;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalIgniteCause;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import me.xxastaspastaxx.dimensions.settings.WorldConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

/**
 * Utility class holding teleportation and location-finding logic for CompletePortal. This keeps
 * CompletePortal.java clean and focused on portal state representation.
 */
class CompletePortalTeleporter {

  public static void handleEntity(CompletePortal portal, Entity en) {
    if (portal.linkedPortal == null && portal.customPortal.getWorld() == null) {
      if (DimensionsSettings.enableDebugLogging) {
        List<World> worlds = Bukkit.getWorlds();
        ArrayList<String> worldNames = new ArrayList<>();
        for (World w : worlds) {
          worldNames.add(w.getName());
        }
        Bukkit.getLogger()
            .info(
                "[Dimensions-Debug] Destination world \""
                    + portal.customPortal.getWorldName()
                    + "\" for portal at "
                    + portal.getCenter()
                    + " is missing or not loaded. Loaded worlds: ["
                    + String.join(", ", worldNames)
                    + "]");
      }
      if (en instanceof Player) {
        en.sendMessage(
            DimensionsSettings.getPrefix()
                .append(
                    net.kyori.adventure.text.Component.text(
                        "§cThe destination world is not loaded or does not exist.")));
      }
      return;
    }

    if (portal.hasInHold(en) || portal.brokenPortal) {
      if (DimensionsSettings.enableDebugLogging && en instanceof Player) {
        Bukkit.getLogger()
            .info(
                "[Dimensions-Debug] handleEntity early return: hasInHold="
                    + portal.hasInHold(en)
                    + ", broken="
                    + portal.brokenPortal
                    + " for player "
                    + en.getName());
      }
      return;
    }

    if (DimensionsSettings.enableDebugLogging && en instanceof Player) {
      Bukkit.getLogger()
          .info(
              "[Dimensions-Debug] handleEntity scheduling teleport for player "
                  + en.getName()
                  + " from portal at "
                  + portal.getCenter());
    }

    int delay = portal.customPortal.getTeleportDelay() * 20;
    if ((en instanceof Player)
        && (((Player) en).getGameMode() == GameMode.CREATIVE
            || ((Player) en).getGameMode() == GameMode.SPECTATOR)) delay = 0;

    // Use the entity scheduler so the task runs on the correct region in Folia
    portal.queue.put(
        en,
        DimensionsScheduler.runEntityDelayed(
            en,
            Dimensions.getInstance(),
            () -> {
              if (portal.brokenPortal) {
                if (DimensionsSettings.enableDebugLogging) {
                  Bukkit.getLogger()
                      .info("[Dimensions-Debug] Teleport task aborted: portal is broken");
                }
                return;
              }
              if (DimensionsSettings.enableDebugLogging && en instanceof Player) {
                Bukkit.getLogger()
                    .info("[Dimensions-Debug] Teleport task executing for player " + en.getName());
              }
              CustomPortalUseEvent useEvent =
                  new CustomPortalUseEvent(
                      portal, en, portal.getDestinationPortal(false, null, null));
              Bukkit.getPluginManager().callEvent(useEvent);

              if (useEvent.isCancelled()) {
                if (DimensionsSettings.enableDebugLogging && en instanceof Player) {
                  Bukkit.getLogger()
                      .info(
                          "[Dimensions-Debug] Teleport task cancelled by CustomPortalUseEvent for"
                              + " player "
                              + en.getName());
                }
                return;
              }

              if (portal.tags.containsKey("disableTP")) {
                portal.tags.remove("disableTP");
                DimensionsDebbuger.DEBUG.print("DISABLE");
                if (DimensionsSettings.enableDebugLogging && en instanceof Player) {
                  Bukkit.getLogger()
                      .info(
                          "[Dimensions-Debug] Teleport task aborted: disableTP tag present for"
                              + " player "
                              + en.getName());
                }
                return;
              }

              CompletePortal destination = useEvent.getDestinationPortal();
              if (destination != null) {
                if (DimensionsSettings.enableDebugLogging && en instanceof Player) {
                  Bukkit.getLogger()
                      .info(
                          "[Dimensions-Debug] Destination portal found at "
                              + destination.getCenter()
                              + ". Loading chunk asynchronously...");
                }
                // Destination portal exists. We load the destination chunk asynchronously first
                Location destLoc = destination.getCenter();
                destLoc
                    .getWorld()
                    .getChunkAtAsync(destLoc)
                    .thenAccept(
                        chunk -> {
                          DimensionsScheduler.run(
                              Dimensions.getInstance(),
                              destLoc,
                              () -> {
                                if (portal.brokenPortal) return;
                                Location teleportLocation = destination.getCenter().clone();
                                teleportLocation.setY(
                                    destination.getPortalGeometry().getInsideMin().getY());
                                teleportLocation.setYaw(en.getLocation().getYaw());
                                teleportLocation.setPitch(en.getLocation().getPitch());

                                EntityType trasnformation =
                                    portal.customPortal.getEntityTransformation(en.getType());
                                if (trasnformation == null) {
                                  if (DimensionsSettings.enableDebugLogging
                                      && en instanceof Player) {
                                    Bukkit.getLogger()
                                        .info(
                                            "[Dimensions-Debug] Teleporting player "
                                                + en.getName()
                                                + " to "
                                                + teleportLocation);
                                  }
                                  destination.pushToHold(en);
                                  en.teleportAsync(teleportLocation)
                                      .thenRun(
                                          () -> {
                                            Object soundOpt =
                                                me.xxastaspastaxx.dimensions.addons.DimensionsAddon
                                                    .getOption(portal, "travelSound");
                                            me.xxastaspastaxx.dimensions.DimensionsUtils
                                                .playPortalSound(
                                                    teleportLocation,
                                                    soundOpt,
                                                    org.bukkit.Sound.BLOCK_PORTAL_TRAVEL,
                                                    1.0f,
                                                    1.0f);

                                            if (DimensionsSettings.enableDebugLogging
                                                && en instanceof Player) {
                                              Bukkit.getLogger()
                                                  .info(
                                                      "[Dimensions-Debug] Teleport completed for"
                                                          + " player "
                                                          + en.getName()
                                                          + ", removing from origin hold");
                                            }
                                            if (Bukkit.isOwnedByCurrentRegion(portal.getCenter())) {
                                              portal.removeFromHold(en);
                                            } else {
                                              DimensionsScheduler.run(
                                                  Dimensions.getInstance(),
                                                  portal.getCenter(),
                                                  () -> portal.removeFromHold(en));
                                            }
                                          });
                                } else {
                                  if (DimensionsSettings.enableDebugLogging
                                      && en instanceof Player) {
                                    Bukkit.getLogger()
                                        .info(
                                            "[Dimensions-Debug] Spawning transformed entity for"
                                                + " player "
                                                + en.getName()
                                                + " at "
                                                + teleportLocation);
                                  }
                                  Entity newEn =
                                      teleportLocation
                                          .getWorld()
                                          .spawnEntity(teleportLocation, trasnformation);

                                  DimensionsUtils.cloneEntity(en, newEn);
                                  destination.pushToHold(newEn);

                                  Object soundOpt =
                                      me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
                                          portal, "travelSound");
                                  me.xxastaspastaxx.dimensions.DimensionsUtils.playPortalSound(
                                      teleportLocation,
                                      soundOpt,
                                      org.bukkit.Sound.BLOCK_PORTAL_TRAVEL,
                                      1.0f,
                                      1.0f);

                                  en.remove();
                                  portal.removeFromHold(en);
                                }
                              });
                        });
              } else {
                if (DimensionsSettings.enableDebugLogging && en instanceof Player) {
                  Bukkit.getLogger()
                      .info(
                          "[Dimensions-Debug] Destination portal not found. Calculating exit"
                              + " location and loading chunk asynchronously...");
                }
                // Destination portal does not exist. We must load the target chunk asynchronously
                // first,
                // then run the portal building and teleportation on the destination region thread.
                Location destLoc = portal.getDestinationLocation(null, null);
                destLoc
                    .getWorld()
                    .getChunkAtAsync(destLoc)
                    .thenAccept(
                        chunk -> {
                          DimensionsScheduler.run(
                              Dimensions.getInstance(),
                              destLoc,
                              () -> {
                                if (portal.brokenPortal) return;

                                CompletePortal finalDestination = null;
                                if (portal.customPortal.canBuildExitPortal()) {
                                  finalDestination = portal.getDestinationPortal(true, null, null);
                                } else {
                                  Location destLocation = portal.getDestinationLocation(null, null);
                                  finalDestination =
                                      new CompletePortal(
                                          portal.customPortal,
                                          destLocation.getWorld(),
                                          portal.portalGeometry.createGeometry(
                                              destLocation.toVector(), destLocation.toVector()));

                                  Block b =
                                      finalDestination
                                          .getCenter()
                                          .getBlock()
                                          .getRelative(BlockFace.DOWN);
                                  if (!b.getType().isSolid())
                                    b.setType(portal.customPortal.getOutsideMaterial());
                                }

                                if (finalDestination == null) {
                                  if (DimensionsSettings.enableDebugLogging
                                      && en instanceof Player) {
                                    Bukkit.getLogger()
                                        .info(
                                            "[Dimensions-Debug] Failed to build or locate final"
                                                + " destination portal for player "
                                                + en.getName());
                                  }
                                  portal.removeFromHold(en);
                                  return;
                                }

                                Location teleportLocation = finalDestination.getCenter().clone();
                                teleportLocation.setY(
                                    finalDestination.getPortalGeometry().getInsideMin().getY());
                                teleportLocation.setYaw(en.getLocation().getYaw());
                                teleportLocation.setPitch(en.getLocation().getPitch());

                                EntityType trasnformation =
                                    portal.customPortal.getEntityTransformation(en.getType());
                                if (trasnformation == null) {
                                  if (DimensionsSettings.enableDebugLogging
                                      && en instanceof Player) {
                                    Bukkit.getLogger()
                                        .info(
                                            "[Dimensions-Debug] Teleporting player "
                                                + en.getName()
                                                + " to new exit portal at "
                                                + teleportLocation);
                                  }
                                  finalDestination.pushToHold(en);
                                  en.teleportAsync(teleportLocation)
                                      .thenRun(
                                          () -> {
                                            Object soundOpt =
                                                me.xxastaspastaxx.dimensions.addons.DimensionsAddon
                                                    .getOption(portal, "travelSound");
                                            me.xxastaspastaxx.dimensions.DimensionsUtils
                                                .playPortalSound(
                                                    teleportLocation,
                                                    soundOpt,
                                                    org.bukkit.Sound.BLOCK_PORTAL_TRAVEL,
                                                    1.0f,
                                                    1.0f);

                                            if (DimensionsSettings.enableDebugLogging
                                                && en instanceof Player) {
                                              Bukkit.getLogger()
                                                  .info(
                                                      "[Dimensions-Debug] Teleport completed to new"
                                                          + " exit portal for player "
                                                          + en.getName()
                                                          + ", removing from origin hold");
                                            }
                                            if (Bukkit.isOwnedByCurrentRegion(portal.getCenter())) {
                                              portal.removeFromHold(en);
                                            } else {
                                              DimensionsScheduler.run(
                                                  Dimensions.getInstance(),
                                                  portal.getCenter(),
                                                  () -> portal.removeFromHold(en));
                                            }
                                          });
                                } else {
                                  if (DimensionsSettings.enableDebugLogging
                                      && en instanceof Player) {
                                    Bukkit.getLogger()
                                        .info(
                                            "[Dimensions-Debug] Spawning transformed entity for"
                                                + " player "
                                                + en.getName()
                                                + " at new exit portal "
                                                + teleportLocation);
                                  }
                                  Entity newEn =
                                      teleportLocation
                                          .getWorld()
                                          .spawnEntity(teleportLocation, trasnformation);

                                  DimensionsUtils.cloneEntity(en, newEn);
                                  finalDestination.pushToHold(newEn);

                                  Object soundOpt =
                                      me.xxastaspastaxx.dimensions.addons.DimensionsAddon.getOption(
                                          portal, "travelSound");
                                  me.xxastaspastaxx.dimensions.DimensionsUtils.playPortalSound(
                                      teleportLocation,
                                      soundOpt,
                                      org.bukkit.Sound.BLOCK_PORTAL_TRAVEL,
                                      1.0f,
                                      1.0f);

                                  en.remove();
                                  portal.removeFromHold(en);
                                }
                              });
                        });
              }
            },
            () -> {
              if (DimensionsSettings.enableDebugLogging && en instanceof Player) {
                Bukkit.getLogger()
                    .info(
                        "[Dimensions-Debug] Teleport task retired (entity removed or unticked) for"
                            + " player "
                            + en.getName());
              }
              portal.removeFromHold(en);
            },
            delay));
  }

  public static Location getDestinationLocation(
      CompletePortal portal, Location overrideLocation, World overrideWorld) {
    Location newLocation = overrideLocation == null ? portal.getCenter() : overrideLocation;

    World destinationWorld = overrideWorld;
    if (destinationWorld == null) {
      destinationWorld = portal.customPortal.getWorld();
      if (destinationWorld == null || portal.world.equals(destinationWorld))
        destinationWorld =
            portal.lastLinkedWorld == null
                ? DimensionsSettings.fallbackWorld
                : portal.lastLinkedWorld;
    }
    if (destinationWorld == null) {
      destinationWorld = portal.world;
    }
    newLocation.setWorld(destinationWorld);

    // Fix world ratio
    newLocation = newLocation.multiply(getWorldRatio(portal, destinationWorld));
    WorldBorder border = destinationWorld.getWorldBorder();
    if (!border.isInside(newLocation)) {

      double borderX = border.getCenter().getX();
      double borderZ = border.getCenter().getZ();
      double borderSize =
          (border.getSize() / 2)
              - (portal.getPortalGeometry().getPortalWidth(portal.customPortal) * 2);

      if (newLocation.getX() > borderX) {
        newLocation.setX(Math.min(newLocation.getX(), borderX + borderSize));
      } else {
        newLocation.setX(Math.max(newLocation.getX(), borderX - borderSize));
      }

      if (newLocation.getZ() > borderZ) {
        newLocation.setZ(Math.min(newLocation.getZ(), borderZ + borderSize));
      } else {
        newLocation.setZ(Math.max(newLocation.getZ(), borderZ - borderSize));
      }
    }

    WorldConfiguration currWorldConfig = DimensionsSettings.getWorldConfiguration(portal.world);
    WorldConfiguration destWorldConfig = DimensionsSettings.getWorldConfiguration(destinationWorld);

    // FIX the world height ratio
    int currMinWorldHeight = currWorldConfig.getMinHeight();
    int currMaxWorldHeight = currWorldConfig.getMaxHeight();
    int currWorldHeight = currMaxWorldHeight - currMinWorldHeight;

    int minWorldHeight = destWorldConfig.getMinHeight();
    int maxWorldHeight = destWorldConfig.getMaxHeight();
    int worldHeight = maxWorldHeight - minWorldHeight;

    double currPercent = (portal.getCenter().getY() - currMinWorldHeight) / currWorldHeight;

    newLocation.setY(worldHeight * currPercent + minWorldHeight);

    return newLocation;
  }

  public static CompletePortal getDestinationPortal(
      CompletePortal portal,
      boolean buildNewPortal,
      Location overrideLocation,
      World overrideWorld) {

    if (portal.linkedPortal != null) return portal.linkedPortal;

    Location newLocation = getDestinationLocation(portal, overrideLocation, overrideWorld);
    DimensionsDebbuger.DEBUG.print("New Location: " + newLocation);
    World destinationWorld = newLocation.getWorld();
    DimensionsDebbuger.DEBUG.print("Destination World: " + destinationWorld);
    double ratio = getWorldRatio(portal, destinationWorld);
    DimensionsDebbuger.DEBUG.print("Ratio: " + ratio);

    CompletePortal destination = null;
    if (DimensionsSettings.searchFirstClonePortal)
      destination =
          Dimensions.getCompletePortalManager()
              .getNearestPortal(newLocation, portal, ratio, true, true);

    if (destination == null)
      destination =
          Dimensions.getCompletePortalManager()
              .getNearestPortal(
                  newLocation,
                  portal,
                  ratio,
                  DimensionsSettings.searchSameAxis,
                  DimensionsSettings.searchSameSize);

    DimensionsDebbuger.DEBUG.print(
        "First try for destination (check for already existing portal): " + destination);
    if (destination != null) {
      DimensionsDebbuger.DEBUG.print(
          "Destination not null. DestLocation: " + destination.getCenter());
    }

    if (destination == null) {
      DimensionsDebbuger.DEBUG.print("Destination not found, attempting to create a portal: ");
      if (!buildNewPortal) {
        DimensionsDebbuger.DEBUG.print("buildExitPortal var is false, skipping this time");
        return null;
      }
      boolean zAxis = portal.portalGeometry.iszAxis();
      byte width = portal.portalGeometry.getPortalWidth(portal.customPortal);
      byte height = portal.portalGeometry.getPortalHeight(portal.customPortal);

      DimensionsDebbuger.DEBUG.print(
          "Exit portal info: zAxis: " + zAxis + ", width: " + width + ", height: " + height);

      Location checkLocation =
          getSafeLocation(portal, newLocation, zAxis, destinationWorld, height, width);
      DimensionsDebbuger.DEBUG.print("SafeLocation found: " + checkLocation);
      if (checkLocation != null) newLocation = checkLocation;
      DimensionsDebbuger.DEBUG.print("Final location: " + newLocation);

      DimensionsDebbuger.DEBUG.print("Attempting to build portal...");
      portal.portalGeometry.buildPortal(newLocation, destinationWorld, portal.customPortal);
      DimensionsDebbuger.DEBUG.print("Portal should be built at: " + newLocation);

      PortalGeometry geom =
          PortalGeometry.getPortalGeometry(portal.customPortal)
              .getPortal(portal.customPortal, newLocation.add(zAxis ? 0 : 1, 1, zAxis ? 1 : 0));

      DimensionsDebbuger.DEBUG.print(
          "Identify built structure: " + (geom == null ? "NOPE" : "Yep"));
      if (geom == null) return null;
      destination =
          Dimensions.getCompletePortalManager()
              .createNew(
                  new CompletePortal(portal.customPortal, newLocation.getWorld(), geom),
                  null,
                  CustomPortalIgniteCause.EXIT_PORTAL,
                  null);
      DimensionsDebbuger.DEBUG.print(
          "Created portal instance: " + (destination == null ? "NOPE" : "Yep"));
      if (destination == null) return null;
    }

    if (destination.getLinkedPortal() == null) {
      portal.setLinkedPortal(destination);
      destination.setLinkedPortal(portal);
    }

    DimensionsDebbuger.DEBUG.print(
        "Final destination portal "
            + (destination == null ? "Houston, we have a problem" : destination.getCenter()));

    return destination;
  }

  public static double getWorldRatio(CompletePortal portal, World destinationWorld) {
    double currWorldSize = DimensionsSettings.getWorldConfiguration(portal.world).getSize();
    double worldSize = DimensionsSettings.getWorldConfiguration(destinationWorld).getSize();
    double ratio = worldSize / currWorldSize;

    return ratio;
  }

  private static Location getSafeLocation(
      CompletePortal portal,
      Location newLocation,
      boolean zAxis,
      World destinationWorld,
      int height,
      int width) {
    Location backupLocation = null;
    Location backupLocation2 = null;
    Location checkLocation;

    WorldConfiguration destWorldConfig = DimensionsSettings.getWorldConfiguration(destinationWorld);
    int maxWorldHeight = destWorldConfig.getMaxHeight() - height;

    for (int m = 0; m < DimensionsSettings.safeSpotSearchRadius; m++) {
      checkLocation = newLocation.clone();

      boolean isCenter =
          !DimensionsSettings.safeSpotSearchAllY || m < DimensionsSettings.safeSpotSearchRadius - 1;
      int y = 0;
      int yAdd = 1;
      boolean step1 = true;
      while ((isCenter && y != m + 1) || !isCenter) {
        checkLocation.setY(newLocation.getY() + y);
        if (checkLocation.getY() >= destWorldConfig.getMinHeight()
            && checkLocation.getY() <= maxWorldHeight) {
          step1 = true;

          int dir = 0;

          int x = 0;
          int z = 0;
          float travel = 1;
          if (!(y >= m || y <= -m)) {
            travel = m * 2 - 0.5f;
            x = -m + 1;
            z = m;
            dir = 1;
          }
          int travelCurr = (int) travel;
          while (x != -m || z != m + 1) {

            checkLocation.setZ(newLocation.getZ() + z);
            checkLocation.setX(newLocation.getX() + x);

            if (destinationWorld.getWorldBorder().isInside(checkLocation)) {
              BoundingBox candidateBox =
                  new BoundingBox(
                      checkLocation.getX(),
                      checkLocation.getY(),
                      checkLocation.getZ(),
                      zAxis ? checkLocation.getX() : checkLocation.getX() + width,
                      checkLocation.getY() + height,
                      zAxis ? checkLocation.getZ() + width : checkLocation.getZ());
              boolean overlaps = false;
              for (CompletePortal complete :
                  Dimensions.getCompletePortalManager().getCompletePortals(destinationWorld)) {
                if (complete.getPortalGeometry().getBoundingBox().overlaps(candidateBox)) {
                  overlaps = true;
                  break;
                }
              }
              if (!overlaps) {
                if (canBuildPortal(
                    portal, checkLocation, zAxis, destinationWorld, height, width, true))
                  return checkLocation;
                if (backupLocation == null
                    && canBuildPortal(
                        portal, checkLocation, !zAxis, destinationWorld, height, width, true))
                  backupLocation = checkLocation.clone();
                if (backupLocation2 == null
                    && canBuildPortal(
                        portal, checkLocation, zAxis, destinationWorld, height, width, false))
                  backupLocation2 = checkLocation.clone();
              }
            }

            switch (dir) {
              case 0:
                z++;
                break;
              case 1:
                x++;
                break;
              case 2:
                z--;
                break;
              case 3:
                x--;
                break;
              default:
                break;
            }
            if (--travelCurr <= 0) {
              travel += 0.5f;
              travelCurr = (int) travel;
              if (++dir == 4) dir = 0;
            }
          }
        } else if (!isCenter) {
          if ((step1 = !step1)) break;
        }

        y += yAdd;
        if (yAdd > 0) yAdd = -(++yAdd);
        else yAdd = Math.abs(--yAdd);
      }
    }

    return backupLocation != null ? backupLocation : backupLocation2;
  }

  private static boolean canBuildPortal(
      CompletePortal portal,
      Location checkLocation,
      boolean zAxis,
      World destinationWorld,
      int height,
      int width,
      boolean checkPlatform) {

    double maxY = (checkLocation.getY() + height);
    double maxSide = ((zAxis ? checkLocation.getZ() : checkLocation.getX()) + width);

    for (double y = checkLocation.getY(); y <= maxY; y++) {
      for (double side = (zAxis ? checkLocation.getZ() : checkLocation.getX());
          side <= maxSide;
          side++) {
        Block block =
            new Location(
                    destinationWorld,
                    zAxis ? checkLocation.getX() : side,
                    y,
                    !zAxis ? checkLocation.getZ() : side)
                .getBlock();
        if (y == checkLocation.getY() && !block.getRelative(BlockFace.DOWN).getType().isSolid())
          return false; // check if has bottom

        if (checkPlatform
            && y == checkLocation.getY()
            && (!block
                    .getRelative(BlockFace.DOWN)
                    .getRelative(zAxis ? BlockFace.WEST : BlockFace.SOUTH)
                    .getType()
                    .isSolid()
                || !block
                    .getRelative(BlockFace.DOWN)
                    .getRelative(zAxis ? BlockFace.EAST : BlockFace.NORTH)
                    .getType()
                    .isSolid())) return false;

        if ((y == checkLocation.getY() || y == maxY)
            || ((side == (zAxis ? checkLocation.getZ() : checkLocation.getX()))
                || side == maxSide)) {
          if (!portal.customPortal.isPortalBlock(block) && !DimensionsUtils.isAir(block))
            return false;
        } else {
          if (!DimensionsUtils.isAir(block)) return false;
        }
      }
    }
    return true;
  }
}
