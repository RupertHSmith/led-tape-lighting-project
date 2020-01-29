package com.rgbtape.app;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class EffectsAdapter extends ArrayAdapter<EffectItem> {
    public EffectsAdapter(Context context, ArrayList<EffectItem> countryItemArrayList){
        super(context,0, countryItemArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent){
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.effect_spinner, parent, false
            );
        }

        TextView textView = convertView.findViewById(R.id.text_view_name);

        EffectItem currentItem = getItem(position);

        if (currentItem != null) {
            textView.setText(currentItem.getEffectname());
        }

        return convertView;
    }
}
