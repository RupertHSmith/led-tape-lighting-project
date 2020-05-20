package com.rgbtape.app;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class ConnectionHandler implements HomeFragmentConnection, AlarmFragmentConnection {
    private static final String DEVICE_UID = "VLUaKArgEQ2oENfZs9WE";

    private FirebaseFirestore db;
    private CustomEffectListener customEffectListener;

    private ScheduledExecutorService lockouts;
    private Future<?> databaseLockoutFuture;


    public ConnectionHandler(){
        db = FirebaseFirestore.getInstance();
        lockouts = Executors.newSingleThreadScheduledExecutor();

    }

    private void lockoutListeners(){
        HomeFragment.updateListenersBlocked = true;
        if (databaseLockoutFuture != null){
            if (!databaseLockoutFuture.isCancelled())
                databaseLockoutFuture.cancel(true);
        }
        databaseLockoutFuture = lockouts.schedule(new Runnable() {
            @Override
            public void run() {
                HomeFragment.updateListenersBlocked = false;
            }
        }, 1, TimeUnit.SECONDS);

    }

    @Override
    public void setCustomEffectsListener(CustomEffectListener customEffectsListener) {
        this.customEffectListener = customEffectsListener;
    }

    @Override
    public void addEffectListener(String username) {
       /*  db.collection("customeffects")
                .whereEqualTo("owner", username)
                .get()
                .addOnCompleteListener(new EffectRequestListener(customEffectListener));*/

       db.collection("customeffects")
               .whereEqualTo("owner", username)
               .addSnapshotListener(new EffectRequestListener(customEffectListener));
    }

    class EffectRequestListener implements EventListener<QuerySnapshot> {
        public CustomEffectListener customEffectListener;
        public EffectRequestListener(CustomEffectListener customEffectListener){
            this.customEffectListener = customEffectListener;
        }

        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            if (e != null){
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (queryDocumentSnapshots != null) {
                List<EffectItem> customEffects = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Object nameObj = document.get("name");
                    String id = document.getId();
                    if (nameObj instanceof String) {
                        customEffects.add(new EffectItem((String) nameObj, EffectItem.CUSTOM_EFFECT, id));
                    }
                }
                customEffectListener.onEffectReceived(customEffects);
            }


        }
    }

    @Override
    public void requestDeviceState(DeviceStateListener deviceStateListener) {
        DocumentReference documentReference = db.collection("devices").document(DEVICE_UID);
        documentReference.get().addOnCompleteListener(new CallBackDeviceStateListener(deviceStateListener));
    }

    class CallBackDeviceStateListener implements OnCompleteListener<DocumentSnapshot> {
        private DeviceStateListener listener;
        public CallBackDeviceStateListener(DeviceStateListener listener){
            this.listener = listener;
        }
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if (document.exists()){
                    Boolean standby = (Boolean) document.get("standby");
                    Long speed = (Long) document.get("speed");
                    Long intensity = (Long) document.get("intensity");
                    String type = (String) document.get("type");

                    ArrayList<Long> colour = (ArrayList<Long>) document.get("colour");
                    String customEffectId = ((DocumentReference) document.get("customEffect")).getId();

                    listener.onDeviceStateReceived(standby, speed.intValue(), intensity.intValue(), type, customEffectId, colour.get(0).intValue(), colour.get(1).intValue(), colour.get(2).intValue());
                }
            }
        }
    }

    @Override
    public void addDeviceStateListener(DeviceStateListener deviceStateListener){
        db.collection("devices")
                .addSnapshotListener(new DeviceStateUpdateListener(deviceStateListener));
    }

    class DeviceStateUpdateListener implements EventListener<QuerySnapshot> {
        private DeviceStateListener listener;
        public DeviceStateUpdateListener(DeviceStateListener listener){
            this.listener = listener;
        }

        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty() && !HomeFragment.updateListenersBlocked){

                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()){
                    if (document.exists() && document.getId().equals("VLUaKArgEQ2oENfZs9WE")){
                        Boolean standby = (Boolean) document.get("standby");
                        Long speed = (Long) document.get("speed");
                        Long intensity = (Long) document.get("intensity");
                        String type = (String) document.get("type");

                        ArrayList<Long> colour = (ArrayList<Long>) document.get("colour");
                        String customEffectId = ((DocumentReference) document.get("customEffect")).getId();

                        listener.onDeviceStateReceived(standby, speed.intValue(), intensity.intValue(), type, customEffectId, colour.get(0).intValue(), colour.get(1).intValue(), colour.get(2).intValue());
                    }
                }

            }
        }
    }


    /**
     * Switches the tape off
     */
    @Override
    public void setStandby(boolean standby) {
        lockoutListeners();
        DocumentReference device = db.collection("devices").document(DEVICE_UID);
        if (standby) {
            device.update("standby", true);
        } else {
            device.update("standby", false);
        }
    }

    @Override
    public void setColour(int r, int g, int b) {
        lockoutListeners();
        DocumentReference device = db.collection("devices").document(DEVICE_UID);
        ArrayList colour = new ArrayList<>();
        colour.add(r);
        colour.add(g);
        colour.add(b);
        device.update("colour", colour);
    }

    @Override
    public void setIntensity(int intensity) {
        lockoutListeners();
        DocumentReference device = db.collection("devices").document(DEVICE_UID);
        device.update("intensity", intensity);
    }

    @Override
    public void setSpeed(int speed) {
        lockoutListeners();
        DocumentReference device = db.collection("devices").document(DEVICE_UID);
        device.update("speed", speed);
    }

    @Override
    public void setEffect(EffectItem effect) {
        lockoutListeners();
        DocumentReference device = db.collection("devices").document(DEVICE_UID);
        device.update("type", effect.getType());

        if (effect.getType().equals(EffectItem.CUSTOM_EFFECT) && effect.getCustomEffectID() != null){
            DocumentReference customEffect = db.collection("customeffects").document(effect.getCustomEffectID());
            device.update("customEffect", customEffect);
        }
    }

    @Override
    public void setAlarmListener(String username, AlarmsListener alarmListener) {
        lockoutListeners();
        db.collection("users").document(username).collection("alarms")
                .addSnapshotListener(new AlarmCallBackListener(alarmListener));
    }

    @Override
    public void updateAlarm(Alarm alarm) {
        if (alarm.getId() != null) {
            DocumentReference alarmRef = db.collection("users")
                    .document("pilton")
                    .collection("alarms")
                    .document(alarm.getId());

            alarmRef.update("enabled", alarm.isEnabled());
            alarmRef.update("finishHour", alarm.getFinishHour());
            alarmRef.update("finishMinute", alarm.getFinishMinute());
            alarmRef.update("startHour", alarm.getStartHour());
            alarmRef.update("startMinute", alarm.getStartMinute());
            alarmRef.update("colour", alarm.getColour());
        }
    }

    @Override
    public void createAlarm(Alarm alarm) {
        db.collection("users").document("pilton").collection("alarms")
                .add(alarm);
    }

    @Override
    public void removeAlarm(Alarm alarm) {
        if (alarm != null){
            if (alarm.getId() != null){

                db.collection("users")
                        .document("pilton")
                        .collection("alarms")
                        .document(alarm.getId())
                        .delete();


            } else {
                Log.e(TAG, "removeAlarm: Alarm id was null.");
            }
        } else {
            Log.e(TAG, "removeAlarm: Alarm was null.");
        }
    }

    class AlarmCallBackListener implements EventListener<QuerySnapshot>{
        private AlarmsListener alarmsListener;

        public AlarmCallBackListener(AlarmsListener alarmsListener){
            this.alarmsListener = alarmsListener;
        }

        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            if (alarmsListener != null && queryDocumentSnapshots != null){
                if (e != null){
                    Log.e(TAG, "onEvent: Listen failed", e);
                    return;
                }

                List<Alarm> alarms = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots){
                    Alarm a = doc.toObject(Alarm.class);
                    a.setId(doc.getId());
                    alarms.add(a);
                }

                //send list
                alarmsListener.onAlarmUpdateRecieved(alarms);
            }
        }
    }


/*
    class EffectRequestListener implements OnCompleteListener<QuerySnapshot>{
        CustomEffectListener customEffectListener;
        public EffectRequestListener(CustomEffectListener customEffectListener) {
            this.customEffectListener = customEffectListener;
        }

        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            List<String> customEffects = new ArrayList<>();
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Object nameObj = document.get("name");
                    if (nameObj instanceof String) {
                        customEffects.add((String) nameObj);
                    }
                }
                customEffectListener.onEffectReceived(customEffects);
            } else {
                Log.e("Error: ", "Failed to retrieve custom effects.");
            }
        }
    }*/


}















    /*
    private void testWrite(){
        Map<String, String> user = new HashMap<>();
        user.put("name","Scruffy");
        user.put("password", "password");

        db.collection("users").document("scruffy")
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Test Write: ", "Success");
                    }
                });
    }*/


