package com.example.mydiabkids.glucosevalues.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.mydiabkids.R;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mydiabkids.settings.ThemeFragment.BLUE;
import static com.example.mydiabkids.settings.ThemeFragment.BROWN;
import static com.example.mydiabkids.settings.ThemeFragment.GREEN;
import static com.example.mydiabkids.settings.ThemeFragment.ORANGE;
import static com.example.mydiabkids.settings.ThemeFragment.PINK;
import static com.example.mydiabkids.settings.ThemeFragment.YELLOW;

public class CheckableCardView extends CardView implements Checkable {
    private boolean isChecked = false;
    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked,
    };

    public CheckableCardView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public CheckableCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CheckableCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.checkable_card_view, this, true);
        setClickable(true);
        setChecked(false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.example.mydiabkids.THEME", MODE_PRIVATE);
        int theme = sharedPreferences.getInt("com.example.mydiabkids.THEME", PINK);
        switch (theme){
            case PINK:
                setCardBackgroundColor(ContextCompat.getColorStateList(getContext(), R.color.pink_card_view_colors));
                break;
            case BLUE:
                setCardBackgroundColor(ContextCompat.getColorStateList(getContext(), R.color.blue_card_view_colors));
                break;
            case GREEN:
                setCardBackgroundColor(ContextCompat.getColorStateList(getContext(), R.color.green_card_view_colors));
                break;
            case YELLOW:
                setCardBackgroundColor(ContextCompat.getColorStateList(getContext(), R.color.yellow_card_view_colors));
                break;
            case ORANGE:
                setCardBackgroundColor(ContextCompat.getColorStateList(getContext(), R.color.orange_card_view_colors));
                break;
            case BROWN:
                setCardBackgroundColor(ContextCompat.getColorStateList(getContext(), R.color.brown_card_view_colors));
                break;
        }

    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }
    @Override
    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }
    @Override
    public boolean isChecked() {
        return isChecked;
    }
    @Override
    public void toggle() {
        setChecked(!this.isChecked);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState =
                super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }


}