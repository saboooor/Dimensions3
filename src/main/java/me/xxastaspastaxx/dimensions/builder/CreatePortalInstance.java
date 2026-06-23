package me.xxastaspastaxx.dimensions.builder;

import java.util.HashMap;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.gui.CreatePortalGUI;
import me.xxastaspastaxx.dimensions.gui.DimensionsGUIType;
import me.xxastaspastaxx.dimensions.gui.player.DimensionsPlayerMainGUI;
import me.xxastaspastaxx.dimensions.gui.player.DimensionsPlayerPortalGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class CreatePortalInstance {

  private Player p;

  public HashMap<DimensionsGUIType, CreatePortalGUI> guiMap =
      new HashMap<DimensionsGUIType, CreatePortalGUI>();
  private DimensionsGUIType currentGUI = DimensionsGUIType.PLAYER_MAIN;

  public CustomPortal selectedPortal = null;

  public CreatePortalInstance(Player p, boolean player) {
    this.p = p;

    if (player) {
      guiMap.put(DimensionsGUIType.PLAYER_MAIN, new DimensionsPlayerMainGUI(this));
      guiMap.put(DimensionsGUIType.PLAYER_PORTAL, new DimensionsPlayerPortalGUI(this));
    }

    open();
  }

  // Getters
  public Player getPlayer() {
    return p;
  }

  // Methods
  public void open() {

    guiMap.get(currentGUI).open();
  }

  public void setCurrentGUI(DimensionsGUIType type) {
    currentGUI = type;
  }

  public void spawnParticles() {}

  // TO-REMOVE

  public boolean click(Inventory inv, int index, boolean rightClick, boolean shiftClick) {
    return guiMap.get(currentGUI).handleClick(inv, index, rightClick, shiftClick);
  }

  //
  public boolean handleChatInput(String string) {
    return guiMap.get(currentGUI).handleChatAsync(string);
  }
}
