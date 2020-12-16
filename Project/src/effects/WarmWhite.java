package effects;

import common.ITapeControl;
import common.LedState;
import common.Logger;
import common.TapeInUseException;

public class WarmWhite implements Runnable, IEffect {
    private ITapeControl tapeControl;
    private int transition;
    private int intensity;
    private LedState colour;
    private LedState unalteredColour;
    private Logger logger;
    private boolean terminated;
    private boolean snapIntensityChange = false;

    public WarmWhite(ITapeControl tapeControl, int intensity, int transition, Logger logger) throws InvalidTransitionTimeException {
        this.logger = logger;
        if (transition < 0 | transition > 10)
            throw new InvalidTransitionTimeException();
        this.colour = new LedState(255,150,40);
        this.unalteredColour = new LedState(255,150,40);
        this.tapeControl = tapeControl;
        this.transition = transition;
        this.intensity = intensity;
        this.colour = LedState.applyIntensity(colour, intensity);
        init();
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
    public void init() {
        terminated = false;
    }

    @Override
    public void setIntensity(int intensity, boolean snap) {
        if (this.intensity != intensity) {
            this.intensity = intensity;
            tapeControl.haltRetainControl();
            this.snapIntensityChange = snap;
            colour = LedState.applyIntensity(unalteredColour, intensity);
        }
    }

    @Override
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
