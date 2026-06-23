package me.xxastaspastaxx.dimensions.addons.commandsonuse;

import java.util.ArrayList;
import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalIgniteCause;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class DimensionsCommandsOnUse extends DimensionsAddon implements Listener {

  private Plugin pl;
  private boolean placeholderEnabled;

  private ArrayList<CompletePortal> cancelled = new ArrayList<CompletePortal>();

  public DimensionsCommandsOnUse() {
    super(
        "DimensionsCommandsOnUseAddon",
        "3.0.4",
        "Execute commands when using a portal",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions main) {
    this.pl = main;

    placeholderEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onPortalIgnite(CustomPortalIgniteEvent e) {
    CompletePortal complete = e.getCompletePortal();
    if (getOption(complete, "disableTp") != null
        && e.getCause() == CustomPortalIgniteCause.EXIT_PORTAL) {
      e.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
  public void onPortalUse(CustomPortalUseEvent e) {
    CompletePortal complete = e.getCompletePortal();
    if (getOption(complete, "disableTp") != null) {
      cancelled.add(complete);
      e.setDestinationPortal(null);

      complete.setTag("disableTP", true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void postPortalUse(CustomPortalUseEvent e) {

    CompletePortal complete = e.getCompletePortal();

    if (cancelled.contains(complete)) {
      complete.pushToHold(e.getEntity());
      cancelled.remove(complete);
    }

    CustomPortal portal = complete.getCustomPortal();

    Location loc = complete.getCenter();
    Location newLoc =
        (e.getDestinationPortal() == null)
            ? complete.getDestinationLocation(null, null)
            : e.getDestinationPortal().getCenter();

    Object commandsOBJ = getOption(complete, "commandsOnUse");
    if (commandsOBJ == null) return;

    Entity entity = e.getEntity();

    @SuppressWarnings("unchecked")
    List<String> commands = (List<String>) commandsOBJ;

    for (String str : commands) {
      str =
          str.replace("%portal%", portal.getDisplayName())
              .replace("%name%", portal.getPortalId())
              .replace("%originalWorld%", loc.getWorld().getName())
              .replace("%originalX%", loc.getX() + "")
              .replace("%originalY%", loc.getY() + "")
              .replace("%originalZ%", loc.getZ() + "")
              .replace("%zAxis%", complete.getPortalGeometry().iszAxis() + "")
              .replace(
                  "%entity%",
                  (entity instanceof Player)
                      ? entity.getName()
                      : entity
                          .getType()
                          .name()
                          .toLowerCase()
                          .replace("_", " ")
                          .replace("%entityID%", entity.getUniqueId().toString()));
      if (newLoc != null)
        str =
            str.replace("%destinationWorld%", newLoc.getWorld().getName())
                .replace("%destinationX%", newLoc.getX() + "")
                .replace("%destinationY%", newLoc.getY() + "")
                .replace("%destinationZ%", newLoc.getZ() + "");

      if (entity instanceof Player && placeholderEnabled)
        str = PlaceholderAPI.setPlaceholders((Player) entity, str);

      String[] spl = str.split(";");
      String sender = spl[0];
      String precond = spl[1];

      if (!eval(precond)) {
        continue;
      }

      String cmd = str.substring(sender.length() + precond.length() + 2);

      if (sender.contentEquals("console")) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
      } else if (sender.contentEquals("player") && entity instanceof Player) {
        ((Player) entity).performCommand(cmd);
      }
    }
  }

  /*PARSING COMMANDS*/
  public boolean eval(String cond) {
    if (cond.equals("true")) return true;
    cond = "(" + cond + ")";

    while (cond.contains("(")) {
      int start = cond.lastIndexOf("(");
      int end = cond.indexOf(")", start);

      String newCond = cond.substring(start + 1, end);
      cond = cond.replace("(" + newCond + ")", getParenthesis(newCond));
    }

    return cond.equals("true");
  }

  // 1==1 && 2=3 || 5=1
  private String getParenthesis(String cond) {
    boolean res = false;
    for (String s : cond.split(" \\|\\| ")) {
      boolean t = true;
      for (String s2 : s.split(" && ")) {
        t = t && equals(s2);
      }
      res = res || t;
    }

    return res + "";
  }

  private boolean equals(String cond) {
    if (cond.equals("true")) return true;

    if (cond.contains("==")) {
      String[] spl = cond.split("==");

      if (calculate(spl[0]) == calculate(spl[1]) && calculate(spl[0]) != Double.MAX_VALUE)
        return true;
      if (spl[0].equals(spl[1])) return true;
    } else if (cond.contains("!=")) {
      String[] spl = cond.split("!=");

      if (calculate(spl[0]) != calculate(spl[1]) && calculate(spl[0]) != Double.MAX_VALUE)
        return true;
      if (!spl[0].equals(spl[1])) return true;
    } else if (cond.contains("<=")) {
      String[] spl = cond.split("<=");

      if (calculate(spl[0]) <= calculate(spl[1])) return true;
    } else if (cond.contains("<")) {
      String[] spl = cond.split("<");

      if (calculate(spl[0]) < calculate(spl[1])) return true;
    } else if (cond.contains(">=")) {
      String[] spl = cond.split(">=");

      if (calculate(spl[0]) >= calculate(spl[1])) return true;
    } else if (cond.contains(">")) {
      String[] spl = cond.split(">");

      if (calculate(spl[0]) > calculate(spl[1])) return true;
    }
    return false;
  }

  public static double calculate(final String str) {
    try {
      return new Object() {
        int pos = -1, ch;

        void nextChar() {
          ch = (++pos < str.length()) ? str.charAt(pos) : -1;
        }

        boolean eat(int charToEat) {
          while (ch == ' ') nextChar();
          if (ch == charToEat) {
            nextChar();
            return true;
          }
          return false;
        }

        double parse() {
          nextChar();
          double x = parseExpression();
          if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
          return x;
        }

        double parseExpression() {
          double x = parseTerm();
          for (; ; ) {
            if (eat('+')) x += parseTerm(); // addition
            else if (eat('-')) x -= parseTerm(); // subtraction
            else return x;
          }
        }

        double parseTerm() {
          double x = parseFactor();
          for (; ; ) {
            if (eat('*')) x *= parseFactor(); // multiplication
            else if (eat('/')) x /= parseFactor(); // division
            else return x;
          }
        }

        double parseFactor() {
          if (eat('+')) return parseFactor(); // unary plus
          if (eat('-')) return -parseFactor(); // unary minus

          double x;
          int startPos = this.pos;
          if (eat('(')) { // parentheses
            x = parseExpression();
            eat(')');
          } else if ((ch >= '0' && ch <= '9') || ch == '.' || ch == 'E') { // numbers
            while ((ch >= '0' && ch <= '9') || ch == '.' || ch == 'E') nextChar();
            x = Double.parseDouble(str.substring(startPos, this.pos));
          } else if (ch >= 'a' && ch <= 'z') { // functions
            while (ch >= 'a' && ch <= 'z') nextChar();
            String func = str.substring(startPos, this.pos);
            x = parseFactor();
            if (func.equals("sqrt")) x = Math.sqrt(x);
            else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
            else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
            else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
            else throw new RuntimeException("Unknown function: " + func);
          } else {
            throw new RuntimeException("Unexpected: " + (char) ch);
          }

          if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

          return x;
        }
      }.parse();
    } catch (Exception e) {
      return Double.MAX_VALUE;
    }
  }

  /*PARSING COMMANDS*/

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    List<String> strs = portalConfig.getStringList("Addon.Commands");

    if (strs.size() == 0) return;

    if (portalConfig.getBoolean("Addon.DisableTeleport", false)) {
      setOption(portal, "disableTp", true);
    }
    setOption(portal, "commandsOnUse", strs);
  }
}
