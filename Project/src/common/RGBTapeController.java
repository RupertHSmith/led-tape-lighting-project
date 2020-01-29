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
    public static final int TRANSITION_DURATION = 2;

    private DeviceUID duid;
    private String ouid;

    private EffectsManager effectsManager;

    private boolean listening = true;
    private BlockingQueue<DeviceState> deviceStateQueue = new LinkedBlockingQueue<>();

    private List<Alarm> alarms;

    private IAlarmListener alarmListener;

    public static void main(String[] args){
        RGBTapeController rgbTapeController = new RGBTapeController();
    }

    public RGBTapeController (){
            try {
                DatabaseHandler handler = new DatabaseHandler(this);
                effectsManager = new EffectsManager(new TapeControl(), this);
                new Thread(this).start();

                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e){
                        System.err.println(e.getCause());
                    }
                }

            } catch (IOException e){
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
            e.printStackTrace();
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
                e.printStackTrace();
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