package com.rgbtape.app;

import java.util.List;

public interface HomeFragmentConnection {

    /**
     * Retrieves custom effects for that user
     * @return
     */
    void addEffectListener(String username);
    void setCustomEffectsListener(CustomEffectListener customEffectsListener);
    void requestDeviceState(DeviceStateListener deviceStateListener);

    void setStandby(boolean standby);

    //TODO: MAYBE ADD VALIDATION
    void setColour(int r, int g, int b);
    void setSpeed(int speed);
    void setIntensity(int intensity);
    void setEffect(EffectItem effect);

}
