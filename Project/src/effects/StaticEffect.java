package effects;

import common.*;

import java.util.ArrayList;

public class StaticEffect implements IEffect, Runnable {
    private ITapeControl tapeControl;
    private int transition;
    private LedState colour;
    private LedState unalteredColour;
    private int intensity;
    private Logger logger;
    private boolean snapIntensityChange = false;

    private boolean terminated;

    //Do not directly mutate this as need synchronized access.
    private boolean transitioning;

    public StaticEffect(ITapeControl tapeControl, ArrayList<Long> colour, int intensity, int transition, Logger logger) throws InvalidTransitionTimeException {
        this(tapeControl, new LedState(colour.get(0), colour.get(1), colour.get(2)), intensity, transition, logger);
    }

    public StaticEffect(ITapeControl tapeControl, LedState staticColour, int intensity, int transition, Logger logger) throws InvalidTransitionTimeException{
        this.logger = logger;
        if (transition < 0 | transition > 10)
            throw new InvalidTransitionTimeException();

        this.tapeControl = tapeControl;
        this.transition = transition;
        this.intensity = intensity;
        this.colour = LedState.applyIntensity(staticColour, intensity);
        this.unalteredColour = staticColour;
        init();
    }

    @Override
    public void setIntensity(int intensity, boolean snap) {
        if (this.intensity != intensity) {
            this.intensity = intensity;
            tapeControl.haltRetainControl();
            this.snapIntensityChange = snap;
            this.colour = LedState.applyIntensity(unalteredColour, intensity);
        }
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
    public void init() {
        terminated = false;
    }

    @Override
    /**
     * This method will have a low cost block
     */
    public LedState terminate() {
        //release control from this effect
        terminated = true;
        return tapeControl.halt();
    }

    @Override
    public void run() {
        //Run transition
        try {
            tapeControl.smartFade(colour, this);
            //check for intensity change..
            while (!terminated) {
                if (!colour.equals(tapeControl.getColour())){
                    if (snapIntensityChange){
                        tapeControl.fadeTo(colour, 0.06f,this);
                    } else {
                        tapeControl.smartFade(colour, this);
                    }
                }

            }
        } catch (TapeInUseException e){
            logger.writeError(this,e);
        }
    }
}
