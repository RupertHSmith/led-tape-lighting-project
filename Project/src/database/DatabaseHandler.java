package database;

import java.io.IOException;

public class DatabaseHandler {
    public DatabaseHandler(DatabaseListener databaseListener) throws IOException {
        DeviceStateListener stateListener = new DeviceStateListener(databaseListener);


        AlarmListener alarmListener = new AlarmListener(databaseListener);

        new Thread(stateListener).start();
        new Thread(alarmListener).start();
    }
}
