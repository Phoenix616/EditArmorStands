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

    public double setSingleAngle(BodyPart part, Axis axis, double angle, boolean relative) {
        EulerAngle ea = getEulerAngleFromDegree(axis, part.getPose(as), angle, relative);
        part.setPose(as, ea);
        return (relative) ? Math.toDegrees(axis.getValue(ea)) : angle;
    }

    public static EulerAngle getEulerAngleFromDegree(Axis axis, EulerAngle ea, double angle, boolean relative) {
        double value = Math.toRadians(angle);
        if (relative)
            value += axis.getValue(ea);
        return axis.setValue(ea, value);
    }

    /**
     * Get the angle in degrees from an EulerAngle
     * @param axis  The axis to get the degree from
     * @param ea    The EulerAngle
     * @return      The angle in degrees rounded to two decimal places
     */
    public static double getDegreeAngleFromEuler(Axis axis, EulerAngle ea) {
        return Math.round(Math.toDegrees(axis.getValue(ea)) * 100) / 100;
    }

    public int[] setEulerAngle(BodyPart part, int[] angles, boolean[] relatives) throws IllegalArgumentException {
        if (angles.length >= 3 && relatives.length >= 3) {
            EulerAngle ea = getNewEulerAngle(part.getPose(as), angles, relatives);
            part.setPose(as, ea);
            return new int[]{
                    relatives[0] ? (int) Math.toDegrees(ea.getX()) : angles[0],
                    relatives[1] ? (int) Math.toDegrees(ea.getY()) : angles[1],
                    relatives[2] ? (int) Math.toDegrees(ea.getZ()) : angles[2]
            };
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

    public void setDirection(BodyPart part, Vector direction) {
        Location origin;
        if (part == BodyPart.HEAD) {
            origin = as.getEyeLocation();
        } else {
            origin = as.getLocation();
            switch (part) {
                case LEFTARM:
                case RIGHTARM:
                    origin = origin.add(0, 1.4, 0);
                    break;
                case LEFTLEG:
                case RIGHTLEG:
                    origin = origin.add(0, 0.8, 0);
                    break;
            }
        }
        double initYaw = origin.getYaw();
        origin.setDirection(direction);
        double yaw = origin.getYaw() - initYaw;
        double pitch = origin.getPitch();
        if (part == BodyPart.HEAD) {
            if (yaw < -180) {
                yaw = yaw + 360;
            } else if (yaw >= 180) {
                yaw -= 360;
            }
        } else {
            pitch -= 90;
        }
        part.setPose(as, new EulerAngle(Math.toRadians(pitch), Math.toRadians(yaw), 0));
    }
}
