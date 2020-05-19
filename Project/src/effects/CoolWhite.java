package effects;

import common.ITapeControl;
import common.LedState;
import common.Logger;
import common.TapeInUseException;

import java.util.ArrayList;

public class CoolWhite implements IEffect, Runnable {
    private ITapeControl tapeControl;
    private int transition;
    private int intensity;
    private boolean terminated;
    private LedState colour;
    private LedState unalteredColour;
    private Logger logger;
    private boolean intensityChanged;


    public CoolWhite(ITapeControl tapeControl, int intensity, int transition, Logger logger) throws InvalidTransitionTimeException {
        this.logger = logger;
        if (transition < 0 | transition > 10)
            throw new InvalidTransitionTimeException();
        this.unalteredColour = new LedState(255,150,70);
        this.tapeControl = tapeControl;
        this.transition = transition;
        this.intensity = intensity;
        intensityChanged = false;
        setTerminated(false);
        setAppliedColour(LedState.applyIntensity(unalteredColour, intensity));
    }

    private void setAppliedColour(LedState colour){
        this.colour = colour;
    }

    private LedState getAppliedColour(){
        return this.colour;
    }



    public int getIntensity (){
        return this.intensity;
    }

    @Override
    public void setIntensity(int intensity) {
        if (this.intensity != intensity) {
            this.intensity = intensity;
            this.intensityChanged = true;
            setAppliedColour(LedState.applyIntensity(unalteredColour, intensity));
        } else {
            intensityChanged = false;
        }
    }

    @Override
    public void start() throws TapeInUseException {
        //Request control of the tape
        tapeControl.setController(this);

        Thread t = new Thread(this);
        t.start();
    }

    public synchronized void setTerminated(boolean terminated){
        this.terminated = terminated;
    }

    public synchronized boolean getTerminated(){
        return this.terminated;
    }

    @Override
    public LedState terminate() {
        //release control from this effect
        setTerminated(true);
        return tapeControl.halt();
    }

    @Override
    public void run() {
        //Run transition
        try {
            tapeControl.smartFade(colour,this);

            //check for intensity change..
            while (!getTerminated()) {
                if (intensityChanged){
                    tapeControl.smartFade(colour,this);
                    intensityChanged = false;
                }
            }

        } catch (TapeInUseException e){
            logger.writeError(this, e);
        }
    }
}
