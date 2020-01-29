package effects;

import common.ITapeControl;
import common.LedState;
import common.TapeInUseException;

import java.util.ArrayList;

public class CoolWhite implements IEffect, Runnable {
    private ITapeControl tapeControl;
    private int transition;
    private int intensity;
    private LedState colour;


    public CoolWhite(ITapeControl tapeControl, int intensity, int transition) throws InvalidTransitionTimeException {
        if (transition < 0 | transition > 10)
            throw new InvalidTransitionTimeException();
        this.colour = new LedState(255,150,40);
        this.tapeControl = tapeControl;
        this.transition = transition;
        this.intensity = intensity;
        this.colour = LedState.applyIntensity(colour, intensity);
    }

    public int getIntensity (){
        return this.intensity;
    }

    @Override
    public void start() throws TapeInUseException {
        //Request control of the tape
        tapeControl.setController(this);

        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public LedState terminate() {
        //release control from this effect
        return tapeControl.halt();
    }

    @Override
    public void run() {
        //Run transition
        try {
            tapeControl.fadeTo(colour, transition, this);
        } catch (TapeInUseException e){
            System.err.println(e.getMessage());
        }
    }
}