package me.xxastaspastaxx.dimensions.addons.chargeonuse;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsDebbuger;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class DimensionsChargeOnUse extends DimensionsAddon implements Listener {

  private Plugin pl;

  private Economy econ;

  public DimensionsChargeOnUse() {
    super(
        "DimensionsChargeOnUseAddon",
        "3.0.4",
        "Charge players for using a portal",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public boolean onLoad(Dimensions main) {
    this.pl = main;

    return main.getServer().getPluginManager().getPlugin("Vault") != null;
  }

  @Override
  public void onEnable(Dimensions main) {
    this.pl = main;

    RegisteredServiceProvider<Economy> rsp =
        main.getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp != null) econ = rsp.getProvider();

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void onPortalUse(CustomPortalUseEvent e) {

    Entity entity = e.getEntity();
    if (!(entity instanceof Player)) return;

    CompletePortal complete = e.getCompletePortal();
    Object chargeAmount = getOption(complete, "chargeAmount");

    if (chargeAmount == null) return;

    if (!shouldPlayerPay(complete, (Player) entity)) return;

    if (econ.getBalance((Player) entity) < (double) chargeAmount) {
      entity.sendMessage((String) getOption(complete, "chargeDenyMessage"));
      e.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postPortalUse(CustomPortalUseEvent e) {

    Entity entity = e.getEntity();
    if (!(entity instanceof Player)) return;

    CompletePortal completePortal = e.getCompletePortal();
    Object chargeAmount = getOption(completePortal, "chargeAmount");
    if (chargeAmount == null) return;

    if (shouldPlayerPay(completePortal, (Player) entity)) {
      entity.sendMessage((String) getOption(completePortal, "chargeAcceptMessage"));
      econ.withdrawPlayer((Player) entity, (double) getOption(completePortal, "chargeAmount"));
      completePortal.setTag("PAID_" + entity.getUniqueId().toString(), true);
    }
  }

  public boolean shouldPlayerPay(CompletePortal complete, Player p) {
    CustomPortal portal = complete.getCustomPortal();
    boolean returns = complete.getWorld().equals(portal.getWorld());

    if (!((boolean) getOption(complete, "chargeOnReturn")) && returns) return false;

    if ((boolean) getOption(complete, "chargeOneTime")) {
      if (complete.getTag("PAID_" + p.getUniqueId().toString()) == null)
        complete.setTag("PAID_" + p.getUniqueId().toString(), false);

      if ((boolean) complete.getTag("PAID_" + p.getUniqueId().toString())) return false;
    }

    return true;
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    double node = portalConfig.getDouble("Addon.ChargeOnUse.Amount", 0.0);
    if (node == 0) return;

    setOption(portal, "chargeAmount", node);
    setOption(
        portal,
        "chargeOnReturn",
        portalConfig.getBoolean("Addon.ChargeOnUse.ChargeOnReturn", false));
    setOption(
        portal, "chargeOneTime", portalConfig.getBoolean("Addon.ChargeOnUse.OneTimePayment", true));
    setOption(
        portal,
        "chargeDenyMessage",
        portalConfig
            .getString(
                "Addon.ChargeOnUse.DenyMessage", "You do not have enough money to use this portal.")
            .replace("&", "§"));
    setOption(
        portal,
        "chargeAcceptMessage",
        portalConfig
            .getString(
                "Addon.ChargeOnUse.ChargeMessage", "You have been charged for using this portal.")
            .replace("&", "§"));

    DimensionsDebbuger.DEBUG.print(portal.getPortalId(), node);

    return;
  }
}
