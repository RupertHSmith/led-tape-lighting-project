package effects;

import common.ITapeControl;
import common.LedState;
import common.TapeInUseException;

public class AlarmEvent implements IEffect, Runnable {
    int duration;
    LedState ledState;
    ITapeControl tc;
    Standby standby;


    public AlarmEvent(ITapeControl tc, int duration, LedState ledState, Standby standby) throws TapeInUseException{
        this.tc = tc;
        this.duration = duration;
        this.ledState = ledState;
        this.standby = standby;
        tc.setController(this);
    }

    @Override
    public void start() throws TapeInUseException {
    }

    public LedState terminate(){
        try {
            tc.halt();
            tc.setController(this);
            tc.smartFadeToBlack(this);
            //tc.fadeToBlack(1, this);
        } catch (TapeInUseException e){
            e.printStackTrace();
        }
        return tc.halt();
    }

    @Override
    public void run() {
        try {
            System.out.println("Beginning fade up on alarm.");
            tc.fadeTo(ledState, duration * 60, this);
            standby.setAlarmComplete(true);
        } catch (TapeInUseException e){
            e.printStackTrace();
        }
    }
}
