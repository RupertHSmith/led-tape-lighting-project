package common;

import java.util.List;

public interface IAlarmController {
    void addAlarmListener(IAlarmListener listener);
    void removeAlarmListener();
    List<Alarm> getAlarms();
}
