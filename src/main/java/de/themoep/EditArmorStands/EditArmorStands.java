package de.themoep.EditArmorStands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
 * EditArmorStands - Plugin to edit armor stand poses and options
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

public class EditArmorStands extends JavaPlugin {

    private Map<UUID, UUID> selectedArmorStands = new HashMap<>();

    private Cache<UUID, String[]> waitingCommands = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

    private Map<UUID, String[]> persistent = new HashMap<>();

    private Cache<UUID, ArmorStandData> clipboard = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    private static DecimalFormat df = new DecimalFormat("#.##");

    private int serverVersion = 0;

    public void onEnable() {
        Matcher versionMatcher = Pattern.compile(".*\\(MC: (.*)\\)").matcher(getServer().getVersion());
        if (versionMatcher.find()) {
            String[] version = versionMatcher.group(1).split("[.]");
            for (int i = 0; i < version.length && i < 3; i++) {
                try {
                    int n = Integer.parseInt(version[i]);
                    serverVersion += n * Math.pow(100, 2 - i);
                } catch (NumberFormatException e) {
                    getLogger().warning("Could not parse " + version[i] + " as an integer?");
                }
            }
        }

        if (serverVersion != 0) {
            getLogger().info("Detect server " + serverVersion);
            if (serverVersion < 10800) {
                getLogger().warning("Armor Stands weren't in Minecraft before 1.8? What are you trying to do with this plugin?");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } else {
            getLogger().warning("Could not detect server version!");
        }

        getServer().getPluginManager().registerEvents(new ArmorStandListener(this), this);
        getCommand("editarmorstand").setExecutor(new EditArmorStandsCommand(this));
    }

    /**
     * Get the server version in the format MajorMinorPatch
     * E.g. 1.8.2 would end up being 10802 and 1.11.2 11102
     */
    public int getServerVersion() {
        return serverVersion;
    }

    boolean calculateAction(Player player, ArmorStand as, String[] args) {
        if (args.length == 0) {
            addSelection(player, as);
            player.sendMessage(ChatColor.GREEN + "Selected Armor Stand at " + ChatColor.YELLOW + as.getLocation().getBlockX() + "/" + as.getLocation().getBlockY() + "/" + as.getLocation().getBlockZ() + ChatColor.GREEN + "!");
            player.getPlayer().sendMessage(ChatColor.GREEN + "You can now use " + ChatColor.YELLOW + "/eas <option> <value> " + ChatColor.GREEN + "to edit the properties of this Armor Stand! To exit the editing mode run " + ChatColor.YELLOW + "/eas exit" + ChatColor.GREEN + "!");
            return true;
        } else if ("name".equalsIgnoreCase(args[0])) {
            if (player.hasPermission("editarmorstands.command.name")) {
                if (args.length > 1) {
                    String name = "";
                    for (int i = 1; i < args.length; i++) {
                        name += args[i] + " ";
                    }
                    if (player.hasPermission("editarmorstands.command.name.colored")) {
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
        } else if ("paste".equalsIgnoreCase(args[0])) {
            ArmorStandData data = getClipboard(player.getUniqueId());
            if (data == null) {
                player.sendMessage(ChatColor.RED + "You don't have a copy in your clipboard?");
                return true;
            }

            Set<String> argSet = Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).map(String::toLowerCase).collect(Collectors.toSet());
            if ((argSet.isEmpty() || argSet.contains("items")) && player.hasPermission("editarmorstands.command.paste.items")) {
                data.applyItems(as);
            }
            if ((argSet.isEmpty() || argSet.contains("pose")) && player.hasPermission("editarmorstands.command.paste.pose")) {
                data.applyPose(as);
            }
            if ((argSet.isEmpty() || argSet.contains("settings")) && player.hasPermission("editarmorstands.command.paste.settings")) {
                data.applySettings(as);
            }
            if ((argSet.isEmpty() || argSet.contains("name")) && player.hasPermission("editarmorstands.command.paste.name")) {
                data.applyName(as);
            }
            player.sendMessage(ChatColor.GREEN + "Pasted clipboard data on Armor Stand!");
            return true;
        } else if (args.length == 1) {
            if (!player.hasPermission("editarmorstands.command." + args[0].toLowerCase())) {
                player.sendMessage(ChatColor.RED + "You don't have the permission editarmorstands.command." + args[0].toLowerCase());
                return false;
            }

            if ("info".equalsIgnoreCase(args[0])) {
                List<String> info = new ArrayList<>();
                info.add(ChatColor.YELLOW + "--- Info about the Armor Stand" + (as.getCustomName() != null ? ChatColor.RESET + " " + as.getCustomName() : "") + " --- ");
                info.add(ChatColor.YELLOW + "Position: " + ChatColor.RESET + as.getLocation().getX() + " " + as.getLocation().getY() + " " + as.getLocation().getZ());
                info.add(ChatColor.YELLOW + "Rotation: " + ChatColor.RESET + as.getLocation().getYaw());
                info.add(ChatColor.YELLOW + "Head:");
                info.add(ChatColor.YELLOW + "  Yaw: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.YAW, as.getHeadPose())
                        + ChatColor.YELLOW + " Pitch: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.PITCH, as.getHeadPose())
                        + ChatColor.YELLOW + " Roll: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.ROLL, as.getHeadPose()));
                info.add(ChatColor.YELLOW + "Body:");
                info.add(ChatColor.YELLOW + "  Yaw: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.YAW, as.getBodyPose())
                        + ChatColor.YELLOW + " Pitch: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.PITCH, as.getBodyPose())
                        + ChatColor.YELLOW + " Roll: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.ROLL, as.getBodyPose()));
                info.add(ChatColor.YELLOW + "Left arm:");
                info.add(ChatColor.YELLOW + "  Yaw: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.YAW, as.getLeftArmPose())
                        + ChatColor.YELLOW + " Pitch: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.PITCH, as.getLeftArmPose())
                        + ChatColor.YELLOW + " Roll: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.ROLL, as.getLeftArmPose()));
                info.add(ChatColor.YELLOW + "Right arm:");
                info.add(ChatColor.YELLOW + "  Yaw: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.YAW, as.getRightArmPose())
                        + ChatColor.YELLOW + " Pitch: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.PITCH, as.getRightArmPose())
                        + ChatColor.YELLOW + " Roll: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.ROLL, as.getRightArmPose()));
                info.add(ChatColor.YELLOW + "Left leg:");
                info.add(ChatColor.YELLOW + "  Yaw: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.YAW, as.getLeftLegPose())
                        + ChatColor.YELLOW + " Pitch: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.PITCH, as.getLeftLegPose())
                        + ChatColor.YELLOW + " Roll: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.ROLL, as.getLeftLegPose()));
                info.add(ChatColor.YELLOW + "Right leg:");
                info.add(ChatColor.YELLOW + "  Yaw: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.YAW, as.getRightLegPose())
                        + ChatColor.YELLOW + " Pitch: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.PITCH, as.getRightLegPose())
                        + ChatColor.YELLOW + " Roll: " + ChatColor.RESET + ArmorStandPoser.getDegreeAngleFromEuler(Axis.ROLL, as.getRightLegPose()));
                info.add(ChatColor.YELLOW + "Name visible: " + (as.isCustomNameVisible() ? ChatColor.GREEN : ChatColor.RED) + as.isCustomNameVisible());
                info.add(ChatColor.YELLOW + "Visible: " + (as.isVisible() ? ChatColor.GREEN : ChatColor.RED) + as.isVisible());
                info.add(ChatColor.YELLOW + "Gravity: " + (as.hasGravity() ? ChatColor.GREEN : ChatColor.RED) + as.hasGravity());
                info.add(ChatColor.YELLOW + "Glowing: " + (as.isGlowing() ? ChatColor.GREEN : ChatColor.RED) + as.isGlowing());
                info.add(ChatColor.YELLOW + "Invulnerable: " + (as.isInvulnerable() ? ChatColor.GREEN : ChatColor.RED) + as.isInvulnerable());
                info.add(ChatColor.YELLOW + "Arms: " + (as.hasArms() ? ChatColor.GREEN : ChatColor.RED) + as.hasArms());
                info.add(ChatColor.YELLOW + "Base: " + (as.hasBasePlate() ? ChatColor.GREEN : ChatColor.RED) + as.hasBasePlate());
                info.add(ChatColor.YELLOW + "Small: " + (as.isSmall() ? ChatColor.GREEN : ChatColor.RED) + as.isSmall());
                info.add(ChatColor.YELLOW + "Marker: " + (as.isMarker() ? ChatColor.GREEN : ChatColor.RED) + as.isMarker());

                for (String i : info) {
                    player.sendMessage(i);
                }
                return true;
            } else if ("copy".equalsIgnoreCase(args[0])) {
                clipboard.put(player.getUniqueId(), new ArmorStandData(as));
                player.sendMessage(ChatColor.GREEN + "Armor Stand data copied! " + ChatColor.GRAY + "Your clipboard is cleared after ten minutes!");
                return true;
            } else if ("items".equalsIgnoreCase(args[0])) {
                ArmorStandGui gui = new ArmorStandGui(this, as, player);
                gui.show();
                return true;
            } else if ("namevisible".equalsIgnoreCase(args[0])) {
                as.setCustomNameVisible(!as.isCustomNameVisible());
                player.sendMessage(ChatColor.GREEN + "The Armor Stand's name is now " + ChatColor.YELLOW + (as.isCustomNameVisible() ? "" : "in") + "visible" + ChatColor.GREEN + "!");
                return true;
            } else if ("arms".equalsIgnoreCase(args[0])) {
                as.setArms(!as.hasArms());
                player.sendMessage(ChatColor.GREEN + "Armor Stand has now " + ChatColor.YELLOW + (as.hasArms() ? "" : "no ") + "arms" + ChatColor.GREEN + "!");
                return true;
            } else if ("base".equalsIgnoreCase(args[0])) {
                as.setBasePlate(!as.hasBasePlate());
                player.sendMessage(ChatColor.GREEN + "Armor Stand has now " + ChatColor.YELLOW + (as.hasBasePlate() ? "a" : "no") + " baseplate" + ChatColor.GREEN + "!");
                return true;
            } else if ("gravity".equalsIgnoreCase(args[0])) {
                as.setGravity(!as.hasGravity());
                player.sendMessage(ChatColor.GREEN + "Armor Stand has now " + ChatColor.YELLOW + (as.hasGravity() ? "" : "no ") + "gravity" + ChatColor.GREEN + "!");
                return true;
            } else if ("size".equalsIgnoreCase(args[0])) {
                as.setSmall(!as.isSmall());
                player.sendMessage(ChatColor.GREEN + "Armor Stand is now " + ChatColor.YELLOW + (as.isSmall() ? "small" : "big") + ChatColor.GREEN + "!");
                return true;
            } else if ("visible".equalsIgnoreCase(args[0])) {
                as.setVisible(!as.isVisible());
                player.sendMessage(ChatColor.GREEN + "Armor Stand is now " + ChatColor.YELLOW + (as.isVisible() ? "" : "in") + "visible" + ChatColor.GREEN + "!");
                return true;
            } else if ("glowing".equalsIgnoreCase(args[0])) {
                as.setGlowing(!as.isGlowing());
                player.sendMessage(ChatColor.GREEN + "Armor Stand is " + ChatColor.YELLOW + (as.isGlowing() ? "now" : "no longer") + " glowing" + ChatColor.GREEN + "!");
                return true;
            } else if ("invulnerable".equalsIgnoreCase(args[0])) {
                as.setInvulnerable(!as.isInvulnerable());
                player.sendMessage(ChatColor.GREEN + "Armor Stand is now " + ChatColor.YELLOW + (as.isInvulnerable() ? "in" : "") + "vulnerable" + ChatColor.GREEN + "!");
                return true;
            } else if ("marker".equalsIgnoreCase(args[0])) {
                as.setMarker(!as.isMarker());
                player.sendMessage(ChatColor.GREEN + "Armor Stand is " + ChatColor.YELLOW + (as.isMarker() ? "now" : "no longer") + " a marker" + ChatColor.GREEN + "!");
                return true;
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
            if (player.hasPermission("editarmorstands.command.pose")) {
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
                    if ("p".equalsIgnoreCase(args[0]) || "pitch".equalsIgnoreCase(args[0])) {
                        Location l = as.getLocation();
                        if (relative)
                            angle += l.getPitch();
                        l.setPitch(angle);
                        as.teleport(l);
                        player.sendMessage(ChatColor.GREEN + "Set Armor Stand's pitch to " + ChatColor.YELLOW + angle + ChatColor.GREEN + "!");
                        return true;
                    } else if ("rotate".equalsIgnoreCase(args[0])) {
                        Location l = as.getLocation();
                        if (relative)
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
        } else if (args.length == 3) {
            if (player.hasPermission("editarmorstands.command.pose")) {
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
        } else if (args.length == 4) {
            if ("move".equalsIgnoreCase(args[0])) {
                if (player.hasPermission("editarmorstands.command.move")) {
                    Location loc = as.getLocation();
                    int[] blockLoc = new int[]{loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
                    double[] locArr = new double[]{loc.getX(), loc.getY(), loc.getZ()};
                    double[] oldLocArr = new double[]{loc.getX(), loc.getY(), loc.getZ()};
                    for (int i = 0; i < 3; i++) {
                        try {
                            if (args[i + 1].startsWith("~")) {
                                if (args[i + 1].length() != 1) {
                                    locArr[i] += Double.parseDouble(args[i + 1].substring(1));
                                }
                            } else {
                                locArr[i] = Double.parseDouble(args[i + 1]);
                            }
                            if (Math.floor(locArr[i]) != blockLoc[i] && !player.hasPermission("editarmorstands.command.move.nextblock") && !player.hasPermission("editarmorstands.command.move.unlimited")) {
                                player.sendMessage(ChatColor.RED + "You can only manipulate the position of an Armor Stands on the " + ChatColor.GOLD + "same block" + ChatColor.RED + ", not move it onto another block! (" + args[i + 1] + " would move it onto another one!)");
                                return false;
                            } else if (Math.abs(locArr[i] - oldLocArr[i]) > 1 && !player.hasPermission("editarmorstands.command.move.unlimited")) {
                                player.sendMessage(ChatColor.RED + "You can't move Armor Stands more than " + ChatColor.GOLD + "one block" + ChatColor.RED + "! You inputted " + ChatColor.GOLD + args[i + 1] + ChatColor.RED + "!");
                                return false;
                            }
                        } catch (NumberFormatException e) {
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
            } else if (player.hasPermission("editarmorstands.command.pose")) {
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
                        int[] r = asp.setEulerAngle(bp, new int[]{x, y, z}, new boolean[]{rx, ry, rz});
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

    /**
     * Get the copied data
     * @param playerId  The UUID of the player
     * @return          The ArmorStandData or <tt>null</tt> if there is none
     */
    public ArmorStandData getClipboard(UUID playerId) {
        return clipboard.getIfPresent(playerId);
    }

    /**
     * Enable persistent mode for a player
     */
    public void enablePersistent(Player player) {
        persistent.put(player.getUniqueId(), null);
    }

    /**
     * Disable persistent mode for a player
     */
    public void disablePersistent(Player player) {
        persistent.remove(player.getUniqueId());
    }

    /**
     * Check if a player is in persistent mode
     */
    public boolean isPersistent(Player player) {
        return persistent.containsKey(player.getUniqueId());
    }

    /**
     * Get the action that the player set to persist
     */
    public String[] getPersistentAction(Player player) {
        return persistent.get(player.getUniqueId());
    }

    /**
     * Set the action that should persist
     */
    public void setPersistentAction(Player player, String[] args) {
        persistent.put(player.getUniqueId(), args);
    }

    /**
     * Add a selection
     * @param player The player that selected the armor stand
     * @param armorStand The armor stand selected by the player
     */
    public void addSelection(Player player, ArmorStand armorStand) {
        selectedArmorStands.put(player.getUniqueId(), armorStand.getUniqueId());
    }

    /**
     * Get the armor stand that a player has selected
     * @return The UUID of the ArmorStand or null if he hasn't selected any
     */
    public UUID getSelection(Player player) {
        return selectedArmorStands.get(player.getUniqueId());
    }

    /**
     * Remove the selection of a player
     */
    public void removeSelection(Player player) {
        selectedArmorStands.remove(player.getUniqueId());
    }

    /**
     * Check if an entity was selected
     */
    public boolean isSelected(Entity entity) {
        return selectedArmorStands.containsValue(entity.getUniqueId());
    }

    /**
     * Queue an action
     */
    public void addWaitingAction(Player player, String[] args) {
        waitingCommands.put(player.getUniqueId(), args);
    }

    /**
     * Check if a player has a queued action
     */
    public boolean hasWaitingAction(Player player) {
        return getWaitingAction(player) != null;
    }

    /**
     * Get a queued action
     */
    public String[] getWaitingAction(Player player) {
        return waitingCommands.getIfPresent(player.getUniqueId());
    }

    /**
     * Remove a queued action
     */
    public void removeWaitingAction(Player player) {
        waitingCommands.invalidate(player.getUniqueId());
    }

    public void removeSelected(Entity entity) {
        Iterator<UUID> it = selectedArmorStands.values().iterator();
        while (it.hasNext()) {
            if (entity.getUniqueId().equals(it.next())) {
                it.remove();
            }
        }
    }
}
