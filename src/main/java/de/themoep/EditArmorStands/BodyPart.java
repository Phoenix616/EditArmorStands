package de.themoep.EditArmorStands;

import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

public enum BodyPart {
    HEAD(ArmorStand::getHeadPose, ArmorStand::setHeadPose, "h"),
    BODY(ArmorStand::getBodyPose, ArmorStand::setBodyPose, "b"),
    LEFTARM(ArmorStand::getLeftArmPose, ArmorStand::setLeftArmPose, "la"),
    RIGHTARM(ArmorStand::getRightArmPose, ArmorStand::setRightArmPose, "ra"),
    LEFTLEG(ArmorStand::getLeftLegPose, ArmorStand::setLeftLegPose, "ll"),
    RIGHTLEG(ArmorStand::getRightLegPose, ArmorStand::setRightLegPose, "rl");

    private final Function<ArmorStand, EulerAngle> getter;
    private final BiConsumer<ArmorStand, EulerAngle> setter;
    private String[] alias;

    private static String valuestring = Arrays.toString(BodyPart.values());

    BodyPart(Function<ArmorStand, EulerAngle> getter, BiConsumer<ArmorStand, EulerAngle> setter, String... alias) {
        this.getter = getter;
        this.setter = setter;
        this.alias = alias;
    }

    public EulerAngle getPose(ArmorStand stand) {
        return getter.apply(stand);
    }

    public void setPose(ArmorStand stand, EulerAngle angle) {
        setter.accept(stand, angle);
    }

    public static BodyPart fromString(String name) throws IllegalArgumentException {
        if (name == null)
            throw new NullPointerException("Name is null");
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (BodyPart p : values())
                for (String a : p.alias)
                    if (a.equalsIgnoreCase(name))
                        return p;
        }
        throw new IllegalArgumentException(name + " is not a body part! Available parts are " + valuestring.toLowerCase().substring(1, valuestring.length() - 1));
    }
}
