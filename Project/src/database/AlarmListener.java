package database;

import com.google.gson.Gson;
import common.Alarm;

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

    public AlarmListener(DatabaseListener databaseListener)throws IOException {
        this.databaseListener = databaseListener;

        serverSocket = new ServerSocket(5556);
        System.out.println("DeviceStateListener: Waiting for connection on port 5556");
        Socket client = serverSocket.accept();
        System.out.println("Connection received on port 5556");
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

                System.out.println(getClass().getSimpleName() + ": Alarm State Received");

                if (noPipes >= 1){
                    String[] splitString = inString.split("\\|");
                    for (int i = 1; i < splitString.length; i++){
                        Gson gson = new Gson();

                        Alarm a = gson.fromJson(splitString[i], Alarm.class);
                        alarmList.add(a);
                    }
                } else {
                    //either malformed json or empty
                    System.out.println(getClass().getSimpleName() + ": no alarms received.");
                }
                databaseListener.onAlarmStateReceived(alarmList);
            } catch (IOException e){
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2){
                    e2.printStackTrace();
                }
            }
        }
    }
}