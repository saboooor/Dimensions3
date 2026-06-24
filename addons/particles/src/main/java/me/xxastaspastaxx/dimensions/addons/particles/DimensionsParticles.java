package me.xxastaspastaxx.dimensions.addons.particles;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.ArrayList;
import java.util.HashMap;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsScheduler;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.events.CustomPortalBreakEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class DimensionsParticles extends DimensionsAddon implements Listener {

  private Plugin pl;

  HashMap<CompletePortal, ArrayList<ScheduledTask>> tasks =
      new HashMap<CompletePortal, ArrayList<ScheduledTask>>();

  public DimensionsParticles() {
    super("DimensionsParticlesAddon", "3.0.4", "Particles!!!", DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    this.pl = pl;

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postIgnitePortal(CustomPortalIgniteEvent e) {
    CompletePortal complete = e.getCompletePortal();

    Object particlesOBJ = getOption(complete, "particlePacks");
    if (particlesOBJ == null) return;
    @SuppressWarnings("unchecked")
    ArrayList<ParticlePack> packs = (ArrayList<ParticlePack>) particlesOBJ;

    complete.setTag("hidePortalParticles", true);

    ArrayList<ScheduledTask> list = new ArrayList<ScheduledTask>();

    for (ParticlePack pack : packs) {
      list.add(
          DimensionsScheduler.runAtFixedRate(
              pl,
              complete.getCenter(),
              new Runnable() {

                @Override
                public void run() {
                  pack.begin(complete);
                }
              },
              1,
              pack.vars.get("frequency").intValue()));
    }

    tasks.put(complete, list);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postDestroyPortal(CustomPortalBreakEvent e) {
    CompletePortal complete = e.getCompletePortal();

    Object particlesOBJ = getOption(complete, "particlePacks");
    if (particlesOBJ == null) return;

    @SuppressWarnings("unchecked")
    ArrayList<ParticlePack> packs = (ArrayList<ParticlePack>) particlesOBJ;
    packs.forEach(pack -> pack.remove(complete));

    complete.setTag("hidePortalParticles", null);
    for (ScheduledTask task : tasks.get(complete)) {
      task.cancel();
    }
    tasks.remove(complete);
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    ArrayList<ParticlePack> spl = ParticlePack.load(portalConfig.getStringList("Addon.Particles"));

    if (spl.isEmpty()) return;

    setOption(portal, "particlePacks", spl);

    return;
  }

  @Override
  public void onDisable() {
    ParticlePack.unloadAll();

    for (CompletePortal completePortal :
        Dimensions.getCompletePortalManager().getCompletePortals()) {
      if (!tasks.containsKey(completePortal)) continue;
      for (ScheduledTask task : tasks.get(completePortal)) {
        task.cancel();
      }
    }
  }
}
