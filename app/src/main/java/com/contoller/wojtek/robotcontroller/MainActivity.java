package com.contoller.wojtek.robotcontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import no.hials.crosscom.networking.CrossComClient;
import no.hials.crosscom.networking.Request;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView angleX;
    private TextView angleY;
    private TextView angleZ;
    private TextView aberrationX;
    private TextView aberrationY;
    private TextView aberrationZ;
    private Button jogButton;
    public CheckBox checkBoxX;
    public CheckBox checkBoxY;
    public CheckBox checkBoxZ;
    private SeekBar seekBar;
    private TextView seekBarTextView;
    private ImageView imageX;
    private ImageView imageY;
    private ImageView imageZ;
    private ImageView imageMinusX;
    private ImageView imageMinusY;
    private ImageView imageMinusZ;

    private SensorManager sensorManager;

    private final String TAG = "Main";
    private final int DEAD_ZONE = 2;

    private float seekBarProgress = 1;

    //--------------------------------------
    private float[] mAccelerometerReading = new float[3];
    private float[] mMagnetometerReading = new float[3];
    private float[] mGyroscopeReading = new float[3];


    private float[] mRotationMatrix = new float[9];
    private float[] mOrientationAngles = new float[3];


    private float[] pressedAngles = {0,0,0};
    private float[] anglesToSend = {0,0,0};
    private double x = 0, y = 0, z = 0, step = 1;
    private String IP;
    private int port;
    private boolean preparedtoSend = false;
    float[] validatedAngles = {0,0,0};

    //--------------------------------------------------
    AngleThread angleThread;
    Thread connectionThread;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* hide actionBar, fullscreen commented */
        getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        /* getting ip and port from menu activity */
        Intent starterIntent = getIntent();
        IP = starterIntent.getStringExtra("IP");
        port = starterIntent.getIntExtra("Port", 7000);
        Log.i(TAG, "onCreate method called.");

        initViews();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);



    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume method called.");


        /* Registering sensors */
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
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

        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        } else {
            Log.i(TAG, "Gyroscope not available.");
        }

        angleThread = new AngleThread(sensorManager, IP, port);
        angleThread.start();
        //connectionThread = new Thread(new KukaThread(IP, Port));
        //connectionThread.start();
        refreshUI();


    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        angleThread.interrupt();
        //connectionThread.interrupt();
    }

    /**
     * SensorEventListener interface method
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if(sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
            System.arraycopy(sensorEvent.values,0, mAccelerometerReading, 0,mAccelerometerReading.length);
            angleThread.setAccReading(mAccelerometerReading);
        }
        else if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(sensorEvent.values, 0 ,mMagnetometerReading, 0, mMagnetometerReading.length);
            angleThread.setMagnetReading(mMagnetometerReading);
        }
        else if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(sensorEvent.values, 0, mGyroscopeReading, 0, mGyroscopeReading.length);
            angleThread.setGyroReading(mGyroscopeReading);
        }

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
        jogButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

                // touched down
                if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "Action down.");

                    pressedAngles = angleThread.getFilteredAngles();
                    angleThread.setJogButtonPressed(true);
                    //angleThread.setPressedAngles(pressedAngles);
                    vibrator.vibrate(200);

                }
                // released
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    Log.i(TAG, "Action up.");
                    angleThread.setJogButtonPressed(false);
                }

                return false;
            }

        });
        checkBoxX = findViewById(R.id.checkBoxX);
        checkBoxY = findViewById(R.id.checkBoxY);
        checkBoxZ = findViewById(R.id.checkBoxZ);
        seekBar = findViewById(R.id.seekBar);
        seekBarTextView = findViewById(R.id.seekBarTextView);
        imageX = findViewById(R.id.imageX);
        imageY = findViewById(R.id.imageY);
        imageZ = findViewById(R.id.imageZ);
        imageMinusX = findViewById(R.id.imageMinusX);
        imageMinusY = findViewById(R.id.imageMinusY);
        imageMinusZ = findViewById(R.id.imageMinusZ);

        seekBar.setProgress(1);
        seekBar.setMax(100);
        seekBarTextView.setText(String.valueOf(1));

        checkBoxX.setChecked(false);
        checkBoxY.setChecked(false);
        checkBoxZ.setChecked(false);

        CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()) {
                    if(compoundButton.getId() == R.id.checkBoxX) {
                        angleThread.setXCheckboxState(true);
                    }
                    if(compoundButton.getId() == R.id.checkBoxY) {
                        angleThread.setYCheckboxState(true);
                    }
                    if(compoundButton.getId() == R.id.checkBoxZ) {
                        angleThread.setZCheckboxState(true);
                    }
                } else {
                    if(compoundButton.getId() == R.id.checkBoxX) {
                        angleThread.setXCheckboxState(false);
                    }
                    if(compoundButton.getId() == R.id.checkBoxY) {
                        angleThread.setYCheckboxState(false);
                    }
                    if(compoundButton.getId() == R.id.checkBoxZ) {
                        angleThread.setZCheckboxState(false);
                    }
                }
            }
        };
        checkBoxX.setOnCheckedChangeListener(checkBoxListener);
        checkBoxY.setOnCheckedChangeListener(checkBoxListener);
        checkBoxZ.setOnCheckedChangeListener(checkBoxListener);

        seekBar.getProgressDrawable()
                .setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                angleThread.setSeekbarProgress(seekBar.getProgress());
                seekBarTextView.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    private void refreshUI() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                    try {
                        angleX.setText(String.valueOf(Math.round(Math.toDegrees(angleThread.getFilteredAngles()[2]))));
                        angleY.setText(String.valueOf(Math.round(Math.toDegrees(angleThread.getFilteredAngles()[1]))));
                        angleZ.setText(String.valueOf(Math.round(Math.toDegrees(angleThread.getFilteredAngles()[0]))));

                        //validatedAngles = angleThread.getValidatedAngles(); //cant do that - synchro issues

                        validatedAngles[0] = Math.round(Math.toDegrees(angleThread.getValidatedAngles()[0]));
                        validatedAngles[1] = Math.round(Math.toDegrees(angleThread.getValidatedAngles()[1]));
                        validatedAngles[2] = Math.round(Math.toDegrees(angleThread.getValidatedAngles()[2]));

                        aberrationX.setText(String.valueOf(validatedAngles[2]));
                        aberrationY.setText(String.valueOf(validatedAngles[1]));
                        aberrationZ.setText(String.valueOf(validatedAngles[0]));

                        if (validatedAngles[2] > DEAD_ZONE) {
                            imageX.setColorFilter(imageX.getContext().getResources().getColor(R.color.axisHighlight), PorterDuff.Mode.SRC_ATOP);
                            imageMinusX.setColorFilter(imageMinusX.getContext().getResources().getColor(R.color.axisDefault), PorterDuff.Mode.SRC_ATOP);
                        } else if (validatedAngles[2] < -DEAD_ZONE) {
                            imageX.setColorFilter(imageX.getContext().getResources().getColor(R.color.axisDefault), PorterDuff.Mode.SRC_ATOP);
                            imageMinusX.setColorFilter(imageMinusX.getContext().getResources().getColor(R.color.axisHighlight), PorterDuff.Mode.SRC_ATOP);
                        } else {
                            imageX.setColorFilter(imageX.getContext().getResources().getColor(R.color.axisDefault), PorterDuff.Mode.SRC_ATOP);
                            imageMinusX.setColorFilter(imageMinusX.getContext().getResources().getColor(R.color.axisDefault), PorterDuff.Mode.SRC_ATOP);
                        }

                        if (validatedAngles[1] > DEAD_ZONE) {
                            imageY.setColorFilter(imageY.getContext().getResources().getColor(R.color.axisHighlight), PorterDuff.Mode.SRC_ATOP);
                            imageMinusY.setColorFilter(imageMinusY.getContext().getResources().getColor(R.color.axisDefault), PorterDuff.Mode.SRC_ATOP);
                        } else if (validatedAngles[1] < -DEAD_ZONE) {
                            imageY.setColorFilter(imageY.getContext().getResources().getColor(R.color.axisDefault), PorterDuff.Mode.SRC_ATOP);
                            imageMinusY.setColorFilter(imageMinusY.getContext().getResources().getColor(R.color.axisHighlight), PorterDuff.Mode.SRC_ATOP);
                        } else {
                            imageY.setColorFilter(imageY.getContext().getResources().getColor(R.color.axisDefault), PorterDuff.Mode.SRC_ATOP);
                            imageMinusY.setColorFilter(imageMinusY.getContext().getResources().getColor(R.color.axisDefault), PorterDuff.Mode.SRC_ATOP);
                        }

                        if (validatedAngles[0] > DEAD_ZONE) {
                            imageZ.setColorFilter(imageZ.getContext().getResources().getColor(R.color.axisHighlight), PorterDuff.Mode.SRC_ATOP);
                            imageMinusZ.setColorFilter(imageMinusZ.getContext().getResources().getColor(R.color.axisDefault), PorterDuff.Mode.SRC_ATOP);
                        } else if (validatedAngles[0] < -DEAD_ZONE) {
                            imageZ.setColorFilter(imageZ.getContext().getResources().getColor(R.color.axisDefault), PorterDuff.Mode.SRC_ATOP);
                            imageMinusZ.setColorFilter(imageMinusZ.getContext().getResources().getColor(R.color.axisHighlight), PorterDuff.Mode.SRC_ATOP);
                        } else {
                            imageZ.setColorFilter(imageZ.getContext().getResources().getColor(R.color.axisDefault), PorterDuff.Mode.SRC_ATOP);
                            imageMinusZ.setColorFilter(imageMinusZ.getContext().getResources().getColor(R.color.axisDefault), PorterDuff.Mode.SRC_ATOP);
                        }
                    } catch (Exception ex) {
                        Log.i("RefreshUI", "Exception in refreshUI.");
                    }
                    handler.postDelayed(this, 200);
            }
        });
    }

    public class KukaThread implements Runnable {

        private String IP;
        private int port;
        public KukaThread(String IP, int port) {
            this.IP = IP;
            this.port = port;

        }

        @Override
        public void run() {
            CrossComClient client = null;

            try {

                while(!Thread.interrupted()) {
                    if (preparedtoSend) {
                        client = new CrossComClient(IP, port);
                        client.sendRequest(new Request(1, "MYPOS", "{X " + x + ",Y " + y + ",Z " + z + "}"));
                        Log.i("KukaThread", "MYPOS: " + "{X " + x + ",Y " + y + ",Z " + z + "}");
                        client.sendRequest(new Request(0, "$OV_PRO", String.valueOf(seekBar.getProgress())));
                        Log.i("KukaThread", "$OV_PRO" + String.valueOf(seekBar.getProgress()));
                    }
                }
            } catch (Exception ex) {
                Log.i("KukaThread", "Unable to connect or send request.");
            } finally {
                try {
                    client.close();
                } catch (Exception ex) {
                    Log.i("KukaThread", "Unable to close client.");
                }
            }
        }
    }

}
