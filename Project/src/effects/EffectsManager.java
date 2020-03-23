package effects;

import common.*;

public class EffectsManager {

    public static final String STATIC = "StaticEffect";
    public static final String BREATHING = "Breathing";
    public static final String STANDBY = "Standby";
    public static final String SPECTRUM_CYCLING = "SpectrumCycling";
    public static final String CUSTOM_EFFECT = "CustomEffect";
    public static final String STROBE_EFFECT = "Strobe";
    public static final String WARM_WHITE = "WarmWhite";
    public static final String COOL_WHITE = "CoolWhite";
//test commit

    private ITapeControl tc;
    private IEffect currentEffect;
    private IAlarmController alarmController;
    private Logger logger;

    public EffectsManager (ITapeControl tc, IAlarmController alarmController, Logger logger){
        this.tc = tc;
        this.alarmController = alarmController;
        this.logger = logger;
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

    /**
     * Process an incoming device update request...
     * @param deviceState
     */

    //TODO change this
    public void processDeviceUpdate (DeviceState deviceState){

        try {
            if (deviceState.isStandby()) {
                if (currentEffect == null) {
                    changeEffect(new Standby(tc, alarmController, 2, alarmController.getAlarms(),logger));
                } else if (!currentEffect.getClass().getSimpleName().equals(STANDBY)) {
                    changeEffect(new Standby(tc, alarmController, 2, alarmController.getAlarms(),logger));
                }
            } else {


                switch (deviceState.getType()) {
                    case STATIC:

                        try {
                            LedState ledState = new LedState(deviceState.getColour());
                            try {
                                if (!checkCurrentEffectStatic(ledState, deviceState.getIntensity())) {
                                    changeEffect(new StaticEffect(tc, ledState, deviceState.getIntensity(), 2,logger));
                                }
                            } catch (InvalidTransitionTimeException e) {
                                logger.writeError(this,e);
                            }
                        } catch (LedState.InvalidRGBException e) {
                            logger.writeError(this,e);
                        }
                        break;
                    case BREATHING:
                        try {
                            LedState ledStateBreathing = new LedState(deviceState.getColour());
                            try {
                                if (!checkCurrentEffectBreathing(ledStateBreathing, deviceState.getSpeed(), deviceState.getIntensity())) {
                                    changeEffect(new Breathing(tc, ledStateBreathing, deviceState.getSpeed(), deviceState.getIntensity(), 2,logger));
                                }
                            } catch (TapeInUseException | InvalidTransitionTimeException e) {
                                logger.writeError(this,e);
                            }
                        } catch (LedState.InvalidRGBException e) {
                            logger.writeError(this,e);
                        }
                        break;
                    case SPECTRUM_CYCLING:
                        try {
                            int spectrumSpeed = deviceState.getSpeed();
                            if (!checkCurrentEffectSpectrum(spectrumSpeed, deviceState.getIntensity()))
                                changeEffect(new SpectrumCycling(tc, spectrumSpeed, deviceState.getIntensity(), 2,logger));
                        } catch (TapeInUseException | InvalidTransitionTimeException e) {
                            logger.writeError(this,e);
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
                            logger.writeError(this,e);
                        }

                        break;

                    case STROBE_EFFECT:
                        try {
                            LedState ledStateStrobe = new LedState(deviceState.getColour());
                            int strobeSpeed = deviceState.getSpeed();
                            if (!checkCurrentEffectStrobe(ledStateStrobe, strobeSpeed, deviceState.getIntensity()))
                                changeEffect(new Strobe(tc, ledStateStrobe, strobeSpeed, deviceState.getIntensity(), 2,logger));
                        } catch (TapeInUseException | LedState.InvalidRGBException | InvalidTransitionTimeException e) {
                            logger.writeError(this,e);
                        }
                        break;

                    case COOL_WHITE:
                        try {
                            if(!checkCurrentEffectCoolWhite(deviceState.getIntensity()))
                                changeEffect(new CoolWhite(tc, deviceState.getIntensity(),2,logger));
                        } catch ( InvalidTransitionTimeException e){
                            logger.writeError(this,e);
                        }
                        break;
                    case WARM_WHITE:
                        try {
                            if(!checkCurrentEffectWarmWhite(deviceState.getIntensity()))
                                changeEffect(new WarmWhite(tc, deviceState.getIntensity(),2,logger));
                        } catch ( InvalidTransitionTimeException e){
                            logger.writeError(this,e);
                        }
                        break;
                    default:
                        logger.writeError(this, "Invalid effect type...");
                        break;
                }
                logger.writeMessage(this,deviceState.getType());
            }



        } catch (ClassCastException e){
            logger.writeError(this,e);
        }
    }
}
