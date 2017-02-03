package de.themoep.EditArmorStands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.*
 */

public class EditArmorStandsCommand implements CommandExecutor {
    private final EditArmorStands plugin;

    private final static List<String> TOGGLE_OPTIONS = Arrays.asList("namevisible", "gravity", "visible", "base", "arms", "size");

    public EditArmorStandsCommand(EditArmorStands plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (args.length == 0) {
                sender.sendMessage(ChatColor.GREEN + "Rightclick on the Armor Stand you want to edit in the next " + ChatColor.YELLOW + "10s" + ChatColor.GREEN + "!");
                plugin.addWaitingAction(p, args);
            } else if (args[0].equalsIgnoreCase("exit")) {
                if (plugin.isPersistent(p)) {
                    sender.sendMessage(ChatColor.GREEN + "Disabled persistent mode!");
                } else if (plugin.hasWaitingAction(p)) {
                    sender.sendMessage(ChatColor.GREEN + "Canceled pending click action!");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Exited Armor Stand editing mode!");
                }
                plugin.disablePersistent(p);
                plugin.removeWaitingAction(p);
                plugin.removeSelection(p);
            } else if (args[0].equalsIgnoreCase("usage") || args[0].equalsIgnoreCase("help")) {
                List<String> usage = new ArrayList<String>();

                usage.add("--- &6EditArmorStands v" + plugin.getDescription().getVersion() + " Usage:&r ---");

                usage.add("&e/editarmorstand &rAlias: &e/eas&r)");
                usage.add("&r - Rightclick an Armor Stand in the next 10s to select it");
                usage.add("&e/eas persist");
                usage.add("&r - Apply options via click without rerunning the command");
                usage.add("&e/eas exit");
                usage.add("&r - Exit the editing/persist mode");
                if (sender.hasPermission("editarmorstands.command.items")) {
                    usage.add("&e/eas items");
                    usage.add("&r - Show a gui to manipulate the items/armor");
                }
                if (sender.hasPermission("editarmorstands.command.name")) {
                    usage.add("&e/eas name <name>");
                    usage.add("&r - Set the Armor Stand's name" + (sender.hasPermission("editarmorstands.command.name.colored") ? ", use & for colorcodes" : ""));
                }
                if (sender.hasPermission("editarmorstands.command.move")) {
                    usage.add("&e/eas move <x> <y> <z>");
                    usage.add("&r - Move an Armor Stand, use ~ for relatives");
                }
                String toggles = "";
                for (String s : TOGGLE_OPTIONS) {
                    if (sender.hasPermission("editarmorstands.command." + s)) {
                        if (toggles.length() > 0)
                            toggles += "&r|";
                        toggles += "&e" + s;
                    }
                }
                if (toggles.length() > 0) {
                    usage.add("&e/eas &r[" + toggles + "&r]");
                    usage.add("&r - Toggle the option");
                }
                if (sender.hasPermission("editarmorstands.command.pose")) {
                    usage.add("&e/eas rotate <degree>");
                    usage.add("&r - Rotate the whole Armor Stand");
                    usage.add("&e/eas <bodypart> &r[&epitch&r|&eyaw&r|&eroll&r] &e<degree>");
                    usage.add("&r - Set an angle, use ~ for relatives");
                    usage.add("&e/eas <bodypart> <pitch> <yaw> <roll>");
                    usage.add("&r - Set all angles of a body part at once, use ~ for relatives");
                    usage.add("&eAvailable bodyparts: head, body, leftarm, rightarm, leftleg, rightleg. (Short forms: h, b, la, ra, ll, rl)");
                }

                for (String s : usage)
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
            } else if (args[0].equalsIgnoreCase("persist")) {
                plugin.enablePersistent(p);
                sender.sendMessage(ChatColor.GREEN + "Enabled persistent mode. Disable via " + ChatColor.YELLOW + "/eas exit");
            } else {
                try {
                    BodyPart.fromString(args[0]);
                    if (!sender.hasPermission("editarmorstands.command.pose")) {
                        sender.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.pose");
                        return true;
                    } else if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /eas " + args[0].toLowerCase() + " [pitch|yaw|roll] <degree>");
                        sender.sendMessage(ChatColor.RED + "Or /eas " + args[0].toLowerCase() + " <pitch> <yaw> <roll>");
                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    if (args[0].equalsIgnoreCase("y") || args[0].equalsIgnoreCase("yaw") || args[0].equalsIgnoreCase("r") || args[0].equalsIgnoreCase("rotate") || args[0].equalsIgnoreCase("rotation")) {
                        if (!sender.hasPermission("editarmorstands.command.pose")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.pose");
                            return true;
                        } else if (args.length < 2) {
                            sender.sendMessage(ChatColor.RED + "Usage: /eas " + args[0].toLowerCase() + " <degree>");
                            return true;
                        }
                    } else if (args[0].equalsIgnoreCase("name") || args[0].equalsIgnoreCase("move") || args[0].equalsIgnoreCase("mv") || args[0].equalsIgnoreCase("items") || args[0].equalsIgnoreCase("inv") || args[0].equalsIgnoreCase("i") || TOGGLE_OPTIONS.contains(args[0].toLowerCase())) {
                        if (args[0].equalsIgnoreCase("mv")) {
                            args[0] = "move";
                        } else if (args[0].equalsIgnoreCase("inv") || args[0].equalsIgnoreCase("i")) {
                            args[0] = "items";
                        }
                        if (!sender.hasPermission("editarmorstands.command." + args[0].toLowerCase())) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command." + args[0].toLowerCase());
                            return true;
                        } else if (args[0].equalsIgnoreCase("move") && args.length < 4) {
                            sender.sendMessage(ChatColor.RED + "Usage: /eas move <x> <y> <z>");
                            return true;
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "The argument " + ChatColor.YELLOW + args[0].toLowerCase() + ChatColor.RED + " doesn't exist. Use " + ChatColor.YELLOW + "/eas help" + ChatColor.RED + " to get all available toggleOptions!");
                        return true;
                    }
                }

                UUID asid = plugin.getSelection(p);
                if (asid != null) {
                    ArmorStand as = null;
                    for (Entity e : p.getNearbyEntities(64, 64, 64))
                        if (e.getType() == EntityType.ARMOR_STAND && e.getUniqueId() == asid) {
                            as = (ArmorStand) e;
                            break;
                        }
                    if (as != null) {
                        plugin.calculateAction(p, as, args);
                    } else {
                        sender.sendMessage(ChatColor.RED + "You can only edit Armor Stands in a 64 block radius!");
                    }
                } else if (plugin.isPersistent(p)) {
                    sender.sendMessage(ChatColor.GREEN + "Rightclick on the Armor Stand you want to edit!");
                    plugin.setPersistentAction(p, args);
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Rightclick on the Armor Stand you want to edit in the next " + ChatColor.YELLOW + "10s" + ChatColor.GREEN + "!");
                    plugin.addWaitingAction(p, args);
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
        }
        return true;
    }
}
