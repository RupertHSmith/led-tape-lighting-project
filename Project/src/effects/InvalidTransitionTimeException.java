package effects;

public class InvalidTransitionTimeException extends Exception{
    public InvalidTransitionTimeException(){
        super("Transition time must be greater than or equal to 0, and less than or equal to 10.");
    }
}