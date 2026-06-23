package me.xxastaspastaxx.dimensions.addons.customworlds.generators;

import java.util.HashMap;
import org.bukkit.generator.ChunkGenerator;

public class WorldGenerator extends ChunkGenerator {

  public static WorldGenerator DEFAULT = new DefaultWorldGenerator();

  @SuppressWarnings("serial")
  private static HashMap<String, WorldGenerator> values =
      new HashMap<String, WorldGenerator>() {
        {
          put("DEFAULT", DEFAULT);
        }
      };

  public static WorldGenerator valueOf(String key) {
    return values.get(key.toUpperCase());
  }
}
