package me.xxastaspastaxx.dimensions.addons.patreoncosmetics;

import java.util.ArrayList;
import java.util.List;
import me.xxastaspastaxx.dimensions.commands.DimensionsCommand;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PatreonCommand extends DimensionsCommand {

  DimensionsPatreonCosmetics main;

  public PatreonCommand(
      String command,
      String args,
      String[] aliases,
      String description,
      String permission,
      boolean adminCommand,
      DimensionsPatreonCosmetics main) {
    super(command, args, aliases, description, permission, adminCommand);
    this.main = main;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {

    if (!(sender instanceof Player)) return;

    Player p = (Player) sender;

    if (args.length >= 2) {
      Player p2;
      if ((p2 = Bukkit.getPlayer(args[1])) != null) {
        p.sendMessage(DimensionsSettings.getPrefix() + getStatusString(p2));
      } else {
        p.sendMessage(
            DimensionsSettings.getPrefix() + ChatColor.RED + "Could not find player " + args[1]);
      }
    } else {
      p.sendMessage(DimensionsSettings.getPrefix() + getStatusString(p));
    }
  }

  public String getStatusString(Player p) {
    String res =
        ChatColor.GRAY
            + "Player: "
            + ChatColor.GREEN
            + p.getName()
            + "\n"
            + ChatColor.GRAY
            + "UUID: "
            + ChatColor.GREEN
            + p.getUniqueId().toString()
            + "\n";
    res +=
        ChatColor.GRAY
            + "Supporter: "
            + (main.getUsers().containsKey(p.getUniqueId())
                ? ChatColor.GREEN + "true"
                : ChatColor.RED + "false")
            + "\n";
    res += ChatColor.GRAY + "Effects:\n";
    if (main.getUsers().containsKey(p.getUniqueId())) {
      for (String s : main.getUsers().get(p.getUniqueId()).keySet()) {
        res +=
            "  " + ChatColor.GREEN + s + ": " + main.getUsers().get(p.getUniqueId()).get(s) + "\n";
      }
    } else {
      res += "  " + ChatColor.RED + "No active effects";
    }
    return res;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, String[] args) {
    ArrayList<String> res = new ArrayList<String>();

    if (args.length != 2) return res;

    Bukkit.getOnlinePlayers().forEach(p -> res.add(p.getName()));

    return res;
  }
}
