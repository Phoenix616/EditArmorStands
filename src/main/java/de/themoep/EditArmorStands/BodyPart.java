package de.themoep.EditArmorStands;

/**
 * Created by Phoenix616 on 19.03.2015.
 */
public enum BodyPart {
    FULL    ("f"),
    HEAD    ("h"),
    BODY    ("b"),
    LEFTARM ("la"),
    RIGHTARM("ra"),
    LEFTLEG ("ll"),
    RIGHTLEG("rl");

    private String alias;
    
    BodyPart(String alias) {
        this.alias = alias;
    }

    public static BodyPart fromString(String name) throws IllegalArgumentException {
        if (name == null)
            throw new NullPointerException("Name is null");
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            for(BodyPart p : values())
                if(p.alias.equalsIgnoreCase(name))
                    return p;
        }
        throw new IllegalArgumentException("No enum const BodyPart." + name.toUpperCase());
    }
}
