package de.themoep.EditArmorStands;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * Created by Phoenix616 on 18.03.2015.
 */
public class ArmorStandPoser {
    
    private ArmorStand as;
    
    public ArmorStandPoser(ArmorStand as) {
        this.as = as;
    }

    public boolean setSingleAngle(String part, String axis, int angle, boolean relative) {
        try {
            EulerAngle ea;
            if (part.equalsIgnoreCase("h") || part.equalsIgnoreCase("head")) {
                this.as.setHeadPose(getEulerAngleFromInput(axis, this.as.getHeadPose(), angle, relative));

            } else if (part.equalsIgnoreCase("b") || part.equalsIgnoreCase("body")) {
                this.as.setBodyPose(getEulerAngleFromInput(axis, this.as.getBodyPose(), angle, relative));

            } else if (part.equalsIgnoreCase("la") || part.equalsIgnoreCase("leftarm")) {
                this.as.setLeftArmPose(getEulerAngleFromInput(axis, this.as.getLeftArmPose(), angle, relative));

            } else if (part.equalsIgnoreCase("ll") || part.equalsIgnoreCase("leftleg")) {
                this.as.setLeftLegPose(getEulerAngleFromInput(axis, this.as.getLeftLegPose(), angle, relative));

            } else if (part.equalsIgnoreCase("ra") || part.equalsIgnoreCase("rightarm")) {
                this.as.setRightArmPose(getEulerAngleFromInput(axis, this.as.getRightArmPose(), angle, relative));

            } else if (part.equalsIgnoreCase("rl") || part.equalsIgnoreCase("rightleg")) {
                this.as.setRightLegPose(getEulerAngleFromInput(axis, this.as.getRightLegPose(), angle, relative));
            }
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private EulerAngle getEulerAngleFromInput(String axis, EulerAngle ea, int angle, boolean relative) {  
        if (axis.equalsIgnoreCase("p") || axis.equalsIgnoreCase("pitch")) {
            double x = Math.toRadians(angle);
            if(relative)
                x += ea.getX();
            ea = ea.setX(x);
        } else if (axis.equalsIgnoreCase("y") || axis.equalsIgnoreCase("yaw")) {
            double y = Math.toRadians(angle);
            if(relative)
                y += ea.getY();
            ea = ea.setY(y);
        } else if (axis.equalsIgnoreCase("r") || axis.equalsIgnoreCase("roll")) {
            double z = Math.toRadians(angle);
            if(relative)
                z += ea.getZ();
            ea = ea.setZ(z);
        } else
            return null;
        return ea;
    }

    public boolean setEulerAngle(String part, int x, int y, int z, boolean relative) {
                
        EulerAngle ea;
        if(part.equalsIgnoreCase("h") || part.equalsIgnoreCase("head")) {
            ea = this.as.getHeadPose();
            ea = getNewEulerAngle(ea, x, y, z, relative);

            this.as.setHeadPose(ea);

        } else if(part.equalsIgnoreCase("b") || part.equalsIgnoreCase("body")) {
            ea = this.as.getBodyPose();
            ea = getNewEulerAngle(ea, x, y, z, relative);

            this.as.setBodyPose(ea);

        } else if(part.equalsIgnoreCase("la") || part.equalsIgnoreCase("leftarm")) {
            ea = this.as.getLeftArmPose();
            ea = getNewEulerAngle(ea, x, y, z, relative);

            this.as.setLeftArmPose(ea);

        } else if(part.equalsIgnoreCase("ll") || part.equalsIgnoreCase("leftleg")) {
            ea = this.as.getLeftLegPose();
            ea = getNewEulerAngle(ea, x, y, z, relative);

            this.as.setLeftLegPose(ea);

        } else if(part.equalsIgnoreCase("ra") || part.equalsIgnoreCase("rightarm")) {
            ea = this.as.getRightArmPose();
            ea = getNewEulerAngle(ea, x, y, z, relative);

            this.as.setRightArmPose(ea);

        } else if(part.equalsIgnoreCase("rl") || part.equalsIgnoreCase("rightleg")) {
            ea = this.as.getRightLegPose();
            ea = getNewEulerAngle(ea, x, y, z, relative);

            this.as.setRightLegPose(ea);
        } else
            return false;
        return true;
    }

    private EulerAngle getNewEulerAngle(EulerAngle ea, int x, int y, int z, boolean relative) {
        if(relative)
            return ea.add(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
        return new EulerAngle(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }


    public boolean translatePlayerLook(String part, Location location) {
        if(part.equalsIgnoreCase("f") || part.equalsIgnoreCase("full")) { 
            Location l = this.as.getLocation();
            l.setYaw(location.getYaw());
            this.as.teleport(l);
        } else {
            Vector v = location.getDirection();
            EulerAngle ea = new EulerAngle(v.getX(), v.getY(), v.getZ());
            if(part.equalsIgnoreCase("h") || part.equalsIgnoreCase("head")) {
                this.as.setHeadPose(ea);

            } else if(part.equalsIgnoreCase("b") || part.equalsIgnoreCase("body")) {
                this.as.setBodyPose(ea);

            } else if(part.equalsIgnoreCase("la") || part.equalsIgnoreCase("leftarm")) {
                this.as.setLeftArmPose(ea);

            } else if(part.equalsIgnoreCase("ll") || part.equalsIgnoreCase("leftleg")) {
                this.as.setLeftLegPose(ea);

            } else if(part.equalsIgnoreCase("ra") || part.equalsIgnoreCase("rightarm")) {
                this.as.setRightArmPose(ea);

            } else if(part.equalsIgnoreCase("rl") || part.equalsIgnoreCase("rightleg")) {
                this.as.setRightLegPose(ea);
            } else {
                return false;
            }
        }
        return true;
    }
}
