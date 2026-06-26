package me.xxastaspastaxx.dimensions.addons.proxy;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class BungeeLocation {

  private String server;
  private String world;
  private double x = 0;
  private double y = 0;
  private double z = 0;

  public BungeeLocation(String server, String world, double x, double y, double z) {
    this.server = server;
    this.world = world;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public BungeeLocation(String server, String world, Vector loc) {
    this(server, world, loc.getX(), loc.getY(), loc.getZ());
  }

  public String getServer() {
    return server;
  }

  public String getWorld() {
    return world;
  }

  public static BungeeLocation parseBungeeLocation(String str) {

    String[] spl = str.split(", ");

    String server = spl[0];
    String world = spl[1];
    double x = Double.parseDouble(spl[2]);
    double y = Double.parseDouble(spl[3]);
    double z = Double.parseDouble(spl[4]);

    return new BungeeLocation(server, world, x, y, z);
  }

  public BungeeLocation clone() {
    return new BungeeLocation(server, world, x, y, z);
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getZ() {
    return z;
  }

  public void setZ(double z) {
    this.z = z;
  }

  public void setServer(String server) {
    this.server = server;
  }

  @Override
  public String toString() {
    return server + ", " + world + ", " + x + ", " + y + ", " + z;
  }

  public Location toLocation(World w) {
    return new Location(w, x, y, z);
  }
}
