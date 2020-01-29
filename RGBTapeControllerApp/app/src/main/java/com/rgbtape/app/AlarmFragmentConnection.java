package com.rgbtape.app;

public interface AlarmFragmentConnection {
    void setAlarmListener(String username, AlarmsListener alarmListener);
    void updateAlarm(Alarm alarm);
    void removeAlarm(Alarm alarm);
    void createAlarm(Alarm alarm);
}
