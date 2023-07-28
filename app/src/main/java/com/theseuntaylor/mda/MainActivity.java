package com.theseuntaylor.mda;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.transition.Transition;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ImageButton imageButton;
    private TextView currentTransition;
    private Viewport viewport;

    private Spinner spinner;
    private Accelerometer accelerometer;

    private List<ActivityTransition> activityTransitionList;

    LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

    private final Handler handler = new Handler();
    Runnable runnable;

    long startTime = System.currentTimeMillis();

    private boolean isPaused = false;
    String actionName = "";

    private final boolean runningQOrLater = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);

    private TransitionsReceiver mTransitionReceiver;

    private boolean activityTrackingEnabled;

    private Utils utils = new Utils();

    private final String TRANSITIONS_RECEIVER_ACTION = "com.theseuntaylor.mda_TRANSITIONS_RECEIVER_ACTION";
    private PendingIntent mActivityTransitionsPendingIntent;

    private static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.WALKING:
                return "WALKING";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.STILL:
                return "STILL";
            default:
                return "UNKNOWN";
        }
    }

    private static String toTransitionType(int transitionType) {
        switch (transitionType) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "ENTER";
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageButton = findViewById(R.id.button_toggleIsPaused);
        currentTransition = findViewById(R.id.tv_currentTransition);
        spinner = findViewById(R.id.spinner_actions);

        activityTrackingEnabled = false;

        activityTransitionList = new ArrayList<>();

        activityTransitionList.add(utils.getActivityTransition(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER));
        activityTransitionList.add(utils.getActivityTransition(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT));
        activityTransitionList.add(utils.getActivityTransition(DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_ENTER));
        activityTransitionList.add(utils.getActivityTransition(DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_EXIT));
        activityTransitionList.add(utils.getActivityTransition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER));
        activityTransitionList.add(utils.getActivityTransition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_EXIT));

        Intent intent = new Intent(TRANSITIONS_RECEIVER_ACTION);

        mActivityTransitionsPendingIntent = PendingIntent
                .getBroadcast(
                        MainActivity.this,
                        100, intent,
                        PendingIntent.FLAG_MUTABLE
                );

        mTransitionReceiver = new TransitionsReceiver();

        if (activityRecognitionPermissionApproved()) {
            enableActivityTransitions();
        } else {
            ActivityCompat
                    .requestPermissions(
                            this,
                            new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                            45
                    );
        }

        toggleIsPaused();

        accelerometer = new Accelerometer(this);

        handleSpinnerSelection();

        GraphView graphView = findViewById(R.id.graphView_xAxis);
        viewport = graphView.getViewport();
        viewport.setScrollable(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);

        graphView.addSeries(series);

    }


    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mTransitionReceiver, new IntentFilter(TRANSITIONS_RECEIVER_ACTION));
    }

    private boolean activityRecognitionPermissionApproved() {
        if (runningQOrLater) {
            return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACTIVITY_RECOGNITION
            );
        } else {
            return true;
        }
    }

    private void enableActivityTransitions() {
        ActivityTransitionRequest request = new ActivityTransitionRequest(activityTransitionList);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat
                    .requestPermissions(
                            this,
                            new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                            45
                    );
            return;
        }

        Task<Void> task = ActivityRecognition.getClient(this)
                .requestActivityTransitionUpdates(
                        request,
                        mActivityTransitionsPendingIntent
                );

        task.addOnSuccessListener(
                res -> {
                    activityTrackingEnabled = true;
                    currentTransition.setText(getString(R.string.activityTransitionsEnabled));
                }
        ).addOnFailureListener(
                e -> currentTransition.setText(getString(R.string.errorEnablingActivityTransitions))
        );
    }


    private void disableActivityTransitions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        ActivityRecognition.getClient(this).removeActivityTransitionUpdates(mActivityTransitionsPendingIntent)
                .addOnSuccessListener(
                        aVoid -> {
                            activityTrackingEnabled = false;
                            currentTransition.setText(getString(R.string.activityTransitionsDisabled));
                        }
                ).addOnFailureListener(
                        e -> currentTransition.setText(getString(R.string.errorDisablingActivityTransitions))
                );
    }

    private void toggleIsPaused() {
        imageButton.setOnClickListener(v -> {
            if (!isPaused) {
                accelerometer.unregister();

                imageButton.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_resume));
            } else {
                accelerometer.register();
                imageButton.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_pause));

            }
            isPaused = !isPaused;
        });
    }

    void handleSpinnerSelection() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actionName = spinner.getSelectedItem().toString();
                Log.e("Spinner Selection", actionName);
                actionName = actionName.replace(' ', '-');

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        createAccelerometerListener();
                        handler.postDelayed(this, 1000); // Update every 1 second
                    }
                };

                handler.post(runnable);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private double calculateAcceleration(float tx, float ty, float tz) {
        float tx_squared = tx * tx;
        float ty_squared = ty * ty;
        float tz_squared = tz * tz;

        return Math.sqrt(tx_squared + ty_squared + tz_squared);
    }

    private void createAccelerometerListener() {
        accelerometer.setListener((tx, ty, tz) -> {

            double timeInSeconds = (System.currentTimeMillis() - startTime) / 1000.0;

            series.appendData(new DataPoint(timeInSeconds, calculateAcceleration(tx, ty, tz)), true, 1000);

            // Set fixed viewport bounds on X-axis
            viewport.setMinX(timeInSeconds - 30); // Display the last 30 seconds
            viewport.setMaxX(timeInSeconds);


            String content = "\n" + timeInSeconds + ": " + tx + ", " + ty + ", " + tz;
            writeToFile(content, actionName);


        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        accelerometer.register();
    }

    @Override
    protected void onPause() {
        if (activityTrackingEnabled) disableActivityTransitions();

        super.onPause();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mTransitionReceiver);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (activityRecognitionPermissionApproved() & !activityTrackingEnabled)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                enableActivityTransitions();
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void writeToFile(String content, String sActionName) {
        try {
            FileOutputStream outputStream = getApplicationContext().openFileOutput("log-for-" + sActionName + "-" + getCurrentTime() + ".txt", Context.MODE_APPEND);
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCurrentTime() {
        Date currentDate = new Date(startTime);

        // Create a SimpleDateFormat object to define the desired date and time format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);

        // Format the date as per the defined format
        return dateFormat.format(currentDate);
    }

    /**
     * Handles intents from from the Transitions API.
     */
    public class TransitionsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("TAG", "onReceive(): " + intent);

            if (!TextUtils.equals(TRANSITIONS_RECEIVER_ACTION, intent.getAction())) {
                return;
            }

            // TODO: Extract activity transition information from listener.
            if (ActivityTransitionResult.hasResult(intent)) {

                ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);

                assert result != null;
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {

                    String info = "Transition: " + toActivityString(event.getActivityType()) +
                            " (" + toTransitionType(event.getTransitionType()) + ")" + "   " +
                            new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());

                    currentTransition.setText(info);
                    // have a function that changes the textview of the screen for instance to the action.
                    // printToScreen(info);
                }
            }
        }
    }
}

