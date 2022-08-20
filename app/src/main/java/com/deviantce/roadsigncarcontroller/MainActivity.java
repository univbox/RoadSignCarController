package com.deviantce.roadsigncarcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import com.deviantce.roadsigncarcontroller.fragments.BulletinFragment;
import com.deviantce.roadsigncarcontroller.fragments.CameraPreviewFragment;
import com.deviantce.roadsigncarcontroller.fragments.SignboardFragment;
import com.deviantce.roadsigncarcontroller.fragments.SirenFragment;
import com.deviantce.roadsigncarcontroller.impl.ControllerViewListener;
import com.deviantce.serial_bulletin_library.SerialItem;
import com.deviantce.serial_bulletin_library.SerialLight;
import com.deviantce.serial_bulletin_library.SerialSignboard;
import com.deviantce.serial_bulletin_library.SerialSigncarBulletin;
import com.deviantce.serial_bulletin_library.SerialSiren;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SerialSiren.SirenSerialListener, ControllerViewListener {

    final String TAG = getClass().getSimpleName();

    Button signboardButton;
    Button bulletinButton;
    Button sirenButton;
    Button emergencyButton;

    SignboardFragment signboardFragment;
    BulletinFragment bulletinFragment;
    SirenFragment sirenFragment;
    CameraPreviewFragment cameraPreviewFragment;

    SerialSiren serialSiren;
    SerialSignboard serialSignboard;
    SerialSigncarBulletin serialSigncarBulletin;

    Button signboardTypeButton;
    Button signboardBrighnessButton;
    Button signboardSpeedButton;

    Button bulletinBrighnessButton;

    Button sirenVolumeButton;
    Button emergencyStateButton;

    ImageButton leftImageButton;
    ImageButton rightImageButton;
    ImageButton xImageButton;
    ImageButton twowayImageButton;

    boolean isEmergencyOn = false;

    Timer _tmrGotoMain = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        serVariables();
        setViews();
    }

    private void serVariables() {
        serialSiren = new SerialSiren(this);
        serialSiren.connect();
        serialSignboard = new SerialSignboard();
        serialSignboard.connect();
        serialSigncarBulletin = new SerialSigncarBulletin();
        serialSigncarBulletin.connect();

        signboardFragment = new SignboardFragment(serialSignboard);

        bulletinFragment = new BulletinFragment(serialSigncarBulletin);
        sirenFragment = new SirenFragment(serialSiren);

        cameraPreviewFragment = new CameraPreviewFragment();
        signboardFragment.setListener(this);
        bulletinFragment.setListener(this);
        sirenFragment.setListener(this);

    }

    private void setViews() {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout, cameraPreviewFragment).commit();

        signboardButton = findViewById(R.id.signboard_button);
        bulletinButton = findViewById(R.id.bulletin_button);
        sirenButton = findViewById(R.id.siren_button);
        emergencyButton = findViewById(R.id.emergency_button);

        signboardTypeButton = findViewById(R.id.signboard_simul_contin_button);
        signboardBrighnessButton = findViewById(R.id.signboard_brighness_button);
        signboardSpeedButton = findViewById(R.id.signboard_speed_button);

        bulletinBrighnessButton = findViewById(R.id.bulletin_bright_button);

        sirenVolumeButton = findViewById(R.id.siren_volume_button);

        emergencyStateButton = findViewById(R.id.emergency_state_button);

        signboardButton.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout, signboardFragment).commit();
            signboardButton.setBackgroundResource(R.drawable.main_button_on);
            bulletinButton.setBackgroundResource(R.drawable.main_button_off);
            sirenButton.setBackgroundResource(R.drawable.main_button_off);
            resetGotoHomeTimer();
        });
        bulletinButton.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,bulletinFragment).commit();

            signboardButton.setBackgroundResource(R.drawable.main_button_off);
            bulletinButton.setBackgroundResource(R.drawable.main_button_on);
            sirenButton.setBackgroundResource(R.drawable.main_button_off);
            resetGotoHomeTimer();
        });
        sirenButton.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,sirenFragment).commit();

            signboardButton.setBackgroundResource(R.drawable.main_button_off);
            bulletinButton.setBackgroundResource(R.drawable.main_button_off);
            sirenButton.setBackgroundResource(R.drawable.main_button_on);
            resetGotoHomeTimer();
        });

        emergencyButton.setOnClickListener(v -> {
            if(isEmergencyOn){
                serialSiren.setEmergencyOff();
                isEmergencyOn = false;
                emergencyStateButton.setText("꺼짐");
            }
            else{
                serialSiren.setEmergencyOn();
                isEmergencyOn = true;
                emergencyStateButton.setText("켜짐");
            }

        });

        leftImageButton = findViewById(R.id.signboard_left);
        rightImageButton = findViewById(R.id.signboard_right);
        xImageButton = findViewById(R.id.signboard_x);
        twowayImageButton = findViewById(R.id.signboard_twoway);
    }

    @Override
    public void onNewData() {

    }

    @Override
    public void onRunError(Exception e) {

    }

    @Override
    public void onSirenVolumeButtonClicked(int volume) {
        sirenVolumeButton.setText(String.valueOf(volume));
    }

    @Override
    public void onBulletinBrighnessClicked(int brighness) {
        bulletinBrighnessButton.setText(String.valueOf(brighness));
    }

    @Override
    public void onSignboardImageClicked(String type) {
        leftImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_left));
        rightImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_right));
        xImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_x));
        twowayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_twoway));
        if(type.equals("left")){
            leftImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_left_on));
        }
        else if(type.equals("right")){
            rightImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_right_on));
        }
        else if(type.equals("twoway")){
            twowayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_twoway_on));
        }
        else if(type.equals("x")){
            xImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_x_on));
        }
    }

    @Override
    public void onBulletinOffClicked() {
        bulletinButton.setText("전광판");
        bulletinBrighnessButton.setText("밝기");
    }

    @Override
    public void onSirenOffClicked() {
        sirenButton.setText("싸이렌");
        sirenVolumeButton.setText("볼륨");
    }

    @Override
    public void onSignboardOffClicked() {
        signboardBrighnessButton.setText("밝기");
        signboardSpeedButton.setText("속도");
        signboardTypeButton.setText("순차");
    }

    @Override
    public void onSirenButtonClicked(char siren_status) {
        if(siren_status == SerialItem.VOICE_1){
            sirenButton.setText("전방 1대");
        }
        else if(siren_status == SerialItem.VOICE_2){
            sirenButton.setText("전방 2대");
        }
        else if(siren_status == SerialItem.VOICE_3){
            sirenButton.setText("전방 3대");
        }
        else if(siren_status == SerialItem.VOICE_4){
            sirenButton.setText("전방 4대");
        }
        else if(siren_status == SerialItem.VOICE_5){
            sirenButton.setText("전방 5대");
        }
    }


    @Override
    public void onSignboardBrighnessClicked(String brightness) {
        signboardBrighnessButton.setText(brightness);
    }

    @Override
    public void onSignboardSpeedClicked(String speed) {
        signboardSpeedButton.setText(speed);
    }

    @Override
    public void onSignboardTypeClicked(String type) {
        signboardTypeButton.setText(type);
    }


    public void resetGotoHomeTimer() {
        /*1. 타이머가 null값이 아니면 예약 된 작업을 삭제하고 타이머를 종료하며 null값으로 변경한다.
         *  새로운 타이머 객체를 만들어서 지정된 작업이 실행되도록 예약한다.*/
        if (_tmrGotoMain != null) {
            _tmrGotoMain.cancel();
            _tmrGotoMain = null;
        }

        _tmrGotoMain = new Timer();
        _tmrGotoMain.schedule(new TimerTask() {

            @Override
            public void run() {
                if(_tmrGotoMain == null){
                    Log.e(TAG,"Timer null값");
                } else {

                    signboardButton.setBackgroundResource(R.drawable.main_button_off);
                    bulletinButton.setBackgroundResource(R.drawable.main_button_off);
                    sirenButton.setBackgroundResource(R.drawable.main_button_off);

                    getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout, cameraPreviewFragment).commit();
                }

            }
        }, 5000);
        Log.d(TAG, "Reset goto home timer.");
    }

    public void stopHomeTimer(){
        if (_tmrGotoMain != null) {
            Log.d(TAG, "stopHomeTimer()");
            _tmrGotoMain.cancel();
            _tmrGotoMain = null;

            signboardButton.setBackgroundResource(R.drawable.main_button_off);
            bulletinButton.setBackgroundResource(R.drawable.main_button_off);
            sirenButton.setBackgroundResource(R.drawable.main_button_off);


            getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout, cameraPreviewFragment).commit();

        }

    }
}