package common;

import effects.IEffect;

public interface ITapeControl {
    void snapTo(LedState s, IEffect controller) throws TapeInUseException;
    void fadeTo(LedState s, float duration, IEffect controller) throws TapeInUseException;
    void fadeThruBlack(LedState s, float duration) throws TapeInUseException;
    void fadeToBlack(float duration, IEffect controller) throws TapeInUseException;
    void setController(IEffect controller) throws TapeInUseException;

    /**
     * halt will pause any current running transition - halt will run on a
     * different thread to where the transition is occurring so will block
     * This also releases control of the LED tape
     * @return Returns the halted led state
     */
    LedState halt();
    LedState getColour();
}
