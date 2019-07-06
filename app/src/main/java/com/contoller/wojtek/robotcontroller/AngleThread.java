package com.contoller.wojtek.robotcontroller;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.io.IOException;
import java.util.Arrays;

import no.hials.crosscom.CrossComClient;
import no.hials.crosscom.Callback;
import no.hials.crosscom.KRL.KRLVariable;
import no.hials.crosscom.KRL.structs.KRLPos;
import no.hials.crosscom.Request;


public class AngleThread extends Thread {

    private final int AXIS_COUNT = 3;
    private final int DEAD_ZONE = 2;

    private float[] utilRotationMatrix = new float[9];
    private float[] utilOrientationAngles = new float[3];

    private float[] accReading = new float[AXIS_COUNT];
    private float[] gyroReading = new float[AXIS_COUNT];
    private float[] magnetReading = new float[AXIS_COUNT];

    private float[] filteredAngles = new float[AXIS_COUNT];
    private float[] rawAngles = new float[AXIS_COUNT];

    private float[] pressedAngles = new float[AXIS_COUNT];
    private float[] validatedAngles = new float[AXIS_COUNT];

    private Filter filter = new Filter(3);

    private boolean xCheckboxState = false, yCheckboxState = false, zCheckboxState = false;
    private boolean jogButtonPressed = false;
    private double x = 0, y = 0, z = 0, step = 1;
    public boolean angleThreadReady = false;
    private String IP;
    private int port;
    private int seekbarProgress = 1;
    private KalmanFilter kalmanFilter = new KalmanFilter();
    private double[] actualTorques = new double[6];
    private double[] actualAxisAngles = new double[6];


    public AngleThread(String name, String IP, int port) {
        super(name);
        this.IP = IP;
        this.port = port;
        initArrays();
    }


    public void setAccReading(float[] accReading) {
        this.accReading = accReading;
    }

    public void setGyroReading(float[] gyroReading) {
        this.gyroReading = gyroReading;
    }

    public void setMagnetReading(float[] magnetReading) {
        this.magnetReading = magnetReading;
    }

    public float[] getFilteredAngles() { return  filteredAngles; }

    public float[] getValidatedAngles() { return  validatedAngles; }

    public float[] getRawAngles() { return  rawAngles; }

    public double[] getTorques() { return  actualTorques; }

    public double[] getActualAxisAngles() { return actualAxisAngles; }

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

        KukaConnectorThread kukaThread = new KukaConnectorThread("KukaConnectorThread", IP, port);
        kukaThread.start();

        while(!Thread.currentThread().isInterrupted()) {
            try {
                /*SensorManager.getRotationMatrix(utilRotationMatrix, gyroReading, accReading, magnetReading); //null instead of gyro?
                SensorManager.getOrientation(utilRotationMatrix, utilOrientationAngles);
                filteredAngles = filter.movingAverage(utilOrientationAngles);*/
                filteredAngles = filter.movingAverage(kalmanFilter.computeAngles(accReading, gyroReading, magnetReading));
                rawAngles = utilOrientationAngles; //utils are commented

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
            } catch (Exception e) {
                e.printStackTrace();
            }

            // kukaconnthread here
            if(kukaThread.connectionSuccessful) {
                kukaThread.setOffsetPosition(x, y, z);
                kukaThread.setPosition(validatedAngles);
                kukaThread.setOv_pro(Integer.toString(seekbarProgress));
                actualAxisAngles = kukaThread.getJointAngles();
                actualTorques = kukaThread.getTorques();
            }
//            try {
//                if(client!=null) {
//                    client.sendRequest(new Request(1, "MYPOS", "{X " + x + ",Y " + y + ",Z " + z + "}"));
//                    client.sendRequest(new Request(0, "$OV_PRO", Integer.toString(seekbarProgress)));
//                    actualTorques = client.readJointTorques(); // CHECK THIS - WILD VALS
//                    actualAxisAngles = client.readJointAngles();
//                }
//            } catch (Exception ex) {
//                Log.i("Kuka connection: ", "Unable to send request");
//            }
        }
//        try {
//            client.close();
//        } catch (Exception ex) {
//            Log.i("Kuka connection", "Unable to close client");
//        }
    }




    private void initArrays() {

        Arrays.fill(accReading, 0);
        Arrays.fill(gyroReading, 0);
        Arrays.fill(magnetReading, 0);

        Arrays.fill(filteredAngles, 0);
        Arrays.fill(rawAngles, 0);

        Arrays.fill(pressedAngles, 0);

        Arrays.fill(actualAxisAngles, 0);
        Arrays.fill(actualTorques, 0);
    }



}
