package common;

import effects.IEffect;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;


public class TapeControl implements ITapeControl {

    private float r;
    private float g;
    private float b;

    private Runtime p;

    private static int PIN_NUMBER_RED = 17;
    private static int PIN_NUMBER_GREEN = 22;
    private static int PIN_NUMBER_BLUE = 24;

    private int[] pwmTranslation;
    private static final double PWM_CURVE_PARAMETER = 2.35;

    private static final int _PI_CMD_PWM = 83886080;
    private static final int _PI_CMD_PFS = 117440512;
    private static final int _PI_CMD_PRS = 100663296;
    private static final int _PWM_FREQUENCY = 1677721600;
    private static final int _PWM_RESOLUTION = 262144;

    private static final int _PIN_RED = 285212672;
    private static final int _PIN_GREEN = 369098752;
    private static final int _PIN_BLUE = 402653184;

    private static final int FADE_UPDATE_PERIOD = 20;
    private static final double TIME_PER_RGB_VAL = 0.004;

    private Socket socket;
    private DataOutputStream gpioDataOut;

    private IEffect controller;

    boolean halted = false;
    boolean transitioning = false;

    public TapeControl(){
        p = Runtime.getRuntime();
        try {
            p.exec("sudo pigpiod");
            p.exec("sudo pigs p 17 0 p 22 0 p 24 0");
        } catch (Exception e){
            e.printStackTrace();
        }

        r = 0;
        g = 0;
        b = 0;

        initialisePWMTranslationArray(PWM_CURVE_PARAMETER);

        try {
            //Initialize PIGPIO TCP socket connection
            InetAddress localHost = InetAddress.getLocalHost();
            socket = new Socket(localHost, 8888);
            gpioDataOut = new DataOutputStream(socket.getOutputStream());

            //Init pins
            initialisePinParameters();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    private void initialisePWMTranslationArray(double k){
        pwmTranslation = new int[256];
        double q = (k / 255d);
        double c = (1023f / (Math.exp(k) - 1));

        for (int x = 0; x < 256; x++){
            pwmTranslation[x] = Integer.reverseBytes((int) Math.round((Math.exp(x * q) - 1) * c));
        }
    }

    private void initialisePinParameters() throws IOException{
        ByteBuffer b;
        byte[] bytes;

        //SET FREQUENCY OF RED TO 100 HZ//
        b = ByteBuffer.allocate(16);

        b.putInt(_PI_CMD_PFS);
        b.putInt(_PIN_RED);
        b.putInt(_PWM_FREQUENCY);
        b.putInt(0);

        bytes = b.array();

        gpioDataOut.write(bytes);

        //SET RANGE OF RED TO 10 BIT//
        b = ByteBuffer.allocate(16);

        b.putInt(_PI_CMD_PRS);
        b.putInt(_PIN_RED);
        b.putInt(_PWM_RESOLUTION);
        b.putInt(0);

        bytes = b.array();

        gpioDataOut.write(bytes);


        //SET FREQUENCY OF PIN 22 TO 100 HZ//
        b = ByteBuffer.allocate(16);

        b.putInt(_PI_CMD_PFS);
        b.putInt(_PIN_GREEN);
        b.putInt(_PWM_FREQUENCY);
        b.putInt(0);

        bytes = b.array();

        gpioDataOut.write(bytes);

        //SET RANGE OF PIN 22 TO 10 BIT//
        b = ByteBuffer.allocate(16);

        b.putInt(_PI_CMD_PRS);
        b.putInt(_PIN_GREEN);
        b.putInt(_PWM_RESOLUTION);
        b.putInt(0);

        bytes = b.array();

        gpioDataOut.write(bytes);

        //SET FREQUENCY OF BLUE TO 100 HZ//
        b = ByteBuffer.allocate(16);

        b.putInt(_PI_CMD_PFS);
        b.putInt(_PIN_BLUE);
        b.putInt(_PWM_FREQUENCY);
        b.putInt(0);

        bytes = b.array();

        gpioDataOut.write(bytes);

        //SET RANGE OF BLUE TO 10 BIT//
        b = ByteBuffer.allocate(16);

        b.putInt(_PI_CMD_PRS);
        b.putInt(_PIN_BLUE);
        b.putInt(_PWM_RESOLUTION);
        b.putInt(0);

        bytes = b.array();

        gpioDataOut.write(bytes);
    }

    private synchronized  void setTransitioning(boolean transitioning){
        this.transitioning = transitioning;
    }

    private synchronized  boolean isTransitioning(){
        return transitioning;
    }



    private synchronized void setState(LedState s){
        ByteBuffer byteBuffer;
        byte[] bytes;

        int newR = s.getRed();
        int newG = s.getGreen();
        int newB = s.getBlue();

        try {
            //Don't send update if the same to save computation
            if (newR != r) {
                r = newR;
                byteBuffer = ByteBuffer.allocate(16);

                byteBuffer.putInt(_PI_CMD_PWM);
                byteBuffer.putInt(_PIN_RED);
                byteBuffer.putInt(pwmTranslation[newR]);
                System.out.println("Red pwm trans " + pwmTranslation[newR]);
                byteBuffer.putInt(0);

                bytes = byteBuffer.array();

                gpioDataOut.write(bytes);
            }

            if (newG != g) {
                g = newG;
                byteBuffer = ByteBuffer.allocate(16);

                byteBuffer.putInt(_PI_CMD_PWM);
                byteBuffer.putInt(_PIN_GREEN);
                byteBuffer.putInt(pwmTranslation[newG]);
                byteBuffer.putInt(0);

                bytes = byteBuffer.array();

                gpioDataOut.write(bytes);
            }

            if (newB != b) {
                b = newB;
                byteBuffer = ByteBuffer.allocate(16);

                byteBuffer.putInt(_PI_CMD_PWM);
                byteBuffer.putInt(_PIN_BLUE);
                byteBuffer.putInt(pwmTranslation[newB]);
                byteBuffer.putInt(0);

                bytes = byteBuffer.array();

                gpioDataOut.write(bytes);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        //System.out.println(r + ", " + g + ", "+ b);

    //    rgbViewer.setColour(s);
    }

    private synchronized LedState getState(){
        return new LedState(r, g, b);
    }

    private synchronized  void setHalted(boolean halted){
        this.halted = halted;
    }

    private synchronized  boolean getHalted(){
        return this.halted;
    }

    @Override
    public void snapTo(LedState s, IEffect controller) throws TapeInUseException{
        if (getController() == controller) {
            setState(s);
        } else
            throw new TapeInUseException(controller, this.controller);
    }

    /**
     * Fades from the current led state to another at a fixed rate (defined by the smart fade constant)
     * @param s
     * @param controller
     * @throws TapeInUseException
     */
    @Override
    public void smartFade(LedState s, IEffect controller) throws TapeInUseException {
        float duration = calculateSmartDuration(s);
        //now perform this fade
        fadeTo(s, duration, controller);
    }

    private float calculateSmartDuration(LedState s){
        //Total rgb difference
        float rChange = Math.abs(s.getRed() - r);
        float gChange = Math.abs(s.getGreen() - g);
        float bChange = Math.abs(s.getBlue() - b);

        float dominant;
        //choose largest
        if (rChange > gChange && rChange > bChange){
            dominant = rChange;
        } else if (gChange > rChange && gChange > bChange){
            dominant = gChange;
        } else {
            dominant = bChange;
        }

        return new Double(dominant * TIME_PER_RGB_VAL).floatValue();
    }

    /**
     * Fades from the current led state to another.
     * @param s The Led State to fade to
     * @param duration The duration of the fade in seconds
     * @param controller The controller requesting this fade
     * @throws TapeInUseException If the tape is currently controlled by another effect, this will be thrown
     */
    public void fadeTo(LedState s, float duration, IEffect controller) throws TapeInUseException {
        if (duration == 0){
            snapTo(s, controller);
        } else {

            if (this.getController() == controller) {

                setTransitioning(true);

                float prevR = r;
                float prevG = g;
                float prevB = b;


                //Total rgb difference
                float rChange = s.getRed() - r;
                float gChange = s.getGreen() - g;
                float bChange = s.getBlue() - b;


                long prevTime = System.nanoTime();
                long durationNano = (long) (duration * Math.pow(10, 9));
                long finTime = prevTime + durationNano;

                while (!halted) {

                    double fadeProportion = getCurrentFadeProportion(prevTime, durationNano, System.nanoTime());

                    //Calc new rgb
                    float newR = (float) (fadeProportion * rChange) + prevR;
                    float newG = (float) (fadeProportion * gChange) + prevG;
                    float newB = (float) (fadeProportion * bChange) + prevB;

                    setState(new LedState(newR, newG, newB));

                    if (fadeProportion >= 1) {
                        break;
                    }

                    try {
                        Thread.sleep(FADE_UPDATE_PERIOD);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (halted) {
                    halted = false;
                }
                setTransitioning(false);
            } else
                throw new TapeInUseException(controller, this.controller);
        }
    }

    private double getCurrentFadeProportion(long prevTime, long durationNano, long curTime){

        long elapsedNano = curTime - prevTime;
        double proportion = (double) elapsedNano / (double) durationNano;

        //Time has already elapsed...
        if (proportion > 1){
            proportion = 1.0;
        }

        return proportion;
    }

    @Override
    public void fadeThruBlack(LedState s, float duration) throws TapeInUseException {
        throw new TapeInUseException(controller, this.controller);

    }

    @Override
    public void fadeToBlack(float duration, IEffect controller) throws TapeInUseException {
        fadeTo(LedState.BLACK, duration, controller);
    }

    @Override
    public void smartFadeToBlack(IEffect controller) throws TapeInUseException {
        fadeToBlack(calculateSmartDuration(LedState.BLACK), controller);
    }

    /**
     * Halt also releases control of tape as the current execution has been halted so tape is available
     * @return
     */
    @Override
    public LedState halt() {
        setHalted(true);

        //Block while execution halts
        while (getHalted() && isTransitioning()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        releaseControl();
        return getState();
    }

    @Override
    public LedState getColour() {
        return getState();
    }



    private synchronized IEffect getController(){
        return this.controller;
    }

    private synchronized  void releaseControl(){
        this.controller = null;
        this.halted = false;
    }

    @Override
    public synchronized void setController (IEffect controller) throws TapeInUseException
    {
        if (this.controller == null) {
            this.controller = controller;
        } else {
            throw new TapeInUseException(this.controller);
        }
    }
}
