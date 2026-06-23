package me.xxastaspastaxx.dimensions.addons.particles;

import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;

public class ParticleSaved {

  Particle valueOf;
  double eval;
  double eval2;
  double eval3;
  int eval4;
  double eval5;
  double eval6;
  double eval7;
  DustOptions dustOptions;
  World world;

  public ParticleSaved(
      Particle valueOf,
      double eval,
      double eval2,
      double eval3,
      int eval4,
      double eval5,
      double eval6,
      double eval7,
      DustOptions dustOptions,
      World world) {
    super();
    this.valueOf = valueOf;
    this.eval = eval;
    this.eval2 = eval2;
    this.eval3 = eval3;
    this.eval4 = eval4;
    this.eval5 = eval5;
    this.eval6 = eval6;
    this.eval7 = eval7;
    this.dustOptions = dustOptions;
    this.world = world;
  }

  public void play() {
    // DimensionsDebbuger.DEBUG.print(eval,eval2,eval3);
    world.spawnParticle(valueOf, eval, eval2, eval3, eval4, eval5, eval6, eval7, dustOptions);
  }
}
