package com.contoller.wojtek.robotcontroller;

public class Filter {

    final int BUFFERSIZE = 4;
    float[] bufferX = new float[BUFFERSIZE];

    public float[] movingAverage(float[] input) {
        float filteredX = input[0];
        float filteredY = input[1];
        float filteredZ = input[2];





        return null;
    }
}
