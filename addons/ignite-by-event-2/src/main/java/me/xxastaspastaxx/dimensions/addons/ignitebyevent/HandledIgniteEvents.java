package me.xxastaspastaxx.dimensions.addons.ignitebyevent;

import java.util.ArrayList;
import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.completePortal.PortalGeometry;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalIgniteCause;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public abstract class HandledIgniteEvents {

  private CustomPortal portal;

  protected HandledIgniteEvents(CustomPortal portal) {
    this.portal = portal;
  }

  public boolean ignite(Location loc) {

    if (!portal.isAllowedWorld(loc.getWorld())) return false;
    PortalGeometry temp = PortalGeometry.getPortalGeometry(portal).getPortal(portal, loc);
    if (temp == null) return false;

    return Dimensions.getCompletePortalManager()
            .createNew(
                new CompletePortal(portal, loc.getWorld(), temp),
                null,
                CustomPortalIgniteCause.PLUGIN,
                null)
        != null;
  }

  //	DROP_ITEM,
  //	BLOCK_BREAK,
  //	BLOCK_PLACE,
  //	ENTITY_DEATH

  public boolean isAccepted(ItemStack item) {
    return false;
  }

  public boolean isAccepted(Entity item) {
    return false;
  }

  public boolean isAccepted(Block item) {
    return false;
  }
}

class DropItem extends HandledIgniteEvents {

  ArrayList<Material> allowedMaterials = new ArrayList<Material>();

  public DropItem(CustomPortal portal, List<String> stringList) {
    super(portal);

    for (String item : stringList) {
      if (item.contains("%")) {

      } else {
        allowedMaterials.add(Material.valueOf(item.toUpperCase()));
      }
    }
  }

  @Override
  public boolean isAccepted(ItemStack item) {

    return allowedMaterials.contains(item.getType());
  }
}

class BlockEvents extends HandledIgniteEvents {

  ArrayList<Material> allowedMaterials = new ArrayList<Material>();

  public BlockEvents(CustomPortal portal, List<String> stringList) {
    super(portal);

    for (String item : stringList) {
      if (item.contains("%")) {

      } else {
        allowedMaterials.add(Material.valueOf(item.toUpperCase()));
      }
    }
  }

  @Override
  public boolean isAccepted(Block item) {

    return allowedMaterials.contains(item.getType());
  }
}

class EntityDeath extends HandledIgniteEvents {

  ArrayList<EntityType> allowedMaterials = new ArrayList<EntityType>();

  public EntityDeath(CustomPortal portal, List<String> stringList) {
    super(portal);

    for (String item : stringList) {
      if (item.contains("%")) {

      } else {
        allowedMaterials.add(EntityType.valueOf(item.toUpperCase()));
      }
    }
  }

  @Override
  public boolean isAccepted(Entity item) {
    return allowedMaterials.contains(item.getType());
  }
}
