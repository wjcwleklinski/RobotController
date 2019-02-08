package com.contoller.wojtek.robotcontroller;

import android.util.Log;

public class Filter {

    private final int X = 0;
    private final int Y = 1;
    private final int Z = 2;

    private String TAG = "Filter";
    //-----moving average-----///
    private int inputArraySize;
    private final int BUFFERSIZE = 4;
    private float[] bufferX = new float[BUFFERSIZE];
    private float[] bufferY = new float[BUFFERSIZE];
    private float[] bufferZ = new float[BUFFERSIZE];
    private float xSum = 0;
    private float ySum = 0;
    private float zSum = 0;

    //---- alfa-beta-----///
    private float[] ypri = {0,0,0};
    private float[] ypost = {0,0,0};
    private float[] vpri= {0,0,0};
    private float[] vpost = {0,0,0};
    private float alfa = 0.3f;
    private float beta = 0.05f;
    private float[] ret = {0,0,0};


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

    public float[] alfaBeta(float[] input, float dt) {
        if (dt < 0.001)
            dt = 0.001f;
        ypri[X] = ypost[X] + vpost[X] * dt;
        ypri[Y] = ypost[Y] + vpost[Y] * dt;
        ypri[Z] = ypost[Z] + vpost[Z] * dt;

        vpri[X] = vpost[X];
        vpri[Y] = vpost[Y];
        vpri[Z] = vpost[Z];

        ypost[X] = ypri[X] + alfa * (input[X] - ypri[X]);
        ypost[Y] = ypri[Y] + alfa * (input[Y] - ypri[Y]);
        ypost[Z] = ypri[Z] + alfa * (input[Z] - ypri[Z]);

        vpost[X] = vpri[X] + beta * (input[X] - ypri[X]) / dt;
        vpost[Y] = vpri[Y] + beta * (input[Y] - ypri[Y]) / dt;
        vpost[Z] = vpri[Z] + beta * (input[Z] - ypri[Z]) / dt;

        Log.i(TAG, String.valueOf(ypost[X]));
        return ypost;
    }

    public void startFiltration(){

    }
}
