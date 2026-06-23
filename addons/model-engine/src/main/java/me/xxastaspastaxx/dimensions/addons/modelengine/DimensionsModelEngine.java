package me.xxastaspastaxx.dimensions.addons.modelengine;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.model.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsDebbuger;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.completePortal.PortalGeometry;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.events.CustomPortalBreakEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class DimensionsModelEngine extends DimensionsAddon implements Listener {

  // private Plugin pl;
  private boolean placeholderEnabled;

  private HashMap<CompletePortal, ArrayList<UUID>> entities =
      new HashMap<CompletePortal, ArrayList<UUID>>();

  public DimensionsModelEngine() {
    super(
        "DimensionsModelEngineAddon", "3.0.1", "Model engine hook", DimensionsAddonPriority.NORMAL);
  }

  @Override
  public boolean onLoad(Dimensions main) {
    // this.pl = main;

    Plugin modelEnginePlugin = Bukkit.getPluginManager().getPlugin("ModelEngine" /*+"_Beta"*/);
    if (modelEnginePlugin != null) {
      return true;
    }

    return false;
  }

  @Override
  public void onEnable(Dimensions pl) {

    placeholderEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @Override
  public void onDisable() {
    /*MEG 2.5.0 entities.values().forEach(arr -> arr.forEach(en -> {try {ModeledEntity m = ModelEngineAPI.getModeledEntity(en);
    m.clearModels();
    Bukkit.getEntity(en).remove();} catch(Exception ex) {}}));*/

    entities
        .values()
        .forEach(
            arr ->
                arr.forEach(
                    en -> {
                      ModeledEntity m = ModelEngineAPI.getModeledEntity(en);
                      m.destroy();
                      m.getBase().broadcastDespawnPacket();
                      Bukkit.getEntity(en).remove();
                    }));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postPortalBreak(CustomPortalBreakEvent e) {
    CompletePortal complete = e.getCompletePortal();

    if (entities.containsKey(complete))
      entities
          .remove(complete)
          .forEach(
              en -> {
                ModeledEntity m = ModelEngineAPI.getModeledEntity(en);
                m.destroy();
                m.getBase().broadcastDespawnPacket();
                Bukkit.getEntity(en).remove();
              });
    /* MEG 2.5.0 entities.remove(complete).forEach(en -> {try {ModeledEntity m = ModelEngineAPI.getModeledEntity(en);
    m.clearModels();
    Bukkit.getEntity(en).remove();} catch(Exception ex) {}});*/
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onPortalUse(CustomPortalUseEvent e) {
    CompletePortal complete = e.getCompletePortal();
    if (getOption(complete, "modelEngineList") == null) return;

    if (e.getEntity() == null || e.getEntity().getCustomName() == null) return;
    e.setCancelled(e.getEntity().getCustomName().equals("DIMENSIONS"));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postPortalIgnite(CustomPortalIgniteEvent e) {
    CompletePortal complete = e.getCompletePortal();

    Object listObj = getOption(complete, "modelEngineList");
    if (listObj == null) return;
    @SuppressWarnings("unchecked")
    ArrayList<String> list = (ArrayList<String>) listObj;

    PortalGeometry geom = complete.getPortalGeometry();
    Vector min = geom.getInsideMin().clone().add(new Vector(0.5, 0.5, 0.5));
    Vector max = geom.getInsideMax().clone().add(new Vector(0.5, 0.5, 0.5));

    Entity player = e.getEntity();

    Location newLoc = complete.getDestinationLocation(null, null);

    ArrayList<UUID> enList = new ArrayList<UUID>();

    for (double x = min.getX(); x <= max.getX(); x++) {
      for (double y = min.getY(); y <= max.getY(); y++) {
        for (double z = min.getZ(); z <= max.getZ(); z++) {
          Location loc = new Location(complete.getWorld(), x, y, z);

          for (String str : list) {
            str =
                str.replace("%originalWorld%", loc.getWorld().getName())
                    .replace("%x%", x + "")
                    .replace("%y%", y + "")
                    .replace("%z%", z + "")
                    .replace("%zAxis%", complete.getPortalGeometry().iszAxis() + "")
                    .replace(
                        "%entity%",
                        (player == null
                            ? "console"
                            : ((player instanceof Player)
                                ? player.getName()
                                : player.getType().name().toLowerCase().replace("_", " "))))
                    .replace("%minX%", min.getX() + "")
                    .replace("%maxX%", max.getX() + "")
                    .replace("%minY%", min.getY() + "")
                    .replace("%maxY%", max.getY() + "")
                    .replace("%minZ%", min.getZ() + "")
                    .replace("%maxZ%", max.getZ() + "")
                    .replace("%portalWidth%", geom.getPortalWidth() + "")
                    .replace("%portalHeight%", geom.getPortalHeight() + "")
                    .replace("%centerX%", geom.getCenter().getX() + "")
                    .replace("%centerY%", geom.getCenter().getY() + "")
                    .replace("%centerZ%", geom.getCenter().getZ() + "");
            if (newLoc != null)
              str =
                  str.replace("%destinationWorld%", newLoc.getWorld().getName())
                      .replace("%destinationX%", newLoc.getX() + "")
                      .replace("%destinationY%", newLoc.getY() + "")
                      .replace("%destinationZ%", newLoc.getZ() + "");

            if (player instanceof Player && placeholderEnabled)
              str = PlaceholderAPI.setPlaceholders((Player) player, str);

            String[] spl = str.split(";");
            String precond = spl[0];
            String modelName = spl[1];
            String newPos = spl[2];

            if (modelName.contentEquals("DEBBUGER")) {
              DimensionsDebbuger.HIGH.print(precond + " == " + eval(precond));
              continue;
            }

            if (precond.equalsIgnoreCase("once")) {
              if (x != min.getX() || y != min.getY() || z != min.getZ()) continue;
            } else if (!eval(precond)) {
              // DimensionsDebbuger.DEBUG.print("FAILED: "+precond);
              continue;
            }
            // DimensionsDebbuger.DEBUG.print("SUCCESS: "+precond);

            String[] newPosSpl = newPos.split(",");
            Location overrideLoc = loc.clone();
            overrideLoc.setX(calculate(newPosSpl[0]));
            overrideLoc.setY(calculate(newPosSpl[1]));
            overrideLoc.setZ(calculate(newPosSpl[2]));
            overrideLoc.setYaw((float) calculate(newPosSpl[3]));

            LivingEntity mob =
                (LivingEntity) complete.getWorld().spawnEntity(overrideLoc, EntityType.ARMOR_STAND);
            mob.setCustomName("DIMENSIONS");
            mob.setCustomNameVisible(false);
            mob.setGravity(false);
            mob.setAI(false);
            mob.setInvulnerable(true);
            mob.setCollidable(false);
            mob.setInvisible(true);

            /*MEG 2.5.0 ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(mob);
            ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelName);
            modeledEntity.addActiveModel(activeModel);
            modeledEntity.detectPlayers();
            //modeledEntity.setInvisible(true);*/

            ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(modelName);
            ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(mob);
            modeledEntity.setBaseEntityVisible(false);
            ActiveModel activeModel = ModelEngineAPI.createActiveModel(blueprint);
            modeledEntity.addModel(activeModel, true);

            enList.add(mob.getUniqueId());
          }
        }
      }
    }

    entities.put(complete, enList);
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    // Addon.ModelEngine.x:

    List<String> spl = portalConfig.getStringList("Addon.ModelEngine");

    if (spl.size() == 0) return;

    setOption(portal, "modelEngineList", spl);

    return;
  }

  public boolean eval(String cond) {
    if (cond.equals("true")) return true;
    cond = "(" + cond + ")";

    // DimensionsDebbuger.DEBUG.print("EVAL: "+cond);
    while (cond.contains("(")) {
      int start = cond.lastIndexOf("(");
      int end = cond.indexOf(")", start);

      String newCond = cond.substring(start + 1, end);
      // DimensionsDebbuger.DEBUG.print("NEWCOND: "+cond);
      cond = cond.replace("(" + newCond + ")", getParenthesis(newCond));
    }
    // DimensionsDebbuger.DEBUG.print("FINAL: "+cond);

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
}
