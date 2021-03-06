package com.deviantce.roadsigncarcontroller.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.deviantce.roadsigncarcontroller.R;
import com.deviantce.roadsigncarcontroller.impl.ControllerViewListener;
import com.deviantce.serial_bulletin_library.SerialSignboard;
import com.deviantce.serial_bulletin_library.SerialSiren;
import com.deviantce.serial_bulletin_library.SignboardItem;


public class SignboardFragment extends Fragment {

    Button leftButton;
    Button twowayButton;
    Button rightButton;
    Button XButton;

    Button dayButton;
    Button cloudyButton;
    Button nightButton;

    Button fastButton;
    Button midButton;
    Button slowButton;

    Button simulButton;
    Button continButton;

    SerialSignboard serialSignboard;
    ControllerViewListener listener;

    public SignboardFragment() {
        // Required empty public constructor
    }

    public SignboardFragment(SerialSignboard serialSignboard) {
        this.serialSignboard = serialSignboard;
    }

    public static SignboardFragment newInstance(String param1, String param2) {
        SignboardFragment fragment = new SignboardFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_signboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView(view);
        setOnClicks();
        setButtonStatus();
    }

    private void setOnClicks() {
        leftButton.setOnClickListener(v -> {
            serialSignboard.setLeft();
            setButtonStatus();
        });
        twowayButton.setOnClickListener( v -> {
            serialSignboard.setTwoway();
            setButtonStatus();
        });
        rightButton.setOnClickListener(v -> {
            serialSignboard.setRight();
            setButtonStatus();
        });
        XButton.setOnClickListener( v -> {
            serialSignboard.setX();
            setButtonStatus();
        });

        dayButton.setOnClickListener( v -> {
            serialSignboard.setDay();
            setButtonStatus();
        });
        cloudyButton.setOnClickListener(v -> {
            serialSignboard.setCloudy();
            setButtonStatus();
        });
        nightButton.setOnClickListener(v -> {
            serialSignboard.setNight();
            setButtonStatus();
        });

        fastButton.setOnClickListener( v ->{
            serialSignboard.setFast();
            setButtonStatus();
        });
        midButton.setOnClickListener(v ->{
            serialSignboard.setMid();
            setButtonStatus();
        });
        slowButton.setOnClickListener( v ->{
            serialSignboard.setSlow();
            setButtonStatus();
        });
        simulButton.setOnClickListener( v ->{
            serialSignboard.setSimul();
            setButtonStatus();
        });
        continButton.setOnClickListener( v ->{
            serialSignboard.setContin();
            setButtonStatus();
        });
    }

    private void setView(View view) {
        leftButton = view.findViewById(R.id.left);
        twowayButton = view.findViewById(R.id.twoway);
        rightButton = view.findViewById(R.id.right);
        XButton = view.findViewById(R.id.X);

        dayButton = view.findViewById(R.id.day);
        cloudyButton = view.findViewById(R.id.cloudy);
        nightButton = view.findViewById(R.id.night);

        fastButton = view.findViewById(R.id.fast);
        midButton = view.findViewById(R.id.mid);
        slowButton = view.findViewById(R.id.slow);

        simulButton = view.findViewById(R.id.simul);
        continButton = view.findViewById(R.id.contin);
    }

    public void setListener(ControllerViewListener listener){
        this.listener = listener;
    }

    public void setButtonStatus(){
        setSignboardStatusButton();
        setBrightnessStatusButton();
        setSpeedStatusButton();
        setSimulContinButton();
    }

    private void setSimulContinButton() {
        char cur_simul_contin = serialSignboard.getCurrentSimulContin();
        if(cur_simul_contin == SignboardItem.SIMUL){
            setButtonOnBackground(simulButton);
            setButtonOffBackground(continButton);
        }
        if(cur_simul_contin == SignboardItem.CONTIN){
            setButtonOffBackground(simulButton);
            setButtonOnBackground(continButton);
        }
    }

    private void setSpeedStatusButton() {
        char cur_speed = serialSignboard.getCurrentSpeed();
        if(cur_speed==SignboardItem.FAST){
            setButtonOnBackground(fastButton);
            setButtonOffBackground(midButton);
            setButtonOffBackground(slowButton);
        }
        if(cur_speed==SignboardItem.MID){
            setButtonOffBackground(fastButton);
            setButtonOnBackground(midButton);
            setButtonOffBackground(slowButton);
        }
        if(cur_speed==SignboardItem.SLOW){
            setButtonOffBackground(fastButton);
            setButtonOffBackground(midButton);
            setButtonOnBackground(slowButton);
        }
    }

    private void setBrightnessStatusButton() {
        char cur_brighness = serialSignboard.getCurrentBrighness();
        if(cur_brighness==SignboardItem.DAY){
            setButtonOnBackground(dayButton);
            setButtonOffBackground(cloudyButton);
            setButtonOffBackground(nightButton);
        }
        if(cur_brighness==SignboardItem.CLOUDY){
            setButtonOffBackground(dayButton);
            setButtonOnBackground(cloudyButton);
            setButtonOffBackground(nightButton);
        }
        if(cur_brighness==SignboardItem.NIGHT){
            setButtonOffBackground(dayButton);
            setButtonOffBackground(cloudyButton);
            setButtonOnBackground(nightButton);
        }
    }

    public void setSignboardStatusButton(){
        char current_signboard_status = serialSignboard.getCurrentSignboardStatus();
        if(current_signboard_status== SignboardItem.LEFT){
            setButtonOnBackground(leftButton);
            setButtonOffBackground(twowayButton);
            setButtonOffBackground(rightButton);
            setButtonOffBackground(XButton);
        }
        if(current_signboard_status== SignboardItem.TWOWAY){
            setButtonOffBackground(leftButton);
            setButtonOnBackground(twowayButton);
            setButtonOffBackground(rightButton);
            setButtonOffBackground(XButton);
        }
        if(current_signboard_status== SignboardItem.RIGHT){
            setButtonOffBackground(leftButton);
            setButtonOffBackground(twowayButton);
            setButtonOnBackground(rightButton);
            setButtonOffBackground(XButton);
        }
        if(current_signboard_status== SignboardItem.X){
            setButtonOffBackground(leftButton);
            setButtonOffBackground(twowayButton);
            setButtonOffBackground(rightButton);
            setButtonOnBackground(XButton);
        }
    }

    public void setButtonOnBackground(Button onbutton){
        onbutton.setBackgroundResource(R.drawable.siren_button_on);
    }

    public void setButtonOffBackground(Button offbutton){
        offbutton.setBackgroundResource(R.drawable.siren_button_off);
    }
}