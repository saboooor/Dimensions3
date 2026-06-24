package me.xxastaspastaxx.dimensions;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Folia-compatible scheduler utility. All scheduling in Dimensions routes through this class
 * instead of {@code Bukkit.getScheduler()} to ensure correct behaviour on both Paper (main-thread)
 * and Folia (regionalized-thread) servers.
 *
 * <p>The Paper API exposes the Folia scheduler interfaces ({@code RegionScheduler}, {@code
 * GlobalRegionScheduler}, {@code AsyncScheduler}, {@code EntityScheduler}) since Paper 1.19+. On
 * non-Folia Paper servers every call executes on the main server thread, preserving full backwards
 * compatibility.
 */
public final class DimensionsScheduler {

  private DimensionsScheduler() {}

  // ---------------------------------------------------------------------------
  // Global (no location context) — GlobalRegionScheduler
  // ---------------------------------------------------------------------------

  /**
   * Schedule a task to run as soon as possible on the global region thread (main thread on Paper).
   *
   * @param plugin the owning plugin
   * @param task the task to run
   * @return the scheduled task
   */
  public static ScheduledTask run(Plugin plugin, Runnable task) {
    return plugin.getServer().getGlobalRegionScheduler().run(plugin, ctx -> task.run());
  }

  /**
   * Schedule a task to run after a delay on the global region thread.
   *
   * @param plugin the owning plugin
   * @param task the task to run
   * @param delayTicks delay in server ticks
   * @return the scheduled task
   */
  public static ScheduledTask runDelayed(Plugin plugin, Runnable task, long delayTicks) {
    return plugin
        .getServer()
        .getGlobalRegionScheduler()
        .runDelayed(plugin, ctx -> task.run(), delayTicks);
  }

  /**
   * Schedule a repeating task on the global region thread.
   *
   * @param plugin the owning plugin
   * @param task the task to run
   * @param initialDelayTicks delay before first run, in ticks
   * @param periodTicks period between runs, in ticks
   * @return the scheduled task (call {@code cancel()} to stop it)
   */
  public static ScheduledTask runAtFixedRate(
      Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks) {
    return plugin
        .getServer()
        .getGlobalRegionScheduler()
        .runAtFixedRate(plugin, ctx -> task.run(), initialDelayTicks, periodTicks);
  }

  // ---------------------------------------------------------------------------
  // Location-aware — RegionScheduler
  // ---------------------------------------------------------------------------

  /**
   * Schedule a task on the region that owns the given location (main thread on Paper).
   *
   * @param plugin the owning plugin
   * @param location the location whose region owns this task
   * @param task the task to run
   * @return the scheduled task
   */
  public static ScheduledTask run(Plugin plugin, Location location, Runnable task) {
    return plugin.getServer().getRegionScheduler().run(plugin, location, ctx -> task.run());
  }

  /**
   * Schedule a delayed task on the region that owns the given location.
   *
   * @param plugin the owning plugin
   * @param location the location whose region owns this task
   * @param task the task to run
   * @param delayTicks delay in server ticks
   * @return the scheduled task
   */
  public static ScheduledTask runDelayed(
      Plugin plugin, Location location, Runnable task, long delayTicks) {
    return plugin
        .getServer()
        .getRegionScheduler()
        .runDelayed(plugin, location, ctx -> task.run(), delayTicks);
  }

  /**
   * Schedule a repeating task on the region that owns the given location.
   *
   * @param plugin the owning plugin
   * @param location the location whose region owns this task
   * @param task the task to run
   * @param initialDelayTicks delay before first run, in ticks
   * @param periodTicks period between runs, in ticks
   * @return the scheduled task (call {@code cancel()} to stop it)
   */
  public static ScheduledTask runAtFixedRate(
      Plugin plugin, Location location, Runnable task, long initialDelayTicks, long periodTicks) {
    return plugin
        .getServer()
        .getRegionScheduler()
        .runAtFixedRate(plugin, location, ctx -> task.run(), initialDelayTicks, periodTicks);
  }

  // ---------------------------------------------------------------------------
  // Async — AsyncScheduler
  // ---------------------------------------------------------------------------

  /**
   * Schedule a task to run asynchronously immediately.
   *
   * @param plugin the owning plugin
   * @param task the task to run
   * @return the scheduled task
   */
  public static ScheduledTask runAsync(Plugin plugin, Runnable task) {
    return plugin.getServer().getAsyncScheduler().runNow(plugin, ctx -> task.run());
  }

  // ---------------------------------------------------------------------------
  // Entity-aware — EntityScheduler
  // ---------------------------------------------------------------------------

  /**
   * Schedule a delayed task tied to an entity. If the entity is removed before the task fires, the
   * {@code retired} callback is invoked instead.
   *
   * @param entity the entity that owns this task
   * @param plugin the owning plugin
   * @param task the task to run
   * @param retired callback invoked if the entity is removed before the task runs (may be null)
   * @param delayTicks delay in server ticks (minimum 1)
   * @return the scheduled task, or null if the entity is already retired
   */
  public static ScheduledTask runEntityDelayed(
      Entity entity, Plugin plugin, Runnable task, Runnable retired, long delayTicks) {
    return entity
        .getScheduler()
        .runDelayed(plugin, ctx -> task.run(), retired, Math.max(1, delayTicks));
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Cancel a task if it is non-null and not already cancelled.
   *
   * @param task the task to cancel (may be null)
   */
  public static void cancel(ScheduledTask task) {
    if (task != null && !task.isCancelled()) {
      task.cancel();
    }
  }
}
