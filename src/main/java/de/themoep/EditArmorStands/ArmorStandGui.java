package de.themoep.EditArmorStands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
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

    private final static List<Integer> SLOTS_ARMORS = Arrays.asList(4, 13, 22, 31);
    private final static List<Integer> SLOTS_PLAYER = Arrays.asList(7, 16, 25, 34);

    private final ItemStack filler;

    private boolean open = false;

    private Inventory inventory = null;

    private InventoryView gui;

    private long lastClick = 0;

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
        if(inventory == null) {
            inventory = plugin.getServer().createInventory(null, 4 * 9, "Armor Stand items:");
        }
        ItemStack[] items = new ItemStack[4 * 9];
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
    public void onInventoryDrag(InventoryDragEvent event) {
        if(open && event.getWhoClicked().getUniqueId().equals(player.getUniqueId()) && event.getView() == gui) {
            for(int i = 0; i < inventory.getSize(); i++) {
                if(event.getRawSlots().contains(i)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(open && !event.isCancelled() && event.getWhoClicked().getUniqueId().equals(player.getUniqueId()) && event.getView() == gui) {
            long curTime = System.currentTimeMillis();
            if(lastClick + 50 > curTime) {
                event.setCancelled(true);
                plugin.getLogger().log(Level.WARNING, event.getWhoClicked().getName() + " tried to click too fast (" + (curTime - lastClick) + "ms)");
                event.getWhoClicked().sendMessage(ChatColor.RED + "Please wait a tiny bit longer between your clicks!");
                return;
            }
            lastClick = System.currentTimeMillis();
            if(event.getRawSlot() < inventory.getSize()) {
                if(event.getSlot() == 8) {
                    event.setCancelled(true);
                    plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                        public void run() {
                            player.closeInventory();
                        }
                    });
                    return;
                }
                ItemStack cur = event.getCurrentItem();
                ItemStack realItem = getSlotItem(event.getSlot());
                if(!areSimilar(cur, realItem)) {
                    event.setCancelled(true);
                    plugin.getLogger().log(Level.WARNING, "The item " + event.getWhoClicked().getName() + " tried to pickup was not the same as the one in the inventory (Armor Stand or player)! Duping attempt?");
                    event.setCurrentItem(realItem);
                    event.getWhoClicked().sendMessage(ChatColor.RED + "This inventory's items were modified! Please try again!");
                    return;
                }
                ItemStack hand = event.getCursor();
                try {
                    ItemStack result = getResultItem(event.getSlot(), event.getAction(), cur, hand);
                    if(!setSlot(event.getSlot(), result)) {
                        event.setCancelled(true);
                    } else if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && (SLOTS_PLAYER.contains(event.getSlot()) || SLOTS_ARMORS.contains(event.getSlot()))) {
                        build();
                        event.setCancelled(true);
                    }

                } catch(ActionNotSupported e) {
                    if(event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
                        event.setCancelled(true);
                        ItemStack hbItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                        if(setSlot(event.getSlot(), hbItem)) {
                            build();
                            event.getWhoClicked().getInventory().setItem(event.getHotbarButton(), cur);
                        } else {
                            int emptySlot = event.getWhoClicked().getInventory().firstEmpty();
                            if(emptySlot > 0 && setSlot(event.getSlot(), null)) {
                                build();
                                event.getWhoClicked().getInventory().setItem(event.getHotbarButton(), cur);
                                event.getWhoClicked().getInventory().setItem(emptySlot, hbItem);
                            }
                        }
                    } else if(event.getAction() == InventoryAction.HOTBAR_SWAP) {
                        ItemStack hbItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                        if(setSlot(event.getSlot(), hbItem)) {
                            build();
                        } else {
                            event.setCancelled(true);
                        }
                    } else {
                        event.setCancelled(true);
                    }
                }
            } else if(event.getRawSlot() >= inventory.getSize()) {
                if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.setCancelled(true);
                    List<Integer> slots = new ArrayList<Integer>(SLOTS_ARMORS);
                    slots.addAll(SLOTS_PLAYER);
                    slots.add(12);
                    //slots.add(14);
                    for(int i : slots) {
                        ItemStack target = event.getWhoClicked().getOpenInventory().getTopInventory().getItem(i);
                        if((target == null || target.getType() == Material.AIR) && isValidItem(i, event.getCurrentItem())) {
                            if(setSlot(i, event.getCurrentItem())) {
                                event.getWhoClicked().getInventory().setItem(event.getSlot(), null);
                                build();
                            }
                            break;
                        }
                    }
                } else if(event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean areSimilar(ItemStack item1, ItemStack item2) {
        if((item1 == null || item1.getType() == Material.AIR) && (item2 == null || item2.getType() == Material.AIR)) {
            return true;
        }
        if(item1 != null && item2 != null) {
            return item1.equals(item2);
        }
        return false;
    }

    private boolean setSlot(int slot, ItemStack item) {
        if(isValidItem(slot, item)) {
            switch(slot) {
                case 4:
                    armorStand.setHelmet(item);
                    break;
                case 12:
                    armorStand.setItemInHand(item);
                    break;
                case 13:
                    armorStand.setChestplate(item);
                    break;
                /*case 14:
                    break;*/
                case 22:
                    armorStand.setLeggings(item);
                    break;
                case 31:
                    armorStand.setBoots(item);
                    break;
                case 7:
                    player.getInventory().setHelmet(item);
                    break;
                case 16:
                    player.getInventory().setChestplate(item);
                    break;
                case 25:
                    player.getInventory().setLeggings(item);
                    break;
                case 34:
                    player.getInventory().setBoots(item);
                    break;
                default:
                    return false;
            }
            return true;
        }
        return false;
    }

    private ItemStack getSlotItem(int slot) {
        switch(slot) {
            case 4:
                return armorStand.getHelmet();
            case 12:
                return armorStand.getItemInHand();
            case 13:
                return armorStand.getChestplate();
            case 14:
                ItemStack leftArm = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
                ItemMeta laim = leftArm.getItemMeta();
                laim.setDisplayName(ChatColor.RED + "Can't set the left arm until 1.9!");
                leftArm.setItemMeta(laim);
                return leftArm;
                //return armorStand.getItemInLeftHand();
            case 22:
                return armorStand.getLeggings();
            case 31:
                return armorStand.getBoots();
            case 7:
                return player.getInventory().getHelmet();
            case 16:
                return player.getInventory().getChestplate();
            case 25:
                return player.getInventory().getLeggings();
            case 34:
                return player.getInventory().getBoots();
            case 8:
                ItemStack close = new ItemStack(Material.BARRIER);
                ItemMeta cim = close.getItemMeta();
                cim.setDisplayName(ChatColor.RED + "Close");
                close.setItemMeta(cim);
                return close;
            default:
                return filler;
        }
    }

    private ItemStack getResultItem(int slot, InventoryAction action, ItemStack current, ItemStack hand) throws ActionNotSupported {
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
            case MOVE_TO_OTHER_INVENTORY:
                if(SLOTS_PLAYER.contains(slot) || SLOTS_ARMORS.contains(slot)) {
                    return swap(slot);
                }
                return null;
            default:
                throw new ActionNotSupported();
        }
    }

    private ItemStack swap(int slot) {
        int swap = getSwap(slot);
        ItemStack slotItem = getSlotItem(slot);
        ItemStack item = getSlotItem(swap);
        if(setSlot(swap, slotItem)) {
            return item;
        }
        return null;
    }

    private int getSwap(int slot) {
        int swap = -1;
        if(SLOTS_ARMORS.contains(slot)) {
            swap = SLOTS_PLAYER.get(SLOTS_ARMORS.indexOf(slot));
        } else if(SLOTS_PLAYER.contains(slot)) {
            swap = SLOTS_ARMORS.get(SLOTS_PLAYER.indexOf(slot));
        }
        return swap;
    }

    private boolean isValidItem(int slot, ItemStack itemStack) {
        boolean empty = itemStack == null || itemStack.getType() == Material.AIR;
        switch(slot) {
            case 4:
            case 7:
                return empty || HELMETS.contains(itemStack.getType());
            case 12:
            //case 14:
                return true;
            case 13:
            case 16:
                return empty || CHESTPLATES.contains(itemStack.getType());
            case 22:
            case 25:
                return empty || PANTS.contains(itemStack.getType());
            case 31:
            case 34:
                return empty || BOOTS.contains(itemStack.getType());
            default:
                return false;
        }
    }

    @EventHandler
    public void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        if(event.getRightClicked().getUniqueId().equals(armorStand.getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Can't manipulate this Armor Stand! " + ChatColor.GOLD + player.getName() + ChatColor.RED + " is currently editing it!");
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getPlayer().getUniqueId().equals(player.getUniqueId()) && event.getView() == gui) {
            destroy();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            destroy();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getEntity().getUniqueId().equals(player.getUniqueId())) {
            destroy();
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if(event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            destroy();
            player.closeInventory();
        }
    }

    @EventHandler
    public void onArmorStandDestroy(EntityDeathEvent event) {
        if(event.getEntity().getUniqueId().equals(armorStand.getUniqueId()) && open) {
            destroy();
            player.closeInventory();
        }
    }

    private void destroy() {
        HandlerList.unregisterAll(this);
        open = false;
    }

    private class ActionNotSupported extends Throwable {

    }
}
