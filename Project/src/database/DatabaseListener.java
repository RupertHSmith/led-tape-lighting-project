package database;

import common.Alarm;
import common.DeviceState;
import effects.CustomEffect;

import java.util.ArrayList;

public interface DatabaseListener {
    void onDeviceStateReceived(DeviceState deviceState);
    void onAlarmStateReceived(ArrayList<Alarm> alarms);
}
