package com.rgbtape.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class Alarm implements Comparable<Alarm>{

    private int startHour;
    private int startMinute;

    private int finishHour;
    private int finishMinute;


    String id;

    private ArrayList<Long> colour;
    private boolean enabled;

    public Alarm(){}

    public Alarm(int startHour, int startMinute, int finishHour, int finishMinute){
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.finishHour = finishHour;
        this.finishMinute = finishMinute;
    }


    public void setId(String id){
        this.id = id;
    }

    @Exclude
    public String getId(){return this.id;}

    public void setFinishHour(int finishHour){
        this.finishHour = finishHour;
    }

    public void setFinishMinute(int finishMinute){
        this.finishMinute = finishMinute;
    }

    public void setStartTime(int duration){
        if ((finishMinute - duration) >= 0){
            startHour = finishHour;
            startMinute = finishMinute - duration;
        } else {
            if (finishHour > 0){
                startHour = finishHour - 1;
                startMinute = 60 - (duration - finishMinute);
            } else {
                startHour = 23;
                startMinute = 60 - (duration - finishMinute);
            }
        }
    }

    public void setColour(ArrayList<Long> colour){
        this.colour = colour;
    }

    public void setColour(int r, int g, int b){
        colour = new ArrayList<>();
        colour.add(Integer.valueOf(r).longValue());
        colour.add(Integer.valueOf(g).longValue());
        colour.add(Integer.valueOf(b).longValue());
    }


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

    @Exclude
    public SimpleTimeObject getSimpleTimeObjectStart(){
        return new SimpleTimeObject(startHour, startMinute);
    }

    @Exclude
    public SimpleTimeObject getSimpleTimeObjectFinish(){
        return new SimpleTimeObject(finishHour, finishMinute);
    }

    @Exclude
    public int getDuration(){
        if (finishHour >= startHour){
                return 60 * (finishHour - startHour) + (finishMinute - startMinute);
        } else {
            return 60 * ((24 - startHour) + finishHour) + (finishMinute - startMinute);
        }
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
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

    @NonNull
    @Override
    public String toString() {
        return String.format("%02d:%02d", getFinishHour(), getFinishMinute());
    }
}
