package de.themoep.EditArmorStands;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EditArmorStands - Plugin to edit armor stand poses and options
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.*
 */

public class EditArmorStands extends JavaPlugin implements Listener, CommandExecutor {

    Map<UUID, Long> clickTimeout = new HashMap<UUID, Long>();

    Map<UUID, UUID> selectedArmorStands = new HashMap<UUID, UUID>();

    Map<UUID, String[]> waitingCommands = new HashMap<UUID, String[]>();

    Set<UUID> persistent = new HashSet<UUID>();

    static List<String> toggleOptions = Arrays.asList("namevisible", "gravity", "visible", "base", "arms", "size");

    static DecimalFormat df = new DecimalFormat("#.##");

    private int serverVersion = 0;

    public void onEnable() {
        Matcher versionMatcher = Pattern.compile(".*\\(MC: (.*)\\)").matcher(getServer().getVersion());
        if(versionMatcher.find()) {
            String[] version = versionMatcher.group(1).split("[.]");
            for(int i = 0; i < version.length && i < 3; i++) {
                try {
                    int n = Integer.parseInt(version[i]);
                    serverVersion += n * Math.pow(100, 2 - i);
                } catch(NumberFormatException e) {
                    getLogger().warning("Could not parse " + version[i] + " as an integer?");
                }
            }
        }

        if(serverVersion != 0) {
            getLogger().info("Detect server " + serverVersion);
            if(serverVersion < 10800) {
                getLogger().warning("Armor Stands weren't in Minecraft before 1.8? What are you trying to do with this plugin?");
                getServer().getPluginManager().disablePlugin(this);
            }
        } else {
            getLogger().warning("Could not detect server version!");
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    public int getServerVersion() {
        return serverVersion;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(args.length == 0) {
                sender.sendMessage(ChatColor.GREEN + "Rightclick on the Armor Stand you want to edit in the next " + ChatColor.YELLOW + "10s" + ChatColor.GREEN + "!");
                clickTimeout.put(p.getUniqueId(), System.currentTimeMillis());
                waitingCommands.put(p.getUniqueId(), args);
            } else if(args[0].equalsIgnoreCase("exit")) {
                if(persistent.contains(p.getUniqueId())) {
                    sender.sendMessage(ChatColor.GREEN + "Disabled persistent mode!");
                } else if(clickTimeout.containsKey(p.getUniqueId()) && clickTimeout.get(p.getUniqueId()) + 10 * 1000 < System.currentTimeMillis()) {
                    sender.sendMessage(ChatColor.GREEN + "Canceled pending click action!");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Exited Armor Stand editing mode!");
                }
                persistent.remove(p.getUniqueId());
                selectedArmorStands.remove(p.getUniqueId());
                waitingCommands.remove(p.getUniqueId());
                clickTimeout.remove(p.getUniqueId());
            } else if(args[0].equalsIgnoreCase("usage") || args[0].equalsIgnoreCase("help")) {
                List<String> usage = new ArrayList<String>();

                usage.add("--- &6EditArmorStands v" + this.getDescription().getVersion() + " Usage:&r ---");

                usage.add("&e/editarmorstand &rAlias: &e/eas&r)");
                usage.add("&r - Rightclick an Armor Stand in the next 10s to select it");
                usage.add("&e/eas persist");
                usage.add("&r - Apply options via click without rerunning the command");
                usage.add("&e/eas exit");
                usage.add("&r - Exit the editing/persist mode");
                if(sender.hasPermission("editarmorstands.command.items")) {
                    usage.add("&e/eas items");
                    usage.add("&r - Show a gui to manipulate the items/armor");
                }
                if(sender.hasPermission("editarmorstands.command.name")) {
                    usage.add("&e/eas name <name>");
                    usage.add("&r - Set the Armor Stand's name" + (sender.hasPermission("editarmorstands.command.name.colored") ? ", use & for colorcodes" : ""));
                }
                if(sender.hasPermission("editarmorstands.command.move")) {
                    usage.add("&e/eas move <x> <y> <z>");
                    usage.add("&r - Move an Armor Stand, use ~ for relatives");
                }
                String toggles = "";
                for(String s : toggleOptions) {
                    if(sender.hasPermission("editarmorstands.command." + s)) {
                        if(toggles.length() > 0)
                            toggles += "&r|";
                        toggles += "&e" + s;
                    }
                }
                if(toggles.length() > 0) {
                    usage.add("&e/eas &r[" + toggles + "&r]");
                    usage.add("&r - Toggle the option");
                }
                if(sender.hasPermission("editarmorstands.command.pose")) {
                    usage.add("&e/eas rotate <degree>");
                    usage.add("&r - Rotate the whole Armor Stand");
                    usage.add("&e/eas <bodypart> &r[&epitch&r|&eyaw&r|&eroll&r] &e<degree>");
                    usage.add("&r - Set an angle, use ~ for relatives");
                    usage.add("&e/eas <bodypart> <pitch> <yaw> <roll>");
                    usage.add("&r - Set all angles of a body part at once, use ~ for relatives");
                    usage.add("&eAvailable bodyparts: head, body, leftarm, rightarm, leftleg, rightleg. (Short forms: h, b, la, ra, ll, rl)");
                }

                for(String s : usage)
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
            } else if(args[0].equalsIgnoreCase("persist")) {
                persistent.add(p.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Enabled persistent mode. Disable via " + ChatColor.YELLOW + "/eas exit");
            } else {
                try {
                    BodyPart.fromString(args[0]);
                    if(!sender.hasPermission("editarmorstands.command.pose")) {
                        sender.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.pose");
                        return true;
                    } else if(args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /eas " + args[0].toLowerCase() + " [pitch|yaw|roll] <degree>");
                        sender.sendMessage(ChatColor.RED + "Or /eas " + args[0].toLowerCase() + " <pitch> <yaw> <roll>");
                        return true;
                    }
                } catch(IllegalArgumentException e) {
                    if(args[0].equalsIgnoreCase("y") || args[0].equalsIgnoreCase("yaw") || args[0].equalsIgnoreCase("r") || args[0].equalsIgnoreCase("rotate") || args[0].equalsIgnoreCase("rotation")) {
                        if(!sender.hasPermission("editarmorstands.command.pose")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.pose");
                            return true;
                        } else if(args.length < 2) {
                            sender.sendMessage(ChatColor.RED + "Usage: /eas " + args[0].toLowerCase() + " <degree>");
                            return true;
                        }
                    } else if(args[0].equalsIgnoreCase("name") || args[0].equalsIgnoreCase("move") || args[0].equalsIgnoreCase("mv") || args[0].equalsIgnoreCase("items") || args[0].equalsIgnoreCase("inv") || args[0].equalsIgnoreCase("i") || toggleOptions.contains(args[0].toLowerCase())) {
                        if(args[0].equalsIgnoreCase("mv")) {
                            args[0] = "move";
                        } else if(args[0].equalsIgnoreCase("inv") || args[0].equalsIgnoreCase("i")) {
                            args[0] = "items";
                        }
                        if(!sender.hasPermission("editarmorstands.command." + args[0].toLowerCase())) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command." + args[0].toLowerCase());
                            return true;
                        } else if(args[0].equalsIgnoreCase("move") && args.length < 4) {
                            sender.sendMessage(ChatColor.RED + "Usage: /eas move <x> <y> <z>");
                            return true;
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "The argument " + ChatColor.YELLOW + args[0].toLowerCase() + ChatColor.RED + " doesn't exist. Use " + ChatColor.YELLOW + "/eas help" + ChatColor.RED + " to get all available toggleOptions!");
                        return true;
                    }
                }
                if(selectedArmorStands.containsKey(p.getUniqueId())) {
                    UUID asid = selectedArmorStands.get(p.getUniqueId());
                    ArmorStand as = null;
                    for(Entity e : p.getNearbyEntities(64, 64, 64))
                        if(e.getType() == EntityType.ARMOR_STAND && e.getUniqueId() == asid) {
                            as = (ArmorStand) e;
                            break;
                        }
                    if(as != null) {
                        calculateAction(p, as, args);
                    } else {
                        sender.sendMessage(ChatColor.RED + "You can only edit Armor Stands in a 64 block radius!");
                    }
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Rightclick on the Armor Stand you want to edit in the next " + ChatColor.YELLOW + "10s" + ChatColor.GREEN + "!");
                    clickTimeout.put(p.getUniqueId(), System.currentTimeMillis());
                    waitingCommands.put(p.getUniqueId(), args);
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
        }
        return true;
    }

    private boolean calculateAction(Player player, ArmorStand as, String[] args) {
        if(args.length == 0) {
            selectedArmorStands.put(player.getUniqueId(), as.getUniqueId());
            clickTimeout.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Selected Armor Stand at " + ChatColor.YELLOW + as.getLocation().getBlockX() + "/" + as.getLocation().getBlockY() + "/" + as.getLocation().getBlockZ() + ChatColor.GREEN + "!");
            player.getPlayer().sendMessage(ChatColor.GREEN + "You can now use " + ChatColor.YELLOW + "/eas <option> <value> " + ChatColor.GREEN + "to edit the properties of this Armor Stand! To exit the editing mode run " + ChatColor.YELLOW + "/eas exit" + ChatColor.GREEN + "!");
            return true;
        } else if(args.length > 0 && args[0].equalsIgnoreCase("name")) {
            if(player.hasPermission("editarmorstands.command.name")) {
                if(args.length > 1) {
                    String name = "";
                    for(int i = 1; i < args.length; i++) {
                        name += args[i] + " ";
                    }
                    if(player.hasPermission("editarmorstands.command.name.colored")) {
                        name = ChatColor.translateAlternateColorCodes('&', name);
                    }
                    as.setCustomName(name.trim() + ChatColor.RESET);
                    player.sendMessage(ChatColor.GREEN + "Set the Armor Stand's name to " + ChatColor.RESET + as.getCustomName() + ChatColor.GREEN + "!");
                } else {
                    as.setCustomName(null);
                    player.sendMessage(ChatColor.GREEN + "Removed the Armor Stand's name!");
                }
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.name");
            }
        } else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("items") || args[0].equalsIgnoreCase("inv") || args[0].equalsIgnoreCase("i")) {
                if(player.hasPermission("editarmorstands.command.items")) {
                    ArmorStandGui gui = new ArmorStandGui(this, as, player);
                    gui.show();
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.items");
                }
            } else if(args[0].equalsIgnoreCase("namevisible")) {
                if(player.hasPermission("editarmorstands.command.namevisible")) {
                    as.setCustomNameVisible(!as.isCustomNameVisible());
                    player.sendMessage(ChatColor.GREEN + "The Armor Stand's name is now " + ChatColor.YELLOW + (as.isCustomNameVisible() ? "" : "in") + "visible" + ChatColor.GREEN + "!");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.namevisible");
                }
            } else if(args[0].equalsIgnoreCase("arms")) {
                if(player.hasPermission("editarmorstands.command.arms")) {
                    as.setArms(!as.hasArms());
                    player.sendMessage(ChatColor.GREEN + "Armor Stand has now " + ChatColor.YELLOW + (as.hasArms() ? "" : "no ") + "arms" + ChatColor.GREEN + "!");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.arms");
                }
            } else if(args[0].equalsIgnoreCase("base")) {
                if(player.hasPermission("editarmorstands.command.base")) {
                    as.setBasePlate(!as.hasBasePlate());
                    player.sendMessage(ChatColor.GREEN + "Armor Stand has now " + ChatColor.YELLOW + (as.hasBasePlate() ? "a" : "no") + " baseplate" + ChatColor.GREEN + "!");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.base");
                }
            } else if(args[0].equalsIgnoreCase("gravity")) {
                if(player.hasPermission("editarmorstands.command.gravity")) {
                    as.setGravity(!as.hasGravity());
                    player.sendMessage(ChatColor.GREEN + "Armor Stand has now " + ChatColor.YELLOW + (as.hasGravity() ? "" : "no ") + "gravity" + ChatColor.GREEN + "!");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.gravity");
                }
            } else if(args[0].equalsIgnoreCase("size")) {
                if(player.hasPermission("editarmorstands.command.size")) {
                    as.setSmall(!as.isSmall());
                    player.sendMessage(ChatColor.GREEN + "Armor Stand is now " + ChatColor.YELLOW + (as.isSmall() ? "small" : "big") + ChatColor.GREEN + "!");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.size");
                }
            } else if(args[0].equalsIgnoreCase("visible")) {
                if(player.hasPermission("editarmorstands.command.visible")) {
                    as.setVisible(!as.isVisible());
                    player.sendMessage(ChatColor.GREEN + "Armor Stand is now " + ChatColor.YELLOW + (as.isVisible() ? "" : "in") + "visible" + ChatColor.GREEN + "!");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.visible");
                }
                /* TODO: Find out why this isn't working!
                } else if(sender.hasPermission("editarmorstands.command.pose")) {
                    ArmorStandPoser asp = new ArmorStandPoser(as);
                    if(asp.translatePlayerLook(args[0], p.getEyeLocation())) {
                        sender.sendMessage(ChatColor.GREEN + "Set " + args[0] + " rotation to your head's view!");
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Sorry but the option " + args[0] + " doesn't exist!");
                    };*/
            }
        } else if(args.length == 2) {
            if(player.hasPermission("editarmorstands.command.pose")) {
                try {
                    int angle;
                    boolean relative = false;

                    if(args[1].startsWith("~")) {
                        relative = true;
                        if(args[1].length() == 1) {
                            angle = 0;
                        } else {
                            angle = Integer.parseInt(args[1].substring(1));
                        }
                    } else {
                        angle = Integer.parseInt(args[1]);
                    }
                    if(args[0].equalsIgnoreCase("p") || args[0].equalsIgnoreCase("pitch")) {
                        Location l = as.getLocation();
                        if(relative)
                            angle += l.getPitch();
                        l.setPitch(angle);
                        as.teleport(l);
                    } else if(args[0].equalsIgnoreCase("y") || args[0].equalsIgnoreCase("yaw") || args[0].equalsIgnoreCase("r") || args[0].equalsIgnoreCase("rotate") || args[0].equalsIgnoreCase("rotation")) {
                        Location l = as.getLocation();
                        if(relative)
                            angle += l.getYaw();
                        l.setYaw(angle);
                        as.teleport(l);
                        player.sendMessage(ChatColor.GREEN + "Set Armor Stand's rotation to " + ChatColor.YELLOW + angle + ChatColor.GREEN + "!");
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "Sorry but the option " + args[0] + " doesn't exist!");
                    }
                } catch(NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Your second argument " + args[1] + " is not a number!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.pose");
            }
        } else if(args.length == 3) {
            if(player.hasPermission("editarmorstands.command.pose")) {
                try {
                    int angle;
                    boolean relative = false;

                    if(args[2].startsWith("~")) {
                        relative = true;
                        if(args[2].length() == 1) {
                            angle = 0;
                        } else {
                            angle = Integer.parseInt(args[2].substring(1));
                        }
                    } else {
                        angle = Integer.parseInt(args[2]);
                    }
                    try {
                        ArmorStandPoser asp = new ArmorStandPoser(as);
                        BodyPart bp = BodyPart.fromString(args[0]);
                        Axis a = Axis.fromString(args[1]);
                        int n = asp.setSingleAngle(bp, a, angle, relative);
                        player.sendMessage(ChatColor.GREEN + "Set " + bp.name().toLowerCase() + "'s " + a.name().toLowerCase() + " to " + ChatColor.YELLOW + n + ChatColor.GREEN + "!");
                        return true;
                    } catch(IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + e.getMessage());
                    }
                } catch(NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Your third argument " + args[1] + " is not a number!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.pose");
            }
        } else if(args.length == 4) {
            if(args[0].equalsIgnoreCase("move") || args[0].equalsIgnoreCase("mv")) {
                if(player.hasPermission("editarmorstands.command.move")) {
                    Location loc = as.getLocation();
                    int[] blockLoc = new int[]{loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
                    double[] locArr = new double[]{loc.getX(), loc.getY(), loc.getZ()};
                    double[] oldLocArr = new double[]{loc.getX(), loc.getY(), loc.getZ()};
                    for(int i = 0; i < 3; i++) {
                        try {
                            if(args[i + 1].startsWith("~")) {
                                if(args[i + 1].length() != 1) {
                                    locArr[i] += Double.parseDouble(args[i + 1].substring(1));
                                }
                            } else {
                                locArr[i] = Double.parseDouble(args[i + 1]);
                            }
                            if(Math.floor(locArr[i]) != blockLoc[i] && !player.hasPermission("editarmorstands.command.move.nextblock") && !player.hasPermission("editarmorstands.command.move.unlimited")) {
                                player.sendMessage(ChatColor.RED + "You can only manipulate the position of an Armor Stands on the " + ChatColor.GOLD + "same block" + ChatColor.RED + ", not move it onto another block! (" + args[i + 1] + " would move it onto another one!)");
                                return false;
                            } else if(Math.abs(locArr[i] - oldLocArr[i]) > 1 && !player.hasPermission("editarmorstands.command.move.unlimited")) {
                                player.sendMessage(ChatColor.RED + "You can't move Armor Stands more than " + ChatColor.GOLD + "one block" + ChatColor.RED + "! You inputted " + ChatColor.GOLD + args[i + 1] + ChatColor.RED + "!");
                                return false;
                            }
                        } catch(NumberFormatException e) {
                            player.sendMessage(ChatColor.GOLD + args[i + 1].substring(1) + ChatColor.RED + " is not a valid double!");
                            return false;
                        }
                    }
                    loc.setX(locArr[0]);
                    loc.setY(locArr[1]);
                    loc.setZ(locArr[2]);
                    as.teleport(loc);
                    player.sendMessage(ChatColor.GREEN + "Moved the Armor Stand to " + ChatColor.YELLOW + df.format(loc.getX()) + " / " + ChatColor.YELLOW + df.format(loc.getY()) + " / " + ChatColor.YELLOW + df.format(loc.getZ()) + ChatColor.GREEN + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.move");
                }
            } else if(player.hasPermission("editarmorstands.command.pose")) {
                ArmorStandPoser asp = new ArmorStandPoser(as);
                try {
                    int x;
                    int y;
                    int z;
                    boolean rx = false;
                    boolean ry = false;
                    boolean rz = false;

                    if(args[1].startsWith("~")) {
                        rx = true;
                        if(args[1].length() == 1)
                            x = 0;
                        else
                            x = Integer.parseInt(args[1].substring(1));
                    } else
                        x = Integer.parseInt(args[1]);

                    if(args[2].startsWith("~")) {
                        ry = true;
                        if(args[2].length() == 1)
                            y = 0;
                        else
                            y = Integer.parseInt(args[2].substring(1));
                    } else
                        y = Integer.parseInt(args[2]);

                    if(args[3].startsWith("~")) {
                        rz = true;
                        if(args[3].length() == 1)
                            z = 0;
                        else
                            z = Integer.parseInt(args[3].substring(1));
                    } else
                        z = Integer.parseInt(args[3]);

                    try {
                        BodyPart bp = BodyPart.fromString(args[0]);
                        int[] r = asp.setEulerAngle(bp, new int[]{x, y, z}, new boolean[]{rx, ry, rz});
                        player.sendMessage(ChatColor.GREEN + "Set " + bp.name().toLowerCase() + " to " + ChatColor.YELLOW + r[0] + " " + r[1] + " " + r[2] + ChatColor.GREEN + "!");
                        return true;
                    } catch(IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + e.getMessage());
                    }

                } catch(NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "One of " + args[1] + ", " + args[2] + " or " + args[3] + " is not a valid number!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.pose");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Error. You inputted more then 4 arguments!");
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorStandClick(PlayerInteractAtEntityEvent event) {
        if(!event.isCancelled() && event.getRightClicked() instanceof ArmorStand) {
            PlayerInteractEntityEvent pie = new PlayerInteractEntityEvent(event.getPlayer(), event.getRightClicked());
            getServer().getPluginManager().callEvent(pie);
            if(pie.isCancelled()) {
                return;
            }
            ArmorStand armorStand = (ArmorStand) event.getRightClicked();
            if((persistent.contains(event.getPlayer().getUniqueId()) || clickTimeout.containsKey(event.getPlayer().getUniqueId())) && waitingCommands.containsKey(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
                if(persistent.contains(event.getPlayer().getUniqueId()) || clickTimeout.get(event.getPlayer().getUniqueId()) + 10 * 1000 > System.currentTimeMillis()) {
                    calculateAction(event.getPlayer(), armorStand, waitingCommands.get(event.getPlayer().getUniqueId()));
                } else {
                    waitingCommands.remove(event.getPlayer().getUniqueId());
                    event.getPlayer().sendMessage(ChatColor.RED + "Your click action expired!");
                }
                clickTimeout.remove(event.getPlayer().getUniqueId());
                return;
            }
            if(event.getPlayer().getItemInHand().getType() == Material.NAME_TAG) {
                ItemStack hi = event.getPlayer().getItemInHand();
                if(hi.hasItemMeta() && hi.getItemMeta().hasDisplayName()) {
                    if(event.getPlayer().hasPermission("editarmorstands.nametag.name")) {
                        event.setCancelled(true);
                        String name = hi.getItemMeta().getDisplayName();
                        if(event.getPlayer().hasPermission("editarmorstands.nametag.name.colored")) {
                            name = ChatColor.translateAlternateColorCodes('&', name);
                        } else {
                            name = ChatColor.ITALIC + name;
                        }
                        armorStand.setCustomName(name + ChatColor.RESET);
                        armorStand.setCustomNameVisible(true);
                        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                            if(hi.getAmount() > 1) {
                                hi.setAmount(hi.getAmount() - 1);
                            } else {
                                event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
                            }
                            event.getPlayer().updateInventory();
                        }
                        return;
                    }
                } else if(event.getPlayer().hasPermission("editarmorstands.nametag.name.clear")) {
                    event.setCancelled(true);
                    armorStand.setCustomName("");
                    armorStand.setCustomNameVisible(false);
                    return;
                }
            }
            if(!event.getPlayer().isSneaking() && event.getPlayer().hasPermission("editarmorstands.openinventory")) {
                event.setCancelled(true);
                ArmorStandGui gui = new ArmorStandGui(this, armorStand, event.getPlayer());
                gui.show();
            }
        }
    }

    @EventHandler
    public void onArmorStandDestroy(EntityDamageEvent event) {
        if(event.getEntity().getType() == EntityType.ARMOR_STAND && selectedArmorStands.containsValue(event.getEntity().getUniqueId())) {
            Iterator<UUID> it = selectedArmorStands.values().iterator();
            while (it.hasNext()) {
                if (event.getEntity().getUniqueId().equals(it.next())) {
                    it.remove();
                }
            }
        }
    }

    @EventHandler
    public void onArmorStandPlace(PlayerInteractEvent event) {
        if(event.isCancelled())
            return;

        boolean isArmorStandPlacement = event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null && event.getItem().getType() == Material.ARMOR_STAND;
        boolean isNamedArmorStand = isArmorStandPlacement && event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName();

        if(isNamedArmorStand && event.getPlayer().hasPermission("editarmorstands.place.name")) {
            String name = event.getItem().getItemMeta().getDisplayName();
            if(event.getPlayer().hasPermission("editarmorstands.place.name.colored")) {
                name = ChatColor.translateAlternateColorCodes('&', name);
            } else {
                name = ChatColor.ITALIC + name;
            }
            final Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
            final String finalName = name + ChatColor.RESET;
            final Listener spawnListener = new Listener() {
                @EventHandler
                public void onArmorStandSpawn(CreatureSpawnEvent event) {
                    if(event.getEntity().getType() == EntityType.ARMOR_STAND && event.getEntity().getLocation().getBlock().getLocation().equals(loc)) {
                        event.getEntity().setCustomName(finalName);
                        event.getEntity().setCustomNameVisible(true);
                    }
                }
            };
            getServer().getPluginManager().registerEvents(spawnListener, this);
            getServer().getScheduler().runTaskLater(this, new Runnable() {
                public void run() {
                    HandlerList.unregisterAll(spawnListener);
                }
            }, 20);
        }
    }
}
