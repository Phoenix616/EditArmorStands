package de.themoep.EditArmorStands;

import java.util.Arrays;

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

public enum Axis {
    YAW     ("y, heading, h"),
    PITCH   ("p, elevation, e"),
    ROLL    ("r, bank, b");

    private String[] alias;

    private static String valuestring = Arrays.toString(Axis.values());

    Axis(String... alias) {
        this.alias = alias;
    }

    public static Axis fromString(String name) throws IllegalArgumentException {
        if (name == null)
            throw new NullPointerException("Name is null");
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (Axis x : values())
                for (String a : x.alias)
                    if (a.equalsIgnoreCase(name))
                        return x;
        }
        throw new IllegalArgumentException(name + " is not an axis! Available axis are " + valuestring.toLowerCase().substring(1, valuestring.length() - 1));
    }
}
