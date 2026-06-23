package me.xxastaspastaxx.dimensions.addons.dimensionsportaltp;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;

public class DimensionsPortalTp extends DimensionsAddon {

  // private Plugin pl;

  public DimensionsPortalTp() {
    super("DimensionsPortalTp", "1.0.0", "Tp to nearest portal", DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    // this.pl = pl;

    Dimensions.getCommandManager()
        .registerCommand(
            new TpToClosestCommand(
                "tpToClosest",
                "<portalName> [player]",
                new String[0],
                "Teleport player to nearest portal",
                "none",
                true,
                this));
  }
}
