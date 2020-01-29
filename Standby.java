package effects;

import common.*;

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

    private SimpleTimeObject nextAlarmStart;
    private SimpleTimeObject nextAlarmFinish;

    private Alarm nextAlarm;

    private AlarmEvent alarmEvent;
    private ScheduledFuture scheduledFuture;

    private boolean alarmComplete;
    private boolean initialized = false;

    public Standby(ITapeControl tc, IAlarmController alarmController, int transition, List<Alarm> alarms){
        this.tc = tc;
        this.transition = transition;
        this.alarmController = alarmController;

        if(alarms != null){
            this.alarms = alarms;
            Collections.sort(this.alarms);
        } else {
            this.alarms = new ArrayList<>();
        }

    }

    public synchronized void setAlarmComplete(boolean alarmEventExecuting){
        this.alarmComplete = alarmEventExecuting;
    }

    public synchronized boolean isAlarmComplete(){
        return alarmComplete;
    }

    synchronized void disableScheduledAlarm(){
        if (alarmEvent != null && scheduledFuture != null){

            //blocks while terminating...
            System.out.println("Terminating event.");
            alarmEvent.terminate();

            System.out.println("Cancelling future");
            scheduledFuture.cancel(false);

//            scheduledFuture = null;
//            alarmEvent = null;

            nextAlarm = null;
        }
    }

    synchronized private Alarm nextAlarm(List<Alarm> alarms){
        Calendar rightNow = Calendar.getInstance();
        SimpleTimeObject curTime = new SimpleTimeObject(rightNow.get(Calendar.HOUR_OF_DAY), rightNow.get(Calendar.MINUTE));

        //First check for this day...
        //This for loop will iterate through from earliest alarm to latest
        for (Alarm a : alarms){
            if (a.isEnabled() && a.getSimpleTimeObjectStart().compareTo(curTime) > 0){
                //Then this alarm is enabled and occurs after the current time so set this as the active alarm
                return a;
            }
        }

        //If no such alarm was found, we then need to check the next day
        for (Alarm a : alarms){
            if (a.isEnabled() && a.getSimpleTimeObjectStart().compareTo(curTime) < 0){
                return a;
            }
        }

        //No active alarm was found...
        return null;
    }

    synchronized private void setActiveAlarm(Alarm a){
        if (a != null) {
            nextAlarm = a;

            int duration = a.getDuration();

            try {
                alarmEvent = new AlarmEvent(tc, duration, new LedState(a.getColour()), this);
                scheduleAlarmTask(alarmEvent, a);

                System.out.println("Alarm scheduled for " + a.getFinishHour() + ":" + a.getFinishMinute() + " with duration " + duration + " minutes.");

            } catch (LedState.InvalidRGBException | TapeInUseException e){
                e.printStackTrace();
                alarmEvent = null;
            }
        }
    }

    synchronized private void scheduleAlarmTask(AlarmEvent alarmEvent, Alarm a){
        //Get current time
        Calendar rightNow = Calendar.getInstance();
        SimpleTimeObject curTime = new SimpleTimeObject(rightNow.get(Calendar.HOUR_OF_DAY), rightNow.get(Calendar.MINUTE));

        int delayMinute = SimpleTimeObject.calculateTimeDifference(curTime, a.getSimpleTimeObjectStart());
        System.out.println("Delay: " + delayMinute);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduledFuture = scheduler.schedule(alarmEvent, delayMinute, TimeUnit.MINUTES);
    }


    public synchronized List<Alarm> getAlarms() {
        return alarms;
    }

    public synchronized void setAlarms(List<Alarm> alarms) {
        this.alarms.retainAll(alarms);
        List<Alarm> alarmsToAdd = alarms;
        alarmsToAdd.removeAll(this.alarms);

        this.alarms.addAll(alarmsToAdd);

        //alarms in ascending order
        Collections.sort(this.alarms);
    }

    @Override
    public void start() throws TapeInUseException {
        new Thread(this).start();
    }

    @Override
    public LedState terminate() {
        //We need to disable all alarms
        disableScheduledAlarm();
        return tc.halt();
    }

    /**
     * Called on external thread when user makes a change to their alarms...
     * @param alarms
     */
    @Override
    public synchronized void notifyUpdate(List<Alarm> alarms) {
        if(!isAlarmComplete() && initialized) {
            setAlarms(alarms);

            Alarm newNextAlarm = nextAlarm(getAlarms());

            if (newNextAlarm == null) {
                disableScheduledAlarm();
                System.out.println("No alarms found.");
            } else {
                if (nextAlarm != null) {
                    if (newNextAlarm.equals(nextAlarm)) {
                        //Then no alarm change required
                        System.out.println("Alarm change not required.");
                    } else {
                        //Disable current alarm
                        disableScheduledAlarm();
                        setActiveAlarm(newNextAlarm);
                    }
                } else {
                    setActiveAlarm(newNextAlarm);
                }
            }
        }
    }
    private synchronized void initAlarms(List<Alarm> alarms){
        Alarm newNextAlarm = nextAlarm(getAlarms());
        if (newNextAlarm == null) {
            disableScheduledAlarm();
            System.out.println("No alarms found.");
        } else {
            setActiveAlarm(newNextAlarm);
        }
        initialized = true;
    }

    @Override
    public void run() {
        try {
            tc.setController(this);
            tc.fadeTo(LedState.BLACK, transition, this);
            tc.halt();


            alarmController.addAlarmListener(this);
            setAlarmComplete(false);

            if(!initialized && getAlarms() != null){
               initAlarms(getAlarms());
            }

        } catch (TapeInUseException e){
            e.printStackTrace();
        }
    }
}
