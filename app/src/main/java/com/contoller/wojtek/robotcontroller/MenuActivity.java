package com.contoller.wojtek.robotcontroller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;


public class MenuActivity extends Activity {

    private EditText editText1, editText2, editText3, editText4;
    private EditText editTextPort;

    private final String FILE_NAME = "log.txt";
    private final String LOCAL_PATH = "/RobotController";
    public static final int REQUEST_WRITE_STORAGE = 112;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //editTextIP = findViewById(R.id.editTextIP);
        editText1 = findViewById(R.id.editTextIP1);
        editText2 = findViewById(R.id.editTextIP2);
        editText3 = findViewById(R.id.editTextIP3);
        editText4 = findViewById(R.id.editTextIP4);
        editTextPort = findViewById(R.id.editTextPort);

        try {
            String dataFromFile = readFromFile();
            String[] parts = dataFromFile.split("\\.");
            editText1.setText(parts[0]);
            editText2.setText(parts[1]);
            editText3.setText(parts[2]);
            editText4.setText(parts[3].split(":")[0]);
            String port = parts[3].split(":")[1];
            editTextPort.setText(port);
        } catch (IOException e) {
            Log.i("ReadFromFile", "Unable to read from file");

        }

    }

    public void onConnect(View view) {

        String IP = editText1.getText().toString() + "." +
                editText2.getText().toString() + "." +
                editText3.getText().toString() + "." +
                editText4.getText().toString();
        String port = editTextPort.getText().toString();
        if(isExternalStorageWritable()) {
            if(hasPermissionToWrite(this)) {
                writeToFile(IP + ":" + port);
            }
        } else {
            Log.i("Storage", "External storage is not writable");
        }


        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("IP", IP);
        intent.putExtra("Port", port);
        startActivity(intent);

    }

    private void writeToFile(String data) {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + LOCAL_PATH);
        dir.mkdir();
        File log = new File(dir, FILE_NAME);
        try {
            FileOutputStream fos = new FileOutputStream(log, false);
            PrintWriter pw = new PrintWriter(fos);
            pw.print(data);
            pw.flush();
            pw.close();
            fos.close();
            Log.i("WriteToFile", "Write successful");
        } catch (IOException ex) {
            Log.i("WriteToFile", "Unable to write");
        }
    }

    private String readFromFile() throws IOException{
        File root = android.os.Environment.getExternalStorageDirectory();
        File fileToRead = new File(root.getAbsolutePath() + LOCAL_PATH, FILE_NAME);

        StringBuilder text = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(fileToRead));
        String line;

        while ((line = br.readLine()) != null) {
            text.append(line);
            text.append('\n');
        }
        br.close();

        return text.toString();
    }

    private boolean hasPermissionToWrite(Activity context) {
        boolean hasPermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if(!hasPermission) {
            ActivityCompat.requestPermissions(context,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
        return true;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
