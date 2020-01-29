package com.rgbtape.app;

import java.util.List;

public interface AlarmsListener {
    void onAlarmUpdateRecieved(List<Alarm> alarms);
}
