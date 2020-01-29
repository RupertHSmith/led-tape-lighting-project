package com.rgbtape.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

public class AlarmsFragment extends Fragment implements AlarmsListener {
    private List<Alarm> alarms;
    private View view;

    private AlarmListAdapter alarmListAdapter;
    private AlarmFragmentConnection alarmFragmentConnection;

    public AlarmsFragment(AlarmFragmentConnection alarmFragmentConnection){
        this.alarmFragmentConnection = alarmFragmentConnection;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_alarms, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        alarms = new ArrayList<>();

        Alarm alarm = new Alarm(7,45,8,0);
        Alarm alarm1 = new Alarm(8, 30, 8, 45);
        Alarm alarm2 = new Alarm(8, 30, 8, 45);
        Alarm alarm3 = new Alarm(8, 30, 8, 45);
        Alarm alarm4 = new Alarm(8, 30, 8, 45);
        Alarm alarm5 = new Alarm(8, 30, 8, 45);
        Alarm alarm6 = new Alarm(8, 30, 8, 45);


        alarms.add(alarm);
        alarms.add(alarm1);
        alarms.add(alarm2);
        alarms.add(alarm3);
        alarms.add(alarm4);
        alarms.add(alarm5);
        alarms.add(alarm6);

        //todo change hard coded username
        alarmFragmentConnection.setAlarmListener("pilton",this);

        initListView(view);
        initCreateButton();

    }

    private void initListView(View view){
        final ListView listView = view.findViewById(R.id.alarms_list);
        alarmListAdapter = new AlarmListAdapter(view.getContext(), alarms, alarmFragmentConnection);
        listView.setAdapter(alarmListAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemLongClick: OUTPUT");
                showConfirmDeletePrompt(alarmListAdapter.getItem(position));
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlarmDialog alarmDialog= new AlarmDialog(getContext(), alarmListAdapter.getItem(position), getActivity(), alarmFragmentConnection);
                alarmDialog.show();
            }
        });
    }

    private void initCreateButton(){
        ImageButton createButton = view.findViewById(R.id.alarm_create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmDialog alarmDialog= new AlarmDialog(getContext(), getActivity(), alarmFragmentConnection);
                alarmDialog.show();
            }
        });
    }

    private void showConfirmDeletePrompt(final Alarm alarmToDelete){
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Delete Alarm");
        builder.setMessage("Are you sure you wish to remove this alarm?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alarmFragmentConnection.removeAlarm(alarmToDelete);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    @Override
    public void onAlarmUpdateRecieved(List<Alarm> alarms) {
        this.alarms.retainAll(alarms);
        alarms.removeAll(this.alarms);
        this.alarms.addAll(alarms);

        Collections.sort(this.alarms);

        Log.d(TAG, "onAlarmUpdateRecieved: Alarm update received");
        Log.d(TAG, "onAlarmUpdateRecieved: Alarms Data: " + this.alarms.toString());

        //refresh ui
        if (alarmListAdapter != null){
            alarmListAdapter.notifyDataSetChanged();
        }

    }
}
