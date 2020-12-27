package com.example.mydiabkids.glucosevalues.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GlucoseValueDetails implements Parcelable, Comparable<GlucoseValueDetails> {

    private String time;
    private String insulin_type;
    private String notes;
    private double gl_value;
    private double insulin;
    private String before_eating;

    public GlucoseValueDetails(String time, String insulin_type, String notes, double gl_value,
                               double insulin, String before_eating) {
        this.time = time;
        this.insulin_type = insulin_type;
        this.notes = notes;
        this.gl_value = gl_value;
        this.insulin = insulin;
        this.before_eating = before_eating;
    }

    protected GlucoseValueDetails(Parcel in) {
        time = in.readString();
        insulin_type = in.readString();
        notes = in.readString();
        gl_value = in.readDouble();
        insulin = in.readDouble();
        before_eating = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(time);
        dest.writeString(insulin_type);
        dest.writeString(notes);
        dest.writeDouble(gl_value);
        dest.writeDouble(insulin);
        dest.writeString(before_eating);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GlucoseValueDetails> CREATOR = new Creator<GlucoseValueDetails>() {
        @Override
        public GlucoseValueDetails createFromParcel(Parcel in) {
            return new GlucoseValueDetails(in);
        }

        @Override
        public GlucoseValueDetails[] newArray(int size) {
            return new GlucoseValueDetails[size];
        }
    };

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInsulin_type() {
        return insulin_type;
    }

    public void setInsulin_type(String insulin_type) {
        this.insulin_type = insulin_type;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getGl_value() {
        return gl_value;
    }

    public void setGl_value(double gl_value) {
        this.gl_value = gl_value;
    }

    public double getInsulin() {
        return insulin;
    }

    public void setInsulin(double insulin) {
        this.insulin = insulin;
    }

    public String getBefore_eating() {
        return before_eating;
    }

    public void setBefore_eating(String before_eating) {
        this.before_eating = before_eating;
    }

    @Override
    public int compareTo(GlucoseValueDetails glucoseValueDetails) {
        return time.compareTo(glucoseValueDetails.getTime());
    }
}
