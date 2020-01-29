package common;

import java.util.ArrayList;
import java.util.List;

public class LedState {
    public static final LedState BLACK = new LedState(0,0,0);

    private float red;
    private float green;
    private float blue;

    public static LedState applyIntensity(LedState ledState, int intensity){
        int newR = Long.valueOf(Math.round(ledState.getRed() * ((double) intensity / (double)100))).intValue();
        int newG = Long.valueOf(Math.round(ledState.getGreen() * ((double) intensity / (double)100))).intValue();
        int newB = Long.valueOf(Math.round(ledState.getBlue() * ((double) intensity / (double)100))).intValue();
        return new LedState(newR, newG, newB);
    }

    public static ArrayList<Long> applyIntensity(ArrayList<Long> ledState, int intensity){
        Long newR = Math.round(ledState.get(0) * ((double) intensity / (double)100));
        Long newG = Math.round(ledState.get(1) * ((double) intensity / (double)100));
        Long newB = Math.round(ledState.get(2) * ((double) intensity / (double)100));
        ArrayList<Long> returnList = new ArrayList<>();
        returnList.add(newR);
        returnList.add(newG);
        returnList.add(newB);
        return returnList;
    }



    public LedState(float red, float green, float blue) {
            this.red = limit(red);
            this.green = limit(green);
            this.blue = limit(blue);
    }



    public LedState(List<Long> array) throws InvalidRGBException{
        this.red = array.get(0).intValue();
        this.green = array.get(1).intValue();
        this.blue = array.get(2).intValue();

        if (red < 0 | red > 255)
            throw new InvalidRGBException("Red value (" + red + ") is out of range 0-255.");

        if (green < 0 | green > 255)
            throw new InvalidRGBException("Green value (" + green + ") is out of range 0-255.");

        if (blue < 0 | blue > 255)
            throw new InvalidRGBException("Blue value (" + blue + ") is out of range 0-255.");
    }

    public int getRed() {
        return (int) red;
    }

    public int getGreen() {
        return (int) green;
    }

    public int getBlue() {
        return (int) blue;
    }

    private float limit(float in){
      if (in > 255)
          return 255;
      else if (in < 0)
          return 0;
      else return in;
    }

    @Override
    public boolean equals(Object obj) {
        if (this.red == ((LedState)obj).getRed() && this.blue == ((LedState)obj).getBlue() && this.green == ((LedState)obj).getGreen())
            return true;
        else
            return false;
    }

    public class InvalidRGBException extends Exception{
        public InvalidRGBException(String error){
            super("Invalid RGB entry: " + error);
        }
    }
}
