package com.contoller.wojtek.robotcontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.os.Build;
import android.support.annotation.RequiresApi;
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

    float finalX = 0;
    float finalY = 0;
    float finalZ = 0;

    float xPressed = 0;
    float yPressed = 0;
    float zPressed = 0;

    private final String TAG = "Info";

    private boolean isJogPressed = false;
    private boolean xChecked = false;
    private boolean yChecked = false;
    private boolean zChecked = false;

    //--------------------------------------
    private float[] mAccelerometerReading = new float[3];
    private float[] mMagnetometerReading = new float[3];

    private float[] mRotationMatrix = new float[9];
    private float[] mOrientationAngles = new float[3];  //4 according to SO

    private float[] filteredVals;

    //--------------------------------------------------
    Filter filter = new Filter(3);

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

                    xPressed = filteredVals[0];
                    yPressed = filteredVals[1];
                    zPressed = filteredVals[2];

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


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Debug", "onResume method called.");

        // defining sensors: accelerometer and magnetometer

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        } else {
            Log.i(TAG, "Accelerometer not available.");
        }

        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        } else {
            Log.i(TAG, "Magnetometer not available.");
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

        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(sensorEvent.values,0, mAccelerometerReading, 0,mAccelerometerReading.length);
        }
        else if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(sensorEvent.values, 0 ,mMagnetometerReading, 0, mMagnetometerReading.length);
        }

        updateAngles();

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

    private void updateAngles() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // getting orientation matrix
                SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);
                SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

                // displaying raw orientation
                angleX.setText(String.valueOf(Math.toDegrees(mOrientationAngles[0])));
                angleY.setText(String.valueOf(Math.toDegrees(mOrientationAngles[1])));
                angleZ.setText(String.valueOf(Math.toDegrees(mOrientationAngles[2])));

                // filtering angles
                filteredVals = filter.movingAverage(mOrientationAngles);

                // rad to deg conversion
                for(int i = 0; i < filteredVals.length; i++) {
                    filteredVals[i] = (float)Math.toDegrees(filteredVals[i]);
                }

                // if jog button is pressed, angles are measured from zero
                if(isJogPressed) {
                    finalX = Math.round(filteredVals[0] - xPressed);
                    finalY = Math.round(filteredVals[1] - yPressed);
                    finalZ = Math.round(filteredVals[2] - zPressed);
                } else {
                    finalX = 0;
                    finalY = 0;
                    finalZ = 0;
                }

                // checking checkboxes
                if (xChecked)
                    finalX = 0;
                if (yChecked)
                    finalY = 0;
                if (zChecked)
                    finalZ = 0;

                // displaying final angles
                aberrationX.setText(String.valueOf(finalX));
                aberrationY.setText(String.valueOf(finalY));
                aberrationZ.setText(String.valueOf(finalZ));
            }
        });
    }





}
