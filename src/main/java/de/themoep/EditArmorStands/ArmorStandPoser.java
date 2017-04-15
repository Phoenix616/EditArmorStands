package de.themoep.EditArmorStands;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * EditArmorStands - Plugin to edit armor stand poses and options
 * Copyright (C) 2015 Max Lee (https://github.com/Phoenix616/)
 * <p>
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

public class ArmorStandPoser {

    private ArmorStand as;

    public ArmorStandPoser(ArmorStand as) {
        this.as = as;
    }

    public int setSingleAngle(BodyPart part, Axis axis, int angle, boolean relative) {
        try {
            if (part == BodyPart.HEAD) {
                EulerAngle ea = this.as.getHeadPose();
                this.as.setHeadPose(getEulerAngleFromDegree(axis, ea, angle, relative));
                if (relative) {
                    double rad = 0;
                    switch (axis) {
                        case YAW:
                            rad = ea.getX();
                            break;
                        case PITCH:
                            rad = ea.getY();
                            break;
                        case ROLL:
                            rad = ea.getZ();
                            break;
                    }
                    return (int) Math.toDegrees(rad);
                }
            } else if (part == BodyPart.BODY) {
                this.as.setBodyPose(getEulerAngleFromDegree(axis, this.as.getBodyPose(), angle, relative));
                return (relative) ? (int) Math.toDegrees(this.as.getBodyPose().getX()) : angle;

            } else if (part == BodyPart.LEFTARM) {
                this.as.setLeftArmPose(getEulerAngleFromDegree(axis, this.as.getLeftArmPose(), angle, relative));
                return (relative) ? (int) Math.toDegrees(this.as.getLeftArmPose().getX()) : angle;

            } else if (part == BodyPart.LEFTLEG) {
                this.as.setLeftLegPose(getEulerAngleFromDegree(axis, this.as.getLeftLegPose(), angle, relative));
                return (relative) ? (int) Math.toDegrees(this.as.getLeftLegPose().getX()) : angle;

            } else if (part == BodyPart.RIGHTARM) {
                this.as.setRightArmPose(getEulerAngleFromDegree(axis, this.as.getRightArmPose(), angle, relative));
                return (relative) ? (int) Math.toDegrees(this.as.getRightArmPose().getX()) : angle;

            } else if (part == BodyPart.RIGHTLEG) {
                this.as.setRightLegPose(getEulerAngleFromDegree(axis, this.as.getRightLegPose(), angle, relative));
                return (relative) ? (int) Math.toDegrees(this.as.getRightLegPose().getX()) : angle;
            }
            return angle;
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Null pointer at asp.setSingleAngle(" + part + ", " + axis + ", " + angle + ", " + relative + ")!");
        }
    }

    public static EulerAngle getEulerAngleFromDegree(Axis axis, EulerAngle ea, int angle, boolean relative) {
        switch (axis) {
            case PITCH:
                double x = Math.toRadians(angle);
                if (relative)
                    x += ea.getX();
                return ea.setX(x);
            case YAW:
                double y = Math.toRadians(angle);
                if (relative)
                    y += ea.getY();
                return ea.setY(y);
            case ROLL:
                double z = Math.toRadians(angle);
                if (relative)
                    z += ea.getZ();
                return ea.setZ(z);
        }
        return null;
    }

    /**
     * Get the angle in degrees from an EulerAngle
     * @param axis  The axis to get the degree from
     * @param ea    The EulerAngle
     * @return      The angle in degrees rounded to two decimal places
     */
    public static double getDegreeAngleFromEuler(Axis axis, EulerAngle ea) {
        switch (axis) {
            case PITCH:
                return Math.round(Math.toDegrees(ea.getX()) * 100) / 100;
            case YAW:
                return Math.round(Math.toDegrees(ea.getY()) * 100) / 100;
            case ROLL:
                return Math.round(Math.toDegrees(ea.getZ()) * 100) / 100;
        }
        return 0;
    }

    public int[] setEulerAngle(BodyPart part, int[] angles, boolean[] relatives) throws IllegalArgumentException {
        if (angles.length >= 3 && relatives.length >= 3) {
            boolean relative = relatives[0] && relatives[1] && relatives[2];
            EulerAngle ea;
            switch (part) {
                case HEAD:
                    ea = this.as.getHeadPose();
                    ea = getNewEulerAngle(ea, angles, relatives);
                    this.as.setHeadPose(ea);
                    return (relative) ? new int[]{(int) Math.toDegrees(ea.getX()), (int) Math.toDegrees(ea.getY()), (int) Math.toDegrees(ea.getZ())} : angles;
                case BODY:
                    ea = this.as.getBodyPose();
                    ea = getNewEulerAngle(ea, angles, relatives);
                    this.as.setBodyPose(ea);
                    return (relative) ? new int[]{(int) Math.toDegrees(ea.getX()), (int) Math.toDegrees(ea.getY()), (int) Math.toDegrees(ea.getZ())} : angles;
                case LEFTARM:
                    ea = this.as.getLeftArmPose();
                    ea = getNewEulerAngle(ea, angles, relatives);
                    this.as.setLeftArmPose(ea);
                    return (relative) ? new int[]{(int) Math.toDegrees(ea.getX()), (int) Math.toDegrees(ea.getY()), (int) Math.toDegrees(ea.getZ())} : angles;
                case LEFTLEG:
                    ea = this.as.getLeftLegPose();
                    ea = getNewEulerAngle(ea, angles, relatives);
                    this.as.setLeftLegPose(ea);
                    return (relative) ? new int[]{(int) Math.toDegrees(ea.getX()), (int) Math.toDegrees(ea.getY()), (int) Math.toDegrees(ea.getZ())} : angles;
                case RIGHTARM:
                    ea = this.as.getRightArmPose();
                    ea = getNewEulerAngle(ea, angles, relatives);
                    this.as.setRightArmPose(ea);
                    return (relative) ? new int[]{(int) Math.toDegrees(ea.getX()), (int) Math.toDegrees(ea.getY()), (int) Math.toDegrees(ea.getZ())} : angles;
                case RIGHTLEG:
                    ea = this.as.getRightLegPose();
                    ea = getNewEulerAngle(ea, angles, relatives);
                    this.as.setRightLegPose(ea);
                    return (relative) ? new int[]{(int) Math.toDegrees(ea.getX()), (int) Math.toDegrees(ea.getY()), (int) Math.toDegrees(ea.getZ())} : angles;
                default:
                    throw new IllegalArgumentException("We encountered an error. Please report that immediately to a dev! asp.setEulerAngle(" + part + ", " + angles[0] + ", " + angles[1] + ", " + angles[3] + ", " + relatives + ")");
            }
        } else {
            throw new IllegalArgumentException("Please input 3 angles!");
        }
    }

    private EulerAngle getNewEulerAngle(EulerAngle ea, int[] angles, boolean[] relatives) {
        if (angles.length >= 3 && relatives.length >= 3) {
            ea = new EulerAngle(
                    (relatives[0]) ? Math.toRadians(angles[0]) + ea.getX() : Math.toRadians(angles[0]),
                    (relatives[1]) ? Math.toRadians(angles[1]) + ea.getY() : Math.toRadians(angles[1]),
                    (relatives[2]) ? Math.toRadians(angles[2]) + ea.getZ() : Math.toRadians(angles[2])
            );
            return ea;
        } else {
            throw new IllegalArgumentException("Please input 3 angles!");
        }
    }


    public boolean translatePlayerLook(BodyPart part, Location location) {
        Vector v = location.getDirection();
        EulerAngle ea = new EulerAngle(v.getX(), v.getY(), v.getZ());
        switch (part) {
            case HEAD:
                this.as.setHeadPose(ea);
                return true;
            case BODY:
                this.as.setBodyPose(ea);
                return true;
            case LEFTARM:
                this.as.setLeftArmPose(ea);
                return true;
            case LEFTLEG:
                this.as.setLeftLegPose(ea);
                return true;
            case RIGHTARM:
                this.as.setRightArmPose(ea);
                return true;
            case RIGHTLEG:
                this.as.setRightLegPose(ea);
                return true;
            default:
                return false;
        }
    }
}
