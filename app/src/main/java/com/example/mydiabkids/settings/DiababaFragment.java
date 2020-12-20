package com.example.mydiabkids.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.mydiabkids.R;

import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

public class DiababaFragment extends Fragment {

    private ImageSwitcher imageSwitcher;
    private final Integer[] images = {R.drawable.blood_smiling, R.drawable.apple, R.drawable.boy, R.drawable.girl, R.drawable.knight,
            R.drawable.princess,R.drawable.salad, R.drawable.pumpkin, R.drawable.snail, R.drawable.strawberry,
            R.drawable.dog, R.drawable.cat, R.drawable.jellyfish, R.drawable.penguin,};
    private final ArrayList<Integer> imageList = new ArrayList<>(Arrays.asList(images));
    private int position = 0;
    private int currentImage = R.drawable.blood_smiling;
    private Context context;
    private SharedPreferences sharedPreferences;

    public DiababaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diababa, container, false);

        context = getContext();
        sharedPreferences = context.getSharedPreferences(getString(R.string.diababa_shared_pref), MODE_PRIVATE);
        currentImage = sharedPreferences.getInt(getString(R.string.diababa_shared_pref), R.drawable.blood_smiling);
        position = imageList.indexOf(currentImage);

        ImageButton previousButton = view.findViewById(R.id.previous);
        ImageButton nextButton = view.findViewById(R.id.next);
        Button confirmButton = view.findViewById(R.id.confirm_image_btn);

        imageSwitcher = view.findViewById(R.id.img_switcher);
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(context);
                imageView.setImageResource(currentImage);
                return imageView;
            }
        });

        final Animation leftToCenter = AnimationUtils.loadAnimation(context, R.anim.img_switch_in_lc);
        final Animation rightToCenter = AnimationUtils.loadAnimation(context, R.anim.img_switch_in_rc);
        final Animation centerToLeft = AnimationUtils.loadAnimation(context, R.anim.img_switch_out_cl);
        final Animation centerToRight = AnimationUtils.loadAnimation(context, R.anim.img_switch_out_cr);

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position>=0)
                    position--;
                if(position<0)
                    position = images.length-1;
                imageSwitcher.setInAnimation(leftToCenter);
                imageSwitcher.setOutAnimation(centerToRight);
                imageSwitcher.setImageResource(imageList.get(position));
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position<images.length)
                    position++;
                if(position>=images.length)
                    position = 0;
                imageSwitcher.setInAnimation(rightToCenter);
                imageSwitcher.setOutAnimation(centerToLeft);
                imageSwitcher.setImageResource(imageList.get(position));
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.diababa_shared_pref), imageList.get(position));
                editor.apply();
                getActivity().recreate();
                Toast.makeText(context, "Diababa beállítása sikerült.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}