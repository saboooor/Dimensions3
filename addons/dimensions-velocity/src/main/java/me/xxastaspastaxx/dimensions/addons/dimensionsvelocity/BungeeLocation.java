package me.xxastaspastaxx.dimensions.addons.dimensionsvelocity;

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

  public String getServer() {
    return server;
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

  public boolean isEqual(BungeeLocation loc) {
    return server.equals(loc.server)
        && this.world.equals(loc.world)
        && x == loc.x
        && y == loc.y
        && z == loc.z;
  }

  @Override
  public String toString() {
    return server + ", " + world + ", " + x + ", " + y + ", " + z;
  }
}
