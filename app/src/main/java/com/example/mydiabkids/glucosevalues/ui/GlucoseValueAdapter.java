package com.example.mydiabkids.glucosevalues.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mydiabkids.R;
import com.example.mydiabkids.glucosevalues.model.GlucoseValueDetails;
import com.example.mydiabkids.glucosevalues.model.GlucoseValueHeader;
import com.thoughtbot.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class GlucoseValueAdapter extends CheckableChildRecyclerViewAdapter<GlValuesViewHolder, ValueViewHolder> {

    public GlucoseValueAdapter(List<GlucoseValueHeader> groups) {
        super(groups);
    }

    @Override
    public GlValuesViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_group, parent, false);
        return new GlValuesViewHolder(view);
    }

    @Override
    public ValueViewHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_child, parent, false);
        return new ValueViewHolder(view);
    }

    @Override
    public void onBindCheckChildViewHolder(ValueViewHolder holder, int flatPosition, CheckedExpandableGroup group, int childIndex) {
        GlucoseValueDetails glucoseValueDetails = (GlucoseValueDetails) ((GlucoseValueHeader) group).getItems().get(childIndex);
        holder.bind(glucoseValueDetails);
    }

    @Override
    public void onBindGroupViewHolder(GlValuesViewHolder holder, int flatPosition, ExpandableGroup group) {
        GlucoseValueHeader value = (GlucoseValueHeader) group;
        holder.bind(value);
    }

}
