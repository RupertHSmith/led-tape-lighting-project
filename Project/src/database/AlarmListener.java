package database;

import com.google.gson.Gson;
import common.Alarm;
import common.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class AlarmListener implements  Runnable{
    private ServerSocket serverSocket;
    private BufferedReader in;
    private DatabaseListener databaseListener;
    private Logger logger;

    public AlarmListener(DatabaseListener databaseListener, Logger logger)throws IOException {
        this.logger = logger;
        this.databaseListener = databaseListener;

        serverSocket = new ServerSocket(5556);
        logger.writeMessage(this, "DeviceStateListener: Waiting for connection on port 5556");
        Socket client = serverSocket.accept();
        logger.writeMessage(this,"Connection received on port 5556");
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
    }

    private int countOccurences(String in){
        if (in != null) {
            String removedPipeString = in.replace("|", "");
            return in.length() - removedPipeString.length();
        } else {
            return 0;
        }

    }

    @Override
    public void run() {
        while (true){
            try {
                String inString = in.readLine();
                int noPipes = countOccurences(inString);
                ArrayList<Alarm> alarmList = new ArrayList<>();

                logger.writeMessage(this, "Alarm State Received");

                if (noPipes >= 1){
                    String[] splitString = inString.split("\\|");
                    for (int i = 1; i < splitString.length; i++){
                        Gson gson = new Gson();

                        Alarm a = gson.fromJson(splitString[i], Alarm.class);
                        alarmList.add(a);
                    }
                } else {
                    //either malformed json or empty
                    logger.writeMessage(this, "no alarms received.");
                }
                databaseListener.onAlarmStateReceived(alarmList);
            } catch (IOException e){
                logger.writeError(this, e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2){
                    logger.writeError(this,e2);
                }
            }
        }
    }
}
