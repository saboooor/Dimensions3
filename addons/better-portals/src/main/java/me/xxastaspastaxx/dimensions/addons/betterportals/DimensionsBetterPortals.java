package me.xxastaspastaxx.dimensions.addons.betterportals;

import com.lauriethefish.betterportals.api.BetterPortal;
import com.lauriethefish.betterportals.api.BetterPortalsAPI;
import com.lauriethefish.betterportals.api.PortalDirection;
import com.lauriethefish.betterportals.api.PortalPosition;
import com.lauriethefish.betterportals.api.PortalPredicate;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.HashMap;
import java.util.UUID;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsScheduler;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.addons.horizontalportals.HorizontalPortalGeometry;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.completePortal.PortalGeometry;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalDestroyCause;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalIgniteCause;
import me.xxastaspastaxx.dimensions.events.CustomPortalBreakEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class DimensionsBetterPortals extends DimensionsAddon implements Listener {

  private Plugin pl;

  private BetterPortalsAPI bpAPI;

  HashMap<CompletePortal, ScheduledTask> tasks = new HashMap<CompletePortal, ScheduledTask>();

  // ArrayList<CompletePortal> used = new ArrayList<CompletePortal>();

  public DimensionsBetterPortals() {
    super(
        "DimensionsBetterPortalsAddon",
        "3.0.7",
        "Hook for the better portals plugin",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public boolean onLoad(Dimensions main) {
    this.pl = main;

    return main.getServer().getPluginManager().getPlugin("BetterPortals") != null;
  }

  @Override
  public void onEnable(Dimensions pl) {
    Dimensions.getCommandManager()
        .registerCommand(
            new MirrorPortalCommand(
                "mirror", "", new String[0], "Make the portal look the other way", "", true, this));
    try {
      this.bpAPI = BetterPortalsAPI.get();

      bpAPI.addPortalTeleportPredicate(
          new PortalPredicate() {

            @Override
            public boolean test(@NotNull BetterPortal portal, @NotNull Player player) {
              if (portal.getName() == null || !portal.getName().equals("dimensions")) return true;

              CompletePortal complete =
                  Dimensions.getCompletePortalManager()
                      .getCompletePortal(portal.getOriginPos().getLocation(), false, false);

              CustomPortalUseEvent useEvent =
                  new CustomPortalUseEvent(
                      complete,
                      player,
                      Dimensions.getCompletePortalManager()
                          .getCompletePortal(portal.getDestPos().getLocation(), false, false));
              Bukkit.getPluginManager().callEvent(useEvent);

              boolean cheat = complete.getTag("cheatPredicate") != null;
              complete.setTag("cheatPredicate", null);

              return !useEvent.isCancelled() || cheat;
            }
          });
    } catch (IllegalStateException ex) {
      ex.printStackTrace();
    }

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onPortalUse(CustomPortalUseEvent e) {
    CompletePortal complete = e.getCompletePortal();
    Object temp = getOption(complete, "betterPortal");
    if (temp == null) return;

    // if (used.contains(completePortal)) {
    //	return 1;
    // }

    if ((tasks.containsKey(complete)
        && tasks.get(complete).getExecutionState() == ScheduledTask.ExecutionState.RUNNING)) {
      e.setCancelled(true);
    } else if ((complete.getTag("betterPortal") != null
        && ((boolean) complete.getTag("betterPortal")))) {
      e.setCancelled(true);
      complete.setTag("cheatPredicate", true);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postIgnitePortal(CustomPortalIgniteEvent e) {
    if (e.getCause() == CustomPortalIgniteCause.EXIT_PORTAL) return;
    CompletePortal complete = e.getCompletePortal();
    Object temp = getOption(complete, "betterPortal");
    if (temp == null) return;

    @SuppressWarnings("unchecked")
    HashMap<CompletePortal, UUID> map = (HashMap<CompletePortal, UUID>) temp;

    if (map.containsKey(complete)) return;

    CompletePortal linked = complete.getLinkedPortal();
    if (linked != null) {
      link(complete, linked, false);
      return;
    }

    if (e.getCause() == CustomPortalIgniteCause.LOAD_PORTAL) return;

    Entity entity = e.getEntity();

    tasks.put(
        complete,
        DimensionsScheduler.run(
            pl,
            entity.getLocation(),
            new Runnable() {

              @Override
              public void run() {
                if (entity instanceof Player player) {
                  player.sendActionBar(Component.text("Creating exit portal...."));
                }

                CompletePortal tpPortal = complete.getDestinationPortal(true, null, null);

                if (tpPortal == null) return;

                if (tpPortal.getLinkedPortal() == null
                    || tpPortal.getLinkedPortal().equals(complete)) {
                  link(complete, tpPortal, false);
                }
              }
            }));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postUsePortal(CustomPortalUseEvent e) {
    CompletePortal complete = e.getCompletePortal();
    Object temp = getOption(complete, "betterPortal");
    if (temp == null) return;

    // if (used.contains(completePortal)) return;
    @SuppressWarnings("unchecked")
    HashMap<CompletePortal, UUID> map = (HashMap<CompletePortal, UUID>) temp;

    if (map.containsKey(complete)) return;

    CompletePortal linked = complete.getLinkedPortal();
    if (linked == null) return;
    link(complete, linked, false);
  }

  public void link(CompletePortal completePortal, CompletePortal linked, boolean mirrored) {
    PortalGeometry geom = completePortal.getPortalGeometry();
    PortalGeometry geom2 = linked.getPortalGeometry();
    if (geom.getPortalWidth() != geom2.getPortalWidth()
        || geom.getPortalHeight() != geom2.getPortalHeight()) return;

    completePortal.setLinkedPortal(linked);
    linked.setLinkedPortal(completePortal);

    boolean horizontal = completePortal.getPortalGeometry() instanceof HorizontalPortalGeometry;
    boolean horizontal2 = linked.getPortalGeometry() instanceof HorizontalPortalGeometry;

    PortalPosition originPos =
        new PortalPosition(
            completePortal.getCenter(),
            horizontal
                ? PortalDirection.UP
                : geom.iszAxis() ? PortalDirection.EAST : PortalDirection.NORTH);

    PortalPosition destinationPos =
        new PortalPosition(
            linked.getCenter(),
            !mirrored
                ? (horizontal
                    ? PortalDirection.UP
                    : geom2.iszAxis() ? PortalDirection.EAST : PortalDirection.NORTH)
                : (horizontal2
                    ? PortalDirection.DOWN
                    : geom2.iszAxis() ? PortalDirection.WEST : PortalDirection.SOUTH));

    Vector size = geom.getMax().clone().subtract(geom.getMin());
    // if (geom.iszAxis()) size.setX(size.getZ());
    size.subtract(new Vector(1, 1, 1));

    BetterPortal betterPortal =
        bpAPI.createPortal(
            originPos,
            destinationPos,
            originPos.getDirection().swapVector(size),
            UUID.randomUUID(),
            "dimensions");

    BetterPortal betterPortal2 =
        bpAPI.createPortal(
            destinationPos,
            originPos,
            destinationPos.getDirection().swapVector(size),
            UUID.randomUUID(),
            "dimensions");

    @SuppressWarnings("unchecked")
    HashMap<CompletePortal, UUID> map =
        (HashMap<CompletePortal, UUID>) getOption(completePortal, "betterPortal");

    if (map.containsKey(completePortal)) {
      BetterPortal bt = bpAPI.getPortalById(map.get(completePortal));
      completePortal.setTag("betterPortal", null);
      completePortal.setTag("hidePortalInside", null);
      completePortal.setTag("hidePortalParticles", null);
      completePortal.setTag("mirrored", null);

      if (bt != null) bt.remove(false);
    }

    if (map.containsKey(linked)) {
      BetterPortal bt = bpAPI.getPortalById(map.get(linked));
      linked.setTag("betterPortal", null);
      linked.setTag("hidePortalInside", null);
      linked.setTag("hidePortalParticles", null);
      linked.setTag("mirrored", null);

      if (bt != null) bt.remove(false);
    }

    map.put(completePortal, betterPortal.getId());
    map.put(linked, betterPortal2.getId());
    completePortal.setTag("betterPortal", true);
    linked.setTag("betterPortal", true);

    completePortal.setTag("hidePortalInside", true);
    linked.setTag("hidePortalInside", true);

    completePortal.setTag("hidePortalParticles", true);
    linked.setTag("hidePortalParticles", true);

    if (mirrored) {
      completePortal.setTag("mirrored", true);
      linked.setTag("mirrored", true);
    }

    completePortal.destroy(null);
  }

  public void unlink(CompletePortal complete) {
    Object temp = getOption(complete, "betterPortal");
    if (temp == null) return;

    @SuppressWarnings("unchecked")
    HashMap<CompletePortal, UUID> map = (HashMap<CompletePortal, UUID>) temp;

    if (!map.containsKey(complete)) return;
    BetterPortal bt = bpAPI.getPortalById(map.get(complete));

    if (bt != null) bt.remove(false);

    BetterPortal bt2 = bpAPI.getPortalById(map.get(complete.getLinkedPortal()));
    if (bt2 != null) {
      bt2.remove(false);
      complete.getLinkedPortal().setTag("betterPortal", null);
      complete.getLinkedPortal().setTag("hidePortalInside", null);
      complete.getLinkedPortal().setTag("hidePortalParticles", null);
      complete.getLinkedPortal().setTag("mirrored", null);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void onPortalDestroy(CustomPortalBreakEvent e) {
    CompletePortal complete = e.getCompletePortal();
    Object temp = getOption(complete, "betterPortal");
    if (temp == null) return;

    if (tasks.containsKey(complete)
        && tasks.get(complete).getExecutionState() == ScheduledTask.ExecutionState.RUNNING) {
      e.setCancelled(true);
      return;
    }

    @SuppressWarnings("unchecked")
    HashMap<CompletePortal, UUID> map = (HashMap<CompletePortal, UUID>) temp;

    if (!map.containsKey(complete)) return;
    BetterPortal bt = bpAPI.getPortalById(map.get(complete));
    if (bt != null && e.getCause() == CustomPortalDestroyCause.PLAYER_INSIDE) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postDestroyPortal(CustomPortalBreakEvent e) {

    CompletePortal complete = e.getCompletePortal();
    Object temp = getOption(complete, "betterPortal");
    if (temp == null) return;

    @SuppressWarnings("unchecked")
    HashMap<CompletePortal, UUID> map = (HashMap<CompletePortal, UUID>) temp;

    if (!map.containsKey(complete)) return;
    BetterPortal bt = bpAPI.getPortalById(map.get(complete));

    if (bt != null) bt.remove(false);

    BetterPortal bt2 = bpAPI.getPortalById(map.get(complete.getLinkedPortal()));
    if (bt2 != null) {
      bt2.remove(false);
      complete.getLinkedPortal().setTag("betterPortal", null);
      complete.getLinkedPortal().setTag("hidePortalInside", null);
      complete.getLinkedPortal().setTag("hidePortalParticles", null);
      complete.getLinkedPortal().setTag("mirrored", null);
    }
  }

  @Override
  public void onDisable() {
    // Dimensions.getCommandManager().unregisterCommand("Portal commands", cmd);

    for (CustomPortal portal : Dimensions.getCustomPortalManager().getCustomPortals()) {
      Object temp = getOption(portal, "betterPortal");
      if (temp == null) continue;

      @SuppressWarnings("unchecked")
      HashMap<CompletePortal, UUID> map = (HashMap<CompletePortal, UUID>) temp;

      for (CompletePortal compl : map.keySet()) {
        try {
          bpAPI.getPortalById(map.get(compl)).remove(false);
          compl.setTag("betterPortal", null);
          compl.setTag("hidePortalInside", null);
          compl.setTag("hidePortalParticles", null);
          compl.setTag("mirrored", null);
        } catch (NullPointerException e) {

        }
      }
    }
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    if (!portalConfig.getBoolean("Addon.EnableBetterPortals", true)) return;

    setOption(portal, "betterPortal", new HashMap<CompletePortal, UUID>());
  }
}
