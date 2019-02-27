package com.contoller.wojtek.robotcontroller;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/*
public class TorqueChartFragment extends Fragment implements Runnable{

    public GraphView torqueGraph;
    private static double xAxis = 0.;
    private LineGraphSeries<DataPoint> seriesA1 = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> seriesA2 = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> seriesA3 = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> seriesA4 = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> seriesA5 = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> seriesA6 = new LineGraphSeries<>();
    private DataPoint[] bufferA1 = new DataPoint[20];
    private DataPoint[] bufferA2 = new DataPoint[20];
    private DataPoint[] bufferA3 = new DataPoint[20];
    private DataPoint[] bufferA4 = new DataPoint[20];
    private DataPoint[] bufferA5 = new DataPoint[20];
    private DataPoint[] bufferA6 = new DataPoint[20];

    public TorqueChartFragment() {
        initBuffers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View chartView = inflater.inflate(R.layout.torque_chart_fragment, container,
                false);
        torqueGraph = chartView.findViewById(R.id.torque_graph);
        seriesA1.setColor(Color.GREEN);

        return chartView;
    }

    public void updateChart(double[] measurements) {

        System.arraycopy(bufferA1, 1, bufferA1, 0, bufferA1.length - 1);
        bufferA1[bufferA1.length - 1] = new DataPoint(bufferA1.length - 1, measurements[0]);
        seriesA1 = new LineGraphSeries<>(bufferA1);

        System.arraycopy(bufferA2, 1, bufferA2, 0, bufferA2.length - 1);
        bufferA2[bufferA2.length - 1] = new DataPoint(bufferA2.length - 1, measurements[1]);
        seriesA2 = new LineGraphSeries<>(bufferA2);

        System.arraycopy(bufferA3, 1, bufferA3, 0, bufferA3.length - 1);
        bufferA3[bufferA3.length - 1] = new DataPoint(bufferA3.length - 1, measurements[2]);
        seriesA3 = new LineGraphSeries<>(bufferA3);

        System.arraycopy(bufferA4, 1, bufferA4, 0, bufferA4.length - 1);
        bufferA4[bufferA4.length - 1] = new DataPoint(bufferA4.length - 1, measurements[3]);
        seriesA4 = new LineGraphSeries<>(bufferA4);

        System.arraycopy(bufferA5, 1, bufferA5, 0, bufferA5.length - 1);
        bufferA5[bufferA5.length - 1] = new DataPoint(bufferA5.length - 1, measurements[4]);
        seriesA5 = new LineGraphSeries<>(bufferA5);

        System.arraycopy(bufferA6, 1, bufferA6, 0, bufferA6.length - 1);
        bufferA6[bufferA6.length - 1] = new DataPoint(bufferA6.length - 1, measurements[5]);
        seriesA6 = new LineGraphSeries<>(bufferA6);

        torqueGraph.addSeries(seriesA1);
        torqueGraph.addSeries(seriesA2);
        torqueGraph.addSeries(seriesA3);
        torqueGraph.addSeries(seriesA4);
        torqueGraph.addSeries(seriesA5);
        torqueGraph.addSeries(seriesA6);
        //xAxis += 1;

    }

    private void initBuffers() {
        for(int i = 0; i < 20; i++) {
            bufferA1[i] = new DataPoint(i, 0);
            bufferA2[i] = new DataPoint(i, 0);
            bufferA3[i] = new DataPoint(i, 0);
            bufferA4[i] = new DataPoint(i, 0);
            bufferA5[i] = new DataPoint(i, 0);
            bufferA6[i] = new DataPoint(i, 0);
        }
    }
    public GraphView getGraph(){
        return torqueGraph;
    }

    @Override
    public void run() {

    }
}*/