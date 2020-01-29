package com.rgbtape.app;

import java.util.List;

public class Colour {
    public static final Colour BLACK = new Colour(0,0,0);

    private float red;
    private float green;
    private float blue;

    public Colour(float red, float green, float blue) {
        this.red = limit(red);
        this.green = limit(green);
        this.blue = limit(blue);

    }

    public Colour(List<Long> array) throws InvalidRGBException{
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
        if (this.red == ((Colour)obj).getRed() && this.blue == ((Colour)obj).getBlue() && this.green == ((Colour)obj).getGreen())
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
