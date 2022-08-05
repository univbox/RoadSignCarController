package com.deviantce.roadsigncarcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.deviantce.roadsigncarcontroller.fragments.BulletinFragment;
import com.deviantce.roadsigncarcontroller.fragments.CameraPreviewFragment;
import com.deviantce.roadsigncarcontroller.fragments.SignboardFragment;
import com.deviantce.roadsigncarcontroller.fragments.SirenFragment;
import com.deviantce.roadsigncarcontroller.impl.ControllerViewListener;
import com.deviantce.serial_bulletin_library.SerialLight;
import com.deviantce.serial_bulletin_library.SerialSignboard;
import com.deviantce.serial_bulletin_library.SerialSigncarBulletin;
import com.deviantce.serial_bulletin_library.SerialSiren;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SerialSiren.SirenSerialListener, ControllerViewListener {

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
            serialSiren.setEmergencyOn();
        });
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
            final FragmentManager fragmentManager = getSupportFragmentManager(); //Activity안에 있는 Fragment와 상호 작용 및 관리(Activity에 추가, 교체...)를 위한 클래스

            @Override
            public void run() {
//                if (!((MainActivity) context).isFinishing()) {
                if(_tmrGotoMain == null){
                    Log.e("MainActivity :","Timer null값");
                } else {
                    //fragmentManager.beginTransaction().replace(R.id.beacon_control_contents, _fragments[BoardIndex.RearCamera.value]).commit(); //카메라 프레그먼트에 대한 값을 가져와서 보여준다.
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout, cameraPreviewFragment).commit();

                }
//                }

            }
        }, 5000);
        Log.d("Juno", "Reset goto home timer.");
    }

    void stopHomeTimer(){
        if (_tmrGotoMain != null) {
            Log.d("Juno", "stopHomeTimer()");
            _tmrGotoMain.cancel();
            _tmrGotoMain = null;
        }
    }
}