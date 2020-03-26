package effects;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import common.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpControlEffect implements IEffect, Runnable{
    private static final int TCP_CONTROL_PORT = 5558;
    private static final int RGB_PACKET = 0;
    private static final int SHUTDOWN_PACKET = 1;
    private static final int REBOOT_PACKET = 2;
    private static final int PACKET_SIZE = 5;

    private ITapeControl tc;
    private String ipAddress;
    private Logger logger;
    private BufferedInputStream dataIn;
    private Socket socket;
    private boolean terminated;
    private TcpDirectFinishedListener tcpDirectFinishedListener;


    public TcpControlEffect(ITapeControl tapeControl, TcpDirectFinishedListener tcpDirectFinishedListener, String ipAddress, Logger logger){
        tc = tapeControl;
        this.ipAddress = ipAddress;
        this.logger = logger;
        this.tcpDirectFinishedListener = tcpDirectFinishedListener;
    }

    @Override
    public void start() throws TapeInUseException {
        tc.setController(this);
        int counter = 0;
        setTerminated(false);
        boolean notSet = true;

        //Make 3 attempts at starting the TCP connection
        while (counter < 2 && notSet) {
            try {
                logger.writeMessage(this,"Attempting to open TCP socket...");
                socket = new Socket();
                socket.connect(new InetSocketAddress(ipAddress, TCP_CONTROL_PORT), 5000);
                dataIn = new BufferedInputStream(socket.getInputStream());
                notSet = false;
                logger.writeMessage(this, "TCP Direct socket opened");
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

        if (notSet)
            tcpDirectFinishedListener.tcpDirectFinished();
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
    public synchronized LedState terminate() {
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
                    // Packet structure...
                    //Byte 1 represents what info the packet contains - 1 for an RGB packet

                    /* |----------|-------|-------|-------|-------|  */
                    /* |    1     |   2   |   3   |   4   |   5   |  */
                    /* |----------|-------|-------|-------|-------|  */
                    /* | Content  |  Red  | Green | Blue  | Fade  |  */
                    /* |----------|-------|-------|-------|-------|  */

                    byte[] inputBytes = new byte[PACKET_SIZE];
                    if (dataIn.read(inputBytes, 0, PACKET_SIZE) == PACKET_SIZE) {
                        if (inputBytes[0] == RGB_PACKET) {
                            int r = byteToInt(inputBytes[1]);
                            int g = byteToInt(inputBytes[2]);
                            int b = byteToInt(inputBytes[3]);
                            int fade = byteToInt(inputBytes[4]);
                            tc.fadeTo(new LedState(r, g, b), fade, this);
                        } else if (inputBytes[0] == SHUTDOWN_PACKET) {
                            shutdownPi();
                        } else if (inputBytes[0] == REBOOT_PACKET){
                            rebootPi();
                        } else {
                            tcpDirectFinishedListener.tcpDirectFinished();
                        }
                    } else {
                        logger.writeError(this, "Invalid TCP message, closing connection...");
                        tcpDirectFinishedListener.tcpDirectFinished();
                    }
            }
        } catch (TapeInUseException | IOException e){
            logger.writeError(this,e);
            tcpDirectFinishedListener.tcpDirectFinished();
        }
    }

    /**
     * Attempts to shutdown the device
     */
    private void shutdownPi(){
        Runtime p = Runtime.getRuntime();
        try {
            logger.writeMessage(this, "Shutting down...");
            p.exec("sudo shutdown");
        } catch (IOException e){
            logger.writeError(this,e);
        }
    }

    /**
     * Attempts to reboot the device
     */
    private void rebootPi(){
        Runtime p = Runtime.getRuntime();
        try {
            logger.writeMessage(this, "Restarting...");
            p.exec("sudo reboot");
        } catch (IOException e){
            logger.writeError(this,e);
        }
    }
}
