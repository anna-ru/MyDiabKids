package com.example.mydiabkids.glucosevalues.model;

import android.os.Parcel;

import com.thoughtbot.expandablecheckrecyclerview.models.SingleCheckExpandableGroup;

import java.util.List;

public class GlucoseValueHeader extends SingleCheckExpandableGroup implements Comparable<GlucoseValueHeader>{
    public GlucoseValueHeader(String title, List<GlucoseValueDetails> items) {
        super(title, items);
    }

    protected GlucoseValueHeader(Parcel in) {
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

    public static final Creator<GlucoseValueHeader> CREATOR = new Creator<GlucoseValueHeader>() {
        @Override
        public GlucoseValueHeader createFromParcel(Parcel in) {
            return new GlucoseValueHeader(in);
        }

        @Override
        public GlucoseValueHeader[] newArray(int size) {
            return new GlucoseValueHeader[size];
        }
    };

    @Override
    public int compareTo(GlucoseValueHeader glucoseValueHeader) {
        return getTitle().compareTo(glucoseValueHeader.getTitle());
    }
}
