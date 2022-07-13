package com.deviantce.roadsigncarcontroller.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.deviantce.roadsigncarcontroller.MainActivity;
import com.deviantce.roadsigncarcontroller.R;
import com.deviantce.roadsigncarcontroller.impl.ControllerViewListener;
import com.deviantce.serial_bulletin_library.SerialItem;
import com.deviantce.serial_bulletin_library.SerialSiren;


public class SirenFragment extends Fragment {
    Button voice1_button;
    Button voice2_button;
    Button voice3_button;
    Button voice4_button;
    Button voice5_button;

    Button police_button;
    Button fire_button;
    Button ambulance_button;

    Button volume1_button;
    Button volume2_button;
    Button volume3_button;
    Button volume4_button;
    Button volume5_button;

    Button[] button_groups;
    Button[] volume_button_groups;

    SerialSiren serialSiren;
    ControllerViewListener listener;

    public SirenFragment() {
        // Required empty public constructor
    }


    public SirenFragment(SerialSiren serialSiren) {
        this.serialSiren = serialSiren;
    }


    public static SirenFragment newInstance() {
        SirenFragment fragment = new SirenFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_siren, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setViews(view);
        setButtonOnClick();
        setButtonStatus();
        setVolumeButtonStatus();
    }

    private void setButtonOnClick() {
        police_button.setOnClickListener( v -> {
            serialSiren.setPolice();
            setButtonStatus();
        });

        fire_button.setOnClickListener( v -> {
            serialSiren.setFire();
            setButtonStatus();
        });

        ambulance_button.setOnClickListener( v -> {
            serialSiren.setAmbulance();
            setButtonStatus();
        });

        voice1_button.setOnClickListener( v -> {
            serialSiren.setVoice1();
            setButtonStatus();
        });
        voice2_button.setOnClickListener( v -> {
            serialSiren.setVoice2();
            setButtonStatus();
        });
        voice3_button.setOnClickListener( v -> {
            serialSiren.setVoice3();
            setButtonStatus();
        });
        voice4_button.setOnClickListener( v -> {
            serialSiren.setVoice4();
            setButtonStatus();
        });
        voice5_button.setOnClickListener( v -> {
            serialSiren.setVoice5();
            setButtonStatus();
        });

        volume1_button.setOnClickListener( v -> {
            serialSiren.setVolume(1);
            setVolumeButtonStatus();
        });
        volume2_button.setOnClickListener( v -> {
            serialSiren.setVolume(2);
            setVolumeButtonStatus();
        });
        volume3_button.setOnClickListener( v -> {
            serialSiren.setVolume(3);
            setVolumeButtonStatus();
        });
        volume4_button.setOnClickListener( v -> {
            serialSiren.setVolume(4);
            setVolumeButtonStatus();
        });
        volume5_button.setOnClickListener( v -> {
            serialSiren.setVolume(5);
            setVolumeButtonStatus();
        });
    }

    private void setViews(View view) {
        voice1_button = view.findViewById(R.id.voice_1);
        voice2_button = view.findViewById(R.id.voice_2);
        voice3_button = view.findViewById(R.id.voice_3);
        voice4_button = view.findViewById(R.id.voice_4);
        voice5_button = view.findViewById(R.id.voice_5);

        police_button = view.findViewById(R.id.police_button);
        fire_button = view.findViewById(R.id.fire_button);
        ambulance_button = view.findViewById(R.id.ambulance_button);

        volume1_button = view.findViewById(R.id.volume_1);
        volume2_button = view.findViewById(R.id.volume_2);
        volume3_button = view.findViewById(R.id.volume_3);
        volume4_button = view.findViewById(R.id.volume_4);
        volume5_button = view.findViewById(R.id.volume_5);

        button_groups = new Button[]{
                police_button,fire_button,ambulance_button,
                voice1_button,voice2_button,voice3_button,voice4_button,voice5_button
        };
        volume_button_groups = new Button[]{
                volume1_button,volume2_button,volume3_button,volume4_button,volume5_button
        };
    }

    public void setButtonStatus(){
        char siren_status = serialSiren.getCurrent_siren_status();
        if(siren_status == SerialItem.CODE_FIRE){
            setButtonOnBackground(fire_button);
        }
        if(siren_status == SerialItem.CODE_POLICE){
            setButtonOnBackground(police_button);
        }
        if(siren_status == SerialItem.CODE_AMBULANCE){
            setButtonOnBackground(ambulance_button);
        }
        if(siren_status == SerialItem.VOICE_1){
            setButtonOnBackground(voice1_button);
        }
        if(siren_status == SerialItem.VOICE_2){
            setButtonOnBackground(voice2_button);
        }
        if(siren_status == SerialItem.VOICE_3){
            setButtonOnBackground(voice3_button);
        }
        if(siren_status == SerialItem.VOICE_4){
            setButtonOnBackground(voice4_button);
        }
        if(siren_status == SerialItem.VOICE_5){
            setButtonOnBackground(voice5_button);
        }
    }

    public void setVolumeButtonStatus(){
        int current_volume = serialSiren.getCurrentVolume();
        this.listener.onSirenVolumeButtonClicked(current_volume);
        volume_button_groups[current_volume-1].setBackgroundResource(R.drawable.siren_button_on);
        for(int i=0;i<volume_button_groups.length;i++){
            if(i!=current_volume-1)
                volume_button_groups[i].setBackgroundResource(R.drawable.siren_button_off);
        }
    }

    public void setButtonOnBackground(Button onbutton){
        onbutton.setBackgroundResource(R.drawable.siren_button_on);
        for(Button button : button_groups){
            if(button != onbutton)
                button.setBackgroundResource(R.drawable.siren_button_off);
        }
    }

    public void setListener(ControllerViewListener listener) {
        this.listener = listener;
    }
}