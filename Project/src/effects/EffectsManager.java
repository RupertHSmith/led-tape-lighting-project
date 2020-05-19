package effects;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import common.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class EffectsManager implements TcpDirectFinishedListener{

    public static final String STATIC = "StaticEffect";
    public static final String BREATHING = "Breathing";
    public static final String STANDBY = "Standby";
    public static final String SPECTRUM_CYCLING = "SpectrumCycling";
    public static final String CUSTOM_EFFECT = "CustomEffect";
    public static final String STROBE_EFFECT = "Strobe";
    public static final String WARM_WHITE = "WarmWhite";
    public static final String COOL_WHITE = "CoolWhite";

    public static final byte UDP_CONNECTION_VERSION = 1;
    public static final int UDP_DIRECT_REQUEST_PORT = 5558;
    public static final int UDP_DIRECT_NOTIFY_PORT = 5557;

    private static final long CONTROL_PANEL_PERIOD = 100;

    private ITapeControl tc;
    private UartCode uartCode;
    private IEffect currentEffect;
    private IAlarmController alarmController;
    private Logger logger;
    private boolean tcpDirectMode;
    private DeviceUID deviceUID;

    private int controlPanelIntensity;

    private Timer controlPanelScheduler;

    private IEffect effectBeforeTcpDirect;

    public EffectsManager (ITapeControl tc, DeviceUID duid, IAlarmController alarmController, Logger logger){
        deviceUID = duid;
        this.tc = tc;
        this.alarmController = alarmController;
        this.logger = logger;
        this.uartCode = new UartCode();
        initControlPanelScheduler();
        setTcpDirectMode(false);
        listenForTcpDirectStart();
    }

    private void initControlPanelScheduler(){
        controlPanelScheduler = new Timer();
        controlPanelScheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //Check for update..
                int newVal = uartCode.getControlPanelIntensity();
                if (newVal != controlPanelIntensity){
                    controlPanelIntensity = newVal;
                    if (currentEffect != null){
                        currentEffect.setIntensity(controlPanelIntensity);
                    }
                }
            }
        }, 0, CONTROL_PANEL_PERIOD);
    }

    /**
     * Method swaps the current effect for a new effect, calling terminate
     * on the current effect and start on the new one
     * @param effect
     */
    private void changeEffect(IEffect effect){
            try {
                if (currentEffect == null) {
                    currentEffect = effect;
                    effect.start();
                } else {
                    currentEffect.terminate();

                    if (currentEffect instanceof IAlarmListener){
                        ((IAlarmListener) currentEffect).removeAlarmController();
                        alarmController.removeAlarmListener();
                    }

                    currentEffect = effect;
                    currentEffect.start();
                }
            } catch (TapeInUseException e){
                logger.writeError(this,e);
            }
    }

    private boolean checkCurrentEffectCoolWhite (int intensity){
        if (currentEffect != null){
            if (currentEffect.getClass().getSimpleName().equals(COOL_WHITE)) {
                CoolWhite coolWhite = (CoolWhite) currentEffect;
                if (coolWhite.getIntensity() == intensity){
                    return true;
                }
            }

        }
        return false;
    }

    private boolean checkCurrentEffectWarmWhite (int intensity){
        if (currentEffect != null){
            if (currentEffect.getClass().getSimpleName().equals(WARM_WHITE)) {
                WarmWhite warmWhite = (WarmWhite) currentEffect;
                if (warmWhite.getIntensity() == intensity){
                    return true;
                }
            }

        }
        return false;
    }

    private boolean checkCurrentEffectSpectrum(int speed, int intensity){
        if (currentEffect != null){
            if (currentEffect.getClass().getSimpleName().equals(SPECTRUM_CYCLING)) {
                SpectrumCycling spectrumCycling = (SpectrumCycling) currentEffect;
                if (speed == spectrumCycling.getSpeed() && intensity == spectrumCycling.getIntensity()){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkCurrentEffectStatic(LedState staticState, int intensity){
        if (currentEffect != null){
            if (currentEffect.getClass().getSimpleName().equals(STATIC)){
                if (((StaticEffect) currentEffect).getColour().equals(staticState) && intensity == ((StaticEffect) currentEffect).getIntensity()){
                    return true;
                } else {
                    return false;
                }
            }  else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean checkCurrentEffectBreathing(LedState breathingState, int speed, int intensity){
        if (currentEffect != null){
            if (currentEffect.getClass().getSimpleName().equals(BREATHING)){
                Breathing breathingEffect = (Breathing) currentEffect;
                if(speed == breathingEffect.getSpeed() && breathingEffect.getColour().equals(breathingState) && intensity == breathingEffect.getIntensity()){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkCurrentEffectStrobe(LedState strobeState, int speed, int intensity){
        if (currentEffect != null){
            if (currentEffect.getClass().getSimpleName().equals(STROBE_EFFECT)){
                Strobe strobe = (Strobe) currentEffect;
                if(speed == strobe.getSpeed() && strobe.getColour().equals(strobeState) && intensity == strobe.getIntensity())
                    return true;
            }
        }
        return false;
    }

    /**
     * Method called by TcpControlEffect class to notify that the TCP direct communication has
     * finished and that we should return to normal operation (synchronised as called by that external
     * thread)
     */
    @Override
    public synchronized void tcpDirectFinished() {
        setTcpDirectMode(false);
        if (effectBeforeTcpDirect == null || effectBeforeTcpDirect instanceof TcpControlEffect) {
            try {
                changeEffect(new WarmWhite(tc, 100, 2, logger));
            } catch (InvalidTransitionTimeException e){
                logger.writeError(this,e);
            }

        } else {
            changeEffect(effectBeforeTcpDirect);
        }
    }

    /**
     * Synchronised setter of tcpDirectMode boolean (must be synchronised as TCP direct mode is set
     * on the UDP listener thread but read on the normal effects manager thread)
     * @param tcpDirectMode
     */
    private synchronized void setTcpDirectMode(boolean tcpDirectMode){
        this.tcpDirectMode = tcpDirectMode;
        if (!tcpDirectMode)
            logger.writeMessage(this,"Returning to standard operation");
        else
            logger.writeMessage(this,"Switching to TCP direct mode");
    }

    private synchronized boolean isTcpDirectMode(){
        return tcpDirectMode;
    }

    /**
     * Begins new thread which, while a TCP direct connection is not currently in use,
     * listens for incoming UDP packets on TCP_DIRECT_NOTIFY port
     *
     * If a UDP packet is received and its text content matches the UID of this device
     * then switch to TCP direct mode
     */
    private void listenForTcpDirectStart(){
        new Thread( () -> {
            while (true) {
                try {
                    while (true) {
                        //only listen if we're not in TCP direct as we can only accept one controller
                        if (!isTcpDirectMode()) {
                            DatagramSocket socket = new DatagramSocket(UDP_DIRECT_REQUEST_PORT);
                            logger.writeMessage(this,"Setup TCP direct listener...");
                            byte[] buf = new byte[256];
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            socket.receive(packet);
                            socket.close();

                            String inputData = new String(packet.getData()).trim();
                            if (inputData.equals(deviceUID.getUid())){
                                logger.writeMessage(this, "UDP payload matched device UID");
                                //then begin TCP direct with this IP
                                switchToTcpDirect(packet.getAddress());
                            } else {
                                logger.writeError(this, "UDP payload did not match device UID");

                            }
                        }
                    }
                } catch (Exception e){
                    logger.writeError(this,e);
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e1) { }
                }
            }
        }).start();
    }

    /**
     * Switches to TCP direct mode and updates tcpDirectMode boolean
     * @param inetAddress
     */
    private synchronized void switchToTcpDirect(InetAddress inetAddress){
        try {
            sendAcknowledgePacket(inetAddress);
            setTcpDirectMode(true);
            effectBeforeTcpDirect = currentEffect;
            String ipAddress = inetAddress.getHostAddress();
            changeEffect(new TcpControlEffect(tc, this, ipAddress, logger));
        } catch (IOException e){
            logger.writeError(this,e);
        }
    }

    /**
     * Sends an acknowledgement packet to the controller requesting UDP control
     * This is how the controller will receive the IP address of the device
     * @param address IP Address of controller
     * @throws IOException Thrown if error sending ack packet
     */
    private void sendAcknowledgePacket(InetAddress address) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = new byte[1];
        buf[0] = UDP_CONNECTION_VERSION;
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, UDP_DIRECT_NOTIFY_PORT);
        socket.send(packet);
        socket.close();
    }

    /**
     * Process an incoming device update request...
     * @param deviceState
     */
    public synchronized void processDeviceUpdate (DeviceState deviceState){
        //We do not switch effects if we are in TCP direct mode
        if (!isTcpDirectMode()) {
            try {
                if (deviceState.isStandby()) {
                    if (currentEffect == null) {
                        changeEffect(new Standby(tc, alarmController, 2, alarmController.getAlarms(), logger));
                    } else if (!currentEffect.getClass().getSimpleName().equals(STANDBY)) {
                        changeEffect(new Standby(tc, alarmController, 2, alarmController.getAlarms(), logger));
                    }
                } else {


                    switch (deviceState.getType()) {
                        case STATIC:

                            try {
                                LedState ledState = new LedState(deviceState.getColour());
                                try {
                                    if (!checkCurrentEffectStatic(ledState, deviceState.getIntensity())) {
                                        changeEffect(new StaticEffect(tc, ledState, deviceState.getIntensity(), 2, logger));
                                    }
                                } catch (InvalidTransitionTimeException e) {
                                    logger.writeError(this, e);
                                }
                            } catch (LedState.InvalidRGBException e) {
                                logger.writeError(this, e);
                            }
                            break;
                        case BREATHING:
                            try {
                                LedState ledStateBreathing = new LedState(deviceState.getColour());
                                try {
                                    if (!checkCurrentEffectBreathing(ledStateBreathing, deviceState.getSpeed(), deviceState.getIntensity())) {
                                        changeEffect(new Breathing(tc, ledStateBreathing, deviceState.getSpeed(), deviceState.getIntensity(), 2, logger));
                                    }
                                } catch (TapeInUseException | InvalidTransitionTimeException e) {
                                    logger.writeError(this, e);
                                }
                            } catch (LedState.InvalidRGBException e) {
                                logger.writeError(this, e);
                            }
                            break;
                        case SPECTRUM_CYCLING:
                            try {
                                int spectrumSpeed = deviceState.getSpeed();
                                if (!checkCurrentEffectSpectrum(spectrumSpeed, deviceState.getIntensity()))
                                    changeEffect(new SpectrumCycling(tc, spectrumSpeed, deviceState.getIntensity(), 2, logger));
                            } catch (TapeInUseException | InvalidTransitionTimeException e) {
                                logger.writeError(this, e);
                            }
                            break;

                        case CUSTOM_EFFECT:
                            CustomEffect customEffect = deviceState.getCustomEffect();
                            try {
                                if (customEffect != null) {
                                    customEffect.setLogger(logger);
                                    customEffect.setTapeControl(tc);
                                    customEffect.setTransistion(1);
                                    customEffect.setSpeed(Integer.valueOf(deviceState.getSpeed()).longValue());
                                    customEffect.setIntensity(deviceState.getIntensity());
                                    changeEffect(customEffect);
                                }

                            } catch (InvalidTransitionTimeException e) {
                                logger.writeError(this, e);
                            }

                            break;

                        case STROBE_EFFECT:
                            try {
                                LedState ledStateStrobe = new LedState(deviceState.getColour());
                                int strobeSpeed = deviceState.getSpeed();
                                if (!checkCurrentEffectStrobe(ledStateStrobe, strobeSpeed, deviceState.getIntensity()))
                                    changeEffect(new Strobe(tc, ledStateStrobe, strobeSpeed, deviceState.getIntensity(), 2, logger));
                            } catch (TapeInUseException | LedState.InvalidRGBException | InvalidTransitionTimeException e) {
                                logger.writeError(this, e);
                            }
                            break;

                        case COOL_WHITE:
                            try {
                                if (!checkCurrentEffectCoolWhite(deviceState.getIntensity()))
                                    changeEffect(new CoolWhite(tc, deviceState.getIntensity(), 2, logger));
                            } catch (InvalidTransitionTimeException e) {
                                logger.writeError(this, e);
                            }
                            break;
                        case WARM_WHITE:
                            try {
                                if (!checkCurrentEffectWarmWhite(deviceState.getIntensity()))
                                    changeEffect(new WarmWhite(tc, deviceState.getIntensity(), 2, logger));
                            } catch (InvalidTransitionTimeException e) {
                                logger.writeError(this, e);
                            }
                            break;
                        default:
                            logger.writeError(this, "Invalid effect type...");
                            break;
                    }
                    logger.writeMessage(this, deviceState.getType());
                }
            } catch (ClassCastException e) {
                logger.writeError(this, e);
            }
        } else {
            //otherwise we're in TCP direct mode so do not attempt to set the effect...
            logger.writeMessage(this, "Blocked inbound effect request as in TCP direct mode");
        }
    }

}
