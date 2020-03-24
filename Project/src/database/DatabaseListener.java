package database;

import common.Alarm;
import common.DeviceState;
import effects.CustomEffect;

import java.util.ArrayList;

/**
 * Implemented by any class that listens to device state
 * and alarm state updates
 */
public interface DatabaseListener {
    /**
     * Callback method for when a device state is received
     * @param deviceState The device state received during this update
     */
    void onDeviceStateReceived(DeviceState deviceState);

    /**
     * Callback method for when an alarm state update is received
     * @param alarms The list of alarms sent by the database
     */
    void onAlarmStateReceived(ArrayList<Alarm> alarms);
}
