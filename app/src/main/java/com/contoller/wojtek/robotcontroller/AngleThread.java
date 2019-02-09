package com.contoller.wojtek.robotcontroller;

import android.hardware.SensorManager;

import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

import no.hials.crosscom.networking.CrossComClient;
import no.hials.crosscom.networking.Request;

public class AngleThread extends Thread {

    private final int BUFFER_SIZE = 10;
    private final int AXIS_COUNT = 3;
    private final int DEAD_ZONE = 2;

    private float[] utilRotationMatrix = new float[9];
    private float[] utilOrientationAngles = new float[3];

    private float[] accXBuffer = new float[BUFFER_SIZE];
    private float[] accYBuffer = new float[BUFFER_SIZE];
    private float[] accZBuffer = new float[BUFFER_SIZE];
    private float[] gyroXBuffer = new float[BUFFER_SIZE];
    private float[] gyroYBuffer = new float[BUFFER_SIZE];
    private float[] gyroZBuffer = new float[BUFFER_SIZE];
    private float[] magnetXBuffer = new float[BUFFER_SIZE];
    private float[] magnetYBuffer = new float[BUFFER_SIZE];
    private float[] magnetZBuffer = new float[BUFFER_SIZE];
    private float[] angleXBuffer = new float[BUFFER_SIZE];
    private float[] angleYBuffer = new float[BUFFER_SIZE];
    private float[] angleZBuffer = new float[BUFFER_SIZE];

    private float[] accReading = new float[AXIS_COUNT];
    private float[] gyroReading = new float[AXIS_COUNT];
    private float[] magnetReading = new float[AXIS_COUNT];

    private float[] filteredAngles = new float[AXIS_COUNT];
    private float[] rawAngles = new float[AXIS_COUNT];

    private float[] pressedAngles = new float[AXIS_COUNT];
    private float[] validatedAngles = new float[AXIS_COUNT];

    private SensorManager threadSensorManager;
    private Filter filter = new Filter(3);

    private boolean xCheckboxState = false, yCheckboxState = false, zCheckboxState = false;
    private boolean jogButtonPressed = false;
    private double x = 0, y = 0, z = 0, step = 1;
    public boolean angleThreadReady = false;
    private String IP;
    private int port;
    private int seekbarProgress = 1;


    public AngleThread(SensorManager sensorManager, String IP, int port) {
        this.threadSensorManager = sensorManager;
        this.IP = IP;
        this.port = port;
        initArrays();
    }


    public void setAccReading(float[] accReading) {
        this.accReading = accReading;
        System.arraycopy(accXBuffer, 1, accXBuffer, 0, BUFFER_SIZE - 1);
        System.arraycopy(accYBuffer, 1, accYBuffer, 0, BUFFER_SIZE - 1);
        System.arraycopy(accZBuffer, 1, accZBuffer, 0, BUFFER_SIZE - 1);

        accXBuffer[BUFFER_SIZE - 1] = accReading[2];
        accYBuffer[BUFFER_SIZE - 1] = accReading[1];
        accZBuffer[BUFFER_SIZE - 1] = accReading[0];
    }

    public void setGyroReading(float[] gyroReading) {
        this.gyroReading = gyroReading;
        System.arraycopy(gyroXBuffer, 1, gyroXBuffer, 0, BUFFER_SIZE - 1);
        System.arraycopy(gyroYBuffer, 1, gyroYBuffer, 0, BUFFER_SIZE - 1);
        System.arraycopy(gyroZBuffer, 1, gyroZBuffer, 0, BUFFER_SIZE - 1);

        gyroXBuffer[BUFFER_SIZE - 1] = gyroReading[2];
        gyroYBuffer[BUFFER_SIZE - 1] = gyroReading[1];
        gyroZBuffer[BUFFER_SIZE - 1] = gyroReading[0];
    }

    public void setMagnetReading(float[] magnetReading) {
        this.magnetReading = magnetReading;
        System.arraycopy(magnetXBuffer, 1, magnetXBuffer, 0, BUFFER_SIZE - 1);
        System.arraycopy(magnetYBuffer, 1, magnetYBuffer, 0, BUFFER_SIZE - 1);
        System.arraycopy(magnetZBuffer, 1, magnetZBuffer, 0, BUFFER_SIZE - 1);

        magnetXBuffer[BUFFER_SIZE - 1] = magnetReading[2];
        magnetYBuffer[BUFFER_SIZE - 1] = magnetReading[1];
        magnetZBuffer[BUFFER_SIZE - 1] = magnetReading[0];
    }

    public float[] getFilteredAngles() { return  filteredAngles; }

    public float[] getValidatedAngles() { return  validatedAngles; }

    public float[] getRawAngles() { return  rawAngles; }

    public void setXCheckboxState(boolean xCheckbox) {
        this.xCheckboxState = xCheckbox;
    }

    public void setYCheckboxState(boolean yCheckboxState) {
        this.yCheckboxState = yCheckboxState;
    }

    public void setZCheckboxState(boolean zCheckboxState) {
        this.zCheckboxState = zCheckboxState;
    }

    public void setJogButtonPressed(boolean isPressed) {
        Log.i("Jog button ", "Jog button pressed");

        this.jogButtonPressed = isPressed;
        if (isPressed)
            pressedAngles = filteredAngles;
        else
            Arrays.fill(pressedAngles, 0);
    }

