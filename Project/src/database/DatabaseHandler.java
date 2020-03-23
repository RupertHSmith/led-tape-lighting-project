package database;

import common.Logger;

import java.io.IOException;

public class DatabaseHandler {
    public DatabaseHandler(DatabaseListener databaseListener, Logger logger) throws IOException {
        DeviceStateListener stateListener = new DeviceStateListener(databaseListener, logger);


        AlarmListener alarmListener = new AlarmListener(databaseListener, logger);

        new Thread(stateListener).start();
        new Thread(alarmListener).start();
    }
}
