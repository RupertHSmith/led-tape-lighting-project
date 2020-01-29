package com.rgbtape.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AlarmListAdapter extends ArrayAdapter<Alarm> {
    Context context;
    List<Alarm> alarms;
    AlarmFragmentConnection alarmFragmentConnection;
    private final static Object o = new Object();

    public AlarmListAdapter(@NonNull Context context, List<Alarm> alarms, AlarmFragmentConnection alarmFragmentConnection) {
        super(context, R.layout.list_view_row, R.id.alarm_text_view, alarms);
        this.context = context;
        this.alarms = alarms;
        this.alarmFragmentConnection = alarmFragmentConnection;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_view_row, parent, false);
        }

        TextView alarmText = convertView.findViewById(R.id.alarm_text_view);
        final Switch alarmSwitch = convertView.findViewById(R.id.alarm_switch_button);

        final Alarm currentItem = getItem(position);
        if (currentItem != null){
            alarmText.setText(currentItem.toString());

            if (currentItem.isEnabled() && !alarmSwitch.isChecked())
                alarmSwitch.setChecked(true);
            else if (!currentItem.isEnabled() && alarmSwitch.isChecked())
                alarmSwitch.setChecked(false);


            //Set listener for this switch
            alarmSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!alarmSwitch.isChecked()){
                        currentItem.setEnabled(false);
                        alarmFragmentConnection.updateAlarm(currentItem);
                    } else {
                        currentItem.setEnabled(true);
                        alarmFragmentConnection.updateAlarm(currentItem);
                    }

                }
            });

        }
        return convertView;
    }


}
