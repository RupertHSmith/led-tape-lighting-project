package effects;

import common.*;

import java.io.*;
import java.net.*;

public class TcpControlEffect implements IEffect, Runnable{
    private static final int UDP_CONTROL_PORT = 5557;
    private static final int RGB_PACKET = 0;
    private static final int SHUTDOWN_PACKET = 1;
    private static final int REBOOT_PACKET = 2;
    private static final int PACKET_SIZE = 8;

    private ITapeControl tc;
    private String ipAddress;
    private Logger logger;

    private DatagramSocket datagramSocket;
    private boolean terminated;
    private TcpDirectFinishedListener tcpDirectFinishedListener;
    private AsyncTapeController asyncTapeController;


    public TcpControlEffect(ITapeControl tapeControl, TcpDirectFinishedListener tcpDirectFinishedListener, String ipAddress, Logger logger){
        tc = tapeControl;
        this.ipAddress = ipAddress;
        this.logger = logger;
        this.tcpDirectFinishedListener = tcpDirectFinishedListener;
        this.asyncTapeController = new AsyncTapeController(tapeControl,this,logger);
        new Thread(asyncTapeController).start();
    }

    @Override
    public void start() throws TapeInUseException {
        tc.setController(this);
        setTerminated(false);
        try {
            logger.writeMessage(this,"Setup UDP listening on port " + UDP_CONTROL_PORT);
            datagramSocket = new DatagramSocket(UDP_CONTROL_PORT);
            new Thread(this).start();
        } catch (SocketException e) {
            logger.writeError(this, e);
            if(datagramSocket != null && !datagramSocket.isClosed()) {
                datagramSocket.close();
                tcpDirectFinishedListener.tcpDirectFinished();
            }
        }
    }

    private synchronized void setTerminated(boolean terminated){ this.terminated = terminated; }

    private synchronized boolean isTerminated() { return this.terminated; }

    @Override
    public synchronized LedState terminate() {
        setTerminated(true);
        tc.halt();
        if (datagramSocket != null && !datagramSocket.isClosed())
            datagramSocket.close();
        if (asyncTapeController != null)
            asyncTapeController.setRunning(false);
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
                // Packet structure...
                //Byte 1 represents what info the packet contains - 1 for an RGB packet

                /* |----------|-------|-------|-------|-------|-------|-------|-------|  */
                /* |    1     |   2   |   3   |   4   |   5   |   6   |   7   |   8   |  */
                /* |----------|-------|-------|-------|-------|-------|-------|-------|  */
                /* | Content  |  Red  | Green | Blue  | Fade  | Fade  | Fade  | Fade  |  */
                /* |----------|-------|-------|-------|-------|-------|-------|-------|  */

                byte[] inputBytes = new byte[PACKET_SIZE];
                DatagramPacket inboundPacket = new DatagramPacket(inputBytes,inputBytes.length);

                //Wait for UDP packet
                datagramSocket.receive(inboundPacket);

                //Handle packet as necessary...
                if (inputBytes[0] == RGB_PACKET) {
                    tc.haltRetainControl();
                    asyncTapeController.putInQueue(inputBytes);
                } else if (inputBytes[0] == SHUTDOWN_PACKET) {
                    shutdownPi();
                } else if (inputBytes[0] == REBOOT_PACKET){
                    rebootPi();
                } else {
                    datagramSocket.close();
                    tcpDirectFinishedListener.tcpDirectFinished();
                }
            }
        } catch (TapeInUseException | IOException e){
            logger.writeError(this,e);
            tcpDirectFinishedListener.tcpDirectFinished();
        } finally {
            //Ensure UDP socket is closed to release the port
            datagramSocket.close();
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
