package com.deviantce.roadsigncarcontroller.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.deviantce.roadsigncarcontroller.R;
import com.deviantce.serial_bulletin_library.SerialSigncarBulletin;


public class BulletinFragment extends Fragment {

    SerialSigncarBulletin serialSigncarBulletin;

    ImageView[] imageViews;

    public BulletinFragment() {
        // Required empty public constructor
    }

    public BulletinFragment(SerialSigncarBulletin serialSigncarBulletin) {
        this.serialSigncarBulletin = serialSigncarBulletin;
    }


    public static BulletinFragment newInstance(String param1, String param2) {
        BulletinFragment fragment = new BulletinFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bulletin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView(view);
    }


    private void setView(View view) {
        imageViews = new ImageView[28];
        for(int i=1;i<=26;i++){
            imageViews[i] = getImageId(view,i);
            int finalI = i;
            imageViews[i].setOnClickListener(v -> {
                serialSigncarBulletin.setBulletinNumber(finalI);
            });
        }
    }

    private ImageView getImageId(View view, int i) {
        switch(i){
            case 1:
                return view.findViewById(R.id.image_1);
            case 2:
                return view.findViewById(R.id.image_2);
            case 3:
                return view.findViewById(R.id.image_3);
            case 4:
                return view.findViewById(R.id.image_4);

            case 5:
                return view.findViewById(R.id.image_5);
            case 6:
                return view.findViewById(R.id.image_6);
            case 7:
                return view.findViewById(R.id.image_7);
            case 8:
                return view.findViewById(R.id.image_8);

            case 9:
                return view.findViewById(R.id.image_9);
            case 10:
                return view.findViewById(R.id.image_10);
            case 11:
                return view.findViewById(R.id.image_11);
            case 12:
                return view.findViewById(R.id.image_12);

            case 13:
                return view.findViewById(R.id.image_13);
            case 14:
                return view.findViewById(R.id.image_14);
            case 15:
                return view.findViewById(R.id.image_15);
            case 16:
                return view.findViewById(R.id.image_16);

            case 17:
                return view.findViewById(R.id.image_17);
            case 18:
                return view.findViewById(R.id.image_18);
            case 19:
                return view.findViewById(R.id.image_19);
            case 20:
                return view.findViewById(R.id.image_20);

            case 21:
                return view.findViewById(R.id.image_21);
            case 22:
                return view.findViewById(R.id.image_22);
            case 23:
                return view.findViewById(R.id.image_23);
            case 24:
                return view.findViewById(R.id.image_24);

            case 25:
                return view.findViewById(R.id.image_25);
            case 26:
                return view.findViewById(R.id.image_26);



            default:
                return view.findViewById(R.id.image_5);
        }

    }
}