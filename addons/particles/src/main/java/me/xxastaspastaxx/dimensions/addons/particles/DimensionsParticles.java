package me.xxastaspastaxx.dimensions.addons.particles;

import java.util.ArrayList;
import java.util.HashMap;
import me.xxastaspastaxx.dimensions.Dimensions;
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

  HashMap<CompletePortal, ArrayList<Integer>> tasks =
      new HashMap<CompletePortal, ArrayList<Integer>>();

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

    ArrayList<Integer> list = new ArrayList<Integer>();

    for (ParticlePack pack : packs) {
      list.add(
          Bukkit.getScheduler()
              .scheduleSyncRepeatingTask(
                  pl,
                  new Runnable() {

                    @Override
                    public void run() {
                      pack.begin(complete);
                    }
                  },
                  0,
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
    for (int i : tasks.get(complete)) {
      Bukkit.getScheduler().cancelTask(i);
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
      for (int i : tasks.get(completePortal)) {
        Bukkit.getScheduler().cancelTask(i);
      }
    }
  }
}
