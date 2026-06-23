package me.xxastaspastaxx.dimensions.addons.stylishportals.style;

import java.util.ArrayList;
import java.util.Arrays;
import me.xxastaspastaxx.dimensions.DimensionsDebbuger;
import me.xxastaspastaxx.dimensions.addons.stylishportals.style.customblocks.NoteBlockMechanic;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockSupport;
import org.bukkit.block.data.BlockData;

public class FakeBlockData implements BlockData {

  ArrayList<String> allowedMaterial = new ArrayList<String>();

  private NoteBlockMechanic noteBlockMechanic;

  private TypeOfFakeBlockData typeOfFakeBlockData = TypeOfFakeBlockData.VANILLA;

  public FakeBlockData(String materialName) {
    typeOfFakeBlockData = TypeOfFakeBlockData.VANILLA;
    DimensionsDebbuger.DEBUG.print(materialName);
    materialName = materialName.substring(materialName.indexOf("placeholderblock") + 16);

    if (materialName.indexOf(']') != -1)
      materialName =
          materialName.substring(materialName.indexOf("[") + 1, materialName.lastIndexOf(']'));

    if (materialName.length() == 0) {
      allowedMaterial.add("all");
      allowedMaterial.add("!air");
      allowedMaterial.add("!cave_air");
    } else {
      allowedMaterial.addAll(Arrays.asList(materialName.split(",")));
    }
  }

  // oraxen
  public FakeBlockData(String plugin, int customVariation) {
    typeOfFakeBlockData = TypeOfFakeBlockData.ORAXEN;
    DimensionsDebbuger.DEBUG.print(customVariation);
    materialName = materialName.substring(materialName.indexOf("placeholderblock") + 16);

    if (materialName.indexOf(']') != -1)
      materialName =
          materialName.substring(materialName.indexOf("[") + 1, materialName.lastIndexOf(']'));

    if (materialName.length() == 0) {
      allowedMaterial.add("all");
      allowedMaterial.add("!air");
      allowedMaterial.add("!cave_air");
    } else {
      allowedMaterial.addAll(Arrays.asList(materialName.split(",")));
    }
  }

  public boolean isAllowedMaterial(Material mat) {
    String matName = mat.name().toLowerCase();
    if (allowedMaterial.contains("!" + matName)) return false;
    if (allowedMaterial.contains("all") || allowedMaterial.contains(matName)) return true;

    return false;
  }

  @Override
  public Material getMaterial() {
    return Arrays.asList(Material.values()).stream()
        .filter(m -> m.isBlock() && isAllowedMaterial(m))
        .findAny()
        .get();
  }

  @Override
  public String getAsString() {
    return null;
  }

  @Override
  public String getAsString(boolean hideUnspecified) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BlockData merge(BlockData data) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean matches(BlockData data) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public BlockData clone() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SoundGroup getSoundGroup() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isSupported(Block block) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isSupported(Location location) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isFaceSturdy(BlockFace face, BlockSupport support) {
    // TODO Auto-generated method stub
    return false;
  }
}

enum TypeOfFakeBlockData {
  VANILLA,
  ORAXEN,
  CUSTOM_ITEMS,
  ITEMS_ADDER
}
