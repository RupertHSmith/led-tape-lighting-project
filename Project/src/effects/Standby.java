package effects;

import common.*;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This is effectively the device switched off and will switch on at the first alarm reached. At this point the effect is terminated releasing control of the tape.
 */
public class Standby implements IEffect, IAlarmListener, Runnable {
    private List<Alarm> alarms;
    private ITapeControl tc;
    private int transition;
    private IAlarmController alarmController;
    private Logger logger;

    private boolean terminated;

    private Alarm activeAlarm;
    private boolean alarmCancelled;

    private boolean alarmComplete;

    @Override
    public void setIntensity(int intensity, boolean snap) {

    }

    public Standby(ITapeControl tc, IAlarmController alarmController, int transition, List<Alarm> alarms, Logger logger){
        this.logger = logger;
        this.tc = tc;
        this.transition = transition;
        this.alarmController = alarmController;

        alarmComplete = false;
        setAlarmCancelled(false);
        init();

        if(alarms != null){
            this.alarms = alarms;
            Collections.sort(this.alarms);
        } else {
            this.alarms = new ArrayList<>();
        }

    }

    private synchronized boolean isAlarmCancelled(){
        return this.alarmCancelled;
    }

    private synchronized void setAlarmCancelled(boolean alarmCancelled){
        this.alarmCancelled = alarmCancelled;
    }

    private synchronized boolean isTerminated(){
        return this.terminated;
    }

    private synchronized void setTerminated(boolean terminated){
        this.terminated = terminated;
    }

    private synchronized void setActiveAlarm(Alarm a){
        this.activeAlarm = a;
    }

    private synchronized  Alarm getActiveAlarm(){
        return activeAlarm;
    }

    public synchronized void setAlarmComplete(boolean alarmEventExecuting){
        this.alarmComplete = alarmEventExecuting;
    }

    public synchronized boolean isAlarmComplete(){
        return alarmComplete;
    }

    public synchronized List<Alarm> getAlarms() {
        return alarms;
    }

    public synchronized void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;

        //alarms in ascending order
        Collections.sort(this.alarms);
    }

    @Override
    public void start() throws TapeInUseException {
        new Thread(this).start();
    }

    @Override
    public void init() {
        terminated = false;
    }

    @Override
    public LedState terminate() {
        //This just stops the loop
        setTerminated(true);
        return tc.halt();
    }

    @Override
    public void removeAlarmController(){
        this.alarmController = null;
    }

    /**
     * Called on external thread when user makes a change to their alarms...
     * @param alarms
     */
    @Override
    public synchronized void notifyUpdate(List<Alarm> alarms) {
        if(!isAlarmComplete()) {
            setAlarms(alarms);

            Alarm activeAlarm = getActiveAlarm();
            if(activeAlarm != null){
                if(!checkAlarmStillActive(activeAlarm)){
                    //need to cancel the currently fading alarm...
                    logger.writeMessage(this,"Cancelling running alarm");
                    tc.halt();
                    setAlarmCancelled(true);
                    try {
                        tc.setController(this);
                        tc.fadeToBlack(1,this);
                    } catch (TapeInUseException e){
                        logger.writeError(this,e);
                    }

                    setAlarmComplete(false);
                }
            }
        }
    }

    private boolean checkAlarmStillActive(Alarm activeAlarm){
        List<Alarm> alarms = getAlarms();

        boolean foundActiveAlarm = false;
        for (Alarm a : alarms){
            if (a.equals(activeAlarm)){
                foundActiveAlarm = true;
                break;
            }
        }
        return foundActiveAlarm;
    }

    /**
     * Go through the list of active alarms and check if any have start time that is the current time
     * @param currentTime The current time of day
     * @return Returns an alarm to be initiated or null if there are none
     */
    private Alarm checkAlarms(SimpleTimeObject currentTime){
        List<Alarm> alarms = getAlarms();
        for (Alarm a : alarms){
            //If alarm is active AND the start time is the current time then activate alarm
            if(a.isEnabled() && a.getSimpleTimeObjectStart().equals(currentTime)) {
                //then we need to initiate this alarm
                return a;
            }
        }
        //Return null if no alarms
        return null;
    }

    private void initiateAlarm(Alarm a){
        try {
            logger.writeMessage(this, "Beginning alarm fade up");
            setActiveAlarm(a);
            tc.fadeTo(new LedState(a.getColour()), a.getDuration() * 60, this);
            setActiveAlarm(null);
            setAlarmComplete(true);
        } catch (TapeInUseException | LedState.InvalidRGBException e){
            logger.writeError(this,e);
        }
    }

    @Override
    public void run() {
        try {
            tc.setController(this);
            tc.smartFade(LedState.BLACK, this);
            setAlarmComplete(false);
            alarmController.addAlarmListener(this);



            //Now run in loop checking current time against the list of alarms
            while (!isTerminated()) {
                if (!isAlarmComplete()) {
                    Calendar timeNow = Calendar.getInstance();
                    SimpleTimeObject curTime = new SimpleTimeObject(timeNow.get(Calendar.HOUR_OF_DAY), timeNow.get(Calendar.MINUTE));

                    //   System.out.println(curTime.toString());

                    //Find an alarm to be initiated
                    Alarm a = checkAlarms(curTime);

                    if (a != null)
                        initiateAlarm(a);
                        if(isAlarmCancelled()){
                            setAlarmComplete(false);
                            setAlarmCancelled(false);
                        }

                    try {
                        //Low frequency to keep pi cool
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        logger.writeError(this,e);
                    }

                } else {
                    try {
                        //Low frequency to keep pi cool
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        logger.writeError(this,e);
                    }
                }
            }

            // if(!initialized && getAlarms() != null){
            //    initAlarms(getAlarms());
            //}

        } catch (TapeInUseException e) {
            logger.writeError(this,e);
        }
    }

}
