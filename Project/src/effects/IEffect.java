package effects;

import common.*;

public interface IEffect {
    public void start() throws TapeInUseException;
    public LedState terminate();
}
