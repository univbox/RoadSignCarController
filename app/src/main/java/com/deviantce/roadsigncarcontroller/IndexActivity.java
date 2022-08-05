package com.deviantce.roadsigncarcontroller;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class IndexActivity extends AppCompatActivity {
    final int REQUEST_CODE = 1000;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        onCheckPermission();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onCheckPermission() {
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ) {
            requestPermissions(new String[]{ Manifest.permission.CAMERA,}, REQUEST_CODE);
        } else {
            startMainActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (requestCode == REQUEST_CODE) {
            boolean check_result = true;
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                startMainActivity();
            } else {
                Toast.makeText(this, "권한 설정 오류", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}