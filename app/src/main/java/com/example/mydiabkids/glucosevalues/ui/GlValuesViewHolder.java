package com.example.mydiabkids.glucosevalues.ui;

import android.view.View;
import android.widget.TextView;

import com.example.mydiabkids.R;
import com.example.mydiabkids.glucosevalues.model.GlucoseValue;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

class GlValuesViewHolder extends GroupViewHolder {

    private final TextView date;

    public GlValuesViewHolder(View itemView) {
        super(itemView);
        date = itemView.findViewById(R.id.date_edit);
    }

    void bind(GlucoseValue glucoseValue){
        date.setText(glucoseValue.getTitle());
    }
}
