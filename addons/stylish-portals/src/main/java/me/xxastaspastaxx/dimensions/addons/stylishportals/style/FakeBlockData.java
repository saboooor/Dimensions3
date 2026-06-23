package me.xxastaspastaxx.dimensions.addons.stylishportals.style;

import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanic;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import me.xxastaspastaxx.dimensions.DimensionsDebbuger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class FakeBlockData implements InvocationHandler {

  ArrayList<String> allowedMaterial = new ArrayList<String>();

  private NoteBlockMechanic noteBlockMechanic;

  private TypeOfFakeBlockData typeOfFakeBlockData = TypeOfFakeBlockData.VANILLA;

  public static BlockData create(String materialName) {
    FakeBlockData fake = new FakeBlockData(materialName);
    return (BlockData)
        Proxy.newProxyInstance(
            FakeBlockData.class.getClassLoader(), new Class<?>[] {BlockData.class}, fake);
  }

  public static BlockData create(String materialName, int customVariation) {
    FakeBlockData fake = new FakeBlockData(materialName, customVariation);
    return (BlockData)
        Proxy.newProxyInstance(
            FakeBlockData.class.getClassLoader(), new Class<?>[] {BlockData.class}, fake);
  }

  public static FakeBlockData getFake(BlockData data) {
    if (data == null) return null;
    if (Proxy.isProxyClass(data.getClass())) {
      InvocationHandler handler = Proxy.getInvocationHandler(data);
      if (handler instanceof FakeBlockData) {
        return (FakeBlockData) handler;
      }
    }
    return null;
  }

  private FakeBlockData(String materialName) {
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
  private FakeBlockData(String materialName, int customVariation) {
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

  public Material getMaterial() {
    return Arrays.asList(Material.values()).stream()
        .filter(m -> m.isBlock() && isAllowedMaterial(m))
        .findAny()
        .get();
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getName().equals("getMaterial")) {
      return getMaterial();
    }
    if (method.getName().equals("equals")) {
      return proxy == args[0];
    }
    if (method.getName().equals("hashCode")) {
      return System.identityHashCode(proxy);
    }
    // Default fallback to delegate block data (so any other method calls won't crash)
    BlockData delegate = Bukkit.getServer().createBlockData(Material.STONE);
    return method.invoke(delegate, args);
  }
}

enum TypeOfFakeBlockData {
  VANILLA,
  ORAXEN,
  CUSTOM_ITEMS,
  ITEMS_ADDER
}
