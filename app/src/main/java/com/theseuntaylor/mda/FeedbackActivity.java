package com.theseuntaylor.mda;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class FeedbackActivity extends AppCompatActivity {

    Button btn_yesFall, btn_noFall;
    ProgressBar pb_timer;
    TextView tv_timer;

    private Handler emergencyResponseHandler = new Handler();
    Runnable emergencyResponseRunnable;

    private SharedPreferences sharedPreferences;

    int i = 30;
    CountDownTimer mCountDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        btn_yesFall = findViewById(R.id.btn_yesFall);
        btn_noFall = findViewById(R.id.btn_noFall);
        pb_timer = findViewById(R.id.progressbar);
        tv_timer = findViewById(R.id.tv_timer);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_file_name), Context.MODE_PRIVATE);

        btn_yesFall.setOnClickListener(view -> phoneCallCheck());
        btn_noFall.setOnClickListener(view -> {
            this.finish();
            emergencyResponseHandler.removeCallbacksAndMessages(null);
        });

        initiateCountdown();

    }

    private void phoneCallCheck() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(FeedbackActivity.this, "Permission Not Granted ", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, 46);
        } else {
            final String[] PERMISSIONS_STORAGE = {Manifest.permission.CALL_PHONE};
            ActivityCompat.requestPermissions(FeedbackActivity.this, PERMISSIONS_STORAGE, 9);
            initiateEmergencyActions();
        }
    }

    private void initiateEmergencyActions() {
        String phone_number = sharedPreferences.getString(getString(R.string.trusted_contact_number_key), null);

        // Getting instance of Intent with action as ACTION_CALL
        Intent phone_intent = new Intent(Intent.ACTION_CALL);

        // Set data of Intent through Uri by parsing phone number
        phone_intent.setData(Uri.parse("tel:" + phone_number));

        // start Intent
        startActivity(phone_intent);
    }

    private void initiateCountdown() {
        emergencyResponseHandler = new Handler();
        emergencyResponseRunnable = this::initiateEmergencyActions;

        emergencyResponseHandler.postDelayed(emergencyResponseRunnable, 31000);

        pb_timer.setProgress(100);
        mCountDownTimer = new CountDownTimer(31000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                i--;
                tv_timer.setText(String.valueOf(i));
                pb_timer.setProgress((int) i * 100 / (31000 / 1000));
            }

            @Override
            public void onFinish() {
                //Do what you want
                i--;
                pb_timer.setProgress(0);
            }
        };
        mCountDownTimer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        emergencyResponseHandler.removeCallbacksAndMessages(null);
    }
}
