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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Phoenix616 on 18.03.2015.
 */
public class EditArmorStands extends JavaPlugin implements Listener, CommandExecutor {
    
    HashMap<UUID,Long> clickTimeout = new HashMap<UUID,Long>();
    
    HashMap<UUID,UUID> selectedArmorStands = new HashMap<UUID,UUID>();
    
    HashMap<UUID,String[]> waitingCommands = new HashMap<UUID,String[]>();

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(args.length == 0) {
                sender.sendMessage(ChatColor.GREEN + "Rightclick on the ArmorStand you want to edit in the next 10s!");
                clickTimeout.put(p.getUniqueId(), System.currentTimeMillis());
                waitingCommands.put(p.getUniqueId(), args);
            } else {
                if(args[0].equalsIgnoreCase("exit")) {
                    if(selectedArmorStands.remove(p.getUniqueId()) != null) {
                        sender.sendMessage(ChatColor.GREEN + "Exited ArmorStand editing mode!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You are currently not editing any ArmorStands!");
                    }
                } else if(args[0].equalsIgnoreCase("usage") || args[0].equalsIgnoreCase("help") ) {
                    sender.sendMessage(ChatColor.GREEN + "EditArmorStands v" + this.getDescription().getVersion() + " Usage:");
                    return false;
                } else {
                    if (selectedArmorStands.containsKey(p.getUniqueId())) {
                        UUID asid = selectedArmorStands.get(p.getUniqueId());
                        ArmorStand as = null;
                        for (Entity e : p.getNearbyEntities(64, 64, 64))
                            if (e.getType() == EntityType.ARMOR_STAND && e.getUniqueId() == asid) {
                                as = (ArmorStand) e;
                                break;
                            }
                        if (as != null) {
                            return calculateAction(p, as, args);
                        } else {
                            sender.sendMessage(ChatColor.RED + "You can only edit ArmorStands in a 64 block radius!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Rightclick on the ArmorStand you want to edit in the next 10s!");
                        clickTimeout.put(p.getUniqueId(), System.currentTimeMillis());
                        waitingCommands.put(p.getUniqueId(), args);
                    }
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
            player.sendMessage(ChatColor.GREEN + "Selected ArmorStand at " + ChatColor.YELLOW + as.getLocation().getBlockX() + "/" + as.getLocation().getBlockY() + "/" + as.getLocation().getBlockZ() + ChatColor.GREEN + "!");
            player.getPlayer().sendMessage(ChatColor.GREEN + "You can now use " + ChatColor.YELLOW + "/eas <option> <value> " + ChatColor.GREEN + "to edit the properties of this ArmorStand! To exit the editing mode run " + ChatColor.YELLOW + "/eas exit" + ChatColor.GREEN + "!");
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
                player.sendMessage(ChatColor.GREEN + "Set the ArmorStand's name to " + ChatColor.RESET + as.getCustomName() + ChatColor.GREEN + "!");
            } else {
                player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.name");
            }
        } else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("namevisible")) {
                if(player.hasPermission("editarmorstands.command.namevisible")) {
                    as.setCustomNameVisible(!as.isCustomNameVisible());
                    player.sendMessage(ChatColor.GREEN + "The ArmorStand's name is now " + ChatColor.YELLOW + (as.isCustomNameVisible() ? "" : "in") + "visible" + ChatColor.GREEN + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.namevisible");
                }
            } else if(args[0].equalsIgnoreCase("arms")) {
                if(player.hasPermission("editarmorstands.command.arms")) {
                    as.setArms(!as.hasArms());
                    player.sendMessage(ChatColor.GREEN + "ArmorStand has now " + ChatColor.YELLOW + (as.hasArms() ? "" : "no ") + "arms" + ChatColor.GREEN + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.arms");
                }
            } else if(args[0].equalsIgnoreCase("base")) {
                if(player.hasPermission("editarmorstands.command.base")) {
                    as.setBasePlate(!as.hasBasePlate());
                    player.sendMessage(ChatColor.GREEN + "ArmorStand has now " + ChatColor.YELLOW + (as.hasBasePlate() ? "a" : "no") + " baseplate" + ChatColor.GREEN + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.base");
                }
            } else if(args[0].equalsIgnoreCase("gravity")) {
                if(player.hasPermission("editarmorstands.command.gravity")) {
                    as.setGravity(!as.hasGravity());
                    player.sendMessage(ChatColor.GREEN + "ArmorStand has now " + ChatColor.YELLOW + (as.hasGravity() ? "" : "no ") + "gravity" + ChatColor.GREEN + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.gravity");
                }
            } else if(args[0].equalsIgnoreCase("size")) {
                if(player.hasPermission("editarmorstands.command.size")) {
                    as.setSmall(!as.isSmall());
                    player.sendMessage(ChatColor.GREEN + "ArmorStand is now " + ChatColor.YELLOW + (as.isSmall() ? "small" : "big") + ChatColor.GREEN + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.size");
                }
            } else if(args[0].equalsIgnoreCase("visible")) {
                if (player.hasPermission("editarmorstands.command.visible")) {
                    as.setVisible(!as.isVisible());
                    player.sendMessage(ChatColor.GREEN + "ArmorStand is now " + ChatColor.YELLOW + (as.isVisible() ? "" : "in") + "visible" + ChatColor.GREEN + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command.visible");
                }
                /* TODO: Find out why this isn't working!
                } else if(sender.hasPermission("editarmorstands.command.pose")) {
                    ArmorStandPoser asp = new ArmorStandPoser(as);
                    if(asp.translatePlayerLook(args[0], p.getEyeLocation())) {
                        sender.sendMessage(ChatColor.GREEN + "Set " + args[0] + " rotation to your head's view!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Sorry but the option " + args[0] + " doesn't exist!");
                    };*/
            } else {
                return false;
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
                        player.sendMessage(ChatColor.GREEN + "Set ArmorStands rotation to " + ChatColor.YELLOW + angle + ChatColor.GREEN + "!");
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
                            if (asp.setSingleAngle(bp, a, angle, relative)) {
                                player.sendMessage(ChatColor.GREEN + "Set " + bp.name().toLowerCase() + "'s " + a.name().toLowerCase() + " to " + args[2] + "!");
                            } else {
                                player.sendMessage(ChatColor.RED + "Fatal error! Please report this bug immediately! asp.setSingleAngle(" + bp + ", " + a + ", " + angle + ", " + relative + ") is false?");
                            }
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
                    boolean relative = false;

                    if (args[1].startsWith("~")) {
                        relative = true;
                        if (args[1].length() == 1)
                            x = 0;
                        else
                            x = Integer.parseInt(args[1].substring(1));
                    } else
                        x = Integer.parseInt(args[1]);

                    if (args[2].startsWith("~")) {
                        relative = true;
                        if (args[2].length() == 1)
                            y = 0;
                        else
                            y = Integer.parseInt(args[2].substring(1));
                    } else
                        y = Integer.parseInt(args[2]);

                    if (args[3].startsWith("~")) {
                        relative = true;
                        if (args[3].length() == 1)
                            z = 0;
                        else
                            z = Integer.parseInt(args[3].substring(1));
                    } else
                        z = Integer.parseInt(args[3]);

                    try {
                        BodyPart bp = BodyPart.fromString(args[0]);
                        if (asp.setEulerAngle(bp, x, y, z, relative)) {
                            player.sendMessage(ChatColor.GREEN + "Set " + bp.name().toLowerCase() + " to " + args[1] + " " + args[2] + " " + args[3] + "!");
                        } else {
                            player.sendMessage(ChatColor.RED + "Fatal error! Please report this bug immediately! asp.setEulerAngle(" + bp + ", " + x + ", " + y + ", " + z + ", " + relative + ") is false?");
                        }
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
            return false;
        }
        return true;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorStandClick(PlayerInteractAtEntityEvent event) {
        if(!event.isCancelled() && event.getRightClicked().getType() == EntityType.ARMOR_STAND) {
            if(clickTimeout.containsKey(event.getPlayer().getUniqueId()) && waitingCommands.containsKey(event.getPlayer().getUniqueId()) && clickTimeout.get(event.getPlayer().getUniqueId()) + 10 * 1000 > System.currentTimeMillis()) {
                event.setCancelled(calculateAction(event.getPlayer(), (ArmorStand) event.getRightClicked(), waitingCommands.get(event.getPlayer().getUniqueId())));
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
