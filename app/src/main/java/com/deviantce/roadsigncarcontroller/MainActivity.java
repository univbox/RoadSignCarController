package com.deviantce.roadsigncarcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.Button;

import com.deviantce.roadsigncarcontroller.fragments.BulletinFragment;
import com.deviantce.roadsigncarcontroller.fragments.SignboardFragment;
import com.deviantce.roadsigncarcontroller.fragments.SirenFragment;
import com.deviantce.serial_bulletin_library.SerialLight;
import com.deviantce.serial_bulletin_library.SerialSignboard;
import com.deviantce.serial_bulletin_library.SerialSigncarBulletin;
import com.deviantce.serial_bulletin_library.SerialSiren;

public class MainActivity extends AppCompatActivity implements SerialSiren.SirenSerialListener {

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


    }

    private void setViews() {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout, signboardFragment).commit();

        signboardButton = findViewById(R.id.signboard_button);
        bulletinButton = findViewById(R.id.bulletin_button);
        sirenButton = findViewById(R.id.siren_button);
        emergencyButton = findViewById(R.id.emergency_button);

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
}