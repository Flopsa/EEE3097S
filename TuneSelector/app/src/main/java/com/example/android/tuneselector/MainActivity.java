package com.example.android.tuneselector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothSocket mSocket = null;
    BluetoothDevice mDevice = null;
    private String sequence;
    private boolean firstClick = true;

    public void sendMessage(String message){
        UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //This must match the server on RPi
        try {
            mSocket =  mDevice.createRfcommSocketToServiceRecord(uuid);
            if (!mSocket.isConnected()){
                mSocket.connect();
            }
            OutputStream mOutputStream = mSocket.getOutputStream();
            mOutputStream.write(message.getBytes());
            //Be careful here. Other threads can prematurely close socket. TODO: do socket closing properly.
            //Thread.sleep(1000);
            //mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button a_btn = (Button) findViewById(R.id.a_btn);
        Button b_btn = (Button) findViewById(R.id.b_btn);
        Button undo = (Button) findViewById(R.id.undo_btn);
        FloatingActionButton sendBtn = (FloatingActionButton) findViewById(R.id.send_btn);


        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final class workerThread implements Runnable {
            private String btMessage;

            public workerThread(String message){
                btMessage = message;
            }

            public void run(){
                sendMessage(btMessage);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    mSocket.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        a_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstClick) {
                    sequence = "a,10,10";
                    firstClick = false;
                } else {
                    sequence += ",a,10,10";
                }
            }
        });
        b_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstClick) {
                    sequence = "b,10,10";
                    firstClick = false;
                } else {
                    sequence += ",b,10,10";
                }
            }
        });

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sequence = sequence.substring(0, sequence.length()-8);
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new workerThread(sequence)).start();
            }
        });

        if (!mBluetoothAdapter.isEnabled()){
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0){
            for (BluetoothDevice device: pairedDevices){
                if (device.getName().equals("raspberrypi")){
                    mDevice = device;
                    break;
                }
            }
        }
    }
}
