package effects;

//TODO: This effect must be reapplied whenever a change is used by updating the document reference in the device

import common.ITapeControl;
import common.LedState;
import common.TapeInUseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CustomEffect implements IEffect, Runnable{

    private String name;
    private String owner;
    private Long speed;
    private TreeMap<String, ArrayList<Long>> stops;
    private int intensity;



    public HashMap<String, ArrayList<Long>> getStops() {
        return new HashMap<>(stops);
    }

    public void setStops(HashMap<String, ArrayList<Long>> stops) {
        this.stops = new TreeMap<>(stops);
    }

    //Tape control
    private ITapeControl tapeControl;
    private int transistion = 0;
    private boolean terminated;

    private float timePerStopUnit;


    ///REQUIRED FOR OBJECT CASTING FIRESTORE///
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Long getSpeed() {
        return speed;
    }

    public void setSpeed(Long speed) {
        this.speed = speed;
    }

    public void setIntensity(int intensity){
        this.intensity = intensity;

        TreeMap<String, ArrayList<Long>> newStops = new TreeMap<>();

        for (String entry : stops.keySet()){
            ArrayList<Long> newColour = LedState.applyIntensity(stops.get(entry), intensity);
            newStops.put(entry, newColour);
        }

        stops = newStops;
    }

    public CustomEffect(){}


    public void setTapeControl(ITapeControl tapeControl){
        this.tapeControl = tapeControl;
    }

    public void setTransistion(int transition) throws InvalidTransitionTimeException {
        if (transition < 0 | transition > 10)
            throw new InvalidTransitionTimeException();

        this.transistion = transition;
    }

    private synchronized void setTerminated(boolean terminated){
        this.terminated = terminated;
    }

    private synchronized boolean isTerminated(){
        return this.terminated;
    }

    /**
     * Finds smallest difference between successive stops
     * @return the smallest difference
     */
     private int findSmallestDifference(){
        int prev = -1;

        Long lastStop = new Long(stops.lastKey());

        int smallestDifference = Integer.MAX_VALUE;

        for (String stop : stops.keySet()){
            if (prev > -1){
                int difference = new Long(stop).intValue() - prev;
                //If this new found difference is smaller change the current smallest
                if (difference < smallestDifference)
                    smallestDifference = difference;

                //If this is the last stop also check distance between last and first
                if (stop.equals(lastStop)){
                    difference = (100 - lastStop.intValue()) + new Long(stops.firstKey()).intValue();
                    if (difference < smallestDifference)
                        smallestDifference = difference;
                }
            } else {
                prev = new Long(stop).intValue();
            }
        }

         System.out.println("SMALLEST DIFFERENCE: " + smallestDifference);

        return smallestDifference;
    }

    private void calculateTimePerStopUnit(){
        timePerStopUnit = (0.1f + calculateSpeedMultiplier() * 29.9f) / (float) findSmallestDifference();
        System.out.println("TIME PER STOP UNIT: " + timePerStopUnit);
    }

    private float calculateSpeedMultiplier(){
        return (float) (100 - speed) / 100f;
    }


    private boolean test(){
        if (stops == null) {
            System.err.println("Stops invalid.");
            return false;
        }

        for (String stop : stops.keySet()) {
            if (new Long(stop).compareTo(0L) < 0 | new Long(stop).compareTo(100L) >= 0) {
                System.err.println("Stop '" + stop + "' out of range.");
                return false;
            }
            try {
                new LedState(stops.get(stop));
            } catch (LedState.InvalidRGBException e){
                e.printStackTrace();
                return false;
            }
        }

        if (tapeControl == null)
            return false;

        return true;
    }

    @Override
    public void start() throws TapeInUseException {
        if(test()){
            tapeControl.setController(this);
            calculateTimePerStopUnit();

            new Thread(this).start();
        }
    }

    @Override
    public LedState terminate() {
        setTerminated(true);

        return tapeControl.halt();
    }

    private float calculateTransistionTime(Long previousStop, Long newStop){
        if (newStop > previousStop){
            return (newStop - previousStop) * timePerStopUnit;
        } else {
            return ((100 - previousStop) + newStop) * timePerStopUnit;
        }

    }

    @Override
    public void run() {
        try {
            boolean firstLoop = true;

            //First fade tape to starting colour
            tapeControl.smartFade(getStartingState(), this);


            Long previousStop = 0L;
            //While the effect is running...
            while(!isTerminated()){
                for (Map.Entry<String, ArrayList<Long>> entry : stops.entrySet()){
                    //Check not terminated
                    if (!isTerminated()) {
                        //if this is not the first loop and the stop we are looking at is not 0
                        if (!(firstLoop && new Long(entry.getKey()).equals(0L))) {

                            //Calc transition time
                            float transitionTime = calculateTransistionTime(previousStop, new Long(entry.getKey()));

                            //Transition to next stop...
                            LedState state = new LedState(entry.getValue());
                            tapeControl.fadeTo(state, transitionTime, this);

                        }
                        previousStop = new Long(entry.getKey());
                        firstLoop = false;
                    }
                }
            }


        } catch (TapeInUseException | LedState.InvalidRGBException e){
            e.printStackTrace();
            System.err.println("Effect terminating.");
            terminate();
        }
    }

    private LedState getStartingState() throws LedState.InvalidRGBException {
        if (stops.containsKey("0")){
            return new LedState(stops.get("0"));
        }

        if (stops.keySet().size() == 1)
            return new LedState(stops.values().iterator().next());

        //otherwise need to work out the proportion through the transition from the last stop to the first stop
        Long firstStop = new Long(stops.firstKey());
        Long lastStop = new Long(stops.lastKey());

        //Calc difference between them
        Long difference = (100 - lastStop) + firstStop;

        //Calculate proportion occurred as decimal
        double proportionOccurred = (100.0 - lastStop.doubleValue()) / difference.doubleValue();

        //Get RGB differences
        Long rDifference = stops.firstEntry().getValue().get(0) - stops.lastEntry().getValue().get(0);
        Long gDifference = stops.firstEntry().getValue().get(1) - stops.lastEntry().getValue().get(1);
        Long bDifference = stops.firstEntry().getValue().get(2) - stops.lastEntry().getValue().get(2);

        //Calculate new RGB state therefore
        Long r = Math.round(proportionOccurred * rDifference.doubleValue());
        Long g = Math.round(proportionOccurred * gDifference.doubleValue());
        Long b = Math.round(proportionOccurred * bDifference.doubleValue());

        return new LedState(r,g,b);
    }

    class InvalidCustomEffectException extends Exception{
        public InvalidCustomEffectException(String effectName, String error){
            super("Error with custom effect: \"" + "\", Issue: " + error + ".");
        }
    }
}
