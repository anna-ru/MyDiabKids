package com.example.mydiabkids.glucosevalues.ui;

import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;

import com.example.mydiabkids.R;
import com.example.mydiabkids.glucosevalues.model.CheckableCardView;
import com.example.mydiabkids.glucosevalues.model.Value;
import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;

class ValueViewHolder extends CheckableChildViewHolder {
    private final TextView time, insulin, value, eating, type, notes;
    private final CheckableCardView checkableCardView;

    public ValueViewHolder(View itemView) {
        super(itemView);

        time = itemView.findViewById(R.id.time_edit);
        insulin = itemView.findViewById(R.id.insulin_edit);
        value = itemView.findViewById(R.id.value_edit);
        eating = itemView.findViewById(R.id.eating_txt);
        type = itemView.findViewById(R.id.insulin_type);
        notes = itemView.findViewById(R.id.notes_edit_text);
        checkableCardView = itemView.findViewById(R.id.checkable_cardView);
    }

    @Override
    public Checkable getCheckable() {
        return checkableCardView;
    }

    public void bind(Value value){
        time.setText(value.getTime());
        insulin.setText(String.valueOf(value.getInsulin()));
        this.value.setText(String.valueOf(value.getGl_value()));
        eating.setText(value.getBefore_eating());
        type.setText(value.getInsulin_type());
        notes.setText(value.getNotes());
    }
}
