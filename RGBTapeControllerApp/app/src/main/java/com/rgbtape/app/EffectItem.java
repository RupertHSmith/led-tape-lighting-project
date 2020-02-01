package com.rgbtape.app;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EffectItem {
    public static final String STATIC = "StaticEffect";
    public static final String BREATHING = "Breathing";
    public static final String STANDBY = "Standby";
    public static final String SPECTRUM_CYCLING = "SpectrumCycling";
    public static final String CUSTOM_EFFECT = "CustomEffect";
    public static final String STROBE_EFFECT = "Strobe";
    public static final String COOL_WHITE = "CoolWhite";
    public static final String WARM_WHITE = "WarmWhite";


    private String effectname;
    private String type;
    private String customEffectID;

    public EffectItem(String effectname, String type){
        this.effectname = effectname;
        this.type = type;
    }

    public EffectItem(String effectname, String type, String customEffectID){
        this.effectname = effectname;
        this.type = type;
        this.customEffectID = customEffectID;
    }

    public String getEffectname(){
        return effectname;
    }

    public static List<EffectItem> getDefaultEffects(){
        List<EffectItem> defaultEffectItems = new ArrayList<>();
        defaultEffectItems.add(new EffectItem("Static", STATIC));
        defaultEffectItems.add(new EffectItem("Cool White", COOL_WHITE));
        defaultEffectItems.add(new EffectItem("Warm White", WARM_WHITE));
        defaultEffectItems.add(new EffectItem("Spectrum Cycling", SPECTRUM_CYCLING));
        defaultEffectItems.add(new EffectItem("Breathing", BREATHING));
        defaultEffectItems.add(new EffectItem("Strobe", STROBE_EFFECT));
        return defaultEffectItems;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof EffectItem){
            EffectItem compareTo = (EffectItem) obj;
            if (compareTo.getType().equals(getType())){
                if(getType().equals(CUSTOM_EFFECT)) {
                    if (compareTo.getCustomEffectID().equals(getCustomEffectID()))
                        return true;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public String getType(){
        return type;
    }

    public String getCustomEffectID(){
        return customEffectID;
    }

    public static void addCustomEffectsToList(List<EffectItem> customEffectsList, List<EffectItem> effectsList){

       effectsList.retainAll(getDefaultEffects());

        effectsList.addAll(customEffectsList);
    }
}
