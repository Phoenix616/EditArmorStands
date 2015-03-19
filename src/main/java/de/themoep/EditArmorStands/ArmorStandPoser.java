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

    public boolean setSingleAngle(BodyPart part, Axis axis, int angle, boolean relative) {
        try {
            EulerAngle ea;
            if(part == BodyPart.HEAD) {
                this.as.setHeadPose(getEulerAngleFromInput(axis, this.as.getHeadPose(), angle, relative));

            } else if(part == BodyPart.BODY) {
                this.as.setBodyPose(getEulerAngleFromInput(axis, this.as.getBodyPose(), angle, relative));

            } else if(part == BodyPart.LEFTARM) {
                this.as.setLeftArmPose(getEulerAngleFromInput(axis, this.as.getLeftArmPose(), angle, relative));

            } else if(part == BodyPart.LEFTLEG) {
                this.as.setLeftLegPose(getEulerAngleFromInput(axis, this.as.getLeftLegPose(), angle, relative));

            } else if(part == BodyPart.RIGHTARM) {
                this.as.setRightArmPose(getEulerAngleFromInput(axis, this.as.getRightArmPose(), angle, relative));

            } else if(part == BodyPart.RIGHTLEG) {
                this.as.setRightLegPose(getEulerAngleFromInput(axis, this.as.getRightLegPose(), angle, relative));
            }
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private EulerAngle getEulerAngleFromInput(Axis axis, EulerAngle ea, int angle, boolean relative) {
        switch(axis) {
            case PITCH:
                double x = Math.toRadians(angle);
                if(relative)
                    x += ea.getX();
                return  ea.setX(x);
            case YAW:
                double y = Math.toRadians(angle);
                if(relative)
                    y += ea.getY();
                return  ea.setY(y);
            case ROLL:
                double z = Math.toRadians(angle);
                if (relative)
                    z += ea.getZ();
                return ea.setZ(z);
        }
        return null;
    }

    public boolean setEulerAngle(BodyPart part, int x, int y, int z, boolean relative) {
        EulerAngle ea;
        switch(part) {
            case HEAD:
                ea = this.as.getHeadPose();
                ea = getNewEulerAngle(ea, x, y, z, relative);
                this.as.setHeadPose(ea);
                return true;
            case BODY:
                ea = this.as.getBodyPose();
                ea = getNewEulerAngle(ea, x, y, z, relative);
                this.as.setBodyPose(ea);
                return true;
            case LEFTARM:
                ea = this.as.getLeftArmPose();
                ea = getNewEulerAngle(ea, x, y, z, relative);
                this.as.setLeftArmPose(ea);
                return true; 
            case LEFTLEG:
                ea = this.as.getLeftLegPose();
                ea = getNewEulerAngle(ea, x, y, z, relative);
                this.as.setLeftLegPose(ea);
                return true;
            case RIGHTARM:
                ea = this.as.getRightArmPose();
                ea = getNewEulerAngle(ea, x, y, z, relative);
                this.as.setRightArmPose(ea);
                return true;
            case RIGHTLEG:
                ea = this.as.getRightLegPose();
                ea = getNewEulerAngle(ea, x, y, z, relative);
                this.as.setRightLegPose(ea);
                return true;
            default:
                return false;
        }
    }

    private EulerAngle getNewEulerAngle(EulerAngle ea, int x, int y, int z, boolean relative) {
        if(relative)
            return ea.add(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
        return new EulerAngle(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }


    public boolean translatePlayerLook(BodyPart part, Location location) {
        if (part == BodyPart.FULL) {
            Location l = this.as.getLocation();
            l.setYaw(location.getYaw());
            this.as.teleport(l);
            return true;
        } else {
            Vector v = location.getDirection();
            EulerAngle ea = new EulerAngle(v.getX(), v.getY(), v.getZ());
            switch(part) {
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
}
