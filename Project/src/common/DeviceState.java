package common;

import effects.CustomEffect;

import java.util.ArrayList;

public class DeviceState {
    private boolean standby;
    private int speed;
    private String type;
    private ArrayList<Long> colour;
    private int intensity;

    private transient CustomEffect customEffect;

    public DeviceState(boolean standby, int speed, int intensity, String type, ArrayList<Long> colour){
        this.standby = standby;
        this.speed = speed;
        this.type = type;
        this.colour = colour;
        this.intensity = intensity;
    }

    public void setCustomEffect(CustomEffect customEffect){
        this.customEffect = customEffect;
    }

    public CustomEffect getCustomEffect(){
        return customEffect;
    }

    public boolean isStandby() {
        return standby;
    }

    public int getSpeed() {
        return speed;
    }

    public int getIntensity(){
        return intensity;
    }

    public String getType() {
        return type;
    }

    public ArrayList<Long> getColour() {
        return colour;
    }
}
