package de.themoep.EditArmorStands;

import java.util.Arrays;

/**
 * Created by Phoenix616 on 19.03.2015.
 */
public enum BodyPart {
    FULL    (new String[]{"f, fullbody, fb, rotation, r"}),
    HEAD    (new String[]{"h"}),
    BODY    (new String[]{"b"}),
    LEFTARM (new String[]{"la"}),
    RIGHTARM(new String[]{"ra"}),
    LEFTLEG (new String[]{"ll"}),
    RIGHTLEG(new String[]{"rl"});

    private String[] alias;
    
    BodyPart(String[] alias) {
        this.alias = alias;
    }

    public static BodyPart fromString(String name) throws IllegalArgumentException {
        if (name == null)
            throw new NullPointerException("Name is null");
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            for(BodyPart p : values())
                for(String a : p.alias)
                    if(a.equalsIgnoreCase(name))
                        return p;
        }
        throw new IllegalArgumentException(name + " is not a body part! Available parts are " + Arrays.toString(BodyPart.values()).toLowerCase());
    }
}
