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
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


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
    private TextView positionA1;
    private TextView positionA2;
    private TextView positionA3;
    private TextView positionA4;
    private TextView positionA5;
    private TextView positionA6;

    private SensorManager sensorManager;

    private final String TAG = "Main";
    private final int DEAD_ZONE = 2;
    private final char DEGREE_UNICODE = 0x00B0;
    private final int UI_REFRESH_MS = 300;

    //--------------------------------------
    private float[] mAccelerometerReading = new float[3];
    private float[] mMagnetometerReading = new float[3];
    private float[] mGyroscopeReading = new float[3];

    private String IP;
    private int port;

    float[] validatedAngles = {0,0,0};

    //--------------------------------------------------
    private AngleThread angleThread;
    private ChartFragment torqueChartFragment = new ChartFragment();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* hide actionBar, fullscreen commented */
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);
        /* getting ip and port from menu activity */
        Intent starterIntent = getIntent();
        IP = starterIntent.getStringExtra("IP");
        port = starterIntent.getIntExtra("Port", 7000);
        Log.i(TAG, "onCreate method called.");

        initViews();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        // prepare fragments
        Bundle args = new Bundle();
        args.putInt("Id", R.id.torque_graph);
        args.putIntArray("TextViewIds", new int[]{R.id.text_view_torque_A1, R.id.text_view_torque_A2,
                R.id.text_view_torque_A3, R.id.text_view_torque_A4, R.id.text_view_torque_A5, R.id.text_view_torque_A6});
        args.putString("unit", " Nm");
        torqueChartFragment.setArguments(args);



        //todo detach
        getSupportFragmentManager().beginTransaction()
                .add(R.id.chart_container, torqueChartFragment)
                .hide(torqueChartFragment)
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.i("Options menu", "Settings selected");
                return true;
            case R.id.show_torque_chart:
                getSupportFragmentManager().beginTransaction()
                        .show(torqueChartFragment)
                        .commit();
                return true;
            case R.id.hide_torque_chart:
                getSupportFragmentManager().beginTransaction()
                        .hide(torqueChartFragment)
                        .commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume method called.");


        /* Registering sensors */
        registerSensor(Sensor.TYPE_GRAVITY, SensorManager.SENSOR_DELAY_NORMAL);
        registerSensor(Sensor.TYPE_MAGNETIC_FIELD, SensorManager.SENSOR_DELAY_NORMAL);
        registerSensor(Sensor.TYPE_GYROSCOPE, SensorManager.SENSOR_DELAY_NORMAL);

        /* Starting angle thread */
        angleThread = new AngleThread("Angle Thread", IP, port);
        angleThread.start();

        /* Updating UI with UI_REFRESH_MS */
        refreshUI();


    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        angleThread.interrupt();
    }

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

    private void registerSensor(int type, int delay) {
        Sensor sensor = sensorManager.getDefaultSensor(type);
        if(sensor != null) {
            sensorManager.registerListener(this, sensor, delay);
        } else {
            Log.i("Sensor Registration", "Sensor unregistered");
        }
    }

    /**
     * Views and listeners initialization
     */
    private void initViews() {
        angleX = findViewById(R.id.angleX);
        angleY = findViewById(R.id.angleY);
        angleZ = findViewById(R.id.angleZ);
        aberrationX = findViewById(R.id.aberrationX);
        aberrationY = findViewById(R.id.aberrationY);
        aberrationZ = findViewById(R.id.aberrationZ);
        positionA1 = findViewById(R.id.textView_position_A1);
        positionA2 = findViewById(R.id.textView_position_A2);
        positionA3 = findViewById(R.id.textView_position_A3);
        positionA4 = findViewById(R.id.textView_position_A4);
        positionA5 = findViewById(R.id.textView_position_A5);
        positionA6 = findViewById(R.id.textView_position_A6);
        jogButton = findViewById(R.id.jogButton);
        jogButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

                // touched down
                if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "Action down.");

                    angleThread.setJogButtonPressed(true);
                    // high vibration interval cause Kalman filter unnecessary distortion
                    vibrator.vibrate(50);

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

        seekBar.setProgress(50);
        seekBar.setMax(100);
        seekBarTextView.setText(String.valueOf(50));

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
                //Log.i("UI Thread", Thread.currentThread().getName());
                    try {
                        // displaying raw angles
                        angleX.setText(String.valueOf(Math.round(Math.toDegrees(angleThread.getFilteredAngles()[2]))) + " " + DEGREE_UNICODE);
                        angleY.setText(String.valueOf(Math.round(Math.toDegrees(angleThread.getFilteredAngles()[1]))) + " " + DEGREE_UNICODE);
                        angleZ.setText(String.valueOf(Math.round(Math.toDegrees(angleThread.getFilteredAngles()[0]))) + " " + DEGREE_UNICODE);

                        // displaying validated angles
                        validatedAngles[0] = Math.round(Math.toDegrees(angleThread.getValidatedAngles()[0]));
                        validatedAngles[1] = Math.round(Math.toDegrees(angleThread.getValidatedAngles()[1]));
                        validatedAngles[2] = Math.round(Math.toDegrees(angleThread.getValidatedAngles()[2]));

                        aberrationX.setText(String.valueOf(validatedAngles[2]) + " " + DEGREE_UNICODE);
                        aberrationY.setText(String.valueOf(validatedAngles[1]) + " " + DEGREE_UNICODE);
                        aberrationZ.setText(String.valueOf(validatedAngles[0]) + " " + DEGREE_UNICODE);

                        // changing arrows color
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

                        // updating torque chart and its textViews
                        torqueChartFragment.setMeasurements(angleThread.getTorques());

                        positionA1.setText("A1:\n" + String.format("%.2f", angleThread.getActualAxisAngles()[0]) + " " + DEGREE_UNICODE);
                        positionA2.setText("A2:\n" + String.format("%.2f", angleThread.getActualAxisAngles()[1]) + " " + DEGREE_UNICODE);
                        positionA3.setText("A3:\n" + String.format("%.2f", angleThread.getActualAxisAngles()[2]) + " " + DEGREE_UNICODE);
                        positionA4.setText("A4:\n" + String.format("%.2f", angleThread.getActualAxisAngles()[3]) + " " + DEGREE_UNICODE);
                        positionA5.setText("A5:\n" + String.format("%.2f", angleThread.getActualAxisAngles()[4]) + " " + DEGREE_UNICODE);
                        positionA6.setText("A6:\n" + String.format("%.2f", angleThread.getActualAxisAngles()[5]) + " " + DEGREE_UNICODE);

                    } catch (Exception ex) {
                        Log.i("RefreshUI", "Exception in refreshUI.");
                        ex.getMessage();
                    }
                    handler.postDelayed(this, UI_REFRESH_MS);
            }
        });
    }




}
