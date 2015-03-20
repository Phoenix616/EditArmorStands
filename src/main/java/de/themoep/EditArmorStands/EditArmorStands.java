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
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Created by Phoenix616 on 18.03.2015.
 */
public class EditArmorStands extends JavaPlugin implements Listener, CommandExecutor {
    
    HashMap<UUID,Long> clickTimeout = new HashMap<UUID,Long>();
    
    HashMap<UUID,UUID> selectedArmorStands = new HashMap<UUID,UUID>();
    
    HashMap<UUID,String[]> waitingCommands = new HashMap<UUID,String[]>();

    static List<String> toggleOptions = Arrays.asList(new String[]{"namevisible", "gravity", "visible", "base", "arms", "size"});
    
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(args.length == 0) {
                sender.sendMessage(ChatColor.GREEN + "Rightclick on the Armor Stand you want to edit in the next " + ChatColor.YELLOW + "10s" + ChatColor.GREEN + "!");
                clickTimeout.put(p.getUniqueId(), System.currentTimeMillis());
                waitingCommands.put(p.getUniqueId(), args);
            } else if(args[0].equalsIgnoreCase("exit")) {
                if(clickTimeout.containsKey(p.getUniqueId()) && clickTimeout.get(p.getUniqueId()) + 10 * 1000 < System.currentTimeMillis()){
                    sender.sendMessage(ChatColor.GREEN + "Canceled pending click action!");
                } else{
                    sender.sendMessage(ChatColor.GREEN + "Exited Armor Stand editing mode!");
                }
                selectedArmorStands.remove(p.getUniqueId());
                waitingCommands.remove(p.getUniqueId());
                clickTimeout.remove(p.getUniqueId());
            } else if(args[0].equalsIgnoreCase("usage") || args[0].equalsIgnoreCase("help") ) {
                List<String> usage = new ArrayList<String>();

                usage.add("--- &6EditArmorStands v" + this.getDescription().getVersion() + " Usage:&r ---");
                
                usage.add("&e/editarmorstand &rAlias: &e/eas&r)");
                usage.add("&r - Rightclick an Armor Stand in the next 10s to select it");
                usage.add("&e/eas exit");
                usage.add("&r - Exit the editing mode");
                if(sender.hasPermission("editarmorstands.command.name")) {
                    usage.add("&e/eas name <name>");
                    usage.add("&r - Set the Armor Stand's name" + (sender.hasPermission("editarmorstands.command.name.colored") ? ", use & for colorcodes" : ""));
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
                    usage.add("&e/eas <bodypart> &r[&epitch&r|&eyaw&r|&eroll&r] <degree>");
                    usage.add("&r - Set an angle, use ~ for relatives");
                    usage.add("&e/eas <bodypart> <pitch> <yaw> <roll>");
                    usage.add("&r - Set all angles of a body part at once, use ~ for relatives");
                    usage.add("&eAvailable bodyparts: head, body, leftarm, rightarm, leftleg, rightleg. (Short forms: h, b, la, ra, ll, rl)");
                }
                
                for(String s : usage)
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
            } else {
                try {
                    BodyPart.fromString(args[0]);
                    if(!sender.hasPermission("editarmorstands.command.pose")) {
                        sender.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.pose");
                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    if(args[0].equalsIgnoreCase("name") || toggleOptions.contains(args[0].toLowerCase())) {
                        if(!sender.hasPermission("editarmorstands.command." + args[0].toLowerCase())) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command." + args[0].toLowerCase());
                            return true;
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "The argument " + ChatColor.YELLOW + args[0].toLowerCase() + ChatColor.RED + " doesn't exist. Use " + ChatColor.YELLOW + "/eas help" + ChatColor.RED + " to get all available toggleOptions!");
                        return true;
                    }
                }
                if (selectedArmorStands.containsKey(p.getUniqueId())) {
                    UUID asid = selectedArmorStands.get(p.getUniqueId());
                    ArmorStand as = null;
                    for (Entity e : p.getNearbyEntities(64, 64, 64))
                        if (e.getType() == EntityType.ARMOR_STAND && e.getUniqueId() == asid) {
                            as = (ArmorStand) e;
                            break;
                        }
                    if (as != null) {
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
                String name = "";
                for(int i = 1; i < args.length; i++) {
                    name += args[i] + " ";
                }
                if (player.hasPermission("editarmorstands.command.name.colored")) {
                    name = ChatColor.translateAlternateColorCodes('&', name);
                }
                as.setCustomName(name.trim() + ChatColor.RESET);
                player.sendMessage(ChatColor.GREEN + "Set the Armor Stand's name to " + ChatColor.RESET + as.getCustomName() + ChatColor.GREEN + "!");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.name");
            }
        } else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("namevisible")) {
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
                if (player.hasPermission("editarmorstands.command.visible")) {
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
        } else if (args.length == 2) {
            if(player.hasPermission("editarmorstands.command.pose")) {
                try {
                    int angle;
                    boolean relative = false;

                    if (args[1].startsWith("~")) {
                        relative = true;
                        if (args[1].length() == 1) {
                            angle = 0;
                        } else {
                            angle = Integer.parseInt(args[1].substring(1));
                        }
                    } else {
                        angle = Integer.parseInt(args[1]);
                    }                    
                    if (args[0].equalsIgnoreCase("p") || args[0].equalsIgnoreCase("pitch")) {
                        Location l = as.getLocation();
                        if(relative)
                            angle += l.getPitch();
                        l.setPitch(angle);
                        as.teleport(l);
                    } else if (args[0].equalsIgnoreCase("y") || args[0].equalsIgnoreCase("yaw") || args[0].equalsIgnoreCase("r") || args[0].equalsIgnoreCase("rotate") || args[0].equalsIgnoreCase("rotation")) {
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
                } catch (NumberFormatException e) {
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

                    if (args[2].startsWith("~")) {
                        relative = true;
                        if (args[2].length() == 1) {
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
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + e.getMessage());
                        }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Your third argument " + args[1] + " is not a number!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.pose");
            }
        } else if(args.length == 4) {
            if (player.hasPermission("editarmorstands.command.pose")) {
                ArmorStandPoser asp = new ArmorStandPoser(as);
                try {
                    int x;
                    int y;
                    int z;
                    boolean rx = false;
                    boolean ry = false;
                    boolean rz = false;

                    if (args[1].startsWith("~")) {
                        rx = true;
                        if (args[1].length() == 1)
                            x = 0;
                        else
                            x = Integer.parseInt(args[1].substring(1));
                    } else
                        x = Integer.parseInt(args[1]);

                    if (args[2].startsWith("~")) {
                        ry = true;
                        if (args[2].length() == 1)
                            y = 0;
                        else
                            y = Integer.parseInt(args[2].substring(1));
                    } else
                        y = Integer.parseInt(args[2]);

                    if (args[3].startsWith("~")) {
                        rz = true;
                        if (args[3].length() == 1)
                            z = 0;
                        else
                            z = Integer.parseInt(args[3].substring(1));
                    } else
                        z = Integer.parseInt(args[3]);

                    try {
                        BodyPart bp = BodyPart.fromString(args[0]);
                        int[] r = asp.setEulerAngle(bp, new int[] {x,y,z}, new boolean[] {rx, ry, rz});
                        player.sendMessage(ChatColor.GREEN + "Set " + bp.name().toLowerCase() + " to " + ChatColor.YELLOW + r[0] + " " + r[1] + " " + r[2] + ChatColor.GREEN + "!");
                        return true;
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + e.getMessage());
                    }

                } catch (NumberFormatException e) {
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
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorStandClick(PlayerInteractAtEntityEvent event) {
        if(!event.isCancelled() && event.getRightClicked().getType() == EntityType.ARMOR_STAND) {
            if(clickTimeout.containsKey(event.getPlayer().getUniqueId()) && waitingCommands.containsKey(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
                if(clickTimeout.get(event.getPlayer().getUniqueId()) + 10 * 1000 > System.currentTimeMillis()) {
                    if(calculateAction(event.getPlayer(), (ArmorStand) event.getRightClicked(), waitingCommands.get(event.getPlayer().getUniqueId()))) {
                        clickTimeout.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
                    }
                } else {
                    clickTimeout.remove(event.getPlayer().getUniqueId());
                    waitingCommands.remove(event.getPlayer().getUniqueId());
                    event.getPlayer().sendMessage(ChatColor.RED + "Your click action expired!");
                }
            } else if (event.getPlayer().getItemInHand().getType() == Material.NAME_TAG) {
                ItemStack hi = event.getPlayer().getItemInHand();
                if(hi.hasItemMeta() && hi.getItemMeta().hasDisplayName()) {
                    if(event.getPlayer().hasPermission("editarmorstands.nametag.name")) {
                        event.setCancelled(true);
                        String name = hi.getItemMeta().getDisplayName();
                        if (event.getPlayer().hasPermission("editarmorstands.nametag.name.colored")) {
                            name = ChatColor.translateAlternateColorCodes('&', name);
                        }
                        event.getRightClicked().setCustomName(name + ChatColor.RESET);
                        event.getRightClicked().setCustomNameVisible(true);
                        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                            if (hi.getAmount() > 1) {
                                hi.setAmount(hi.getAmount() - 1);
                            } else {
                                event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
                            }
                            event.getPlayer().updateInventory();
                        }
                    }
                } else if(event.getPlayer().hasPermission("editarmorstands.nametag.name.clear")) {
                    event.setCancelled(true);
                    event.getRightClicked().setCustomName("");
                    event.getRightClicked().setCustomNameVisible(false);
                }
            }
        }
    }

    @EventHandler
    public void onArmorStandDestroy(EntityDamageEvent event) {
        if(event.getEntity().getType() == EntityType.ARMOR_STAND && selectedArmorStands.containsValue(event.getEntity().getUniqueId()))
            for (Map.Entry<UUID, UUID> e : selectedArmorStands.entrySet())
                if (e.getValue().equals(event.getEntity().getUniqueId()))
                    selectedArmorStands.remove(e.getKey());
    }
}
