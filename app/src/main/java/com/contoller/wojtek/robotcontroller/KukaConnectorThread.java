package com.contoller.wojtek.robotcontroller;


import android.util.Log;
import java.io.IOException;
import no.hials.crosscom.CrossComClient;
import no.hials.crosscom.KRL.structs.KRLPos;
import no.hials.crosscom.Request;


public class KukaConnectorThread extends Thread {

    private String IP;
    private int port;
    private double x = 0., y = 0., z = 0.;
    private int ov_pro;
    private double[] torques;
    private double[] jointAngles;


    public KukaConnectorThread(String IP, int port) {
        this.IP = IP;
        this.port = port;
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setOv_pro(int ov_pro) {
        this.ov_pro = ov_pro;
    }

    public double[] getTorques() { return torques; }

    public double[] getJointAngles() { return jointAngles; }

    @Override
    public void run() {
        CrossComClient client = null;
        try {
            client = new CrossComClient(IP, port);
            Log.i("Kuka connection", "Client created");
            if (client != null) {
                while (!Thread.currentThread().isInterrupted()) {
                    client.sendRequest(new Request(1, "MYPOS", "{X " + x + ",Y " + y + ",Z " + z + "}"));
                    client.sendRequest(new Request(0, "$OV_PRO", String.valueOf(ov_pro)));
                    torques = client.readJointTorques();
                    jointAngles = client.readJointAngles();
                }
            }

        } catch(IOException ex) {
            Log.i("Kuka connection", "Unable to create client");
        }
        try {
            client.close();
        } catch (IOException ex) {
            Log.i("Kuka connection", "Unable to close client");
        }
    }
}
