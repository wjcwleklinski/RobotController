package com.contoller.wojtek.robotcontroller;


import android.opengl.Matrix;

public class MyMatrix {

    public static float[][] sum(float[][] in1, float[][] in2) {
        if( (in1.length != in2.length) || in1[0].length != in2.length )
            return null;
        float[][] result = new float[in1[0].length][in1.length];

        for(int r = 0; r < in1[0].length; r++) {
            for(int k = 0; k < in1.length; k++) {
                result[r][k] = in1[r][k] + in2[r][k];
            }
        }
        return result;

    }
}
