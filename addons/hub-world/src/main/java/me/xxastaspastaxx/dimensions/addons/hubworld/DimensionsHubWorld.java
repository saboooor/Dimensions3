package me.xxastaspastaxx.dimensions.addons.hubworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.events.CustomPortalBreakEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class DimensionsHubWorld extends DimensionsAddon implements Listener {

  private HashMap<CustomPortal, YamlConfiguration> configs =
      new HashMap<CustomPortal, YamlConfiguration>();

  private Plugin pl;

  public DimensionsHubWorld() {
    super("DimensionsHubWorldAddon", "3.0.5", "Set a portal hub", DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions main) {
    this.pl = main;

    Dimensions.getCommandManager()
        .registerCommand(
            new AddHubPortalCommand(
                "addHub",
                "",
                new String[0],
                "Add a portal to the list of hub portals",
                "",
                true,
                this));

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void onPortalIgnite(CustomPortalIgniteEvent e) {

    CompletePortal complete = e.getCompletePortal();
    Object hubWorldName = getOption(complete, "hubWorld");
    if (hubWorldName == null) return;

    World hubWorld = Bukkit.getWorld((String) hubWorldName);

    if (complete.getWorld().equals(hubWorld)
        && !Dimensions.getCompletePortalManager().getCompletePortals(hubWorld).isEmpty())
      e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postPortalBreak(CustomPortalBreakEvent e) {

    CompletePortal complete = e.getCompletePortal();
    if (complete.getTag("hubDummy") == null) return;

    @SuppressWarnings("unchecked")
    ArrayList<Location> list = (ArrayList<Location>) getOption(complete, "hubPortals");

    CompletePortal dummy = null;
    for (Location t : list) {
      dummy = Dimensions.getCompletePortalManager().getCompletePortal(t, false, false);
      if (dummy != null) {
        if (dummy.getTag("hubDummy") != null) {
          dummy = null;
        } else {
          break;
        }
      }
    }
    if (dummy == null) return;
    CompletePortal newDummy = dummy;

    newDummy.setTag("hubDummy", true);
    complete
        .getTags()
        .forEach(
            (key, val) -> {
              if (key.startsWith("HUBRETURN-")) newDummy.setTag(key, val);
            });
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void onPortalUse(CustomPortalUseEvent e) {

    CompletePortal complete = e.getCompletePortal();
    Object hubPortals = getOption(complete, "hubPortals");
    Object hubWorldName = getOption(complete, "hubWorld");

    if (hubWorldName == null && hubPortals == null) return;
    if (!(e.getEntity() instanceof Player)) {
      e.setCancelled(true);
      return;
    }

    Player p = (Player) e.getEntity();
    CustomPortal portal = complete.getCustomPortal();
    if (hubWorldName != null) {
      World hubWorld = Bukkit.getWorld((String) hubWorldName);

      Location loc = complete.getCenter();
      if (!loc.getWorld().equals(hubWorld)) {

        ArrayList<CompletePortal> hubPortal =
            Dimensions.getCompletePortalManager().getCompletePortals(portal, hubWorld);
        if (hubPortal.size() == 1) {
          CompletePortal dest = hubPortal.get(0);
          dest.setTag(
              "HUBRETURN-" + p.getUniqueId().toString(),
              DimensionsUtils.locationToString(loc, ","));
          e.setDestinationPortal(dest);
        }
      } else {
        Object str = complete.getTag("HUBRETURN-" + p.getUniqueId().toString());
        if (str != null) {
          Location ret = DimensionsUtils.parseLocationFromString((String) str, ",");
          complete.setTag("HUBRETURN-" + p.getUniqueId().toString(), null);
          e.setDestinationPortal(
              Dimensions.getCompletePortalManager().getCompletePortal(ret, false, false));
        }
      }
    } else {
      if (hubPortals == null) return;

      @SuppressWarnings("unchecked")
      ArrayList<Location> list = (ArrayList<Location>) hubPortals;

      CompletePortal dummy = null;
      for (Location t : list) {
        if ((dummy = Dimensions.getCompletePortalManager().getCompletePortal(t, false, false))
                != null
            && dummy.getTag("hubDummy") != null) break;
      }

      if (dummy == null) return;
      if (dummy.getTag("hubDummy") == null) {
        dummy.setTag("hubDummy", true);
      }

      boolean isHubPortal =
          list.stream()
              .anyMatch(
                  new Predicate<Location>() {
                    @Override
                    public boolean test(Location t) {
                      return complete.getWorld().equals(t.getWorld())
                          && complete.getPortalGeometry().isInside(t, false, false);
                    }
                  });

      // e.setDestinationPortal(complete.getDestinationPortal(true,
      // list.get(0).clone().subtract(zAxis?0:1,1,zAxis?1:0), list.get(0).getWorld()));

      complete.unlinkPortal();
      if (isHubPortal) {
        Object str = dummy.getTag("HUBRETURN-" + p.getUniqueId().toString());
        if (str != null) {
          Location ret = DimensionsUtils.parseLocationFromString((String) str, ",");
          dummy.setTag("HUBRETURN-" + p.getUniqueId().toString(), null);
          e.setDestinationPortal(
              Dimensions.getCompletePortalManager().getCompletePortal(ret, false, false));
        }
      } else {
        Location loc = null;
        String mode = (String) getOption(complete, "hubPortalsMode");

        if (mode.equals("random")) {
          while ((loc = list.get(DimensionsUtils.getRandom(0, list.size() - 1))) == null)
            ;
        } else if (mode.equals("firstToLast"))
          loc =
              list.stream()
                  .filter(
                      new Predicate<Location>() {

                        @Override
                        public boolean test(Location t) {
                          return Dimensions.getCompletePortalManager()
                                  .getCompletePortal(t, false, false)
                              != null;
                        }
                      })
                  .findFirst()
                  .get();
        CompletePortal dest =
            Dimensions.getCompletePortalManager().getCompletePortal(loc, false, false);
        dummy.setTag(
            "HUBRETURN-" + p.getUniqueId().toString(),
            DimensionsUtils.locationToString(complete.getCenter(), ","));
        e.setDestinationPortal(dest);
      }
    }
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    String worldName = portalConfig.getString("Addon.Hub.World", "false");
    List<String> hubPortals = portalConfig.getStringList("Addon.Hub.Portals.List");

    if (!worldName.equals("false")) {
      setOption(portal, "hubWorld", worldName);
    } else if (!hubPortals.isEmpty()) {
      ArrayList<Location> locs = new ArrayList<Location>();
      hubPortals.stream().forEach(l -> locs.add(DimensionsUtils.parseLocationFromString(l, ",")));
      setOption(portal, "hubPortals", locs);

      // random, firstToLast
      setOption(
          portal, "hubPortalsMode", portalConfig.getString("Addon.Hub.Portals.Mode", "random"));
    }
    configs.put(portal, portalConfig);
  }

  public YamlConfiguration getPortalConfig(CustomPortal portal) {
    return configs.get(portal);
  }
}
