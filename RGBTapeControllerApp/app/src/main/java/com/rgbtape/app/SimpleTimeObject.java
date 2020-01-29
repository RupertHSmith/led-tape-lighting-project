package com.rgbtape.app;



public class SimpleTimeObject implements Comparable<SimpleTimeObject>{
    private int hours;
    private int minutes;

    public SimpleTimeObject(int hours, int minutes){
        this.hours = hours;
        this.minutes = minutes;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    @Override
    public int compareTo(SimpleTimeObject o) {
        if (this.hours < o.getHours()){
            //must be before...
            return -1;
        } else if (this.hours == o.getHours()){
            //now compare minutes
            if (this.minutes < o.getMinutes()){
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
        if (obj instanceof SimpleTimeObject){
            if (hours == ((SimpleTimeObject) obj).getHours() && minutes == ((SimpleTimeObject)obj).getMinutes()){
                return true;
            } else{
                return false;
            }
        } else {
            return false;
        }
    }

    public static int calculateTimeDifference(SimpleTimeObject o1, SimpleTimeObject o2){
        if (o2.getHours() >= o1.getHours()){
            return 60 * (o2.getHours() - o1.getHours()) + (o2.getMinutes() - o1.getMinutes());
        } else {
            return 60 * ((24 - o1.getHours()) + o2.getHours()) + (o2.getMinutes() - o1.getMinutes());
        }
    }
}
