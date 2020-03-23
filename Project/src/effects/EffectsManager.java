package effects;

import common.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class EffectsManager implements TcpDirectFinishedListener{

    public static final String STATIC = "StaticEffect";
    public static final String BREATHING = "Breathing";
    public static final String STANDBY = "Standby";
    public static final String SPECTRUM_CYCLING = "SpectrumCycling";
    public static final String CUSTOM_EFFECT = "CustomEffect";
    public static final String STROBE_EFFECT = "Strobe";
    public static final String WARM_WHITE = "WarmWhite";
    public static final String COOL_WHITE = "CoolWhite";

    public static final int TCP_DIRECT_NOTIFY_PORT = 5557;
//test commit

    private ITapeControl tc;
    private IEffect currentEffect;
    private IAlarmController alarmController;
    private Logger logger;
    private boolean tcpDirectMode;
    private DeviceUID deviceUID;

    private IEffect effectBeforeTcpDirect;

    public EffectsManager (ITapeControl tc, IAlarmController alarmController, Logger logger){
        this.tc = tc;
        this.alarmController = alarmController;
        this.logger = logger;
        setTcpDirectMode(false);
        listenForTcpDirectStart();
    }

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

    @Override
    public synchronized void tcpDirectFinished() {
        setTcpDirectMode(false);
        if (effectBeforeTcpDirect != null) {
            changeEffect(effectBeforeTcpDirect);
        } else {
            try {
                changeEffect(new WarmWhite(tc, 100, 2, logger));
            } catch (InvalidTransitionTimeException e){
                logger.writeError(this,e);
            }
        }
    }

    private synchronized void setTcpDirectMode(boolean tcpDirectMode){
        logger.writeMessage(this,"Returning to standard operation");
        this.tcpDirectMode = tcpDirectMode;
    }

    private synchronized boolean isTcpDirectMode(){
        return tcpDirectMode;
    }

    private void listenForTcpDirectStart(){
        new Thread( () -> {
            while (true) {
                try {
                    DatagramSocket socket = new DatagramSocket(TCP_DIRECT_NOTIFY_PORT);
                    while (true) {
                        //only listen if we're not in TCP direct as we can only accept one controller
                        if (!isTcpDirectMode()) {
                            byte[] buf = new byte[256];
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            socket.receive(packet);

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
        });
    }

    private synchronized void switchToTcpDirect(InetAddress inetAddress){
        logger.writeMessage(this,"Switching to TCP direct mode");
        effectBeforeTcpDirect = currentEffect;
        String ipAddress = inetAddress.getHostAddress();
        changeEffect(new TcpControlEffect(tc,ipAddress,logger));
    }

    /**
     * Process an incoming device update request...
     * @param deviceState
     */
    public synchronized void processDeviceUpdate (DeviceState deviceState){
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
        }
    }

}
