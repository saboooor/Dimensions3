package me.xxastaspastaxx.dimensions.customportal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.xxastaspastaxx.dimensions.AxisOrFace;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.completePortal.PortalGeometry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Axis;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

/** Loads all the custom portals */
public class CustomPortalLoader {

  public static final String DIRECTORY_PATH = "./plugins/Dimensions/Portals";
  public static final File PORTALS_DIRECTORY = new File(DIRECTORY_PATH);
  public static final String CONFIG_VERSION = "4.0.0";

  private static Class<?> craftBlockDataClass;
  private static Method getStateMethod;
  private static Class<?> blockClass;
  private static Method getCombinedIdMethod;

  static {
    try {
      try {
        craftBlockDataClass = Class.forName("org.bukkit.craftbukkit.block.data.CraftBlockData");
      } catch (ClassNotFoundException e) {
        String version =
            org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        craftBlockDataClass =
            Class.forName("org.bukkit.craftbukkit." + version + ".block.data.CraftBlockData");
      }
      getStateMethod = craftBlockDataClass.getMethod("getState");

      try {
        blockClass = Class.forName("net.minecraft.world.level.block.Block");
      } catch (ClassNotFoundException e) {
        try {
          blockClass = Class.forName("net.minecraft.world.item.Block");
        } catch (ClassNotFoundException e2) {
          String version =
              org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
          blockClass = Class.forName("net.minecraft.server." + version + ".Block");
        }
      }

      Class<?> blockStateClass;
      try {
        blockStateClass = Class.forName("net.minecraft.world.level.block.state.BlockState");
      } catch (ClassNotFoundException e) {
        try {
          blockStateClass = Class.forName("net.minecraft.world.level.block.state.IBlockData");
        } catch (ClassNotFoundException e2) {
          try {
            String version =
                org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            blockStateClass = Class.forName("net.minecraft.server." + version + ".IBlockData");
          } catch (ClassNotFoundException e3) {
            blockStateClass = getStateMethod.getReturnType();
          }
        }
      }

      for (String methodName : new String[] {"getCombinedId", "i", "j", "m"}) {
        try {
          getCombinedIdMethod = blockClass.getMethod(methodName, blockStateClass);
          break;
        } catch (NoSuchMethodException ignored) {
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Cunstructor of the loader */
  public CustomPortalLoader() {}

  /** Load all the custom portals */
  public ArrayList<CustomPortal> loadAll() {
    ArrayList<CustomPortal> res = new ArrayList<CustomPortal>();

    File portalFolder = new File(DIRECTORY_PATH);
    if (!portalFolder.exists()) {
      portalFolder.mkdir();
    }

    File[] files = PORTALS_DIRECTORY.listFiles();
    if (files == null || files.length == 0) {
      Dimensions main = Dimensions.getInstance();
      if (main != null) {
        main.saveResource("Portals/aether.yml", false);
        main.saveResource("Portals/twilight_forest.yml", false);
      }
    }

    PortalGeometry.instance = PortalGeometry.nullGeometry();

    for (File f : PORTALS_DIRECTORY.listFiles()) {
      String portalID = f.getName().replace(".yml", "");
      if (portalID.contains(" ")) continue;

      YamlConfiguration portalConfig = YamlConfiguration.loadConfiguration(f);

      String fVersion = portalConfig.getString("configVersion", "pre3");
      boolean commentsUpdated = updateComments(portalConfig);
      boolean versionMismatch = !fVersion.equals(CONFIG_VERSION);

      if (versionMismatch) {

        if (portalConfig.contains("Options.BuildExitPortal")) {
          portalConfig.set(
              "Options.ExitPortal.Enable", portalConfig.getBoolean("Options.BuildExitPortal"));
        } else {
          portalConfig.set("Options.ExitPortal.Enable", true);
        }

        portalConfig.set("Options.BuildExitPortal", null);

        portalConfig.set("Options.ExitPortal.FixedWidth", -1);
        portalConfig.set("Options.ExitPortal.FixedHeight", -1);

        portalConfig.set("configVersion", CONFIG_VERSION);
      }

      boolean migrated = false;
      if (portalConfig.contains("Addon.LightAPI.Level")) {
        portalConfig.set("Options.LightLevel", portalConfig.getInt("Addon.LightAPI.Level"));
        portalConfig.set("Addon.LightAPI", null);
        migrated = true;
      }

      if (versionMismatch || commentsUpdated || migrated) {
        try {
          portalConfig.save(f);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      boolean enabled = portalConfig.getBoolean("Enable", false);
      String displayName = portalConfig.getString("DisplayName", "Unnamed");

      Material outsideMaterial =
          Material.matchMaterial(portalConfig.getString("Portal.Frame.Material", "COBBLESTONE"));
      AxisOrFace outsideBlockDir =
          new AxisOrFace(portalConfig.getString("Portal.Frame.Face", "all"));

      Material insideMaterial =
          Material.matchMaterial(portalConfig.getString("Portal.InsideMaterial", "NETHER_PORTAL"));
      Component insideSprite =
          portalConfig.getString("Portal.InsideSprite") == null
              ? null
              : MiniMessage.miniMessage()
                  .deserialize(portalConfig.getString("Portal.InsideSprite"));

      //			BlockData[] insideBlockData = new BlockData[] {getInsideBlockData(false,
      // tempBlockData),getInsideBlockData(true, tempBlockData)};
      //			int[] combinedId = createCombinedID(insideBlockData, insideMaterial);

      String ligherMaterialString =
          portalConfig.getString("Portal.LighterMaterial", "FLINT_AND_STEEL");
      Material lighterMaterial =
          ligherMaterialString.equalsIgnoreCase("null")
              ? null
              : Material.matchMaterial(ligherMaterialString);
      String[] particlesColorString =
          portalConfig.getString("Portal.ParticlesColor", "0;0;0").split(";");
      Color particlesColor =
          Color.fromBGR(
              Integer.parseInt(particlesColorString[2]),
              Integer.parseInt(particlesColorString[1]),
              Integer.parseInt(particlesColorString[0]));

      Sound breakEffect =
          Sound.valueOf(portalConfig.getString("Portal.BreakEffect", "BLOCK_GLASS_BREAK"));

      int minimumHeight = portalConfig.getInt("Portal.MinimumHeight", 4);
      int maximumHeight = portalConfig.getInt("Portal.MaximumHeight", 15);

      int maximumWidth = portalConfig.getInt("Portal.MaximumWidth", 14);
      int minimumWidth = portalConfig.getInt("Portal.MinimumWidth", 3);

      String worldName = portalConfig.getString("World.Name", "world");
      /*if (DimensionsSettings.generateNewWorlds && !Bukkit.getServer().getWorlds().contains(Bukkit.getWorld(worldName))) {
      	Bukkit.getServer().createWorld(new WorldCreator(worldName));
      }*/

      //			String[] ratioString = portalConfig.getString("World.Ratio", "1:1").split(":");
      //			int ratio0 = Integer.parseInt(ratioString[0]);
      //			int ratio1 = Integer.parseInt(ratioString[1]);
      //			int ratio = ratio1/ratio0;

      List<String> allowedWorlds = portalConfig.getStringList("Options.AllowedWorlds");
      if (allowedWorlds.size() == 0) allowedWorlds.add("all");

      boolean buildExitPortal = portalConfig.getBoolean("Options.ExitPortal.Enable", true);
      int fixedExitPortalWidth = portalConfig.getInt("Options.ExitPortal.FixedWidth", -1);
      int fixedExitPortalHeight = portalConfig.getInt("Options.ExitPortal.FixedHeight", -1);

      int teleportDelay = portalConfig.getInt("Options.TeleportDelay", 4);
      boolean enableParticles = portalConfig.getBoolean("Options.EnableParticles", true);
      int lightLevel = portalConfig.getInt("Options.LightLevel", 0);

      HashMap<EntityType, EntityType> entityTransformation = new HashMap<EntityType, EntityType>();
      for (String entity : portalConfig.getStringList("Entities.Transformation")) {
        String[] spl = entity.toUpperCase().split("->");
        entityTransformation.put(EntityType.valueOf(spl[0]), EntityType.valueOf(spl[1]));
      }

      String s = portalConfig.getString("Entities.Spawning.Delay", "5000-10000");
      int[] spawningDelay = new int[2];
      if (s.contains("-")) {
        String[] spawningDelayString = s.split("-");
        spawningDelay =
            new int[] {
              Integer.parseInt(spawningDelayString[0]), Integer.parseInt(spawningDelayString[1])
            };
      } else {
        int delay = Integer.parseInt(s);
        spawningDelay = new int[] {delay, delay};
      }
      HashMap<EntityType, Integer> entitySpawning = new HashMap<EntityType, Integer>();
      for (String entity : portalConfig.getStringList("Entities.Spawning.List")) {
        String[] spl = entity.toUpperCase().split(";");
        entitySpawning.put(EntityType.valueOf(spl[0]), Integer.parseInt(spl[1]));
      }
      CustomPortal portal =
          new CustomPortal(
              portalID,
              displayName,
              enabled,
              outsideMaterial,
              outsideBlockDir,
              insideMaterial,
              insideSprite,
              lighterMaterial,
              particlesColor,
              breakEffect,
              minimumHeight,
              maximumHeight,
              maximumWidth,
              minimumWidth,
              worldName,
              buildExitPortal,
              fixedExitPortalWidth,
              fixedExitPortalHeight,
              allowedWorlds,
              teleportDelay,
              enableParticles,
              lightLevel,
              entityTransformation,
              spawningDelay[0],
              spawningDelay[1],
              entitySpawning);
      if (insideMaterial != null) portal.setInsideBlockData(insideMaterial.createBlockData());
      for (DimensionsAddon addon : Dimensions.getAddonManager().getAddons()) {
        addon.registerPortal(portalConfig, portal);
      }
      res.add(portal);
    }

    return res;
  }

  /**
   * Creates combinedID for the block data inside the portal
   *
   * @param insideBlockData
   * @param insideMaterial
   * @return
   */
  public static int[] createCombinedID(BlockData[] insideBlockData, Material insideMaterial) {
    int combinedId[] = new int[2];
    if (insideMaterial.isSolid()
        || insideMaterial == Material.NETHER_PORTAL
        || insideMaterial == Material.END_GATEWAY) {
      if (getStateMethod != null && getCombinedIdMethod != null)
        try {
          Object nmsBlockData = getStateMethod.invoke(insideBlockData[0]);
          combinedId[0] = (int) getCombinedIdMethod.invoke(null, nmsBlockData);

          nmsBlockData = getStateMethod.invoke(insideBlockData[1]);
          combinedId[1] = (int) getCombinedIdMethod.invoke(null, nmsBlockData);
        } catch (Exception e1) {
          e1.printStackTrace();
        }
    }
    return combinedId;
  }

  /**
   * Creates BlockData in the correct Z Axis
   *
   * @param zAxis
   * @param blockData
   * @return
   */
  public static BlockData getInsideBlockData(boolean zAxis, BlockData blockData) {
    if (zAxis) {
      if (blockData instanceof Orientable) {
        Orientable orientable = (Orientable) blockData;
        orientable.setAxis(Axis.Z);
        blockData = orientable;
      } else if (blockData instanceof Directional) {
        Directional directional = (Directional) blockData;
        directional.setFacing(BlockFace.NORTH);
        blockData = directional;
      } else if (blockData instanceof MultipleFacing) {
        MultipleFacing face = (MultipleFacing) blockData;
        face.setFace(BlockFace.NORTH, true);
        face.setFace(BlockFace.SOUTH, true);
        blockData = face;
      }
    } else {
      if (blockData instanceof MultipleFacing) {
        MultipleFacing face = (MultipleFacing) blockData;
        face.setFace(BlockFace.EAST, true);
        face.setFace(BlockFace.WEST, true);
        blockData = face;
      }
    }

    return blockData;
  }

  private boolean updateComments(YamlConfiguration config) {
    boolean modified = false;
    HashMap<String, List<String>> commentsMap = new HashMap<>();
    commentsMap.put("configVersion", Arrays.asList("Config version for version control"));
    commentsMap.put("Enable", Arrays.asList("Enable or disable this custom portal"));
    commentsMap.put("DisplayName", Arrays.asList("The display name of the custom portal"));
    commentsMap.put(
        "Portal.Frame.Material", Arrays.asList("The block material used for the portal frame"));
    commentsMap.put(
        "Portal.Frame.Face",
        Arrays.asList("The block face direction or orientation restrictions for the frame"));
    commentsMap.put(
        "Portal.InsideMaterial",
        Arrays.asList("The block material inside the portal when ignited"));
    commentsMap.put(
        "Portal.InsideSprite",
        Arrays.asList("Optional MiniMessage-formatted sprite component for the portal inside"));
    commentsMap.put(
        "Portal.LighterMaterial",
        Arrays.asList("The item material used to ignite the portal (e.g. FLINT_AND_STEEL)"));
    commentsMap.put(
        "Portal.ParticlesColor", Arrays.asList("The color of portal particles (format: R;G;B)"));
    commentsMap.put("Portal.BreakEffect", Arrays.asList("The sound played when the portal breaks"));
    commentsMap.put(
        "Portal.MinimumHeight",
        Arrays.asList("The minimum height of the portal frame (including frame blocks)"));
    commentsMap.put(
        "Portal.MaximumHeight",
        Arrays.asList("The maximum height of the portal frame (including frame blocks)"));
    commentsMap.put(
        "Portal.MaximumWidth",
        Arrays.asList("The maximum width of the portal frame (including frame blocks)"));
    commentsMap.put(
        "Portal.MinimumWidth",
        Arrays.asList("The minimum width of the portal frame (including frame blocks)"));
    commentsMap.put(
        "World.Name", Arrays.asList("The destination world name that players teleport to"));
    commentsMap.put(
        "Options.AllowedWorlds",
        Arrays.asList(
            "List of worlds from which players are allowed to use this portal (use 'all' for all"
                + " worlds)"));
    commentsMap.put(
        "Options.ExitPortal.Enable",
        Arrays.asList("Whether to automatically build an exit portal in the destination world"));
    commentsMap.put(
        "Options.ExitPortal.FixedWidth",
        Arrays.asList(
            "The fixed width of the generated exit portal (-1 to match incoming portal width)"));
    commentsMap.put(
        "Options.ExitPortal.FixedHeight",
        Arrays.asList(
            "The fixed height of the generated exit portal (-1 to match incoming portal height)"));
    commentsMap.put(
        "Options.TeleportDelay",
        Arrays.asList("The teleport delay in seconds when standing inside the portal"));
    commentsMap.put(
        "Options.EnableParticles",
        Arrays.asList("Whether to enable particle effects for this portal"));
    commentsMap.put(
        "Options.LightLevel",
        Arrays.asList("The light level emitted by the portal (0 to disable)"));
    commentsMap.put(
        "Entities.Transformation",
        Arrays.asList(
            "List of entity transformations when they pass through the portal (format:"
                + " SOURCE_ENTITY->TARGET_ENTITY)"));
    commentsMap.put(
        "Entities.Spawning.Delay",
        Arrays.asList(
            "The delay range in ticks between entity spawns from the portal (format: min-max or"
                + " single value)"));
    commentsMap.put(
        "Entities.Spawning.List",
        Arrays.asList(
            "List of entities that can spawn from the portal and their weight/chance (format:"
                + " ENTITY;WEIGHT)"));

    List<String> header =
        Arrays.asList(
            "Custom Portal Configuration File",
            "Configure frame materials, inside materials, particle effects, and more.");
    if (!header.equals(config.options().getHeader())) {
      config.options().setHeader(header);
      modified = true;
    }

    for (Map.Entry<String, List<String>> entry : commentsMap.entrySet()) {
      String path = entry.getKey();
      if (config.contains(path)) {
        List<String> currentComments = config.getComments(path);
        if (!entry.getValue().equals(currentComments)) {
          config.setComments(path, entry.getValue());
          modified = true;
        }
      }
    }
    return modified;
  }
}
