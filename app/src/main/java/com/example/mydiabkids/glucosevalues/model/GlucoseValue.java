package com.example.mydiabkids.glucosevalues.model;

import android.os.Parcel;

import com.thoughtbot.expandablecheckrecyclerview.models.SingleCheckExpandableGroup;

import java.util.List;

public class GlucoseValue extends SingleCheckExpandableGroup {
    public GlucoseValue(String title, List<Value> items) {
        super(title, items);
    }

    protected GlucoseValue(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GlucoseValue> CREATOR = new Creator<GlucoseValue>() {
        @Override
        public GlucoseValue createFromParcel(Parcel in) {
            return new GlucoseValue(in);
        }

        @Override
        public GlucoseValue[] newArray(int size) {
            return new GlucoseValue[size];
        }
    };
}
