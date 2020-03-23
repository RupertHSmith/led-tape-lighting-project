package common;

import database.DatabaseHandler;
import database.DatabaseListener;
import effects.EffectsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class RGBTapeController implements Runnable, IAlarmController, DatabaseListener {
    private Logger logger;
    public static final int TRANSITION_DURATION = 2;

    private DeviceUID duid;
    private String ouid;

    private EffectsManager effectsManager;

    private boolean listening = true;
    private BlockingQueue<DeviceState> deviceStateQueue = new LinkedBlockingQueue<>();

    private List<Alarm> alarms;

    private IAlarmListener alarmListener;

    public static void main(String[] args){
        new RGBTapeController();
    }

    public RGBTapeController (){
        System.out.println("Beginning logging...");

            try {
                logger = new Logger();
                logger.writeMessage( this,"Logging began...");

                try {
                    duid = new DeviceUID("rupertrgbtape","SMD5050 RGB Tape 5m");

                } catch (DeviceUID.InvalidUIDException e){
                    logger.writeError(this,e);
                }

                try {
                    DatabaseHandler handler = new DatabaseHandler(this, logger);
                    effectsManager = new EffectsManager(new TapeControl(logger), this, logger);
                    new Thread(this).start();

                    while (true){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e){
                            logger.writeError(this, e);
                        }
                    }

                } catch (IOException e){
                    logger.writeError(RGBTapeController.class.getSimpleName(),e);
                }
            } catch (IOException e){
                System.out.println("Logging failed:");
                e.printStackTrace();
            }
    }

    private synchronized List<Alarm> getLocalAlarms(){
        return this.alarms;
    }

    private synchronized void setLocalAlarms(List<Alarm> alarms){
        this.alarms = alarms;
    }

    @Override
    public void onAlarmStateReceived(ArrayList<Alarm> alarms) {
        if(alarmListener != null && alarms != null){
            setLocalAlarms(alarms);
            alarmListener.notifyUpdate(alarms);
        } else if (alarms != null) {
            setLocalAlarms(alarms);
        }
    }

    @Override
    public void onDeviceStateReceived(DeviceState deviceState) {
        try {
            deviceStateQueue.put(deviceState);
        } catch (InterruptedException e){
            logger.writeError(this,e);
        }
    }


    /**
     * Runs in loop to process the queue
     */
    @Override
    public void run() {

        while (listening) {
            try {
                //Wait for an item in the queue to become available
                effectsManager.processDeviceUpdate(deviceStateQueue.take());
            } catch (InterruptedException e){
                logger.writeError(this,e);
            }
        }
    }

    @Override
    public void addAlarmListener(IAlarmListener listener) {
        this.alarmListener = listener;

        List<Alarm> alarms = getLocalAlarms();
        listener.notifyUpdate(alarms);
    }

    @Override
    public void removeAlarmListener() {
        this.alarmListener = null;
    }


    @Override
    public List<Alarm> getAlarms() {
        return this.getLocalAlarms();
    }

    class NoDeviceUIDException extends Exception{
        public NoDeviceUIDException(){
            super("No device UID file was found.");
        }
    }
}
