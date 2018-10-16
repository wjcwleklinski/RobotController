package com.contoller.wojtek.robotcontroller;

import android.util.Log;

public class Filter {

    private String TAG = "Filter";
    private int inputArraySize;
    private final int BUFFERSIZE = 4;
    private float[] bufferX = new float[BUFFERSIZE];
    private float[] bufferY = new float[BUFFERSIZE];
    private float[] bufferZ = new float[BUFFERSIZE];
    private float xSum = 0;
    private float ySum = 0;
    private float zSum = 0;

    public Filter(int inputArraySize){
        this.inputArraySize = inputArraySize;
        nullifyBuffers();
    }

    public float[] movingAverage(float[] input) {

        if(input.length != inputArraySize) {
            Log.i(TAG, "Input size is not correct.");
            return  null;
        }

        System.arraycopy(bufferX, 1, bufferX, 0, bufferX.length - 1);
        System.arraycopy(bufferY, 1, bufferY, 0, bufferY.length - 1);
        System.arraycopy(bufferZ, 1, bufferZ, 0, bufferZ.length - 1);

        bufferX[BUFFERSIZE - 1] = input[0];
        bufferY[BUFFERSIZE - 1] = input[1];
        bufferZ[BUFFERSIZE - 1] = input[2];

        xSum += input[0];
        ySum += input[1];
        zSum += input[2];

        float x = xSum / BUFFERSIZE;
        float y = ySum / BUFFERSIZE;
        float z = zSum / BUFFERSIZE;
        float[] xyz = {xSum / BUFFERSIZE, ySum / BUFFERSIZE, zSum / BUFFERSIZE};

        xSum -= bufferX[0];
        ySum -= bufferY[0];
        zSum -= bufferZ[0];

        return xyz;
    }

    private void nullifyBuffers() {
        for (int i = 0; i < BUFFERSIZE; i++) {
            bufferX[i] = 0;
            bufferY[i] = 0;
            bufferZ[i] = 0;

        }
    }

    public void startFiltration(){

    }
}
