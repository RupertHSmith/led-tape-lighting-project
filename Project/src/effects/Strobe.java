package effects;

import common.*;

import java.util.ArrayList;

public class Strobe implements IEffect, Runnable {

    private ITapeControl tapeControl;
    private int transition;
    private LedState colour;
    private int speed;
    private boolean terminated;
    private int intensity;
    private float timeBetweenFlash;
    private Logger logger;


    public Strobe(ITapeControl tapeControl, ArrayList<Long> colour, int speed, int intensity, int transition, Logger logger) throws InvalidTransitionTimeException, TapeInUseException {
        this(tapeControl, new LedState(colour.get(0).intValue(), colour.get(1).intValue(), colour.get(2).intValue()), speed, intensity, transition, logger);
    }

    public Strobe(ITapeControl tapeControl, LedState colour, int speed, int intensity, int transition, Logger logger) throws InvalidTransitionTimeException, TapeInUseException {
        this.logger = logger;
        if (transition < 0 | transition > 10)
            throw new InvalidTransitionTimeException();

        this.tapeControl = tapeControl;
        this.transition = transition;
        this.intensity = intensity;
        this.colour = LedState.applyIntensity(colour, intensity);
        this.speed = speed;

        calculateTimeBetweenFlash(speed);

        terminated = false;
    }

    private void calculateTimeBetweenFlash(int speed){
        timeBetweenFlash = 0.1f + ((float) (100 - speed) / 100f) * 2;
    }

    @Override
    public void start() throws TapeInUseException {
        //Take control of the tape
        tapeControl.setController(this);

        new Thread(this).start();
    }

    public int getSpeed() {
        return this.speed;
    }

    public LedState getColour() {
        return this.colour;
    }

    public int getIntensity() {
        return this.intensity;
    }

    /**
     * Runs on own thread
     */
    @Override
    public void run() {
        try {
            //First fade tape out
            tapeControl.smartFadeToBlack(this);


            //Then begin the cycling effect while running, check termination before requesting another transition
            //(While this does not seem robust, this effect can only request transitions of the tape and will not
            //be granted if this effect does not have control
            while (!getTerminated()) {
                tapeControl.snapTo(colour, this);
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e){
                    logger.writeError(this,e);
                }
                tapeControl.snapTo(LedState.BLACK, this);
                try {
                    Thread.sleep(Float.valueOf(1000 * timeBetweenFlash).longValue());
                } catch (InterruptedException e) {
                    logger.writeError(this,e);
                }
            }
        } catch (TapeInUseException e) {
            logger.writeError(this,e);
        }
    }

    public synchronized void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    public synchronized boolean getTerminated() {
        return this.terminated;
    }

    @Override
    public LedState terminate() {
        setTerminated(true);

        //release control from this effect by halting...
        return tapeControl.halt();
    }
}