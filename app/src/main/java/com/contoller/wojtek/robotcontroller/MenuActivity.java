package com.contoller.wojtek.robotcontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;


public class MenuActivity extends Activity {

    EditText editTextIP;
    EditText editTextPort;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        editTextIP = findViewById(R.id.editTextIP);
        editTextPort = findViewById(R.id.editTextPort);
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        String proposedIP = null;
        /*try {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();
            proposedIP = InetAddress.getByAddress(ipByteArray).getHostAddress();
            editTextIP.setText(proposedIP);
        } catch (Exception ex) {
            Log.i("MenuActivity", "Unable to get wifi info.");
        }*/
        editTextIP.setText("192.168.1.155");
        editTextPort.setText(7000+"");
    }

    public void onConnect(View view) {


        String IP = editTextIP.getText().toString();
        int port = Integer.valueOf(editTextPort.getText().toString());

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("IP", IP);
        intent.putExtra("Port", port);
        startActivity(intent);

    }
}
