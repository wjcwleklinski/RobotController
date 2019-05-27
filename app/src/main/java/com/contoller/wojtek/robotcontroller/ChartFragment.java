package com.contoller.wojtek.robotcontroller;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.Arrays;


public class ChartFragment extends Fragment {


    private int fragmentId;
    private XYPlot graph;
    //private double[] measurements = new double[6];
    private Number[] seriesA1 = new Number[20];
    private Number[] seriesA2 = new Number[20];
    private Number[] seriesA3 = new Number[20];
    private Number[] seriesA4 = new Number[20];
    private Number[] seriesA5 = new Number[20];
    private Number[] seriesA6 = new Number[20];
    private int[] textViewIds = new int[6];
    private TextView A1,A2, A3, A4, A5, A6;
    private String unit = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentId = getArguments().getInt("Id", 0);
        textViewIds = getArguments().getIntArray("TextViewIds");
        unit = getArguments().getString("unit");
        initSeries();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View chartView = inflater.inflate(R.layout.torque_chart_fragment, container,
                false);
        graph = chartView.findViewById(fragmentId);
        A1 = chartView.findViewById(textViewIds[0]);
        A2 = chartView.findViewById(textViewIds[1]);
        A3 = chartView.findViewById(textViewIds[2]);
        A4 = chartView.findViewById(textViewIds[3]);
        A5 = chartView.findViewById(textViewIds[4]);
        A6 = chartView.findViewById(textViewIds[5]);

        return chartView;
    }

    public void setMeasurements(double[] measurements) {
        //this.measurements = measurements;
        //Log.i("Co przychodzi:", Thread.currentThread().getName()+ "  " + Arrays.toString(measurements));
        A1.setText("A1:\n" + String.format("%.2f", measurements[0]) + unit);
        A2.setText("A2:\n" + String.format("%.2f", measurements[1]) + unit);
        A3.setText("A3:\n" + String.format("%.2f", measurements[2]) + unit);
        A4.setText("A4:\n" + String.format("%.2f", measurements[3]) + unit);
        A5.setText("A5:\n" + String.format("%.2f", measurements[4]) + unit);
        A6.setText("A6:\n" + String.format("%.2f", measurements[5]) + unit);

        System.arraycopy(seriesA1, 1, seriesA1, 0, (seriesA1.length) - 1);
        System.arraycopy(seriesA2, 1, seriesA2, 0, (seriesA2.length) - 1);
        System.arraycopy(seriesA3, 1, seriesA3, 0, (seriesA3.length) - 1);
        System.arraycopy(seriesA4, 1, seriesA4, 0, (seriesA4.length) - 1);
        System.arraycopy(seriesA5, 1, seriesA5, 0, (seriesA5.length) - 1);
        System.arraycopy(seriesA6, 1, seriesA6, 0, (seriesA6.length) - 1);


        seriesA1[(seriesA1.length) - 1] = Math.round(measurements[0]);
        seriesA2[(seriesA2.length) - 1] = Math.round(measurements[1]);
        seriesA3[(seriesA3.length) - 1] = Math.round(measurements[2]);
        seriesA4[(seriesA4.length) - 1] = Math.round(measurements[3]);
        seriesA5[(seriesA5.length) - 1] = Math.round(measurements[4]);
        seriesA6[(seriesA6.length) - 1] = Math.round(measurements[5]);

        XYSeries torquesA1 = new SimpleXYSeries(Arrays.asList(seriesA1),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "A1");
        XYSeries torquesA2 = new SimpleXYSeries(Arrays.asList(seriesA2),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "A2");
        XYSeries torquesA3 = new SimpleXYSeries(Arrays.asList(seriesA3),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "A3");
        XYSeries torquesA4 = new SimpleXYSeries(Arrays.asList(seriesA4),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "A4");
        XYSeries torquesA5 = new SimpleXYSeries(Arrays.asList(seriesA5),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "A5");
        XYSeries torquesA6 = new SimpleXYSeries(Arrays.asList(seriesA6),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "A6");

        LineAndPointFormatter torquesA1Format = new LineAndPointFormatter(Color.RED, Color.RED, null, null);
        LineAndPointFormatter torquesA2Format = new LineAndPointFormatter(Color.GREEN, Color.GREEN, null, null);
        LineAndPointFormatter torquesA3Format = new LineAndPointFormatter(Color.YELLOW, Color.YELLOW, null, null);
        LineAndPointFormatter torquesA4Format = new LineAndPointFormatter(Color.BLUE, Color.BLUE, null, null);
        LineAndPointFormatter torquesA5Format = new LineAndPointFormatter(Color.MAGENTA, Color.MAGENTA, null, null);
        LineAndPointFormatter torquesA6Format = new LineAndPointFormatter(Color.WHITE, Color.WHITE, null, null);


        graph.clear();

        graph.addSeries(torquesA1, torquesA1Format);
        graph.addSeries(torquesA2, torquesA2Format);
        graph.addSeries(torquesA3, torquesA3Format);
        graph.addSeries(torquesA4, torquesA4Format);
        graph.addSeries(torquesA5, torquesA5Format);
        graph.addSeries(torquesA6, torquesA6Format);
        graph.redraw();

    }

    public void initSeries() {
        Arrays.fill(seriesA1, 0.);
    }
}
