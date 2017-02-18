package de.themoep.EditArmorStands;

/*
 * EditArmorStands
 * Copyright (C) 2017 Max Lee (https://github.com/Phoenix616/)
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

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ArmorStandListener implements Listener {
    private EditArmorStands plugin;

    public ArmorStandListener(EditArmorStands plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorStandClick(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            PlayerInteractEntityEvent pie = new PlayerInteractEntityEvent(event.getPlayer(), event.getRightClicked());
            plugin.getServer().getPluginManager().callEvent(pie);
            if (pie.isCancelled()) {
                return;
            }

            ArmorStand armorStand = (ArmorStand) event.getRightClicked();

            if (plugin.hasWaitingAction(event.getPlayer())) {
                event.setCancelled(true);
                plugin.calculateAction(event.getPlayer(), armorStand, plugin.getWaitingAction(event.getPlayer()));
                plugin.removeWaitingAction(event.getPlayer());
                return;
            }

            if (plugin.getPersistentAction(event.getPlayer()) != null) {
                event.setCancelled(true);
                plugin.calculateAction(event.getPlayer(), armorStand, plugin.getPersistentAction(event.getPlayer()));
                return;
            }

            if (event.getPlayer().getItemInHand().getType() == Material.NAME_TAG) {
                ItemStack hi = event.getPlayer().getItemInHand();
                if (hi.hasItemMeta() && hi.getItemMeta().hasDisplayName()) {
                    if (event.getPlayer().hasPermission("editarmorstands.nametag.name")) {
                        event.setCancelled(true);
                        String name = hi.getItemMeta().getDisplayName();
                        if (event.getPlayer().hasPermission("editarmorstands.nametag.name.colored")) {
                            name = ChatColor.translateAlternateColorCodes('&', name);
                        } else {
                            name = ChatColor.ITALIC + name;
                        }
                        armorStand.setCustomName(name + ChatColor.RESET);
                        armorStand.setCustomNameVisible(true);
                        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                            if (hi.getAmount() > 1) {
                                hi.setAmount(hi.getAmount() - 1);
                            } else {
                                event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
                            }
                            event.getPlayer().updateInventory();
                        }
                        return;
                    }
                } else if (event.getPlayer().hasPermission("editarmorstands.nametag.name.clear")) {
                    event.setCancelled(true);
                    armorStand.setCustomName("");
                    armorStand.setCustomNameVisible(false);
                    return;
                }

            } else if (!event.getPlayer().isSneaking() && event.getPlayer().hasPermission("editarmorstands.openinventory")) {
                event.setCancelled(true);
                ArmorStandGui gui = new ArmorStandGui(plugin, armorStand, event.getPlayer());
                gui.show();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandDestroy(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.ARMOR_STAND && plugin.isSelected(event.getEntity())) {
            plugin.removeSelected(event.getEntity());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandPlace(PlayerInteractEvent event) {
        boolean isArmorStandPlacement = event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null && event.getItem().getType() == Material.ARMOR_STAND;
        boolean isNamedArmorStand = isArmorStandPlacement && event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName();

        if (isNamedArmorStand && event.getPlayer().hasPermission("editarmorstands.place.name")) {
            String name = event.getItem().getItemMeta().getDisplayName();
            if (event.getPlayer().hasPermission("editarmorstands.place.name.colored")) {
                name = ChatColor.translateAlternateColorCodes('&', name);
            } else {
                name = ChatColor.ITALIC + name;
            }
            final Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
            final String finalName = name + ChatColor.RESET;
            final Listener spawnListener = new Listener() {
                @EventHandler
                public void onArmorStandSpawn(CreatureSpawnEvent event) {
                    if (event.getEntity().getType() == EntityType.ARMOR_STAND && event.getEntity().getLocation().getBlock().getLocation().equals(loc)) {
                        event.getEntity().setCustomName(finalName);
                        event.getEntity().setCustomNameVisible(true);
                    }
                }
            };
            plugin.getServer().getPluginManager().registerEvents(spawnListener, plugin);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> HandlerList.unregisterAll(spawnListener), 20);
        }
    }
}
