package de.themoep.EditArmorStands;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

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
public class ArmorStandData {

    private final EulerAngle headPose;
    private final EulerAngle bodyPose;
    private final EulerAngle leftArmPose;
    private final EulerAngle rightArmPose;
    private final EulerAngle leftLegPose;
    private final EulerAngle rightLegPose;
    private final float rotation;

    private final String customName;

    private final boolean nameVisible;
    private final boolean visible;
    private final boolean gravity;
    private final boolean glowing;
    private final boolean invulnerable;
    private final boolean arms;
    private final boolean base;
    private final boolean small;
    private final boolean marker;

    private final ItemStack helmet;
    private final ItemStack chest;
    private final ItemStack leggings;
    private final ItemStack boots;

    private final ItemStack mainHand;
    private final ItemStack offHand;

    public ArmorStandData(ArmorStand armorStand) {
        this.headPose = armorStand.getHeadPose();
        this.bodyPose = armorStand.getBodyPose();
        this.leftArmPose = armorStand.getLeftArmPose();
        this.rightArmPose = armorStand.getRightArmPose();
        this.leftLegPose = armorStand.getLeftLegPose();
        this.rightLegPose = armorStand.getRightLegPose();
        this.rotation = armorStand.getLocation().getYaw();

        this.customName = armorStand.getCustomName();

        this.nameVisible = armorStand.isCustomNameVisible();
        this.visible = armorStand.isVisible();
        this.gravity = armorStand.hasGravity();
        this.glowing = armorStand.isGlowing();
        this.invulnerable = armorStand.isInvulnerable();
        this.arms = armorStand.hasArms();
        this.base = armorStand.hasBasePlate();
        this.small = armorStand.isSmall();
        this.marker = armorStand.isMarker();

        this.helmet = armorStand.getHelmet();
        this.chest = armorStand.getChestplate();
        this.leggings = armorStand.getLeggings();
        this.boots = armorStand.getBoots();

        this.mainHand = armorStand.getEquipment().getItemInMainHand();
        this.offHand = armorStand.getEquipment().getItemInOffHand();
    }

    public EulerAngle getHeadPose() {
        return headPose;
    }

    public EulerAngle getBodyPose() {
        return bodyPose;
    }

    public EulerAngle getLeftArmPose() {
        return leftArmPose;
    }

    public EulerAngle getRightArmPose() {
        return rightArmPose;
    }

    public EulerAngle getLeftLegPose() {
        return leftLegPose;
    }

    public EulerAngle getRightLegPose() {
        return rightLegPose;
    }

    public float getRotation() {
        return rotation;
    }

    public String getCustomName() {
        return customName;
    }

    public boolean isNameVisible() {
        return nameVisible;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean hasGravity() {
        return gravity;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public boolean hasArms() {
        return arms;
    }

    public boolean hasBase() {
        return base;
    }

    public boolean isSmall() {
        return small;
    }

    public boolean isMarker() {
        return marker;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public ItemStack getChest() {
        return chest;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public ItemStack getMainHand() {
        return mainHand;
    }

    public ItemStack getOffHand() {
        return offHand;
    }

    public void applyPose(ArmorStand as) {
        as.setHeadPose(getHeadPose());
        as.setBodyPose(getBodyPose());
        as.setLeftArmPose(getLeftArmPose());
        as.setRightArmPose(getRightArmPose());
        as.setLeftLegPose(getLeftLegPose());
        as.setRightLegPose(getRightLegPose());
        Location l = as.getLocation();
        l.setYaw(getRotation());
        as.teleport(l);
    }

    public void applyName(ArmorStand as) {
        as.setCustomName(getCustomName());
    }

    public void applySettings(ArmorStand as) {
        as.setCustomNameVisible(isNameVisible());
        as.setVisible(isVisible());
        as.setGravity(hasGravity());
        as.setGlowing(isGlowing());
        as.setInvulnerable(isInvulnerable());
        as.setArms(hasArms());
        as.setBasePlate(hasBase());
        as.setSmall(isSmall());
        as.setMarker(isMarker());
    }

    public void applyItems(ArmorStand as) {
        as.setHelmet(getHelmet());
        as.setChestplate(getChest());
        as.setLeggings(getLeggings());
        as.setBoots(getBoots());
        as.getEquipment().setItemInMainHand(getMainHand());
        as.getEquipment().setItemInOffHand(getOffHand());
    }
}
