package common;


import effects.SimpleTimeObject;

import java.util.ArrayList;



public class Alarm implements Comparable<Alarm>{

    private int startHour;
    private int startMinute;

    private int finishHour;
    private int finishMinute;

    private ArrayList<Long> colour;
    private boolean enabled;

    public Alarm(){}



    public ArrayList<Long> getColour() {
        return colour;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getFinishHour() {
        return finishHour;
    }

    public int getFinishMinute() {
        return finishMinute;
    }

    public SimpleTimeObject getSimpleTimeObjectStart(){
        return new SimpleTimeObject(startHour, startMinute);
    }

    public SimpleTimeObject getSimpleTimeObjectFinish(){
        return new SimpleTimeObject(finishHour, finishMinute);
    }

    public int getDuration(){
        if (finishHour >= startHour){
                return 60 * (finishHour - startHour) + (finishMinute - startMinute);
        } else {
            return 60 * ((24 - startHour) + finishHour) + (finishMinute - startMinute);
        }
    }

    /**
     * Compares this object to the object o
     * @param o
     * @return -1 if this is less than o, 1 if it is greater than o
     */
    @Override
    public int compareTo(Alarm o) {
        if (this.startHour < o.getStartHour()){
            //must be before...
            return -1;
        } else if (this.startHour == o.getStartHour()){
            //now compare minutes
            if (this.startMinute < o.getStartMinute()){
                return -1;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        boolean returnVal = true;
        if (obj instanceof Alarm) {
            Alarm compareTo = (Alarm) obj;

            if (getStartHour() != compareTo.getStartHour())
                returnVal = false;

            if (getStartMinute() != compareTo.getStartMinute())
                returnVal = false;

            if (getFinishHour() != compareTo.getFinishHour())
                returnVal = false;

            if (getFinishMinute() != compareTo.getFinishMinute())
                returnVal = false;

            if (isEnabled() != compareTo.isEnabled())
                returnVal = false;

            //xor null to protect from null pointer comparison
            if (getColour() == null ^ compareTo.getColour() == null){
                return false;
            } else if (getColour() != null && compareTo.getColour() != null){
                if (!getColour().equals(compareTo.getColour()))
                    returnVal = false;
            }

        } else {
            returnVal = false;
        }
        return returnVal;
    }
}
