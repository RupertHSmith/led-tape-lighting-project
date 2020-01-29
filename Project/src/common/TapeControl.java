package common;

import effects.IEffect;


public class TapeControl implements ITapeControl {

    private float r;
    private float g;
    private float b;

    private Runtime p;

    private static int PIN_NUMBER_RED = 17;
    private static int PIN_NUMBER_GREEN = 22;
    private static int PIN_NUMBER_BLUE = 24;

    private IEffect controller;

    boolean halted = false;
    boolean transitioning = false;

    public TapeControl(){

        r = 0;
        g = 0;
        b = 0;

        p = Runtime.getRuntime();
        try {
        p.exec("sudo pigpiod");
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private synchronized  void setTransitioning(boolean transitioning){
        this.transitioning = transitioning;
    }

    private synchronized  boolean isTransitioning(){
        return transitioning;
    }



    private synchronized void setState(LedState s){
        r = s.getRed();
        g = s.getGreen();
        b = s.getBlue();

        setStateTape(r,g,b);
       // System.out.println(r + ", " + g + ", "+ b);

    //    rgbViewer.setColour(s);
    }

    private void setStateTape(float r, float g, float b){
        try {
            int intR = Float.valueOf(r).intValue();
            int intG = Float.valueOf(g).intValue();
            int intB = Float.valueOf(b).intValue();
         p.exec("sudo pigs p 17 " + intR +" p 22 " + intG + " p 24 " + intB);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private synchronized LedState getState(){
        return new LedState(r, g, b);
    }

    private synchronized  void setHalted(boolean halted){
        this.halted = halted;
    }

    private synchronized  boolean getHalted(){
        return this.halted;
    }

    @Override
    public void snapTo(LedState s, IEffect controller) throws TapeInUseException{
        if (getController() == controller) {
            setState(s);
        } else
            throw new TapeInUseException(controller, this.controller);
    }

    /**
     * Fades from the current led state to another.
     * @param s The Led State to fade to
     * @param duration The duration of the fade
     * @param controller The controller requesting this fade
     * @throws TapeInUseException If the tape is currently controlled by another effect, this will be thrown
     */
    public void fadeTo(LedState s, float duration, IEffect controller) throws TapeInUseException {
        if (duration == 0){
            snapTo(s, controller);
        } else {

            if (this.getController() == controller) {

                setTransitioning(true);

                float prevR = r;
                float prevG = g;
                float prevB = b;


                //Total rgb difference
                float rChange = s.getRed() - r;
                float gChange = s.getGreen() - g;
                float bChange = s.getBlue() - b;

                //check not zero difference

//                boolean isEqual = true;
//                if (Float.valueOf(r).intValue() != Float.valueOf(s.getRed()).intValue())
//                    isEqual = false;
//                if (Float.valueOf(g).intValue() != Float.valueOf(s.getGreen()).intValue())
//                    isEqual = false;
//                if (Float.valueOf(b).intValue() != Float.valueOf(s.getBlue()).intValue())
//                    isEqual = false;
//
//                if (!isEqual) {


                    long prevTime = System.nanoTime();
                    long durationNano = (long) (duration * Math.pow(10, 9));
                    long finTime = prevTime + durationNano;


                    while (!halted) {

                        double fadeProportion = getCurrentFadeProportion(prevTime, durationNano, System.nanoTime());

                        //Calc new rgb
                        float newR = (float) (fadeProportion * rChange) + prevR;
                        float newG = (float) (fadeProportion * gChange) + prevG;
                        float newB = (float) (fadeProportion * bChange) + prevB;

                        setState(new LedState(newR, newG, newB));

                        if (fadeProportion >= 1) {
                            break;
                        }

                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
//             //   } else {
//                    try {
//                        int totalDurationMilis = Float.valueOf(duration * 1000).intValue();
//
//                        int timePerCycle = Float.valueOf(totalDurationMilis / 15f).intValue();
//
//                        for (int i = 0; i < 15; i++){
//                            if (!getHalted()){
//
//                                Thread.sleep(timePerCycle);
//
//
//
//                            } else {
//                                break;
//                            }
//                        }
//
//
//                        Thread.sleep(Float.valueOf(duration * 1000).intValue());
//                    } catch (InterruptedException e){
//                        e.printStackTrace();
//                    }
//              //  }
                if (halted) {
                    halted = false;
                }
                setTransitioning(false);
            } else
                throw new TapeInUseException(controller, this.controller);
        }
    }

    private double getCurrentFadeProportion(long prevTime, long durationNano, long curTime){

        long elapsedNano = curTime - prevTime;
        double proportion = (double) elapsedNano / (double) durationNano;

        //Time has already elapsed...
        if (proportion > 1){
            proportion = 1.0;
        }

        return proportion;
    }

    @Override
    public void fadeThruBlack(LedState s, float duration) throws TapeInUseException {
        throw new TapeInUseException(controller, this.controller);

    }

    @Override
    public void fadeToBlack(float duration, IEffect controller) throws TapeInUseException {
        fadeTo(LedState.BLACK, duration, controller);
    }

    /**
     * Halt also releases control of tape as the current execution has been halted so tape is available
     * @return
     */
    @Override
    public LedState halt() {
        setHalted(true);

        //Block while execution halts
        while (getHalted() && isTransitioning()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        releaseControl();
        return getState();
    }

    @Override
    public LedState getColour() {
        return getState();
    }



    private synchronized IEffect getController(){
        return this.controller;
    }

    private synchronized  void releaseControl(){
        this.controller = null;
        this.halted = false;
    }

    @Override
    public synchronized void setController (IEffect controller) throws TapeInUseException
    {
        if (this.controller == null) {
            this.controller = controller;
        } else {
            throw new TapeInUseException(this.controller);
        }
    }
}
