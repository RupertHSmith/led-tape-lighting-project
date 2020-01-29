package common;

import effects.IEffect;

public class TapeInUseException extends Exception{
    public TapeInUseException (IEffect effect){
        super ("Tape in use by effect: " + effect.getClass().getSimpleName());
    }

    public TapeInUseException (IEffect requester, IEffect currentController){
       super(requester.getClass().getSimpleName() + ": request of control not granted as tape is in use by effect " + currentController);
    }
}
