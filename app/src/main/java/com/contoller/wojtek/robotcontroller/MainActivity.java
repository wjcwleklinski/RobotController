package com.contoller.wojtek.robotcontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView angleX;
    private TextView angleY;
    private TextView angleZ;
    private TextView aberrationX;
    private TextView aberrationY;
    private TextView aberrationZ;
    private Button jogButton;

    private SensorManager sensorManager;
    private Sensor orientationSensor;

    private float[] orientation = new float[3];

    private float x0 = 0, y0 = 0, z0 = 0;

    private boolean isJogPressed = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Debug", "onCreate method called.");
        setContentView(R.layout.activity_main);
        initElements();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        jogButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // touched down
                if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    Log.i("Info", "Action down.");
                    isJogPressed = true;
                    view.setBackgroundColor(0xFF941212);
                    x0 = orientation[0];
                    y0 = orientation[1];
                    z0 = orientation[2];
                }
                // released
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    Log.i("Info", "Action up.");
                    isJogPressed = false;
                    view.setBackgroundColor(0xFFFF2424);
                }



                return false;
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Debug", "onResume method called.");

        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if ( orientationSensor != null ) {
            Log.i("Info", "Orientation sensor available.");
            sensorManager.registerListener(this,orientationSensor,SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            Log.i("Info", "Orientation sensor not available.");
            Toast.makeText(this, "Hardware requirements not met.", Toast.LENGTH_LONG ).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.i("Info", "Sensor changed.");
        orientation = sensorEvent.values;
        angleX.setText(String.valueOf(orientation[0]));
        angleY.setText(String.valueOf(orientation[1]));
        angleZ.setText(String.valueOf(orientation[2]));

        if (isJogPressed) {
            aberrationX.setText(String.valueOf(orientation[0] - x0));
            aberrationY.setText(String.valueOf(orientation[1] - y0));
            aberrationZ.setText(String.valueOf(orientation[2] - z0));
        } else {

            aberrationX.setText("0.0");
            aberrationY.setText("0.0");
            aberrationZ.setText("0.0");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i("Info", "Accuracy changed.");
    }

    public void initElements() {
        angleX = findViewById(R.id.angleX);
        angleY = findViewById(R.id.angleY);
        angleZ = findViewById(R.id.angleZ);
        aberrationX = findViewById(R.id.aberrationX);
        aberrationY = findViewById(R.id.aberrationY);
        aberrationZ = findViewById(R.id.aberrationZ);
        jogButton = findViewById(R.id.jogButton);
    }


}
