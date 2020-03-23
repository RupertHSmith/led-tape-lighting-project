package effects;

import common.*;

import java.io.*;
import java.net.Socket;

public class TcpControlEffect implements IEffect, Runnable{
    private static final int TCP_CONTROL_PORT = 5558;

    private ITapeControl tc;
    private String ipAddress;
    private Logger logger;
    private BufferedInputStream dataIn;
    private Socket socket;
    private boolean terminated;


    public TcpControlEffect(ITapeControl tapeControl, String ipAddress, Logger logger){
        tc = tapeControl;
        this.ipAddress = ipAddress;
        this.logger = logger;
    }

    @Override
    public void start() throws TapeInUseException {
        tc.setController(this);
        int counter = 0;
        setTerminated(false);
        boolean notSet = true;

        //Make 3 attempts at starting the TCP connection
        while (counter < 3 && notSet) {
            try {
                socket = new Socket(ipAddress, TCP_CONTROL_PORT);
                dataIn = new BufferedInputStream(socket.getInputStream());
                notSet = false;
                new Thread(this).start();
            } catch (Exception e) {
                logger.writeError(this, e);
                try {
                    Thread.sleep(2000);
                } catch (Exception e1){
                    logger.writeError(this,e1);
                }
            }
            counter++;
        }
    }

    public boolean isSocketConnected(){
        if (socket != null)
            return socket.isConnected();
        else
            return false;
    }

    private synchronized void setTerminated(boolean terminated){ this.terminated = terminated; }

    private synchronized boolean isTerminated() { return this.terminated; }

    @Override
    public LedState terminate() {
        setTerminated(true);
        tc.halt();
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            logger.writeError(this, e);
        }
        return tc.getColour();
    }

    private int byteToInt(byte b){
        int returnVal = b & 0xFF;
        return returnVal;
    }

    @Override
    public void run() {
        //first fade tape out
        try {
            tc.smartFadeToBlack(this);
            while (!isTerminated()){
                //Listen to TCP connection...
                try {
                    // Packet structure...

                    /* |----------|-------|-------|-------|-------|  */
                    /* |    1     |   2   |   3   |   4   |   5   |  */
                    /* |----------|-------|-------|-------|-------|  */
                    /* | End flag |  Red  | Green | Blue  | Fade  |  */
                    /* |----------|-------|-------|-------|-------|  */

                    byte[] inputBytes = new byte[5];

                    if (dataIn.read(inputBytes, 0, 5) == 5) {
                        if (inputBytes[0] == 0) {
                            int r = byteToInt(inputBytes[0]);
                            int g = byteToInt(inputBytes[1]);
                            int b = byteToInt(inputBytes[2]);
                            int fade = byteToInt(inputBytes[3]);
                            tc.fadeTo(new LedState(r, g, b), fade, this);
                        } else {
                            terminate();
                        }
                    } else {
                        logger.writeError(this, "Invalid TCP message");
                    }
                } catch (IOException e1){
                    logger.writeError(this,e1);
                }

            }
        } catch (TapeInUseException e){
            logger.writeError(this,e);
        }
    }
}
