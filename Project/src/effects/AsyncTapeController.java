package effects;

import common.ITapeControl;
import common.LedState;
import common.Logger;
import common.TapeInUseException;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncTapeController implements Runnable{
    private BlockingQueue<byte[]> packetQueue = new LinkedBlockingQueue<>();
    private IEffect controller;
    private boolean running = true;
    private ITapeControl tc;
    private Logger logger;

    public AsyncTapeController(ITapeControl tc, IEffect controller, Logger logger){
        this.controller = controller;
        this.tc = tc;
        this.logger = logger;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    private int byteToInt(byte b){
        int returnVal = b & 0xFF;
        return returnVal;
    }

    public void putInQueue(byte[] packet){
        try {
            packetQueue.put(packet);
        } catch (InterruptedException e){
            logger.writeError(this,e);
        }
    }

    @Override
    public void run() {
        while(isRunning()){
            try {
                byte[] inputBytes = packetQueue.take();
                int r = byteToInt(inputBytes[1]);
                int g = byteToInt(inputBytes[2]);
                int b = byteToInt(inputBytes[3]);
                float fade = getFade(inputBytes);
                tc.fadeTo(new LedState(r, g, b), fade, controller);
            } catch (TapeInUseException | InterruptedException e){
                logger.writeError(this, e);
            }
        }
    }

    private float getFade(byte[] packet){
        return ByteBuffer.wrap(packet, 4,4).getFloat();
    }
}
