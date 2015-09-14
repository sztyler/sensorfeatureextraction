package de.unima.ar.collector.features.model;

import java.util.Arrays;


public class Action
{
    public static enum TYPE {
        DEVICEPOSITIONS, HUMANACTIVITIES, HUMANPOSITIONS, HUMANPOSTURES;
    }

    public static enum DEVICEPOSITIONS {
        CHEST, FOREARM, HEAD, SHIN, THIGH, UPPERARM, WAIST;
    }

    public static enum HUMANPOSTURES {
        CLIMBINGDOWN, CLIMBINGUP, JUMPING, LYING, RUNNING, SITTING, STANDING, WALKING;
    }

    private long time;

    private DEVICEPOSITIONS devicePosition;
    private HUMANPOSTURES   humanPosture;
    private String          humanPosition;
    private String          humanActvitiy;


    public Action()
    {
        this.devicePosition = null;
        this.humanPosture = null;
        this.humanPosition = "unknown";
        this.humanActvitiy = "unknown";
    }


    public DEVICEPOSITIONS getDevicePosition()
    {
        return devicePosition;
    }


    public HUMANPOSTURES getHumanPosture()
    {
        return this.humanPosture;
    }


    public String getHumanPosition()
    {
        return this.humanPosition;
    }


    public String getHumanActvitiy()
    {
        return this.humanActvitiy;
    }


    public long getTime()
    {
        return this.time;
    }


    public String getFormatedStringValues(Action.TYPE targetClass)
    {
        StringBuilder sb = new StringBuilder();

        for(Action.TYPE type : Action.TYPE.values()) {
            if(type.equals(targetClass)) {
                continue;
            }

            if(Action.TYPE.DEVICEPOSITIONS.equals(type)) {
                sb.append(getDevicePosition().toString().toLowerCase() + ",");
            }

            if(Action.TYPE.HUMANACTIVITIES.equals(type)) {
                sb.append(getHumanActvitiy().toLowerCase() + ",");
            }

            if(Action.TYPE.HUMANPOSITIONS.equals(type)) {
                sb.append(getHumanPosition().toLowerCase() + ",");
            }

            if(Action.TYPE.HUMANPOSTURES.equals(type)) {
                sb.append(getHumanPosture().toString().toLowerCase() + ",");
            }
        }

        if(Action.TYPE.DEVICEPOSITIONS.equals(targetClass)) {
            sb.append(getDevicePosition().toString().toLowerCase());
        }

        if(Action.TYPE.HUMANACTIVITIES.equals(targetClass)) {
            sb.append(getHumanActvitiy().toLowerCase());
        }

        if(Action.TYPE.HUMANPOSITIONS.equals(targetClass)) {
            sb.append(getHumanPosition().toLowerCase());
        }

        if(Action.TYPE.HUMANPOSTURES.equals(targetClass)) {
            sb.append(getHumanPosture().toString().toLowerCase());
        }

        if(targetClass == null) {
            sb.delete(sb.length() - 1, sb.length());
        }

        return sb.toString();
    }


    public void setHumanPosture(HUMANPOSTURES humanPosture)
    {
        if(humanPosture == null) { return; }

        this.humanPosture = humanPosture;
    }


    public void setHumanPosition(String humanPosition)
    {
        if(humanPosition == null) { return; }

        this.humanPosition = humanPosition.toLowerCase();
    }


    public void setHumanActvitiy(String humanActvitiy)
    {
        if(humanActvitiy == null) { return; }

        this.humanActvitiy = humanActvitiy.toLowerCase();
    }


    public void setDevicePosition(DEVICEPOSITIONS devicePosition)
    {
        if(devicePosition == null) { return; }

        this.devicePosition = devicePosition;
    }


    public long setTime(long time)
    {
        return this.time = time;
    }


    public static String getFormatedStringType(Action.TYPE targetClass)
    {
        StringBuilder sb = new StringBuilder();

        for(Action.TYPE type : Action.TYPE.values()) {
            if(type.equals(targetClass)) {
                continue;
            }

            sb.append(type.toString().toLowerCase() + ",");
        }

        if(targetClass != null) {
            sb.append(targetClass.toString().toLowerCase());
        } else {
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }


    public static String getFormatedStringClass(Action.TYPE targetClass)
    {
        switch(targetClass) {
            case DEVICEPOSITIONS:
                return Arrays.toString(DEVICEPOSITIONS.values()).replace("[", "").replace("]", "").replace(" ", "").trim().toLowerCase();
            case HUMANPOSTURES:
                return Arrays.toString(HUMANPOSTURES.values()).replace("[", "").replace("]", "").replace(" ", "").trim().toLowerCase();
            default:
                return "";
        }
    }


    @Override
    public int hashCode()
    {
        String value = this.getDevicePosition() + this.getHumanActvitiy() + this.getHumanPosition() + this.getHumanPosture();

        return value.hashCode();
    }


    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Action)) { return false; }

        Action action = (Action) o;

        String s1 = this.getDevicePosition() + this.getHumanActvitiy() + this.getHumanPosition() + this.getHumanPosture();
        String s2 = action.getDevicePosition() + action.getHumanActvitiy() + action.getHumanPosition() + action.getHumanPosture();

        return s1.equals(s2);
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("DevicePosition: " + this.getDevicePosition() + System.lineSeparator());
        sb.append("HumanPosture: " + this.getHumanPosture() + System.lineSeparator());
        sb.append("HumanPosition: " + this.getHumanPosition() + System.lineSeparator());
        sb.append("HumanActivity: " + this.getHumanActvitiy());

        return sb.toString();
    }
}