package me.xxastaspastaxx.dimensions.addons.customlighter.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.customlighter.DimensionsCustomLighter;
import me.xxastaspastaxx.dimensions.commands.DimensionsCommand;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomLighterCommand extends DimensionsCommand {

  DimensionsCustomLighter main;

  public CustomLighterCommand(
      String command,
      String args,
      String[] aliases,
      String description,
      String permission,
      boolean adminCommand,
      DimensionsCustomLighter main) {
    super(command, args, aliases, description, permission, adminCommand);
    this.main = main;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {

    if (!(sender instanceof Player)) return;

    Player p = (Player) sender;
    if (args.length < 2) {
      p.sendMessage("§7[§cDimensions§7] No portal found.");
      return;
    }

    CustomPortal portal = Dimensions.getCustomPortalManager().getCustomPortal(args[1]);
    if (portal == null) {
      p.sendMessage("§7[§cDimensions§7] No portal found.");
      return;
    }

    ItemStack item = p.getInventory().getItemInMainHand();
    if (item == null || item.getType() == Material.AIR) {
      p.sendMessage("§7[§cDimensions§7] No item found in your hand.");
      return;
    }

    try {
      YamlConfiguration conf = new YamlConfiguration();
      conf.set("item", item);
      String data = conf.saveToString();

      List<String> prevList =
          main.getPortalConfig(portal).getStringList("Addon.CustomLighter.Item");
      prevList.add("MINECRAFT:" + data);
      main.getPortalConfig(portal).set("Addon.CustomLighter.Item", prevList);
      main.getPortalConfig(portal).set("Portal.LighterMaterial", "null");

      p.sendMessage(
          "§7[§cDimensions§7] §aThe custom lighter was succesfully updated, please reload"
              + " Dimensions");
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      main.getPortalConfig(portal)
          .save(new File("plugins/Dimensions/Portals/" + portal.getPortalId() + ".yml"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, String[] args) {
    ArrayList<String> res = new ArrayList<String>();

    if (args.length != 2) return res;

    Dimensions.getCustomPortalManager().getCustomPortals().forEach(p -> res.add(p.getPortalId()));

    return res;
  }
}
