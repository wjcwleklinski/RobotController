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
    private String ov_pro = "50";
    private double[] torques = {0., 0., 0.};
    private double[] jointAngles = {0., 0., 0.};
    private float[] currentAngles;
    public boolean connectionSuccessful = false;

    //todo remove mypos and add curpos and relpos
    //ov callback


    public KukaConnectorThread(String name, String IP, int port) {
        super(name);
        this.IP = IP;
        this.port = port;
    }

    public void setOffsetPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setPosition(float[] position) {
        this.currentAngles = position;
    }

    public void setOv_pro(String ov_pro) {
        this.ov_pro = ov_pro;
    }

    public double[] getTorques() { return torques; }

    public double[] getJointAngles() { return jointAngles; }

    @Override
    public void run() {
        Log.i("KukaThread", Thread.currentThread().getName());
        CrossComClient client = null;
        try {
            client = new CrossComClient(IP, port);
            Log.i("Kuka connection", "Client created");
            connectionSuccessful = true;
            if (client != null) {
                while (!Thread.currentThread().isInterrupted()) {
//                    client.sendRequest(new Request(1, "MYPOS", "{X " + x + ",Y " + y + ",Z " + z + "}"));
                    client.sendRequest(new Request(1, "CURPOS", "{X " + x + ",Y " + y + ",Z " + z + "}"));
                    client.sendRequest(new Request(1, "RELPOS",
                            "{X " + currentAngles[2] + ",Y " + currentAngles[1] + ",Z " + currentAngles[0] + "}"));
                    client.sendRequest(new Request(0, "$OV_PRO", String.valueOf(ov_pro)));
                    torques = client.readJointTorques();
                    jointAngles = client.readJointAngles();
                }
            }

        } catch(IOException ex) {
            Log.i("Kuka connection", "Unable to create client");
        }
        try {
            if(client != null)
                client.close();

        } catch (IOException ex) {
            Log.i("Kuka connection", "Unable to close client");
        }
    }
}
