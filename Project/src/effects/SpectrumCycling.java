package effects;

import common.ITapeControl;
import common.LedState;
import common.Logger;
import common.TapeInUseException;

import java.util.ArrayList;

public class SpectrumCycling implements IEffect, Runnable{
    private ITapeControl tapeControl;
    private int transition;
    private int speed;
    private float duration;
    private boolean terminated;
    private int intensity;
    private Logger logger;

    private LedState STAGE_1 = new LedState(255,0,0);
    private LedState STAGE_2 = new LedState(255,255,0);
    private LedState STAGE_3 = new LedState(0,255,0);
    private LedState STAGE_4 = new LedState(0,255,255);
    private LedState STAGE_5 = new LedState(0,0,255);
    private LedState STAGE_6 = new LedState(255,0,255);

    public SpectrumCycling(ITapeControl tapeControl, int speed , int intensity, int transition, Logger logger) throws InvalidTransitionTimeException, TapeInUseException{
        this.logger = logger;
        if (transition < 0 | transition > 10)
            throw new InvalidTransitionTimeException();

        this.tapeControl = tapeControl;
        this.transition = transition;
        this.speed = speed;
        this.intensity = intensity;
        terminated = false;

        applyIntensities(intensity);

        duration = calculateDuration(speed);
    }

    private void applyIntensities(int intensity){
        STAGE_1 = LedState.applyIntensity(STAGE_1, intensity);
        STAGE_2 = LedState.applyIntensity(STAGE_2, intensity);
        STAGE_3 = LedState.applyIntensity(STAGE_3, intensity);
        STAGE_4 = LedState.applyIntensity(STAGE_4, intensity);
        STAGE_5 = LedState.applyIntensity(STAGE_5, intensity);
        STAGE_6 = LedState.applyIntensity(STAGE_6, intensity);
    }

    private float calculateDuration(int speed){
        return (float)(0.25 + getDecimalSpeed(speed) * 9.75);
    }

    private float getDecimalSpeed(int speed){
        return (float) (100 - speed) / 100;
    }

    synchronized private boolean isTerminated() {
        return terminated;
    }

    synchronized private void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    @Override
    public void start() throws TapeInUseException {
        //Take control of the tape
        tapeControl.setController(this);
        new Thread(this).start();
    }

    @Override
    public LedState terminate() {
        setTerminated(true);
        return tapeControl.halt();
    }

    public int getSpeed(){
        return speed;
    }

    public int getIntensity(){return intensity;}

    @Override
    public void run() {
        //fade to start colour
        try {
            tapeControl.fadeTo(STAGE_1,  transition, this);

            while (!isTerminated()){
                tapeControl.fadeTo(STAGE_2,  duration, this);

                if (!isTerminated())
                    tapeControl.fadeTo(STAGE_3,  duration, this);

                if (!isTerminated())
                    tapeControl.fadeTo(STAGE_4,  duration, this);

                if (!isTerminated())
                    tapeControl.fadeTo(STAGE_5, duration, this);

                if (!isTerminated())
                    tapeControl.fadeTo(STAGE_6,  duration, this);

                if (!isTerminated())
                    tapeControl.fadeTo(STAGE_1,  duration, this);
            }
        } catch (TapeInUseException e){
            logger.writeError(this,e);
        }

    }
}
