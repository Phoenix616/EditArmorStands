package de.themoep.EditArmorStands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * EditArmorStands
 * Copyright (C) 2015 Max Lee (https://github.com/Phoenix616/)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ArmorStandGui implements Listener {

    private final EditArmorStands plugin;
    private final ArmorStand armorStand;
    private final Player player;

    private final static List<Material> HELMETS = Arrays.asList(
            Material.LEATHER_HELMET,
            Material.IRON_HELMET,
            Material.GOLD_HELMET,
            Material.DIAMOND_HELMET,
            Material.CHAINMAIL_HELMET,
            Material.SKULL_ITEM,
            Material.PUMPKIN
    );

    private final static List<Material> CHESTPLATES = Arrays.asList(
            Material.LEATHER_CHESTPLATE,
            Material.IRON_CHESTPLATE,
            Material.GOLD_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE,
            Material.CHAINMAIL_CHESTPLATE
    );

    private final static List<Material> PANTS = Arrays.asList(
            Material.LEATHER_LEGGINGS,
            Material.IRON_LEGGINGS,
            Material.GOLD_LEGGINGS,
            Material.DIAMOND_LEGGINGS,
            Material.CHAINMAIL_LEGGINGS
    );

    private final static List<Material> BOOTS = Arrays.asList(
            Material.LEATHER_BOOTS,
            Material.IRON_BOOTS,
            Material.GOLD_BOOTS,
            Material.DIAMOND_BOOTS,
            Material.CHAINMAIL_BOOTS
    );
    private final ItemStack filler;

    private boolean open = false;

    private Inventory inventory;

    private InventoryView gui;

    public ArmorStandGui(EditArmorStands plugin, ArmorStand armorStand, Player player) {
        this.plugin = plugin;
        this.armorStand = armorStand;
        this.player = player;

        filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta fim = filler.getItemMeta();
        fim.setDisplayName(ChatColor.BLACK + "X");
        filler.setItemMeta(fim);

        build();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void build() {
        inventory = plugin.getServer().createInventory(null, 6 * 9, "Armor Stand items:");
        ItemStack[] items = new ItemStack[6 * 9];
        for(int i = 0; i < inventory.getSize(); i++) {
            items[i] = getSlotItem(i);
        }
        inventory.setContents(items);
    }

    public void show() {
        open = true;
        gui = player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(open && !event.isCancelled() && event.getClickedInventory() == event.getWhoClicked().getOpenInventory().getTopInventory() && event.getView() == gui) {
            ItemStack cur = event.getCurrentItem();
            ItemStack realItem = getSlotItem(event.getSlot());
            if(cur != null && cur.getType() != Material.AIR && !cur.equals(realItem)) {
                plugin.getLogger().log(Level.WARNING, "The item " + event.getWhoClicked().getName() + " tried to pickup was not the same as the one on the armor stand! Duping attempt?");
                event.setCancelled(true);
                event.setCurrentItem(realItem);
                event.getWhoClicked().sendMessage(ChatColor.RED + "This Armor Stands items where modified! Please try again!");
                return;
            }
            ItemStack hand = event.getCursor();
            plugin.getLogger().info("event.getAction() == " + event.getAction());
            try {
                ItemStack result = getResultItem(event.getAction(), cur, hand);
                if(isValidItem(event.getSlot(), result)) {
                    switch(event.getSlot()) {
                        case 13:
                            armorStand.setHelmet(result);
                            break;
                        case 21:
                            armorStand.setItemInHand(result);
                            break;
                        case 22:
                            armorStand.setChestplate(result);
                            break;
                        /*case 23:
                            break;*/
                        case 31:
                            armorStand.setLeggings(result);
                            break;
                        case 40:
                            armorStand.setBoots(result);
                            break;
                        case 16:
                            player.getInventory().setHelmet(result);
                            break;
                        case 25:
                            player.getInventory().setChestplate(result);
                            break;
                        case 34:
                            player.getInventory().setLeggings(result);
                            break;
                        case 43:
                            player.getInventory().setBoots(result);
                            break;
                    }
                } else {
                    event.setCancelled(true);
                }
            } catch(ActionNotSupported e) {
                event.setCancelled(true);
            }
        }
    }

    private ItemStack getSlotItem(int slot) {
        switch(slot) {
            case 13:
                return armorStand.getHelmet();
            case 21:
                return armorStand.getItemInHand();
            case 22:
                return armorStand.getChestplate();
            case 23:
                ItemStack leftArm = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
                ItemMeta laim = leftArm.getItemMeta();
                laim.setDisplayName(ChatColor.RED + "Can't set the left arm until 1.9!");
                leftArm.setItemMeta(laim);
                return leftArm;
                //return armorStand.getItemInLeftHand();
            case 31:
                return armorStand.getLeggings();
            case 40:
                return armorStand.getBoots();
            case 16:
                return player.getInventory().getHelmet();
            case 25:
                return player.getInventory().getChestplate();
            case 34:
                return player.getInventory().getLeggings();
            case 43:
                return player.getInventory().getBoots();
            default:
                return filler;
        }
    }

    private ItemStack getResultItem(InventoryAction action, ItemStack current, ItemStack hand) throws ActionNotSupported {
        ItemStack curClone = current == null ? null : current.clone();
        ItemStack handClone = hand == null ? null : hand.clone();
        switch(action) {
            case DROP_ALL_SLOT:
            case PICKUP_ALL:
                return null;
            case SWAP_WITH_CURSOR:
                return handClone;
            case CLONE_STACK:
                return curClone;
            case PLACE_ALL:
                if(curClone != null && handClone != null) {
                    handClone.setAmount(curClone.getAmount() + handClone.getAmount());
                }
                return handClone;
            case DROP_ONE_SLOT:
            case PICKUP_ONE:
                if(curClone != null) {
                    curClone.setAmount(curClone.getAmount() - 1);
                }
                return curClone;
            case PLACE_ONE:
                if(handClone != null) {
                    handClone.setAmount(1);
                }
                return handClone;
            case PLACE_SOME:
                if(curClone != null) {
                    curClone.setAmount(curClone.getMaxStackSize());
                }
                return curClone;
            case PICKUP_SOME:
                if(curClone != null && handClone != null) {
                    curClone.setAmount(curClone.getAmount() - (handClone.getMaxStackSize() - handClone.getAmount()));
                } else if(curClone != null) {
                    int amount = curClone.getAmount() - curClone.getMaxStackSize();
                    curClone.setAmount(amount > 0 ? amount : 0);
                }
                return curClone;
            default:
                throw new ActionNotSupported();
        }
    }

    private boolean isValidItem(int slot, ItemStack itemStack) {
        boolean empty = itemStack == null || itemStack.getType() == Material.AIR;
        switch(slot) {
            case 13:
            case 16:
                return empty || HELMETS.contains(itemStack.getType());
            case 21:
            //case 23:
                return true;
            case 22:
            case 25:
                return empty || CHESTPLATES.contains(itemStack.getType());
            case 31:
            case 34:
                return empty || PANTS.contains(itemStack.getType());
            case 40:
            case 43:
                return empty || BOOTS.contains(itemStack.getType());
            default:
                return false;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        plugin.getLogger().info("InventoryCloseEvent: " + event.getPlayer().getName());
        if(event.getPlayer().getUniqueId() == player.getUniqueId() && event.getView() == gui) {
            destroy();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(event.getPlayer().getUniqueId() == player.getUniqueId()) {
            destroy();
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if(event.getPlayer().getUniqueId() == player.getUniqueId()) {
            destroy();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getEntity().getUniqueId() == player.getUniqueId()) {
            destroy();
        }
    }

    private void destroy() {
        HandlerList.unregisterAll(this);
        open = false;
    }

    private class ActionNotSupported extends Throwable {

    }
}
