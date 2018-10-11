package com.example.android.tuneselector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

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

    private String playbackMode = "sequence";
    private String defaultAmplitude = "50";
    private String defaultDuration = "500";

    private EditText amp_value;
    private EditText dur_value;

    public void sendMessage(String message) {
        try {
            connect(mDevice);
            //mOutputStream.flush();
            message = message + "\n";
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
        Toolbar myToolBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(myToolBar);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            System.out.println("Failed to get bluetoothadapter");
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        LayoutInflater inflater;
        View layout;
        final AlertDialog alertDialog;
        AlertDialog.Builder builder;
        switch (item.getItemId()){
            case R.id.action_settings:
                inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                layout = inflater.inflate(R.layout.dialog_settings, (ViewGroup) findViewById(R.id.action_settings));
                builder = new AlertDialog.Builder(this)
                        .setView(layout)
                        .setTitle(R.string.action_setting)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                alertDialog = builder.create();
                alertDialog.show();
                final SeekBar ampSeekbar = (SeekBar)layout.findViewById(R.id.amp_seekbar);
                final SeekBar durSeekbar = (SeekBar)layout.findViewById(R.id.duration_seekbar);
                amp_value = (EditText) layout.findViewById(R.id.amplitude_value);
                dur_value = (EditText)layout.findViewById(R.id.dur_value);
                amp_value.setFocusable(false);
                amp_value.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        amp_value.setFocusableInTouchMode(true);
                        return false;
                    }
                });
                dur_value.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        dur_value.setFocusableInTouchMode(true);
                        return false;
                    }
                });

                dur_value.setFocusable(false);
                ampSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                        //Do something here with new value
                        String sbValue = Integer.toString(progress);
                        defaultAmplitude = sbValue;
                        amp_value.setText(sbValue);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                durSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                        //Do something here with new value
                        String sbValue = Integer.toString(progress);
                        defaultDuration = sbValue;
                        dur_value.setText(sbValue);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                amp_value.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.toString().equals("")) {
                            defaultDuration = "0";
                            ampSeekbar.setProgress(0);
                        } else{
                            defaultAmplitude = s.toString();
                            ampSeekbar.setProgress(Integer.parseInt(s.toString()));
                        }
                    }
                });
                dur_value.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.toString().equals("")){
                           defaultDuration = "0";
                           durSeekbar.setProgress(0);
                        } else{
                            defaultDuration = s.toString();
                            durSeekbar.setProgress(Integer.parseInt(s.toString()));
                        }
                    }
                });
                return true;

            case R.id.default_mode:
                final CharSequence[] modes = {"Note", "Song"};
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.example_songs);
                builder.setSingleChoiceItems(modes, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playbackMode = modes[which].toString();
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog = builder.create();
                alertDialog.show();
                return true;
            case R.id.example_songs:
                final CharSequence[] example_songs = {"Super Mario", "Pirates"};
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.example_songs);
                builder.setSingleChoiceItems(example_songs, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (example_songs[which] == "Super Mario") {
                            sequence = "g,100,1000,_,100,1000,e,100,500,_,100,200,e,100,500,c," +
                                    "100,250,e,1,500,g,100,500";
                        } else if (example_songs[which] == "Pirates") {
                        }
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOutEditText.getVisibility() == View.INVISIBLE){
                            mOutEditText.setVisibility(View.VISIBLE);
                        }
                        mOutEditText.setText(sequence);
                    }
                });
                alertDialog = builder.create();
                alertDialog.show();

            default: return super.onOptionsItemSelected(item);
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
                if (playbackMode.equals("Note")){
                    sequence = "a,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "a,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",a,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
                }
                mOutEditText.setText(sequence);
            }
        });

        a_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (playbackMode.equals("Note")){
                    sequence = "A,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "A,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",A,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
                }
                mOutEditText.setText(sequence);
                return true;
            }
        });
        b_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playbackMode.equals("Note")){
                    sequence = "b,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "b,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",b,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
                }
                mOutEditText.setText(sequence);
            }
        });
        c_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playbackMode.equals("Note")){
                    sequence = "c,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "c,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",c,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
                }
                mOutEditText.setText(sequence);
            }
        });
        c_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (playbackMode.equals("Note")){
                    sequence = "C,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "C,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",C,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
                }
                mOutEditText.setText(sequence);
                return true;
            }
        });
        d_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playbackMode.equals("Note")){
                    sequence = "d,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "d,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",d,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
                }
                mOutEditText.setText(sequence);
            }
        });
        d_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (playbackMode.equals("Note")){
                    sequence = "D,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "D,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",D,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
                }
                mOutEditText.setText(sequence);
                return true;
            }
        });
        e_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playbackMode.equals("Note")){
                    sequence = "e,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "e,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",e,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
                }
                mOutEditText.setText(sequence);
            }
        });
        f_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playbackMode.equals("Note")){
                    sequence = "f,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "f,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",f,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
                }
                mOutEditText.setText(sequence);
            }
        });
        f_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (playbackMode.equals("Note")){
                    sequence = "F,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "F,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",F,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
                }
                mOutEditText.setText(sequence);
                return true;
            }
        });
        g_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playbackMode.equals("Note")){
                    sequence = "g,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "g,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",g,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
                }
                mOutEditText.setText(sequence);
            }
        });
        g_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (playbackMode.equals("Note")){
                    sequence = "G,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "G,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",G,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
                }
                mOutEditText.setText(sequence);
                return true;
            }
        });
        space_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playbackMode.equals("Note")){
                    sequence = "_,"+ defaultAmplitude +","+defaultDuration;
                    new Thread(new workerThread(sequence)).start();
                } else {
                    if (firstClick) {
                        sequence = "_,"+ defaultAmplitude +","+defaultDuration;
                        firstClick = false;
                    } else {
                        sequence = sequence + ",_,"+ defaultAmplitude +","+defaultDuration;
                    }
                }
                if (mOutEditText.getVisibility() == View.INVISIBLE){
                    mOutEditText.setVisibility(View.VISIBLE);
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
                    // Used to test Bit Error Rate
                    sequence = "a,50,2500";
                    for (int i = 0; i < 312; i++){
                        // 313 * 32 =10,016 bits
                        sequence = sequence + ",a,50,2000";
                    }
                    new Thread(new workerThread(sequence)).start();
                    mOutEditText.setVisibility(View.VISIBLE);
                    mOutEditText.setText(sequence);
                }
                else if (sequence.length() > 0) {
                    // Remove all whitespace
                    sequence = sequence.replaceAll("\\s+", "");
                    //            byte[] send = sequence.getBytes();
                    //      mSerialService.write(send);
                    new Thread(new workerThread(sequence)).start();
                    // Strip new line character.
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
