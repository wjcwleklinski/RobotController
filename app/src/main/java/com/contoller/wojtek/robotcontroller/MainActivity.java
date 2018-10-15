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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private CheckBox checkBoxX;
    private CheckBox checkBoxY;
    private CheckBox checkBoxZ;

    private SensorManager sensorManager;
    private Sensor orientationSensor;

    private float[] bufferX = {0,0,0,0};
    private float[] bufferY = {0,0,0,0};
    private float[] bufferZ = {0,0,0,0};

    float filteredX = 0;
    float filteredY = 0;
    float filteredZ = 0;

    float finalX = 0;
    float finalY = 0;
    float finalZ = 0;

    private float[] orientation = new float[3];

    float xPressed = 0;
    float yPressed = 0;
    float zPressed = 0;


    private boolean isJogPressed = false;
    private boolean xChecked = false;
    private boolean yChecked = false;
    private boolean zChecked = false;

    private static final int BUFFER = 8;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** hide actionBar, fullscreen commented */
        getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        Log.i("Debug", "onCreate method called.");

        initViews();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        jogButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // touched down
                if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    Log.i("Info", "Action down.");
                    isJogPressed = true;
                    view.setBackgroundColor(0xFF941212);
                    xPressed = filteredX;
                    yPressed = filteredY;
                    zPressed = filteredZ;

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

        checkBoxX.setOnCheckedChangeListener(checkListener);
        checkBoxY.setOnCheckedChangeListener(checkListener);
        checkBoxZ.setOnCheckedChangeListener(checkListener);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Debug", "onResume method called.");

        /** defining orientation sensor */
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        /** checking if sensor is available and registering onChange listener */
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

    /**
     * SensorEventListener interface method
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

// UMIARKOWANIE ŹLE TO JEST
        //Log.i("Info", "Sensor changed.");
        orientation = sensorEvent.values;

        angleX.setText(String.valueOf(orientation[0]));
        angleY.setText(String.valueOf(orientation[1]));
        angleZ.setText(String.valueOf(orientation[2]));


        if(isJogPressed) {
            finalX = (filteredX - xPressed) / bufferX.length;
            finalY = (filteredY - yPressed) / bufferY.length;
            finalZ = (filteredZ- zPressed) / bufferZ.length;

        } else {
            finalX = 0;
            finalY = 0;
            finalZ = 0;
        }

        if (xChecked)
            finalX = 0;
        if (yChecked)
            finalY = 0;
        if (zChecked)
            finalZ = 0;

        aberrationX.setText(String.valueOf(Math.round(finalX)));
        aberrationY.setText(String.valueOf(Math.round(finalY)));
        aberrationZ.setText(String.valueOf(Math.round(finalZ)));

        filteredX += orientation[0];
        filteredY += orientation[1];
        filteredZ += orientation[2];

        System.arraycopy(bufferX,1, bufferX, 0, bufferX.length - 1);
        System.arraycopy(bufferY,1, bufferY, 0, bufferY.length - 1);
        System.arraycopy(bufferZ,1, bufferZ, 0, bufferZ.length - 1);

        bufferX[bufferX.length - 1] = orientation[0];
        bufferY[bufferY.length - 1] = orientation[1];
        bufferZ[bufferZ.length - 1] = orientation[2];



        filteredX -= bufferX[0];
        filteredY -= bufferY[0];
        filteredZ -= bufferZ[0];

    }

    /**
     * SensorEventListener interface method
     * @param sensor
     * @param i
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i("Info", "Accuracy changed.");
    }

    /** listener for checkboxes */
    CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
            Log.i("Info", (String)buttonView.getTag());
            Log.i("Info", String.valueOf(isChecked));

            if( (((String)buttonView.getTag()).equals("x")) )
                xChecked = isChecked;

            if( (((String)buttonView.getTag()).equals("y")) )
                yChecked = isChecked;

            if( (((String)buttonView.getTag()).equals("z")) )
                zChecked = isChecked;
        }
    };


    /**
     * Views initialization
     */
    private void initViews() {
        angleX = findViewById(R.id.angleX);
        angleY = findViewById(R.id.angleY);
        angleZ = findViewById(R.id.angleZ);
        aberrationX = findViewById(R.id.aberrationX);
        aberrationY = findViewById(R.id.aberrationY);
        aberrationZ = findViewById(R.id.aberrationZ);
        jogButton = findViewById(R.id.jogButton);
        checkBoxX = findViewById(R.id.checkBoxX);
        checkBoxY = findViewById(R.id.checkBoxY);
        checkBoxZ = findViewById(R.id.checkBoxZ);

    }






}
