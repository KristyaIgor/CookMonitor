package edi.md.cookmonitor;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * Created by Igor on 18.05.2020
 */

public class StartedActivity extends AppCompatActivity {
    ImageButton btnCookMonitor, btnOrderMonitor;
    SharedPreferences settingsPreference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_started);

        btnCookMonitor = findViewById(R.id.btn_cook_monitor);
        btnOrderMonitor = findViewById(R.id.btn_monitor_order);

        settingsPreference = getSharedPreferences("Settings", MODE_PRIVATE);
        int selectedModeWork = settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE);

        if(selectedModeWork == BaseEnum.NONE_SELECTED_MODE){
            requestMultiplePermissions();

            btnOrderMonitor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    settingsPreference.edit().putInt("WorkAs",BaseEnum.OrderMonitor).apply();
                    Intent startMain = new Intent(StartedActivity.this, MainActivity.class);
                    startActivity(startMain);
                    finish();
                }
            });
            btnCookMonitor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    settingsPreference.edit().putInt("WorkAs",BaseEnum.CookMonitor).apply();
                    Intent startMain = new Intent(StartedActivity.this, MainActivity.class);
                    startActivity(startMain);
                    finish();
                }
            });
        }
        else{
            Intent startMain = new Intent(this,MainActivity.class);
            startActivity(startMain);
            finish();
        }
    }
    public void requestMultiplePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.INTERNET,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE
                }, 12);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 12 && grantResults.length == 4) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StartedActivity.this, new String[]{Manifest.permission.INTERNET}, 1);
            } else if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StartedActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            } else if (grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StartedActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 1);
            } else if (grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StartedActivity.this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
