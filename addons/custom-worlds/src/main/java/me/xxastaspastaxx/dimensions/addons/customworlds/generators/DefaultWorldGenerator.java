package me.xxastaspastaxx.dimensions.addons.customworlds.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import me.xxastaspastaxx.dimensions.addons.customworlds.FastNoiseLite;
import org.bukkit.Material;
import org.bukkit.generator.WorldInfo;

public class DefaultWorldGenerator extends WorldGenerator {
  private final FastNoiseLite terrainNoise = new FastNoiseLite();
  private final FastNoiseLite detailNoise = new FastNoiseLite();

  @SuppressWarnings("serial")
  private final HashMap<Integer, List<Material>> layers =
      new HashMap<Integer, List<Material>>() {
        {
          put(0, Arrays.asList(Material.GRASS_BLOCK));
          put(
              1,
              Arrays.asList(Material.DIRT, Material.COARSE_DIRT, Material.SAND, Material.GRAVEL));
          put(
              2,
              Arrays.asList(
                  Material.COAL_ORE,
                  Material.IRON_ORE,
                  Material.REDSTONE_ORE,
                  Material.LAPIS_ORE,
                  Material.GOLD_ORE,
                  Material.DIAMOND_ORE));
          put(3, Arrays.asList(Material.BEDROCK));
        }
      };

  public DefaultWorldGenerator() {
    // Set frequencies
    terrainNoise.SetFrequency(0.001f);
    detailNoise.SetFrequency(0.05f);

    // Add fractals
    terrainNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
    terrainNoise.SetFractalOctaves(5);
  }

  @Override
  public void generateNoise(
      WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
    for (int y = chunkData.getMinHeight(); y < 130 && y < chunkData.getMaxHeight(); y++) {
      for (int x = 0; x < 16; x++) {
        for (int z = 0; z < 16; z++) {
          float noise2 =
              (terrainNoise.GetNoise(x + (chunkX * 16), z + (chunkZ * 16)) * 2)
                  + (detailNoise.GetNoise(x + (chunkX * 16), z + (chunkZ * 16)) / 10);
          float noise3 = detailNoise.GetNoise(x + (chunkX * 16), y, z + (chunkZ * 16));
          float currentY = (65 + (noise2 * 30));

          if (y < 1) {
            chunkData.setBlock(x, y, z, layers.get(3).get(random.nextInt(layers.get(3).size())));
          } else if (y < currentY) {
            float distanceToSurface =
                Math.abs(y - currentY); // The absolute y distance to the world surface.
            double function =
                .1 * Math.pow(distanceToSurface, 2)
                    - 1; // A second grade polynomial offset to the noise max and min (1, -1).

            if (noise3 > Math.min(function, -.3)) {
              // Set grass if the block closest to the surface.
              if (distanceToSurface < 1 && y > 63) {
                chunkData.setBlock(x, y, z, layers.get(0).get(0));
              }

              // It is not the closest block to the surface but still very close.
              else if (distanceToSurface < 5) {
                chunkData.setBlock(
                    x, y, z, layers.get(1).get(random.nextInt(layers.get(1).size())));
              }

              // Not close to the surface at all.
              else {
                Material neighbour = Material.STONE;
                List<Material> neighbourBlocks =
                    new ArrayList<Material>(
                        Arrays.asList(
                            chunkData.getType(Math.max(x - 1, 0), y, z),
                            chunkData.getType(x, Math.max(y - 1, 0), z),
                            chunkData.getType(
                                x, y, Math.max(z - 1, 0)))); // A list of all neighbour blocks.

                // Randomly place vein anchors.
                if (random.nextFloat() < 0.002) {
                  neighbour =
                      layers
                          .get(2)
                          .get(
                              Math.min(
                                  random.nextInt(layers.get(2).size()),
                                  random.nextInt(
                                      layers
                                          .get(2)
                                          .size()))); // A basic way to shift probability to lower
                  // values.
                }

                // If the current block has an ore block as neighbour, try the current block.
                if ((!Collections.disjoint(neighbourBlocks, layers.get(2)))) {
                  for (Material neighbourBlock : neighbourBlocks) {
                    if (layers.get(2).contains(neighbourBlock)
                        && random.nextFloat()
                            < -0.01 * layers.get(2).indexOf(neighbourBlock) + 0.4) {
                      neighbour = neighbourBlock;
                    }
                  }
                }

                chunkData.setBlock(x, y, z, neighbour);
              }
            }
          } else if (y < 62) {
            chunkData.setBlock(x, y, z, Material.WATER);
          }
        }
      }
    }
  }
}
