package effects;

import common.*;

import java.util.ArrayList;

public class StaticEffect implements IEffect, Runnable {
    private ITapeControl tapeControl;
    private int transition;
    private LedState colour;
    private int intensity;
    private Logger logger;

    //Do not directly mutate this as need synchronized access.
    private boolean transitioning;

    public StaticEffect(ITapeControl tapeControl, ArrayList<Long> colour, int intensity, int transition, Logger logger) throws InvalidTransitionTimeException {
        this(tapeControl, new LedState(colour.get(0), colour.get(1), colour.get(2)), intensity, transition, logger);
    }

    @Override
    public void setIntensity(int intensity) {

    }

    public StaticEffect(ITapeControl tapeControl, LedState staticColour, int intensity, int transition, Logger logger) throws InvalidTransitionTimeException{
        this.logger = logger;
        if (transition < 0 | transition > 10)
            throw new InvalidTransitionTimeException();

        this.tapeControl = tapeControl;
        this.transition = transition;
        this.intensity = intensity;
        this.colour = LedState.applyIntensity(staticColour, intensity);




    }

    public int getIntensity(){return this.intensity;}

    public LedState getColour(){
        return colour;
    }

    private synchronized void setTransistioning(boolean transitioning){
        this.transitioning = transitioning;
    }

    private synchronized boolean getTransitioning(){
        return transitioning;
    }

    @Override
    public void start() throws TapeInUseException {
        //Request control of the tape
        tapeControl.setController(this);

        Thread t = new Thread(this);
        t.start();
    }

    @Override
    /**
     * This method will have a low cost block
     */
    public LedState terminate() {
        //release control from this effect
        return tapeControl.halt();
    }

    @Override
    public void run() {
        //Run transition
        try {
            tapeControl.smartFade(colour, this);
        } catch (TapeInUseException e){
            logger.writeError(this,e);
        }
    }
}
