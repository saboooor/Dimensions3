package me.xxastaspastaxx.dimensions.customportal;

import java.util.ArrayList;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.settings.Oogabooga;

/** Manages all custom portals */
public class CustomPortalManager {

  Dimensions pl;

  ArrayList<CustomPortal> customPortals = new ArrayList<CustomPortal>();

  /**
   * Constructor of the manager
   *
   * @param pl
   */
  public CustomPortalManager(Dimensions pl) {
    this.pl = pl;

    ArrayList<CustomPortal> loaded = (new CustomPortalLoader()).loadAll();
    customPortals.addAll(loaded);

    // customPortals.add(new CustomPortal("test", "", true,
    // Material.DIAMOND_BLOCK.createBlockData(),
    //		Material.BLACK_STAINED_GLASS_PANE.createBlockData(), new
    // ItemStack(Material.FLINT_AND_STEEL), Sound.BLOCK_GLASS_BREAK, (byte) 4,
    //		(byte) 15, (byte) 14, (byte) 3, "world_nether", (float) 1, (byte)  255, (byte) 1, true,
    // false, new ArrayList<String>(), new HashMap<EntityType, EntityType>(),
    //		10000, 10001, new HashMap<EntityType, Byte>()));
  }

  /**
   * Get all the custom portals registered
   *
   * @return
   */
  public ArrayList<CustomPortal> getCustomPortals() {
    return customPortals;
  }

  /**
   * Get a custom portal by name
   *
   * @param name the name of the portal
   * @return
   */
  public CustomPortal getCustomPortal(String name) {
    for (CustomPortal portal : customPortals) {
      if (portal.getPortalId().contentEquals(name)) return portal;
    }
    return null;
  }

  /** Reload all custom portals */
  public void reload() {
    customPortals.clear();
    ArrayList<CustomPortal> loaded = (new CustomPortalLoader()).loadAll();
    if (Oogabooga.oogabooga() && loaded.size() > 2) {
      me.xxastaspastaxx.dimensions.DimensionsDebbuger.VERY_LOW.print(
          Oogabooga.boogaooga() + ": Limiting custom portal configs to 2.");
      customPortals.addAll(loaded.subList(0, 2));
    } else {
      customPortals.addAll(loaded);
    }
  }
}