    public void setSeekbarProgress(int inSeekbarProgress) {this.seekbarProgress = inSeekbarProgress;}

    public void run() {
        CrossComClient client = null;
        Log.i("AngleThread", "AngleThread started");
        try {
            client = new CrossComClient(IP, port);
            Log.i("Kuka connection", "Client created");
        } catch(IOException ex) {
            Log.i("Kuka connection", "Unable to create client");
        }

        while(!Thread.currentThread().isInterrupted()) {

            try {
                SensorManager.getRotationMatrix(utilRotationMatrix, gyroReading, accReading, magnetReading); //null instead of gyro?
                SensorManager.getOrientation(utilRotationMatrix, utilOrientationAngles);
                filteredAngles = filter.movingAverage(utilOrientationAngles);
                rawAngles = utilOrientationAngles;
                //Log.i("Pressed angles: ", Arrays.toString(pressedAngles));
                //Log.i("Filtered angles: ", Arrays.toString(filteredAngles));

                if (jogButtonPressed) {
                    if (xCheckboxState)
                        validatedAngles[2] = filteredAngles[2] - pressedAngles[2];
                    else
                        validatedAngles[2] = 0;
                    if (yCheckboxState)
                        validatedAngles[1] = filteredAngles[1] - pressedAngles[1];
                    else
                        validatedAngles[1] = 0;
                    if (zCheckboxState)
                        validatedAngles[0] = filteredAngles[0] - pressedAngles[0];
                    else
                        validatedAngles[0] = 0;
                } else {
                    validatedAngles[0] = 0;
                    validatedAngles[1] = 0;
                    validatedAngles[2] = 0;
                }
                //Log.i("Validated angles: ", Arrays.toString(validatedAngles));

                long roundedX = Math.round(Math.toDegrees(validatedAngles[2]));
                long roundedY = Math.round(Math.toDegrees(validatedAngles[1]));
                long roundedZ = Math.round(Math.toDegrees(validatedAngles[0]));
                if (roundedX > DEAD_ZONE) {
                    x = step;
                } else if (roundedX < -DEAD_ZONE) {
                    x = -step;
                } else {
                    x = 0;
                }

                if (roundedY > DEAD_ZONE) {
                    y = step;
                } else if (roundedY < -DEAD_ZONE) {
                    y = -step;
                } else {
                    y = 0;
                }

                if (roundedZ > DEAD_ZONE) {
                    z = step;
                } else if (roundedZ < -DEAD_ZONE) {
                    z = -step;
                } else {
                    z = 0;
                }
                angleThreadReady = true;
                //Log.i("Kuka connection", "MYPOS: " + "{X " + x + ",Y " + y + ",Z " + z + "}");
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                client.sendRequest(new Request(1, "MYPOS", "{X " + x + ",Y " + y + ",Z " + z + "}"));
                Log.i("Kuka connection", "MYPOS: " + "{X " + x + ",Y " + y + ",Z " + z + "}");
                client.sendRequest(new Request(0, "$OV_PRO", String.valueOf(seekbarProgress)));
                Log.i("Kuka connection", "$OV_PRO" + String.valueOf(seekbarProgress));

            } catch (Exception ex) {
                Log.i("Kuka connection: ", "Unable to send request");
            } /*finally {
                try {
                    client.close();
                } catch (Exception ex) {
                    Log.i("Kuka connection", "Unable to close client");
                }
            }*/
        }
        try {
            client.close();
        } catch (Exception ex) {
            Log.i("Kuka connection", "Unable to close client");
        }
    }




    private void initArrays() {
        Arrays.fill(accXBuffer, 0);
        Arrays.fill(accYBuffer, 0);
        Arrays.fill(accZBuffer, 0);
        Arrays.fill(gyroXBuffer, 0);
        Arrays.fill(gyroYBuffer, 0);
        Arrays.fill(gyroZBuffer, 0);
        Arrays.fill(magnetXBuffer, 0);
        Arrays.fill(magnetYBuffer, 0);
        Arrays.fill(magnetZBuffer, 0);

        Arrays.fill(angleXBuffer, 0);
        Arrays.fill(angleYBuffer, 0);
        Arrays.fill(angleZBuffer, 0);

        Arrays.fill(accReading, 0);
        Arrays.fill(gyroReading, 0);
        Arrays.fill(magnetReading, 0);

        Arrays.fill(filteredAngles, 0);
        Arrays.fill(rawAngles, 0);

        Arrays.fill(pressedAngles, 0);
    }

    private void bufferizeAngles(float[] inAngles) {
        System.arraycopy(angleXBuffer, 1, angleXBuffer, 0, BUFFER_SIZE - 1);

    }

    private float[] movingAverage(float[] xBuffer, float[] yBuffer, float[] zBuffer) {

        float[] filteredAngles = {0, 0, 0};
        for (int i = 0; i < xBuffer.length; i++) {
            filteredAngles[0] += xBuffer[i];
            filteredAngles[1] += yBuffer[i];
            filteredAngles[2] += zBuffer[i];
        }

        for(int i = 0; i < filteredAngles.length; i++) {
            filteredAngles[i] = filteredAngles[i] / filteredAngles.length;
        }


        return filteredAngles;
    }

}
