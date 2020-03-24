package effects;

import common.*;

/**
 * Interface implemented by any effect that can run on the device
 * providing compulsory methods
 */
public interface IEffect {
    /**
     * Called to set the effect running,
     * @throws TapeInUseException Thrown if the tape is currently being used by another effect and therefore cannot begin
     */
    void start() throws TapeInUseException;

    /**
     * Called to stop the effect running and release control of the Tape
     * @return Returns the state of the tape after termination (i.e. most effects will halt as they were)
     */
    LedState terminate();
}
