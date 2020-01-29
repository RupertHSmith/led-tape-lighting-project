package com.rgbtape.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import static android.content.ContentValues.TAG;

public class AlarmDialog extends Dialog implements View.OnClickListener {
    final static String TITLE_CREATE = "Create Alarm";
    final static String TITLE_EDIT = "Edit Alarm";
    boolean creating;

    Activity activity;

    Alarm alarm;
    private AlarmFragmentConnection alarmFragmentConnection;

    public AlarmDialog(@NonNull Context context, Activity activity, AlarmFragmentConnection alarmFragmentConnection) {
        super(context);
        creating = true;

        alarm = new Alarm();
        alarm.setColour(255,255,0);
        this.activity = activity;
        this.alarmFragmentConnection = alarmFragmentConnection;
    }

    public AlarmDialog(@NonNull Context context, Alarm alarm, Activity activity, AlarmFragmentConnection alarmFragmentConnection) {
        super(context);
        creating = false;
        this.alarm = alarm;
        this.activity = activity;
        this.alarmFragmentConnection = alarmFragmentConnection;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_dialog);

        TextView titleBar = findViewById(R.id.alarm_title);
        if (creating){
            titleBar.setText(TITLE_CREATE);
        } else {
            titleBar.setText(TITLE_EDIT);
        }
        initDisplays();
    }

    private void initDisplays(){
        initCancel();
        initTimePicker();
        initSeekBar();
        initColourButton();
        initOkay();
    }

    private void initOkay(){
        //Update alarm in db
        Button okayButton = findViewById(R.id.alarm_okay_button);
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set finish time
                alarm.setFinishHour(getTimePickerHours());
                alarm.setFinishMinute(getTimePickerMinute());

                //Set start time
                SeekBar seekBar = findViewById(R.id.duration_seek_bar);
                alarm.setStartTime(getProgressDuration(seekBar.getProgress()));
                alarm.setEnabled(true);

                if (!creating){
                    alarmFragmentConnection.removeAlarm(alarm);
                    alarmFragmentConnection.createAlarm(alarm);
                } else {
                    alarmFragmentConnection.createAlarm(alarm);
                }

                //finally close dialog
                dismiss();
            }
        });
    }

    private void initCancel(){
        Button cancelButton = findViewById(R.id.alarm_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void initColourButton(){
        Button colourButton = findViewById(R.id.alarm_colour_button);
        //set colour of button
        if (alarm.getColour() != null) {
            colourButton.setBackgroundColor(android.graphics.Color.rgb(alarm.getColour().get(0).intValue(), alarm.getColour().get(1).intValue(), alarm.getColour().get(2).intValue()));
        }
        colourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alarm.getColour() != null) {
                    showCp();
                }
            }
        });
    }

    private void showCp(){
        final ColorPicker cp = new ColorPicker(activity,
                alarm.getColour().get(0).intValue(), alarm.getColour().get(1).intValue(),
                alarm.getColour().get(2).intValue());


        cp.show();
        Button okButton = cp.findViewById(R.id.okColorButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Or the android RGB Color (see the android Color class reference) */
                setButtonColour(cp.getColor());
                alarm.setColour(cp.getRed(), cp.getGreen(), cp.getBlue());

                cp.dismiss();
            }
        });
    }

    private void setButtonColour(int colour){
        Button colourButton = findViewById(R.id.alarm_colour_button);
        colourButton.setBackgroundColor(colour);
    }

    private int getTimePickerHours(){
        TimePicker timePicker = findViewById(R.id.time_picker);
        return timePicker.getHour();
    }

    private int getTimePickerMinute(){
        TimePicker timePicker = findViewById(R.id.time_picker);
        return timePicker.getMinute();
    }

    private void initTimePicker(){
        //set time picker
        TimePicker timePicker = findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);

        if (!creating) {
            timePicker.setHour(alarm.getFinishHour());
            timePicker.setMinute(alarm.getFinishMinute());
        }

    }

    private void initSeekBar(){
        //set duration
        SeekBar seekBar = findViewById(R.id.duration_seek_bar);
        final TextView durationText = findViewById(R.id.duration_text_view);
        if (!creating) {
            switch (alarm.getDuration()) {
                case 1:
                    seekBar.setProgress(0);
                    break;
                case 5:
                    seekBar.setProgress(1);
                    break;
                case 10:
                    seekBar.setProgress(2);
                    break;
                case 15:
                    seekBar.setProgress(3);
                    break;
                case 30:
                    seekBar.setProgress(4);
                    break;
                default:
                    seekBar.setProgress(0);
                    break;
            }
            String duration = Integer.valueOf(alarm.getDuration()).toString();
            durationText.setText(duration);
        }

        //set listner
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                durationText.setText(getProgressDurationString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private int getProgressDuration(int progress){
        switch (progress){
            case 0:
                return 1;
            case 1:
                return 5;
            case 2:
                return 10;
            case 3 :
                return 15;
            case 4:
                return 30;
            default:
                return 1;
        }
    }

    private String getProgressDurationString(int progress){
        return Integer.valueOf(getProgressDuration(progress)).toString();
    }





    @Override
    public void onClick(View v) {

    }
}
