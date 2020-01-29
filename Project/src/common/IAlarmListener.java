package common;

import java.util.List;

public interface IAlarmListener {
    void notifyUpdate(List<Alarm> alarms);
    void removeAlarmController();
}
