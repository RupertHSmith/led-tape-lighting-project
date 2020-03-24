package common;

import effects.IEffect;

/**
 * Interface of any RGB Tape device
 */
public interface ITapeControl {
    /**
     * Changes to a new colour with no fade
     * @param s The LedState (colour) to change to
     * @param controller The IEffect instance requesting this change
     * @throws TapeInUseException Thrown if the tape is being used by another effect
     */
    void snapTo(LedState s, IEffect controller) throws TapeInUseException;

    /**
     * Changes to a new colour with a fade of variable length
     * @param s The LedState (colour) to change to
     * @param duration The length of the fade in seconds
     * @param controller The IEffect instance requesting this change
     * @throws TapeInUseException Thrown if the tape is being used by another effect
     */
    void fadeTo(LedState s, float duration, IEffect controller) throws TapeInUseException;

    /**
     * Switches to black (i.e. off) with a fade of variable length
     * @param duration The length of the fade in seconds
     * @param controller The IEffect instance requesting this change
     * @throws TapeInUseException Thrown if the tape is being used by another effect
     */
    void fadeToBlack(float duration, IEffect controller) throws TapeInUseException;

    /**
     * Changes to a new colour with a fade of fixed rate
     * The length of the fade will depend on relative difference of the
     * current colour, to the new colour
     * @param s The LedState (colour) to change to
     * @param controller The IEffect instance requesting this change
     * @throws TapeInUseException Thrown if the tape is being used by another effect
     */
    void smartFade(LedState s, IEffect controller) throws TapeInUseException;

    /**
     * Fades to black at a fixed rate
     * The length of the fade will depend on relative difference of the
     * current colour to the off state
     * @param controller The IEffect instance requesting this change
     * @throws TapeInUseException Thrown if the tape is being used by another effect
     */
    void smartFadeToBlack(IEffect controller) throws TapeInUseException;

    /**
     * Requests control of the tape so that if granted, no other effect
     * can affect the tape while this effect is in control
     * @param controller The IEffect instance requesting control
     * @throws TapeInUseException Thrown if the tape is in use by another effect and control therefore cannot be granted
     */
    void setController(IEffect controller) throws TapeInUseException;

    /**
     * halt will pause any current running transition - halt will run on a
     * different thread to where the transition is occurring so will block
     * This also releases control of the LED tape
     * @return Returns the halted led state
     */
    LedState halt();

    /**
     * Returns the current colour of the tape
     * @return The current LED state (contains RGB values)
     */
    LedState getColour();
}
