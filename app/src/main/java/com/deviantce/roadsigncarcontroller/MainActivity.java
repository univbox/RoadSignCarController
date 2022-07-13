package com.deviantce.roadsigncarcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.Button;

import com.deviantce.roadsigncarcontroller.fragments.BulletinFragment;
import com.deviantce.roadsigncarcontroller.fragments.SignboardFragment;
import com.deviantce.roadsigncarcontroller.fragments.SirenFragment;
import com.deviantce.roadsigncarcontroller.impl.ControllerViewListener;
import com.deviantce.serial_bulletin_library.SerialLight;
import com.deviantce.serial_bulletin_library.SerialSignboard;
import com.deviantce.serial_bulletin_library.SerialSigncarBulletin;
import com.deviantce.serial_bulletin_library.SerialSiren;

public class MainActivity extends AppCompatActivity implements SerialSiren.SirenSerialListener, ControllerViewListener {

    Button signboardButton;
    Button bulletinButton;
    Button sirenButton;
    Button emergencyButton;

    SignboardFragment signboardFragment;
    BulletinFragment bulletinFragment;
    SirenFragment sirenFragment;

    SerialSiren serialSiren;
    SerialSignboard serialSignboard;
    SerialSigncarBulletin serialSigncarBulletin;

    Button signboardTypeButton;
    Button signboardBrighnessButton;
    Button signboardSpeedButton;

    Button bulletinBrighnessButton;

    Button sirenVolumeButton;
    Button emergencyStateButton;

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
        signboardFragment.setListener(this);
        bulletinFragment.setListener(this);
        sirenFragment.setListener(this);

    }

    private void setViews() {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout, signboardFragment).commit();

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
        });
        bulletinButton.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,bulletinFragment).commit();

            signboardButton.setBackgroundResource(R.drawable.main_button_off);
            bulletinButton.setBackgroundResource(R.drawable.main_button_on);
            sirenButton.setBackgroundResource(R.drawable.main_button_off);
        });
        sirenButton.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,sirenFragment).commit();

            signboardButton.setBackgroundResource(R.drawable.main_button_off);
            bulletinButton.setBackgroundResource(R.drawable.main_button_off);
            sirenButton.setBackgroundResource(R.drawable.main_button_on);
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
    public void onSignboardBrighnessClicked(int brightness) {
        signboardBrighnessButton.setText(String.valueOf(brightness));
    }

    @Override
    public void onSignboardSpeedClicked(int speed) {
        signboardSpeedButton.setText(String.valueOf(speed));
    }

    @Override
    public void onSignboardTypeClicked(int type) {
        if(type==1){
            signboardTypeButton.setText("순차");
        }
        else if(type==2){
            signboardTypeButton.setText("점멸");
        }
    }
}