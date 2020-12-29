package com.example.mydiabkids.glucosevalues.ui;

import android.view.View;
import android.widget.TextView;

import com.example.mydiabkids.R;
import com.example.mydiabkids.glucosevalues.model.GlucoseValueHeader;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

class HeaderViewHolder extends GroupViewHolder {

    private final TextView date;

    public HeaderViewHolder(View itemView) {
        super(itemView);
        date = itemView.findViewById(R.id.date_edit);
    }

    void bind(GlucoseValueHeader glucoseValueHeader){
        date.setText(glucoseValueHeader.getTitle());
    }
}
