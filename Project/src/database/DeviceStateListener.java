package database;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import common.DeviceState;
import common.Logger;
import effects.CustomEffect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class DeviceStateListener implements Runnable{
    private Logger logger;
    private DatabaseListener databaseListener;

    private ServerSocket serverSocket;
    private Socket client;
    private BufferedReader in;

    public DeviceStateListener(DatabaseListener databaseListener, Logger logger) throws IOException {
        this.logger = logger;
        this.databaseListener = databaseListener;

        serverSocket = new ServerSocket(5555);
        logger.writeMessage(this,"DeviceStateListener: Waiting for connection on port 5555");

        Socket client = serverSocket.accept();
        logger.writeMessage(this,"Connection received on port 5555");
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
        while (true) {
            try {
                String inString = in.readLine();
                int noPipes = countOccurences(inString);
                logger.writeMessage(this, "Device state received");

                //Only attempt to split if properly formatted
                if (noPipes == 1) {
                    Gson g = new Gson();

                    String[] splitString = inString.split("\\|");

                    DeviceState d = g.fromJson(splitString[0], DeviceState.class);
                    CustomEffect e = g.fromJson(splitString[1], CustomEffect.class);
                    d.setCustomEffect(e);

                    if (d != null && e != null) {
                        databaseListener.onDeviceStateReceived(d);
                    }
                } else {
                    logger.writeError(this, "Input json incorrectly formatted");
                }

            } catch (IOException | JsonSyntaxException e){
                logger.writeError(this, e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2){
                    logger.writeError(this, e2);
                }
            }
        }
    }
}
