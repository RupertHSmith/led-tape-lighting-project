package com.rgbtape.app;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public class HomeFragment extends Fragment implements CustomEffectListener, DeviceStateListener {
    public static boolean updateListenersBlocked = false;

    private HomeFragmentConnection homeFragmentConnection;

    private boolean deviceStateReceived = false;

    private ArrayList<EffectItem> effectsList;
    private EffectsAdapter mAdapter;


    private boolean on = true;
    private Colour colour;

    private EffectItem currentEffect;




    /**
     * Set instance of home fragment connection so can request info from db.
     * @param homeFragmentConnection The instance
     */
    public HomeFragment(HomeFragmentConnection homeFragmentConnection){
        this.homeFragmentConnection = homeFragmentConnection;
        homeFragmentConnection.setCustomEffectsListener(this);
        homeFragmentConnection.addDeviceStateListener(this);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //init controls
        colour = new Colour(0,0,0);
        initList();
        initColourButton(view);
        initOnOff(view);
        initSliders(view);

        Spinner effectsSpinner = view.findViewById(R.id.spinner);
        mAdapter = new EffectsAdapter(getContext(), effectsList);
        effectsSpinner.setAdapter(mAdapter);
        effectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (deviceStateReceived) {
                    EffectItem clickedItem = (EffectItem) parent.getItemAtPosition(position);
                    String clickedName = clickedItem.getEffectname();

                    currentEffect = clickedItem;

                    homeFragmentConnection.setEffect(clickedItem);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        homeFragmentConnection.requestDeviceState(this);
        return view;
    }

    private void initOnOff(View view){
        final ImageButton onOff = view.findViewById(R.id.on_off_button);
        onOff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (deviceStateReceived) {
                    if (on) {
                        on = false;
                        onOff.setImageResource(R.drawable.off);

                        homeFragmentConnection.setStandby(true);
                    } else {
                        on = true;
                        onOff.setImageResource(R.drawable.on);

                        homeFragmentConnection.setStandby(false);
                    }
                }
            }
        });
    }

    private void initColourButton(View view){
          Button colourButton = view.findViewById(R.id.alarm_colour_button);
        colourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deviceStateReceived) {
                    showCp();
                }
            }
        });
    }

    private void initSliders(View view){
        SeekBar seekBar = view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (deviceStateReceived) {
                    homeFragmentConnection.setSpeed(seekBar.getProgress());
                }
            }
        });

        //Init intensity seek bar
        SeekBar intensitySeekBar = view.findViewById(R.id.intensity_seekbar);
        intensitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(deviceStateReceived){
                    homeFragmentConnection.setIntensity(seekBar.getProgress());
                }
            }
        });

    }

    private void showCp(){
        final ColorPicker cp = new ColorPicker(getActivity(), colour.getRed(), colour.getGreen(),colour.getBlue());
        cp.show();
        Button okButton = cp.findViewById(R.id.okColorButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* You can get single channel (value 0-255) */
                colour = new Colour(cp.getRed(), cp.getGreen(), cp.getBlue());

                /* Or the android RGB Color (see the android Color class reference) */
                setButtonColour(cp.getColor());

                homeFragmentConnection.setColour(cp.getRed(), cp.getGreen(), cp.getBlue());

                cp.dismiss();
            }
        });
    }

    private void setButtonColour(int colour){
        View view = getView();
        if (view != null) {
            Button colourButton = view.findViewById(R.id.alarm_colour_button);
            colourButton.setBackgroundColor(colour);
        }
    }

    private void initList(){
        effectsList = new ArrayList<>(EffectItem.getDefaultEffects());

        //This is a request to the handler to listen for db updates
        homeFragmentConnection.addEffectListener("pilton");
    }

    /**
     * This method will be called by the database handler once the custom effects list
     * is received.
     * @param effects The effects received from the db (all custom effects)
     */
    @Override
    public void onEffectReceived(List<EffectItem> effects) {
        EffectItem.addCustomEffectsToList(effects, effectsList);

        //If the currently selected custom effect has been removed by this update then
        //change to static

        if (!effectsList.contains(currentEffect)){
            if (getView() != null) {
                Spinner effectsSpinner = getView().findViewById(R.id.spinner);
                effectsSpinner.setSelection(0, true);
                currentEffect = effectsList.get(0);
            }
        }
    }

    @Override
    public void onDeviceStateReceived(boolean standby, int speed, int intensity, String type, String customId, int r, int g, int b) {
        if (getView() != null) {
            //Set colour picker
            Button colourButton = getView().findViewById(R.id.alarm_colour_button);
            colourButton.setBackgroundColor(android.graphics.Color.rgb(r,g,b));
            colour = new Colour(r, g, b);

            //Set on off button
            ImageButton onOff = getView().findViewById(R.id.on_off_button);
            if (standby){
                on = false;
                onOff.setImageResource(R.drawable.off);
            } else {
                on = true;
                onOff.setImageResource(R.drawable.on);
            }

            //Set speed
            SeekBar seekBar = getView().findViewById(R.id.seekBar);
            seekBar.setProgress(speed, true);

            SeekBar intensitySeekBar = getView().findViewById(R.id.intensity_seekbar);
            intensitySeekBar.setProgress(intensity, true);

            //Set selected effect
            if (type.equals(EffectItem.CUSTOM_EFFECT)) {

                if (effectsList.contains(new EffectItem("", type, customId))) {

                    Spinner effectsSpinner = getView().findViewById(R.id.spinner);
                    effectsSpinner.setSelection(effectsList.indexOf(new EffectItem("", type, customId)), true);
                }
            } else {
                if (effectsList.contains(new EffectItem("", type, null))) {

                    Spinner effectsSpinner = getView().findViewById(R.id.spinner);
                    effectsSpinner.setSelection(effectsList.indexOf(new EffectItem("", type, null)), true);
                }
            }

            //unlock listeners
            deviceStateReceived = true;
        }
    }
}
