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

public class GlucoseValueAdapter extends CheckableChildRecyclerViewAdapter<HeaderViewHolder, DetailsViewHolder> {

    public GlucoseValueAdapter(List<GlucoseValueHeader> groups) {
        super(groups);
    }

    @Override
    public HeaderViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_group, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public DetailsViewHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_child, parent, false);
        return new DetailsViewHolder(view);
    }

    @Override
    public void onBindCheckChildViewHolder(DetailsViewHolder holder, int flatPosition, CheckedExpandableGroup group, int childIndex) {
        GlucoseValueDetails glucoseValueDetails = (GlucoseValueDetails) ((GlucoseValueHeader) group).getItems().get(childIndex);
        holder.bind(glucoseValueDetails);
    }

    @Override
    public void onBindGroupViewHolder(HeaderViewHolder holder, int flatPosition, ExpandableGroup group) {
        GlucoseValueHeader value = (GlucoseValueHeader) group;
        holder.bind(value);
    }

}
