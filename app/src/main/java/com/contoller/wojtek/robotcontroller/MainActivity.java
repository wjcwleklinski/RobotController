package com.contoller.wojtek.robotcontroller;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Debug", "onCreate method called.");
        setContentView(R.layout.activity_main);

        angleX = findViewById(R.id.angleX);
        angleY = findViewById(R.id.angleY);
        angleZ = findViewById(R.id.angleZ);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i("Info", "Accuracy changed.");
    }


}
