package me.xxastaspastaxx.dimensions.addons.ignitebyevent;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import org.bukkit.configuration.file.YamlConfiguration;

public class DimensionsIgniteByEvent extends DimensionsAddon {

  // private Plugin pl;
  private HashMap<CustomPortal, HandledIgniteEvents> handlers =
      new HashMap<CustomPortal, HandledIgniteEvents>();

  private ListenerHandler handler;

  public DimensionsIgniteByEvent() {
    super(
        "DimensionsIgniteByEventAddon",
        "3.0.2-BETA",
        "Ignite portals without lighter items",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    // this.pl = pl;

    handler = new ListenerHandler(pl, this);
  }

  /*public DropItem onItemDrop;
  public BlockEvents onBlockBreak;
  public BlockEvents onBlockPlace;
  public EntityDeath onEntityDeath;*/

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    if (!portalConfig.getBoolean("Addon.IgniteByEvent.Enable", false)) return;

    // Events:
    // DropItem:
    //  - 'diamond'
    //  - '%item%'

    //  PlaceBlock/BreakBlock:
    //    - 'stone'
    //    - '%frame%'
    //    - '%inside%'

    //  Death:
    //    - 'PLAYER'

    DropItem onItemDrop =
        new DropItem(portal, portalConfig.getStringList("Addon.IgniteByEvent.Events.DropItem"));
    handlers.put(portal, onItemDrop);
    // setOption(portal, "IngniteOnDrop", onItemDrop);

    //
    //		onBlockBreak = new BlockEvents(portal,
    // portalConfig.getStringList("Addon.IgniteByEvent.Events.BlockBreak"));
    //		setOption(portal, "IngniteOnBlockBreak", onBlockBreak);
    //
    //		onBlockPlace = new BlockEvents(portal,
    // portalConfig.getStringList("Addon.IgniteByEvent.Events.BlockPlace"));
    //		setOption(portal, "IngniteOnBlockPlace", onBlockPlace);

    EntityDeath onEntityDeath =
        new EntityDeath(
            portal, portalConfig.getStringList("Addon.IgniteByEvent.Events.EntityDeath"));
    handlers.put(portal, onEntityDeath);
    // setOption(portal, "IngniteOnDeath", onEntityDeath);

    return;
  }

  public List<HandledIgniteEvents> getHandler(Class<?> cl) {
    return handlers.values().stream().filter(i -> cl.isInstance(i)).collect(Collectors.toList());
  }
}
