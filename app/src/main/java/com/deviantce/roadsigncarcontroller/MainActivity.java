package com.deviantce.roadsigncarcontroller;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
import com.deviantce.serial_bulletin_library.SignboardItem;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SerialSiren.SirenSerialListener, ControllerViewListener {

    final String TAG = getClass().getSimpleName();

    LinearLayout signboardStatusLL;
    LinearLayout signboardStatusLL1;
    LinearLayout signboardStatusLL2;
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

    RelativeLayout bulletinRL;
    ImageView bulletinImgView;

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
        signboardStatusLL = findViewById(R.id.signboard_status_rl);
        signboardStatusLL1 = findViewById(R.id.signboard_status_ll_1);
        signboardStatusLL2 = findViewById(R.id.signboard_status_ll_2);

        bulletinRL = findViewById(R.id.bulletin_relative_layout);
        bulletinImgView = findViewById(R.id.bulletin_img_imageview);
        bulletinImgView.setVisibility(View.GONE);
        bulletinRL.setBackgroundColor(getResources().getColor(R.color.white));

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

        bulletinImgView.setOnClickListener(v -> {
            bulletinButton.performClick();
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

        leftImageButton.setOnClickListener( v -> {
            char current_signboard_status = signboardFragment.getSignboardStatus();
            if(current_signboard_status==SignboardItem.LEFT){
                signboardFragment.offSignboard();
                onSignboardOffClicked();
            }
            else{
                signboardFragment.onArrowButtonClicked(SignboardItem.LEFT);
                onSignboardImageClicked("left");
            }

        });
        rightImageButton.setOnClickListener(v -> {
            char current_signboard_status = signboardFragment.getSignboardStatus();
            if(current_signboard_status==SignboardItem.RIGHT){
                signboardFragment.offSignboard();
                onSignboardOffClicked();
            }
            else{
                signboardFragment.onArrowButtonClicked(SignboardItem.RIGHT);
                onSignboardImageClicked("right");
            }


        });
        xImageButton.setOnClickListener( v -> {
            char current_signboard_status = signboardFragment.getSignboardStatus();
            if(current_signboard_status==SignboardItem.X){
                signboardFragment.offSignboard();
                onSignboardOffClicked();
            }
            else{
                signboardFragment.onArrowButtonClicked(SignboardItem.X);
                onSignboardImageClicked("x");
            }

        });
        twowayImageButton.setOnClickListener( v -> {
            char current_signboard_status = signboardFragment.getSignboardStatus();
            if(current_signboard_status==SignboardItem.TWOWAY){
                signboardFragment.offSignboard();
                onSignboardOffClicked();
            }
            else{
                signboardFragment.onArrowButtonClicked(SignboardItem.TWOWAY);
                onSignboardImageClicked("twoway");
            }

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
    public void onSignboardImageClicked(String type) {
        signboardStatusLL.setVisibility(View.VISIBLE);
        leftImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_left));
        rightImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_right));
        xImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_x));
        twowayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_twoway));

        rightImageButton.setBackgroundResource(R.drawable.siren_button_off);
        xImageButton.setBackgroundResource(R.drawable.siren_button_off);
        twowayImageButton.setBackgroundResource(R.drawable.siren_button_off);
        leftImageButton.setBackgroundResource(R.drawable.siren_button_off);

        if(type.equals("left")){
//            signboardStatusLL1.setVisibility(View.VISIBLE);
//            signboardStatusLL2.setVisibility(View.GONE);

//            leftImageButton.setVisibility(View.VISIBLE);
//            rightImageButton.setVisibility(View.GONE);
//            twowayImageButton.setVisibility(View.GONE);
//            xImageButton.setVisibility(View.GONE);

            leftImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_left_on));
            leftImageButton.setBackgroundResource(R.drawable.siren_button_on);
        }
        else if(type.equals("right")){
//            signboardStatusLL1.setVisibility(View.VISIBLE);
//            signboardStatusLL2.setVisibility(View.GONE);

//            leftImageButton.setVisibility(View.GONE);
//            rightImageButton.setVisibility(View.VISIBLE);
//            twowayImageButton.setVisibility(View.GONE);
//            xImageButton.setVisibility(View.GONE);

            rightImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_right_on));
            rightImageButton.setBackgroundResource(R.drawable.siren_button_on);
        }
        else if(type.equals("twoway")){
//            signboardStatusLL1.setVisibility(View.GONE);
//            signboardStatusLL2.setVisibility(View.VISIBLE);

//            leftImageButton.setVisibility(View.GONE);
//            rightImageButton.setVisibility(View.GONE);
//            twowayImageButton.setVisibility(View.VISIBLE);
//            xImageButton.setVisibility(View.GONE);

            twowayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_twoway_on));
            twowayImageButton.setBackgroundResource(R.drawable.siren_button_on);
        }
        else if(type.equals("x")){
//            signboardStatusLL1.setVisibility(View.GONE);
//            signboardStatusLL2.setVisibility(View.VISIBLE);

//            leftImageButton.setVisibility(View.GONE);
//            rightImageButton.setVisibility(View.GONE);
//            twowayImageButton.setVisibility(View.GONE);
//            xImageButton.setVisibility(View.VISIBLE);

            xImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_x_on));
            xImageButton.setBackgroundResource(R.drawable.siren_button_on);
        }
    }

    @Override
    public void onBulletinOffClicked() {
        bulletinButton.setVisibility(View.VISIBLE);
        bulletinButton.setText("전광판");
        bulletinBrighnessButton.setText("밝기");
        bulletinRL.setBackgroundColor(getResources().getColor(R.color.white));
        bulletinImgView.setVisibility(View.GONE);
    }

    @Override
    public void onSirenOffClicked() {
        sirenButton.setText("싸이렌");
        sirenVolumeButton.setText("볼륨");
    }

    @Override
    public void onSignboardOffClicked() {
        //signboardStatusLL.setVisibility(View.GONE);
        signboardBrighnessButton.setText("밝기");
        signboardSpeedButton.setText("속도");
        signboardTypeButton.setText("순차");

        leftImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_left));
        rightImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_right));
        xImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_x));
        twowayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.signboard_twoway));


        rightImageButton.setBackgroundResource(R.drawable.siren_button_off);
        xImageButton.setBackgroundResource(R.drawable.siren_button_off);
        twowayImageButton.setBackgroundResource(R.drawable.siren_button_off);
        leftImageButton.setBackgroundResource(R.drawable.siren_button_off);
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
        else if(siren_status == SerialItem.CODE_POLICE){
            sirenButton.setText("경찰");
        }
        else if(siren_status == SerialItem.CODE_FIRE){
            sirenButton.setText("소방");
        }
        else if(siren_status == SerialItem.CODE_AMBULANCE){
            sirenButton.setText("구급");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBulletinImageClicked(int img_id) {
        bulletinRL.setBackgroundColor(getResources().getColor(R.color.black));
        bulletinImgView.setVisibility(View.VISIBLE);
        bulletinButton.setVisibility(View.GONE);

        if(img_id==1){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_1));
        }
        else if(img_id==2){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_2));
        }
        else if(img_id==3){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_3));
        }
        else if(img_id==4){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_4));
        }
        else if(img_id==5){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_5));
        }
        else if(img_id==6){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_6));
        }
        else if(img_id==7){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_7));
        }
        else if(img_id==8){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_8));
        }
        else if(img_id==9){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_9));
        }

        else if(img_id==10){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_10));
        }
        else if(img_id==11){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_11));
        }
        else if(img_id==12){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_12));
        }
        else if(img_id==13){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_13));
        }
        else if(img_id==14){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_14));
        }
        else if(img_id==15){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_15));
        }
        else if(img_id==16){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_16));
        }
        else if(img_id==17){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_17));
        }
        else if(img_id==18){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_18));
        }
        else if(img_id==19){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_19));
        }

        else if(img_id==20){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_20));
        }
        else if(img_id==21){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_21));
        }
        else if(img_id==22){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_22));
        }
        else if(img_id==23){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_23));
        }
        else if(img_id==24){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_24));
        }
        else if(img_id==25){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_25));
        }
        else if(img_id==26){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_26));
        }
        else if(img_id==27){
            bulletinImgView.setImageDrawable(getDrawable(R.drawable.image_27));
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