package com.example.android.tuneselector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //private BluetoothSerialService mSerialService = null;
    private static final UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //This must match the server on RPi

    private BluetoothDevice mDevice = null;
    private BluetoothSocket mSocket = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private OutputStream mOutputStream = null;
    private String sequence;
    private EditText mOutEditText;
    private boolean firstClick = true;

    private Button a_btn;
    private Button b_btn;
    private Button c_btn;
    private Button d_btn;
    private Button e_btn;
    private Button f_btn;
    private Button g_btn;
    private Button space_btn;

    private FloatingActionButton undo_btn;
    private FloatingActionButton send_btn;

    public void sendMessage(String message) {
        try {
            connect(mDevice);
            //mOutputStream.flush();
            mOutputStream.write(message.getBytes("UTF-8"));
            System.out.println("Sending: "+message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            System.out.println("Failed to get bluetoothadapter");
            return;
        }
    }

    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("raspberrypi")) {
                    //          mSerialService.connect(device);
                    mDevice = device;
                    connect(device);
                    break;
                }
            }
        }

        //     if (mSerialService == null){
        setup();
        //   }
    }

    private boolean connect(BluetoothDevice device) {
        // Reset all streams and socket.
        resetConnection();
        if (device == null) {
            //run 'hciconfig' on pi to find your BT Mac address
            try {
                mDevice = mBluetoothAdapter.getRemoteDevice("B8:27:EB:44:D5:50");
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        // Make an RFCOMM binding.
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (Exception e) {
            System.out.println("Failed to create RFCOMM socket from uuid: " + e.getMessage());
            return false;
        }

        try {
            mSocket.connect();
        } catch (Exception e) {
            System.out.println("Failed to connect socket:" + e.getMessage());
            return false;
        }

        try {
            mOutputStream = mSocket.getOutputStream();
        } catch (Exception e) {
            System.out.println("Failed to get socket outputsream.");
            return false;
        }
        return true;
    }

    private void resetConnection() {
        if (mOutputStream != null) {
            try {
                //mOutputStream.flush();
                mOutputStream.close();
            } catch (Exception e) {
            }
            mOutputStream = null;
        }

        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (Exception e) {
            }
            mSocket = null;
        }
    }

    private void setup() {
        a_btn = (Button) findViewById(R.id.a_btn);
        b_btn = (Button) findViewById(R.id.b_btn);
        c_btn = (Button) findViewById(R.id.c_btn);
        d_btn = (Button) findViewById(R.id.d_btn);
        e_btn = (Button) findViewById(R.id.e_btn);
        f_btn = (Button) findViewById(R.id.f_btn);
        g_btn = (Button) findViewById(R.id.g_btn);
        space_btn = (Button) findViewById(R.id.space_btn);
        mOutEditText = (EditText) findViewById(R.id.send_sequence);
        undo_btn = (FloatingActionButton) findViewById(R.id.undo_btn);
        send_btn = (FloatingActionButton) findViewById(R.id.send_btn);

        // mSerialService = new BluetoothSerialService(this);
        // mSerialService.start();

        final class workerThread implements Runnable {
            private String btMessage;

            public workerThread(String message) {
                btMessage = message;
            }

            public void run() {
                sendMessage(btMessage);
            }
        }

        a_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstClick) {
                    sequence = "a,10,10";
                    mOutEditText.setVisibility(View.VISIBLE);
                    firstClick = false;
                } else {
                    sequence += ",a,10,10";
                }
                mOutEditText.setText(sequence);
            }
        });
        b_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstClick) {
                    sequence = "b,10,10";
                    mOutEditText.setVisibility(View.VISIBLE);
                    firstClick = false;
                } else {
                    sequence += ",b,10,10";
                }
                mOutEditText.setText(sequence);
            }
        });
        c_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstClick) {
                    sequence = "c,10,10";
                    mOutEditText.setVisibility(View.VISIBLE);
                    firstClick = false;
                } else {
                    sequence += ",c,10,10";
                }
                mOutEditText.setText(sequence);
            }
        });
        d_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstClick) {
                    sequence = "d,10,10";
                    mOutEditText.setVisibility(View.VISIBLE);
                    firstClick = false;
                } else {
                    sequence += ",d,10,10";
                }
                mOutEditText.setText(sequence);
            }
        });
        e_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstClick) {
                    sequence = "e,10,10";
                    mOutEditText.setVisibility(View.VISIBLE);
                    firstClick = false;
                } else {
                    sequence += ",e,10,10";
                }
                mOutEditText.setText(sequence);
            }
        });
        f_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstClick) {
                    sequence = "f,10,10";
                    mOutEditText.setVisibility(View.VISIBLE);
                    firstClick = false;
                } else {
                    sequence += ",f,10,10";
                }
                mOutEditText.setText(sequence);
            }
        });
        g_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstClick) {
                    sequence = "g,10,10";
                    mOutEditText.setVisibility(View.VISIBLE);
                    firstClick = false;
                } else {
                    sequence += ",g,10,10";
                }
                mOutEditText.setText(sequence);
            }
        });
        space_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstClick) {
                    sequence = "_,0,10";
                    mOutEditText.setVisibility(View.VISIBLE);
                    firstClick = false;
                } else {
                    sequence += ",_,0,10";
                }
                mOutEditText.setText(sequence);
            }
        });

        undo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sequence.length() <= 7) {
                    sequence = "";
                    firstClick = true;
                } else {
                    sequence = sequence.substring(0, sequence.length() - 8);
                }
                mOutEditText.setText(sequence);
            }
        });

        mOutEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sequence = editable.toString();
            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sequence == null || sequence.length() == 0){
                    sequence = "a,10,10";
                    for (int i = 0; i < 313; i++){
                        // 313 * 32 =10,016 bits
                        sequence = sequence + ",a,10,10";
                    }
                    sequence = sequence +"\n";
                    new Thread(new workerThread(sequence)).start();
                    sequence = sequence.replaceAll("\n", "");
                    mOutEditText.setVisibility(View.VISIBLE);
                    mOutEditText.setText(sequence);
                }
                else if (sequence.length() > 0) {
                    sequence = sequence.replaceAll("\\s+", "");
                    sequence = sequence +"\n";
                    //            byte[] send = sequence.getBytes();
                    //      mSerialService.write(send);
                    new Thread(new workerThread(sequence)).start();
                    sequence = sequence.replaceAll("\n", "");
                    mOutEditText.setText(sequence);
                }
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
  /*      if (mSerialService != null){
            mSerialService.stop();
        }*/
    }

    public void onResume() {
        super.onResume();
 /*       if (mSerialService != null){
            if (mSerialService.getState() == BluetoothSerialService.STATE_NONE){
                mSerialService.start();
            }
        }*/
    }
}
